package Civilian;

import NPC.NPCBase;
import godot.annotation.*;

@RegisterClass
public class Civilian extends NPCBase
{
	@RegisterFunction
	@Override
	public void _ready()
	{
		hostile = false;
		minSpeed = 1.5f;
		maxSpeed = 3.5f;
		super._ready();
	}

	@RegisterFunction
	@Override
	public void _physicsProcess(double delta)
	{
		super._physicsProcess(delta);
	}
}
