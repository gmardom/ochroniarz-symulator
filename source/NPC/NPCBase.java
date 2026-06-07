package NPC;

import godot.annotation.*;
import godot.api.*;
import godot.core.*;
import static godot.global.GD.*;

@RegisterClass
public class NPCBase extends CharacterBody3D
{
	@RegisterProperty @Export public float health = 100f;
	@RegisterProperty @Export public boolean hostile = false;
	@RegisterProperty @Export public float speed = 2.0f;
	@RegisterProperty @Export public float wanderRadius = 8f;

	protected NavigationAgent3D navAgent;
	protected RandomNumberGenerator rng;
	protected Vector3 startPos;
	protected Vector3 wanderTarget;
	protected float waitTimer = 0f;
	protected boolean waiting = false;
	protected boolean hasTarget = false;

	private static final float GRAVITY = 9.8f;
	private static final float TARGET_REACHED_DIST = 0.8f;

	@RegisterFunction
	public void _ready()
	{
		var node = getNode("NavigationAgent3D");
		if (node instanceof NavigationAgent3D agent) {
			navAgent = agent;
		}
		rng = new RandomNumberGenerator();
		startPos = getPosition();
		pickWanderTarget();
		print(getName() + " ready at " + formatPos(startPos));
	}

	@RegisterFunction
	public void _physicsProcess(double delta)
	{
		if (health <= 0f) {
			queueFree();
			return;
		}

		var vel = getVelocity();

		if (!isOnFloor()) {
			vel.setY(vel.getY() - GRAVITY * (float) delta);
		}

		if (waiting) {
			waitTimer -= (float) delta;
			vel.setX(0f);
			vel.setZ(0f);
			if (waitTimer <= 0f) {
				waiting = false;
				pickWanderTarget();
			}
		} else if (hasTarget) {
			boolean arrived = false;

			if (navAgent != null && !navAgent.isNavigationFinished()) {
				var nextPos = navAgent.getNextPathPosition();
				var dx = (float) nextPos.getX() - (float) getPosition().getX();
				var dz = (float) nextPos.getZ() - (float) getPosition().getZ();
				var dist = (float) Math.sqrt(dx * dx + dz * dz);
				if (dist < TARGET_REACHED_DIST) {
					arrived = true;
				} else {
					vel.setX((dx / dist) * speed);
					vel.setZ((dz / dist) * speed);
				}
			} else {
				var dx = (float) wanderTarget.getX() - (float) getPosition().getX();
				var dz = (float) wanderTarget.getZ() - (float) getPosition().getZ();
				var dist = (float) Math.sqrt(dx * dx + dz * dz);
				if (dist < TARGET_REACHED_DIST) {
					arrived = true;
				} else {
					vel.setX((dx / dist) * speed);
					vel.setZ((dz / dist) * speed);
				}
			}

			if (arrived) {
				vel.setX(0f);
				vel.setZ(0f);
				waiting = true;
				waitTimer = rng.randfRange(2f, 5f);
				hasTarget = false;
				print(getName() + " dotarl do celu, czeka " + String.format("%.1f", waitTimer) + "s");
			}
		}

		setVelocity(vel);
		moveAndSlide();

		var vx = (float) vel.getX();
		var vz = (float) vel.getZ();
		if (Math.abs(vx) > 0.01f || Math.abs(vz) > 0.01f) {
			var rot = getRotation();
			rot.setY((float) Math.atan2(vx, vz));
			setRotation(rot);
		}
	}

	protected void pickWanderTarget()
	{
		var ox = rng.randfRange(-wanderRadius, wanderRadius);
		var oz = rng.randfRange(-wanderRadius, wanderRadius);
		wanderTarget = new Vector3(
			startPos.getX() + ox,
			startPos.getY(),
			startPos.getZ() + oz
		);
		hasTarget = true;

		if (navAgent != null) {
			navAgent.setTargetPosition(wanderTarget);
		}
		print(getName() + " zmienil cel na " + formatPos(wanderTarget));
	}

	protected void startWaiting()
	{
		waiting = true;
		waitTimer = rng.randfRange(2f, 5f);
		hasTarget = false;
		print(getName() + " czeka " + String.format("%.1f", waitTimer) + "s");
	}

	public void damage(int points)
	{
		health -= points;
		print(getName() + " otrzymal " + points + " obrazen, zdrowie=" + (int) health);
	}

	private static String formatPos(Vector3 p)
	{
		return "(" + String.format("%.1f", p.getX()) + ", " + String.format("%.1f", p.getZ()) + ")";
	}
}
