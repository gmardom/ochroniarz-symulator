package Level;

import Game.GameLoop;
import Player.HeadsUpDisplay;
import godot.annotation.*;
import godot.api.*;
import static godot.global.GD.*;

@RegisterClass
public class Level extends Node3D
{
	@RegisterFunction
	public void _ready()
	{
		var gameLoop = (GameLoop) getNode("GameLoop");
		var hud = (HeadsUpDisplay) getNode("/root/Game/Level/Player/HeadsUpDisplay");

		print("gameLoop: " + gameLoop);
		print("hud: " + hud);

		if (gameLoop != null && hud != null) {
			gameLoop.hud = hud;
			print("HUD assigned to GameLoop!");
		} else if (hud == null) {
			print("HUD is null!");
		} else {
			print("GameLoop is null!");
		}
	}
}
