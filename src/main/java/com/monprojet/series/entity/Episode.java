// Episode.java
package com.monprojet.series.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "episode")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer numero;

    private String titre;

    private Integer dureeMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saison_id", nullable = false)
    private Saison saison;
}