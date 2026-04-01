package com.whoareyou.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameResponse {
    private String message;
    private String hint;
    private int currentStep;
    private boolean gameOver;
    private boolean win;
    private Long playerId;
}