package mods.su5ed.somnia.api.capability;

import net.minecraft.nbt.CompoundNBT;

//Thanks @TheSilkMiner for a custom capability example
public class Fatigue implements IFatigue {

    private double fatigue;
    private int fatigueUpdateCounter = 0, sideEffectStage = -1;
    private boolean resetSpawn = true;

    @Override
    public double getFatigue()
    {
        return this.fatigue;
    }

    @Override
    public void setFatigue(double fatigue)
    {
        this.fatigue = fatigue;
    }

    @Override
    public int getSideEffectStage()
    {
        return this.sideEffectStage;
    }

    @Override
    public void setSideEffectStage(int stage)
    {
        this.sideEffectStage = stage;
    }

    @Override
    public int updateFatigueCounter()
    {
        return ++fatigueUpdateCounter;
    }

    @Override
    public void resetFatigueCounter()
    {
        this.fatigueUpdateCounter = 0;
    }

    @Override
    public void maxFatigueCounter() {
        this.fatigueUpdateCounter = 100;
    }

    @Override
    public void shouldResetSpawn(boolean resetSpawn) {
        this.resetSpawn = resetSpawn;
    }

    @Override
    public boolean resetSpawn() {
        return this.resetSpawn;
    }


    @Override
    public CompoundNBT serializeNBT()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putDouble("fatigue", this.fatigue);
        tag.putInt("sideEffectStage", this.sideEffectStage);
        tag.putBoolean("resetSpawn", this.resetSpawn);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        this.fatigue = nbt.getDouble("fatigue");
        this.sideEffectStage = nbt.getInt("sideEffectStage");
        this.resetSpawn = nbt.getBoolean("resetSpawn");
    }
}
