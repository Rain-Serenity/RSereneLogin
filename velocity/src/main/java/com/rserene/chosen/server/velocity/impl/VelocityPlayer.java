package com.rserene.chosen.server.velocity.impl;

import com.velocitypowered.api.proxy.Player;
import com.rserene.chosen.server.velocity.main.RSereneLoginVelocity;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.UUID;
import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import net.kyori.adventure.text.Component;

public class VelocityPlayer extends VelocitySender implements IPlayer {
   private final Player player;

   public VelocityPlayer(Player player) {
      super(player);
      this.player = player;
   }

   @Override
   public void kickPlayer(String message) {
      this.player.disconnect(Component.text(message));
   }

   @Override
   public UUID getUniqueId() {
      return this.player.getUniqueId();
   }

   @Override
   public SocketAddress getAddress() {
      return this.player.getRemoteAddress();
   }

   @Override
   public boolean isOnline() {
      return RSereneLoginVelocity.getInstance().getRunServer().getPlayerManager().getPlayer(this.player.getUniqueId()) != null;
   }

   @Override
   public String getName() {
      return this.player.getUsername();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         VelocityPlayer that = (VelocityPlayer)o;
         return Objects.equals(this.player.getUniqueId(), that.player.getUniqueId());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.player.getUniqueId());
   }
}
