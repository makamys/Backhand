package mods.battlegear2.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

/**
 * User: nerd-boy
 * Date: 26/06/13
 * Time: 1:40 PM
 */
public final class BattlegearSyncItemPacket extends AbstractMBPacket {

    public static final String packetName = "MB2|SyncItem";
	private String user;
	private InventoryPlayer inventory;
	private EntityPlayer player;

    public BattlegearSyncItemPacket(EntityPlayer player){
        this(player.getCommandSenderName(), player.inventory, player);
    }

    public BattlegearSyncItemPacket(String user, InventoryPlayer inventory, EntityPlayer player) {
        this.user = user;
        this.inventory = inventory;
        this.player = player;
    }

    public BattlegearSyncItemPacket() {
	}

	@Override
    public void process(ByteBuf inputStream, EntityPlayer player) {
        this.user = ByteBufUtils.readUTF8String(inputStream);
        this.player = player.worldObj.getPlayerEntityByName(user);
        if(this.player!=null){
            int slot = inputStream.readInt();
            ItemStack currentItem = ByteBufUtils.readItemStack(inputStream);
            ItemStack offhandItem = ByteBufUtils.readItemStack(inputStream);
            if(InventoryPlayerBattle.isValidSwitch(slot))
                this.player.inventory.currentItem = slot;
            BattlegearUtils.setPlayerCurrentItem(this.player, currentItem);
            BattlegearUtils.setPlayerOffhandItem(this.player, offhandItem);
            if(!player.worldObj.isRemote){//Using data sent only by client
                try {
                    ItemStack itemInUse = ByteBufUtils.readItemStack(inputStream);
                    int itemUseCount = inputStream.readInt();
                    this.player.setItemInUse(itemInUse,itemUseCount);
                } catch (Exception ignored){}
            }
        }
    }

	@Override
	public String getChannel() {
		return packetName;
	}

	@Override
	public void write(ByteBuf out) {
        ByteBufUtils.writeUTF8String(out, user);
        out.writeInt(inventory.currentItem);
        ByteBufUtils.writeItemStack(out, inventory.getCurrentItem());
        ByteBufUtils.writeItemStack(out, ((InventoryPlayerBattle)inventory).offhandItem);
        if(player.worldObj.isRemote){//client-side only thing
            ByteBufUtils.writeItemStack(out, player.getItemInUse());
        	out.writeInt(player.getItemInUseCount());
        }
	}
}