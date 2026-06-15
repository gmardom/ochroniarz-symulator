package Game;

import godot.annotation.RegisterClass;
import godot.annotation.RegisterFunction;
import godot.api.Node;

@RegisterClass
public class GameLoop extends Node
{
	private static GameLoop I = null;
	public static GameLoop I() { return I; }

	public static final float HOUR_DURATION = 60f;
	public static final int WORK_HOURS = 8;

	private float timeAccumulator = 0f;
	private int currentHour = 0;
	private int lastDisplayedMinute = -1;
	private boolean shiftActive = false;

	public int caughtThisShift = 0;
	public int robbedThisShift = 0;

	public SaveData saveData = new SaveData();

	public Player.HeadsUpDisplay hud;

	@RegisterFunction
	public void _enterTree() { I = this; }

	@RegisterFunction
	public void _ready()
	{
		setProcess(false);
	}

	@RegisterFunction
	public void _process(double delta)
	{
		if (!shiftActive) return;
		if (GameManager.I().currentState == GameManager.State.Paused) return;

		timeAccumulator += (float) delta;

		if (timeAccumulator >= HOUR_DURATION) {
			timeAccumulator -= HOUR_DURATION;
			currentHour++;
			lastDisplayedMinute = -1;

			if (currentHour >= 8 + WORK_HOURS) {
				endShift();
			}
		}

		int displayMinute = (int)(timeAccumulator / HOUR_DURATION * 60);
		if (displayMinute != lastDisplayedMinute && displayMinute % 5 == 0) {
			lastDisplayedMinute = displayMinute;
			if (hud != null) hud.updateClock(currentHour, displayMinute);
		}
	}

	public void startShift()
	{
		// Spróbuj znaleźć HUD jeśli jeszcze nie przypisany
		if (hud == null) {
			var playerNode = getNodeOrNull("/root/Game/Level/Player");
			if (playerNode instanceof Node n) {
				var hudNode = n.getNodeOrNull("HeadsUpDisplay");
				if (hudNode instanceof Player.HeadsUpDisplay h) {
					hud = h;
				}
			}
		}

		shiftActive = true;
		currentHour = 8;
		timeAccumulator = 0f;
		lastDisplayedMinute = -1;
		caughtThisShift = 0;
		robbedThisShift = 0;
		setProcess(true);
		if (hud != null) hud.updateClock(currentHour, 0);
	}

	public void endShift()
	{
		shiftActive = false;
		setProcess(false);

		saveData.totalCaught += caughtThisShift;
		saveData.totalRobbed += robbedThisShift;
		saveData.day++;

		// Powiadom GameManager o zakończeniu zmiany
		if (GameManager.I() != null) {
			GameManager.I().completeShift();
		}
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
