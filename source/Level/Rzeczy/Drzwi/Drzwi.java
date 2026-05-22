package Game; // Deklaracja pakietu Game

import godot.annotation.*; // Import adnotacji Godot
import godot.api.*; // Import klas API Godot (RigidBody3D, AnimationPlayer itp.)
import godot.core.*; // Import typów rdzeniowych Godot

@RegisterClass // Rejestruje klasę jako klasę Godot widoczną w edytorze
public class Drzwi extends RigidBody3D // Klasa drzwi dziedziczy po RigidBody3D (fizyczne ciało)
{
	@RegisterProperty @Export public AnimationPlayer animationPlayer; // Referencja do AnimationPlayer – ustawiana w edytorze

	private boolean toggle = false; // Aktualny stan drzwi: false = zamknięte, true = otwarte
	private boolean interactable = true; // Czy gracz może teraz wejść w interakcję z drzwiami
	private double timer = 0.0; // Licznik czasu blokady interakcji (odlicza w dół)
	private boolean waiting = false; // Czy aktualnie trwa odliczanie blokady

	@RegisterFunction
	public void _process(double delta) // Wywoływany co klatkę – obsługuje timer blokady
	{
		if (waiting) { // Jeśli trwa odliczanie blokady
			timer -= delta; // Odejmuje czas klatki od licznika
			if (timer <= 0.0) { // Jeśli odliczanie dobiegło końca
				interactable = true; // Odblokowuje możliwość interakcji
				waiting = false; // Zatrzymuje odliczanie
			}
		}
	}

	@RegisterFunction
	public void interact() // Wywoływane przez gracza gdy wciśnie przycisk interakcji
	{
		if (!interactable) return; // Jeśli drzwi są zablokowane, ignoruje wywołanie

		interactable = false; // Blokuje interakcję na czas animacji
		toggle = !toggle; // Przełącza stan drzwi (otwarte ↔ zamknięte)

		if (animationPlayer != null) { // Jeśli AnimationPlayer jest przypisany
			if (!toggle) { // Jeśli drzwi mają się zamknąć
				animationPlayer.play("close"); // Odtwarza animację zamykania
			} else { // Jeśli drzwi mają się otworzyć
				animationPlayer.play("open"); // Odtwarza animację otwierania
			}
		}

		timer = 1.0; // Ustawia timer blokady na 1 sekundę
		waiting = true; // Rozpoczyna odliczanie blokady
	}
}
