package Game.Rzeczy.GrateArea;

import Player.Player;
import Game.Interactable;
import godot.annotation.*;
import godot.api.*;
import godot.core.*;
import static godot.global.GD.*;

@RegisterClass
public class GrateArea extends StaticBody3D implements Interactable
{
	// Where the enemy is placed relative to this area's origin
	@RegisterProperty @Export public Vector3 imprisonOffset = new Vector3(0, 0, 2);

	@RegisterFunction
	public void _ready()
	{
		addToGroup("interactable");
	}
	
	@Override
	public void interact(Player player)
	{
		print("Zamknięto ziuta");
		player.dropDeliver();
	}
}
