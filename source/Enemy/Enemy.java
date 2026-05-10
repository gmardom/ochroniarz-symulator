package Enemy;

import godot.annotation.*;
import godot.api.*;
import godot.global.GD;

@RegisterClass
public class Enemy extends CharacterBody3D
{
	@RegisterProperty @Export
	public int health = 100;
	
	@RegisterFunction
	public void _ready()
	{
	}
	
	@RegisterFunction
	public void _process(double delta)
	{
		if (health < 0) {
			queueFree();
		}
	}
	
	public void damage(int points)
	{
		health -= points;
	}
}
