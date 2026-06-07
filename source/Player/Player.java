package Player;

import Game.*;
import Enemy.*;
import NPC.*;
import godot.annotation.*;
import godot.api.*;
import godot.core.*;

import static godot.global.GD.*;
import static java.lang.Math.PI;

@RegisterClass
public class Player extends CharacterBody3D
{
	@RegisterProperty @Export public Node3D neck;
	@RegisterProperty @Export public Camera3D camera;
	@RegisterProperty @Export public float baseEyeLevel = 1.6f;
	@RegisterProperty @Export public float crouchEyeLevel = 0.8f;
	private float eyeLevel;

	@RegisterProperty @Export public float walkSpeed = 5.0f;
	@RegisterProperty @Export public float runSpeed = 7.0f;
	@RegisterProperty @Export public float mouseSensitivity = 0.002f;

	@RegisterProperty @Export public float jumpPeakTime = 0.45f;
	@RegisterProperty @Export public float jumpFallTime = 0.45f;
	@RegisterProperty @Export public float jumpHeight = 1.5f;
	@RegisterProperty @Export public float jumpDistance = 4.0f;
	private float jumpGravity;
	private float fallGravity;
	private float jumpVelocity;

	@RegisterProperty @Export public float fovChangeSpeed = 10f;
	@RegisterProperty @Export public float runFovModifier = 1.15f;
	private float base_fov = 0f;

	@RegisterProperty @Export public HeadsUpDisplay hud;
	@RegisterProperty @Export public RayCast3D interactionRayCast;
	private boolean interactionRayCastHit = false;

	@RegisterProperty @Export public AnimationPlayer weaponAnimationPlayer;
	private boolean attacking = false;

	@RegisterProperty @Export public StaminaManager staminaManager;
	private boolean isSprinting = false;
	private boolean isSlipping = false;
	private float slipTimer = 0f;
	private boolean isDragging = false;
	private Enemy draggedEnemy = null;
	private boolean isInDropZone = false;

	@RegisterProperty @Export public float dragMoveSpeedMultiplier = 0.45f;
	@RegisterProperty @Export public Vector3 dragOffset = new Vector3(0, 0, 1.5f);

	public boolean isSprinting() { return isSprinting; }

	public boolean isDragging() { return isDragging; }

	public void setInDropZone(boolean value) { isInDropZone = value; }

	@RegisterFunction
	public void _ready()
	{
		if (camera != null) base_fov = camera.getFov();
		eyeLevel = baseEyeLevel;

		jumpGravity = (2 * jumpHeight) / pow(jumpPeakTime, 2);
		fallGravity = (2 * jumpHeight) / pow(jumpFallTime, 2);
		jumpVelocity = jumpGravity * jumpPeakTime;

		if (hud != null) hud.stopInteraction();

		if (weaponAnimationPlayer != null) {
			weaponAnimationPlayer.stop();
			weaponAnimationPlayer.play("Idle");
		}

		if (interactionRayCast != null) {
			interactionRayCast.setCollisionMaskValue(2, true);
		}

		if (hud != null && GameManager.I() != null) {
			GameManager.I().gameHud = hud;
		}

		setCollisionLayerValue(8, true);
	}

	@RegisterFunction
	public void _unhandledInput(InputEvent event)
	{
		if (event instanceof InputEventMouseMotion ev) {
			rotateY((float) -ev.getRelative().getX() * mouseSensitivity);
			if (neck != null) {
				var rotation = neck.getRotation();
				rotation.setY(getRotation().getY());
				rotation.setX(clamp(rotation.getX() + (-ev.getRelative().getY() * mouseSensitivity), -PI/2f, PI/2f));
				neck.setRotation(rotation);
			}
		}
	}

	@RegisterFunction
	public void _process(double delta)
	{
		final float CAMERA_SMOOTHNESS = 50f;

		if (neck != null) {
			var position = getPosition();
			position.setY(position.getY() + eyeLevel);
			var t = (float) Math.min(delta * CAMERA_SMOOTHNESS, 1.0);
			neck.setPosition(lerp(neck.getPosition(), position, t));
		}

		if (isSlipping) {
			slipTimer -= (float) delta;
			if (slipTimer <= 0f) {
				isSlipping = false;
				print("Player recovered");
			}
		}

		if (isSlipping || isDragging) return;

		if (Input.isActionJustPressed("attack")) {
			if (staminaManager != null && !staminaManager.canAttack()) {
				print("Player attack missed — not enough stamina");
				return;
			}
			if (staminaManager != null) staminaManager.consume(staminaManager.attackCost);

			if (weaponAnimationPlayer != null) {
				weaponAnimationPlayer.stop();
				weaponAnimationPlayer.play("Attack");
				weaponAnimationPlayer.queue("Idle");
			}

			if (interactionRayCast != null && interactionRayCast.isColliding()) {
				var collider = interactionRayCast.getCollider();
				if (collider instanceof Enemy enemy) {
					enemy.takeDamage(1);
					print("Player attack hit");
				} else {
					print("Player attack missed — not an enemy");
				}
			} else {
				print("Player attack missed — nothing in range");
			}
		}
	}

