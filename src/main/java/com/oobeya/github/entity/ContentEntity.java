package com.oobeya.github.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "github_repository_contents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String name;

    @Column(length = 1000)
    private String path;

    @Column(length = 255)
    private String sha;

    @Column(length = 100)
    private String type;

    @Column(length = 1000)
    private String downloadUrl;

    @Column(length = 500)
    private String repoName;

    @Column(length = 1000)
    private String content;

    @Column(length = 1000)
    private String encoding;
}