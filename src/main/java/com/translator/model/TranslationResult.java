package com.translator.model;

import java.io.Serializable;

public class TranslationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String originalFileName;
    private String translatedFileName;
    private byte[] translatedPdfBytes;
    private String intermediateHtml;
    private String translatedHtml;
    private long processingTimeMs;
    private int pageCount;
    private int wordCount;

    // Constructors
    public TranslationResult() {}

    // Getters & Setters
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getTranslatedFileName() { return translatedFileName; }
    public void setTranslatedFileName(String translatedFileName) { this.translatedFileName = translatedFileName; }

    public byte[] getTranslatedPdfBytes() { return translatedPdfBytes; }
    public void setTranslatedPdfBytes(byte[] translatedPdfBytes) { this.translatedPdfBytes = translatedPdfBytes; }

    public String getIntermediateHtml() { return intermediateHtml; }
    public void setIntermediateHtml(String intermediateHtml) { this.intermediateHtml = intermediateHtml; }

    public String getTranslatedHtml() { return translatedHtml; }
    public void setTranslatedHtml(String translatedHtml) { this.translatedHtml = translatedHtml; }

    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }

    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }
}
