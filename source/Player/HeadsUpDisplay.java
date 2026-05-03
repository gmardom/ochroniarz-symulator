package Player;

import godot.annotation.*;
import godot.api.*;
import godot.core.*;

@RegisterClass
public class HeadsUpDisplay extends CanvasLayer
{
    @RegisterProperty @Export public CanvasItem crosshair;
    @RegisterProperty @Export public Label interractionText;

    @RegisterFunction
    public void _ready() {}

    public void startInteraction(String text)
    {
        if (crosshair != null) {
            crosshair.setVisible(true);
        }
        if (interractionText != null) {
            interractionText.setText("F) " + text);
            interractionText.setVisible(true);
        }
    }

    public void stopInteraction()
    {
        if (crosshair != null) crosshair.setVisible(false);
        if (interractionText != null) interractionText.setVisible(false);
    }
}
