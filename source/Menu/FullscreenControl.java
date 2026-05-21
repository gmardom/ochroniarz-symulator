package Menu; // Deklaracja pakietu Menu

import godot.annotation.*; // Import adnotacji Godot
import godot.api.*; // Import klas API Godot (CheckButton itp.)
import Game.*; // Import klas z pakietu Game (np. GameManager)

@RegisterClass // Rejestruje klasę jako klasę Godot widoczną w edytorze
public class FullscreenControl extends CheckButton // Kontrolka pełnego ekranu dziedziczy po CheckButton (przycisk przełącznik)
{
	@RegisterFunction
	public void _ready() // Wywoływane przy starcie – ustawia początkowy stan przycisku
	{
		setPressed(GameManager.getFullscreen()); // Ustawia stan przycisku zgodnie z aktualnym trybem okna
	}

	@RegisterFunction
	public void _onToggled(boolean toggledOn) // Wywoływane gdy użytkownik kliknie przycisk
	{
		GameManager.setFullscreen(toggledOn == false); // Przełącza tryb pełnoekranowy (odwraca wartość przycisku)
	}
}
