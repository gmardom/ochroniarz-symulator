package Enemy; // Deklaracja pakietu Enemy

import godot.annotation.*; // Import adnotacji Godot
import godot.api.*; // Import klas API Godot (CharacterBody3D itp.)
import godot.global.GD; // Import globalnych metod Godot

@RegisterClass // Rejestruje klasę jako klasę Godot widoczną w edytorze
public class Enemy extends CharacterBody3D // Klasa przeciwnika dziedziczy po CharacterBody3D (fizyczna postać)
{
	@RegisterProperty @Export
	public int health = 100; // Punkty życia przeciwnika – widoczne i edytowalne w edytorze

	@RegisterFunction
	public void _ready() // Wywoływane przy starcie – brak akcji inicjalizacyjnych
	{
	}

	@RegisterFunction
	public void _process(double delta) // Wywoływany co klatkę – sprawdza czy przeciwnik żyje
	{
		if (health < 0) { // Jeśli punkty życia spadły poniżej zera
			queueFree(); // Usuwa przeciwnika ze sceny
		}
	}

	public void damage(int points) // Zadaje obrażenia przeciwnikowi
	{
		health -= points; // Odejmuje podaną liczbę punktów od zdrowia
	}
}
