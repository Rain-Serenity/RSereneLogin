package fun.ksnb.rserenelogin.velocity.impl;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import lombok.Generated;

public class NewChatSessionPacketIDEvent {
   private final int packetID;
   private final ProtocolVersion version;
   private final Player player;

   @Generated
   public NewChatSessionPacketIDEvent(int packetID, ProtocolVersion version, Player player) {
      this.packetID = packetID;
      this.version = version;
      this.player = player;
   }

   @Generated
   public int getPacketID() {
      return this.packetID;
   }

   @Generated
   public ProtocolVersion getVersion() {
      return this.version;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }
}
