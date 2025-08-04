package com.oobeya.github.dto.request;

public record ContentInitRequest(
        String owner,
        String repo,
        String path // boş veya null gönderilebilir
) {}
