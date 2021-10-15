package com.yyon.grapplinghook;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yyon.grapplinghook.blocks.BlockGrappleModifier;
import com.yyon.grapplinghook.blocks.TileEntityGrappleModifier;
import com.yyon.grapplinghook.controllers.grappleController;
import com.yyon.grapplinghook.enchantments.DoublejumpEnchantment;
import com.yyon.grapplinghook.enchantments.SlidingEnchantment;
import com.yyon.grapplinghook.enchantments.WallrunEnchantment;
import com.yyon.grapplinghook.entities.grappleArrow;
import com.yyon.grapplinghook.items.KeypressItem;
import com.yyon.grapplinghook.items.LongFallBoots;
import com.yyon.grapplinghook.items.grappleBow;
import com.yyon.grapplinghook.items.launcherItem;
import com.yyon.grapplinghook.items.repeller;
import com.yyon.grapplinghook.items.upgrades.BaseUpgradeItem;
import com.yyon.grapplinghook.items.upgrades.DoubleUpgradeItem;
import com.yyon.grapplinghook.items.upgrades.ForcefieldUpgradeItem;
import com.yyon.grapplinghook.items.upgrades.LimitsUpgradeItem;
import com.yyon.grapplinghook.items.upgrades.MagnetUpgradeItem;
import com.yyon.grapplinghook.items.upgrades.MotorUpgradeItem;
import com.yyon.grapplinghook.items.upgrades.RocketUpgradeItem;
import com.yyon.grapplinghook.items.upgrades.RopeUpgradeItem;
import com.yyon.grapplinghook.items.upgrades.StaffUpgradeItem;
import com.yyon.grapplinghook.items.upgrades.SwingUpgradeItem;
import com.yyon.grapplinghook.items.upgrades.ThrowUpgradeItem;
import com.yyon.grapplinghook.network.DetachSingleHookMessage;
import com.yyon.grapplinghook.network.GrappleAttachMessage;
import com.yyon.grapplinghook.network.GrappleAttachPosMessage;
import com.yyon.grapplinghook.network.GrappleDetachMessage;
import com.yyon.grapplinghook.network.GrappleEndMessage;
import com.yyon.grapplinghook.network.GrappleModifierMessage;
import com.yyon.grapplinghook.network.KeypressMessage;
import com.yyon.grapplinghook.network.LoggedInMessage;
import com.yyon.grapplinghook.network.PlayerMovementMessage;
import com.yyon.grapplinghook.network.SegmentMessage;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantment.Rarity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

/*
 * This file is part of GrappleMod.

    GrappleMod is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GrappleMod is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GrappleMod.  If not, see <http://www.gnu.org/licenses/>.
 */

//TODO
// Pull mobs
// Attach 2 things together
// wallrun on diagonal walls
// smart motor acts erratically when aiming above hook
// key events

