import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GameService, GameResponse } from './services/game.service'; 

import { Component, ChangeDetectorRef } from '@angular/core'; // Ajoute ChangeDetectorRef

declare var confetti: any;

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  gameState: GameResponse | null = null;
  allPlayerNames: string[] = [];      // Liste complète venant du back
  filteredNames: string[] = [];       // Liste filtrée à afficher
  showSuggestions: boolean = false;
  history: any[] = []; // Ajoute cette variable
  

  constructor(private gameService: GameService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.refreshHistory();
  }

  refreshHistory() {
    this.gameService.getHistory().subscribe(data => this.history = data);
  }

  onStart() {
    const unlockAudio = new Audio();
    unlockAudio.src = "sounds/success.mp3";
    unlockAudio.volume = 0;
    unlockAudio.play().then(() => unlockAudio.pause());
    this.gameService.getPlayerNames().subscribe(names => this.allPlayerNames = names);
    
    this.gameService.startGame().subscribe(res => {
      this.gameState = res;
      this.cdr.detectChanges();
    });
  }

  // Fonction pour filtrer la liste pendant la saisie
  filterPlayers(event: any) {
    const query = event.target.value.toLowerCase();
    if (query.length > 1) { // On commence à suggérer après 2 caractères
      this.filteredNames = this.allPlayerNames.filter(name => 
        name.toLowerCase().includes(query)
      );
      this.showSuggestions = true;
    } else {
      this.showSuggestions = false;
    }
  }

  selectName(name: string, inputElement: HTMLInputElement) {
    inputElement.value = name; // Remplit l'input
    this.showSuggestions = false;
    this.onGuess(name); // Envoie directement la réponse
    inputElement.value = ''; // Vide l'input après l'envoi
  }

  onGuess(name: string) {
    const guess = name.trim();
    if (!guess) return;

    this.gameService.makeGuess(guess).subscribe({
      next: (res) => {
        this.gameState = res;
        this.cdr.detectChanges();
        if (res.gameOver) {
          if (res.win) {
            this.playAudio('success');
            confetti({
              particleCount: 150,
              spread: 70,
              origin: { y: 0.6 },
              colors: ['#C8102E', '#ffffff', '#00B2A9'] // Couleurs de Liverpool !
            });
          }
          else{
            this.playAudio('fail');
          }
          this.refreshHistory();
        }
      },
      error: (err) => {
        console.error("Erreur Backend :", err);
      }
    });
  }

  getCorrectPlayerName(): string {
    if (!this.gameState || !this.allPlayerNames) return '';
    // Puisqu'on ne renvoie pas le nom complet du joueur final dans GameResponse,
    // on peut soit l'ajouter au GameResponse côté Back,
    // soit le deviner en espérant que la réponse précédente l'avait.
    // La méthode la plus sûre est de l'AJOUTER dans GameResponse.java.
    return this.gameState.message.split(': ')[1] || ''; // Extrait le nom si le message est "C'était : Salah"
  }

  handleImageError(event: any) {
    // Image générique si le joueur n'est pas trouvé sur FotMob
    event.target.src = 'https://images.fotmob.com/image_resources/playerimages/unknown.png';
    // Ou simplement masquer l'image : event.target.style.display = 'none';
  }

  playAudio(type: 'success' | 'fail') {
    let audio = new Audio();
    audio.src = type === 'success' ? "sounds/success.mp3" : "sounds/fail.mp3";    audio.load();
    audio.volume = 0.1;
    audio.play().catch(err => console.log("Erreur audio (interaction requise) :", err));
  }
}