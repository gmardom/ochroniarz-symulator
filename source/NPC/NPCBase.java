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
	@RegisterProperty @Export public float wanderRadius = 15f;
	@RegisterProperty @Export public float minWaitTime = 2f;
	@RegisterProperty @Export public float maxWaitTime = 5f;
	@RegisterProperty @Export public float minSpeed = 1.2f;
	@RegisterProperty @Export public float maxSpeed = 3.0f;

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

		// naturalne zroznicowanie: losowa predkosc w zadanym zakresie
		speed = rng.randfRange(minSpeed, maxSpeed);

		// drobny offset startowy, aby NPC nie stali idealnie w tym samym punkcie
		var offset = new Vector3(
			rng.randfRange(-0.5f, 0.5f),
			0f,
			rng.randfRange(-0.5f, 0.5f)
		);
		setPosition(new Vector3(
			startPos.getX() + offset.getX(),
			startPos.getY(),
			startPos.getZ() + offset.getZ()
		));
		startPos = getPosition();

		pickWanderTarget();
		print(getName() + " ready " + formatPos(startPos) + " speed=" + String.format("%.1f", speed));
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
				arrived = moveAlongPath(vel);
			} else {
				arrived = moveDirect(vel);
			}

			if (arrived) {
				vel.setX(0f);
				vel.setZ(0f);
				waiting = true;
				waitTimer = rng.randfRange(minWaitTime, maxWaitTime);
				hasTarget = false;
				print(getName() + " dotarl, czeka " + String.format("%.1f", waitTimer) + "s");
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

	private boolean moveAlongPath(Vector3 vel)
	{
		var nextPos = navAgent.getNextPathPosition();
		var dx = (float) nextPos.getX() - (float) getPosition().getX();
		var dz = (float) nextPos.getZ() - (float) getPosition().getZ();
		var dist = (float) Math.sqrt(dx * dx + dz * dz);
		if (dist < TARGET_REACHED_DIST) {
			return true;
		}
		vel.setX((dx / dist) * speed);
		vel.setZ((dz / dist) * speed);
		return false;
	}

	private boolean moveDirect(Vector3 vel)
	{
		var dx = (float) wanderTarget.getX() - (float) getPosition().getX();
		var dz = (float) wanderTarget.getZ() - (float) getPosition().getZ();
		var dist = (float) Math.sqrt(dx * dx + dz * dz);
		if (dist < TARGET_REACHED_DIST) {
			return true;
		}
		vel.setX((dx / dist) * speed);
		vel.setZ((dz / dist) * speed);
		return false;
	}

	protected void pickWanderTarget()
	{
		// losuj punkt w globalnym promieniu od pozycji startowej
		var ox = rng.randfRange(-wanderRadius, wanderRadius);
		var oz = rng.randfRange(-wanderRadius, wanderRadius);
		wanderTarget = new Vector3(
			startPos.getX() + ox,
			startPos.getY(),
			startPos.getZ() + oz
		);

		// snap do nawmesha: przyczep wylosowany punkt do najblizszego poligona
		if (navAgent != null) {
			var map = navAgent.getNavigationMap();
			if (map.isValid()) {
				wanderTarget = NavigationServer3D.mapGetClosestPoint(map, wanderTarget);
			}
		}

		hasTarget = true;
		if (navAgent != null) {
			navAgent.setTargetPosition(wanderTarget);
		}
		print(getName() + " nowy cel " + formatPos(wanderTarget));
	}

	public void damage(int points)
	{
		health -= points;
		print(getName() + " obrazenia=" + (int) health);
	}

	private static String formatPos(Vector3 p)
	{
		return "(" + String.format("%.1f", p.getX()) + ", " + String.format("%.1f", p.getZ()) + ")";
	}
}
