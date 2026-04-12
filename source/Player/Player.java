package Player;

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
    private float base_fov = 0f; // Set in editor in camera

    @RegisterFunction
    public void _ready()
    {
        if (camera != null) base_fov = camera.getFov();
        eyeLevel = baseEyeLevel;

        // Calculate jump speeds
        jumpGravity = (2 * jumpHeight) / pow(jumpPeakTime, 2);
        fallGravity = (2 * jumpHeight) / pow(jumpFallTime, 2);
        jumpVelocity = jumpGravity * jumpPeakTime;

        // Hide mouse cursor
        //Input.setMouseMode(Input.MouseMode.CAPTURED);
    }

    @RegisterFunction
    public void _unhandledInput(InputEvent event)
    {
        if (event instanceof InputEventMouseMotion ev) { // && Input.getMouseMode() == Input.MouseMode.CAPTURED) {
            // Rotate player left and right
            rotateY((float) -ev.getRelative().getX() * mouseSensitivity);
            // Rotate neck up and down
            if (neck != null) {
                var rotation = neck.getRotation();
                rotation.setY(getRotation().getY()); // Apply player rotation on y
                rotation.setX(clamp(rotation.getX() + (-ev.getRelative().getY() * mouseSensitivity), -PI/2f, PI/2f));
                neck.setRotation(rotation);
            }
        }
    }

    @RegisterFunction
    public void _process(double delta)
    {
        final float CAMERA_SMOOTHNESS = 50f;

        // Smoothly move neck
        if (neck != null) {
            var position = getPosition();
            position.setY(position.getY() + eyeLevel);
            neck.setPosition(lerp(neck.getPosition(), position, delta * CAMERA_SMOOTHNESS));
        }
    }

    @RegisterFunction
    public void _physicsProcess(double delta)
    {
        var velocity = getVelocity();
        var speed = walkSpeed;
        var fov = base_fov;

        var inputDir = Input.getVector("move_left", "move_right", "move_forward", "move_backward");
        var direction = getBasis().times(new Vector3(inputDir.getX(), 0, inputDir.getY())).normalized();

        // Apply gravity
        if (!isOnFloor()) {
            if (velocity.getY() > 0f) {
                velocity.setY(velocity.getY() - jumpGravity * delta);
            } else {
                velocity.setY(velocity.getY() - fallGravity * delta);
            }
        }

        // Jumping
        if (Input.isActionPressed("jump") && isOnFloor()) {
            velocity.setY(jumpVelocity);
        }

        // Sprinting
        if (Input.isActionPressed("sprint") && inputDir.getY() < 0) {
            speed = runSpeed;
            fov *= runFovModifier;
        }

        // Apply movement
        if (!direction.isZeroApprox()) {
            velocity.setX(direction.getX() * speed);
            velocity.setZ(direction.getZ() * speed);
        } else {
            if (isOnFloor()) {
                velocity.setX(moveToward(velocity.getX(), 0, speed));
                velocity.setZ(moveToward(velocity.getZ(), 0, speed));
            }
        }

        // Smoothly apply FOV change
        if (camera != null) {
            camera.setFov(lerp(camera.getFov(), fov, (float) delta * fovChangeSpeed));
        }

        // Finally apply velocity
        setVelocity(velocity);
        moveAndSlide();
    }
}
