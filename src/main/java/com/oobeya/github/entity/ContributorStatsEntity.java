package com.oobeya.github.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "contributor_stats")
public class ContributorStatsEntity {
    //TODO deneme

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ownerLogin;
    private String repoName;

    private String authorName;
    private String authorEmail;
    private int totalCommits;

    @OneToMany(mappedBy = "contributorStats", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContributorWeekStatsEntity> weeks;
}
