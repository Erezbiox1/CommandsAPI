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
@SuppressWarnings("ALL")
public class CommandManager {

    // This is the wildcard symbol, This Can be changed.
    private static final String wildcardSymbol = "*";

    private static final String errorPrefix = RED + "" + BOLD + "Sorry!" + RESET + GRAY + " ";
    private static final String permissionErrorDefault = errorPrefix + "You don't have permission to access this command.";
    private static final String playerErrorDefault = errorPrefix + "You must be a player to access this command.";
    private static final String argumentsErrorDefault = errorPrefix + "Invalid Arguments, Please try again!";

    public static void register(CommandListener... commandListeners) {

        // Do the following for each class provided.
        for(CommandListener commandListener: commandListeners) {

            // Get the class, then all of the methods in the class.
            Class<?> _class = commandListener.getClass();
            Method[] methods = _class.getMethods();

            for (Method method : methods) {

                // Check if the method has the command annotation.
                if (method.isAnnotationPresent(Command.class)) {

                    //Get staff from the annotation.
                    Command command = method.getAnnotation(Command.class);

                    String commandName = command.name().isEmpty() ? method.getName() : command.name();
                    String commandPermission = command.permission();
                    String wildcards = command.wildcards();
                    boolean commandPlayer = command.player();

                    String permissionError = command.permissionError().isEmpty() ? permissionErrorDefault : command.permissionError();
                    String playerError = command.playerError().isEmpty() ? playerErrorDefault : command.playerError();
                    String argumentsError = command.argumentsError().isEmpty() ? argumentsErrorDefault : command.argumentsError();

                    //Register the command.
                    new CustomCommand(commandName) {

                        @Override
                        public boolean execute(CommandSender commandSender, String com, String[] args) {
                            try {

                                // Check if the sender is a player, has enough permissions, and weather or not his arguments match.
                                if (!permissionCheck(commandPermission, commandSender)) {
                                    commandSender.sendMessage(permissionError);
                                    return true;
                                }

                                if (!playerCheck(commandPlayer, commandSender)) {
                                    commandSender.sendMessage(playerError);
                                    return true;
                                }

                                if (!argsCheck(wildcards, args)) {
                                    commandSender.sendMessage(argumentsError);
                                    return true;
                                }

                                // Not the best way - I know. I don't have the time to make this better.
                                // Invoke the orignal method.

                                if (commandPlayer && !wildcards.isEmpty()) // Both player and wildcard
                                    method.invoke(commandListener, (Player) commandSender, getArgs(wildcards, args));
                                else if (!commandPlayer && !wildcards.isEmpty()) // Only Wildcard
                                    method.invoke(commandListener, commandSender, getArgs(wildcards, args));
                                else if (commandPlayer && wildcards.isEmpty()) // Only Player
                                    method.invoke(commandListener, (Player) commandSender, args);
                                else if (!commandPlayer && wildcards.isEmpty()) // Neither.
                                    method.invoke(commandListener, commandSender, args);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // We want to send our own error message.
                            return true;
                        }
                    };
                }
            }

        }

    }

    private static boolean permissionCheck(String permission, CommandSender sender) {
        // OP will override.
        return permission.isEmpty() || sender.hasPermission(permission) || sender.isOp();
    }

    private static boolean playerCheck(boolean player, CommandSender sender) {
        return !player || sender instanceof Player;
    }

    private static boolean argsCheck(String wildcard, String[] args){

        // Check if there is anything to check. Then splitting the wildcards.
        if(wildcard.isEmpty()) return true;
        String[] wildcards = wildcard.split(" ");

        // Check for the length, this will prevent NPE's.
        if(wildcards.length != args.length) return false;

        for (int i = 0; i < wildcards.length; i++) {

            // Wildcard would override.
            if(wildcards[i].equals(wildcardSymbol))
                continue;

            // Check if the static argument matches the desired argument.
            if(!wildcards[i].toLowerCase().equals(args[i].toLowerCase()))
                return false;

        }

        return true;
    }

    private static String[] getArgs(String wildcard, String[] args){

        // Splits the wildcards
        String[] wildcards = wildcard.split(" ");
        List<String> list = new ArrayList<>();

        // Add every wildcarded argument to the list.
        for (int i = 0; i < wildcards.length; i++) {
            if(wildcards[i].equals(wildcardSymbol))
                list.add(args[i]);
        }

        // Return the list.
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
