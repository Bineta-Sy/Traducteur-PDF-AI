package com.translator.model;

public enum TranslationStatus {
    IDLE,
    UPLOADING,
    CONVERTING_TO_HTML,
    TRANSLATING,
    CONVERTING_TO_PDF,
    DONE,
    ERROR
}
