package Menu;

import Game.*;
import godot.annotation.*;

@RegisterClass
public class MenuPause extends Menu
{
	@RegisterFunction
	public void _onBackButtonPressed()
	{
		GameManager.I().unpauseGame();
	}

	@RegisterFunction
	public void _onQuitButtonPressed()
	{
		GameManager.I().loadMenu();
	}
	
}
