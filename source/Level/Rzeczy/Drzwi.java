package Game;

import godot.annotation.*;
import godot.api.*;
import godot.core.*;

@RegisterClass
public class Drzwi extends RigidBody3D
{
	@RegisterProperty @Export public AnimationPlayer animationPlayer; // Referencja do AnimationPlayer

	private boolean toggle = false; // Czy drzwi są otwarte
	private boolean interactable = true; // Czy można wejść w interakcję
	private double timer = 0.0; // Odliczanie czasu blokady
	private boolean waiting = false; // Czy aktualnie odliczamy

	@RegisterFunction
	public void _process(double delta) // Sprawdza timer co klatkę
	{
		if (waiting) {
			timer -= delta; // Odejmuje czas
			if (timer <= 0.0) {
				interactable = true; // Odblokowuje interakcję
				waiting = false; // Zatrzymuje odliczanie
			}
		}
	}

	@RegisterFunction
	public void interact() // Wywoływane przez gracza
	{
		if (!interactable) return; // Jeśli zablokowane, ignoruj

		interactable = false; // Blokuje interakcję
		toggle = !toggle; // Przełącza stan

		if (animationPlayer != null) {
			if (!toggle) {
				animationPlayer.play("close"); // Animacja zamykania
			} else {
				animationPlayer.play("open"); // Animacja otwierania
			}
		}

		timer = 1.0; // Ustaw timer na 1 sekundę
		waiting = true; // Rozpocznij odliczanie
	}
}
