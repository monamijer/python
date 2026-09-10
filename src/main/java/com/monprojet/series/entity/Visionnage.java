// Visionnage.java
// Join entity: records that a given user has watched a given episode.
// Kept as its own entity (not a plain @ManyToMany) because it carries
// the watch date — an attribute of the relationship itself.
package com.monprojet.series.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "visionnage",
    uniqueConstraints = @UniqueConstraint(columnNames = {"utilisateur_id", "episode_id"})
    // Enforces "one watch record per user per episode" at the database level,
    // as a safety net behind the service-level check.
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visionnage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    @Column(nullable = false)
    private LocalDateTime dateVisionnage;
}