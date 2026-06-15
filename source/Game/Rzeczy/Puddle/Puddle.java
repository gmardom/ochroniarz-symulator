package Game.Rzeczy.Puddle;

import Player.Player;
import godot.annotation.*;
import godot.api.*;
import godot.core.*;
import static godot.global.GD.*;

@RegisterClass
public class Puddle extends Node3D
{
	@RegisterProperty @Export public float lifetimeSeconds = 15f;
	@RegisterProperty @Export public float slipDuration = 2f;

	// Puddle slowdown config
	@RegisterProperty @Export public float slowFactor = 0.6f;
	@RegisterProperty @Export public float slowDuration = 3.0f;
	@RegisterProperty @Export public boolean applyWhileInside = true;

	private float lifetimeTimer = 0f;
	private boolean alive = true;

	@RegisterFunction
	public void _ready()
	{
		lifetimeTimer = lifetimeSeconds;

		var area = getNode("Area3D");
		if (area instanceof Area3D a) {
			a.connect("body_entered", new NativeCallable(this, new StringName("onBodyEntered")));
			a.connect("body_exited", new NativeCallable(this, new StringName("onBodyExited")));
		}
	}

	@RegisterFunction
	public void _process(double delta)
	{
		if (!alive) return;

		lifetimeTimer -= (float) delta;
		if (lifetimeTimer <= 0f) {
			alive = false;
			print("Puddle expired");
			queueFree();
		}
	}

	@RegisterFunction
	public void onBodyEntered(Node body)
	{
		if (!alive) return;

		if (body instanceof Player player) {
			if (player.isSprinting()) {
				player.applySlip(slipDuration);
			}
			player.enterPuddle(slowFactor, slowDuration);
		}
	}

	@RegisterFunction
	public void onBodyExited(Node body)
	{
		if (body instanceof Player player) {
			player.exitPuddle(applyWhileInside);
		}
	}
}
