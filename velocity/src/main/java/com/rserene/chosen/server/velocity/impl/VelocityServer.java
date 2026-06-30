package com.rserene.chosen.server.velocity.impl;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.config.PlayerInfoForwarding;
import com.velocitypowered.proxy.config.VelocityConfiguration;
import com.rserene.chosen.server.api.internal.plugin.BaseScheduler;
import com.rserene.chosen.server.api.internal.plugin.IPlayerManager;
import com.rserene.chosen.server.api.internal.plugin.ISender;
import com.rserene.chosen.server.api.internal.plugin.IServer;

public class VelocityServer implements IServer {
   private final ProxyServer server;
   private final BaseScheduler scheduler;
   private final IPlayerManager playerManager;

   public VelocityServer(ProxyServer server) {
      this.server = server;
      this.scheduler = new VelocityScheduler();
      this.playerManager = new VelocityPlayerManager(server);
   }

   @Override
   public BaseScheduler getScheduler() {
      return this.scheduler;
   }

   @Override
   public IPlayerManager getPlayerManager() {
      return this.playerManager;
   }

   @Override
   public boolean isOnlineMode() {
      return this.server.getConfiguration().isOnlineMode();
   }

   @Override
   public boolean isForwarded() {
      return ((VelocityConfiguration)this.server.getConfiguration()).getPlayerInfoForwardingMode() != PlayerInfoForwarding.NONE;
   }

   @Override
   public String getName() {
      return this.server.getVersion().getName();
   }

   @Override
   public String getVersion() {
      return this.server.getVersion().getVersion();
   }

   @Override
   public void shutdown() {
      this.server.shutdown();
   }

   @Override
   public ISender getConsoleSender() {
      return new VelocitySender(this.server.getConsoleCommandSource());
   }

   @Override
   public boolean pluginHasEnabled(String id) {
      for (PluginContainer plugin : this.server.getPluginManager().getPlugins()) {
         if (plugin.getDescription().getName().map(name -> name.equalsIgnoreCase(id)).orElse(false)) {
            return true;
         }
      }

      return false;
   }
}
