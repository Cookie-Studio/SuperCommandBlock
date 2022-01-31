package cn.wode490390.nukkit.cmdblock.functionlib;

import cn.nukkit.utils.Config;
import cn.wode490390.nukkit.cmdblock.CommandBlockPlugin;
import lombok.Getter;

import java.util.*;

public class Storage {

    @Getter
    private static Config config = new Config(CommandBlockPlugin.getInstance().getDataFolder() + "/storage.yml");
    @Getter
    private static Map<String,Object> storage = config.getAll();

    public static void save(){
        config.setAll((LinkedHashMap<String, Object>) storage);
        config.save();
    }

    public static Map createMap(String mapName){
        Map map = new HashMap();
        storage.put(mapName,map);
        return map;
    }

    public static List createList(String listName){
        List list = new ArrayList();
        storage.put(listName,list);
        return list;
    }

    public static Object get(String name){
        return storage.get(name);
    }

    public static void remove(String name){
        storage.remove(name);
    }

    public static boolean contain(String name){
        return storage.containsKey(name);
    }
}
