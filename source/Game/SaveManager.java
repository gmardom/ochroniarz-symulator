package Game;

import godot.api.FileAccess;

public class SaveManager
{
	private static final String SAVE_PATH = "user://save.json";

	public static void save(SaveData data)
	{
		var file = FileAccess.open(SAVE_PATH, FileAccess.ModeFlags.WRITE);
		if (file == null) return;

		file.storeString(
			"{" +
			"\"day\":" + data.day + "," +
			"\"week\":" + data.week + "," +
			"\"money\":" + data.money + "," +
			"\"totalCaught\":" + data.totalCaught + "," +
			"\"totalRobbed\":" + data.totalRobbed +
            "}"
		);
		file.close();
	}

	public static SaveData load()
	{
		var data = new SaveData();
		if (!FileAccess.fileExists(SAVE_PATH)) return data; // Brak pliku = nowa gra

		var file = FileAccess.open(SAVE_PATH, FileAccess.ModeFlags.READ);
		if (file == null) return data;

		var json = file.getAsText();
		file.close();

		// Proste parsowanie JSON bez bibliotek
		data.day         = parseIntField(json, "day");
		data.week        = parseIntField(json, "week");
		data.money       = parseIntField(json, "money");
		data.totalCaught = parseIntField(json, "totalCaught");
		data.totalRobbed = parseIntField(json, "totalRobbed");

		return data;
	}

	public static boolean hasSave()
	{
		return FileAccess.fileExists(SAVE_PATH);
	}

	public static void deleteSave()
	{
		// Usuwa zapis (nowa gra)
		var dir = godot.api.DirAccess.open("user://");
		if (dir != null) dir.remove("save.json");
	}

	private static int parseIntField(String json, String field)
	{
		try {
			String search = "\"" + field + "\":";
			int idx = json.indexOf(search);
			if (idx == -1) return 0;
			int start = idx + search.length();
			int end = start;
			while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
			return Integer.parseInt(json.substring(start, end));
		} catch (Exception e) { return 0; }
	}
}
