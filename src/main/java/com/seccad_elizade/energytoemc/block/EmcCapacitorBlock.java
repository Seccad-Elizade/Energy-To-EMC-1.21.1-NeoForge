package com.seccad_elizade.energytoemc.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.seccad_elizade.energytoemc.block.entity.EmcCapacitorBlockEntity;
import com.seccad_elizade.energytoemc.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EmcCapacitorBlock extends BaseEntityBlock {
    public static final MapCodec<EmcCapacitorBlock> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            propertiesCodec(),
            Codec.LONG.fieldOf("capacity").forGetter(EmcCapacitorBlock::getCapacity)
    ).apply(inst, EmcCapacitorBlock::new));

    private final long capacity;

    public EmcCapacitorBlock(Properties properties, long capacity) {
        super(properties);
        this.capacity = capacity;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    public long getCapacity() { return this.capacity; }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EmcCapacitorBlockEntity(pos, state, this.capacity);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.EMC_CAPACITOR_BE.get(),
                (lvl, pos, st, be) -> be.tick(lvl, pos, st));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EmcCapacitorBlockEntity capacitor) {
                player.openMenu(capacitor, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Component amountText;
        if (this.capacity >= Long.MAX_VALUE || this.capacity < 0) {
            amountText = Component.literal("∞").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.OBFUSCATED);
        } else {
            amountText = Component.literal(String.format("%,d", this.capacity)).withStyle(ChatFormatting.WHITE);
        }

        tooltip.add(Component.translatable("tooltip.energytoemc.max_storage")
                .append(Component.literal(": "))
                .append(amountText)
                .append(Component.literal(" EMC").withStyle(ChatFormatting.GOLD))
                .withStyle(ChatFormatting.GOLD));

        super.appendHoverText(stack, context, tooltip, flag);
    }
}