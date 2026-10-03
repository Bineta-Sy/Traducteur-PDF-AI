package com.translator.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Endpoint REST pour visualiser un PDF traduit dans le navigateur.
 *
 * URL : GET /api/pdf/{nomFichier}
 * Ex  : GET /api/pdf/rapport_EN.pdf
 *
 * Le header "Content-Disposition: inline" force l'affichage
 * dans le navigateur (viewer PDF intégré) plutôt que le téléchargement.
 */
@jakarta.ws.rs.Path("/pdf")
public class PdfViewerResource {

    private static final String OUTPUT_DIR = "C:/pdf-translator/output/";
    // Sous Linux : "/var/pdf-translator/output/"

    @GET
    @jakarta.ws.rs.Path("/{fileName}")
    @Produces("application/pdf")
    public Response viewPdf(@PathParam("fileName") String fileName) {

        // Sécurité : interdire les chemins relatifs (ex: ../../etc/passwd)
        if (fileName == null || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return Response.status(Status.BAD_REQUEST)
                .entity("Nom de fichier invalide.").build();
        }

        Path filePath = Paths.get(OUTPUT_DIR, fileName);

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            return Response.status(Status.NOT_FOUND)
                .entity("Fichier introuvable : " + fileName).build();
        }

        try {
            byte[] pdfBytes = Files.readAllBytes(filePath);

            return Response.ok(pdfBytes)
                .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                .header("Content-Type", "application/pdf")
                .build();

        } catch (IOException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity("Erreur lecture fichier : " + e.getMessage()).build();
        }
    }
}
