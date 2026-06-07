package Enemy;

import Game.GameManager;
import NPC.NPCBase;
import godot.annotation.*;
import godot.api.*;
import godot.core.*;
import godot.global.GD;
import static godot.global.GD.*;

@RegisterClass
public class Enemy extends NPCBase
{
	@RegisterProperty @Export public float steal_chance = 0.4f;
	@RegisterProperty @Export public float steal_duration = 6.0f;
	@RegisterProperty @Export public float stolen_speed_multiplier = 0.5f;

	// --- Faza 3: Combat ---
	@RegisterProperty @Export public int maxHealth = 3;

	private int health;

	private float baseSpeed;
	private boolean hasStolenBeer = false;
	private boolean wantsToSteal = false;
	private boolean escapeRegistered = false;
	private boolean puddleSpawned = false;
	private boolean isBeingDragged = false;

	@RegisterFunction
	@Override
	public void _ready()
	{
		hostile = true;
		minSpeed = 3.0f;
		maxSpeed = 6.0f;
		super._ready();
		baseSpeed = speed;
		health = maxHealth;
		print("[Enemy] " + getName() + " gotowy | speed=" + speed + " | steal_chance=" + steal_chance + " | health=" + health + "/" + maxHealth);
	}

	public boolean canBeDragged()
	{
		return state == CustomerState.STATE_KNOCKED_OUT && !isBeingDragged;
	}

	public void setDragged(boolean dragged)
	{
		isBeingDragged = dragged;
		if (dragged) {
			setGhostMode(true);
		}
	}

	public void deliver()
	{
		isBeingDragged = false;
		notifyDespawn();
		queueFree();
	}

	@RegisterFunction
	@Override
	public void _physicsProcess(double delta)
	{
		if (isBeingDragged) return;

		// Nie przetwarzaj AI jesli ogłuszony
		if (state == CustomerState.STATE_KNOCKED_OUT) {
			super._physicsProcess(delta);
			return;
		}

		// Zastosuj mnoznik predkosci jesli skradziono piwo (Faza 2)
		speed = baseSpeed * (hasStolenBeer ? stolen_speed_multiplier : 1f);

		// Nadpisz czas postoju na steal_duration przy pierwszej klatce po onArriveAtShelf
		if (wantsToSteal && state == CustomerState.STATE_AT_SHELF && isWaiting) {
			waitTimer = steal_duration;
			wantsToSteal = false;
			print("[Enemy] " + getName() + " Kradnie piwo przez " + steal_duration + "s | Stan: " + state + " | Skradzione piwo: " + hasStolenBeer);
		}

		super._physicsProcess(delta);

		// Po super: przekieruj zlodzieja z kasy do wyjscia
		if (hasStolenBeer) {
			if (state == CustomerState.STATE_TO_CASHIER) {
				state = CustomerState.STATE_TO_EXIT;
				if (exitPoint != null) {
					setTarget(exitPoint.getGlobalPosition(), true);
				}
				print("[Enemy] " + getName() + " Pominieto kase | Stan: " + state + " | Skradzione piwo: " + hasStolenBeer);

			} else if (state == CustomerState.STATE_AT_CASHIER) {
				state = CustomerState.STATE_TO_EXIT;
				if (exitPoint != null) {
					setTarget(exitPoint.getGlobalPosition(), true);
				}
				print("[Enemy] " + getName() + " Pominieto kase (AT) | Stan: " + state + " | Skradzione piwo: " + hasStolenBeer);
			}
		}

		// Po dotarciu do wyjscia, przed despawnem: rejestruj ucieczke
		if (!escapeRegistered && hasStolenBeer
			&& state == CustomerState.STATE_TO_SPAWN
			&& previousState == CustomerState.STATE_TO_EXIT)
		{
			if (GameManager.I() != null) {
				GameManager.I().registerThiefEscape(true);
			}
			escapeRegistered = true;
			print("[Enemy] " + getName() + " Uciekl z piwem! Zarejestrowano.");
		}
	}

	@Override
	protected void onArriveAtShelf()
	{
		if (hasStolenBeer) return;

		if (rng.randf() < steal_chance) {
			hasStolenBeer = true;
			wantsToSteal = true;
			print("[Enemy] " + getName() + " Zaczyna kradziez piwa! (szansa=" + steal_chance + ")");
		}
	}

	// --- Faza 3: Combat ---

	public void takeDamage(int amount)
	{
		if (state == CustomerState.STATE_KNOCKED_OUT) {
			return;
		}

		health -= amount;
		print("[Enemy] " + getName() + " Enemy damaged | health=" + health + "/" + maxHealth);

		if (health <= 0) {
			knockout();
		}
	}

	private void knockout()
	{
		state = CustomerState.STATE_KNOCKED_OUT;
		isWaiting = false;
		setVelocity(new Vector3(0, 0, 0));

		if (animationPlayer != null && animationPlayer.hasAnimation("knockout")) {
			animationPlayer.play("knockout");
		}

		print("[Enemy] " + getName() + " Enemy knocked out");

		spawnPuddle();
	}

	private void spawnPuddle()
	{
		if (puddleSpawned) return;
		puddleSpawned = true;

		var scene = (PackedScene) GD.load("res://scenes/Puddle.tscn");
		if (scene != null) {
			var instance = scene.instantiate();
			if (instance instanceof Node3D puddleNode) {
				puddleNode.setGlobalPosition(getGlobalPosition());
			}
			var parent = getParent();
			if (parent != null) {
				parent.addChild(instance);
				print("[Enemy] " + getName() + " Puddle spawned at " + getGlobalPosition());
			}
		} else {
			print("[Enemy] " + getName() + " Puddle.tscn not found — create at res://scenes/Puddle.tscn");
		}
	}

	@Override
	public void damage(int points)
	{
		// Faza 3: uzywamy takeDamage() zamiast starego systemu
	}
}
