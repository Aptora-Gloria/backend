package com.optimetric.github.application.dto;

/**
 * Tetikleme use case giriş komutu: login ve repo fullName.
 */
public record TriggerSyncCommand(String login, String fullName) {
}
