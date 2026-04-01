package com.whoareyou.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data 
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    private Long id;
    private String name;
    private String position;
    private String nationality;
    private String league;
    private String club; 
}