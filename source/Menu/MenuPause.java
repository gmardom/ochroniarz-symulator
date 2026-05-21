package Menu; // Deklaracja pakietu Menu

import Game.*; // Import klas z pakietu Game (np. GameManager)
import godot.api.*; // Import klas API Godot (VBoxContainer, Panel itp.)
import godot.annotation.*; // Import adnotacji Godot

@RegisterClass // Rejestruje klasę jako klasę Godot widoczną w edytorze
public class MenuPause extends Menu // Menu pauzy dziedziczy po klasie Menu
{
	private VBoxContainer mainButtons; // Kontener z głównymi przyciskami menu pauzy
	private Panel settings; // Panel ustawień (ukryty domyślnie)

	@RegisterFunction
	public void _ready() // Wywoływane przy starcie – inicjalizuje elementy UI
	{
		mainButtons = (VBoxContainer) getNode("MainButtons"); // Pobiera węzeł z przyciskami po nazwie
		settings = (Panel) getNode("Settings"); // Pobiera panel ustawień po nazwie
		mainButtons.setVisible(true); // Pokazuje główne przyciski
		settings.setVisible(false); // Ukrywa panel ustawień
	}

	@RegisterFunction
	public void _onBackButtonPressed() // Wywoływane gdy gracz kliknie przycisk "Wróć"
	{
		GameManager.I().unpauseGame(); // Wznawia grę przez GameManager
	}

	@RegisterFunction
	public void _onSettingsButtonPressed() // Wywoływane gdy gracz kliknie przycisk "Ustawienia"
	{
		mainButtons.setVisible(false); // Ukrywa główne przyciski
		settings.setVisible(true); // Pokazuje panel ustawień
	}

	@RegisterFunction
	public void _onQuitButtonPressed() // Wywoływane gdy gracz kliknie przycisk "Wyjdź"
	{
		GameManager.I().loadMenu(); // Wraca do menu głównego przez GameManager
	}

	@RegisterFunction
	public void _onBackSettingsButtonPressed() // Wywoływane gdy gracz kliknie "Wróć" w ustawieniach
	{
		_ready(); // Resetuje UI do stanu początkowego (pokazuje przyciski, ukrywa ustawienia)
	}
}
