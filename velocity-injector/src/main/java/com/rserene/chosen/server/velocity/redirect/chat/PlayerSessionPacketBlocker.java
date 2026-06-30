package com.rserene.chosen.server.velocity.injector.redirect.chat;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.ProtocolUtils.Direction;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;

public class PlayerSessionPacketBlocker implements MinecraftPacket {
   private UUID sessionId;
   private IdentifiedKey identifiedKey;
   private boolean hasKey = true;

   public void decode(ByteBuf byteBuf, Direction direction, ProtocolVersion protocolVersion) {
      byteBuf.markReaderIndex();

      try {
         this.sessionId = ProtocolUtils.readUuid(byteBuf);
         this.identifiedKey = ProtocolUtils.readPlayerKey(protocolVersion, byteBuf);
      } catch (Throwable t) {
         byteBuf.resetReaderIndex();
         LoggerProvider.getLogger().debug("Failed to decode player session packet.", t);
         this.hasKey = false;
      }
   }

   public void encode(ByteBuf byteBuf, Direction direction, ProtocolVersion protocolVersion) {
      if (this.hasKey) {
         ProtocolUtils.writeUuid(byteBuf, this.sessionId);
         ProtocolUtils.writePlayerKey(byteBuf, this.identifiedKey);
      }
   }

   public boolean handle(MinecraftSessionHandler minecraftSessionHandler) {
      return true;
   }
}
