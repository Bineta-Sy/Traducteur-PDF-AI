package com.translator.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Traduction directe de documents PDF via l'API DeepL Document.
 *
 * Avantages vs pipeline HTML :
 *  - Mise en forme 100% préservée (polices, couleurs, tableaux, images)
 *  - Pas de conversion PDF→HTML→PDF
 *  - Gestion native des tableaux, colonnes, en-têtes/pieds de page
 *
 * Pipeline DeepL Document :
 *   1. POST /v2/document         → upload du PDF + obtenir document_id + document_key
 *   2. GET  /v2/document/{id}    → polling jusqu'à status = "done"
 *   3. POST /v2/document/{id}/result → télécharger le PDF traduit
 *
 * Limites API DeepL Free :
 *  - 5 Mo max par document
 *  - Formats supportés : PDF, DOCX, PPTX, XLSX, TXT, HTML
 *
 * Doc officielle : https://developers.deepl.com/docs/api-reference/document
 */
@ApplicationScoped
public class DeepLDocumentTranslationService {

    private static final Logger log = LoggerFactory.getLogger(DeepLDocumentTranslationService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String DEEPL_API_KEY = "0521fc24-9718-4cf4-9c66-3508a24a61bf:fx";
    private static final String DEEPL_BASE_URL = "https://api-free.deepl.com/v2";

    // Intervalle de polling (ms) et timeout max
    private static final int POLL_INTERVAL_MS = 3_000;
    private static final int POLL_TIMEOUT_MS  = 300_000; // 5 minutes

    /**
     * Traduit un PDF FR → EN en passant directement les bytes à DeepL.
     * La mise en forme (polices, couleurs, tableaux) est intégralement préservée.
     *
     * @param pdfBytes  bytes du PDF source
     * @param fileName  nom du fichier (ex: "rapport.pdf")
     * @param callback  progression optionnelle
     * @return          bytes du PDF traduit
     */
    public byte[] translatePdf(byte[] pdfBytes, String fileName,
                               TranslationOrchestrator.ProgressCallback callback) throws Exception {

        notify(callback, "Envoi du document à DeepL...", 20);

        // ── Étape 1 : Upload du document ──────────────────────────────────
        String[] uploadResult = uploadDocument(pdfBytes, fileName);
        String documentId  = uploadResult[0];
        String documentKey = uploadResult[1];
        log.info("Document uploadé. ID={}", documentId);

        // ── Étape 2 : Polling jusqu'à traduction terminée ─────────────────
        notify(callback, "Traduction en cours sur les serveurs DeepL...", 50);
        waitForTranslation(documentId, documentKey);
        log.info("Traduction terminée pour ID={}", documentId);

        // ── Étape 3 : Téléchargement du PDF traduit ───────────────────────
        notify(callback, "Téléchargement du PDF traduit...", 85);
        byte[] translatedBytes = downloadDocument(documentId, documentKey);
        log.info("PDF traduit téléchargé : {} octets", translatedBytes.length);

        return translatedBytes;
    }

    // ── Étape 1 : Upload ─────────────────────────────────────────────────

    /**
     * Envoie le PDF à DeepL via multipart/form-data.
     * Retourne [document_id, document_key].
     */
    private String[] uploadDocument(byte[] pdfBytes, String fileName) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost request = new HttpPost(DEEPL_BASE_URL + "/document");
            request.setHeader("Authorization", "DeepL-Auth-Key " + DEEPL_API_KEY);

            HttpEntity multipart = MultipartEntityBuilder.create()
                .addBinaryBody(
                    "file",
                    pdfBytes,
                    ContentType.create("application/pdf"),
                    fileName
                )
                .addTextBody("source_lang", "FR")
                .addTextBody("target_lang", "EN-US")
                // Optionnel : préserver la mise en forme (activé par défaut pour PDF)
                .addTextBody("formality", "default")
                .build();

            request.setEntity(multipart);

            try (CloseableHttpResponse response = client.execute(request)) {
                int status = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity(), "UTF-8");

                if (status != 200) {
                    throw new Exception("DeepL upload erreur " + status + " : " + body);
                }

                JsonNode json = MAPPER.readTree(body);
                return new String[]{
                    json.get("document_id").asText(),
                    json.get("document_key").asText()
                };
            }
        }
    }

    // ── Étape 2 : Polling ────────────────────────────────────────────────

    /**
     * Interroge DeepL toutes les POLL_INTERVAL_MS ms jusqu'à status = "done".
     * Statuts possibles : "queued", "translating", "done", "error"
     */
    private void waitForTranslation(String documentId, String documentKey) throws Exception {
        long start = System.currentTimeMillis();

        while (true) {
            if (System.currentTimeMillis() - start > POLL_TIMEOUT_MS) {
                throw new Exception("Timeout : DeepL n'a pas terminé dans les " + (POLL_TIMEOUT_MS / 1000) + "s");
            }

            String status = checkStatus(documentId, documentKey);
            log.debug("DeepL status pour {} : {}", documentId, status);

            switch (status) {
                case "done":
                    return;
                case "error":
                    throw new Exception("DeepL a retourné une erreur pour le document " + documentId);
                case "queued":
                case "translating":
                    Thread.sleep(POLL_INTERVAL_MS);
                    break;
                default:
                    log.warn("Statut DeepL inconnu : {}", status);
                    Thread.sleep(POLL_INTERVAL_MS);
            }
        }
    }

    /**
     * Appelle GET /v2/document/{id} et retourne le statut.
     */
    private String checkStatus(String documentId, String documentKey) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            String url = DEEPL_BASE_URL + "/document/" + documentId;
            HttpPost request = new HttpPost(url);
            request.setHeader("Authorization", "DeepL-Auth-Key " + DEEPL_API_KEY);
            request.setHeader("Content-Type", "application/json");

            String body = "{\"document_key\":\"" + documentKey + "\"}";
            request.setEntity(new org.apache.http.entity.StringEntity(body,
                ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                JsonNode json = MAPPER.readTree(responseBody);

                // Logguer la progression si disponible
                if (json.has("seconds_remaining")) {
                    log.info("DeepL : ~{}s restantes", json.get("seconds_remaining").asInt());
                }
                if (json.has("billed_characters")) {
                    log.info("DeepL : {} caractères facturés", json.get("billed_characters").asInt());
                }

                return json.get("status").asText();
            }
        }
    }

    // ── Étape 3 : Download ───────────────────────────────────────────────

    /**
     * Télécharge le PDF traduit via POST /v2/document/{id}/result.
     */
    private byte[] downloadDocument(String documentId, String documentKey) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            String url = DEEPL_BASE_URL + "/document/" + documentId + "/result";
            HttpPost request = new HttpPost(url);
            request.setHeader("Authorization", "DeepL-Auth-Key " + DEEPL_API_KEY);
            request.setHeader("Content-Type", "application/json");

            String body = "{\"document_key\":\"" + documentKey + "\"}";
            request.setEntity(new org.apache.http.entity.StringEntity(body,
                ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(request)) {
                int status = response.getStatusLine().getStatusCode();

                if (status != 200) {
                    String err = EntityUtils.toString(response.getEntity(), "UTF-8");
                    throw new Exception("DeepL download erreur " + status + " : " + err);
                }

                // Lire les bytes du PDF directement depuis le stream
                try (InputStream is = response.getEntity().getContent();
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, read);
                    }
                    return baos.toByteArray();
                }
            }
        }
    }

    private void notify(TranslationOrchestrator.ProgressCallback callback,
                        String message, int progress) {
        if (callback != null) {
            callback.onProgress(null, message, progress);
        }
    }
}
