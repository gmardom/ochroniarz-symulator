package Menu;

import godot.annotation.*;
import godot.api.*;
import Game.*;

@RegisterClass
public class FullscreenControl extends CheckButton
{
    @RegisterFunction
    public void _ready()
    {
        setPressed(GameManager.getFullscreen());
    }

    @RegisterFunction
    public void _onToggled(boolean toggledOn)
    {
        GameManager.setFullscreen(toggledOn == false);
    }
}
