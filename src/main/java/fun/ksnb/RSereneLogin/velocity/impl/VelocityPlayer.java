package fun.ksnb.rserenelogin.velocity.impl;

import com.velocitypowered.api.proxy.Player;
import fun.ksnb.rserenelogin.velocity.main.RSereneLoginVelocity;
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

   public void kickPlayer(String message) {
      this.player.disconnect(Component.text(message));
   }

   public UUID getUniqueId() {
      return this.player.getUniqueId();
   }

   public SocketAddress getAddress() {
      return this.player.getRemoteAddress();
   }

   public boolean isOnline() {
      return RSereneLoginVelocity.getInstance().getRunServer().getPlayerManager().getPlayer(this.player.getUniqueId()) != null;
   }

   public String getName() {
      return this.player.getUsername();
   }

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

   public int hashCode() {
      return Objects.hash(new Object[]{this.player.getUniqueId()});
   }
}
