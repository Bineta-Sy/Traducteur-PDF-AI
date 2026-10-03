package com.translator.service;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.pdfdom.PDFDomTree;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service de conversion PDF → HTML via PDF2DOM.
 * Utilise la bibliothèque pdf2dom (CSSBox) pour transformer
 * un PDF en DOM HTML fidèle au mise en page originale.
 */
@ApplicationScoped
public class PdfToHtmlService {

    private static final Logger log = LoggerFactory.getLogger(PdfToHtmlService.class);
    /**
     * Compte le nombre de pages d'un PDF.
     */
    public int getPageCount(byte[] pdfBytes) throws IOException {
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            return document.getNumberOfPages();
        }
    }

    /**
     * Extrait le texte brut du PDF pour compter les mots.
     */
    public int getWordCount(byte[] pdfBytes) throws IOException {
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
            String text = stripper.getText(document);
            if (text == null || text.trim().isEmpty()) return 0;
            return text.trim().split("\\s+").length;
        }
    }
}
