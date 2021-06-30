package net.darktree.redbits;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.darktree.redbits.blocks.*;
import net.darktree.redbits.blocks.ComplexPressurePlateBlock.CollisionCondition;
import net.darktree.redbits.blocks.vision.VisionSensorNetwork;
import net.darktree.redbits.blocks.vision.VisionSensorTracker;
import net.darktree.redbits.config.RedBitsConfig;
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
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.WallStandingBlockItem;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Predicate;

public class RedBits implements ModInitializer, ClientModInitializer {

	public static final Logger LOGGER = LogManager.getLogger("RedBits");
	public static final RedBitsConfig CONFIG = AutoConfig.register(RedBitsConfig.class, PartitioningSerializer.wrap(GsonConfigSerializer::new)).getConfig();
	public static final Item.Settings SETTINGS = new Item.Settings().group(ItemGroup.REDSTONE);
	public static final String NAMESPACE = "redbits";

	private final static Predicate<Entity> CANT_AVOID_TRAPS = n -> !n.canAvoidTraps();
	public final static CollisionCondition COLLISION_CONDITION_PET = ( World world, Box box ) -> world.getNonSpectatingEntities(TameableEntity.class, box).stream().anyMatch(n -> n.isTamed() && !n.canAvoidTraps());
	public final static CollisionCondition COLLISION_CONDITION_PLAYERS = ( World world, Box box ) -> world.getNonSpectatingEntities(PlayerEntity.class, box).stream().anyMatch(CANT_AVOID_TRAPS);
	public final static CollisionCondition COLLISION_CONDITION_HOSTILE = ( World world, Box box ) -> world.getNonSpectatingEntities(HostileEntity.class, box).stream().anyMatch(CANT_AVOID_TRAPS);
	public final static CollisionCondition COLLISION_CONDITION_VILLAGER = ( World world, Box box ) -> world.getNonSpectatingEntities(VillagerEntity.class, box).stream().anyMatch(CANT_AVOID_TRAPS);

	// Buttons
	public final static Block OAK_LARGE_BUTTON = new LargeButtonBlock( true, AbstractBlock.Settings.of(Material.DECORATION).noCollision().strength(0.5F).sounds(BlockSoundGroup.WOOD));
	public final static Block SPRUCE_LARGE_BUTTON = new LargeButtonBlock( true, AbstractBlock.Settings.of(Material.DECORATION).noCollision().strength(0.5F).sounds(BlockSoundGroup.WOOD));
	public final static Block BIRCH_LARGE_BUTTON = new LargeButtonBlock( true, AbstractBlock.Settings.of(Material.DECORATION).noCollision().strength(0.5F).sounds(BlockSoundGroup.WOOD));
	public final static Block JUNGLE_LARGE_BUTTON = new LargeButtonBlock( true, AbstractBlock.Settings.of(Material.DECORATION).noCollision().strength(0.5F).sounds(BlockSoundGroup.WOOD));
	public final static Block ACACIA_LARGE_BUTTON = new LargeButtonBlock( true, AbstractBlock.Settings.of(Material.DECORATION).noCollision().strength(0.5F).sounds(BlockSoundGroup.WOOD));
	public final static Block DARK_OAK_LARGE_BUTTON = new LargeButtonBlock( true, AbstractBlock.Settings.of(Material.DECORATION).noCollision().strength(0.5F).sounds(BlockSoundGroup.WOOD));
	public final static Block CRIMSON_LARGE_BUTTON = new LargeButtonBlock( true, AbstractBlock.Settings.of(Material.DECORATION).noCollision().strength(0.5F).sounds(BlockSoundGroup.WOOD));
	public final static Block WARPED_LARGE_BUTTON = new LargeButtonBlock( true, AbstractBlock.Settings.of(Material.DECORATION).noCollision().strength(0.5F).sounds(BlockSoundGroup.WOOD));
	public final static Block STONE_LARGE_BUTTON = new LargeButtonBlock( false, AbstractBlock.Settings.of(Material.DECORATION).noCollision().strength(0.5F));
	public final static Block POLISHED_BLACKSTONE_LARGE_BUTTON = new LargeButtonBlock( false, AbstractBlock.Settings.of(Material.DECORATION).noCollision().strength(0.5F));

