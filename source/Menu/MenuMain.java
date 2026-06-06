package Menu;

import Game.*;
import godot.annotation.*;
import godot.api.VBoxContainer;
import godot.api.Panel;

@RegisterClass
public class MenuMain extends Menu
{
	private VBoxContainer mainButtons;
	private Panel settings;
	private VBoxContainer gameSelect; // Nowy panel wyboru trybu gry

	@RegisterFunction
	public void _ready()
	{
		mainButtons = (VBoxContainer) getNode("MainButtons");
		settings    = (Panel) getNode("Settings");
		gameSelect  = (VBoxContainer) getNode("GameSelect"); // Pobierz nowy panel

		mainButtons.setVisible(true);
		settings.setVisible(false);
		gameSelect.setVisible(false); // Domyślnie ukryty
	}

	// --- MainButtons ---

	@RegisterFunction
	public void _onStartButtonPressed()
	{
		// Zamiast od razu ładować grę, pokaż panel wyboru
		mainButtons.setVisible(false);
		gameSelect.setVisible(true);
	}

	@RegisterFunction
	public void _onSettingsButtonPressed()
	{
		mainButtons.setVisible(false);
		settings.setVisible(true);
	}

	@RegisterFunction
	public void _onQuitButtonPressed()
	{
		GameManager.I().exit();
	}

	// --- Settings ---

	@RegisterFunction
	public void _onBackSettingsButtonPressed()
	{
		_ready(); // Reset do stanu początkowego
	}

	// --- GameSelect ---

	@RegisterFunction
	public void _onNewGameButtonPressed()
	{
		GameManager.I().loadGame(); // Nowa gra — bez slotu
	}

	@RegisterFunction
	public void _onLoadGameButtonPressed()
	{
		// Zapis będzie powiązany z modelem w grze (unikalny identyfikator)
		// Na razie wywołaj loadGame z flagą/parametrem — rozszerz GameManager gdy będzie gotowy system zapisu
		GameManager.I().loadGame(); // TODO: przekaż slot zapisu
	}

	@RegisterFunction
	public void _onBackGameSelectButtonPressed()
	{
		gameSelect.setVisible(false);
		mainButtons.setVisible(true);
	}
}
