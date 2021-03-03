package tconstruct.armor.player;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import cpw.mods.fml.relauncher.Side;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import mantle.player.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.*;
import net.minecraft.entity.Entity.EnumEntitySize;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import tconstruct.TConstruct;
import tconstruct.library.tools.AbilityHelper;
import tconstruct.tools.TinkerTools;
import tconstruct.util.config.PHConstruct;

//TODO: Redesign this class
public class TPlayerHandler
{
    /* Player */
    // public int hunger;

    private ConcurrentHashMap<UUID, TPlayerStats> playerStats = new ConcurrentHashMap<UUID, TPlayerStats>();

    @SubscribeEvent
    public void PlayerLoggedInEvent (PlayerLoggedInEvent event)
    {
        onPlayerLogin(event.player);
    }

    @SubscribeEvent
    public void onPlayerRespawn (PlayerRespawnEvent event)
    {
        onPlayerRespawn(event.player);
    }

    @SubscribeEvent
    public void onEntityConstructing (EntityEvent.EntityConstructing event)
    {
        if (event.entity instanceof EntityPlayer && TPlayerStats.get((EntityPlayer) event.entity) == null)
        {
            TPlayerStats.register((EntityPlayer) event.entity);
        }
    }

    public void onPlayerLogin (EntityPlayer player)
    {
        // Lookup player
        TPlayerStats stats = TPlayerStats.get(player);

        stats.level = player.experienceLevel;
        stats.hunger = player.getFoodStats().getFoodLevel();

        if (!stats.beginnerManual)
        {
            stats.beginnerManual = true;
            if (PHConstruct.beginnerBook)
            {
                ItemStack diary = new ItemStack(TinkerTools.manualBook);
                if (!player.inventory.addItemStackToInventory(diary))
                {
                    AbilityHelper.spawnItemAtPlayer(player, diary);
                }
            }

        }

    }

    public void onPlayerRespawn (EntityPlayer entityplayer)
    {
        // Boom!
        TPlayerStats playerData = playerStats.remove(entityplayer.getPersistentID());
        TPlayerStats stats = TPlayerStats.get(entityplayer);
        if (playerData != null)
        {
            stats.copyFrom(playerData, false);
            stats.level = playerData.level;
            stats.hunger = playerData.hunger;
        }

        stats.init(entityplayer, entityplayer.worldObj);
        stats.armor.recalculateHealth(entityplayer, stats);

        /*
         * TFoodStats food = new TFoodStats(); entityplayer.foodStats = food;
         */

        if (PHConstruct.keepLevels)
            entityplayer.experienceLevel = stats.level;

        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.CLIENT)
        {
            // TProxyClient.controlInstance.resetControls();
        }
    }

    @SubscribeEvent
    public void playerDeath (LivingDeathEvent event)
    {
        if(!(event.entity instanceof EntityPlayer))
            return;

        if (!event.entity.worldObj.isRemote)
        {
            TPlayerStats properties = (TPlayerStats) event.entity.getExtendedProperties(TPlayerStats.PROP_NAME);
            properties.hunger = ((EntityPlayer) event.entity).getFoodStats().getFoodLevel();
            playerStats.put(((EntityPlayer) event.entity).getPersistentID(), properties);
        }
    }

    @SubscribeEvent
    public void playerDrops (PlayerDropsEvent evt)
    {
        // After playerDeath event. Modifying saved data.
        TPlayerStats stats = playerStats.get(evt.entityPlayer.getPersistentID());

        stats.level = evt.entityPlayer.experienceLevel / 2;
        // stats.health = 20;
        int hunger = evt.entityPlayer.getFoodStats().getFoodLevel();
        if (hunger < 6)
            stats.hunger = 6;
        else
            stats.hunger = evt.entityPlayer.getFoodStats().getFoodLevel();

        if (evt.entityPlayer.capturedDrops != evt.drops)
        {
            evt.entityPlayer.capturedDrops.clear();
        }

        evt.entityPlayer.captureDrops = true;
        stats.armor.dropItems();
        stats.knapsack.dropItems();
        evt.entityPlayer.captureDrops = false;

        if (evt.entityPlayer.capturedDrops != evt.drops)
        {
            evt.drops.addAll(evt.entityPlayer.capturedDrops);
        }

        playerStats.put(evt.entityPlayer.getPersistentID(), stats);
    }

}
