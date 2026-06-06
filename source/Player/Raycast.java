package Player; // Deklaracja pakietu Player

import Game.*;
import godot.annotation.*; // Import adnotacji Godot
import godot.api.*; // Import klas API Godot (RayCast3D, Node itp.)
import godot.core.*; // Import typów rdzeniowych Godot
import static godot.global.GD.*; // Import statycznych metod globalnych Godot (np. print)

@RegisterClass // Rejestruje klasę jako klasę Godot widoczną w edytorze
public class Raycast extends RayCast3D // Klasa Raycast dziedziczy po RayCast3D (promień wykrywający kolizje)
{
	@RegisterProperty @Export public HeadsUpDisplay hud; // Referencja do HUD – widoczna w edytorze

	@RegisterFunction
	public void _process(double delta) // Wywoływany co klatkę
	{
		if (isColliding()) { // Jeśli promień trafia w jakiś obiekt
			var hitObj = getCollider(); // Pobiera trafiony obiekt
			if (hud != null) hud.startInteraction("Interakcja"); // Pokazuje tekst interakcji na HUD
			if (hitObj != null && Input.isActionJustPressed("interact")) { // Jeśli obiekt istnieje i gracz wcisnął przycisk interakcji
				if (hitObj instanceof Node node) { // Sprawdza czy trafiony obiekt jest węzłem
					print("Wywoluje interact na: " + node.getName()); // Wypisuje nazwę obiektu w konsoli (debug)
					node.callDeferred("interact"); // Wywołuje metodę interact() na trafionym obiekcie
				}
			}
		} else { // Jeśli promień nic nie trafia
			if (hud != null) hud.stopInteraction(); // Ukrywa tekst interakcji na HUD
		}
	}
}
