package com.oobeya.github.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "github_repository_commits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // todo base entity

    @Column(length = 255)
    private String sha;


    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(length = 255)
    private String authorName;

    @Column(length = 100)
    private String commitDate; // ISO tarih formatı (2025-07-21T12:58:00Z) //todo localdate time olsun

    @Column(length = 500)
    private String repoName;
}
