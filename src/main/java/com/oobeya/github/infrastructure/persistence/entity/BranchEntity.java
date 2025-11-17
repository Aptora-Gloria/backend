package com.oobeya.github.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "github_branches",
    uniqueConstraints = {
        @jakarta.persistence.UniqueConstraint(
            name = "uk_branch_repo_name",
            columnNames = {"repo_id", "name"}
        )
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id", referencedColumnName = "id", nullable = false)
    private RepositoryEntity repository;


}
