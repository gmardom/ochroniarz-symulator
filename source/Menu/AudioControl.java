package Menu;

import godot.annotation.*;
import godot.api.*;

@RegisterClass
public class AudioControl extends HSlider
{
    @RegisterProperty @Export
    public String audioBusName;
    private int audioBusId;

    @RegisterFunction
    public void _ready()
    {
        setMax(0.0);
        setMin(-80.0);

        if (audioBusName != null) {
            audioBusId = AudioServer.getBusIndex(audioBusName);
            setValueNoSignal(AudioServer.getBusVolumeDb(audioBusId));
        }
    }

    @RegisterFunction
    public void _onValueChanged(float value)
    {
        AudioServer.setBusVolumeDb(audioBusId, value);
    }
}
