package cn.wode490390.nukkit.cmdblock.functionlib;

import cn.nukkit.Server;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Position;
import cn.wode490390.nukkit.cmdblock.exceptions.SelectorSyntaxException;
import cn.wode490390.nukkit.cmdblock.util.EntitySelector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MC {
    public static boolean runcmd(CommandSender sender, String str, String... args) {
        String result = new String(str);
        if (args.length != 0) {
            try {
                for (String arg : args) {
                    result = result.replaceFirst("\\$", arg);
                }
            } catch (Throwable t) {
                return false;
            }
        }
        return Server.getInstance().dispatchCommand(sender, result);
    }

    public static Map<CommandSender, Boolean> runcmd(List<CommandSender> senders, String str, String... args) {
        Map<CommandSender, Boolean> results = new HashMap<>();
        for (CommandSender sender : senders) {
            results.put(sender, runcmd(sender, str, args));
        }
        return results;
    }

    public static List<Entity> matchEntities(Position reference, String selector) {
        try {
            if (reference instanceof BlockEntity)
                return EntitySelector.matchEntities(reference.add(0.5,0.5,0.5), selector);
            else
                return EntitySelector.matchEntities(reference, selector);
        } catch (SelectorSyntaxException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static List<Entity> matchEntities(List<Position> references, String selector) {
        List<Entity> result = new ArrayList<>();
        for (Position reference : references) {
            result.addAll(matchEntities(reference, selector));
        }
        return result;
    }

    public static List<CommandSender> filterOutCommandSender(List<Entity> entities){
        return entities.stream().filter(e -> e instanceof CommandSender).map(e -> (CommandSender)e).collect(Collectors.toList());
    }
}
