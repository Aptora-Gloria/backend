package com.oobeya.github.dto;

public record ContentInitRequest(
        String owner,
        String repo,
        String path // boş veya null gönderilebilir
) {}
