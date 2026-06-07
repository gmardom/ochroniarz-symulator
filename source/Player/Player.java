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
			neck.setPosition(lerp(neck.getPosition(), position, delta * CAMERA_SMOOTHNESS));
		}

		if (Input.isActionJustPressed("attack")) {
			if (staminaManager != null && !staminaManager.canAttack()) {
				print("Not enough stamina to attack!");
			} else if (weaponAnimationPlayer != null && !weaponAnimationPlayer.getCurrentAnimation().equals("Attack")) {
				if (staminaManager != null) staminaManager.consume(staminaManager.attackCost);

				weaponAnimationPlayer.stop();
				weaponAnimationPlayer.play("Attack");
				weaponAnimationPlayer.queue("Idle");

				if (interactionRayCast != null && interactionRayCast.isColliding()) {
					var collider = interactionRayCast.getCollider();
					if (collider instanceof Enemy enemy) {
						enemy.damage(20);
					}
				}
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
		var direction = getBasis().times(new Vector3(inputDir.getX(), 0, inputDir.getY())).normalized();

		if (!isOnFloor()) {
			if (velocity.getY() > 0f) {
				velocity.setY(velocity.getY() - jumpGravity * delta);
			} else {
				velocity.setY(velocity.getY() - fallGravity * delta);
			}
		}

		if (Input.isActionPressed("jump") && isOnFloor()) {
			velocity.setY(jumpVelocity);
		}

		isSprinting = Input.isActionPressed("sprint") && inputDir.getY() < 0;
		if (isSprinting && staminaManager != null && !staminaManager.canSprint()) {
			isSprinting = false;
		}
		if (isSprinting) {
			speed = runSpeed;
			fov *= runFovModifier;
			if (staminaManager != null) staminaManager.consume(staminaManager.decayRate * (float) delta);
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

		if (GameLoop.I() != null && interactionRayCast != null && interactionRayCast.isColliding()) {
			var collider = (Node3D) interactionRayCast.getCollider();

			if (collider.isInGroup("interactable")) {
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
			} else {
				if (hud != null) hud.startInteraction("Interakcja");
			}
		} else {
			if (hud != null) hud.stopInteraction();
		}
	}
}
