package eu.mikroskeem.orion.mod.impl;

import net.minecraft.server.v1_11_R1.EntityHuman;
import net.minecraft.server.v1_11_R1.IPlayerFileData;
import net.minecraft.server.v1_11_R1.NBTTagCompound;

import javax.annotation.Nullable;

/**
 * @author Mark Vainomaa
 */
public class NullPlayerNBTStorage implements IPlayerFileData {
    @Override public String[] getSeenPlayers() { return new String[0]; }
    @Nullable @Override public NBTTagCompound load(EntityHuman entityHuman) { return null; }
    @Override public void save(EntityHuman entityHuman) {}
}
