package com.wynntils.modules.wynnhanni;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.IClientCommand;

public class TestHanni extends CommandBase implements IClientCommand {
    @Override
    public String getName() {
        return "testhanni";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/testhanni";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        testHanni();
    }

    private void testHanni() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        System.out.println(" ");
        for (Entity entity : Minecraft.getMinecraft().world.loadedEntityList) {
            float distance = entity.getDistance(player);
            if (distance < 10) {
                if (entity instanceof EntityArmorStand) {
//                    System.out.println(" ");
//                    System.out.println("EntityArmorStand!");
                    String name = entity.getName();
                    String formattedText = entity.getDisplayName().getFormattedText();
                    String unformattedText = entity.getDisplayName().getUnformattedText();
                    String customNameTag = entity.getCustomNameTag();
                    System.out.println("EntityArmorStand '" + name + "' - " + distance );
//                    System.out.println("formattedText: '" + formattedText + "'");
//                    System.out.println("unformattedText: '" + unformattedText + "'");
//                    System.out.println("customNameTag: '" + customNameTag + "'");
                    System.out.println("distance: '" + distance + "'");
//                    System.out.println(" ");
                } else {
                    String simpleName = entity.getClass().getSimpleName();
                    System.out.println("distance to '" + simpleName + "' (" + distance + ")");
//                    if (entity instanceof EntityZombie) {
//                        EntityZombie zombie = (EntityZombie) entity;
//                        float health = zombie.getHealth();
//                        float maxHealth = zombie.getMaxHealth();
//                        System.out.println("health: " + health + "/" + maxHealth);
//                    }
                }
                if (entity instanceof EntityZombie) {
                    EntityZombie zombie = (EntityZombie) entity;
                    System.out.println(" ");
                    System.out.println("EntityZombie");
                    String customNameTag = zombie.getCustomNameTag();
                    String name = zombie.getName();
                    System.out.println("customNameTag: '" + customNameTag + "'");
                    System.out.println("name: '" + name + "'");
                    System.out.println(" ");
                }
            }
        }
        System.out.println(" ");
    }

    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return false;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
