package Level;

import Game.GameLoop;
import Game.GameManager;
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

		Node playerNode = getNodeOrNull("Player");
		HeadsUpDisplay hud = null;
		if (playerNode != null) {
			hud = (HeadsUpDisplay) ((Node) playerNode).getNodeOrNull("HeadsUpDisplay");
		}

		if (gameLoop != null && hud != null) {
			gameLoop.hud = hud;
			GameManager.I().gameHud = hud;
		}
	}
}
