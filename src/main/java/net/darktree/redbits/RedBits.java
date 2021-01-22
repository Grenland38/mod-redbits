package net.darktree.redbits;

import net.darktree.redbits.blocks.*;
import net.darktree.redbits.blocks.ComplexPressurePlateBlock.CollisionCondition;
import net.darktree.redbits.blocks.vision.VisionSensorNetwork;
import net.darktree.redbits.blocks.vision.VisionSensorTracker;
import net.darktree.redbits.utils.ColorProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AbstractTraderEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.WallStandingBlockItem;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;


public class RedBits implements ModInitializer, ClientModInitializer {

	public static final Logger LOGGER = LogManager.getLogger("RedBits");
	public static final Item.Settings SETTINGS = new Item.Settings().group(ItemGroup.REDSTONE);
	public static final String NAMESPACE = "redbits";

	@SuppressWarnings("unchecked")
	public final static CollisionCondition COLLISION_CONDITION_PET = ( World world, Box box ) -> {
		List<TameableEntity> l = world.getNonSpectatingEntities(TameableEntity.class, box);
		l.removeIf( n -> !n.isTamed() ); return (List<Entity>) (List<?>) l;
	};
	public final static CollisionCondition COLLISION_CONDITION_PLAYERS = ( World world, Box box ) -> world.getNonSpectatingEntities(PlayerEntity.class, box);
	public final static CollisionCondition COLLISION_CONDITION_HOSTILE = ( World world, Box box ) -> world.getNonSpectatingEntities(HostileEntity.class, box);
	public final static CollisionCondition COLLISION_CONDITION_VILLAGER = ( World world, Box box ) -> world.getNonSpectatingEntities(AbstractTraderEntity.class, box);

	// Buttons
	public final static Block OAK_LARGE_BUTTON = new LargeButtonBlock( true, AbstractBlock.Settings.of(Material.SUPPORTED).noCollision().strength(0.5F).sounds(BlockSoundGroup.WOOD));
	public final static Block SPRUCE_LARGE_BUTTON = new LargeButtonBlock( true, AbstractBlock.Settings.of(Material.SUPPORTED).noCollision().strength(0.5F).sounds(BlockSoundGroup.WOOD));
	public final static Block BIRCH_LARGE_BUTTON = new LargeButtonBlock( true, AbstractBlock.Settings.of(Material.SUPPORTED).noCollision().strength(0.5F).sounds(BlockSoundGroup.WOOD));
	public final static Block JUNGLE_LARGE_BUTTON = new LargeButtonBlock( true, AbstractBlock.Settings.of(Material.SUPPORTED).noCollision().strength(0.5F).sounds(BlockSoundGroup.WOOD));
	public final static Block ACACIA_LARGE_BUTTON = new LargeButtonBlock( true, AbstractBlock.Settings.of(Material.SUPPORTED).noCollision().strength(0.5F).sounds(BlockSoundGroup.WOOD));
	public final static Block DARK_OAK_LARGE_BUTTON = new LargeButtonBlock( true, AbstractBlock.Settings.of(Material.SUPPORTED).noCollision().strength(0.5F).sounds(BlockSoundGroup.WOOD));
	public final static Block CRIMSON_LARGE_BUTTON = new LargeButtonBlock( true, AbstractBlock.Settings.of(Material.SUPPORTED).noCollision().strength(0.5F).sounds(BlockSoundGroup.WOOD));
	public final static Block WARPED_LARGE_BUTTON = new LargeButtonBlock( true, AbstractBlock.Settings.of(Material.SUPPORTED).noCollision().strength(0.5F).sounds(BlockSoundGroup.WOOD));
	public final static Block STONE_LARGE_BUTTON = new LargeButtonBlock( false, AbstractBlock.Settings.of(Material.SUPPORTED).noCollision().strength(0.5F));
	public final static Block POLISHED_BLACKSTONE_LARGE_BUTTON = new LargeButtonBlock( false, AbstractBlock.Settings.of(Material.SUPPORTED).noCollision().strength(0.5F));

