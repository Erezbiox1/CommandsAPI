package com.erezbiox1.CommandsAPI;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;

import static net.md_5.bungee.api.ChatColor.*;

/**
 * Created by Erezbiox1 on 18/04/2017.
 * (C) 2016 Erez Rotem All Rights Reserved.
 */
@SuppressWarnings({"Duplicates", "unused"})
public class CommandManager {

    // Constant
    private static final String
            wildcardSymbol = "*",
            numberWildcardSymbol = "**",
            eitherSymbol = "|",
            errorPrefix = RED + "" + BOLD + "Sorry!" + RESET + GRAY + " ",
            permissionErrorDefault = errorPrefix + "You don't have permission to access this command.",
            playerErrorDefault = errorPrefix + "You must be a player to access this command.",
            argumentsErrorDefault = errorPrefix + "Invalid Arguments, Please try again!";

    // Get all of the name and methods of this listeners.
    @SuppressWarnings("Convert2Diamond")
    public static void register(CommandListener... listeners){

        for(CommandListener listener: listeners){

            Class<?> _class = listener.getClass();
            Method[] classMethods = _class.getMethods();

            Map<String, Set<Method>> map = new HashMap<>();

            for(Method method: classMethods){

                if(!method.isAnnotationPresent(Command.class)) continue;

                List<String> names = new ArrayList<>();
                if(!method.getAnnotation(Command.class).name().isEmpty()){

                    Command command = method.getAnnotation(Command.class);

                    if(command.name().trim().contains(" ")){

                        // Multiple names ( Command annotation )
                        names.addAll(new ArrayList<String>(Arrays.asList(command.name().trim().split(" "))));

                    }else{

                        // Single name ( Command annotation )
                        names.add(command.name());

                    }

                }else if(_class.isAnnotationPresent(CommandClass.class) && !_class.getAnnotation(CommandClass.class).name().isEmpty()){

                    CommandClass command = _class.getAnnotation(CommandClass.class);

                    if(command.name().trim().contains(" ")){

                        // Multiple names ( Class annotation )
                        names.addAll(new ArrayList<String>(Arrays.asList(command.name().trim().split(" "))));

                    }else{

                        // Single name ( Class annotation )
                        names.add(command.name());

                    }
                }else{

                    // Method name
                    names.add(method.getName());

                }

                for(String name: names){

                    if(!map.containsKey(name)){

                        Set<Method> methods = new HashSet<>();
                        methods.add(method);
                        map.put(name, methods);

                    }else{

                        map.get(name).add(method);

                    }

                }

            }

            registerCommands(listener, map);

        }

    }

    private static void registerCommands(CommandListener listener, Map<String, Set<Method>> map){
        map.forEach((name, methods) -> new CustomCommand(name) {

            @SuppressWarnings("ConstantConditions")
            @Override
            public boolean execute(CommandSender sender, String cmd, String[] args) {

                String error = "";

                for (Method method : methods) {

                    Command command = method.getAnnotation(Command.class);

                    String permission = command.permission();
                    String arguments = command.arguments();

                    String permissionError = command.permissionError();
                    String playerError = command.playerError();
                    String argumentsError = command.argumentsError();

                    if (listener.getClass().isAnnotationPresent(CommandClass.class)) {

                        CommandClass commandClass = method.getClass().getAnnotation(CommandClass.class);

                        if (permission.isEmpty())
                            permission = commandClass.permission();
                        else permission += commandClass.permission();


                        if (permissionError.equals("default"))
                            permissionError = commandClass.permissionError();

                        if (playerError.equals("default"))
                            playerError = commandClass.playerError();

                        if (argumentsError.equals("default"))
                            argumentsError = commandClass.argumentsError();

                    }

                    if (permissionError.equals("default"))
                        permissionError = permissionErrorDefault;

                    if (playerError.equals("default"))
                        playerError = playerErrorDefault;

                    if (argumentsError.equals("default"))
                        argumentsError = argumentsErrorDefault;


                    boolean player;
                    player = method.getParameterTypes().length != 0 && method.getParameterTypes()[0].equals(Player.class);

                    if (!playerCheck(player, sender)) {
                        if (!(playerError.equalsIgnoreCase("none") || playerError.isEmpty()))
                            error = playerError;

                        continue;
                    }

                    if (!permissionCheck(permission, sender)) {
                        if (!(playerError.equalsIgnoreCase("none") || permissionError.isEmpty()))
                            error = permissionError;

                        continue;
                    }

                    if (!argumentsCheck(arguments, args)) {
                        if (!(argumentsError.equalsIgnoreCase("none") || argumentsError.isEmpty()))
                            error = argumentsError;

                        continue;
                    }


                    run(method, arguments, args, listener, sender, player);
                    error = "";


                }

                if(!error.equals(""))
                    sender.sendMessage(error);

                return true;
            }

        });
    }

