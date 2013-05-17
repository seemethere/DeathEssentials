package com.github.seemethere.DeathEssentials.utils;

import com.github.seemethere.DeathEssentials.ModularPlugin;
import com.github.seemethere.DeathEssentials.utils.commands.CMD;
import com.github.seemethere.DeathEssentials.utils.commands.CallInfo;
import com.github.seemethere.DeathEssentials.utils.commands.InvalidCommandArgumentsException;
import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

public class CommandManager {
    private ModularPlugin plugin;
    private Map<Method, Object> instances;
    private Logger logger;
    private Map<String, Method> commands;
    private Map<String, Method> subcommands;
    private Map<String, CCommand> commandMap;
    private List<String> subcommandMap;

    public CommandManager(ModularPlugin plugin) {
        this.plugin = plugin;
        commands = new HashMap<String, Method>();
        instances = new HashMap<Method, Object>();
        commandMap = new HashMap<String, CCommand>();
        subcommandMap = new ArrayList<String>();
        subcommands = new HashMap<String, Method>();
        logger = plugin.getLogger();
    }

    //=========================================
    //Helper methods
    //=========================================
    private static Object getPrivateField(Object object, String field) throws
            NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field objField = clazz.getDeclaredField(field);
        objField.setAccessible(true);
        Object result = objField.get(object);
        objField.setAccessible(false);
        return result;
    }

    public static List<String> seperateList(String s) {
        return Arrays.asList(s.split("\\s*,\\s*"));
    }

    private Object find(String name, Map<String, ?> map) {
        for (String s : map.keySet())
            if (s.equalsIgnoreCase(name))
                return map.get(s);
        return null;
    }

    //=========================================
    //Register Commands and Subcommands
    //=========================================
    @SuppressWarnings("ConstantConditions")
    public void register(Class<?> cls, Object instance) {
        for (Method method : cls.getMethods()) {
            if (method.isAnnotationPresent(CMD.class)) {
                CMD c = method.getAnnotation(CMD.class);
                List<String> aliases = new ArrayList<String>();
                aliases.add(c.command());
                if (!c.aliases().equals("")) {
                    aliases.addAll(seperateList(c.aliases()));
                }
                //Register command via reflection to Minecraft
                CCommand cmd = new CCommand(c.command(), c.description(),
                        c.usage(), aliases, this);
                try {
                    Object o = getPrivateField(plugin.getServer().getPluginManager(), "commandMap");
                    SimpleCommandMap cmap = (SimpleCommandMap) o;
                    cmap.register("", cmd);
                    //Only register this the first time
                    if (find(c.command(), commandMap) == null) {
                        instances.put(method, instance);
                        commandMap.put(c.command(), cmd);
                        //Register command for my own plugin's knowledge
                        for (String s : aliases)
                            commands.put(s, method);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } //Register a subcommand
            else if (method.isAnnotationPresent(CMD.SUB.class)) {
                registerSub(method, instance);
            }
        }
    }

    private void registerSub(Method method, Object instance) {
        CMD.SUB c = method.getAnnotation(CMD.SUB.class);
        if (find(c.parent(), commandMap) != null) {
            subcommandMap.add(c.parent().toLowerCase() + " " + c.name().toLowerCase());
            Object o = find(c.parent(), commandMap);
            for (String s : ((CCommand) o).getAliases())
                subcommandMap.add(s.toLowerCase() + " " + c.name().toLowerCase());
            subcommands.put(c.name(), method);
            instances.put(method, instance);
        }
    }

    //=========================================
    //Unregister Commands and Subcommands
    //=========================================
    public void unregister(Class<?> cls) {
        unregisterSubs(cls);
        for (Method method : cls.getMethods()) {
            if (method.isAnnotationPresent(CMD.class)) {
                CMD c = method.getAnnotation(CMD.class);
                CCommand cmd = commandMap.get(c.command());
                // Actually remove all commands plus their aliases
                try {
                    Object i = getPrivateField(plugin.getServer().getPluginManager(), "commandMap");
                    SimpleCommandMap cmap = (SimpleCommandMap) i;
                    Object o = getPrivateField(cmap, "knownCommands");
                    @SuppressWarnings("unchecked")
                    HashMap<String, Command> knownCommands = (HashMap<String, Command>) o;
                    knownCommands.remove(cmd.getName());
                    for (String alias : cmd.getAliases())
                        if (knownCommands.containsKey(alias))
                            knownCommands.remove(alias);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                commandMap.remove(c.command());
            }
        }
    }

    public void unregisterSubs(Class<?> cls) {
        for (Method method : cls.getDeclaredMethods()) {
            if (method.isAnnotationPresent(CMD.SUB.class)) {
                CMD.SUB c = method.getAnnotation(CMD.SUB.class);
                Object o = find(c.parent(), commandMap);
                subcommandMap.remove(c.parent().toLowerCase() + " " + c.name().toLowerCase());
                for (String s : ((CCommand) o).getAliases())
                    subcommandMap.remove(s.toLowerCase() + " " + c.name().toLowerCase());
            }
        }
    }

    //=========================================
    //Calling Commands and Subcommands
    //=========================================
    public void commandHandler(String name, CommandSender sender, String[] args) {
        CallInfo info = new CallInfo(sender, args);
        Method method = commands.get(name.toLowerCase());
        if (method == null)
            return;
        CMD command = method.getAnnotation(CMD.class);
        if (args.length > 0 && !command.hasArgs()) {
            if (!subCommandHandler(name, info))
                info.reply(command.usage());
            return;
        }
        if (!sender.hasPermission(command.permission())) {
            info.reply(command.permissionsMessage());
            return;
        }
        if (!command.AllowConsole() && !(sender instanceof Player)) {
            logger.info("This command is for players only!");
            return;
        }
        invokeMethod(method, info);
    }

    private boolean subCommandHandler(String name, CallInfo info) {
        // Return false if it's an invalid command
        if (!subcommandMap.contains(name.toLowerCase() + " " + info.args[0].toLowerCase()))
            return false;
        Object o = find(info.args[0], subcommands);
        Method method = (Method) o;
        if (method == null)
            return false;
        CMD.SUB c = method.getAnnotation(CMD.SUB.class);
        if (!info.sender.hasPermission(c.permission())) {
            info.reply(c.permissionsMessage());
            return true;
        }
        if (!(c.min() <= info.args.length - 1 && (c.max() == -1 || c.max() >= info.args.length - 1))) {
            info.reply(c.usage());
            return true;
        }
        if (!c.AllowConsole() && !(info.sender instanceof Player)) {
            logger.info("This command is for players only!");
            return true;
        }
        invokeMethod(method, info);
        return true;
    }

    private void invokeMethod(Method method, CallInfo info) {
        try {
            method.invoke(instances.get(method), info);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Private method for command!");
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof InvalidCommandArgumentsException) {
                e.printStackTrace();
                throw new RuntimeException("Inavlid arguments on command!");
            } else {
                e.printStackTrace();
                throw new RuntimeException("Invalid methods on command!");
            }
        }
    }

    //=========================================
    //Creating custom Command class
    //=========================================
    private static class CCommand extends Command {
        CommandManager cm = null;

        protected CCommand(String name, String desc, String usage,
                           List<String> aliases, CommandManager cm) {
            super(name, desc, usage, aliases);
            Validate.notNull(cm);
            this.cm = cm;
        }

        public boolean execute(CommandSender sender, String label, String[] args) {
            cm.commandHandler(label, sender, args);
            return true;
        }
    }
}
