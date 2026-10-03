package com.translator.servlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet pour visualiser un PDF traduit directement dans le navigateur.
 *
 * URL : /pdf-viewer?file=rapport_EN.pdf
 */
@WebServlet("/pdf-viewer")
public class PdfViewerServlet extends HttpServlet {

    private static final String OUTPUT_DIR = "C:/pdf-translator/output/";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String fileName = req.getParameter("file");

        // Sécurité : bloquer les path traversal
        if (fileName == null || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Nom de fichier invalide.");
            return;
        }

        Path filePath = Paths.get(OUTPUT_DIR, fileName);

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Fichier introuvable : " + fileName);
            return;
        }

        byte[] pdfBytes = Files.readAllBytes(filePath);

        resp.setContentType("application/pdf");
        resp.setContentLength(pdfBytes.length);
        // "inline" → affichage dans le navigateur, pas de téléchargement
        resp.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        resp.getOutputStream().write(pdfBytes);
        resp.getOutputStream().flush();
    }
}
