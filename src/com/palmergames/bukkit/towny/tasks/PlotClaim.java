package com.palmergames.bukkit.towny.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.EconomyException;
import com.palmergames.bukkit.towny.NotRegisteredException;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyException;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.command.TownCommand;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.WorldCoord;

/**
 * @author ElgarL
 *
 */
public class PlotClaim extends Thread {
	
	Towny plugin;
	volatile Player player;
	volatile Resident resident;
	volatile TownyWorld world;
	List<WorldCoord> selection;
	boolean forced;
	Action action;
	
	public enum Action {
		CLAIM,
		UNCLAIM
	}
	
	/**
	 * @param plugin reference to towny
	 * @param player Doing the claiming, or null
	 * @param selection List of WoorldCoords to claim/unclaim
	 * @param claim or unclaim
	 */
	public PlotClaim(Towny plugin, Player player, Resident resident, List<WorldCoord> selection, Action action) {
		super();
		this.plugin = plugin;
		this.player = player;
		this.resident = resident;
		this.selection = selection;
		this.action = action;
		this.setPriority(MIN_PRIORITY);
	}
	
	@Override
	public void run() {
		
		if (player != null)
			TownyMessaging.sendMsg(player, String.format("Processing Plot %s...", action.name().toLowerCase()));
				
		if (selection != null) {

			for (WorldCoord worldCoord : selection) {
				
				// Make sure this is a valid world (mainly when unclaiming).
				try {
					this.world = TownyUniverse.getDataSource().getWorld(worldCoord.getWorld().getName());
				} catch (NotRegisteredException e) {
					TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_err_not_configured"));
					continue;
				}
				try {
					if (action == Action.CLAIM) {
						residentClaim(worldCoord);
					} else {
						residentUnclaim(worldCoord);
					}
				} catch (EconomyException e) {
					/*
					 *  Can't pay, but try the rest as we may be
					 *  re-possessing and claiming for personal plots.
					 */
					TownyMessaging.sendErrorMsg(player, e.getError());
				} catch (TownyException x) {
					TownyMessaging.sendErrorMsg(player, x.getMessage());
				}

			}
		} else if (action == Action.UNCLAIM){	
			residentUnclaimAll();
		}
					
		if (player != null) {
			if (action == Action.CLAIM)
				TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_claimed") + ( (selection.size() > 5) ? "Total TownBlocks: " + selection.size() : Arrays.toString(selection.toArray(new WorldCoord[0]))));
			else
				if (selection != null)
					TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_unclaimed") + ( (selection.size() > 5) ? "Total TownBlocks: " + selection.size() : Arrays.toString(selection.toArray(new WorldCoord[0]))));
				else
					TownyMessaging.sendMsg(player, TownySettings.getLangString("msg_unclaimed"));
		}
		
		TownyUniverse.getDataSource().saveResident(resident);
		plugin.updateCache();
		
	}
	
