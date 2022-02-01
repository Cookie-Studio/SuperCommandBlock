package cn.wode490390.nukkit.cmdblock;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockUnknown;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginBase;
import cn.wode490390.nukkit.cmdblock.block.BlockCommandBlock;
import cn.wode490390.nukkit.cmdblock.block.BlockCommandBlockChain;
import cn.wode490390.nukkit.cmdblock.block.BlockCommandBlockRepeating;
import cn.wode490390.nukkit.cmdblock.block.BlockId;
import cn.wode490390.nukkit.cmdblock.blockentity.BlockEntityCommandBlock;
import cn.wode490390.nukkit.cmdblock.blockentity.BlockEntityId;
import cn.wode490390.nukkit.cmdblock.functionlib.Storage;
import cn.wode490390.nukkit.cmdblock.protocol.CommandBlockUpdatePacket;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CommandBlockPlugin extends PluginBase implements Listener {

    private static CommandBlockPlugin INSTANCE;

    @Override
    public void onLoad() {
        INSTANCE = this;
        proxyPluginManager();
    }

    @Override
    public void onEnable() {
//        try {
//            new MetricsLite(this, 6923);
//        } catch (Throwable ignore) {
//
//        }
        this.copyResource();
        //init storage
        Storage.save();
        //packet
        this.getServer().getNetwork().registerPacket(ProtocolInfo.COMMAND_BLOCK_UPDATE_PACKET, CommandBlockUpdatePacket.class);
        //block
        this.registerBlock(BlockId.COMMAND_BLOCK, BlockCommandBlock.class);
        this.registerBlock(BlockId.CHAIN_COMMAND_BLOCK, BlockCommandBlockChain.class);
        this.registerBlock(BlockId.REPEATING_COMMAND_BLOCK, BlockCommandBlockRepeating.class);
        //blockentity
        BlockEntity.registerBlockEntity(BlockEntityId.COMMAND_BLOCK, BlockEntityCommandBlock.class);
        //listener
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    private void copyResource(){
        this.saveResource("globalScript.js");
        this.saveResource("storage.yml");
        Path p = Paths.get(Server.getInstance().getDataPath() + "resource_packs/命令方块换行材质包.mcpack");
        if (!Files.exists(p)){
            this.getLogger().warning("未在目录" + p.toString() + "下找到材质包，正在复制，请在完成后重启服务器应用更改");
            try {
                Files.copy(this.getClass().getClassLoader().getResourceAsStream("命令方块换行材质包.mcpack"),p);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    @Override
    public void onDisable() {
        Storage.save();
    }

    private void registerBlock(int id, Class<? extends Block> clazz) {
        Item.list[id] = clazz;
        Block.list[id] = clazz;
        Block block;

        try {
            block = clazz.newInstance();
            try {
                Constructor<? extends Block> constructor = clazz.getDeclaredConstructor(int.class);
                constructor.setAccessible(true);
                for (int data = 0; data < 16; ++data) {
                    Block.fullList[(id << 4) | data] = constructor.newInstance(data);
                }
                Block.hasMeta[id] = true;
            } catch (NoSuchMethodException ignore) {
                for (int data = 0; data < 16; ++data) {
                    Block.fullList[(id << 4) | data] = block;
                }
            }
        } catch (Exception e) {
            this.getLogger().alert("Error while registering " + clazz.getName(), e);
            for (int data = 0; data < 16; ++data) {
                Block.fullList[(id << 4) | data] = new BlockUnknown(id, data);
            }
            return;
        }

        Block.solid[id] = block.isSolid();
        Block.transparent[id] = block.isTransparent();
        Block.hardness[id] = block.getHardness();
        Block.light[id] = block.getLightLevel();

        if (block.isSolid()) {
            if (block.isTransparent()) {
                Block.lightFilter[id] = 1;
            } else {
                Block.lightFilter[id] = 15;
            }
        } else {
            Block.lightFilter[id] = 1;
        }
    }

    @EventHandler
    public void onDataPacketReceive(DataPacketReceiveEvent event) {
        DataPacket packet = event.getPacket();
        if (packet instanceof CommandBlockUpdatePacket) {
            CommandBlockUpdatePacket pk = (CommandBlockUpdatePacket) packet;
            Player player = event.getPlayer();
            if (player.isOp() && player.isCreative()) {
                if (pk.isBlock) {
                    BlockEntity blockEntity = player.level.getBlockEntity(new Vector3(pk.x, pk.y, pk.z));
                    if (blockEntity instanceof BlockEntityCommandBlock) {
                        BlockEntityCommandBlock commandBlock = (BlockEntityCommandBlock) blockEntity;
                        Block block = commandBlock.getLevelBlock();

                        //change commandblock type
                        switch (pk.commandBlockMode) {
                            case ICommandBlock.MODE_REPEATING:
                                if (block.getId() != BlockId.REPEATING_COMMAND_BLOCK) {
                                    block = Block.get(BlockId.REPEATING_COMMAND_BLOCK, block.getDamage());
                                    commandBlock.scheduleUpdate();
                                }
                                break;
                            case ICommandBlock.MODE_CHAIN:
                                if (block.getId() != BlockId.CHAIN_COMMAND_BLOCK) {
                                    block = Block.get(BlockId.CHAIN_COMMAND_BLOCK, block.getDamage());
                                }
                                break;
                            case ICommandBlock.MODE_NORMAL:
                            default:
                                if (block.getId() != BlockId.COMMAND_BLOCK) {
                                    block = Block.get(BlockId.COMMAND_BLOCK, block.getDamage());
                                }
                                break;
                        }

                        boolean conditional = pk.isConditional;
//                        int meta = block.getDamage();
//
//                        if (conditional) {
//                            if (meta < 8) {
//                                block.setDamage(meta + 8);
//                            }
//                        } else {
//                            if (meta > 8) {
//                                block.setDamage(meta - 8);
//                            }
//                        }
                        block.setPropertyValue(BlockCommandBlock.CONDITIONAL_BIT, conditional);

                        player.level.setBlock(commandBlock, block, true);

                        commandBlock.setCommand(pk.command);
                        commandBlock.setName(pk.name);
                        commandBlock.setTrackOutput(pk.shouldTrackOutput);
                        commandBlock.setConditional(conditional);
                        commandBlock.setTickDelay(pk.tickDelay);
                        commandBlock.setExecutingOnFirstTick(pk.executingOnFirstTick);

                        //redstone mode / auto
                        boolean isRedstoneMode = pk.isRedstoneMode;
                        commandBlock.setAuto(!isRedstoneMode);
                        if (!isRedstoneMode && pk.commandBlockMode == ICommandBlock.MODE_NORMAL) {
                            commandBlock.trigger();
                        }

//                        commandBlock.spawnToAll();
                    }
                }/* else {
                    Entity entity = this.getLevel().getEntity(commandBlockUpdatePacket.minecartEid);
                    if (entity instanceof EntityMinecartCommandBlock) {
                        EntityMinecartCommandBlock commandMinecart = (EntityMinecartCommandBlock) entity;
                        //TODO: Minecart with Command Block
                    }
                }*/
            }
        }
    }

    public static CommandBlockPlugin getInstance() {
        return INSTANCE;
    }

    private static void proxyPluginManager() {
        ProxyPluginManager proxyPluginManager = new ProxyPluginManager(Server.getInstance().getPluginManager());

        try {
            Field field = Server.class.getDeclaredField("pluginManager");
            field.setAccessible(true);

            field.set(Server.getInstance(), proxyPluginManager);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getMessage());
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage());
        }
    }
}
