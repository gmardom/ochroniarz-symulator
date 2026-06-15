package Game.Rzeczy.IncidentDropZone;

import Player.Player;
import godot.annotation.*;
import godot.api.*;
import godot.core.*;

@RegisterClass
public class IncidentDropZone extends Area3D
{
	@RegisterFunction
	public void _ready()
	{
		connect("body_entered", new NativeCallable(this, new StringName("onBodyEntered")));
		connect("body_exited", new NativeCallable(this, new StringName("onBodyExited")));
	}

	@RegisterFunction
	public void onBodyEntered(Node body)
	{
		if (body instanceof Player player) {
			player.setInDropZone(true);
			if (player.isDragging()) {
				player.dropDeliver();
			}
		}
	}

	@RegisterFunction
	public void onBodyExited(Node body)
	{
		if (body instanceof Player player) {
			player.setInDropZone(false);
		}
	}
}
