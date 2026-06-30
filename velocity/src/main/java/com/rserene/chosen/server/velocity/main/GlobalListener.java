package com.rserene.chosen.server.velocity.main;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.rserene.chosen.server.velocity.impl.VelocityPlayer;
import com.rserene.chosen.server.api.internal.handle.HandleResult;
import net.kyori.adventure.text.Component;

public class GlobalListener {
   private final RSereneLoginVelocity multiLoginVelocity;

   public GlobalListener(RSereneLoginVelocity multiLoginVelocity) {
      this.multiLoginVelocity = multiLoginVelocity;
   }

   public void register() {
      this.multiLoginVelocity.getServer().getEventManager().register(this.multiLoginVelocity, this);
   }

   @Subscribe(order = PostOrder.FIRST)
   public void onPlayerJoin(LoginEvent event) {
      HandleResult result = this.multiLoginVelocity
         .getRSereneLoginCoreAPI()
         .getPlayerHandler()
         .pushPlayerJoinGame(event.getPlayer().getUniqueId(), event.getPlayer().getUsername());
      if (result.getType() != HandleResult.Type.KICK) {
         this.multiLoginVelocity.getRSereneLoginCoreAPI().getPlayerHandler().callPlayerJoinGame(new VelocityPlayer(event.getPlayer()));
      } else {
         if (result.getKickMessage() != null && result.getKickMessage().trim().length() != 0) {
            event.getPlayer().disconnect(Component.text(result.getKickMessage()));
         } else {
            event.getPlayer().disconnect(Component.text(""));
         }
      }
   }

   @Subscribe(order = PostOrder.FIRST)
   public void onDisconnect(DisconnectEvent event) {
      this.multiLoginVelocity.getRSereneLoginCoreAPI().getPlayerHandler().pushPlayerQuitGame(event.getPlayer().getUniqueId(), event.getPlayer().getUsername());
   }
}
