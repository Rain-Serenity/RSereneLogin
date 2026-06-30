package fun.ksnb.rserenelogin.velocity.impl;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.jetbrains.annotations.NotNull;

public class ChatSessionHandler extends ChannelDuplexHandler {
   private final Player player;
   private final EventManager eventManager;
   private boolean detected;

   public ChatSessionHandler(Player player, EventManager eventManager) {
      this.player = player;
      this.eventManager = eventManager;
   }

   public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object packet) throws Exception {
      if (!this.detected && packet instanceof ByteBuf buffer) {
         ByteBuf c = buffer.asReadOnly();
         c.markReaderIndex();

          try {
             int packetId = c.readByte();
             ProtocolUtils.readUuid(c);
             ProtocolUtils.readPlayerKey(this.player.getProtocolVersion(), c);
             if (!c.isReadable()) {
                this.detected = true;
                this.eventManager.fire(new NewChatSessionPacketIDEvent(packetId, this.player.getProtocolVersion(), this.player));
                ReferenceCountUtil.release(packet);
                ctx.pipeline().remove(this);
                return;
             }

             super.channelRead(ctx, packet);
          } finally {
             c.resetReaderIndex();
          }

          return;
      }

      super.channelRead(ctx, packet);
   }
}