	@RegisterFunction
	public void _physicsProcess(double delta)
	{
		if (GameManager.I().currentState == GameManager.State.Paused) return;

		var velocity = getVelocity();
		var speed = walkSpeed;
		var fov = base_fov;

		var inputDir = Input.getVector("move_left", "move_right", "move_forward", "move_backward");
		if (isSlipping) inputDir = new Vector2(0, 0);
		var direction = getBasis().times(new Vector3(inputDir.getX(), 0, inputDir.getY())).normalized();

		if (!isOnFloor()) {
			if (velocity.getY() > 0f) {
				velocity.setY(velocity.getY() - jumpGravity * delta);
			} else {
				velocity.setY(velocity.getY() - fallGravity * delta);
			}
		}

		if (!isSlipping && Input.isActionPressed("jump") && isOnFloor()) {
			velocity.setY(jumpVelocity);
		}

		isSprinting = !isDragging && Input.isActionPressed("sprint") && inputDir.getY() < 0;
		if (isSprinting && staminaManager != null && !staminaManager.canSprint()) {
			isSprinting = false;
		}
		if (isSprinting) {
			speed = runSpeed;
			fov *= runFovModifier;
			if (staminaManager != null) staminaManager.consume(staminaManager.decayRate * (float) delta);
		}
		if (isDragging) {
			speed = walkSpeed * dragMoveSpeedMultiplier;
		}

		if (!direction.isZeroApprox()) {
			velocity.setX(direction.getX() * speed);
			velocity.setZ(direction.getZ() * speed);
		} else {
			if (isOnFloor()) {
				velocity.setX(moveToward(velocity.getX(), 0, speed));
				velocity.setZ(moveToward(velocity.getZ(), 0, speed));
			}
		}

		if (camera != null) {
			camera.setFov(lerp(camera.getFov(), fov, (float) delta * fovChangeSpeed));
		}

		setVelocity(velocity);
		moveAndSlide();

		if (isDragging && draggedEnemy != null) {
			var pos = getGlobalPosition();
			var offset = getBasis().times(dragOffset);
			draggedEnemy.setGlobalPosition(new Vector3(
				pos.getX() + offset.getX(),
				pos.getY() + offset.getY(),
				pos.getZ() + offset.getZ()
			));
		}

		if (isDragging && isInDropZone) {
			dropDeliver();
		}

		if (!isSlipping && !isDragging && GameLoop.I() != null && interactionRayCast != null && interactionRayCast.isColliding()) {
			var collider = (Node3D) interactionRayCast.getCollider();

			if (collider.isInGroup("interactable")) {
				if (collider instanceof Interactable interactable) {
					if (hud != null) hud.startInteraction("Interakcja");
					if (Input.isActionJustPressed("interact")) {
						interactable.interact(this);
						if (hud != null) hud.stopInteraction();
					}
				} else {
					String name = collider.getName().toString();

					if (name.equals("PC")) {
						if (!GameLoop.I().isShiftActive()) {
							if (hud != null) hud.startInteraction("Zacznij prace");
							if (Input.isActionJustPressed("interact")) {
								GameLoop.I().startShift();
								if (hud != null) hud.stopInteraction();
							}
						} else {
							if (hud != null) hud.startInteraction("Koniec zmiany");
							if (Input.isActionJustPressed("interact")) {
								if (hud != null) hud.stopInteraction();
							}
						}
					} else if (name.equals("Bed")) {
						if (hud != null) hud.startInteraction("Zapisz gre");
						if (Input.isActionJustPressed("interact")) {
							GameLoop.I().saveGame();
							if (hud != null) hud.stopInteraction();
						}
					}
				}
			} else if (collider instanceof Enemy enemy) {
				if (enemy.canBeDragged()) {
					if (hud != null) hud.startInteraction("Przeciagnij");
					if (Input.isActionJustPressed("interact")) {
						isDragging = true;
						draggedEnemy = enemy;
						enemy.setDragged(true);
						if (hud != null) hud.stopInteraction();
						print("Started dragging enemy");
					}
				} else {
					if (hud != null) hud.startInteraction("Interakcja");
				}
			} else {
				if (hud != null) hud.startInteraction("Interakcja");
			}
		} else {
			if (hud != null) hud.stopInteraction();
		}
	}

	public void dropDeliver()
	{
		if (!isDragging || draggedEnemy == null) return;

		print("Stopped dragging enemy");
		draggedEnemy.deliver();
		draggedEnemy = null;
		isDragging = false;
		print("Enemy delivered");

		if (GameManager.I() != null) {
			GameManager.I().registerResolvedIncident();
		}
	}

	public void applySlip(float duration)
	{
		isSlipping = true;
		slipTimer = duration;
		print("Player slipped");
	}
}
