package Game;

import Menu.*;
import Player.HeadsUpDisplay;
import Transition.*;
import godot.annotation.*;
import godot.api.*;
import godot.global.GD;
import static java.util.Objects.requireNonNull;

@RegisterClass
public final class GameManager extends Node
{
	private static GameManager I = null;
	public static GameManager I() { return I; }

	public Node gameRoot;
	public TransitionPlayer transition;

	public MenuMain mainMenu;
	public MenuPause menuPause;
	public Node3D gameLevel;

	// --- Application state (menu, playing, paused) ---
	public enum State
	{
		Nil,
		Starting,
		Menu,
		Playing,
		Paused,
	};

	public State initialState = State.Starting;
	public State currentState = State.Nil;

	// --- Gameplay loop state (Faza 1, rozszerzone Faza 6) ---
	public enum GameState
	{
		WAITING_FOR_START,
		SHIFT_ACTIVE,
		SHIFT_COMPLETE,
		GAME_OVER,
	}

	public GameState gameState = GameState.WAITING_FOR_START;

	// --- Escape counter ---
	private int escapedThievesCount = 0;
	@RegisterProperty @Export public int max_escapes_to_lose = 5;

	// --- Resolved incidents counter (Faza 5/6) ---
	private int resolvedIncidentsCount = 0;

	// --- Shift timer (Faza 6) ---
	@RegisterProperty @Export public float shiftDurationSeconds = 300f;
	private float shiftTimer = 0f;

	// --- HUD reference ---
	public HeadsUpDisplay gameHud;

	@RegisterFunction
	public void _enterTree()
	{
		I = this;
		gameRoot = getNode("/root/Game");
		transition = loadScene("res://source/Transition/TransitionPlayer.tscn", this);
		currentState = initialState;
	}

	@RegisterFunction
	public void _ready()
	{
		switch (currentState) {
		case Starting -> { loadMenu(); }
		case Menu -> { loadMenu(); }
		case Playing -> { loadGame(); }
		case Paused -> { loadGame(); pauseGame(); }
		}
	}

	@RegisterFunction
	public void _process(double delta)
	{
		if (gameState == GameState.SHIFT_ACTIVE) {
			shiftTimer -= (float) delta;
			if (shiftTimer <= 0f) {
				completeShift();
			}
		}

		switch (currentState) {
		case Playing -> {
			if (Input.isActionJustPressed("ui_cancel")) {
				pauseGame();
			}
		}
		case Paused -> {
			if (Input.isActionJustPressed("ui_cancel")) {
				unpauseGame();
			}
		}
		}
	}

	public void loadMenu()
	{
		currentState = State.Menu;
		transition.in();
		{
			if (mainMenu == null) mainMenu = loadScene("res://source/Menu/MenuMain.tscn", gameRoot);
			if (mainMenu != null) mainMenu.setVisible(true);

			if (gameLevel != null) gameLevel.queueFree();
			gameLevel = null;
			if (menuPause != null) menuPause.queueFree();
			menuPause = null;

			Input.setMouseMode(Input.MouseMode.VISIBLE);
		}
		transition.out();
	}

	public void loadGame()
	{
		currentState = State.Playing;
		transition.in();
		{
			if (mainMenu != null) mainMenu.setVisible(false);

			if (gameLevel == null) gameLevel = loadScene("res://source/Level/Level.tscn", gameRoot);
			if (menuPause == null) menuPause = loadScene("res://source/Menu/MenuPause.tscn", gameRoot);
			if (menuPause != null) menuPause.setVisible(false);

			Input.setMouseMode(Input.MouseMode.CAPTURED);
		}
		transition.out();
	}

	public void pauseGame()
	{
		currentState = State.Paused;
		if (menuPause != null) menuPause.setVisible(true);
		Input.setMouseMode(Input.MouseMode.VISIBLE);
	}

	public void unpauseGame()
	{
		currentState = State.Playing;
		if (menuPause != null) menuPause.setVisible(false);
		Input.setMouseMode(Input.MouseMode.CAPTURED);
	}

	public void exit()
	{
		requireNonNull(getTree()).quit();
	}

	// --- Gameplay loop methods ---

	public void startShift()
	{
		gameState = GameState.SHIFT_ACTIVE;
		escapedThievesCount = 0;
		resolvedIncidentsCount = 0;
		shiftTimer = shiftDurationSeconds;
		GameLoop.I().startShift();
		GD.print("Shift started");

		if (gameHud != null) gameHud.showShiftActive();
		refreshHud();
	}

	public void endShift()
	{
		gameState = GameState.WAITING_FOR_START;
		GameLoop.I().endShift();
		GD.print("Zmiana zakonczona — stan: WAITING_FOR_START");
	}

	private void completeShift()
	{
		if (gameState != GameState.SHIFT_ACTIVE) return;

		gameState = GameState.SHIFT_COMPLETE;
		GameLoop.I().endShift();
		GD.print("Shift completed");

		if (gameHud != null) gameHud.showShiftComplete(resolvedIncidentsCount, escapedThievesCount);
	}

	public void registerResolvedIncident()
	{
		resolvedIncidentsCount++;
		GD.print("Incident resolved");
		refreshHud();
	}

	public void registerEscapedThief()
	{
		escapedThievesCount++;
		GD.print("Escaped thief registered");

		if (escapedThievesCount >= max_escapes_to_lose) {
			gameState = GameState.GAME_OVER;
			GD.print("Game over");
			if (gameHud != null) gameHud.showGameOver();
		}

		refreshHud();
	}

	public void registerThiefEscape(boolean hadBeer)
	{
		if (!hadBeer) return;
		registerEscapedThief();
	}

	public int getEscapedThievesCount() { return escapedThievesCount; }
	public int getResolvedIncidentsCount() { return resolvedIncidentsCount; }
	public boolean isShiftActive() { return gameState == GameState.SHIFT_ACTIVE; }

	private void refreshHud()
	{
		if (gameHud != null) gameHud.updateStats(escapedThievesCount, resolvedIncidentsCount);
	}

	public static boolean getFullscreen()
	{
		return DisplayServer.windowGetMode() == DisplayServer.WindowMode.FULLSCREEN;
	}

	public static void setFullscreen(boolean isFullscreen)
	{
		if (isFullscreen) {
			DisplayServer.windowSetMode(DisplayServer.WindowMode.WINDOWED);
		} else {
			DisplayServer.windowSetMode(DisplayServer.WindowMode.FULLSCREEN);
		}
	}

	public static void toggleFullscreen()
	{
		setFullscreen(getFullscreen());
	}

	@SuppressWarnings("unchecked")
	private static <T extends Node> T loadScene(String path, Node root)
	{
		var scene = (PackedScene) GD.load(path);
		assert scene != null;
		var instance = scene.instantiate();
		root.addChild(instance);
		return (T) instance;
	}
}
