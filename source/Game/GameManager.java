package Game;

import Menu.*;
import Transition.*;

import godot.annotation.*;
import godot.api.*;
import godot.global.GD;

import static java.util.Objects.requireNonNull;

@RegisterClass
public final class GameManager extends Node
{
	// Global instance for easy access from code
	private static GameManager I = null;
	public static GameManager I() { return I; }

	// Global state
	public Node gameRoot;
	public TransitionPlayer transition;

	// Loaded scenes
	public MenuMain mainMenu;
	public MenuPause menuPause;
	public Node3D gameLevel;

	// Program state
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

	@RegisterFunction
	public void _enterTree()
	{
		// Setup global instance
		I = this;

		// Gather and create necessary resources
		gameRoot = getNode("/root/Game");
		transition = loadScene("res://source/Transition/TransitionPlayer.tscn", this);

		// Finish loading
		currentState = initialState;
	}

	@RegisterFunction
	public void _ready()
	{
		switch (currentState) {
			case Starting -> {
				// NOTE: Currently doing nothing. Later show studio logo or something.
				loadMenu();
			}
			case Menu -> {
				loadMenu();
			}
			case Playing -> {
				loadGame();
			}
			case Paused -> {
				loadGame();
				pauseGame();
			}
		}
	}

	@RegisterFunction
	public void _process(double delta)
	{
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
			// Load menu
			if (mainMenu == null) mainMenu = loadScene("res://source/Menu/MenuMain.tscn", gameRoot);
			if (mainMenu != null) mainMenu.setVisible(true);

			// Unload game
			if (gameLevel != null) gameLevel.queueFree();
			gameLevel = null;
			if (menuPause != null) menuPause.queueFree();
			menuPause = null;

			// Make cursor visible
			Input.setMouseMode(Input.MouseMode.VISIBLE);
		}
		transition.out();
	}

	public void loadGame()
	{
		currentState = State.Playing;
		transition.in();
		{
			// Hide the main menu
			if (mainMenu != null) mainMenu.setVisible(false);

			// Load the game scenes and assets
			if (gameLevel == null) gameLevel = loadScene("res://source/Level/Level.tscn", gameRoot);
			if (menuPause == null) menuPause = loadScene("res://source/Menu/MenuPause.tscn", gameRoot);
			if (menuPause != null) menuPause.setVisible(false);

			// Hide the cursor
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