	// Gates
	public final static Block INVERTER = new InverterBlock(AbstractBlock.Settings.of(Material.DECORATION).breakInstantly().sounds(BlockSoundGroup.WOOD));
	public final static Block T_FLIP_FLOP = new FlipFlopBlock(AbstractBlock.Settings.of(Material.DECORATION).breakInstantly().sounds(BlockSoundGroup.WOOD));
	public final static Block DETECTOR = new DetectorBlock(AbstractBlock.Settings.of(Material.DECORATION).breakInstantly().sounds(BlockSoundGroup.WOOD));
	public final static Block TWO_WAY_REPEATER = new TwoWayRepeaterBlock(AbstractBlock.Settings.of(Material.DECORATION).breakInstantly().sounds(BlockSoundGroup.WOOD));
	public final static Block LATCH = new LatchBlock(AbstractBlock.Settings.of(Material.DECORATION).breakInstantly().sounds(BlockSoundGroup.WOOD));
	public final static Block TIMER = new TimerBlock(AbstractBlock.Settings.of(Material.DECORATION).breakInstantly().sounds(BlockSoundGroup.WOOD));

	// Pressure Plates
	public final static Block OBSIDIAN_PRESSURE_PLATE = new ComplexPressurePlateBlock( COLLISION_CONDITION_PLAYERS, AbstractBlock.Settings.of(Material.STONE, MapColor.BLACK).requiresTool().noCollision().strength(0.5F) );
	public final static Block CRYING_OBSIDIAN_PRESSURE_PLATE = new ComplexPressurePlateBlock( COLLISION_CONDITION_HOSTILE, AbstractBlock.Settings.of(Material.STONE, MapColor.BLACK).requiresTool().noCollision().strength(0.5F) );
	public final static Block END_STONE_PRESSURE_PLATE = new ComplexPressurePlateBlock( COLLISION_CONDITION_VILLAGER, AbstractBlock.Settings.of(Material.STONE, MapColor.PALE_YELLOW).requiresTool().noCollision().strength(0.5F) );
	public final static Block BASALT_PRESSURE_PLATE = new ComplexPressurePlateBlock( COLLISION_CONDITION_PET, AbstractBlock.Settings.of(Material.STONE, MapColor.BLACK).requiresTool().noCollision().strength(0.5F) );

	// Other Components
	public final static Block REDSTONE_LAMP = new RedstoneLampBlock(FabricBlockSettings.of(Material.REDSTONE_LAMP).lightLevel((n) -> n.get(Properties.LIT) ? 1 : 0).postProcess((a, b, c) -> a.get(Properties.LIT)).emissiveLighting((a, b, c) -> a.get(Properties.LIT)).strength(0.3F).sounds(BlockSoundGroup.GLASS).allowsSpawning( (BlockState state, BlockView world, BlockPos pos, EntityType<?> type) -> true ) );
	public final static Block RGB_LAMP = new AnalogLampBlock(FabricBlockSettings.of(Material.REDSTONE_LAMP).lightLevel((n) -> n.get(AnalogLampBlock.POWER) > 0 ? 1 : 0).postProcess((a, b, c) -> a.get(AnalogLampBlock.POWER) > 0).emissiveLighting((a, b, c) -> a.get(AnalogLampBlock.POWER) > 0).strength(0.3F).sounds(BlockSoundGroup.GLASS).allowsSpawning( (BlockState state, BlockView world, BlockPos pos, EntityType<?> type) -> true ) );
	public final static Block REDSTONE_EMITTER = new EmitterBlock( AbstractBlock.Settings.of(Material.STONE).requiresTool().strength(3.5F).solidBlock( (BlockState state, BlockView world, BlockPos pos) -> true ) );
	public final static Block VISION_SENSOR = new VisionSensorBlock( AbstractBlock.Settings.of(Material.STONE).requiresTool().strength(3.5F).solidBlock( (BlockState state, BlockView world, BlockPos pos) -> true ) );
	public final static Block INVERTED_REDSTONE_TORCH = new InvertedRedstoneTorchBlock(AbstractBlock.Settings.of(Material.DECORATION).noCollision().breakInstantly().luminance( (n) -> n.get(Properties.LIT) ? 7 : 0 ).sounds(BlockSoundGroup.WOOD));
	public final static Block INVERTED_REDSTONE_WALL_TORCH = new WallInvertedRedstoneTorchBlock(AbstractBlock.Settings.of(Material.DECORATION).noCollision().breakInstantly().luminance( (n) -> n.get(Properties.LIT) ? 7 : 0 ).sounds(BlockSoundGroup.WOOD));

	// Statistics
	public static final Identifier INTERACT_WITH_SIGHT_SENSOR = new Identifier(NAMESPACE, "interact_with_sight_sensor");
	public static final Identifier INTERACT_WITH_REDSTONE_EMITTER = new Identifier(NAMESPACE, "interact_with_redstone_emitter");

