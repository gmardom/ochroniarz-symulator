package Civilian;

import NPC.NPCBase;
import godot.annotation.*;
import static godot.global.GD.*;

@RegisterClass
public class Civilian extends NPCBase
{
	@RegisterFunction
	@Override
	public void _ready()
	{
		super._ready();
		hostile = false;
		speed = 1.5f;
		print(getName() + " skonfigurowany jako cywil");
	}

	@RegisterFunction
	@Override
	public void _physicsProcess(double delta)
	{
		super._physicsProcess(delta);
	}
}
