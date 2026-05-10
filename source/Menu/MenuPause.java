package Menu;

import Game.*;
import godot.api.*;
import godot.annotation.*;

@RegisterClass
public class MenuPause extends Menu
{
	private VBoxContainer mainButtons;
	private Panel settings;

	@RegisterFunction
	public void _ready()
	{
		mainButtons = (VBoxContainer) getNode("MainButtons");
		settings = (Panel) getNode("Settings");

		mainButtons.setVisible(true);
		settings.setVisible(false);
	}
	
	@RegisterFunction
	public void _onBackButtonPressed()
	{
		GameManager.I().unpauseGame();
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
		GameManager.I().loadMenu();
	}
	
	@RegisterFunction
	public void _onBackSettingsButtonPressed()
	{
		_ready();
	}
}
