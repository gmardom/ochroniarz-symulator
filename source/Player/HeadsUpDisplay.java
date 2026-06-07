package Player;

import godot.annotation.*;
import godot.api.*;

@RegisterClass
public class HeadsUpDisplay extends CanvasLayer
{
	@RegisterProperty @Export public CanvasItem crosshair;
	@RegisterProperty @Export public Label interractionText;
	@RegisterProperty @Export public Label clockLabel;
	@RegisterProperty @Export public Control shiftSummaryPanel;
	@RegisterProperty @Export public Label statusLabel;
	@RegisterProperty @Export public Label escapedLabel;
	@RegisterProperty @Export public Label resolvedLabel;
	@RegisterProperty @Export public Label gameOverLabel;
	@RegisterProperty @Export public Label shiftCompleteLabel;
	@RegisterProperty @Export public Label summaryText;

	@RegisterFunction
	public void _ready()
	{
		if (crosshair != null) crosshair.setVisible(true);
		if (shiftSummaryPanel != null) shiftSummaryPanel.setVisible(false);
		if (gameOverLabel != null) gameOverLabel.setVisible(false);
		if (shiftCompleteLabel != null) shiftCompleteLabel.setVisible(false);
		updateStats(0, 0);
		if (statusLabel != null) statusLabel.setText("Shift: INACTIVE");
	}

	public void startInteraction(String text)
	{
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

	public void updateStats(int escaped, int resolved)
	{
		if (escapedLabel != null)
			escapedLabel.setText("Escaped: " + escaped);
		if (resolvedLabel != null)
			resolvedLabel.setText("Resolved: " + resolved);
	}

	public void showShiftActive()
	{
		if (statusLabel != null) statusLabel.setText("Shift: ACTIVE");
	}

	public void showGameOver()
	{
		if (shiftSummaryPanel != null) shiftSummaryPanel.setVisible(false);
		if (statusLabel != null) statusLabel.setText("Shift: GAME OVER");
		if (gameOverLabel != null) gameOverLabel.setVisible(true);
	}

	public void showShiftComplete(int resolved, int escaped)
	{
		if (shiftSummaryPanel != null) shiftSummaryPanel.setVisible(false);
		if (shiftCompleteLabel != null) {
			shiftCompleteLabel.setVisible(true);
			shiftCompleteLabel.setText(
				"SHIFT COMPLETE\n\n" +
				"Resolved Incidents: " + resolved + "\n" +
				"Escaped Thieves: " + escaped
			);
		}
		if (statusLabel != null) statusLabel.setText("Shift: COMPLETE");
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
