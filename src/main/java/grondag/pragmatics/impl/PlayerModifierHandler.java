package grondag.pragmatics.impl;

import org.lwjgl.glfw.GLFW;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class PlayerModifierHandler {

    public static Identifier PACKET_ID = new Identifier("pragma", "modifiers");
    
    @Environment(EnvType.CLIENT)
    private static byte lastFlags = 0;
    
    @Environment(EnvType.CLIENT)
    public static void update(MinecraftClient client) {
        final long handle = client.window.getHandle();
        
        byte f = 0;
        if(InputUtil.isKeyPressed(handle, GLFW.GLFW_MOD_SHIFT)) {
            f |= PlayerModifierAccess.SHIFT;
        }
        if(InputUtil.isKeyPressed(handle, GLFW.GLFW_MOD_CONTROL)) {
            f |= PlayerModifierAccess.CONTROL;
        }
        if(InputUtil.isKeyPressed(handle, GLFW.GLFW_MOD_ALT)) {
            f |= PlayerModifierAccess.ALT;
        }
        if(InputUtil.isKeyPressed(handle, GLFW.GLFW_MOD_SUPER)) {
            f |= PlayerModifierAccess.SUPER;
        }
        
        if(f != lastFlags) {
            lastFlags = f;
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if(player != null) {
                ((PlayerModifierAccess)player).prg_flags(f);
            }
            sendUpdatePacket(f);
        }
    }
    
    @Environment(EnvType.CLIENT)
    private static void sendUpdatePacket(byte flags) {
        final PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeByte(flags);
        ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, buf);
    }
    
    public static void accept(PacketContext context, PacketByteBuf buf) {
        final PlayerEntity player = context.getPlayer();
        if(player != null) {
            ((PlayerModifierAccess)player).prg_flags(buf.readByte());
        }
    }
}
