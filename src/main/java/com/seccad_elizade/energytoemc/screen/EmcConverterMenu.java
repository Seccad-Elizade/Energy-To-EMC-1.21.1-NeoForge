package com.seccad_elizade.energytoemc.screen;

import com.seccad_elizade.energytoemc.block.entity.EmcConverterBlockEntity;
import com.seccad_elizade.energytoemc.registry.ModBlocks;
import com.seccad_elizade.energytoemc.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class EmcConverterMenu extends AbstractContainerMenu {
    private final EmcConverterBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public EmcConverterMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(13));
    }

    public EmcConverterMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.EMC_CONVERTER_MENU.get(), containerId);

        if (entity instanceof EmcConverterBlockEntity converterEntity) {
            this.blockEntity = converterEntity;
        } else {
            throw new IllegalStateException("BlockEntity is not an EmcConverterBlockEntity!");
        }

        checkContainerDataCount(data, 13);
        this.level = inv.player.level();
        this.data = data;

        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 80, 35));

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        addDataSlots(data);
    }

    public EmcConverterBlockEntity getBlockEntity() { return this.blockEntity; }

    public int getEnergy() { return this.data.get(0) | (this.data.get(1) << 16); }
    public long getEmc() { return (long) this.data.get(2) | ((long) this.data.get(3) << 16); }
    public int getEnergyUsage() { return this.data.get(11); }
    public int getEmcProduction() { return this.data.get(12); }
    public int getRedstoneMode() { return this.data.get(10); }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyStack = sourceStack.copy();

        if (index < 1) {
            if (!moveItemStackTo(sourceStack, 1, 37, true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(sourceStack, 0, 1, false)) return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) sourceSlot.setByPlayer(ItemStack.EMPTY);
        else sourceSlot.setChanged();

        sourceSlot.onTake(playerIn, sourceStack);
        return copyStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.EMC_CONVERTER.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}