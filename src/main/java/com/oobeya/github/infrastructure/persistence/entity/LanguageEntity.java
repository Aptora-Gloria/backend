package com.oobeya.github.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "github_repository_languages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String repoName;

    @Column(length = 100)
    private String languageName;

    private Long bytes;
}
