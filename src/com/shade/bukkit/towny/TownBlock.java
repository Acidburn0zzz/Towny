package com.shade.bukkit.towny;

public class TownBlock {
	//TODO: Admin only or possibly a group check
	//private List<Group> groups;
	private TownyWorld world;
	private Town town;
	private Resident resident;
	private int x, z;
	private boolean isForSale = false;
	
	public TownBlock(int x, int z, TownyWorld world) {
		this.x = x;
		this.z = z;
		this.setWorld(world);
	}
	
	public void setTown(Town town) {
		this.town = town;
		try {
			town.addTownBlock(this);
		} catch(AlreadyRegisteredException e) {}
	}
	public Town getTown() throws NotRegisteredException {
		if (!hasTown())
			throw new NotRegisteredException();
		return town;
	}
	public boolean hasTown() {
		return town != null;
	}
	public void setResident(Resident resident) {
		this.resident = resident;
		try {
			resident.addTownBlock(this);
		} catch(AlreadyRegisteredException e) {}
	}
	public Resident getResident() {
		return resident;
	}
	public boolean hasResident() {
		return resident != null;
	}
	public void setForSale(boolean isForSale) {
		this.isForSale = isForSale;
	}
	public boolean isForSale() {
		return isForSale;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getX() {
		return x;
	}
	public void setZ(int z) {
		this.z = z;
	}
	public int getZ() {
		return z;
	}
	
	public Coord getCoord() {
		return new Coord(x, z);
	}

	public void setWorld(TownyWorld world) {
		this.world = world;
	}

	public TownyWorld getWorld() {
		return world;
	}
}
