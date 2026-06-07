package NPC;

import godot.annotation.*;
import godot.api.*;
import godot.core.*;
import static godot.global.GD.*;

@RegisterClass
public class NPCBase extends CharacterBody3D
{
	public interface NpcSpawner {
		void removeNpc(NPCBase npc);
	}

	public enum CustomerState {
		STATE_SPAWN,
		STATE_TO_ENTRANCE,
		STATE_AT_SHELF,
		STATE_TO_CASHIER,
		STATE_AT_CASHIER,
		STATE_TO_EXIT,
		STATE_TO_SPAWN,
		STATE_DESPAWN
	}

	@RegisterProperty @Export public float health = 100f;
	@RegisterProperty @Export public boolean hostile = false;
	@RegisterProperty @Export public float speed = 2.0f;
	@RegisterProperty @Export public float minSpeed = 2.0f;
	@RegisterProperty @Export public float maxSpeed = 5.0f;
	@RegisterProperty @Export public float arrivalThreshold = 1.2f;

	public Node3D entrancePoint;
	public Node3D[] shelfWaypoints;
	public Node3D cashierPoint;
	public Node3D exitPoint;
	public Node3D spawnPoint;

	public NpcSpawner spawnerRef;

	protected CustomerState state = CustomerState.STATE_SPAWN;
	protected CustomerState previousState = CustomerState.STATE_SPAWN;
	protected boolean isWaiting = false;
	protected float waitTimer = 0f;
	protected Vector3 currentTarget;

	protected int shelvesVisited = 0;

	private static final int MAX_SHELVES = 3;

	protected NavigationAgent3D navAgent;
	protected RandomNumberGenerator rng;

	protected float timeSinceSpawn = 0f;
	protected boolean ghostMode = true;

	private static final float ARRIVAL_DIST = 0.3f;
	private static final float ROT_EPSILON = 0.01f;
	private static final float MIN_ARRIVAL_TIME = 0.5f;

	@RegisterFunction
	public void _ready()
	{
		var node = getNode("NavigationAgent3D");
		if (node instanceof NavigationAgent3D agent) {
			navAgent = agent;
			navAgent.setTargetDesiredDistance(1.5f);
			navAgent.setPathDesiredDistance(2.0f);
		}

		rng = new RandomNumberGenerator();
		speed = rng.randfRange(minSpeed, maxSpeed);

		setGhostMode(true);
	}

	public void initialize(Node3D entrance, Node3D[] shelves, Node3D cashier, Node3D exit, Node3D spawn)
	{
		entrancePoint = entrance;
		shelfWaypoints = shelves;
		cashierPoint = cashier;
		exitPoint = exit;
		spawnPoint = spawn;

		previousState = CustomerState.STATE_SPAWN;

		if (entrancePoint != null) {
			state = CustomerState.STATE_TO_ENTRANCE;
			setTarget(entrancePoint.getGlobalPosition(), true);
			print("[NPC] " + getName() + ": " + previousState + " -> " + state + " | cel: " + formatPos(currentTarget));
		} else {
			print("[NPC] " + getName() + ": brak entrance - idle (manual placement)");
		}
	}

	@RegisterFunction
	public void _physicsProcess(double delta)
	{
		if (health <= 0f) {
			notifyDespawn();
			queueFree();
			return;
		}

		timeSinceSpawn += (float) delta;

		var vel = getVelocity();
		if (ghostMode) {
			vel.setY(0f);
		} else {
			vel.setY(vel.getY() - (float)(9.8 * delta));
		}

		if (isWaiting) {
			waitTimer -= (float) delta;
			vel.setX(0f);
			vel.setZ(0f);
			setVelocity(vel);
			moveAndSlide();
			if (waitTimer <= 0f) {
				isWaiting = false;
				advanceState(false);
			}
			return;
		}

		if (state == CustomerState.STATE_SPAWN || state == CustomerState.STATE_DESPAWN) {
			setVelocity(vel);
			moveAndSlide();
			return;
		}

		if (currentTarget != null) {
			var dist = getGlobalPosition().distanceTo(currentTarget);
			if (dist <= arrivalThreshold && timeSinceSpawn >= MIN_ARRIVAL_TIME) {
				vel.setX(0f);
				vel.setZ(0f);
				setVelocity(vel);
				moveAndSlide();
				advanceState(true);
				return;
			}

			if (ghostMode) {
				moveTowardDirect(currentTarget, vel);
			} else if (navAgent != null) {
				var pathPos = navAgent.getNextPathPosition();
				if (pathPos != null && getGlobalPosition().distanceTo(pathPos) > 0.5f) {
					moveTowardDirect(pathPos, vel);
				} else {
					moveTowardDirect(currentTarget, vel);
				}
			} else {
				moveTowardDirect(currentTarget, vel);
			}
		} else {
			vel.setX(0f);
			vel.setZ(0f);
		}

		setVelocity(vel);
		moveAndSlide();

		var vx = (float) vel.getX();
		var vz = (float) vel.getZ();
		if (Math.abs(vx) > ROT_EPSILON || Math.abs(vz) > ROT_EPSILON) {
			var rot = getRotation();
			rot.setY((float) Math.atan2(vx, vz));
			setRotation(rot);
		}
	}

	// --- Navigation ---