	@Override
	public void onInitialize() {

		if(RedBits.CONFIG.other.inverted_redstone_torch) {
			registerBlock("inverted_redstone_torch", INVERTED_REDSTONE_TORCH);
			registerBlock("inverted_redstone_wall_torch", INVERTED_REDSTONE_WALL_TORCH);
			registerItem("inverted_redstone_torch", new WallStandingBlockItem(INVERTED_REDSTONE_TORCH, INVERTED_REDSTONE_WALL_TORCH, SETTINGS));
		}

		if(RedBits.CONFIG.other.redstone_emitter) {
			register("emitter", REDSTONE_EMITTER, true);
			registerStat(INTERACT_WITH_REDSTONE_EMITTER);
		}

		register("two_way_repeater", TWO_WAY_REPEATER, RedBits.CONFIG.gates.two_way_repeater);
		register("t_flip_flop", T_FLIP_FLOP, RedBits.CONFIG.gates.t_flip_flop);
		register("inverter", INVERTER, RedBits.CONFIG.gates.inverter);
		register("detector", DETECTOR, RedBits.CONFIG.gates.detector);
		register("latch", LATCH, RedBits.CONFIG.gates.latch);
		register("redstone_lamp", REDSTONE_LAMP, RedBits.CONFIG.other.shaded_redstone_lamp);
		register("oak_large_button", OAK_LARGE_BUTTON, RedBits.CONFIG.other.large_button);
		register("spruce_large_button", SPRUCE_LARGE_BUTTON, RedBits.CONFIG.other.large_button);
		register("birch_large_button", BIRCH_LARGE_BUTTON, RedBits.CONFIG.other.large_button);
		register("jungle_large_button", JUNGLE_LARGE_BUTTON, RedBits.CONFIG.other.large_button);
		register("acacia_large_button", ACACIA_LARGE_BUTTON, RedBits.CONFIG.other.large_button);
		register("dark_oak_large_button", DARK_OAK_LARGE_BUTTON, RedBits.CONFIG.other.large_button);
		register("crimson_large_button", CRIMSON_LARGE_BUTTON, RedBits.CONFIG.other.large_button);
		register("warped_large_button", WARPED_LARGE_BUTTON, RedBits.CONFIG.other.large_button);
		register("stone_large_button", STONE_LARGE_BUTTON, RedBits.CONFIG.other.large_button);
		register("polished_blackstone_large_button", POLISHED_BLACKSTONE_LARGE_BUTTON, RedBits.CONFIG.other.large_button);
		register("obsidian_pressure_plate", OBSIDIAN_PRESSURE_PLATE, RedBits.CONFIG.pressure_plates.obsidian);
		register("crying_obsidian_pressure_plate", CRYING_OBSIDIAN_PRESSURE_PLATE, RedBits.CONFIG.pressure_plates.crying_obsidian);
		register("end_stone_pressure_plate", END_STONE_PRESSURE_PLATE, RedBits.CONFIG.pressure_plates.end_stone);
		register("basalt_pressure_plate", BASALT_PRESSURE_PLATE, RedBits.CONFIG.pressure_plates.basalt);
		register("rgb_lamp", RGB_LAMP, RedBits.CONFIG.other.rgb_lamp);
		register("timer", TIMER, RedBits.CONFIG.gates.timer);

		if(RedBits.CONFIG.other.vision_sensor) {
			register("vision_sensor", VISION_SENSOR, true);
			registerStat(INTERACT_WITH_SIGHT_SENSOR);
			VisionSensorNetwork.init();
		}

	}

	@Override
	public void onInitializeClient() {
		cutout(INVERTER, T_FLIP_FLOP, DETECTOR, LATCH, TWO_WAY_REPEATER, INVERTED_REDSTONE_TORCH, INVERTED_REDSTONE_WALL_TORCH, TIMER);
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

	private void register( String name, Block block, boolean flag ) {
		if( flag ) {
			registerBlock(name, block);
			registerItem(name, new BlockItem(block, SETTINGS));
		}else{
			LOGGER.info( "Registration of: '" + name + "' skipped!" );
		}
	}

	private void registerStat( Identifier id ) {
		Registry.register(Registry.CUSTOM_STAT, id, id);
		Stats.CUSTOM.getOrCreateStat(id, StatFormatter.DEFAULT);
	}

	@Environment(EnvType.CLIENT)
	private void cutout( Block... blocks ) {
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), blocks);
	}

}
