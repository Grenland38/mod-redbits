package net.darktree.redbits.blocks;

import net.darktree.redbits.utils.TwoWayPower;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.Random;

public class TwoWayRepeaterBlock extends AbstractRedstoneGate {

    public static final EnumProperty<TwoWayPower> POWER = EnumProperty.of( "power", TwoWayPower.class );
    public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;

    public TwoWayRepeaterBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(AXIS, Direction.Axis.X).with(POWER, TwoWayPower.NONE));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS, POWER);
    }

    @Override
    public boolean connectsTo(BlockState state, Direction direction) {
        return state.get(AXIS) == direction.getAxis();
    }

    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        TwoWayPower power = state.get(POWER);
        TwoWayPowerUnit block = getPower(world, pos, state, power);
        boolean locked = power != TwoWayPower.NONE;

        if( !locked && block.getPower() > 0 && block.getDirection() != power ) {
            world.setBlockState(pos, state.with(POWER, block.getDirection()), 2);
        }else if( block.getPower() == 0 ) {
            world.setBlockState(pos, state.with(POWER, TwoWayPower.NONE), 2);
        }else if( !locked ) {
            world.setBlockState(pos, state.with(POWER, getPower(world, pos, state, TwoWayPower.NONE).getDirection()), 2);
            world.getBlockTickScheduler().schedule(pos, this, this.getUpdateDelayInternal(), TickPriority.VERY_HIGH);
        }
    }

    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if( state.get(POWER) == TwoWayPower.NONE ) {
            return 0;
        } else {
            return state.get(AXIS) == direction.getAxis() && state.get(POWER).isAligned( direction.getDirection() ) ? 15 : 0;
        }
    }

    protected void updatePowered(World world, BlockPos pos, BlockState state) {
        boolean power = state.get(POWER) != TwoWayPower.NONE;
        boolean block = this.hasPower(world, pos, state, state.get(POWER));

        if (power != block && !world.getBlockTickScheduler().isTicking(pos, this)) {
            TickPriority tickPriority = TickPriority.HIGH;
            if (this.isTargetNotAligned(world, pos, state)) {
                tickPriority = TickPriority.EXTREMELY_HIGH;
            } else if (power) {
                tickPriority = TickPriority.VERY_HIGH;
            }

            world.getBlockTickScheduler().schedule(pos, this, this.getUpdateDelayInternal(), tickPriority);
        }
    }

    protected boolean hasPower(World world, BlockPos pos, BlockState state, TwoWayPower power ) {
        return this.getPower(world, pos, state, power).getPower() > 0;
    }

    protected TwoWayPowerUnit getPower(World world, BlockPos pos, BlockState state, TwoWayPower power ) {

        if (power == TwoWayPower.NONE) {
            TwoWayPowerUnit a = getPower( world, pos, state, TwoWayPower.FRONT );
            if ( a.getPower() > 0 ) return a;
            TwoWayPowerUnit b =  getPower( world, pos, state, TwoWayPower.BACK );
            if ( b.getPower() > 0 ) return b;
            return new TwoWayPowerUnit( TwoWayPower.NONE, 0 );
        }

        Direction direction = Direction.from( state.get(AXIS), power.asAxisDirection() );
        BlockPos blockPos = pos.offset( direction );

        return new TwoWayPowerUnit( power, getInputPower( world, blockPos, direction ) );
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(AXIS, ctx.getPlayerFacing().getAxis());
    }

    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (this.hasPower(world, pos, state, state.get(POWER))) {
            world.getBlockTickScheduler().schedule(pos, this, 1);
        }
    }

    protected void updateTarget(World world, BlockPos pos, BlockState state) {
        Direction direction = Direction.from( state.get(AXIS), Direction.AxisDirection.NEGATIVE ).getOpposite();
        BlockPos blockPos = pos.offset(direction);
        world.updateNeighbor(blockPos, this, pos);
        world.updateNeighborsExcept(blockPos, this, direction);

        direction = Direction.from( state.get(AXIS), Direction.AxisDirection.POSITIVE ).getOpposite();
        blockPos = pos.offset(direction);
        world.updateNeighbor(blockPos, this, pos);
        world.updateNeighborsExcept(blockPos, this, direction);
    }

    public boolean isTargetNotAligned(BlockView world, BlockPos pos, BlockState state) {
        if( state.get(POWER) != TwoWayPower.NONE ) {
            Direction direction = Direction.from( state.get(AXIS), state.get(POWER).asAxisDirection() );
            BlockState blockState = world.getBlockState(pos.offset(direction));
            return isRedstoneGate(blockState) && blockState.get(HorizontalFacingBlock.FACING) != direction;
        }
        return false;
    }

    static class TwoWayPowerUnit {

        private final int power;
        private final TwoWayPower direction;

        TwoWayPowerUnit( TwoWayPower direction, int power ) {
            this.direction = direction;
            this.power = power;
        }

        public int getPower() {
            return power;
        }

        public TwoWayPower getDirection() {
            return direction;
        }

    }

}
