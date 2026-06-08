package Level;

import Game.GameLoop;
import Player.HeadsUpDisplay;
import godot.annotation.*;
import godot.api.*;

@RegisterClass
public class Level extends Node3D
{
	@RegisterFunction
	public void _ready()
	{
		var gameLoop = (GameLoop) getNode("GameLoop");
		var hud = (HeadsUpDisplay) getNode("../Player/HeadsUpDisplay");

		if (gameLoop != null && hud != null) {
			gameLoop.hud = hud;
		}
	}
}
