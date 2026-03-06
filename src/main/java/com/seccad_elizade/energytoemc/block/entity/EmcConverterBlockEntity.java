package com.seccad_elizade.energytoemc.block.entity;

import com.seccad_elizade.energytoemc.item.ConversionUpgradeItem;
import com.seccad_elizade.energytoemc.registry.ModBlockEntities;
import com.seccad_elizade.energytoemc.screen.EmcConverterMenu;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class EmcConverterBlockEntity extends BlockEntity implements MenuProvider, IEmcStorage {

    private static final int FE_PER_EMC_BASE = 500;
    private final long MAX_EMC = 100000L;
    private long emcStored = 0;

    private int energyUsage = 0;
    private int emcProduction = 0;
    private final int[] feHistory = new int[20];
    private final int[] emcHistory = new int[20];
    private int historyIndex = 0;

    private int redstoneMode = 0;
    private final Map<Direction, Integer> sideModes = new HashMap<>();

    private final EnergyStorage energy = new EnergyStorage(1000000, 100000, 100000) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received != 0 && !simulate) { setChanged(); }
            return received;
        }
    };

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof ConversionUpgradeItem;
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored() & 0xffff;
                case 1 -> (energy.getEnergyStored() >> 16) & 0xffff;
                case 2 -> (int) (emcStored & 0xffff);
                case 3 -> (int) ((emcStored >> 16) & 0xffff);
                case 4, 5, 6, 7, 8, 9 -> sideModes.getOrDefault(Direction.from3DDataValue(index - 4), 0);
                case 10 -> redstoneMode;
                case 11 -> energyUsage;
                case 12 -> emcProduction;
                default -> 0;
            };
        }
        @Override
        public void set(int index, int value) {
            if (index >= 4 && index <= 9) sideModes.put(Direction.from3DDataValue(index - 4), value);
            if (index == 10) redstoneMode = value;
        }
        @Override
        public int getCount() { return 13; }
    };

    public EmcConverterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EMC_CONVERTER_BE.get(), pos, state);
        for(Direction d : Direction.values()) sideModes.put(d, 0);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EmcConverterBlockEntity entity) {
        if (level.isClientSide) return;

        int feUsedThisTick = 0;
        int emcProducedThisTick = 0;

        if (entity.redstoneMode == 2 || (entity.redstoneMode == 1 && !level.hasNeighborSignal(pos))) {
            entity.updateSmoothing(0, 0);
            return;
        }

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);

            IEnergyStorage neighborEnergy = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, dir.getOpposite());
            if (neighborEnergy != null && neighborEnergy.canExtract()) {
                int space = entity.energy.getMaxEnergyStored() - entity.energy.getEnergyStored();
                if (space > 0) {
                    int pulled = neighborEnergy.extractEnergy(Math.min(space, 50000), false);
                    entity.energy.receiveEnergy(pulled, false);
                }
            }

            if (entity.emcStored > 0) {
                IEmcStorage neighborEmc = level.getCapability(PECapabilities.EMC_STORAGE_CAPABILITY, neighborPos, dir.getOpposite());
                if (neighborEmc != null) {
                    long toPush = Math.min(entity.emcStored, 50000L);
                    long accepted = neighborEmc.insertEmc(toPush, EmcAction.EXECUTE);
                    if (accepted > 0) {
                        entity.emcStored -= accepted;
                        entity.setChanged();
                    }
                }
            }
        }

        int multiplier = 1;
        int tier = 0;
        ItemStack upgradeStack = entity.itemHandler.getStackInSlot(0);

        if (upgradeStack.getItem() instanceof ConversionUpgradeItem upgrade) {
            multiplier = upgrade.getProfitMultiplier();
            tier = upgrade.getTier();
        }

        if (entity.energy.getEnergyStored() >= FE_PER_EMC_BASE && entity.emcStored < entity.MAX_EMC) {
            int unitsPerTick = (int) (10 * Math.pow(2, tier));
            int availableUnits = entity.energy.getEnergyStored() / FE_PER_EMC_BASE;
            int unitsToProcess = Math.min(availableUnits, unitsPerTick);

            long spaceLeft = entity.MAX_EMC - entity.emcStored;
            long potentialEmc = (long) unitsToProcess * multiplier;

            if (potentialEmc > spaceLeft) {
                potentialEmc = spaceLeft;
                unitsToProcess = (int) (potentialEmc / multiplier);
            }

            if (unitsToProcess > 0) {
                feUsedThisTick = unitsToProcess * FE_PER_EMC_BASE;
                entity.energy.extractEnergy(feUsedThisTick, false);

                entity.emcStored += (long) unitsToProcess * multiplier;
                emcProducedThisTick = (int) (unitsToProcess * multiplier);
                entity.setChanged();
            }
        }

        entity.updateSmoothing(feUsedThisTick, emcProducedThisTick);
    }

    private void updateSmoothing(int currentFe, int currentEmc) {
        feHistory[historyIndex] = currentFe;
        emcHistory[historyIndex] = currentEmc;
        historyIndex = (historyIndex + 1) % 20;

        long totalFe = 0;
        long totalEmc = 0;
        for (int i = 0; i < 20; i++) {
            totalFe += feHistory[i];
            totalEmc += emcHistory[i];
        }

        this.energyUsage = (int) (totalFe / 20);
        this.emcProduction = (int) (totalEmc / 20);
    }

    @Override
    public long insertEmc(long amount, EmcAction action) {
        long space = MAX_EMC - emcStored;
        long toInsert = Math.min(amount, space);
        if (action.execute() && toInsert > 0) {
            emcStored += toInsert;
            setChanged();
        }
        return toInsert;
    }

    @Override
    public long extractEmc(long amount, EmcAction action) {
        long toExtract = Math.min(amount, emcStored);
        if (action.execute() && toExtract > 0) {
            emcStored -= toExtract;
            setChanged();
        }
        return toExtract;
    }

    @Override public long getStoredEmc() { return emcStored; }
    @Override public long getMaximumEmc() { return MAX_EMC; }
    @Override public Component getDisplayName() { return Component.literal("EMC Converter"); }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy.getEnergyStored());
        tag.putLong("emc", emcStored);
        tag.putInt("redstoneMode", redstoneMode);
        tag.put("inventory", itemHandler.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.energy.receiveEnergy(tag.getInt("energy"), false);
        this.emcStored = tag.getLong("emc");
        this.redstoneMode = tag.getInt("redstoneMode");
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }
    }

    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
        return new EmcConverterMenu(id, inv, this, this.data);
    }

    public EnergyStorage getEnergyStorage() { return this.energy; }
    public ItemStackHandler getItemHandler() { return this.itemHandler; }

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }
}