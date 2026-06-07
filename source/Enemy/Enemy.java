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
		super._ready();
		hostile = true;
		print(getName() + " skonfigurowany jako wrog");
	}

	@RegisterFunction
	@Override
	public void _physicsProcess(double delta)
	{
		super._physicsProcess(delta);
	}
}
