package Menu; // Deklaracja pakietu Menu

import godot.annotation.*; // Import adnotacji Godot
import godot.api.*; // Import klas API Godot (HSlider, AudioServer itp.)

@RegisterClass // Rejestruje klasę jako klasę Godot widoczną w edytorze
public class AudioControl extends HSlider // Kontrolka głośności dziedziczy po HSlider (poziomy suwak)
{
	@RegisterProperty @Export
	public String audioBusName; // Nazwa magistrali audio do kontrolowania – ustawiana w edytorze (np. "Master", "Music")
	private int audioBusId; // Indeks magistrali audio w AudioServer (obliczany w _ready)

	@RegisterFunction
	public void _ready() // Wywoływane przy starcie – inicjalizuje suwak
	{
		setMax(0.0); // Ustawia maksymalną wartość suwaka na 0 dB (pełna głośność)
		setMin(-80.0); // Ustawia minimalną wartość suwaka na -80 dB (praktyczna cisza)
		if (audioBusName != null) { // Jeśli nazwa magistrali audio jest ustawiona
			audioBusId = AudioServer.getBusIndex(audioBusName); // Pobiera indeks magistrali audio po nazwie
			setValueNoSignal(AudioServer.getBusVolumeDb(audioBusId)); // Ustawia pozycję suwaka na aktualną głośność bez wywoływania sygnału
		}
	}

	@RegisterFunction
	public void _onValueChanged(float value) // Wywoływane gdy użytkownik przesunie suwak
	{
		AudioServer.setBusVolumeDb(audioBusId, value); // Ustawia głośność magistrali audio na wartość suwaka w dB
	}
}
