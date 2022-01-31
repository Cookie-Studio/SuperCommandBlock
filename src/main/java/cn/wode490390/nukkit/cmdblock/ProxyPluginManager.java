package cn.wode490390.nukkit.cmdblock;

import cn.nukkit.api.PowerNukkitDifference;
import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.event.Event;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.permission.Permissible;
import cn.nukkit.permission.Permission;
import cn.nukkit.plugin.EventExecutor;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginLoader;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.PluginException;
import cn.wode490390.nukkit.cmdblock.blockentity.BlockEntityCommandBlock;
import cn.wode490390.nukkit.cmdblock.util.ReflectUtil;

import javax.script.ScriptEngine;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProxyPluginManager extends PluginManager {

    private PluginManager pluginManager;

    public ProxyPluginManager(PluginManager pluginManager) {
        super(null, null);
        this.pluginManager = pluginManager;
    }

    @Override
    public List<PluginCommand> parseYamlCommands(Plugin plugin) {
        return (List<PluginCommand>)ReflectUtil.callUnaccessibleMethod(pluginManager,"parseYamlCommands",plugin);
    }

    @Override
    public void callEvent(Event event) {
        String eventName = event.getClass().getSimpleName();
        for (BlockEntityCommandBlock cb : BlockEntityCommandBlock.getListenMap().keySet()) {
            ScriptEngine scriptEngine = cb.getScriptEngine();
            BlockEntityCommandBlock.getListenMap().get(cb).forEach((k, v) -> {
                if (eventName.equals(k) && !v.isEmpty()) {
                    scriptEngine.put(v, event);
                    cb.execute();
                    scriptEngine.put(v, null);
                }
            });
        }
        pluginManager.callEvent(event);
    }

    @Override
    public Plugin getPlugin(String name) {
        return pluginManager.getPlugin(name);
    }

    @Override
    public boolean registerInterface(Class<? extends PluginLoader> loaderClass) {
        return pluginManager.registerInterface(loaderClass);
    }

    @Override
    @Since("1.3.0.0-PN")
    @PowerNukkitOnly
    public void loadPowerNukkitPlugins() {
        pluginManager.loadPowerNukkitPlugins();
    }

    @Override
    public Map<String, Plugin> getPlugins() {
        return pluginManager.getPlugins();
    }

    @Override
    public Plugin loadPlugin(String path) {
        return pluginManager.loadPlugin(path);
    }

    @Override
    public Plugin loadPlugin(File file) {
        return pluginManager.loadPlugin(file);
    }

    @Override
    public Plugin loadPlugin(String path, Map<String, PluginLoader> loaders) {
        return pluginManager.loadPlugin(path, loaders);
    }

    @Override
    public Plugin loadPlugin(File file, Map<String, PluginLoader> loaders) {
        return pluginManager.loadPlugin(file, loaders);
    }

    @Override
    public Map<String, Plugin> loadPlugins(String dictionary) {
        return pluginManager.loadPlugins(dictionary);
    }

    @Override
    public Map<String, Plugin> loadPlugins(File dictionary) {
        return pluginManager.loadPlugins(dictionary);
    }

    @Override
    public Map<String, Plugin> loadPlugins(String dictionary, List<String> newLoaders) {
        return pluginManager.loadPlugins(dictionary, newLoaders);
    }

    @Override
    public Map<String, Plugin> loadPlugins(File dictionary, List<String> newLoaders) {
        return pluginManager.loadPlugins(dictionary, newLoaders);
    }

    @Override
    public Map<String, Plugin> loadPlugins(File dictionary, List<String> newLoaders, boolean includeDir) {
        return pluginManager.loadPlugins(dictionary, newLoaders, includeDir);
    }

    @Override
    public Permission getPermission(String name) {
        return pluginManager.getPermission(name);
    }

    @Override
    public boolean addPermission(Permission permission) {
        return pluginManager.addPermission(permission);
    }

    @Override
    public void removePermission(String name) {
        pluginManager.removePermission(name);
    }

    @Override
    public void removePermission(Permission permission) {
        pluginManager.removePermission(permission);
    }

    @Override
    public Map<String, Permission> getDefaultPermissions(boolean op) {
        return pluginManager.getDefaultPermissions(op);
    }

    @Override
    public void recalculatePermissionDefaults(Permission permission) {
        pluginManager.recalculatePermissionDefaults(permission);
    }

    @Override
    public void subscribeToPermission(String permission, Permissible permissible) {
        pluginManager.subscribeToPermission(permission, permissible);
    }

    @Override
    public void unsubscribeFromPermission(String permission, Permissible permissible) {
        pluginManager.unsubscribeFromPermission(permission, permissible);
    }

    @Override
    public Set<Permissible> getPermissionSubscriptions(String permission) {
        return pluginManager.getPermissionSubscriptions(permission);
    }

    @Override
    public void subscribeToDefaultPerms(boolean op, Permissible permissible) {
        pluginManager.subscribeToDefaultPerms(op, permissible);
    }

    @Override
    public void unsubscribeFromDefaultPerms(boolean op, Permissible permissible) {
        pluginManager.unsubscribeFromDefaultPerms(op, permissible);
    }

    @Override
    public Set<Permissible> getDefaultPermSubscriptions(boolean op) {
        return pluginManager.getDefaultPermSubscriptions(op);
    }

    @Override
    public Map<String, Permission> getPermissions() {
        return pluginManager.getPermissions();
    }

    @Override
    public boolean isPluginEnabled(Plugin plugin) {
        return pluginManager.isPluginEnabled(plugin);
    }

    @Override
    public void enablePlugin(Plugin plugin) {
        pluginManager.enablePlugin(plugin);
    }

    @Override
    @PowerNukkitDifference(info = "Makes sure the PowerNukkitPlugin is never disabled", since = "1.3.0.0-PN")
    public void disablePlugins() {
        pluginManager.disablePlugins();
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        pluginManager.disablePlugin(plugin);
    }

    @Override
    public void clearPlugins() {
        pluginManager.clearPlugins();
    }

    @Override
    public void registerEvents(Listener listener, Plugin plugin) {
        pluginManager.registerEvents(listener, plugin);
    }

    @Override
    public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, Plugin plugin) throws PluginException {
        pluginManager.registerEvent(event, listener, priority, executor, plugin);
    }

    @Override
    public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, Plugin plugin, boolean ignoreCancelled) throws PluginException {
        pluginManager.registerEvent(event, listener, priority, executor, plugin, ignoreCancelled);
    }
}
