package grondag.hard_science.network.client_to_server;

import grondag.exotic_matter.simulator.job.RequestPriority;
import grondag.hard_science.superblock.placement.Build;
import grondag.hard_science.superblock.placement.BuildManager;

/**
 * Universal packet for commands and notifications that require no parameters.
 */
public class PacketSimpleAction
{
    public static enum ActionType
    {
        LAUNCH_CURRENT_BUILD
    }

    private ActionType actionType;
    
    public PacketSimpleAction()
    {
        super();
    }
    
    public PacketSimpleAction(ActionType actionType)
    {
        this();
        this.actionType = actionType;
    }
    
    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.actionType = pBuff.readEnumValue(ActionType.class);
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeEnumValue(this.actionType);
    }

    @Override
    protected void handle(PacketSimpleAction message, EntityPlayerMP player)
    {
        switch(message.actionType)
        {
            case LAUNCH_CURRENT_BUILD:
            {
                Build build = BuildManager.getActiveBuildForPlayer(player);
                if(build != null && build.isOpen() && !build.isEmpty())
                {
                    build.launch(RequestPriority.MEDIUM, player);
                    if(!build.isOpen())
                    {
                        String chatMessage = I18n.translateToLocalFormatted("placement.message.launch_build_confirm", build.getId());
                        player.sendMessage(new TextComponentString(chatMessage));
                        return;
                    }
                }
                String chatMessage = I18n.translateToLocal("placement.message.launch_build_fail");
                player.sendMessage(new TextComponentString(chatMessage));
                return;
            }
                
            default:
                break;
        
        }
    }
}
