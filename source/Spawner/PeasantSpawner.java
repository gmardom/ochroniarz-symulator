package Spawner;

import java.util.ArrayList;
import java.util.List;

import NPC.NPCBase;
import godot.annotation.*;
import godot.api.*;
import godot.core.*;
import static godot.global.GD.*;

@RegisterClass
public class PeasantSpawner extends Node3D implements NPCBase.NpcSpawner
{
	@RegisterProperty @Export public PackedScene civilianScene;
	@RegisterProperty @Export public PackedScene enemyScene;

	@RegisterProperty @Export public Node3D spawnPoint;
	@RegisterProperty @Export public Node3D entrancePoint;
	@RegisterProperty @Export public Node3D cashierPoint;
	@RegisterProperty @Export public Node3D exitPoint;

	@RegisterProperty @Export public Node3D shelfWaypointsParent;

	@RegisterProperty @Export public float minSpawnInterval = 5f;
	@RegisterProperty @Export public float maxSpawnInterval = 15f;
	@RegisterProperty @Export public float civilianChance = 0.7f;
	@RegisterProperty @Export public int maxActiveNpcs = 15;

	private Node3D[] shelfWaypoints;
	private float spawnTimer = 0f;
	private List<NPCBase> activeNpcs = new ArrayList<>();
	private RandomNumberGenerator spawnRng = new RandomNumberGenerator();

	@RegisterFunction
	public void _ready()
	{
		patchEnvironmentCollision();
		collectShelfWaypoints();
		spawnCustomer();
		spawnTimer = spawnRng.randfRange(minSpawnInterval, maxSpawnInterval);
	}

	private void patchEnvironmentCollision()
	{
		var root = getTree().getRoot();
		patchNode(root);
		print("PeasantSpawner: collision mask layer 2 dodany do StaticBody3D w scenie");
	}

	private void patchNode(Node node)
	{
		if (node instanceof StaticBody3D body) {
			body.setCollisionMaskValue(2, true);
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			var child = node.getChild(i);
			if (child != null) {
				patchNode(child);
			}
		}
	}

	@RegisterFunction
	public void _process(double delta)
	{
		spawnTimer -= (float) delta;
		if (spawnTimer <= 0f) {
			spawnTimer = spawnRng.randfRange(minSpawnInterval, maxSpawnInterval);
			spawnCustomer();
		}
	}

	@Override
	public void removeNpc(NPCBase npc)
	{
		activeNpcs.remove(npc);
		print("NPC usuniety. Aktywnych: " + activeNpcs.size());
	}

	private void collectShelfWaypoints()
	{
		if (shelfWaypointsParent == null) {
			shelfWaypoints = new Node3D[0];
			print("PeasantSpawner: shelfWaypointsParent nie ustawiony!");
			return;
		}

		int count = shelfWaypointsParent.getChildCount();
		shelfWaypoints = new Node3D[count];
		for (int i = 0; i < count; i++) {
			var child = shelfWaypointsParent.getChild(i);
			if (child instanceof Node3D wp) {
				shelfWaypoints[i] = wp;
			}
		}
		print("PeasantSpawner: zebrano " + shelfWaypoints.length + " punktow polek");
	}

	private void spawnCustomer()
	{
		if (activeNpcs.size() >= maxActiveNpcs) {
			return;
		}

		if (spawnPoint == null || entrancePoint == null || cashierPoint == null || exitPoint == null) {
			print("PeasantSpawner: brak wymaganych punktow!");
			return;
		}

		boolean isEnemy = rng.randf() >= civilianChance;
		PackedScene scene = isEnemy ? enemyScene : civilianScene;

		if (scene == null) {
			print("PeasantSpawner: PackedScene " + (isEnemy ? "enemyScene" : "civilianScene") + " nie ustawiona!");
			return;
		}

		var instance = scene.instantiate();
		if (!(instance instanceof NPCBase npc)) {
			print("PeasantSpawner: glowny node sceny nie extends NPCBase!");
			return;
		}

		npc.setGlobalPosition(spawnPoint.getGlobalPosition());
		npc.spawnerRef = this;
		activeNpcs.add(npc);
		addChild(npc);
		npc.initialize(entrancePoint, shelfWaypoints, cashierPoint, exitPoint, spawnPoint);

		print("PeasantSpawner: zrespiono " + (isEnemy ? "ENEMY" : "CIVILIAN") + " -> " + npc.getName() + " (aktywni: " + activeNpcs.size() + "/" + maxActiveNpcs + ")");
	}

	private RandomNumberGenerator rng = new RandomNumberGenerator();
}