	// Gates
	public final static Block INVERTER = new InverterBlock(AbstractBlock.Settings.of(Material.SUPPORTED).breakInstantly().sounds(BlockSoundGroup.WOOD));
	public final static Block T_FLIP_FLOP = new FlipFlopBlock(AbstractBlock.Settings.of(Material.SUPPORTED).breakInstantly().sounds(BlockSoundGroup.WOOD));
	public final static Block DETECTOR = new DetectorBlock(AbstractBlock.Settings.of(Material.SUPPORTED).breakInstantly().sounds(BlockSoundGroup.WOOD));
	public final static Block TWO_WAY_REPEATER = new TwoWayRepeaterBlock(AbstractBlock.Settings.of(Material.SUPPORTED).breakInstantly().sounds(BlockSoundGroup.WOOD));
	public final static Block LATCH = new LatchBlock(AbstractBlock.Settings.of(Material.SUPPORTED).breakInstantly().sounds(BlockSoundGroup.WOOD));
	public final static Block TIMER = new TimerBlock(AbstractBlock.Settings.of(Material.SUPPORTED).breakInstantly().sounds(BlockSoundGroup.WOOD));

	// Pressure Plates
	public final static Block OBSIDIAN_PRESSURE_PLATE = new ComplexPressurePlateBlock( COLLISION_CONDITION_PLAYERS, AbstractBlock.Settings.of(Material.STONE, MaterialColor.BLACK).requiresTool().noCollision().strength(0.5F) );
	public final static Block CRYING_OBSIDIAN_PRESSURE_PLATE = new ComplexPressurePlateBlock( COLLISION_CONDITION_HOSTILE, AbstractBlock.Settings.of(Material.STONE, MaterialColor.BLACK).requiresTool().noCollision().strength(0.5F) );
	public final static Block END_STONE_PRESSURE_PLATE = new ComplexPressurePlateBlock( COLLISION_CONDITION_VILLAGER, AbstractBlock.Settings.of(Material.STONE, MaterialColor.SAND).requiresTool().noCollision().strength(0.5F) );
	public final static Block BASALT_PRESSURE_PLATE = new ComplexPressurePlateBlock( COLLISION_CONDITION_PET, AbstractBlock.Settings.of(Material.STONE, MaterialColor.BLACK).requiresTool().noCollision().strength(0.5F) );

	// Other Components
	public final static Block REDSTONE_LAMP = new RedstoneLampBlock(FabricBlockSettings.of(Material.REDSTONE_LAMP).lightLevel((n) -> n.get(Properties.LIT) ? 1 : 0).postProcess((a, b, c) -> a.get(Properties.LIT)).emissiveLighting((a, b, c) -> a.get(Properties.LIT)).strength(0.3F).sounds(BlockSoundGroup.GLASS).allowsSpawning( (BlockState state, BlockView world, BlockPos pos, EntityType<?> type) -> true ) );
	public final static Block RGB_LAMP = new AnalogLampBlock(FabricBlockSettings.of(Material.REDSTONE_LAMP).lightLevel((n) -> n.get(AnalogLampBlock.POWER) > 0 ? 1 : 0).postProcess((a, b, c) -> a.get(AnalogLampBlock.POWER) > 0).emissiveLighting((a, b, c) -> a.get(AnalogLampBlock.POWER) > 0).strength(0.3F).sounds(BlockSoundGroup.GLASS).allowsSpawning( (BlockState state, BlockView world, BlockPos pos, EntityType<?> type) -> true ) );
	public final static Block REDSTONE_EMITTER = new EmitterBlock( AbstractBlock.Settings.of(Material.STONE).requiresTool().strength(3.5F).solidBlock( (BlockState state, BlockView world, BlockPos pos) -> true ) );
	public final static Block VISION_SENSOR = new VisionSensorBlock( AbstractBlock.Settings.of(Material.STONE).requiresTool().strength(3.5F).solidBlock( (BlockState state, BlockView world, BlockPos pos) -> true ) );
	public final static Block POWER_OBSERVER = new PowerObserverBlock(AbstractBlock.Settings.of(Material.STONE).strength(3.0F).requiresTool().solidBlock((state, world, pos) -> false));
	public final static Block INVERTED_REDSTONE_TORCH = new InvertedRedstoneTorchBlock(AbstractBlock.Settings.of(Material.SUPPORTED).noCollision().breakInstantly().lightLevel( (n) -> n.get(Properties.LIT) ? 7 : 0 ).sounds(BlockSoundGroup.WOOD));
	public final static Block INVERTED_REDSTONE_WALL_TORCH = new WallInvertedRedstoneTorchBlock(AbstractBlock.Settings.of(Material.SUPPORTED).noCollision().breakInstantly().lightLevel( (n) -> n.get(Properties.LIT) ? 7 : 0 ).sounds(BlockSoundGroup.WOOD));

