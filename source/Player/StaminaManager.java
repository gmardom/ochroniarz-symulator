package Player;

import godot.annotation.*;
import godot.api.*;
import godot.core.*;
import static godot.global.GD.*;

@RegisterClass
public class StaminaManager extends Node
{
	@RegisterProperty @Export public float maxStamina = 100f;
	@RegisterProperty @Export public float decayRate = 25f;
	@RegisterProperty @Export public float regenRate = 15f;
	@RegisterProperty @Export public float minSprintThreshold = 15f;
	@RegisterProperty @Export public float attackCost = 10f;
	@RegisterProperty @Export public ProgressBar staminaBar;
	@RegisterProperty @Export public Panel staminaPanel;

	private float currentStamina;
	private boolean exhausted = false;
	private float regenBlockTimer = 0f;
	private static final float REGEN_BLOCK_DURATION = 0.3f;

	@RegisterFunction
	public void _ready()
	{
		currentStamina = maxStamina;

		if (staminaBar != null) {
			staminaBar.setMax(maxStamina);
			staminaBar.setValue(currentStamina);
			staminaBar.setCustomMinimumSize(new Vector2(200, 14));
			staminaBar.setShowPercentage(false);
		}
		if (staminaPanel != null) {
			staminaPanel.setCustomMinimumSize(new Vector2(208, 20));
		}
		print("StaminaManager ready: " + (int) currentStamina + "/" + (int) maxStamina);
	}

	@RegisterFunction
	public void _process(double delta)
	{
		if (currentStamina <= 0f && !exhausted) {
			exhausted = true;
			print("Stamina exhausted!");
		} else if (currentStamina > minSprintThreshold) {
			exhausted = false;
		}

		if (regenBlockTimer > 0f) {
			regenBlockTimer -= (float) delta;
		} else if (currentStamina < maxStamina) {
			currentStamina = Math.min(maxStamina, currentStamina + regenRate * (float) delta);
		}

		if (staminaBar != null) {
			staminaBar.setValue(currentStamina);
		}
	}

	public boolean canSprint()
	{
		return currentStamina > minSprintThreshold && !exhausted;
	}

	public boolean canAttack()
	{
		return currentStamina >= attackCost;
	}

	public void consume(float amount)
	{
		float before = currentStamina;
		currentStamina = Math.max(0f, currentStamina - amount);
		regenBlockTimer = REGEN_BLOCK_DURATION;
		if ((int) before != (int) currentStamina) {
			print("Stamina: " + (int) currentStamina + "/" + (int) maxStamina);
		}
		if (staminaBar != null) {
			staminaBar.setValue(currentStamina);
		}
	}

	public float getCurrent()
	{
		return currentStamina;
	}
}
