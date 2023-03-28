package com.wynntils.modules.wynnhanni;

import com.wynntils.core.events.custom.ChatEvent;
import com.wynntils.core.framework.interfaces.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WynnHanniEvents implements Listener {
    private Pattern patternCombatExp = Pattern.compile("§7\\[§f\\+§f.*§f Combat XP§7]");
    private Pattern patternDamageSplash = Pattern.compile("((§.)+-\\d+ . )+");
    private Pattern nonPvPPattern = Pattern.compile("§[ac].§f .§7 (Woodcutting|Mining|Farming|Fishing) Lv\\. Min: §f.*");
    private Pattern gatheringPostPattern = Pattern.compile("§2§aGathering Post §2\\[(Mining|Woodcutting|Farming) Lv\\. .*]");
    private Pattern slayingPostPattern = Pattern.compile("§2§aSlaying Post §2\\[Combat Lv\\. .*]");
    private Pattern guildAreaPattern = Pattern.compile("§7Controlled by §b§l.*§r§7 \\[Lv\\. .*]");
    private Pattern compactDamageIndicatorPattern = Pattern.compile("§4\\[(.*)§4]");

    private List<EntityArmorStand> posts = new ArrayList<>();
    private List<EntityArmorStand> guildAreas = new ArrayList<>();
    private List<EntityArmorStand> craftingStations = new ArrayList<>();
    private Map<Entity, Integer> mobHealth = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChatToRedirect(ChatEvent.Pre event) {
        String text = event.getMessage().getFormattedText();

        Pattern reachLevel = Pattern.compile("§8\\[§r§7!§r§8] §r§7Congratulations to §r§f.*§r§7 for reaching .* §r§flevel .*§r§7!§r");

        Matcher matcher = reachLevel.matcher(text);
        if (matcher.matches()) {
            event.setCanceled(true);
            return;
        }

        //§6[Event] §r§c§lFestival of the Heroes: §r§5Watch a live performance in Detlas to gain temporary buffs!
        // §r§5The festival closes its doors in §r§c19 days.§r
        if (text.startsWith("§6[Event] §r§c§lFestival of the Heroes: §r§5Watch a live performance in Detlas")) {
            event.setCanceled(true);
            return;
        }

        //§6[Event] §r§c§lFestival of the Heroes: §r§5For a limited-time, use the Event Airship in Detlas to play:
        // §r§dThe Claim to Heroism§r§5! §r§5The festival closes its doors in §r§c19 days.§r'
        //[03:33:50] [Client thread/INFO] [chat]: [CHAT] [Event] Festival of the Heroes: For a limited-time,
        // use the Event Airship in Detlas to play: The Claim to Heroism! The festival closes its doors in 19 days.

        //§6[Event] §r§c§lFestival of the Heroes: §r§5Grab 3 free Heroes Crates on our store at §r§dwynncraft.com/store§r§d !
        // §r§5The festival closes its doors in §r§c19 days.§r
        if (text.startsWith("§6[Event] §r§c§lFestival of the Heroes: ")) {
            event.setCanceled(true);
            return;
        }

        if (text.equals("§6Thank you for using the WynnPack. Enjoy the game!§r")) {
            event.setCanceled(true);
            return;
        }

        //§3JohnBleu [WC1] shouts: §r§bskill issue§r
        Pattern pattern = Pattern.compile("§3.* \\[W.*] shouts: §r§b.*§r");
        if (pattern.matcher(text).matches()) {
            event.setCanceled(true);
            return;
        }

        System.out.println("chat: '" + text + "'");
    }

    @SubscribeEvent
    public void processPacketQueue(TickEvent.ClientTickEvent e) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) return;
        WorldClient world = minecraft.world;
        if (world == null) return;
        List<Entity> loadedEntityList = world.loadedEntityList;
        if (loadedEntityList == null) return;

        EntityPlayerSP player = minecraft.player;
        if (player == null) return;

        String playerName = player.getName();
        String playerCombatExpName = "§7[" + playerName + "]";

        for (Entity entity : loadedEntityList) {
            if (WynnHanniConfig.COMPACT_DAMAGE_INDICATOR) {
                damageIndicator(entity);
            }

            if (WynnHanniConfig.HIDE_NEUTRAL_NPCS) {
                hideNeutralNpcs(entity);
            }
            hideFestivalOfHeroes(entity);

            if (entity instanceof EntityArmorStand) {
                EntityArmorStand armorStand = (EntityArmorStand) entity;
                String name = armorStand.getName();
                if (WynnHanniConfig.HIDE_EXP_SPLASH) {
                    if (patternCombatExp.matcher(name).matches()) {
                        armorStand.setCustomNameTag(" ");
                    }
                    if (name.equals(playerCombatExpName)) {
                        armorStand.setCustomNameTag(" ");
                    }
                }
                if (WynnHanniConfig.HIDE_DAMAGE_SPLASH) {
                    if (patternDamageSplash.matcher(name).matches()) {
                        armorStand.setCustomNameTag(" ");
                    }
                }
                if (WynnHanniConfig.HIDE_GATHERING_SPOTS) {
                    hideGatherings(armorStand, name);
                }
                if (WynnHanniConfig.HIDE_GATHERING_POSTS) {
                    hideGatheringPost(armorStand, name);
                }
                if (WynnHanniConfig.HIDE_SLAYING_POSTS) {
                    hideSlayingPost(armorStand, name);
                }
                if (WynnHanniConfig.HIDE_GUILD_SPOTS) {
                    hideAreaPost(armorStand, name);
                }
                if (hideArmorStandsAround(armorStand)) {
                    world.removeEntity(armorStand);
                }

                if (WynnHanniConfig.HIDE_CRAFTING_STATION) {
                    if (armorStand.getName().equals("§7§7Crafting Station")) {
                        craftingStations.add(armorStand);
                    }
                    if (hideCraftingStation(entity)) {
                        world.removeEntity(entity);
                    }
                }
            }
            if (WynnHanniConfig.HIDE_ARROWS) {
                if (entity instanceof EntityArrow) {
                    if (entity.getDistance(player) < 5) {
                        world.removeEntity(entity);
                    }
                }
            }


            if (WynnHanniConfig.HIDE_DYING_MOBS) {
                if (entity instanceof EntityLivingBase) {
                    if (((EntityLivingBase) entity).getHealth() == 0) {
                        float distance = entity.getDistance(player);
                        if (distance < 40) {
                            world.removeEntity(entity);
                        }
                    }
                }
            }
        }
    }

    private boolean hideCraftingStation(Entity entity) {
        for (EntityArmorStand station : craftingStations) {
            if (station.posX == entity.posX) {
                if (station.posZ == entity.posZ) {
                    return true;
                }
            }
        }
        return false;
    }

    private void hideFestivalOfHeroes(Entity entity) {

    }

    private void hideNeutralNpcs(Entity entity) {
        if (isDetlasNpc(entity.getName())) {
            entity.setCustomNameTag(" ");
        }
        if (isAlmujNpc(entity.getName())) {
            entity.setCustomNameTag(" ");
        }
    }

    private boolean isAlmujNpc(String name) {
        if (name.equals("§aAlmuj Citizen§6 [Lv. 30]")) return true;
        if (name.equals("§bAlmuj Soldier§6 [Lv. 40]")) return true;
        if (name.equals("§aHungry Almuj Citizen§6 [Lv. 20]")) return true;
        if (name.equals("§bAlmuj Slums Watch§6 [Lv. 30]")) return true;

        return false;
    }

    private boolean isDetlasNpc(String name) {
        if (name.equals("§aDetlas Citizen§6 [Lv. 10]")) return true;
        if (name.equals("§bDetlas Soldier§6 [Lv. 20]")) return true;
        if (name.equals("§bDetlas Guard§6 [Lv. 20]")) return true;
        if (name.equals("§bBarrack Guard§6 [Lv. 30]")) return true;
        if (name.equals("§aTravelling Merchant§6 [Lv. 10]")) return true;
        if (name.equals("§bElite Soldier§6 [Lv. 100]")) return true;

        return false;
    }

    private Boolean hideArmorStandsAround(EntityArmorStand entity) {
        for (EntityArmorStand stand : posts) {
            if (stand.getDistance(entity) < 3.5) {
                return true;
            }
        }
        for (EntityArmorStand stand : guildAreas) {
            if (stand.getDistance(entity) < 4.5) {
                return true;
            }
        }

        return false;
    }

    private void hideGatheringPost(EntityArmorStand entity, String name) {
        Matcher matcher = gatheringPostPattern.matcher(name);
        if (matcher.matches()) {
            posts.add(entity);
        }
    }

    private void hideSlayingPost(EntityArmorStand entity, String name) {
        Matcher matcher = slayingPostPattern.matcher(name);
        if (matcher.matches()) {
            posts.add(entity);
        }
    }

    private void hideAreaPost(EntityArmorStand entity, String name) {
        Matcher matcher = guildAreaPattern.matcher(name);
        if (matcher.matches()) {
            guildAreas.add(entity);
        }
    }

    private void damageIndicator(Entity entity) {
        String customNameTag = entity.getCustomNameTag();
        Matcher matcher = compactDamageIndicatorPattern.matcher(customNameTag);
        if (matcher.matches()) {
            String text = matcher.group(1);
            String cleanText = removeColor(text);
            String fullyClean = cleanText.replace("|", "");
            int health = Integer.parseInt(fullyClean);
            String result = "§l" + fullyClean;
            if (mobHealth.containsKey(entity)) {
                Integer oldHealth = mobHealth.get(entity);
                int diff = health - oldHealth;
                if (diff != 0) {
                    if (diff > 0) {
                        result += " §7(§a+" + diff + "§7)";
                    } else {
                        diff *= -1;
                        result += " §7(§c-" + diff + "§7)";
                    }
                }
            }
            mobHealth.put(entity, health);
            entity.setCustomNameTag(result);
        }
    }

    private String removeColor(String text) {
        StringBuilder builder = new StringBuilder();
        boolean skipNext = false;
        char number = "§".toCharArray()[0];
        for (char c : text.toCharArray()) {
            if (c == number) {
                skipNext = true;
                continue;
            }
            if (skipNext) {
                skipNext = false;
                continue;
            }
            builder.append(c);
        }

        return builder.toString();
    }

    private void hideGatherings(Entity entity, String name) {
        if (nonPvPPattern.matcher(name).matches()) {
            System.out.println("hide gathering: '" + name + "'");
            entity.setCustomNameTag(" ");
        }

        if (name.equals("§8Left-Click for Wood") ||
                name.equals("§8Right-Click for Paper") ||
                name.equals("§aOak") || // lvl 5
                name.equals("§aWillow") || // lvl 20
                name.equals("§aAcacia") || // lvl 30
                name.equals("§aSpruce") || // lvl 40
                name.equals("§aBirch")) { // lvl 10
            System.out.println("hide Woodcutting: '" + name + "'");
            entity.setCustomNameTag(" ");
        }
        if (name.equals("§8Left-Click for Ingot") ||
                name.equals("§8Right-Click for Gem") ||
                name.equals("§aCopper") || // lvl 5
                name.equals("§aGold") || // lvl 20
                name.equals("§aSandstone") || // lvl 30
                name.equals("§aIron") || // lvl 40
                name.equals("§aGranite")) { // lvl 10
            System.out.println("hide Mining: '" + name + "'");
            entity.setCustomNameTag(" ");
        }
        if (name.equals("§8Left-Click for String") ||
                name.equals("§8Right-Click for Grains") ||
                name.equals("§aBarley") || // lvl 10
                name.equals("§aMalt") || // lvl 30
                name.equals("§aOat")) {  // lvl 20
            System.out.println("hide Farming: '" + name + "'");
            entity.setCustomNameTag(" ");
        }
        if (name.equals("§8Left-Click for Oil") ||
                name.equals("§8Right-Click for Meat") ||
                name.equals("§aTrout")  // lvl 10
        ) {
            System.out.println("hide Fishing: '" + name + "'");
            entity.setCustomNameTag(" ");
        }
    }
}
