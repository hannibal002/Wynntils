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
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.IClientCommand;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;

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
        int radius = 10;
        if (args.length == 1) {
            try {
                radius = Integer.parseInt(args[0]);
            } catch (Exception e) {
                return;
            }
        }
        testHanni(radius);
    }

    private void testHanni(int radius) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        List<String> list = new ArrayList<>();
        for (Entity entity : Minecraft.getMinecraft().world.loadedEntityList) {
            if (entity instanceof EntityPlayerSP) {
                continue;
            }
            float distance = entity.getDistance(player);
            if (distance < radius) {
                if (entity instanceof EntityArmorStand) {
                    String name = entity.getName();
                    list.add("EntityArmorStand '" + name + "' - " + distance);
                } else {
                    String simpleName = entity.getClass().getSimpleName();
                    list.add("distance to '" + simpleName + "' (" + distance + ")");
                    String name = entity.getName();
                    list.add("name: '" + name + "'");
//                    if (entity instanceof EntityZombie) {
//                        EntityZombie zombie = (EntityZombie) entity;
//                        float health = zombie.getHealth();
//                        float maxHealth = zombie.getMaxHealth();
//                        System.out.println("health: " + health + "/" + maxHealth);
//                    }
                }
                if (entity instanceof EntityZombie) {
                    EntityZombie zombie = (EntityZombie) entity;
                    list.add(" ");
                    list.add("EntityZombie");
                    String customNameTag = zombie.getCustomNameTag();
                    String name = zombie.getName();
                    list.add("customNameTag: '" + customNameTag + "'");
                    list.add("name: '" + name + "'");
                    list.add(" ");
                }
            }
        }
        StringBuilder builder = new StringBuilder();
        for (String line : list) {
            builder.append(line);
            builder.append("\n");
        }
        Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Found " + list.size() + " entities nearby!"));
        setToClipboard(builder.toString());
    }

    private void setToClipboard(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);
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
