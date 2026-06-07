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
		STATE_ENTRANCE_QUEUE,
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
	@RegisterProperty @Export public float arrivalThreshold = 1.5f;

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
	protected java.util.ArrayList<Node3D> entranceChildren = new java.util.ArrayList<>();
	protected int currentChildIndex = 0;

	private static final int MAX_SHELVES = 3;

	protected NavigationAgent3D navAgent;
	protected RandomNumberGenerator rng;

	protected float timeSinceSpawn = 0f;
	protected boolean ghostMode = true;

	// Anti-stuck
	protected float stuckTimer = 0f;
	private static final float STUCK_THRESHOLD = 2.0f;
	private static final float STUCK_VELOCITY_LIMIT = 0.1f;

	// Pre-allocated temp vectors (ZERO ALOKACJI w pętli)
	private Vector3 tempPos = new Vector3();
	private Vector3 tempTarget = new Vector3();

	private static final float ROT_EPSILON = 0.01f;
	private static final float MIN_ARRIVAL_TIME = 0.5f;
	private static final float ARRIVAL_DIST = 0.3f;
	private static final float GRAVITY = 9.8f;

	@RegisterFunction
	public void _ready()
	{
		var node = getNode("NavigationAgent3D");
		if (node instanceof NavigationAgent3D agent) {
			navAgent = agent;
			navAgent.setTargetDesiredDistance(1.5f);
			navAgent.setPathDesiredDistance(2.0f);
		}

		// Ghost mode start: wszystkie warstwy wyłączone — NPC lata przez ściany do entrance
		for (int i = 1; i <= 32; i++) {
			setCollisionLayerValue(i, false);
			setCollisionMaskValue(i, false);
		}

		rng = new RandomNumberGenerator();
		speed = rng.randfRange(minSpeed, maxSpeed);
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
			print("NPC " + getName() + " zmienił stan na: " + state);
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
		float dt = (float) delta;

		Vector3 vel = getVelocity();

		// Grawitacja — tylko gdy NPC nie lata (ghostMode = false)
		if (ghostMode) {
			vel.setY(0f);
		} else if (!isOnFloor()) {
			vel.setY((float) vel.getY() - GRAVITY * dt);
		}

		if (isWaiting) {
			waitTimer -= dt;
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
			// ANTI-STUCK: tylko składowa pozioma XZ (grawitacja nie może blokować detekcji)
			float hVel = (float) Math.sqrt(
				vel.getX() * vel.getX() + vel.getZ() * vel.getZ()
			);
			if (hVel < STUCK_VELOCITY_LIMIT && timeSinceSpawn >= 1.0f) {
				stuckTimer += dt;
				if (stuckTimer >= STUCK_THRESHOLD) {
					resolveStuck();
					stuckTimer = 0f;
					vel.setX(0f);
					vel.setZ(0f);
					setVelocity(vel);
					moveAndSlide();
					return;
				}
			} else {
				stuckTimer = 0f;
			}

			// Arrival check — tylko dystans XZ (Y = 0, bo setTarget blokuje Y)
			tempPos = getGlobalPosition();
			float dx = (float) (currentTarget.getX() - tempPos.getX());
			float dz = (float) (currentTarget.getZ() - tempPos.getZ());
			float distXZ = (float) Math.sqrt(dx * dx + dz * dz);

			if (distXZ <= arrivalThreshold && timeSinceSpawn >= MIN_ARRIVAL_TIME) {
				vel.setX(0f);
				vel.setZ(0f);
				setVelocity(vel);
				moveAndSlide();
				advanceState(true);
				stuckTimer = 0f;
				return;
			}

			// Ruch — tylko XZ. Y sterowane przez grawitację i moveAndSlide().
			if (ghostMode) {
				moveTowardDirect(currentTarget, vel);
			} else if (navAgent != null) {
				Vector3 nextPos = navAgent.getNextPathPosition();
				if (nextPos != null) {
					float pathDx = (float) (nextPos.getX() - tempPos.getX());
					float pathDz = (float) (nextPos.getZ() - tempPos.getZ());
					float distToPath = (float) Math.sqrt(pathDx * pathDx + pathDz * pathDz);
					if (distToPath > 0.5f) {
						moveTowardDirect(nextPos, vel);
					} else {
						moveTowardDirect(currentTarget, vel);
					}
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

		// Rotacja — tylko w osi Y, na podstawie kierunku XZ
		float vx = (float) vel.getX();
		float vz = (float) vel.getZ();
		if (Math.abs(vx) > ROT_EPSILON || Math.abs(vz) > ROT_EPSILON) {
			Vector3 rot = getRotation();
			rot.setY((float) Math.atan2(vx, vz));
			setRotation(rot);
		}
	}

	// --- BEZPIECZNE PUNKTY CELOWE z BLOKADĄ OSI Y ---

	protected void setTarget(Vector3 globalPos, boolean addOffset)
	{
		if (globalPos == null) return;

		if (addOffset) {
			float ox = rng.randfRange(-1.5f, 1.5f);
			float oz = rng.randfRange(-1.5f, 1.5f);
			tempTarget.setX(globalPos.getX() + ox);
			tempTarget.setY(globalPos.getY());
			tempTarget.setZ(globalPos.getZ() + oz);
		} else {
			tempTarget.setX(globalPos.getX());
			tempTarget.setY(globalPos.getY());
			tempTarget.setZ(globalPos.getZ());
		}

		if (navAgent != null) {
			var map = navAgent.getNavigationMap();
			if (map.isValid()) {
				currentTarget = NavigationServer3D.mapGetClosestPoint(map, tempTarget);
			} else {
				currentTarget = new Vector3(tempTarget.getX(), tempTarget.getY(), tempTarget.getZ());
			}
			// BLOKADA Y: wymuś wysokość NPC, aby NavAgent nie planował pionowej ścieżki
			tempPos = getGlobalPosition();
			currentTarget.setY(tempPos.getY());
			navAgent.setTargetPosition(currentTarget);
		} else {
			currentTarget = new Vector3(tempTarget.getX(), tempTarget.getY(), tempTarget.getZ());
			tempPos = getGlobalPosition();
			currentTarget.setY(tempPos.getY());
		}
	}

	// --- ANTI-STUCK: losowy punkt w promieniu 5m, przepuszczony przez mapGetClosestPoint ---

	private void resolveStuck()
	{
		tempPos = getGlobalPosition();
		float ox = rng.randfRange(-5.0f, 5.0f);
		float oz = rng.randfRange(-5.0f, 5.0f);
		tempTarget.setX(tempPos.getX() + ox);
		tempTarget.setY(tempPos.getY());
		tempTarget.setZ(tempPos.getZ() + oz);

		if (navAgent != null) {
			var map = navAgent.getNavigationMap();
			if (map.isValid()) {
				currentTarget = NavigationServer3D.mapGetClosestPoint(map, tempTarget);
			} else {
				currentTarget = new Vector3(tempTarget.getX(), tempTarget.getY(), tempTarget.getZ());
			}
			currentTarget.setY(tempPos.getY());
			navAgent.setTargetPosition(currentTarget);
		} else {
			currentTarget = new Vector3(tempTarget.getX(), tempTarget.getY(), tempTarget.getZ());
			currentTarget.setY(tempPos.getY());
		}

		stuckTimer = 0f;
	}

	// --- Pomocnicze ---

	protected void setGhostMode(boolean enabled)
	{
		ghostMode = enabled;
		if (enabled) {
			for (int i = 1; i <= 32; i++) {
				setCollisionLayerValue(i, false);
				setCollisionMaskValue(i, false);
			}
		} else {
			for (int i = 1; i <= 32; i++) {
				setCollisionLayerValue(i, false);
				setCollisionMaskValue(i, false);
			}
			// NPC = warstwa 2, maska = warstwa 1 (tylko podłoga)
			// Podłoga ma już collision_mask(2)=true przez patchEnvironmentCollision
			setCollisionLayerValue(2, true);
			setCollisionMaskValue(1, true);
		}
	}

	private void moveTowardDirect(Vector3 target, Vector3 vel)
	{
		if (target == null) return;

		tempPos = getGlobalPosition();
		float dx = (float) (target.getX() - tempPos.getX());
		float dz = (float) (target.getZ() - tempPos.getZ());
		float dist = (float) Math.sqrt(dx * dx + dz * dz);

		// Ustawia TYLKO X i Z — Y zostawione grawitacji i moveAndSlide()
		if (dist >= ARRIVAL_DIST) {
			vel.setX((dx / dist) * speed);
			vel.setZ((dz / dist) * speed);
		}
	}

	// --- State Machine ---

	private void advanceState(boolean fromArrival)
	{
		previousState = state;

		if (fromArrival) {
			handleArrival();
		} else {
			handleWaitExpiry();
		}

		if (previousState != state) {
			String childInfo = entranceChildren.isEmpty()
				? "done"
				: currentChildIndex + "/" + entranceChildren.size();
			print("[NPC] " + getName() + " Stan: " + state + " | Dziecko wejścia: " + childInfo);
		}
	}

	private void handleArrival()
	{
		switch (state) {
			case STATE_TO_ENTRANCE:
				if (currentTarget != null) {
					Vector3 pos = getGlobalPosition();
					pos.setY(currentTarget.getY());
					setGlobalPosition(pos);
				}
				setGhostMode(false);

				entranceChildren.clear();
				if (entrancePoint != null) {
					int count = entrancePoint.getChildCount();
					for (int i = 0; i < count; i++) {
						Node child = entrancePoint.getChild(i);
						if (child instanceof Node3D child3d) {
							entranceChildren.add(child3d);
						}
					}
				}
				currentChildIndex = 0;

				if (entranceChildren.isEmpty()) {
					state = CustomerState.STATE_AT_SHELF;
					enterShopping();
				} else {
					state = CustomerState.STATE_ENTRANCE_QUEUE;
					setTarget(entranceChildren.get(0).getGlobalPosition(), false);
				}
				break;

			case STATE_ENTRANCE_QUEUE:
				currentChildIndex++;
				if (currentChildIndex < entranceChildren.size()) {
					setTarget(entranceChildren.get(currentChildIndex).getGlobalPosition(), false);
					print("NPC " + getName() + " | Dziecko wejścia: " + currentChildIndex + "/" + entranceChildren.size());
				} else {
					entranceChildren.clear();
					currentChildIndex = 0;
					state = CustomerState.STATE_AT_SHELF;
					enterShopping();
				}
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
		Node3D shelf = shelfWaypoints[idx];
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

	protected void notifyDespawn()
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
}
