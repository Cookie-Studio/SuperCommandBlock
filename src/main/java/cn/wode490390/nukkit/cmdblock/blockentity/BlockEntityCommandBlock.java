package cn.wode490390.nukkit.cmdblock.blockentity;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntityNameable;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.event.Event;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.permission.PermissibleBase;
import cn.nukkit.permission.Permission;
import cn.nukkit.permission.PermissionAttachment;
import cn.nukkit.permission.PermissionAttachmentInfo;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.Faceable;
import cn.wode490390.nukkit.cmdblock.CommandBlockPlugin;
import cn.wode490390.nukkit.cmdblock.ICommandBlock;
import cn.wode490390.nukkit.cmdblock.block.BlockCommandBlock;
import cn.wode490390.nukkit.cmdblock.block.BlockCommandBlockChain;
import cn.wode490390.nukkit.cmdblock.block.BlockId;
import cn.wode490390.nukkit.cmdblock.functionlib.MC;
import cn.wode490390.nukkit.cmdblock.inventory.CommandBlockInventory;
import cn.wode490390.nukkit.cmdblock.util.ListenDefiner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import lombok.Getter;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
public class BlockEntityCommandBlock extends BlockEntitySpawnable implements ICommandBlock, BlockEntityNameable {

    @Getter
    protected static Map<BlockEntityCommandBlock,Map<String,String>> listenMap = new HashMap<>();
    protected boolean conditionalMode;
    protected boolean auto;
    protected String command;
    protected boolean isScript;
    protected long lastExecution;
    protected boolean trackOutput;
    protected String lastOutput;
    protected ListTag<StringTag> lastOutputParams; //TODO
    protected int lastOutputCommandMode;
    protected boolean lastOutputCondionalMode;
    protected boolean lastOutputRedstoneMode;
    protected int successCount;
    protected boolean conditionMet;
    protected boolean powered;
    protected int tickDelay;
    protected boolean executingOnFirstTick; //TODO: ???
    protected NashornScriptEngine scriptEngine;
    protected NashornScriptEngineFactory scriptEngineFactory;

    protected PermissibleBase perm;
    protected final Set<Player> viewers = Sets.newHashSet();
    protected int currentTick;

