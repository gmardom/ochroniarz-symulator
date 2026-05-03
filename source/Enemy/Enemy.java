package Enemy;

import godot.annotation.*;
import godot.api.*;
import godot.global.GD;

@RegisterClass
public class Enemy extends CharacterBody3D
{
    @RegisterFunction
    public void _ready()
    {
        GD.print("Hello");
    }
}
