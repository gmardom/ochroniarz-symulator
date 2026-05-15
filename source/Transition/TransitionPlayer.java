package Transition;

import godot.annotation.*;
import godot.api.*;

@RegisterClass
public class TransitionPlayer extends CanvasLayer
{
	@RegisterProperty @Export
	public AnimationPlayer animationPlayer;

	@RegisterFunction
	public void _enterTree()
	{
		animationPlayer = (AnimationPlayer) getNode("AnimationPlayer");
	}

	public void in()
	{
		animationPlayer.play("LevelTransitions/in");
	}

	public void out()
	{
		animationPlayer.play("LevelTransitions/out");
	}
}
