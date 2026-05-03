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

	@RegisterFunction
	public void _ready()
	{
		mainButtons = (VBoxContainer) getNode("MainButtons");
		settings = (Panel) getNode("Settings");

		mainButtons.setVisible(true);
		settings.setVisible(false);
	}

	@RegisterFunction
	public void _onStartButtonPressed()
	{
		GameManager.I().loadGame();
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
	
	@RegisterFunction
	public void _onBackSettingsButtonPressed()
	{
		_ready();
	}
}
