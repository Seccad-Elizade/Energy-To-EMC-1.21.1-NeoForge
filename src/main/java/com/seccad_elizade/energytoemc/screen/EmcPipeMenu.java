package com.seccad_elizade.energytoemc.screen;

import com.seccad_elizade.energytoemc.block.entity.EmcPipeBlockEntity;
import com.seccad_elizade.energytoemc.registry.ModBlocks;
import com.seccad_elizade.energytoemc.registry.ModMenuTypes;
import com.seccad_elizade.energytoemc.item.PipeUpgradeItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class EmcPipeMenu extends AbstractContainerMenu {
    private final EmcPipeBlockEntity blockEntity;
    private final ContainerData data;

    public EmcPipeMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(1));
    }

    public EmcPipeMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.EMC_PIPE_MENU.get(), containerId);

        if (entity instanceof EmcPipeBlockEntity pipeBe) {
            this.blockEntity = pipeBe;
        } else {
            throw new IllegalStateException("BlockEntity is not an EmcPipeBlockEntity!");
        }

        this.data = data;
        checkContainerDataCount(data, 1);

        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 80, 25) {
            @Override
            public boolean isActive() {
                return isExtractMode();
            }

            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return isExtractMode() && stack.getItem() instanceof PipeUpgradeItem;
            }
        });

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        addDataSlots(data);
    }

    public boolean isExtractMode() {
        return this.data.get(0) == 1;
    }

    public EmcPipeBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index == 0) {
                if (!this.moveItemStackTo(itemstack1, 1, 37, true)) return ItemStack.EMPTY;
                slot.onQuickCraft(itemstack1, itemstack);
            } else {
                if (isExtractMode() && itemstack1.getItem() instanceof PipeUpgradeItem) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) return ItemStack.EMPTY;
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();

            if (itemstack1.getCount() == itemstack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, itemstack1);
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, ModBlocks.EMC_PIPE.get());
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