	protected void setTarget(Vector3 globalPos, boolean addOffset)
	{
		if (globalPos == null) return;

		if (addOffset) {
			var ox = rng.randfRange(-1.5f, 1.5f);
			var oz = rng.randfRange(-1.5f, 1.5f);
			globalPos = new Vector3(
				globalPos.getX() + ox,
				globalPos.getY(),
				globalPos.getZ() + oz
			);
		}

		currentTarget = globalPos;
		if (navAgent != null) {
			var map = navAgent.getNavigationMap();
			if (map.isValid()) {
				currentTarget = NavigationServer3D.mapGetClosestPoint(map, globalPos);
			}
			navAgent.setTargetPosition(currentTarget);
		}
	}

	protected void setGhostMode(boolean enabled)
	{
		ghostMode = enabled;
		if (enabled) {
			for (int i = 1; i <= 4; i++) {
				setCollisionLayerValue(i, false);
				setCollisionMaskValue(i, false);
			}
		} else {
			setCollisionLayerValue(1, false);
			setCollisionLayerValue(2, true);
			setCollisionLayerValue(3, false);
			setCollisionLayerValue(4, false);

			setCollisionMaskValue(1, true);
			setCollisionMaskValue(2, false);
			setCollisionMaskValue(3, false);
			setCollisionMaskValue(4, false);
		}
	}

	private void moveTowardDirect(Vector3 target, Vector3 vel)
	{
		if (target == null) return;
		var dx = (float) target.getX() - (float) getGlobalPosition().getX();
		var dz = (float) target.getZ() - (float) getGlobalPosition().getZ();
		var dist = (float) Math.sqrt(dx * dx + dz * dz);
		if (dist >= ARRIVAL_DIST) {
			vel.setX((dx / dist) * speed);
			vel.setZ((dz / dist) * speed);
		}
	}

	// --- State machine ---

	private void advanceState(boolean fromArrival)
	{
		previousState = state;

		if (fromArrival) {
			handleArrival();
		} else {
			handleWaitExpiry();
		}

		if (previousState != state) {
			print("[NPC] " + getName() + ": " + previousState + " -> " + state + " | cel: " + formatPos(currentTarget));
			print(getName() + " status: " + state + " (wait=" + isWaiting + ")");
		}
	}

	private void handleArrival()
	{
		switch (state) {
			case STATE_TO_ENTRANCE:
				if (currentTarget != null) {
					setGlobalPosition(currentTarget);
				}
				setGhostMode(false);
				state = CustomerState.STATE_AT_SHELF;
				enterShopping();
				break;

			case STATE_AT_SHELF:
				onArriveAtShelf();
				startWaiting(rng.randfRange(2f, 5f));
				break;

			case STATE_TO_CASHIER:
				state = CustomerState.STATE_AT_CASHIER;
				startWaiting(rng.randfRange(3f, 6f));
				break;

			case STATE_TO_EXIT:
				setGhostMode(true);
				state = CustomerState.STATE_TO_SPAWN;
				if (spawnPoint != null) {
					setTarget(spawnPoint.getGlobalPosition(), false);
				}
				break;

			case STATE_TO_SPAWN:
				state = CustomerState.STATE_DESPAWN;
				notifyDespawn();
				queueFree();
				break;

			default:
				break;
		}
	}

	private void handleWaitExpiry()
	{
		switch (state) {
			case STATE_AT_SHELF:
				shelvesVisited++;
				boolean goToCashier = shelvesVisited >= MAX_SHELVES || rng.randf() < 0.35f;
				if (goToCashier) {
					state = CustomerState.STATE_TO_CASHIER;
					if (cashierPoint != null) {
						setTarget(cashierPoint.getGlobalPosition(), true);
					}
				} else {
					goToRandomShelf();
				}
				break;

			case STATE_AT_CASHIER:
				state = CustomerState.STATE_TO_EXIT;
				if (exitPoint != null) {
					setTarget(exitPoint.getGlobalPosition(), true);
				}
				break;

			default:
				break;
		}
	}

	private void enterShopping()
	{
		if (shelfWaypoints == null || shelfWaypoints.length == 0) {
			state = CustomerState.STATE_TO_CASHIER;
			if (cashierPoint != null) {
				setTarget(cashierPoint.getGlobalPosition(), true);
			}
			return;
		}

		shelvesVisited = 0;
		goToRandomShelf();
	}

	private void goToRandomShelf()
	{
		if (shelfWaypoints == null || shelfWaypoints.length == 0) {
			return;
		}
		int idx = rng.randiRange(0, shelfWaypoints.length - 1);
		var shelf = shelfWaypoints[idx];
		if (shelf != null) {
			setTarget(shelf.getGlobalPosition(), true);
		}
	}

	private void startWaiting(float duration)
	{
		isWaiting = true;
		waitTimer = duration;
	}

	// --- Virtual hook ---

	protected void onArriveAtShelf()
	{
	}

	// --- Despawn ---

	private void notifyDespawn()
	{
		if (spawnerRef != null) {
			spawnerRef.removeNpc(this);
			spawnerRef = null;
		}
	}

	// --- Public API ---

	public void damage(int points)
	{
		health -= points;
	}

	private static String formatPos(Vector3 p)
	{
		if (p == null) return "null";
		return "(" + String.format("%.1f", p.getX()) + ", " + String.format("%.1f", p.getZ()) + ")";
	}
}
