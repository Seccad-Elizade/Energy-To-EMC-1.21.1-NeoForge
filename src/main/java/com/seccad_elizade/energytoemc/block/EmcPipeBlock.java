package com.seccad_elizade.energytoemc.block;

import com.mojang.serialization.MapCodec;
import com.seccad_elizade.energytoemc.block.entity.EmcPipeBlockEntity;
import com.seccad_elizade.energytoemc.registry.ModBlockEntities;
import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmcPipeBlock extends BaseEntityBlock {
    public static final MapCodec<EmcPipeBlock> CODEC = simpleCodec(EmcPipeBlock::new);

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    public EmcPipeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false)
                .setValue(LIT, false)
                .setValue(AXIS, Direction.Axis.Y));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        VoxelShape shape = Block.box(5, 5, 5, 11, 11, 11);
        if (state.getValue(NORTH)) shape = Shapes.or(shape, Block.box(5, 5, 0, 11, 11, 5));
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, Block.box(5, 5, 11, 11, 11, 16));
        if (state.getValue(EAST)) shape = Shapes.or(shape, Block.box(11, 5, 5, 16, 11, 11));
        if (state.getValue(WEST)) shape = Shapes.or(shape, Block.box(0, 5, 5, 5, 11, 11));
        if (state.getValue(UP)) shape = Shapes.or(shape, Block.box(5, 11, 5, 11, 16, 11));
        if (state.getValue(DOWN)) shape = Shapes.or(shape, Block.box(5, 0, 5, 11, 5, 11));
        return shape;
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof EmcPipeBlockEntity pipeBe)) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean isWrench = !stack.isEmpty() && stack.getItem().toString().toLowerCase().contains("wrench");

        boolean connectedToMachine = false;
        for (Direction dir : Direction.values()) {
            if (state.getValue(getPropertyForDirection(dir))) {
                BlockPos neighborPos = pos.relative(dir);
                if (!(level.getBlockState(neighborPos).getBlock() instanceof EmcPipeBlock)) {
                    connectedToMachine = true;
                    break;
                }
            }
        }

        if (isWrench) {
            if (player.isShiftKeyDown()) {
                pipeBe.toggleMode();
                player.displayClientMessage(Component.literal("Pipe Mode Toggled").withStyle(ChatFormatting.AQUA), true);
            } else {
                Direction side = hit.getDirection();
                BlockPos neighborPos = pos.relative(side);

                if (level.getBlockState(neighborPos).getBlock() instanceof EmcPipeBlock) {
                    return InteractionResult.PASS;
                }

                BooleanProperty prop = getPropertyForDirection(side);
                level.setBlock(pos, state.setValue(prop, !state.getValue(prop)), 3);
            }
            return InteractionResult.SUCCESS;
        }

        if (stack.isEmpty()) {
            if (!connectedToMachine) {
                player.displayClientMessage(Component.literal("§cYou can only configure pipes connected to machines!"), true);
                return InteractionResult.FAIL;
            }
            player.openMenu(pipeBe, pos);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public boolean canConnectTo(LevelAccessor level, BlockPos neighborPos, Direction side) {
        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof EmcPipeBlock) return true;
        if (level instanceof Level fullLevel) {
            if (fullLevel.getCapability(PECapabilities.EMC_STORAGE_CAPABILITY, neighborPos, side.getOpposite()) != null) {
                return true;
            }
        }
        return false;
    }

    public static BooleanProperty getPropertyForDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return this.defaultBlockState()
                .setValue(NORTH, canConnectTo(level, pos.north(), Direction.NORTH))
                .setValue(SOUTH, canConnectTo(level, pos.south(), Direction.SOUTH))
                .setValue(EAST, canConnectTo(level, pos.east(), Direction.EAST))
                .setValue(WEST, canConnectTo(level, pos.west(), Direction.WEST))
                .setValue(UP, canConnectTo(level, pos.above(), Direction.UP))
                .setValue(DOWN, canConnectTo(level, pos.below(), Direction.DOWN))
                .setValue(LIT, false)
                .setValue(AXIS, context.getNearestLookingDirection().getAxis());
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, @NotNull Direction dir, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        return state.setValue(getPropertyForDirection(dir), canConnectTo(level, neighborPos, dir));
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EmcPipeBlockEntity pipeBe) {
                pipeBe.drops();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new EmcPipeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.EMC_PIPE_BE.get(), EmcPipeBlockEntity::tick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, LIT, AXIS);
    }
}