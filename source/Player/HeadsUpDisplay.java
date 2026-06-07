package Player;

import godot.annotation.*;
import godot.api.*;
import static godot.global.GD.*;

@RegisterClass
public class HeadsUpDisplay extends CanvasLayer
{
	@RegisterProperty @Export public CanvasItem crosshair;
	@RegisterProperty @Export public Label interractionText;
	@RegisterProperty @Export public Label clockLabel;
	@RegisterProperty @Export public Control shiftSummaryPanel;

	private Label summaryText;

	@RegisterFunction
	public void _ready()
	{
		if (crosshair != null) crosshair.setVisible(true);
		if (shiftSummaryPanel != null) shiftSummaryPanel.setVisible(false);
		summaryText = (Label) getNode("ShiftSummaryPanel/SummaryText");
		print("interractionText: " + interractionText);
	}

	public void startInteraction(String text)
	{
		print("startInteraction: '" + text + "'");
		if (interractionText != null) {
			interractionText.setText("F) " + text);
			interractionText.setVisible(true);
		}
	}

	public void stopInteraction()
	{
		if (interractionText != null) interractionText.setVisible(false);
	}

	public void updateClock(int hour)
	{
		if (clockLabel != null)
			clockLabel.setText(hour + ":00");
	}

	public void showShiftSummary(int caught, int robbed)
	{
		if (shiftSummaryPanel != null) shiftSummaryPanel.setVisible(true);
		if (summaryText != null)
			summaryText.setText(
				"Koniec zmiany!\n" +
				"Zlapani zlodzieje: " + caught + "\n" +
				"Okradzeni klienci: " + robbed
			);
	}

	public void hideShiftSummary()
	{
		if (shiftSummaryPanel != null) shiftSummaryPanel.setVisible(false);
	}
}
