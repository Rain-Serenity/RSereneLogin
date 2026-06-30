package com.rserene.chosen.server.velocity.injector.redirect.auth;

import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.main.RSereneLoginCoreAPI;

public class RSereneLoginServerLogin extends ServerLoginPacket {
   private final RSereneLoginCoreAPI RSereneLoginCoreAPI;

   public boolean handle(MinecraftSessionHandler handler) {
      return super.handle(handler);
   }

   @Generated
   public RSereneLoginServerLogin(RSereneLoginCoreAPI RSereneLoginCoreAPI) {
      this.RSereneLoginCoreAPI = RSereneLoginCoreAPI;
   }
}