    @SuppressWarnings("RedundantCast")
    private static void run(Method method, String arguments, String[] args, CommandListener listener, CommandSender sender, boolean player){
        try {

            //This way sucks, I know, I don't have time to make this better so this will do.
            int parameters = method.getParameterTypes().length;

            if (parameters == 0)
                method.invoke(listener);
            else if (parameters == 1) {
                if (method.getParameterTypes()[0].equals(Player.class) || method.getParameterTypes()[0].equals(CommandSender.class)) {
                    method.invoke(listener, sender);
                } else {
                    if (arguments.isEmpty())
                        method.invoke(listener, (Object) args);
                    else
                        method.invoke(listener, (Object) getArgs(arguments, args));
                }
            } else if (parameters == 2) {
                if(method.getParameterTypes()[0].equals(Player.class) || method.getParameterTypes()[0].equals(CommandSender.class)) {
                    if (arguments.isEmpty())
                        method.invoke(listener, sender, (Object) args);
                    else
                        method.invoke(listener, sender, (Object) getArgs(arguments, args));
                }else{
                    if (!arguments.isEmpty())
                        method.invoke(listener, (Object) getArgs(arguments, args), sender);
                    else
                        method.invoke(listener, (Object) args, sender);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static boolean permissionCheck(String permissions, CommandSender sender) {
        if(sender.isOp() || permissions.isEmpty()) return true;

        if(permissions.trim().contains(" ")) {
            for (String permission : permissions.trim().split(" ")) {
                if (!sender.hasPermission(permission))
                    return false;
            }
        }else if(!sender.hasPermission(permissions))
                return false;

        return true;
    }

    private static boolean playerCheck(boolean player, CommandSender sender) {
        return !player || sender instanceof Player;
    }

    private static boolean argumentsCheck(String arguments, String[] args) {

        // Check if there is anything to check. Then splitting the wildcards.
        if (arguments.isEmpty()) return true;
        String[] wildcards = arguments.split(" ");

        // Check for the length, this will prevent NPE's.
        if (wildcards.length != args.length) return false;

        for (int i = 0; i < wildcards.length; i++) {

            // Wildcard would override.
            if (wildcards[i].equals(wildcardSymbol))
                continue;

            // Check if multiple values work for this argument. Example: "group|player" matches both group and player.
            if (wildcards[i].contains(eitherSymbol))
                for (String o :  wildcards[i].split("\\|"))
                    if (!o.equalsIgnoreCase(args[i]))
                        return false;

            //Check if the argument is a number.
            if(wildcards[i].contains(numberWildcardSymbol))
                if(!isNumber(wildcards[i]))
                    return false;

            // Check if the static argument matches the desired argument.
            if (!wildcards[i].toLowerCase().equals(args[i].toLowerCase()))
                return false;

        }

        return true;
    }

    private static String[] getArgs(String arguments, String[] args) {

        // Splits the wildcards
        String[] wildcards = arguments.split(" ");
        List<String> list = new ArrayList<>();

        // Add every wildcarded argument to the list.
        for (int i = 0; i < wildcards.length; i++) {
            if (wildcards[i].equals(wildcardSymbol) || wildcards[i].contains(eitherSymbol) || wildcards[i].equals(numberWildcardSymbol))
                list.add(args[i]);
        }

        // Return the list.
        String[] array = new String[list.size()];
        array = list.toArray(array);

        return array;
    }

    private static boolean isNumber(String string){

        //Not my code. Open to suggestions.
        return string.matches("-?\\d+(\\.\\d+)?");
    }

    // Bukkit reflection stuff and shit.
    abstract static class CustomCommand extends BukkitCommand {

        CustomCommand(String name) {
            super(name);
            try {

                SimpleCommandMap smp = (SimpleCommandMap) Class.forName("org.bukkit.craftbukkit." +
                        Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".CraftServer")
                        .getMethod("getCommandMap").invoke(Bukkit.getServer());
                smp.register(name, this);
                register(smp);
            } catch (Exception ignored) {
            }
        }

    }

}