	private boolean residentClaim(WorldCoord worldCoord) throws TownyException, EconomyException {
		TownBlock townBlock;
		Town town;
		
		TownyMessaging.sendMsg(player, String.format("1) %s", worldCoord.toString()));
		
		try {
			townBlock = worldCoord.getTownBlock();
			town = townBlock.getTown();
		} catch (NotRegisteredException e) {
			throw new TownyException(TownySettings.getLangString("msg_err_not_part_town"));
		}
		
		TownyMessaging.sendMsg(player, String.format("2) %s", worldCoord.toString()));
		
		if (!resident.hasTown() && town.isOpen()) {
			// Town is open, so have the player join the town first.
			TownCommand.townJoin(town, resident);
			TownyMessaging.sendMsg(player, String.format("3) %s", worldCoord.toString()));
		}
		
		TownyMessaging.sendMsg(player, String.format("4) %s", worldCoord.toString()));
		
		if (!resident.hasTown()) {
			// Town is not open, or resident failed to join.
			// You are thus needed to belong to a town in order to claim plots.
			TownyMessaging.sendMsg(player, String.format("5) %s", worldCoord.toString()));
			throw new TownyException(TownySettings.getLangString("msg_err_not_in_town_claim"));
		} else {
			// Resident is (now) in a town.
			try {
				// Check for embassy plot, or if the resident is is the town.
				if (!resident.getTown().equals(town) && !townBlock.getType().equals(TownBlockType.EMBASSY))
					throw new TownyException(TownySettings.getLangString("msg_err_not_part_town"));

				try {
					Resident owner = townBlock.getResident();
					if (townBlock.getPlotPrice() != -1) {
						// Plot is for sale
						if (TownySettings.isUsingEconomy() && !resident.payTo(townBlock.getPlotPrice(), owner, "Plot - Buy From Seller"))
							throw new TownyException(TownySettings.getLangString("msg_no_money_purchase_plot"));
						
						int maxPlots = TownySettings.getMaxResidentPlots(resident);
						if (maxPlots >= 0 && resident.getTownBlocks().size() + 1 > maxPlots)
							throw new TownyException(String.format(TownySettings.getLangString("msg_max_plot_own"), maxPlots));
						
						TownyMessaging.sendTownMessage(town, TownySettings.getBuyResidentPlotMsg(resident.getName(), owner.getName(), townBlock.getPlotPrice()));
						townBlock.setPlotPrice(-1);
						townBlock.setResident(resident);
						
						// Set the plot permissions to mirror the new owners.
						townBlock.setType(townBlock.getType());

						TownyUniverse.getDataSource().saveResident(owner);
						TownyUniverse.getDataSource().saveTownBlock(townBlock);
						
						plugin.updateCache();
						return true;
					} else if (town.isMayor(resident) || town.hasAssistant(resident)) {
						//Plot isn't for sale but re-possessing for town.
						
						if (TownySettings.isUsingEconomy() && !town.payTo(0.0, owner, "Plot - Buy Back"))
							throw new TownyException(TownySettings.getLangString("msg_town_no_money_purchase_plot"));
						
						TownyMessaging.sendTownMessage(town, TownySettings.getBuyResidentPlotMsg(town.getName(), owner.getName(), 0.0));
						townBlock.setResident(null);
						townBlock.setPlotPrice(-1);
						
						// Set the plot permissions to mirror the towns.
						townBlock.setType(townBlock.getType());

						TownyUniverse.getDataSource().saveResident(owner);
						TownyUniverse.getDataSource().saveTownBlock(townBlock);

						return true;
					} else {
						//Should never reach here.
						throw new AlreadyRegisteredException(String.format(TownySettings.getLangString("msg_already_claimed"), owner.getName()));
					}
								
				} catch (NotRegisteredException e) {
					//Plot has no owner so it's the town selling it
					TownyMessaging.sendMsg(player, String.format("6) %s", worldCoord.toString()));
					if (townBlock.getPlotPrice() == -1)
							throw new TownyException(TownySettings.getLangString("msg_err_plot_nfs"));
					TownyMessaging.sendMsg(player, String.format("7) %s", worldCoord.toString()));
					if (TownySettings.isUsingEconomy() && !resident.payTo(townBlock.getPlotPrice(), town, "Plot - Buy From Town"))
							throw new TownyException(TownySettings.getLangString("msg_no_money_purchase_plot"));
					TownyMessaging.sendMsg(player, String.format("8) %s", worldCoord.toString()));
					townBlock.setPlotPrice(-1);
					townBlock.setResident(resident);
					
					// Set the plot permissions to mirror the new owners.
					townBlock.setType(townBlock.getType());
					TownyUniverse.getDataSource().saveTownBlock(townBlock);
					TownyMessaging.sendMsg(player, String.format("9) %s", worldCoord.toString()));
					return true;
				}
			} catch (NotRegisteredException e) {
				throw new TownyException(TownySettings.getLangString("msg_err_not_part_town"));
			}
		}
	}

	private boolean residentUnclaim(WorldCoord worldCoord) throws TownyException {
		
		try {
			TownBlock townBlock = worldCoord.getTownBlock();

			townBlock.setResident(null);
			townBlock.setPlotPrice(townBlock.getTown().getPlotPrice());
			
			// Set the plot permissions to mirror the towns.
			townBlock.setType(townBlock.getType());
			TownyUniverse.getDataSource().saveTownBlock(townBlock);
			
			plugin.updateCache();
						

		} catch (NotRegisteredException e) {
				throw new TownyException(TownySettings.getLangString("msg_not_own_place"));
		}
		
		return true;
	}
	
	private void residentUnclaimAll()  {
		List<TownBlock> selection = new ArrayList<TownBlock>(resident.getTownBlocks());
		
		for (TownBlock townBlock: selection) {
			try {
				residentUnclaim(townBlock.getWorldCoord());
			} catch (TownyException e) {
				TownyMessaging.sendErrorMsg(player, e.getMessage());
			}
			
			
		}
		
	}
	
}
