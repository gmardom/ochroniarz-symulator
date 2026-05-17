package Player;

import godot.annotation.*;
import godot.api.*;
import godot.core.*;
import static godot.global.GD.*;

@RegisterClass
public class Raycast extends RayCast3D
{
	@RegisterProperty @Export public HeadsUpDisplay hud;

	@RegisterFunction
	public void _process(double delta)
	{
		if (isColliding()) {
			var hitObj = getCollider();
			if (hud != null) hud.startInteraction("Interakcja");
			if (hitObj != null && Input.isActionJustPressed("interact")) {
				if (hitObj instanceof Node node) {
					print("Wywoluje interact na: " + node.getName());
					node.callDeferred("interact");
				}
			}
		} else {
			if (hud != null) hud.stopInteraction();
		}
	}
}