    public BlockEntityCommandBlock(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initBlockEntity() {
        this.perm = new PermissibleBase(this);
        this.currentTick = 0;
        this.scriptEngineFactory = new NashornScriptEngineFactory();

        if (this.namedTag.contains(TAG_POWERED)) {
            this.powered = this.namedTag.getBoolean(TAG_POWERED);
        } else {
            this.powered = false;
        }

        if (this.namedTag.contains(TAG_CONDITIONAL_MODE)) {
            this.conditionalMode = this.namedTag.getBoolean(TAG_CONDITIONAL_MODE);
        } else {
            this.conditionalMode = false;
        }

        if (this.namedTag.contains(TAG_AUTO)) {
            this.auto = this.namedTag.getBoolean(TAG_AUTO);
        } else {
            this.auto = false;
        }

        if (this.namedTag.contains(TAG_COMMAND)) {
            setCommand(this.namedTag.getString(TAG_COMMAND));
        } else {
            setCommand("");
        }

        if (this.namedTag.contains(TAG_LAST_EXECUTION)) {
            this.lastExecution = this.namedTag.getLong(TAG_LAST_EXECUTION);
        } else {
            this.lastExecution = 0;
        }

        if (this.namedTag.contains(TAG_TRACK_OUTPUT)) {
            this.trackOutput = this.namedTag.getBoolean(TAG_TRACK_OUTPUT);
        } else {
            this.trackOutput = true;
        }

        if (this.namedTag.contains(TAG_LAST_OUTPUT)) {
            this.lastOutput = this.namedTag.getString(TAG_LAST_OUTPUT);
        } else {
            this.lastOutput = null;
        }

        if (this.namedTag.contains(TAG_LAST_OUTPUT_PARAMS)) {
            this.lastOutputParams = (ListTag<StringTag>) this.namedTag.getList(TAG_LAST_OUTPUT_PARAMS);
        } else {
            this.lastOutputParams = new ListTag<>(TAG_LAST_OUTPUT_PARAMS);
        }

        if (this.namedTag.contains(TAG_LP_COMMAND_MODE)) {
            this.lastOutputCommandMode = this.namedTag.getInt(TAG_LP_COMMAND_MODE);
        } else {
            this.lastOutputCommandMode = 0;
        }

        if (this.namedTag.contains(TAG_LP_CONDIONAL_MODE)) {
            this.lastOutputCondionalMode = this.namedTag.getBoolean(TAG_LP_CONDIONAL_MODE);
        } else {
            this.lastOutputCondionalMode = true;
        }

        if (this.namedTag.contains(TAG_LP_REDSTONE_MODE)) {
            this.lastOutputRedstoneMode = this.namedTag.getBoolean(TAG_LP_REDSTONE_MODE);
        } else {
            this.lastOutputRedstoneMode = true;
        }

        if (this.namedTag.contains(TAG_SUCCESS_COUNT)) {
            this.successCount = this.namedTag.getInt(TAG_SUCCESS_COUNT);
        } else {
            this.successCount = 0;
        }

        if (this.namedTag.contains(TAG_CONDITION_MET)) {
            this.conditionMet = this.namedTag.getBoolean(TAG_CONDITION_MET);
        } else {
            this.conditionMet = false;
        }

        if (this.namedTag.contains(TAG_TICK_DELAY)) {
            this.tickDelay = this.namedTag.getInt(TAG_TICK_DELAY);
        } else {
            this.tickDelay = 0;
        }

        if (this.namedTag.contains(TAG_EXECUTE_ON_FIRST_TICK)) {
            this.executingOnFirstTick = this.namedTag.getBoolean(TAG_EXECUTE_ON_FIRST_TICK);
        } else {
            this.executingOnFirstTick = false;
        }

        super.initBlockEntity();

        if (this.getMode() == MODE_REPEATING) {
            this.scheduleUpdate();
        }
    }

    @Override
    public void saveNBT() {
        super.saveNBT();
        this.namedTag.putBoolean(TAG_POWERED, this.powered);
        this.namedTag.putBoolean(TAG_CONDITIONAL_MODE, this.conditionalMode);
        this.namedTag.putBoolean(TAG_AUTO, this.auto);
        if (this.command != null) {
            this.namedTag.putString(TAG_COMMAND, this.command);
        }
        this.namedTag.putLong(TAG_LAST_EXECUTION, this.lastExecution);
        this.namedTag.putBoolean(TAG_TRACK_OUTPUT, this.trackOutput);
        if (this.lastOutput != null) {
            this.namedTag.putString(TAG_LAST_OUTPUT, this.lastOutput);
        }
        if (this.lastOutputParams != null) {
            this.namedTag.putList(this.lastOutputParams);
        }
        this.namedTag.putInt(TAG_LP_COMMAND_MODE, this.lastOutputCommandMode);
        this.namedTag.putBoolean(TAG_LP_CONDIONAL_MODE, this.lastOutputCondionalMode);
        this.namedTag.putBoolean(TAG_LP_REDSTONE_MODE, this.lastOutputRedstoneMode);
        this.namedTag.putInt(TAG_SUCCESS_COUNT, this.successCount);
        this.namedTag.putBoolean(TAG_CONDITION_MET, this.conditionMet);
        this.namedTag.putInt(TAG_VERSION, CURRENT_VERSION);
        this.namedTag.putInt(TAG_TICK_DELAY, this.tickDelay);
        this.namedTag.putBoolean(TAG_EXECUTE_ON_FIRST_TICK, this.executingOnFirstTick);
    }

    @Override
    public CompoundTag getSpawnCompound() {
        CompoundTag nbt = getDefaultCompound(this, BlockEntityId.COMMAND_BLOCK)
                .putBoolean(TAG_CONDITIONAL_MODE, this.conditionalMode)
                .putBoolean(TAG_AUTO, this.auto)
                .putLong(TAG_LAST_EXECUTION, this.lastExecution)
                .putBoolean(TAG_TRACK_OUTPUT, this.trackOutput)
                .putInt(TAG_LP_COMMAND_MODE, this.lastOutputCommandMode)
                .putBoolean(TAG_LP_CONDIONAL_MODE, this.lastOutputCondionalMode)
                .putBoolean(TAG_LP_REDSTONE_MODE, this.lastOutputRedstoneMode)
                .putInt(TAG_SUCCESS_COUNT, this.successCount)
                .putBoolean(TAG_CONDITION_MET, this.conditionMet)
                .putInt(TAG_VERSION, CURRENT_VERSION)
                .putInt(TAG_TICK_DELAY, this.tickDelay)
                .putBoolean(TAG_EXECUTE_ON_FIRST_TICK, this.executingOnFirstTick);
        if (this.command != null) {
            nbt.putString(TAG_COMMAND, this.command);
        }
        if (this.lastOutput != null) {
            nbt.putString(TAG_LAST_OUTPUT, this.lastOutput);
        }
        if (this.lastOutputParams != null) {
            nbt.putList(this.lastOutputParams);
        }
        if (this.hasName()) {
            nbt.putString(TAG_CUSTOM_NAME, this.getName());
        }
        return nbt;
    }

    @Override
    public boolean isBlockEntityValid() {
        int blockId = this.getBlock().getId();
        return blockId == BlockId.COMMAND_BLOCK || blockId == BlockId.CHAIN_COMMAND_BLOCK || blockId == BlockId.REPEATING_COMMAND_BLOCK;
    }

    @Override
    public String getName() {
        return this.hasName() ? this.namedTag.getString(TAG_CUSTOM_NAME) : "!";
    }

    @Override
    public boolean hasName() {
        return this.namedTag.contains(TAG_CUSTOM_NAME);
    }

    @Override
    public void setName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            this.namedTag.remove(TAG_CUSTOM_NAME);
        } else {
            this.namedTag.putString(TAG_CUSTOM_NAME, name);
        }
//        this.spawnToAll();
    }

    @Override
    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    @Override
    public boolean isPowered() {
        return this.powered;
    }

    @Override
    public boolean onUpdate() {
        if (this.getMode() != MODE_REPEATING) {
            return false;
        }

        if (this.currentTick++ < this.getTickDelay()) {
            return true;
        }

        this.execute();
        this.currentTick = 0;

        return true;
    }

    @Override
    public boolean execute(int chain) {
        if (this.getLastExecution() != this.getServer().getTick()) {
            this.setConditionMet();
            if (/*this.getLevel().getGameRules().getBoolean(GameRule.COMMAND_BLOCKS_ENABLED) &&*/ this.isConditionMet() && (this.isAuto() || this.isPowered())) {
                String cmd = ListenDefiner.clearDefinition(this.getCommand().replace("#js",""));
                if (!Strings.isNullOrEmpty(cmd)) {
                    if (cmd.equalsIgnoreCase("Searge")) {
                        this.lastOutput = "#itzlipofutzli";
                        this.successCount = 1;
                    } else {
                        this.lastOutput = null;
                        if (cmd.startsWith("/")) {
                            cmd = cmd.substring(1);
                        }

                        //run cmd
                        if (isScript){
                            Object result = null;
                            try {
                                result = scriptEngine.invokeFunction("cmd");
                            } catch (Exception e) {
                                e.printStackTrace();
                                CommandBlockPlugin.getInstance().getLogger().error("throw an exception when running script");
                            }
                            if (!(result instanceof Boolean)){
                                this.successCount = 0;
                            }else{
                                if (((Boolean) result).booleanValue()){

                                    this.successCount = 1; //TODO: >1
                                }else{
                                    this.successCount = 0;
                                }
                            }
                        }else {
                            if (Server.getInstance().dispatchCommand(this, cmd)) {
                                this.successCount = 1; //TODO: >1
                            } else {
                                this.successCount = 0;
                            }
                        }
                    }

                    if (this.getSuccessCount() > 0) {
                        Block block = this.getBlock().getSide(((Faceable) this.getBlock()).getBlockFace());
                        if (block instanceof BlockCommandBlockChain) {
                            (((BlockCommandBlock) block).getBlockEntity()).trigger(++chain);
                        }
                    }
                }

                this.lastExecution = this.getServer().getTick();
                this.lastOutputCommandMode = this.getMode();
                this.lastOutputCondionalMode = this.isConditional();
                this.lastOutputRedstoneMode = !this.isAuto();
            } else {
                this.successCount = 0;
            }

//            this.spawnToAll();
            return true;
        }

        return false;
    }

    @Override
    public int getMode() {
        Block block = this.getBlock();
        if (block.getId() == BlockId.REPEATING_COMMAND_BLOCK) {
            return MODE_REPEATING;
        } else if (block.getId() == BlockId.CHAIN_COMMAND_BLOCK) {
            return MODE_CHAIN;
        }
        return MODE_NORMAL;
    }

    @Override
    public String getCommand() {
        return this.command;
    }

    @Override
    public void setCommand(String command) {
        this.command = command;
        if (ListenDefiner.isExistDefinition(command)){
            Map<String,String> arguments = ListenDefiner.getDefinedEvents(command);
            listenMap.put(this,arguments);
            command = ListenDefiner.clearDefinition(command);
        }
        if (command.contains("#js")) {
            isScript = true;
            command = command.replace("#js", "");
            //init nashorn engine
            scriptEngine = (NashornScriptEngine) scriptEngineFactory.getScriptEngine(new String[]{"-doe"}, this.getClass().getClassLoader(), str -> true);
            initScriptEngine();
            String script = "function cmd(){" + command + "}";
            if (!listenMap.get(this).isEmpty()){
                listenMap.get(this).forEach((k,v) -> scriptEngine.put(v,null));//define value
            }
            try {
                scriptEngine.eval(script);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }else{
            isScript = false;

        }
        this.successCount = 0;
//        this.spawnToAll();
    }

    @Override
    public boolean isAuto() {
        return this.auto;
    }

    @Override
    public void setAuto(boolean auto) {
        //boolean autoed = this.auto;
        this.auto = auto;
        //if (!autoed && this.auto && !this.powered && this.getMode() != MODE_CHAIN) {
        //    this.setConditionMet();
        //}
//        this.spawnToAll();
    }

    @Override
    public boolean isConditional() {
        return this.conditionalMode;
    }

    @Override
    public void setConditional(boolean conditionalMode) {
        this.conditionalMode = conditionalMode;
        this.setConditionMet();
    }

    @Override
    public boolean isConditionMet() {
        return this.conditionMet;
    }

    @Override
    public boolean setConditionMet() {
        Block block;
        if (this.isConditional() && (block = this.getBlock()) instanceof BlockCommandBlock) {
            Block next = block.getSide(((Faceable) block).getBlockFace().getOpposite());
            if (next instanceof BlockCommandBlock) {
                BlockEntityCommandBlock commandBlock = ((BlockCommandBlock) next).getBlockEntity();
                this.conditionMet = commandBlock.getSuccessCount() > 0;
            } else {
                this.conditionMet = false;
            }
        } else {
            this.conditionMet = true;
        }
//        this.spawnToAll();
        return this.conditionMet;
    }

    @Override
    public int getSuccessCount() {
        return this.successCount;
    }

    @Override
    public void setSuccessCount(int count) {
        this.successCount = count;
//        this.spawnToAll();
    }

    @Override
    public long getLastExecution() {
        return this.lastExecution;
    }

    @Override
    public void setLastExecution(long time) {
        this.lastExecution = time;
//        this.spawnToAll();
    }

    @Override
    public boolean isTrackingOutput() {
        return this.trackOutput;
    }

    @Override
    public void setTrackOutput(boolean track) {
        this.trackOutput = track;
//        this.spawnToAll();
    }

    @Override
    public String getLastOutput() {
        return this.lastOutput;
    }

    @Override
    public void setLastOutput(String output) {
        if (Strings.isNullOrEmpty(output)) {
            this.lastOutput = null;
        } else {
            this.lastOutput = output;
        }
//        this.spawnToAll();
    }

    @Override
    public int getLastOutputCommandMode() {
        return this.lastOutputCommandMode;
    }

    @Override
    public void setLastOutputCommandMode(int mode) {
        this.lastOutputCommandMode = mode;
//        this.spawnToAll();
    }

    @Override
    public boolean isLastOutputCondionalMode() {
        return this.lastOutputCondionalMode;
    }

    @Override
    public void setLastOutputCondionalMode(boolean condionalMode) {
        this.lastOutputCondionalMode = condionalMode;
//        this.spawnToAll();
    }

    @Override
    public boolean isLastOutputRedstoneMode() {
        return this.lastOutputRedstoneMode;
    }

    @Override
    public void setLastOutputRedstoneMode(boolean redstoneMode) {
        this.lastOutputRedstoneMode = redstoneMode;
//        this.spawnToAll();
    }

    @Override
    public void setLastOutputParams(ListTag<StringTag> params) {
        this.lastOutputParams = params;
//        this.spawnToAll();
    }

    @Override
    public int getTickDelay() {
        return this.tickDelay;
    }

    @Override
    public void setTickDelay(int tickDelay) {
        this.tickDelay = tickDelay;
//        this.spawnToAll();
    }

    @Override
    public boolean isExecutingOnFirstTick() {
        return this.executingOnFirstTick;
    }

    @Override
    public void setExecutingOnFirstTick(boolean executingOnFirstTick) {
        this.executingOnFirstTick = executingOnFirstTick;
//        this.spawnToAll();
    }

    @Override
    public boolean isPermissionSet(String name) {
        return this.perm.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        return this.perm.isPermissionSet(permission);
    }

    @Override
    public boolean hasPermission(String name) {
        return this.perm.hasPermission(name);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return this.perm.hasPermission(permission);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return this.perm.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name) {
        return this.perm.addAttachment(plugin, name);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, Boolean value) {
        return this.perm.addAttachment(plugin, name, value);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        this.perm.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        this.perm.recalculatePermissions();
    }

    @Override
    public Map<String, PermissionAttachmentInfo> getEffectivePermissions() {
        return this.perm.getEffectivePermissions();
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public Server getServer() {
        return Server.getInstance();
    }

    @Override
    public void sendMessage(TextContainer message) {
        this.sendMessage(this.getServer().getLanguage().translate(message));
    }

    @Override
    public void sendMessage(String message) {
        message = this.getServer().getLanguage().translateString(message);
        if (this.isTrackingOutput()) {
            this.lastOutput = message;
        }
        if (this.getLevel().getGameRules().getBoolean(GameRule.COMMAND_BLOCK_OUTPUT)) {
            for (Player player : this.getLevel().getPlayers().values()) {
                if (player.isOp()) {
                    player.sendMessage(message);
                }
            }
        }
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {

    }

    @Override
    public Inventory getInventory() {
        return new CommandBlockInventory(this, this.viewers);
    }

    private void initScriptEngine(){
        try {
            //global value
            scriptEngine.eval("var MC = Java.type('" + MC.class.getName() + "');");
            scriptEngine.put("own",this);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBreak() {
        super.onBreak();
        listenMap.remove(this);
    }
}
