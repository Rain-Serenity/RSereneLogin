package com.rserene.chosen.server.velocity.injector.redirect.auth;

import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.main.RSereneLoginCoreAPI;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.velocity.injector.handler.RSereneLoginInitialLoginSessionHandler;
import net.kyori.adventure.text.Component;

public class RSereneLoginEncryptionResponse extends EncryptionResponsePacket {
   private final RSereneLoginCoreAPI RSereneLoginCoreAPI;

   public boolean handle(MinecraftSessionHandler handler) {
      if (!(handler instanceof InitialLoginSessionHandler)) {
         return super.handle(handler);
      }

      RSereneLoginInitialLoginSessionHandler RSereneLoginInitialLoginSessionHandler = new RSereneLoginInitialLoginSessionHandler(
         (InitialLoginSessionHandler)handler, this.RSereneLoginCoreAPI
      );

      try {
         RSereneLoginInitialLoginSessionHandler.handle(this);
      } catch (Throwable e) {
         if (RSereneLoginInitialLoginSessionHandler.isEncrypted()) {
            RSereneLoginInitialLoginSessionHandler.getInbound()
               .disconnect(Component.text(this.RSereneLoginCoreAPI.getLanguageHandler().getMessage("auth_error", new Pair[0])));
         }

         RSereneLoginInitialLoginSessionHandler.getMcConnection().close(true);
         LoggerProvider.getLogger().error("An exception occurred while processing a login request.", e);
      }

      return true;
   }

   @Generated
   public RSereneLoginEncryptionResponse(RSereneLoginCoreAPI RSereneLoginCoreAPI) {
      this.RSereneLoginCoreAPI = RSereneLoginCoreAPI;
   }
}
