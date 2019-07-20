/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.hard_science.network.client_to_server;

import grondag.exotic_matter.simulator.job.RequestPriority;
import grondag.hard_science.superblock.placement.Build;
import grondag.hard_science.superblock.placement.BuildManager;

/**
 * Universal packet for commands and notifications that require no parameters.
 */
public class PacketSimpleAction {
    public static enum ActionType {
        LAUNCH_CURRENT_BUILD
    }

    private ActionType actionType;

    public PacketSimpleAction() {
        super();
    }

    public PacketSimpleAction(ActionType actionType) {
        this();
        this.actionType = actionType;
    }

    @Override
    public void fromBytes(PacketBuffer pBuff) {
        this.actionType = pBuff.readEnumValue(ActionType.class);
    }

    @Override
    public void toBytes(PacketBuffer pBuff) {
        pBuff.writeEnumValue(this.actionType);
    }

    @Override
    protected void handle(PacketSimpleAction message, EntityPlayerMP player) {
        switch (message.actionType) {
        case LAUNCH_CURRENT_BUILD: {
            Build build = BuildManager.getActiveBuildForPlayer(player);
            if (build != null && build.isOpen() && !build.isEmpty()) {
                build.launch(RequestPriority.MEDIUM, player);
                if (!build.isOpen()) {
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
