package com.optimetric.metrics.domain.model;

import java.util.List;

// Kullanıcı dil çeşitliliği girdisi; repo başına bir kayıt.
public record UserLanguageDiversityInput(List<RepoLanguagesRaw> repos) {
    public UserLanguageDiversityInput {
        repos = repos != null ? List.copyOf(repos) : List.of();
    }

    // Tek repo için diller (isim + byte).
    public record RepoLanguagesRaw(List<LanguageRaw> languages) {
        public RepoLanguagesRaw {
            languages = languages != null ? List.copyOf(languages) : List.of();
        }
    }
}
