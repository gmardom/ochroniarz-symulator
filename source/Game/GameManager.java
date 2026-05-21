package Game; // Deklaracja pakietu Game

import Menu.*; // Import klas z pakietu Menu
import Transition.*; // Import klas z pakietu Transition
import godot.annotation.*; // Import adnotacji Godot
import godot.api.*; // Import klas API Godot
import godot.global.GD; // Import globalnych metod Godot (np. load)
import static java.util.Objects.requireNonNull; // Import metody requireNonNull do sprawdzania null

@RegisterClass // Rejestruje klasę jako klasę Godot
public final class GameManager extends Node // Singleton zarządzający stanem gry, dziedziczy po Node
{
	// Globalna instancja singletona – dostępna z każdego miejsca w kodzie
	private static GameManager I = null; // Przechowuje jedyną instancję GameManager
	public static GameManager I() { return I; } // Zwraca globalną instancję GameManager

	// Globalne referencje do węzłów sceny
	public Node gameRoot; // Korzeń sceny gry (/root/Game)
	public TransitionPlayer transition; // Węzeł obsługujący animacje przejść między scenami

	// Załadowane sceny
	public MenuMain mainMenu; // Scena głównego menu
	public MenuPause menuPause; // Scena menu pauzy
	public Node3D gameLevel; // Scena poziomu gry

	// Enum definiujący możliwe stany gry
	public enum State
	{
		Nil, // Stan niezainicjowany
		Starting, // Gra się uruchamia
		Menu, // Gracz jest w menu głównym
		Playing, // Gracz aktywnie gra
		Paused, // Gra jest wstrzymana
	};

	public State initialState = State.Starting; // Stan początkowy przy uruchomieniu
	public State currentState = State.Nil; // Aktualny stan gry

	@RegisterFunction
	public void _enterTree() // Wywołuje się gdy węzeł wchodzi do drzewa sceny (przed _ready)
	{
		I = this; // Ustawia globalną instancję singletona na ten obiekt

		// Pobiera i tworzy niezbędne zasoby
		gameRoot = getNode("/root/Game"); // Pobiera korzeń sceny gry po ścieżce
		transition = loadScene("res://source/Transition/TransitionPlayer.tscn", this); // Ładuje i dodaje węzeł przejść jako dziecko

		currentState = initialState; // Ustawia aktualny stan na stan początkowy
	}

	@RegisterFunction
	public void _ready() // Wywołuje się gdy węzeł i wszystkie dzieci są gotowe
	{
		switch (currentState) { // Sprawdza stan początkowy i wykonuje odpowiednią akcję
		case Starting -> { // Jeśli gra się właśnie uruchamia
			// NOTE: Obecnie nic nie robi. Później można pokazać logo studia itp.
			loadMenu(); // Ładuje menu główne
		}
		case Menu -> { // Jeśli stan to menu
			loadMenu(); // Ładuje menu główne
		}
		case Playing -> { // Jeśli stan to gra
			loadGame(); // Ładuje poziom gry
		}
		case Paused -> { // Jeśli stan to pauza
			loadGame(); // Ładuje poziom gry
			pauseGame(); // Natychmiast wstrzymuje grę
		}
		}
	}

	@RegisterFunction
	public void _process(double delta) // Wywoływany co klatkę
	{
		switch (currentState) { // Obsługuje wejście zależnie od stanu gry
		case Playing -> { // Podczas gry
			if (Input.isActionJustPressed("ui_cancel")) { // Jeśli wciśnięto Escape
				pauseGame(); // Wstrzymuje grę
			}
		}
		case Paused -> { // Podczas pauzy
			if (Input.isActionJustPressed("ui_cancel")) { // Jeśli wciśnięto Escape
				unpauseGame(); // Wznawia grę
			}
		}
		}
	}

