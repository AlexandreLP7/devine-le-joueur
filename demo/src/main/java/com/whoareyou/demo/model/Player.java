package com.whoareyou.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data // Génère getters, setters, toString grâce à Lombok
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    private Long id;

    private String name;
    private String position;    // ex: Attaquant
    private String nationality; // ex: France
    private String league;      // ex: Ligue 1
    private String club;        // ex: PSG
}