package Enemy;

import NPC.NPCBase;
import godot.annotation.*;
import static godot.global.GD.*;

@RegisterClass
public class Enemy extends NPCBase
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
		if (rng.randf() < 0.3f) {
			print(getName() + " COS CHOWAM...");
		} else {
			print(getName() + " udaje ze oglada towar");
		}
	}
}
