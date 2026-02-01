package com.optimetric.github.application.dto;

/**
 * Sonuç DTO: repo için sync tetikleme (analyze) sonucu.
 */
public record AnalyzeResult(AnalyzeState state) {

    public enum AnalyzeState {
        /** Sync job'ları kuyruğa alındı. */
        SYNC_STARTED,
        /** Repo zaten hazır; job kuyruğa alınmadı. */
        ALREADY_READY,
        /** Sync zaten devam ediyor (PENDING/RUNNING job var). */
        SYNC_ALREADY_RUNNING
    }
}
