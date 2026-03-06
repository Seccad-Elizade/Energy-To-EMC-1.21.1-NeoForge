package com.seccad_elizade.energytoemc.screen;

import com.seccad_elizade.energytoemc.block.entity.EmcCapacitorBlockEntity;
import com.seccad_elizade.energytoemc.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EmcCapacitorMenu extends AbstractContainerMenu {
    private final EmcCapacitorBlockEntity blockEntity;
    private final ContainerData data;

    public EmcCapacitorMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(8));
    }

    public EmcCapacitorMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.EMC_CAPACITOR_MENU.get(), containerId);

        if (entity instanceof EmcCapacitorBlockEntity capacitor) {
            this.blockEntity = capacitor;
        } else {
            throw new IllegalStateException("BlockEntity is not an EmcCapacitorBlockEntity! (Got: " + entity + ")");
        }

        checkContainerDataCount(data, 8);
        this.data = data;
        addDataSlots(data);

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    public long getStoredEmc() {
        return ((long) (data.get(0) & 0xFFFF)) |
                ((long) (data.get(1) & 0xFFFF) << 16) |
                ((long) (data.get(2) & 0xFFFF) << 32) |
                ((long) (data.get(3) & 0xFFFF) << 48);
    }

    public long getMaxEmc() {
        return ((long) (data.get(4) & 0xFFFF)) |
                ((long) (data.get(5) & 0xFFFF) << 16) |
                ((long) (data.get(6) & 0xFFFF) << 32) |
                ((long) (data.get(7) & 0xFFFF) << 48);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, blockEntity.getBlockState().getBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < 27) {
            if (!this.moveItemStackTo(stack, 27, 36, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(stack, 0, 27, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return copy;
    }

    private void addPlayerInventory(Inventory inv) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(inv, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
        }
    }
}