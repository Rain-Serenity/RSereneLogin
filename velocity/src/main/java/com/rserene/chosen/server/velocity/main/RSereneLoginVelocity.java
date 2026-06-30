package com.rserene.chosen.server.velocity.main;

import com.google.inject.Inject;
import com.velocitypowered.api.event.AwaitingEventExecutor;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent.LoginStatus;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.rserene.chosen.server.velocity.impl.ChatSessionHandler;
import com.rserene.chosen.server.velocity.impl.NewChatSessionPacketIDEvent;
import com.rserene.chosen.server.velocity.logger.Slf4jLoggerBridge;
import io.netty.channel.Channel;
import java.io.File;
import java.nio.file.Path;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.injector.Injector;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.main.RSereneLoginCoreAPI;
import com.rserene.chosen.server.api.internal.plugin.IPlugin;
import com.rserene.chosen.server.loader.main.PluginLoader;
import org.slf4j.Logger;

public class RSereneLoginVelocity implements IPlugin {
   private static RSereneLoginVelocity instance;
   private final Path dataDirectory;
   private final VelocityServer server;
   private final com.rserene.chosen.server.velocity.impl.VelocityServer runServer;
   private final PluginLoader pluginLoader;
   private RSereneLoginCoreAPI RSereneLoginCoreAPI;
   private static final String KEY = "RSereneLoginChatSession";
   private Injector injector;

   @Inject
   public RSereneLoginVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
      instance = this;
      this.server = (VelocityServer)server;
      this.runServer = new com.rserene.chosen.server.velocity.impl.VelocityServer(this.server);
      this.dataDirectory = dataDirectory;
      LoggerProvider.setLogger(new Slf4jLoggerBridge(logger));
      this.pluginLoader = new PluginLoader(this);

      try {
         this.pluginLoader.load("RSereneLogin-Velocity-Injector.JarFile");
      } catch (Exception e) {
         LoggerProvider.getLogger().error("An exception was encountered while initializing the plugin.", e);
         server.shutdown();
      }
   }

   @Subscribe
   public void onInitialize(ProxyInitializeEvent event) {
      try {
         this.RSereneLoginCoreAPI = this.pluginLoader.getCoreObject();
         this.RSereneLoginCoreAPI.load();
         this.injector = (Injector)this.pluginLoader.findClass("com.rserene.chosen.server.velocity.injector.VelocityInjector").getConstructor().newInstance();
         this.injector.inject(this.RSereneLoginCoreAPI);
         this.injector.registerChatSession(this.RSereneLoginCoreAPI.getMapperConfig().getPacketMapping());
      } catch (Throwable e) {
         LoggerProvider.getLogger().error("An exception was encountered while loading the plugin.", e);
         this.server.shutdown();
         return;
      }

      new GlobalListener(this).register();
      new CommandHandler(this).register("rserenelogin");
      this.server.getEventManager().register(this, PostLoginEvent.class, (AwaitingEventExecutor<PostLoginEvent>)postLoginEvent -> EventTask.withContinuation(continuation -> {
         try {
            if (postLoginEvent.getPlayer().getProtocolVersion().getProtocol() >= 761) {
               this.injectPlayer(postLoginEvent.getPlayer());
               return;
            }
         } finally {
            continuation.resume();
         }
      }));
      this.server
         .getEventManager()
         .register(
            this,
            DisconnectEvent.class,
            PostOrder.LAST,
            (AwaitingEventExecutor<DisconnectEvent>)disconnectEvent -> disconnectEvent.getLoginStatus() == LoginStatus.CONFLICTING_LOGIN
               ? null
               : EventTask.async(() -> this.removePlayer(disconnectEvent.getPlayer()))
         );
      this.server
         .getEventManager()
         .register(this, NewChatSessionPacketIDEvent.class, (AwaitingEventExecutor<NewChatSessionPacketIDEvent>)packetEvent -> EventTask.withContinuation(continuation -> {
            try {
               this.RSereneLoginCoreAPI.getMapperConfig().getPacketMapping().put(packetEvent.getVersion().getProtocol(), packetEvent.getPacketID());
               this.RSereneLoginCoreAPI.getMapperConfig().save();
               this.injector.registerChatSession(this.RSereneLoginCoreAPI.getMapperConfig().getPacketMapping());
            } finally {
               continuation.resume();
            }
         }));
   }

   @Subscribe
   public void onDisable(ProxyShutdownEvent event) {
      try {
         this.RSereneLoginCoreAPI.close();
         this.pluginLoader.close();
      } catch (Exception e) {
         LoggerProvider.getLogger().error("An exception was encountered while close the plugin", e);
      } finally {
         this.RSereneLoginCoreAPI = null;
         this.server.shutdown();
      }
   }

   @Override
   public File getDataFolder() {
      return this.dataDirectory.toFile();
   }

   @Override
   public File getTempFolder() {
      return new File(this.getDataFolder(), "tmp");
   }

   private void injectPlayer(Player player) {
      ConnectedPlayer connectedPlayer = (ConnectedPlayer)player;
      connectedPlayer.getConnection()
         .getChannel()
         .pipeline()
         .addBefore("handler", "RSereneLoginChatSession", new ChatSessionHandler(player, this.server.getEventManager()));
   }

   private void removePlayer(Player player) {
      ConnectedPlayer connectedPlayer = (ConnectedPlayer)player;
      Channel channel = connectedPlayer.getConnection().getChannel();
      channel.eventLoop().submit(() -> channel.pipeline().remove("RSereneLoginChatSession"));
   }

   @Generated
   public static RSereneLoginVelocity getInstance() {
      return instance;
   }

   @Generated
   public VelocityServer getServer() {
      return this.server;
   }

   @Generated
   public com.rserene.chosen.server.velocity.impl.VelocityServer getRunServer() {
      return this.runServer;
   }

   @Generated
   public RSereneLoginCoreAPI getRSereneLoginCoreAPI() {
      return this.RSereneLoginCoreAPI;
   }
}