@Mod(grapplemod.MODID)
@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class grapplemod {
    public static final String MODID = "grapplemod";
    
    public static final String VERSION = "1.16.5-v12";

    public static final Logger LOGGER = LogManager.getLogger();

    public static grappleBow grapplebowitem;
    public static launcherItem launcheritem;
    public static repeller repelleritem;

    public static BaseUpgradeItem baseupgradeitem;
    public static DoubleUpgradeItem doubleupgradeitem;
    public static ForcefieldUpgradeItem forcefieldupgradeitem;
    public static MagnetUpgradeItem magnetupgradeitem;
    public static MotorUpgradeItem motorupgradeitem;
    public static RopeUpgradeItem ropeupgradeitem;
    public static StaffUpgradeItem staffupgradeitem;
    public static SwingUpgradeItem swingupgradeitem;
    public static ThrowUpgradeItem throwupgradeitem;
    public static LimitsUpgradeItem limitsupgradeitem;
    public static RocketUpgradeItem rocketupgradeitem;

    public static Item longfallboots;
    
    public static WallrunEnchantment wallrunenchantment;
    public static DoublejumpEnchantment doublejumpenchantment;
    public static SlidingEnchantment slidingenchantment;

	public static SimpleChannel network;    // used to transmit your network messages
	public static final ResourceLocation simpleChannelRL = new ResourceLocation("grapplemod", "channel");

	public static HashMap<Integer, grappleController> controllers = new HashMap<Integer, grappleController>(); // client side
	public static HashMap<BlockPos, grappleController> controllerpos = new HashMap<BlockPos, grappleController>();
	public static HashSet<Integer> attached = new HashSet<Integer>(); // server side	
	public static HashMap<Integer, HashSet<grappleArrow>> allarrows = new HashMap<Integer, HashSet<grappleArrow>>(); // server side
	
	private static int controllerid = 0;
	public static int GRAPPLEID = controllerid++;
	public static int REPELID = controllerid++;
	public static int AIRID = controllerid++;
		
	private static boolean anyblocks = true;
	private static HashSet<Block> grapplingblocks;
	private static boolean removeblocks = false;
	private static HashSet<Block> grapplingbreaksblocks;
	private static boolean anybreakblocks = false;
	
	public static Block blockGrappleModifier;
	public static BlockItem itemBlockGrappleModifier;
	
	public ResourceLocation resourceLocation;
	
	public enum upgradeCategories {
		ROPE ("Rope"), 
		THROW ("Hook Thrower"), 
		MOTOR ("Motor"), 
		SWING ("Swing Speed"), 
		STAFF ("Ender Staff"), 
		FORCEFIELD ("Forcefield"), 
		MAGNET ("Hook Magnet"), 
		DOUBLE ("Double Hook"),
		LIMITS ("Limits"),
		ROCKET ("Rocket");
		
		public String description;
		private upgradeCategories(String desc) {
			this.description = desc;
		}
		
		public static upgradeCategories fromInt(int i) {
			return upgradeCategories.values()[i];
		}
		public int toInt() {
			for (int i = 0; i < size(); i++) {
				if (upgradeCategories.values()[i] == this) {
					return i;
				}
			}
			return -1;
		}
		public static int size() {
			return upgradeCategories.values().length;
		}
		public Item getItem() {
			if (this == upgradeCategories.ROPE) {
				return ropeupgradeitem;
			} else if (this == upgradeCategories.THROW) {
				return throwupgradeitem;
			} else if (this == upgradeCategories.MOTOR) {
				return motorupgradeitem;
			} else if (this == upgradeCategories.SWING) {
				return swingupgradeitem;
			} else if (this == upgradeCategories.STAFF) {
				return staffupgradeitem;
			} else if (this == upgradeCategories.FORCEFIELD) {
				return forcefieldupgradeitem;
			} else if (this == upgradeCategories.MAGNET) {
				return magnetupgradeitem;
			} else if (this == upgradeCategories.DOUBLE) {
				return doubleupgradeitem;
			} else if (this == upgradeCategories.LIMITS) {
				return limitsupgradeitem;
			} else if (this == upgradeCategories.ROCKET) {
				return rocketupgradeitem;
			}
			return null;
		}
	};
	
	public static final ItemGroup tabGrapplemod = new ItemGroup("grapplemod") {
	      @OnlyIn(Dist.CLIENT)
	      public ItemStack makeIcon() {
	         return new ItemStack(grapplebowitem);
	      }
	};

	public enum keys {
		keyBindUseItem,
		keyBindForward,
		keyBindLeft,
		keyBindBack,
		keyBindRight,
		keyBindJump,
		keyBindSneak,
		keyBindAttack
	}
	
	public static EventHandlers eventHandlers = new EventHandlers();;

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		network = NetworkRegistry.newSimpleChannel(simpleChannelRL, () -> "1.0",
	            version -> true,
	            version -> true);
		int id = 0;
		network.registerMessage(id++, PlayerMovementMessage.class, PlayerMovementMessage::encode, PlayerMovementMessage::new, PlayerMovementMessage::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		network.registerMessage(id++, GrappleEndMessage.class, GrappleEndMessage::encode, GrappleEndMessage::new, GrappleEndMessage::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		network.registerMessage(id++, GrappleModifierMessage.class, GrappleModifierMessage::encode, GrappleModifierMessage::new, GrappleModifierMessage::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		network.registerMessage(id++, KeypressMessage.class, KeypressMessage::encode, KeypressMessage::new, KeypressMessage::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		network.registerMessage(id++, GrappleAttachMessage.class, GrappleAttachMessage::encode, GrappleAttachMessage::new, GrappleAttachMessage::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		network.registerMessage(id++, GrappleDetachMessage.class, GrappleDetachMessage::encode, GrappleDetachMessage::new, GrappleDetachMessage::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		network.registerMessage(id++, DetachSingleHookMessage.class, DetachSingleHookMessage::encode, DetachSingleHookMessage::new, DetachSingleHookMessage::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		network.registerMessage(id++, GrappleAttachPosMessage.class, GrappleAttachPosMessage::encode, GrappleAttachPosMessage::new, GrappleAttachPosMessage::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		network.registerMessage(id++, SegmentMessage.class, SegmentMessage::encode, SegmentMessage::new, SegmentMessage::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		network.registerMessage(id++, LoggedInMessage.class, LoggedInMessage::encode, LoggedInMessage::new, LoggedInMessage::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
	}
	
	@SubscribeEvent
	public static void onBlocksRegistration(final RegistryEvent.Register<Block> blockRegisterEvent) {
		blockGrappleModifier = (BlockGrappleModifier)(new BlockGrappleModifier().setRegistryName("grapplemod", "block_grapple_modifier"));
		blockRegisterEvent.getRegistry().register(blockGrappleModifier);
	}

	public static TileEntityType<TileEntityGrappleModifier> tileEntityGrappleModifierType;

	public static CommonProxyClass proxy = DistExecutor.unsafeRunForDist(() -> ClientProxyClass::new, () -> () -> null);
	
	@SubscribeEvent
	public static void onTileEntityTypeRegistration(final RegistryEvent.Register<TileEntityType<?>> event) {
		tileEntityGrappleModifierType =
				TileEntityType.Builder.of(TileEntityGrappleModifier::new, blockGrappleModifier).build(null);  // you probably don't need a datafixer --> null should be fine
		tileEntityGrappleModifierType.setRegistryName("grapplemod:block_grapple_modifier");
		event.getRegistry().register(tileEntityGrappleModifierType);
	}

	public static EntityType<grappleArrow> grappleArrowType;
	
	@SubscribeEvent
	public static void onEntityTypeRegistration(RegistryEvent.Register<EntityType<?>> entityTypeRegisterEvent) {
		grappleArrowType = EntityType.Builder.<grappleArrow>of(grappleArrow::new, EntityClassification.MISC)
	            .sized(0.25F, 0.25F)
	            .build("grapplemod:grapplearrow");
		grappleArrowType.setRegistryName("grapplemod:grapplearrow");
	    entityTypeRegisterEvent.getRegistry().register(grappleArrowType);
	}
	
	public static HashSet<Block> stringToBlocks(String s) {
		HashSet<Block> blocks = new HashSet<Block>();
		
		if (s.equals("") || s.equals("none") || s.equals("any")) {
			return blocks;
		}
		
		String[] blockstr = s.split(",");
		
	    for(String str:blockstr){
	    	str = str.trim();
	    	String modid;
	    	String name;
	    	if (str.contains(":")) {
	    		String[] splitstr = str.split(":");
	    		modid = splitstr[0];
	    		name = splitstr[1];
	    	} else {
	    		modid = "minecraft";
	    		name = str;
	    	}
	    	
	    	Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(modid, name));
	    	
	    	blocks.add(b);
	    }
	    
	    return blocks;
	}
	
	public static void updateGrapplingBlocks() {
		String s = GrappleConfig.getconf().grapplinghook.blocks.grapplingBlocks;
		if (s.equals("any") || s.equals("")) {
			s = GrappleConfig.getconf().grapplinghook.blocks.grapplingNonBlocks;
			if (s.equals("none") || s.equals("")) {
				anyblocks = true;
			} else {
				anyblocks = false;
				removeblocks = true;
			}
		} else {
			anyblocks = false;
			removeblocks = false;
		}
	
		if (!anyblocks) {
			grapplingblocks = stringToBlocks(s);
		}
		
		grapplingbreaksblocks = stringToBlocks(GrappleConfig.getconf().grapplinghook.blocks.grappleBreakBlocks);
		anybreakblocks = grapplingbreaksblocks.size() != 0;
		
	}

	private static String prevGrapplingBlocks = null;
	private static String prevGrapplingNonBlocks = null;
	public static boolean attachesblock(Block block) {
		if (!GrappleConfig.getconf().grapplinghook.blocks.grapplingBlocks.equals(prevGrapplingBlocks) || !GrappleConfig.getconf().grapplinghook.blocks.grapplingNonBlocks.equals(prevGrapplingNonBlocks)) {
			updateGrapplingBlocks();
		}
		
		if (anyblocks) {
			return true;
		}
		
		boolean inlist = grapplingblocks.contains(block);
		
		if (removeblocks) {
			return !inlist;
		} else {
			return inlist;
		}
	}

	private static String prevGrapplingBreakBlocks = null;
	public static boolean breaksblock(Block block) {
		if (!GrappleConfig.getconf().grapplinghook.blocks.grappleBreakBlocks.equals(prevGrapplingBreakBlocks)) {
			updateGrapplingBlocks();
		}
		
		if (!anybreakblocks) {
			return false;
		}
		
		return grapplingbreaksblocks.contains(block);
	}
	public static Item[] getAllItems() {
		return new Item[] {
				grapplebowitem,
				launcheritem, 
				repelleritem, 
				longfallboots, 
				baseupgradeitem, 
				ropeupgradeitem, 
				throwupgradeitem, 
				motorupgradeitem, 
				swingupgradeitem, 
				staffupgradeitem, 
				forcefieldupgradeitem, 
				magnetupgradeitem, 
				doubleupgradeitem, 
				rocketupgradeitem, 
				limitsupgradeitem, 
				};
	}
	
	@SubscribeEvent
	public static void registerEnchantments(final RegistryEvent.Register<Enchantment> event) {
	    wallrunenchantment = new WallrunEnchantment();
	    wallrunenchantment.setRegistryName("wallrunenchantment");
	    doublejumpenchantment = new DoublejumpEnchantment();
	    doublejumpenchantment.setRegistryName("doublejumpenchantment");
	    slidingenchantment = new SlidingEnchantment();
	    slidingenchantment.setRegistryName("slidingenchantment");
	    
	    event.getRegistry().registerAll(wallrunenchantment, doublejumpenchantment, slidingenchantment);

	}

	public static void registerItem(Item item, String itemName, final RegistryEvent.Register<Item> itemRegisterEvent) {
		item.setRegistryName(itemName);
		itemRegisterEvent.getRegistry().register(item);
	}
	
	@SubscribeEvent
	public static void onItemsRegistration(final RegistryEvent.Register<Item> itemRegisterEvent) {
		grapplebowitem = new grappleBow();
		registerItem(grapplebowitem, "grapplinghook", itemRegisterEvent);

		launcheritem = new launcherItem();
		registerItem(launcheritem, "launcheritem", itemRegisterEvent);
		longfallboots = new LongFallBoots(ArmorMaterial.DIAMOND, 3);
		registerItem(longfallboots, "longfallboots", itemRegisterEvent);
		repelleritem = new repeller();
		registerItem(repelleritem, "repeller", itemRegisterEvent);
	    baseupgradeitem = new BaseUpgradeItem();
		registerItem(baseupgradeitem, "baseupgradeitem", itemRegisterEvent);
	    doubleupgradeitem = new DoubleUpgradeItem();
		registerItem(doubleupgradeitem, "doubleupgradeitem", itemRegisterEvent);
	    forcefieldupgradeitem = new ForcefieldUpgradeItem();
		registerItem(forcefieldupgradeitem, "forcefieldupgradeitem", itemRegisterEvent);
	    magnetupgradeitem = new MagnetUpgradeItem();
		registerItem(magnetupgradeitem, "magnetupgradeitem", itemRegisterEvent);
	    motorupgradeitem = new MotorUpgradeItem();
		registerItem(motorupgradeitem, "motorupgradeitem", itemRegisterEvent);
	    ropeupgradeitem = new RopeUpgradeItem();
		registerItem(ropeupgradeitem, "ropeupgradeitem", itemRegisterEvent);
	    staffupgradeitem = new StaffUpgradeItem();
		registerItem(staffupgradeitem, "staffupgradeitem", itemRegisterEvent);
	    swingupgradeitem = new SwingUpgradeItem();
		registerItem(swingupgradeitem, "swingupgradeitem", itemRegisterEvent);
	    throwupgradeitem = new ThrowUpgradeItem();
		registerItem(throwupgradeitem, "throwupgradeitem", itemRegisterEvent);
	    limitsupgradeitem = new LimitsUpgradeItem();
		registerItem(limitsupgradeitem, "limitsupgradeitem", itemRegisterEvent);
	    rocketupgradeitem = new RocketUpgradeItem();
		registerItem(rocketupgradeitem, "rocketupgradeitem", itemRegisterEvent);

		// We need to create a BlockItem so the player can carry this block in their hand and it can appear in the inventory
		Item.Properties itemSimpleProperties = new Item.Properties()
				.stacksTo(64)
				.tab(grapplemod.tabGrapplemod);  // which inventory tab?
		itemBlockGrappleModifier = new BlockItem(blockGrappleModifier, itemSimpleProperties);
		itemBlockGrappleModifier.setRegistryName(blockGrappleModifier.getRegistryName());
		itemRegisterEvent.getRegistry().register(itemBlockGrappleModifier);
	}
	
	public static void addarrow(int id, grappleArrow arrow) {
		if (!allarrows.containsKey(id)) {
			allarrows.put(id, new HashSet<grappleArrow>());
		}
		allarrows.get(id).add(arrow);
	}
	
	public static void removeallmultihookarrows(int id) {
		if (!allarrows.containsKey(id)) {
			allarrows.put(id, new HashSet<grappleArrow>());
		}
		for (grappleArrow arrow : allarrows.get(id)) {
			if (arrow != null && arrow.isAlive()) {
				arrow.removeServer();
			}
		}
		allarrows.put(id, new HashSet<grappleArrow>());
	}
	
	public static void registerController(int entityId, grappleController controller) {
		if (controllers.containsKey(entityId)) {
			controllers.get(entityId).unattach();
		}
		
		controllers.put(entityId, controller);
	}
	
	public static void unregisterController(int entityId) {
		controllers.remove(entityId);
	}

	public static void receiveGrappleDetach(int id) {
		grappleController controller = controllers.get(id);
		if (controller != null) {
			controller.receiveGrappleDetach();
		}
	}
	
	public static void receiveGrappleDetachHook(int id, int hookid) {
		grappleController controller = controllers.get(id);
		if (controller != null) {
			controller.receiveGrappleDetachHook(hookid);
		}
	}

	public static void receiveEnderLaunch(int id, double x, double y, double z) {
		grappleController controller = controllers.get(id);
		if (controller != null) {
			controller.receiveEnderLaunch(x, y, z);
		} else {
			System.out.println("Couldn't find controller");
		}
	}
	
	public static void sendtocorrectclient(Object message, int playerid, World w) {
		Entity entity = w.getEntity(playerid);
		if (entity instanceof ServerPlayerEntity) {
			grapplemod.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) entity), message);
		} else {
			System.out.println("ERROR! couldn't find player");
		}
	}

	public static void receiveGrappleEnd(int id, World world, HashSet<Integer> arrowIds) {
		if (grapplemod.attached.contains(id)) {
			grapplemod.attached.remove(id);
		} else {
		}
		
		for (int arrowid : arrowIds) {
	      	Entity grapple = world.getEntity(arrowid);
	  		if (grapple instanceof grappleArrow) {
	  			((grappleArrow) grapple).removeServer();
	  		} else {
	
	  		}
		}
  		
  		Entity entity = world.getEntity(id);
  		if (entity != null) {
      		entity.fallDistance = 0;
  		}
  		
  		grapplemod.removeallmultihookarrows(id);
	}

	public static void receiveKeypress(PlayerEntity player, KeypressItem.Keys key, boolean isDown) {
		if (player != null) {
			ItemStack stack = player.getItemInHand(Hand.MAIN_HAND);
			if (stack != null) {
				Item item = stack.getItem();
				if (item instanceof KeypressItem) {
					if (isDown) {
						((KeypressItem)item).onCustomKeyDown(stack, player, key, true);
					} else {
						((KeypressItem)item).onCustomKeyUp(stack, player, key, true);
					}
					return;
				}
			}

			stack = player.getItemInHand(Hand.OFF_HAND);
			if (stack != null) {
				Item item = stack.getItem();
				if (item instanceof KeypressItem) {
					if (isDown) {
						((KeypressItem)item).onCustomKeyDown(stack, player, key, false);
					} else {
						((KeypressItem)item).onCustomKeyUp(stack, player, key, false);
					}
					return;
				}
			}
		}
	}

	public static Rarity getRarityFromInt(int rarity_int) {
		Rarity[] rarities = (new Rarity[] {Rarity.VERY_RARE, Rarity.RARE, Rarity.UNCOMMON, Rarity.COMMON});
		if (rarity_int < 0) {rarity_int = 0;}
		if (rarity_int >= rarities.length) {rarity_int = rarities.length-1;}
		return rarities[rarity_int];
	}
	
	public static BlockRayTraceResult rayTraceBlocks(World world, vec from, vec to) {
		RayTraceResult result = world.clip(new RayTraceContext(from.toVec3d(), to.toVec3d(), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null));
		if (result != null && result instanceof BlockRayTraceResult) {
			BlockRayTraceResult blockhit = (BlockRayTraceResult) result;
			if (blockhit.getType() != RayTraceResult.Type.BLOCK) {
				return null;
			}
			return blockhit;
		}
		return null;
	}
	
	public static long getTime(World w) {
		return w.getGameTime();
	}
}