	@Override
	public void onInitialize() {
		registerBlock("inverted_redstone_torch", INVERTED_REDSTONE_TORCH);
		registerBlock("inverted_redstone_wall_torch", INVERTED_REDSTONE_WALL_TORCH);
		registerItem("inverted_redstone_torch", new WallStandingBlockItem(INVERTED_REDSTONE_TORCH, INVERTED_REDSTONE_WALL_TORCH, SETTINGS));
		register("two_way_repeater", TWO_WAY_REPEATER);
		register("t_flip_flop", T_FLIP_FLOP);
		register("inverter", INVERTER);
		register("detector", DETECTOR);
		register("latch", LATCH);
		register("emitter", REDSTONE_EMITTER);
		register("redstone_lamp", REDSTONE_LAMP);
		register("oak_large_button", OAK_LARGE_BUTTON);
		register("spruce_large_button", SPRUCE_LARGE_BUTTON);
		register("birch_large_button", BIRCH_LARGE_BUTTON);
		register("jungle_large_button", JUNGLE_LARGE_BUTTON);
		register("acacia_large_button", ACACIA_LARGE_BUTTON);
		register("dark_oak_large_button", DARK_OAK_LARGE_BUTTON);
		register("crimson_large_button", CRIMSON_LARGE_BUTTON);
		register("warped_large_button", WARPED_LARGE_BUTTON);
		register("stone_large_button", STONE_LARGE_BUTTON);
		register("polished_blackstone_large_button", POLISHED_BLACKSTONE_LARGE_BUTTON);
		register("obsidian_pressure_plate", OBSIDIAN_PRESSURE_PLATE);
		register("crying_obsidian_pressure_plate", CRYING_OBSIDIAN_PRESSURE_PLATE);
		register("end_stone_pressure_plate", END_STONE_PRESSURE_PLATE);
		register("basalt_pressure_plate", BASALT_PRESSURE_PLATE);
		register("rgb_lamp", RGB_LAMP);
		register("power_observer", POWER_OBSERVER);
		register("timer", TIMER);
		register("vision_sensor", VISION_SENSOR);

		VisionSensorNetwork.init();
	}

	@Override
	public void onInitializeClient() {
		cutout(INVERTER);
		cutout(T_FLIP_FLOP);
		cutout(DETECTOR);
		cutout(LATCH);
		cutout(TWO_WAY_REPEATER);
		cutout(INVERTED_REDSTONE_TORCH);
		cutout(INVERTED_REDSTONE_WALL_TORCH);
		cutout(TIMER);
		ColorProviderRegistry.ITEM.register( (stack, tintIndex) -> RedstoneWireBlock.getWireColor(1), REDSTONE_EMITTER );
		ColorProviderRegistry.BLOCK.register( (state, view, pos, tintIndex) -> RedstoneWireBlock.getWireColor( state.get( EmitterBlock.POWER ) ), REDSTONE_EMITTER );
		ColorProviderRegistry.ITEM.register( (stack, tintIndex) -> ColorProvider.getColor(0), RGB_LAMP );
		ColorProviderRegistry.BLOCK.register( (state, view, pos, tintIndex) -> ColorProvider.getColor(state.get(AnalogLampBlock.POWER)), RGB_LAMP );

		VisionSensorTracker.init();
	}

	private void registerBlock( String name, Block block ) {
		Registry.register(Registry.BLOCK, new Identifier( NAMESPACE, name ), block);
	}

	private void registerItem( String name, Item item ) {
		Registry.register(Registry.ITEM, new Identifier( NAMESPACE, name ), item);
	}

	private void register( String name, Block block ) {
		registerBlock( name, block );
		registerItem( name, new BlockItem( block, SETTINGS ) );
	}

	@Environment(EnvType.CLIENT)
	private void cutout( Block block ) {
		BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout());
	}

}
