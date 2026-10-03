package com.translator.service;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.translator.model.TranslationResult;
import com.translator.model.TranslationStatus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Orchestrateur simplifié — utilise l'API DeepL Document directement.
 *
 * Ancien pipeline (abandonné) :
 *   PDF → HTML (PDF2DOM) → Traduction HTML → PDF (OpenHTMLtoPDF)
 *   Problèmes : mise en forme perdue, page vierge, complexité élevée
 *
 * Nouveau pipeline :
 *   PDF bytes → DeepL Document API → PDF traduit bytes
 *   Avantages : polices, couleurs, tableaux, images 100% préservés
 */
@ApplicationScoped
public class TranslationOrchestrator implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(TranslationOrchestrator.class);

    @Inject
    private PdfToHtmlService pdfToHtmlService; // Conservé uniquement pour getPageCount / getWordCount

    @Inject
    private DeepLDocumentTranslationService deepLDocumentService;

    /**
     * Exécute la traduction PDF FR → EN via DeepL Document API.
     *
     * @param pdfBytes  bytes du PDF source
     * @param fileName  nom du fichier (ex: "rapport.pdf")
     * @param callback  listener de progression (peut être null)
     * @return          TranslationResult avec le PDF traduit
     */
    public TranslationResult translate(byte[] pdfBytes, String fileName,
                                       ProgressCallback callback) throws Exception {

        long startTime = System.currentTimeMillis();
        TranslationResult result = new TranslationResult();
        result.setOriginalFileName(fileName);

        try {
            // ── Étape 1 : Métadonnées du PDF ──────────────────────────────
            notify(callback, TranslationStatus.UPLOADING, "Analyse du PDF...", 10);
            int pageCount = pdfToHtmlService.getPageCount(pdfBytes);
            int wordCount = pdfToHtmlService.getWordCount(pdfBytes);
            result.setPageCount(pageCount);
            result.setWordCount(wordCount);
            log.info("PDF analysé : {} pages, {} mots", pageCount, wordCount);

            // ── Étape 2 : Traduction directe via DeepL Document API ───────
            // DeepL reçoit le PDF brut et retourne un PDF traduit
            // avec toute la mise en forme préservée (polices, couleurs, tableaux)
            notify(callback, TranslationStatus.TRANSLATING, "Traduction via DeepL...", 30);
            byte[] translatedPdfBytes = deepLDocumentService.translatePdf(
                pdfBytes, fileName, callback
            );
            result.setTranslatedPdfBytes(translatedPdfBytes);

            // Nom du fichier de sortie
            String baseName = fileName.replaceAll("(?i)\\.pdf$", "");
            result.setTranslatedFileName(baseName + "_EN.pdf");

            result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            notify(callback, TranslationStatus.DONE, "Traduction terminée !", 100);
            log.info("Pipeline terminé en {}ms", result.getProcessingTimeMs());

        } catch (Exception e) {
            notify(callback, TranslationStatus.ERROR, "Erreur : " + e.getMessage(), -1);
            log.error("Erreur dans le pipeline de traduction", e);
            throw e;
        }

        return result;
    }

    private void notify(ProgressCallback callback, TranslationStatus status,
                        String message, int progress) {
        if (callback != null) {
            callback.onProgress(status, message, progress);
        }
    }

    @FunctionalInterface
    public interface ProgressCallback {
        void onProgress(TranslationStatus status, String message, int progressPercent);
    }
}
