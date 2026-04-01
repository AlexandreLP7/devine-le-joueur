package com.whoareyou.demo.controller;

import com.whoareyou.demo.model.Player;
import com.whoareyou.demo.model.GameResponse;
import com.whoareyou.demo.repository.PlayerRepository;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "https://devine-le-joueur.netlify.app")
public class GameController {

    private final PlayerRepository playerRepository;
    
    private Player targetPlayer; 
    private int step = 1;

    private List<Map<String, Object>> gameHistory = new ArrayList<>();

    public GameController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @GetMapping("/start")
    public GameResponse startGame() {
        List<Player> allPlayers = playerRepository.findAll();
        if (allPlayers.isEmpty()) {
            return new GameResponse("Base vide", "Aucun joueur trouvé en base de données.", 0, true, false,targetPlayer.getId());
        }
 
        Collections.shuffle(allPlayers);
        this.targetPlayer = allPlayers.get(0);
        this.step = 1;
        
        return new GameResponse(
            "Nouvelle partie !", 
            "Indice 1 (Poste) : " + targetPlayer.getPosition(), 
            1, false, false,targetPlayer.getId()
        );
    }

    @PostMapping("/guess")
    public GameResponse makeGuess(@RequestParam String name) {
        System.err.println("Target Player: " + targetPlayer.getName());
        if (targetPlayer == null) {
            return new GameResponse("Erreur", "Veuillez démarrer une partie.", 0, true, false,targetPlayer.getId());
        }

        if (name.trim().equalsIgnoreCase(targetPlayer.getName())) {
            String fullName = targetPlayer.getName();
            Long savedId = targetPlayer.getId(); // On sauvegarde l'ID AVANT le reset
            addToHistory(fullName, true);
            targetPlayer = null; // Maintenant on peut faire le reset
            return new GameResponse("BRAVO !", "C'était bien " + fullName, step, true, true, savedId);
        }

        this.step++;

        if (step == 2) {
            return new GameResponse("Faux !", "Indice 2 (Nationalité) : " + targetPlayer.getNationality(), 2, false, false,targetPlayer.getId());
        } else if (step == 3) {
            return new GameResponse("Toujours pas !", "Indice 3 (Championnat) : " + targetPlayer.getLeague(), 3, false, false,targetPlayer.getId());
        } else if (step == 4) {
            return new GameResponse("Dernière chance !", "Indice 4 (Club) : " + targetPlayer.getClub(), 4, false, false,targetPlayer.getId());
        } else {
            String correctName = targetPlayer.getName();
            Long savedId = targetPlayer.getId();
            addToHistory(correctName, false);
            targetPlayer = null; 
            return new GameResponse("PERDU !", "Le joueur était : " + correctName, 5, true, false,savedId);
        }
    }

    @GetMapping("/player-names")
    public List<String> getAllPlayerNames() {
        return playerRepository.findAll().stream()
                .map(Player::getName)
                .distinct()
                .sorted() 
                .collect(Collectors.toList());
    }

    private void addToHistory(String name, boolean win) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("name", name);
        entry.put("win", win);
        gameHistory.add(0, entry);
        if (gameHistory.size() > 10) {
            gameHistory.remove(10);
        }
    }

    @GetMapping("/history")
    public List<Map<String, Object>> getHistory() {
        return gameHistory;
    }

}