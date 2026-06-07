package Player;

import godot.annotation.*;
import godot.api.*;

@RegisterClass
public class Raycast extends RayCast3D
{
	@RegisterFunction
	public void _process(double delta)
	{
		if (isColliding()) {
			var hitObj = getCollider();
			if (hitObj instanceof RigidBody3D node) {
				if (Input.isActionJustPressed("interact")) {
					node.callDeferred("interact");
				}
			}
		}
	}
}
