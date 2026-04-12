package Menu;

import Game.*;

import godot.annotation.*;

@RegisterClass
public class MenuMain extends Menu
{
    @RegisterFunction
    public void _onStartButtonPressed()
    {
        GameManager.I().loadGame();
    }

    @RegisterFunction
    public void _onQuitButtonPressed()
    {
        GameManager.I().exit();
    }
}