	public void loadMenu() // Ładuje i pokazuje menu główne, usuwa scenę gry
	{
		currentState = State.Menu; // Ustawia stan na Menu
		transition.in(); // Odtwarza animację wejścia przejścia (zakrywa ekran)
		{
			// Ładuje menu
			if (mainMenu == null) mainMenu = loadScene("res://source/Menu/MenuMain.tscn", gameRoot); // Ładuje scenę menu jeśli nie istnieje
			if (mainMenu != null) mainMenu.setVisible(true); // Pokazuje menu główne

			// Usuwa scenę gry
			if (gameLevel != null) gameLevel.queueFree(); // Usuwa poziom gry z pamięci
			gameLevel = null; // Zeruje referencję do poziomu
			if (menuPause != null) menuPause.queueFree(); // Usuwa menu pauzy z pamięci
			menuPause = null; // Zeruje referencję do menu pauzy

			Input.setMouseMode(Input.MouseMode.VISIBLE); // Pokazuje kursor myszy
		}
		transition.out(); // Odtwarza animację wyjścia przejścia (odkrywa ekran)
	}

	public void loadGame() // Ładuje scenę gry i ukrywa menu
	{
		currentState = State.Playing; // Ustawia stan na Playing
		transition.in(); // Odtwarza animację wejścia przejścia
		{
			if (mainMenu != null) mainMenu.setVisible(false); // Ukrywa menu główne

			// Ładuje sceny gry jeśli nie istnieją
			if (gameLevel == null) gameLevel = loadScene("res://source/Level/Level.tscn", gameRoot); // Ładuje scenę poziomu
			if (menuPause == null) menuPause = loadScene("res://source/Menu/MenuPause.tscn", gameRoot); // Ładuje scenę menu pauzy
			if (menuPause != null) menuPause.setVisible(false); // Ukrywa menu pauzy

			Input.setMouseMode(Input.MouseMode.CAPTURED); // Ukrywa i blokuje kursor myszy
		}
		transition.out(); // Odtwarza animację wyjścia przejścia
	}

	public void pauseGame() // Wstrzymuje grę i pokazuje menu pauzy
	{
		currentState = State.Paused; // Ustawia stan na Paused
		if (menuPause != null) menuPause.setVisible(true); // Pokazuje menu pauzy
		Input.setMouseMode(Input.MouseMode.VISIBLE); // Pokazuje kursor myszy
	}

	public void unpauseGame() // Wznawia grę i ukrywa menu pauzy
	{
		currentState = State.Playing; // Ustawia stan na Playing
		if (menuPause != null) menuPause.setVisible(false); // Ukrywa menu pauzy
		Input.setMouseMode(Input.MouseMode.CAPTURED); // Ukrywa i blokuje kursor myszy
	}

	public void exit() // Zamyka aplikację
	{
		requireNonNull(getTree()).quit(); // Pobiera drzewo sceny (rzuca wyjątek jeśli null) i zamyka grę
	}

	public static boolean getFullscreen() // Zwraca true jeśli gra jest w trybie pełnoekranowym
	{
		return DisplayServer.windowGetMode() == DisplayServer.WindowMode.FULLSCREEN; // Porównuje aktualny tryb okna z pełnoekranowym
	}

	public static void setFullscreen(boolean isFullscreen) // Ustawia tryb okna na pełnoekranowy lub okienkowy
	{
		if (isFullscreen) { // Jeśli ma być pełnoekranowy
			DisplayServer.windowSetMode(DisplayServer.WindowMode.WINDOWED); // Przełącza na tryb okienkowy
		} else { // Jeśli ma być okienkowy
			DisplayServer.windowSetMode(DisplayServer.WindowMode.FULLSCREEN); // Przełącza na tryb pełnoekranowy
		}
	}

	public static void toggleFullscreen() // Przełącza między trybem pełnoekranowym a okienkowym
	{
		setFullscreen(getFullscreen()); // Wywołuje setFullscreen z aktualnym stanem (odwraca tryb)
	}

	@SuppressWarnings("unchecked") // Wycisza ostrzeżenie o niezweryfikowanym rzutowaniu generycznym
	private static <T extends Node> T loadScene(String path, Node root) // Ładuje scenę z pliku i dodaje jako dziecko węzła root
	{
		var scene = (PackedScene) GD.load(path); // Ładuje plik sceny (.tscn) z podanej ścieżki
		assert scene != null; // Sprawdza czy scena została załadowana (tylko w trybie debug)
		var instance = scene.instantiate(); // Tworzy instancję załadowanej sceny
		root.addChild(instance); // Dodaje instancję jako dziecko węzła root
		return (T) instance; // Zwraca instancję rzutowaną na oczekiwany typ
	}
}
