package com.optimetric.github.application.dto;

/**
 * Tetikleme use case çıktısı: durum ve isteğe bağlı mesaj.
 */
public record TriggerSyncResult(TriggerSyncStatus status, String message) {

    public enum TriggerSyncStatus {
        /** Sync job'ları kuyruğa alındı. */
        TRIGGERED,
        /** Repo zaten hazır. */
        ALREADY_READY,
        /** Sync zaten devam ediyor. */
        SYNC_ALREADY_RUNNING,
        /** Repo bulunamadı. */
        REPO_NOT_FOUND
    }

    public static TriggerSyncResult triggered(String message) {
        return new TriggerSyncResult(TriggerSyncStatus.TRIGGERED, message);
    }

    public static TriggerSyncResult alreadyReady() {
        return new TriggerSyncResult(TriggerSyncStatus.ALREADY_READY, "Repo zaten hazır.");
    }

    public static TriggerSyncResult syncAlreadyRunning() {
        return new TriggerSyncResult(TriggerSyncStatus.SYNC_ALREADY_RUNNING, "Sync zaten devam ediyor.");
    }

    public static TriggerSyncResult repoNotFound(String message) {
        return new TriggerSyncResult(TriggerSyncStatus.REPO_NOT_FOUND, message);
    }
}
