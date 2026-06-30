package fun.ksnb.rserenelogin.velocity.impl;

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

   public BaseScheduler getScheduler() {
      return this.scheduler;
   }

   public IPlayerManager getPlayerManager() {
      return this.playerManager;
   }

   public boolean isOnlineMode() {
      return this.server.getConfiguration().isOnlineMode();
   }

   public boolean isForwarded() {
      return ((VelocityConfiguration)this.server.getConfiguration()).getPlayerInfoForwardingMode() != PlayerInfoForwarding.NONE;
   }

   public String getName() {
      return this.server.getVersion().getName();
   }

   public String getVersion() {
      return this.server.getVersion().getVersion();
   }

   public void shutdown() {
      this.server.shutdown();
   }

   public ISender getConsoleSender() {
      return new VelocitySender(this.server.getConsoleCommandSource());
   }

   public boolean pluginHasEnabled(String id) {
      for(PluginContainer plugin : this.server.getPluginManager().getPlugins()) {
         if ((Boolean)plugin.getDescription().getName().map((name) -> name.equalsIgnoreCase(id)).orElse(false)) {
            return true;
         }
      }

      return false;
   }
}
