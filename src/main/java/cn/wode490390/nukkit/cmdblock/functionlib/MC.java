package cn.wode490390.nukkit.cmdblock.functionlib;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;

public class MC {
    public static boolean runcmd(CommandSender sender, String str, String... args){
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
}
