package com.shade.bukkit.towny;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.nijikokun.bukkit.iConomy.iConomy;
import com.shade.bukkit.util.ChatTools;
import com.shade.bukkit.util.Colors;

//TODO: Make static
//TODO: pull names from the config

public class TownyFormatter {
	public String getTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
		return sdf.format(System.currentTimeMillis());
	}

	public List<String> getStatus(Resident resident) {
		List<String> out = new ArrayList<String>();

		// ___[ King Harlus ]___
		out.add(ChatTools.formatTitle(getFormattedName(resident)));

		// Registered: Sept 3 2009 | Last Online: March 7 @ 14:30
		SimpleDateFormat rsdf = new SimpleDateFormat("MMM d yyyy");
		SimpleDateFormat losdf = new SimpleDateFormat("MMMMM dd '@' HH:mm");
		out.add(Colors.Green + "Last Online: " + Colors.LightGreen + rsdf.format(resident.getRegistered())
			+ Colors.Gray + " | "
			+ Colors.Green + "Last Online: " + Colors.LightGreen + losdf.format(resident.getLastOnline()));

		// Owner of: 4 Town Blocks
		if (resident.getTownBlocks().size() > 0)
			out.add(Colors.Green + "Owner of: " + Colors.LightGreen + resident.getTownBlocks().size() + " plots");

		// Bank: 534 coins
		if (TownySettings.isUsingIConomy())
			try {
				TownyIConomyObject.checkIConomy();
				out.add(Colors.Green + "Bank: " + Colors.LightGreen + resident.getIConomyBalance() + " " + iConomy.currency);
			} catch (IConomyException e1) {
			}
		
		// Town: Camelot
		String line = Colors.Green + "Mayor: " + Colors.LightGreen;
		if (!resident.hasTown())
			line += "None";
		else
			try {
				line += getFormattedName(resident.getTown());
			} catch (TownyException e) {
				line += "Error: " + e.getError();
			}
		out.add(line);

		// Friends [12]:
		// James, Carry, Mason
		List<Resident> friends = resident.getFriends();
		out.add(Colors.Green + "Friends " + Colors.LightGreen + "[" + friends.size() + "]" + Colors.Green + ":");
		out.addAll(ChatTools.list(getFormattedNames(friends.toArray(new Resident[0]))));

		return out;
	}

	public List<String> getStatus(Town town) {
		List<String> out = new ArrayList<String>();

		// ___[ Raccoon City (PvP) ]___
		out.add(ChatTools.formatTitle(getFormattedName(town)
				+ (town.isPVP() ? Colors.Red + " (PvP)" : "")));

		// Lord: Mayor Quimby
		// Board: Get your fried chicken
		try {
			out.add(Colors.Green + "Board: " + Colors.LightGreen
					+ town.getTownBoard());
		} catch (NullPointerException e) {
		}

		// Town Size: 0 / 16 [Bonus: 0] [Home: 33,44]
		try {
			out.add(Colors.Green
					+ "Town Size: " + Colors.LightGreen + town.getTownBlocks().size() + " / " + TownySettings.getMaxTownBlocks(town)
					+ Colors.LightBlue + " [Bonus: " + town.getBonusBlocks() + "]"
					+ Colors.LightGray + " [Home: " + (town.hasHomeBlock() ? town.getHomeBlock().getCoord().toString() : "None") + "]");
		} catch (TownyException e) {
		}
		
		// Bank: 534 coins
		if (TownySettings.isUsingIConomy())
			try {
				TownyIConomyObject.checkIConomy();
				out.add(Colors.Green + "Bank: " + Colors.LightGreen + town.getIConomyBalance() + " " + iConomy.currency);
			} catch (IConomyException e1) {
			}

		// if (mayor != null)
		out.add(Colors.Green + "Mayor: " + Colors.LightGreen
				+ getFormattedName(town.getMayor()));
		// Assistants:
		// Sammy, Ginger
		if (town.getAssistants().size() > 0) {
			out.add(Colors.Green + "Assistants:");
			out.addAll(ChatTools.list(getFormattedNames(town.getAssistants()
					.toArray(new Resident[0]))));
		}
		// Nation: Azur Empire
		try {
			out.add(Colors.Green + "Nation: " + Colors.LightGreen
					+ getFormattedName(town.getNation()));
		} catch (TownyException e) {
		}

		// Residents [12]:
		// James, Carry, Mason
		out.add(Colors.Green + "Residents " + Colors.LightGreen + "["
				+ town.getNumResidents() + "]" + Colors.Green + ":");
		out.addAll(ChatTools.list(getFormattedNames(town.getResidents()
				.toArray(new Resident[0]))));

		return out;
	}

	public List<String> getStatus(Nation nation) {
		List<String> out = new ArrayList<String>();

		// ___[ Azur Empire ]___
		out.add(ChatTools.formatTitle(getFormattedName(nation)));

		// Bank: 534 coins
		if (TownySettings.isUsingIConomy())
			try {
				TownyIConomyObject.checkIConomy();
				out.add(Colors.Green + "Bank: " + Colors.LightGreen + nation.getIConomyBalance() + " " + iConomy.currency);
			} catch (IConomyException e1) {
			}
		
		// King: King Harlus
		if (nation.getNumTowns() > 0 && nation.hasCapital()
				&& nation.getCapital().hasMayor())
			out.add(Colors.Green + "King: " + Colors.LightGreen
					+ getFormattedName(nation.getCapital().getMayor()));
		// Assistants:
		// Mayor Rockefel, Sammy, Ginger
		if (nation.getAssistants().size() > 0) {
			out.add(Colors.Green + "Assistants:");
			out.addAll(ChatTools.list(getFormattedNames(nation.getAssistants()
					.toArray(new Resident[0]))));
		}
		// Towns [44]:
		// James City, Carry Grove, Mason Town
		out.add(Colors.Green + "Towns " + Colors.LightGreen + "["
				+ nation.getNumTowns() + "]" + Colors.Green + ":");
		out.addAll(ChatTools.list(getFormattedNames(nation.getTowns().toArray(
				new Town[0]))));
		// Allies [4]:
		// James Nation, Carry Territory, Mason Country
		out.add(Colors.Green + "Allies " + Colors.LightGreen + "["
				+ nation.getAllies().size() + "]" + Colors.Green + ":");
		out.addAll(ChatTools.list(getFormattedNames(nation.getAllies().toArray(
				new Nation[0]))));
		// Enemies [4]:
		// James Nation, Carry Territory, Mason Country
		out.add(Colors.Green + "Enemies " + Colors.LightGreen + "["
				+ nation.getEnemies().size() + "]" + Colors.Green + ":");
		out.addAll(ChatTools.list(getFormattedNames(nation.getEnemies()
				.toArray(new Nation[0]))));

		return out;
	}

	public String getNamePrefix(Resident resident) {
		if (resident == null)
			return "";
		if (resident.isKing())
			return TownySettings.getKingPrefix(resident);
		else if (resident.isMayor())
			return TownySettings.getMayorPrefix(resident);
		return "";
	}
	
	public String getNamePostfix(Resident resident) {
		if (resident == null)
			return "";
		if (resident.isKing())
			return TownySettings.getKingPostfix(resident);
		else if (resident.isMayor())
			return TownySettings.getMayorPostfix(resident);
		return "";
	}
	
	public String getFormattedName(Resident resident) {
		if (resident == null)
			return "null";
		if (resident.isKing())
			return TownySettings.getKingPrefix(resident) + resident.getName() + TownySettings.getKingPostfix(resident);
		else if (resident.isMayor())
			return TownySettings.getMayorPrefix(resident) + resident.getName() + TownySettings.getMayorPostfix(resident);
		return resident.getName();
	}

	public String getFormattedName(Town town) {
		if (town.isCapital())
			return TownySettings.getCapitalPrefix(town) + town.getName() + TownySettings.getCapitalPostfix(town);
		return TownySettings.getTownPrefix(town) + town.getName() + TownySettings.getTownPostfix(town);
	}

	public String getFormattedName(Nation nation) {
		return TownySettings.getNationPrefix() + nation.getName() + TownySettings.getNationPostfix();
	}

	public String[] getFormattedNames(Resident[] residents) {
		List<String> names = new ArrayList<String>();
		for (Resident resident : residents)
			names.add(getFormattedName(resident));
		return names.toArray(new String[0]);
	}

	public String[] getFormattedNames(Town[] towns) {
		List<String> names = new ArrayList<String>();
		for (Town town : towns)
			names.add(getFormattedName(town));
		return names.toArray(new String[0]);
	}

	public String[] getFormattedNames(Nation[] nations) {
		List<String> names = new ArrayList<String>();
		for (Nation nation : nations)
			names.add(getFormattedName(nation));
		return names.toArray(new String[0]);
	}
}
