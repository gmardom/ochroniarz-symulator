package Player;

import godot.annotation.*;
import godot.api.*;
import godot.core.*;

@RegisterClass
public class Raycast extends RayCast3D
{
	@RegisterFunction
	public void _process(double delta)
	{
		if (isColliding()) {
			var hitObj = getCollider();
			if (hitObj != null && Input.isActionJustPressed("interact")) {
				if (hitObj instanceof Node node) {
					node.callDeferred("interact");
				}
			}
		}
	}
}
