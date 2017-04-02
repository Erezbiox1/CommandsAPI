package com.erezbiox1.CommandsAPI;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static net.md_5.bungee.api.ChatColor.*;

/**
 * Created by Erezbiox1 on 27/03/2017.
 * (C) 2016 Erez Rotem All Rights Reserved.
 */
public class CommandManager {

    private static final String wildcardSymbol = "*";

    private static final String errorPrefix = RED + "" + BOLD + "Sorry!" + RESET + GRAY + " ";
    private static final String permissionErrorDefault = errorPrefix + "You don't have permission to access this command.";
    private static final String playerErrorDefault = errorPrefix + "You must be a player to access this command.";
    private static final String argumentsErrorDefault = errorPrefix + "Invalid Arguments, Please try again!";

    public static void register(CommandListener commandListener) {

        Class<?> _class = commandListener.getClass();
        Method[] methods = _class.getMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(Command.class)) {

                Command command = method.getAnnotation(Command.class);

                String commandName = command.name().isEmpty() ? method.getName() : command.name();
                String commandPermission = command.permission();
                String wildcards = command.wildcards();
                boolean commandPlayer = command.player();

                String permissionError = command.permissionError().isEmpty() ? permissionErrorDefault : command.permissionError();
                String playerError = command.playerError().isEmpty() ? playerErrorDefault : command.playerError();
                String argumentsError = command.argumentsError().isEmpty() ? argumentsErrorDefault : command.argumentsError();

                new CustomCommand(commandName) {

                    @Override
                    public boolean execute(CommandSender commandSender, String com, String[] args){
                        try {

                            if (!permissionCheck(commandPermission, commandSender)){
                                commandSender.sendMessage(permissionError);
                                return true;
                            }
                            if (!playerCheck(commandPlayer, commandSender)){
                                commandSender.sendMessage(playerError);
                                return true;
                            }
                            if (!argsCheck(wildcards, args)){
                                commandSender.sendMessage(argumentsError);
                                return true;
                            }

                            if(commandPlayer && !wildcards.isEmpty()) // Both player and wildcard
                                method.invoke(commandListener, (Player) commandSender, getArgs(wildcards, args));
                            else if(!commandPlayer && !wildcards.isEmpty()) // Only Wildcard
                                method.invoke(commandListener, commandSender, getArgs(wildcards, args));
                            else if(commandPlayer && wildcards.isEmpty()) // Only Player
                                method.invoke(commandListener, (Player) commandSender, args);
                            else if(!commandPlayer && wildcards.isEmpty()) // Neither.
                                method.invoke(commandListener, commandSender, args);
                        } catch (Exception e){
                            e.printStackTrace();
                        }

                        return true;
                    }
                };
            }
        }

    }

    private static boolean permissionCheck(String permission, CommandSender sender) {
        return permission.isEmpty() || sender.hasPermission(permission) || sender.isOp();
    }

    private static boolean playerCheck(boolean player, CommandSender sender) {
        return !player || sender instanceof Player;
    }

    private static boolean argsCheck(String wildcard, String[] args){

        if(wildcard.isEmpty()) return true;
        String[] wildcards = wildcard.split(" ");

        if(wildcards.length != args.length) return false;

        for (int i = 0; i < wildcards.length; i++) {
            if(wildcards[i].equals(wildcardSymbol))
                continue;

            if(!wildcards[i].toLowerCase().equals(args[i].toLowerCase()))
                return false;

        }

        return true;
    }

    private static String[] getArgs(String wildcard, String[] args){

        String[] wildcards = wildcard.split(" ");
        List<String> list = new ArrayList<>();

        for (int i = 0; i < wildcards.length; i++) {
            if(wildcards[i].equals(wildcardSymbol))
                list.add(args[i]);
        }

        String[] array = new String[list.size()];
        array = list.toArray(array);

        return array;
    }

    abstract static class CustomCommand extends BukkitCommand {

        CustomCommand(String name) {
            super(name);
            try {

                SimpleCommandMap smp = (SimpleCommandMap) Class.forName("org.bukkit.craftbukkit." +
                        Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".CraftServer")
                        .getMethod("getCommandMap").invoke(Bukkit.getServer());
                smp.register(name, this);
                register(smp);
            } catch (Exception ignored) {}
        }

    }
}
