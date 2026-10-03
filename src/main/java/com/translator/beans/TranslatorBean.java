package com.translator.beans;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.translator.dao.GenericDAO;
import com.translator.dao.GenericDAOImpl;
import com.translator.metier.Fichier;
import com.translator.model.TranslationResult;
import com.translator.service.TranslationOrchestrator;
import com.translator.utils.SpringBeansContext;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.primefaces.model.file.UploadedFile;

@ViewScoped
@Named("translatorBean")
public class TranslatorBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(TranslatorBean.class);

    // ── Répertoire de sortie (à adapter selon votre environnement) ────
    private static final String OUTPUT_DIR = "C:/pdf-translator/output/";
    // Sous Linux : "/var/pdf-translator/output/"

    @Inject
    private TranslationOrchestrator orchestrator;

    private GenericDAO<Fichier> dao;
    private UserInfo userInfo;

    // ─── Fichier ──────────────────────────────────────────────────────
    private UploadedFile uploadedFile;

    // ─── Nom personnalisé saisi par l'utilisateur ─────────────────────
    private String customFileName;

    // ─── État ─────────────────────────────────────────────────────────
    private boolean processing  = false;
    private boolean done        = false;
    private boolean saved       = false;   // true après enregistrement réussi
    private boolean error       = false;
    private String  errorMessage;
    private String  savedPath;             // chemin affiché à l'utilisateur après sauvegarde

    private TranslationResult result;

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        log.info("TranslatorBean initialisé");
        dao      = new GenericDAOImpl<>(Fichier.class);
        userInfo = SpringBeansContext.getContext().getBean(UserInfo.class);
    }

    // ─────────────────────────────────────────────────────────────────
    //  Étape 1 : Traduction (inchangée)
    // ─────────────────────────────────────────────────────────────────

    public void startTranslation() {

        if (uploadedFile == null
                || uploadedFile.getContent() == null
                || uploadedFile.getContent().length == 0) {
            addError("Veuillez sélectionner un fichier PDF.");
            return;
        }
        if (!uploadedFile.getFileName().toLowerCase().endsWith(".pdf")) {
            addError("Seuls les fichiers PDF sont acceptés.");
            return;
        }

        processing   = true;
        done         = false;
        saved        = false;
        error        = false;
        errorMessage = null;
        result       = null;
        savedPath    = null;

        try {
            result = orchestrator.translate(
                uploadedFile.getContent(),
                uploadedFile.getFileName(),
                null
            );

            // Pré-remplir le champ nom avec le nom par défaut généré
            customFileName = result.getTranslatedFileName()
                .replaceAll("(?i)\\.pdf$", ""); // sans extension, l'utilisateur voit "rapport_EN"

            done = true;

        } catch (Exception e) {
            error        = true;
            errorMessage = e.getMessage();
            log.error("Erreur pipeline", e);
        } finally {
            processing = false;
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Étape 2 : Enregistrement dans le répertoire + base de données
    // ─────────────────────────────────────────────────────────────────

    public void saveTranslation() {
        if (result == null || result.getTranslatedPdfBytes() == null) {
            addError("Aucune traduction disponible à enregistrer.");
            return;
        }

        // ── Construire le nom final du fichier ────────────────────────
        String name = (customFileName != null) ? customFileName.trim() : "";
        if (name.isEmpty()) {
            name = result.getTranslatedFileName();
        }
        if (!name.toLowerCase().endsWith(".pdf")) {
            name = name + ".pdf";
        }
        result.setTranslatedFileName(name);

        // ── Créer le répertoire si nécessaire ─────────────────────────
        try {
            Path outputDir = Paths.get(OUTPUT_DIR);
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
                log.info("Répertoire créé : {}", outputDir.toAbsolutePath());
            }

            // ── Écrire le PDF sur le disque ───────────────────────────
            Path filePath = outputDir.resolve(name);
            Files.write(filePath, result.getTranslatedPdfBytes(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            savedPath = filePath.toAbsolutePath().toString();
            log.info("PDF traduit enregistré : {}", savedPath);

        } catch (IOException e) {
            error        = true;
            errorMessage = "Erreur lors de l'enregistrement : " + e.getMessage();
            log.error("Erreur écriture fichier", e);
            return;
        }

        // ── Enregistrer en base de données ────────────────────────────
        try {
            Fichier f = new Fichier();
            f.setNomFichier(uploadedFile.getFileName());          // nom original FR
            f.setNomFichierEN(result.getTranslatedFileName()); // nom traduit EN
//            f.setCheminFichier(savedPath);                         // chemin sur le disque
//            f.setDateTraduction(LocalDateTime.now());
            f.setUtilisateur(userInfo.getCurrentUser());

            dao.persist(f);
            log.info("Enregistrement BDD OK : {} → {}", f.getNomFichier(), f.getNomFichierEN());

        } catch (Exception e) {
            // Le fichier est sauvegardé sur disque mais la BDD a échoué → avertir
            addError("Fichier enregistré mais erreur BDD : " + e.getMessage());
            log.error("Erreur persistence BDD", e);
            saved = true; // le fichier existe quand même
            return;
        }

        saved = true;
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Fichier enregistré avec succès !", savedPath));
    }

    // ─────────────────────────────────────────────────────────────────
    //  Reset
    // ─────────────────────────────────────────────────────────────────

    public void reset() {
        result         = null;
        uploadedFile   = null;
        customFileName = null;
        processing     = false;
        done           = false;
        saved          = false;
        error          = false;
        errorMessage   = null;
        savedPath      = null;
    }

    // ─────────────────────────────────────────────────────────────────
    //  Utilitaires
    // ─────────────────────────────────────────────────────────────────

    public String getFormattedProcessingTime() {
        if (result == null) return "";
        long ms = result.getProcessingTimeMs();
        return ms < 1000 ? ms + " ms" : String.format("%.1f s", ms / 1000.0);
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance()
            .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    // ─────────────────────────────────────────────────────────────────
    //  Getters / Setters
    // ─────────────────────────────────────────────────────────────────

    public UploadedFile getUploadedFile()        { return uploadedFile; }
    public void setUploadedFile(UploadedFile f)  { this.uploadedFile = f; }

    public String getCustomFileName()            { return customFileName; }
    public void setCustomFileName(String n)      { this.customFileName = n; }

    public boolean isProcessing()    { return processing; }
    public boolean isDone()          { return done; }
    public boolean isSaved()         { return saved; }
    public boolean isError()         { return error; }
    public String  getErrorMessage() { return errorMessage; }
    public String  getSavedPath()    { return savedPath; }
    public TranslationResult getResult() { return result; }
}
