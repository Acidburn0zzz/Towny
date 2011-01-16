package com.shade.bukkit.towny;

import java.util.ArrayList;
import java.util.List;

public class Resident extends TownBlockOwner {
	private List<Resident> friends = new ArrayList<Resident>();
	private Town town;
	private long lastOnline;
	
	public Resident(String name) {
		setName(name);
	}

	public void setLastOnline(long lastOnline) {
		this.lastOnline = lastOnline;
	}

	public long getLastOnline() {
		return lastOnline;
	}

	public boolean isKing() {
		try {
			return town.getNation().isKing(this);
		} catch (TownyException e) {
			return false;
		}
	}

	public boolean isMayor() {
		return (hasTown() ? town.isMayor(this) : false);
	}
	
	public boolean hasTown() {
		return !(town == null);
	}
	
	public boolean hasNation() {
		return (hasTown() ? town.hasNation() : false);
	}
	
	public Town getTown() throws TownyException {
		if (hasTown())
			return town;
		else
			throw new TownyException("Resident doesn't belong to any town");
	}
	
	public void setTown(Town town) {
		this.town = town;
	}
	
	public void setFriends(List<Resident> friends) {
		this.friends = friends;
	}
	
	public List<Resident> getFriends() {
		return friends;
	}
	
	public boolean removeFriend(Resident resident) {
		return friends.remove(resident);
	}
	
	public boolean hasFriend(Resident resident) {
		return friends.contains(resident);
	}
	
	public void addFriend(Resident resident) throws AlreadyRegisteredException {
		if (hasFriend(resident)) {
			throw new AlreadyRegisteredException();
		} else {
			friends.add(resident);
		}
	}
}
