package com.seccad_elizade.energytoemc.item;

import com.seccad_elizade.energytoemc.block.EmcPipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;

public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        Player player = context.getPlayer();
        BlockState clickedState = level.getBlockState(clickedPos);

        if (player == null) return InteractionResult.PASS;

        if (clickedState.getBlock() instanceof EmcPipeBlock) {
            if (!level.isClientSide) {
                Direction sideToToggle = getTargetDirection(context);
                toggleConnection(level, clickedPos, clickedState, sideToToggle, player);
                level.playSound(null, clickedPos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.5f, 2.0f);
            }
            player.swing(context.getHand());
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        BlockPos pipePos = clickedPos.relative(clickedFace);
        BlockState pipeState = level.getBlockState(pipePos);

        if (pipeState.getBlock() instanceof EmcPipeBlock) {
            if (!level.isClientSide) {
                Direction sideOfPipe = clickedFace.getOpposite();
                toggleConnection(level, pipePos, pipeState, sideOfPipe, player);
                level.playSound(null, pipePos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.5f, 2.0f);
            }
            player.swing(context.getHand());
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    private Direction getTargetDirection(UseOnContext context) {
        Vec3 hitVec = context.getClickLocation().subtract(Vec3.atLowerCornerOf(context.getClickedPos()));
        double x = hitVec.x - 0.5;
        double y = hitVec.y - 0.5;
        double z = hitVec.z - 0.5;
        double absX = Math.abs(x);
        double absY = Math.abs(y);
        double absZ = Math.abs(z);

        if (absX > absY && absX > absZ) return x > 0 ? Direction.EAST : Direction.WEST;
        if (absY > absX && absY > absZ) return y > 0 ? Direction.UP : Direction.DOWN;
        return z > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private void toggleConnection(Level level, BlockPos pos, BlockState state, Direction side, Player player) {
        BooleanProperty sideProperty = EmcPipeBlock.getPropertyForDirection(side);
        boolean newState = !state.getValue(sideProperty);

        BlockPos neighborPos = pos.relative(side);
        EmcPipeBlock pipeBlock = (EmcPipeBlock) state.getBlock();

        if (newState && !pipeBlock.canConnectTo(level, neighborPos, side)) {
            player.displayClientMessage(Component.literal("§cCannot connect pipe to this block!"), true);
            return;
        }

        level.setBlock(pos, state.setValue(sideProperty, newState), 3);

        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof EmcPipeBlock) {
            Direction opposingSide = side.getOpposite();
            BooleanProperty opposingProperty = EmcPipeBlock.getPropertyForDirection(opposingSide);
            if (neighborState.getValue(opposingProperty) != newState) {
                level.setBlock(neighborPos, neighborState.setValue(opposingProperty, newState), 3);
            }
        }

        String color = newState ? "§a" : "§c";
        String status = newState ? "Connected" : "Disconnected";
        player.displayClientMessage(Component.literal(color + "Side [" + side.name() + "] " + status), true);
    }
}