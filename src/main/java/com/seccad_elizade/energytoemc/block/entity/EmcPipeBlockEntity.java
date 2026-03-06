package com.seccad_elizade.energytoemc.block.entity;

import com.seccad_elizade.energytoemc.block.EmcPipeBlock;
import com.seccad_elizade.energytoemc.item.PipeUpgradeItem;
import com.seccad_elizade.energytoemc.registry.ModBlockEntities;
import com.seccad_elizade.energytoemc.screen.EmcPipeMenu;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EmcPipeBlockEntity extends BlockEntity implements MenuProvider, IEmcStorage {
    private int pipeMode = 0;
    private long internalEmcBuffer = 0;
    private final EnergyStorage energy = new EnergyStorage(10000, 1000, 1000);
    private boolean isScanning = false;

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof PipeUpgradeItem;
        }
    };

    protected final ContainerData data = new ContainerData() {
        @Override public int get(int index) { return index == 0 ? pipeMode : 0; }
        @Override public void set(int index, int value) { if (index == 0) pipeMode = value; }
        @Override public int getCount() { return 1; }
    };

    public EmcPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EMC_PIPE_BE.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return this.energy; }
    public ItemStackHandler getItemHandler() { return this.itemHandler; }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public boolean isConnectedToMachine() {
        if (level == null) return false;
        for (Direction dir : Direction.values()) {
            if (getBlockState().getValue(EmcPipeBlock.getPropertyForDirection(dir))) {
                BlockPos neighborPos = worldPosition.relative(dir);
                if (!(level.getBlockState(neighborPos).getBlock() instanceof EmcPipeBlock)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public long insertEmc(long amount, EmcAction action) {
        if (this.pipeMode == 1 || amount <= 0 || level == null || isScanning) return 0;
        for (Direction dir : Direction.values()) {
            if (!getBlockState().getValue(EmcPipeBlock.getPropertyForDirection(dir))) continue;
            BlockPos neighborPos = worldPosition.relative(dir);
            if (level.getBlockState(neighborPos).getBlock() instanceof EmcPipeBlock) continue;

            IEmcStorage machine = level.getCapability(PECapabilities.EMC_STORAGE_CAPABILITY, neighborPos, dir.getOpposite());
            if (machine != null) {
                long accepted = machine.insertEmc(amount, action);
                if (accepted > 0 && action.execute()) {
                    this.internalEmcBuffer = accepted;
                    setChanged();
                }
                return accepted;
            }
        }
        return 0;
    }

    @Override public long extractEmc(long amount, EmcAction action) { return 0; }
    @Override public long getStoredEmc() { return this.internalEmcBuffer; }
    @Override public long getMaximumEmc() { return getTransferLimit(); }

    public static void tick(Level level, BlockPos pos, BlockState state, EmcPipeBlockEntity entity) {
        if (level.isClientSide) return;

        if (entity.internalEmcBuffer > 0) {
            entity.internalEmcBuffer = Math.max(0, entity.internalEmcBuffer - 500);
        }

        boolean shouldBeLit = entity.internalEmcBuffer > 0;
        if (state.hasProperty(EmcPipeBlock.LIT) && state.getValue(EmcPipeBlock.LIT) != shouldBeLit) {
            level.setBlock(pos, state.setValue(EmcPipeBlock.LIT, shouldBeLit), 3);
            level.sendBlockUpdated(pos, state, state.setValue(EmcPipeBlock.LIT, shouldBeLit), 3);
        }

        if (entity.pipeMode == 1) {
            entity.pumpFromMachines(level, pos, state);
        }
    }

    private void pumpFromMachines(Level level, BlockPos pos, BlockState state) {
        for (Direction dir : Direction.values()) {
            if (!state.getValue(EmcPipeBlock.getPropertyForDirection(dir))) continue;
            BlockPos targetPos = pos.relative(dir);
            if (level.getBlockState(targetPos).getBlock() instanceof EmcPipeBlock) continue;

            IEmcStorage sourceMachine = level.getCapability(PECapabilities.EMC_STORAGE_CAPABILITY, targetPos, dir.getOpposite());
            if (sourceMachine != null && sourceMachine.getStoredEmc() > 0) {
                long toMove = Math.min(sourceMachine.getStoredEmc(), getTransferLimit());
                Set<BlockPos> network = new HashSet<>();
                List<EmcPipeBlockEntity> sinks = new ArrayList<>();
                findNetworkSinks(level, pos, network, sinks);

                if (sinks.isEmpty()) return;

                long perSink = toMove / sinks.size();
                if (perSink <= 0) perSink = 1;

                long totalMoved = 0;
                this.isScanning = true;
                try {
                    for (EmcPipeBlockEntity sinkPipe : sinks) {
                        totalMoved += sinkPipe.insertEmc(perSink, EmcAction.EXECUTE);
                    }
                } finally {
                    this.isScanning = false;
                }

                if (totalMoved > 0) {
                    sourceMachine.extractEmc(totalMoved, EmcAction.EXECUTE);
                    for (BlockPos p : network) {
                        if (level.getBlockEntity(p) instanceof EmcPipeBlockEntity pbe) {
                            pbe.internalEmcBuffer = totalMoved;
                            BlockState pState = level.getBlockState(p);
                            if (pState.hasProperty(EmcPipeBlock.LIT) && !pState.getValue(EmcPipeBlock.LIT)) {
                                level.setBlock(p, pState.setValue(EmcPipeBlock.LIT, true), 3);
                            }
                            pbe.setChanged();
                        }
                    }
                    setChanged();
                }
            }
        }
    }

    private void findNetworkSinks(Level level, BlockPos start, Set<BlockPos> visited, List<EmcPipeBlockEntity> sinks) {
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockState state = level.getBlockState(current);
            if (!(state.getBlock() instanceof EmcPipeBlock)) continue;
            for (Direction dir : Direction.values()) {
                if (!state.getValue(EmcPipeBlock.getPropertyForDirection(dir))) continue;
                BlockPos next = current.relative(dir);
                if (visited.contains(next)) continue;
                if (level.getBlockEntity(next) instanceof EmcPipeBlockEntity neighborPipe) {
                    visited.add(next);
                    queue.add(next);
                    if (neighborPipe.pipeMode == 0) {
                        sinks.add(neighborPipe);
                    }
                }
            }
        }
    }

    private long getTransferLimit() {
        long base = 2048L;
        ItemStack stack = itemHandler.getStackInSlot(0);
        if (!stack.isEmpty() && stack.getItem() instanceof PipeUpgradeItem upgrade) {
            return base + (upgrade.getTransferPerTick() * 20L);
        }
        return base;
    }

    public void toggleMode() {
        this.pipeMode = (this.pipeMode == 0) ? 1 : 0;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.saveAdditional(tag, reg);
        tag.putInt("pipeMode", pipeMode);
        tag.putLong("buffer", internalEmcBuffer);
        tag.putInt("energy", energy.getEnergyStored());
        tag.put("inv", itemHandler.serializeNBT(reg));
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.loadAdditional(tag, reg);
        this.pipeMode = tag.getInt("pipeMode");
        this.internalEmcBuffer = tag.getLong("buffer");
        if (tag.contains("energy")) {
            this.energy.receiveEnergy(tag.getInt("energy") - energy.getEnergyStored(), false);
        }
        itemHandler.deserializeNBT(reg, tag.getCompound("inv"));
    }

    public void drops() {
        if (this.level != null) {
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if (!stack.isEmpty()) Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
            }
        }
    }

    @Override public @NotNull Component getDisplayName() { return Component.literal("EMC Pipe"); }

    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new EmcPipeMenu(id, inv, this, this.data);
    }
}