package com.seccad_elizade.energytoemc.block.entity;

import com.seccad_elizade.energytoemc.registry.ModBlockEntities;
import com.seccad_elizade.energytoemc.screen.EmcCapacitorMenu;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmcCapacitorBlockEntity extends BlockEntity implements IEmcStorage, MenuProvider {
    private long storedEmc = 0;
    private long capacity;

    private final EnergyStorage energyStorage;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) (storedEmc & 0xFFFF);
                case 1 -> (int) ((storedEmc >> 16) & 0xFFFF);
                case 2 -> (int) ((storedEmc >> 32) & 0xFFFF);
                case 3 -> (int) ((storedEmc >> 48) & 0xFFFF);
                case 4 -> (int) (capacity & 0xFFFF);
                case 5 -> (int) ((capacity >> 16) & 0xFFFF);
                case 6 -> (int) ((capacity >> 32) & 0xFFFF);
                case 7 -> (int) ((capacity >> 48) & 0xFFFF);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> storedEmc = (storedEmc & ~0xFFFFL) | (value & 0xFFFFL);
                case 1 -> storedEmc = (storedEmc & ~(0xFFFFL << 16)) | ((value & 0xFFFFL) << 16);
                case 2 -> storedEmc = (storedEmc & ~(0xFFFFL << 32)) | ((value & 0xFFFFL) << 32);
                case 3 -> storedEmc = (storedEmc & ~(0xFFFFL << 48)) | ((value & 0xFFFFL) << 48);
                case 4 -> capacity = (capacity & ~0xFFFFL) | (value & 0xFFFFL);
                case 5 -> capacity = (capacity & ~(0xFFFFL << 16)) | ((value & 0xFFFFL) << 16);
                case 6 -> capacity = (capacity & ~(0xFFFFL << 32)) | ((value & 0xFFFFL) << 32);
                case 7 -> capacity = (capacity & ~(0xFFFFL << 48)) | ((value & 0xFFFFL) << 48);
            }
        }

        @Override
        public int getCount() { return 8; }
    };

    public EmcCapacitorBlockEntity(BlockPos pos, BlockState state, long capacity) {
        super(ModBlockEntities.EMC_CAPACITOR_BE.get(), pos, state);
        this.capacity = capacity;

        this.energyStorage = new EnergyStorage((int)Math.min(Integer.MAX_VALUE, capacity)) {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                int received = super.receiveEnergy(maxReceive, simulate);
                if (!simulate && received > 0) setChanged();
                return received;
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                int extracted = super.extractEnergy(maxExtract, simulate);
                if (!simulate && extracted > 0) setChanged();
                return extracted;
            }
        };

        if (isCreative()) {
            this.storedEmc = Long.MAX_VALUE;
        } else {
            this.storedEmc = 0;
        }
    }

    public EmcCapacitorBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, 100_000L);
    }

    public IEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    private boolean isCreative() {
        return this.capacity >= Long.MAX_VALUE || this.capacity < 0;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        if (isCreative()) {
            if (this.storedEmc != Long.MAX_VALUE) {
                this.storedEmc = Long.MAX_VALUE;
                setChanged();
            }
        }
    }

    @Override
    public long insertEmc(long amount, EmcAction action) {
        if (isCreative()) return amount;
        long canInsert = Math.min(amount, capacity - storedEmc);
        if (action.execute() && canInsert > 0) {
            storedEmc += canInsert;
            setChanged();
        }
        return canInsert;
    }

    @Override
    public long extractEmc(long amount, EmcAction action) {
        if (isCreative()) return amount;
        long canExtract = Math.min(amount, storedEmc);
        if (action.execute() && canExtract > 0) {
            storedEmc -= canExtract;
            setChanged();
        }
        return canExtract;
    }

    @Override
    public long getStoredEmc() { return storedEmc; }

    @Override
    public long getMaximumEmc() { return capacity; }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.energytoemc.emc_capacitor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new EmcCapacitorMenu(containerId, playerInventory, this, this.data);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("StoredEmc", storedEmc);
        tag.putLong("Capacity", capacity);
        tag.putInt("FEStored", energyStorage.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.storedEmc = tag.getLong("StoredEmc");
        this.capacity = tag.getLong("Capacity");
        if (tag.contains("FEStored")) {
            this.energyStorage.receiveEnergy(tag.getInt("FEStored"), false);
        }
    }
}