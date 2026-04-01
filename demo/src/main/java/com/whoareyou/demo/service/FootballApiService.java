package com.whoareyou.demo.service;

import com.whoareyou.demo.model.Player;
import com.whoareyou.demo.repository.PlayerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class FootballApiService {

    private final PlayerRepository playerRepository;
    private final WebClient webClient;

    public FootballApiService(PlayerRepository playerRepository, WebClient.Builder webClientBuilder) {
        this.playerRepository = playerRepository;
        this.webClient = webClientBuilder.baseUrl("https://free-api-live-football-data.p.rapidapi.com").build();
    }

    @PostConstruct
    public void importPlayers() {
        this.webClient.get()
            .uri("/football-get-list-player?teamid=8650")
            .header("x-rapidapi-key", "74b4af44cemshed4dceec9bd51cap139fc1jsn1fdb96acaa83")
            .header("x-rapidapi-host", "free-api-live-football-data.p.rapidapi.com")
            .retrieve()
            .bodyToMono(String.class)
            .subscribe(this::parseAndSavePlayers, 
                       err -> System.err.println("Erreur API : " + err.getMessage()));
    }

    private void parseAndSavePlayers(String jsonResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            
            // On descend dans l'arborescence : response -> list -> squad
            JsonNode squadNode = root.path("response").path("list").path("squad");

            List<Player> playersToSave = new ArrayList<>();

            for (JsonNode group : squadNode) {
                String title = group.path("title").asText();

                // 🛑 CONDITION : On ignore le groupe "coach"
                if ("coach".equalsIgnoreCase(title)) {
                    continue;
                }

                // On boucle sur les membres du groupe (keepers, defenders, etc.)
                JsonNode members = group.path("members");
                for (JsonNode m : members) {
                    Player player = new Player();
                    player.setId(m.get("id").asLong());
                    player.setName(m.path("name").asText());
                    player.setNationality(m.path("cname").asText()); // Utilise cname pour le nom complet du pays
                    
                    // On utilise le titre du groupe pour la position (ex: keepers -> Gardien)
                    player.setPosition(translatePosition(title));
                    
                    player.setClub("Liverpool");
                    player.setLeague("Premier League");

                    playersToSave.add(player);
                }
            }

            playerRepository.saveAll(playersToSave);
            System.out.println(">>> IMPORTATION TERMINÉE : " + playersToSave.size() + " joueurs ajoutés !");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du parsing : " + e.getMessage());
        }
    }

    private String translatePosition(String title) {
        return switch (title.toLowerCase()) {
            case "keepers" -> "Gardien";
            case "defenders" -> "Défenseur";
            case "midfielders" -> "Milieu";
            case "attackers" -> "Attaquant";
            default -> title;
        };
    }
}