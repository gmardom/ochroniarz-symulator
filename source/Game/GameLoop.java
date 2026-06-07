package Game;

import Player.HeadsUpDisplay;
import godot.annotation.RegisterClass;
import godot.annotation.RegisterFunction;
import godot.api.Node;
import godot.api.Input;
import static godot.global.GD.*;

@RegisterClass
public class GameLoop extends Node
{
	private static GameLoop I = null;
	public static GameLoop I() { return I; }

	public static final float HOUR_DURATION = 3f * 60f;
	public static final int WORK_HOURS = 8;

	private float timeAccumulator = 0f;
	private int currentHour = 0;
	private boolean shiftActive = false;

	public int caughtThisShift = 0;
	public int robbedThisShift = 0;

	public SaveData saveData = new SaveData();
	public HeadsUpDisplay hud;

	@RegisterFunction
	public void _enterTree() { I = this; }

	@RegisterFunction
	public void _ready()
	{
		setProcess(false);
		var player = getNode("/root/Game/Level/Player");
		if (player != null) {
			hud = (HeadsUpDisplay) player.getNode("HeadsUpDisplay");
			print("GameLoop hud: " + hud);
		} else {
			print("Player not found!");
		}
	}

	@RegisterFunction
	public void _process(double delta)
	{
		if (!shiftActive) return;
		if (GameManager.I().currentState == GameManager.State.Paused) return;

		timeAccumulator += (float) delta;
		print("timeAccumulator: " + timeAccumulator);

		if (timeAccumulator >= HOUR_DURATION) {
			timeAccumulator -= HOUR_DURATION;
			currentHour++;
			if (hud != null) hud.updateClock(currentHour);
			if (currentHour >= WORK_HOURS) {
				endShift();
			}
		}
	}

	public void startShift()
	{
		shiftActive = true;
		currentHour = 0;
		timeAccumulator = 0f;
		caughtThisShift = 0;
		robbedThisShift = 0;
		setProcess(true);
		print("startShift called, setProcess(true)");
		if (hud != null) hud.updateClock(0);
	}

	public void endShift()
	{
		shiftActive = false;
		setProcess(false);
		saveData.totalCaught += caughtThisShift;
		saveData.totalRobbed += robbedThisShift;
		saveData.day++;
		if (hud != null) hud.showShiftSummary(caughtThisShift, robbedThisShift);
	}

	public void saveGame()
	{
		SaveManager.save(saveData);
	}

	public void loadGame(boolean newGame)
	{
		if (newGame || !SaveManager.hasSave()) {
			saveData = new SaveData();
		} else {
			saveData = SaveManager.load();
		}
	}

	public boolean isShiftActive() { return shiftActive; }
	public int getCurrentHour() { return currentHour; }
}
