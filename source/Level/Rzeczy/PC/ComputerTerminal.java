package Level.Rzeczy.PC;

import Game.GameManager;
import Game.Interactable;
import Player.Player;
import godot.annotation.*;
import godot.api.*;

@RegisterClass
public class ComputerTerminal extends StaticBody3D implements Interactable
{
	@RegisterFunction
	public void _ready()
	{
		addToGroup("interactable");
	}

	@Override
	public void interact(Player player)
	{
		switch (GameManager.I().gameState) {
			case WAITING_FOR_START -> {
				GameManager.I().startShift();
			}
			case SHIFT_ACTIVE -> {
				// Future: konczenie zmiany / podsumowanie
			}
			case GAME_OVER -> {
				// Future: blokada interakcji po przegranej
			}
		}
	}
}
