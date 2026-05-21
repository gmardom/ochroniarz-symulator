package Player; // Deklaracja pakietu Player

import godot.annotation.*; // Import adnotacji Godot
import godot.api.*; // Import klas API Godot (CanvasLayer, Label itp.)
import godot.core.*; // Import typów rdzeniowych Godot

@RegisterClass // Rejestruje klasę jako klasę Godot widoczną w edytorze
public class HeadsUpDisplay extends CanvasLayer // HUD dziedziczy po CanvasLayer (warstwa UI rysowana na ekranie)
{
	@RegisterProperty @Export public CanvasItem crosshair; // Celownik – element UI widoczny w edytorze
	@RegisterProperty @Export public Label interractionText; // Etykieta tekstowa podpowiedzi interakcji

	@RegisterFunction
	public void _ready() {} // Wywoływane przy starcie – brak akcji inicjalizacyjnych

	public void startInteraction(String text) // Włącza tryb interakcji: pokazuje celownik i tekst
	{
		if (crosshair != null) { // Jeśli celownik istnieje
			crosshair.setVisible(true); // Pokazuje celownik
		}
		if (interractionText != null) { // Jeśli etykieta tekstowa istnieje
			interractionText.setText("F) " + text); // Ustawia tekst z prefiksem klawisza akcji "F)"
			interractionText.setVisible(true); // Pokazuje etykietę
		}
	}

	public void stopInteraction() // Wyłącza tryb interakcji: ukrywa celownik i tekst
	{
		if (crosshair != null) crosshair.setVisible(false); // Ukrywa celownik (jeśli istnieje)
		if (interractionText != null) interractionText.setVisible(false); // Ukrywa tekst interakcji (jeśli istnieje)
	}
}
