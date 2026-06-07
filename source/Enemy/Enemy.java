package Enemy;

import NPC.NPCBase;
import godot.annotation.*;

@RegisterClass
public class Enemy extends NPCBase
	@RegisterProperty @Export public float steal_chance = 0.4f;
	@RegisterProperty @Export public float steal_duration = 6.0f;
	@RegisterProperty @Export public float stolen_speed_multiplier = 0.5f;

{
	@RegisterFunction
	@Override
	public void _ready()
	{
		hostile = true;
		minSpeed = 3.0f;
		maxSpeed = 6.0f;
		super._ready();
	}

	@RegisterFunction
	@Override
	public void _physicsProcess(double delta)
	{
		super._physicsProcess(delta);
	}

	@Override
	protected void onArriveAtShelf()
	{
	}
}
