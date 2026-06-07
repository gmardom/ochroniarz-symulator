package Level;

import Game.GameLoop;
import Player.HeadsUpDisplay;
import godot.annotation.*;
import godot.api.*;
import godot.global.GD;

@RegisterClass
public class Level extends Node3D
{
	@RegisterFunction
	public void _ready()
	{
		var gameLoop = (GameLoop) getNode("GameLoop");
		var hud = (HeadsUpDisplay) getNode("Player/HeadsUpDisplay");

		GD.print("[Level] gameLoop: " + (gameLoop != null ? "OK" : "NULL"));
		GD.print("[Level] hud: " + (hud != null ? "OK" : "NULL"));

		if (gameLoop != null && hud != null) {
			gameLoop.hud = hud;
			GD.print("[Level] hud przypisany do GameLoop");
		}
	}
}
