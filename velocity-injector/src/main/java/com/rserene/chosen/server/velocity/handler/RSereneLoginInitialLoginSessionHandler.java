package com.rserene.chosen.server.velocity.injector.handler;

import com.google.common.primitives.Longs;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.api.util.GameProfile.Property;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.client.AuthSessionHandler;
import com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler;
import com.velocitypowered.proxy.connection.client.LoginInboundConnection;
import com.velocitypowered.proxy.crypto.EncryptionUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket;
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.stream.Collectors;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.auth.AuthResult.Result;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.main.RSereneLoginCoreAPI;
import com.rserene.chosen.server.api.internal.skinrestorer.SkinRestorerResult;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.internal.util.reflect.Accessor;
import com.rserene.chosen.server.api.internal.util.reflect.EnumAccessor;
import com.rserene.chosen.server.api.internal.util.reflect.NoSuchEnumException;
import com.rserene.chosen.server.api.internal.util.reflect.ReflectUtil;
import com.rserene.chosen.server.core.auth.LoginAuthResult;
import net.kyori.adventure.text.Component;

public class RSereneLoginInitialLoginSessionHandler {
   private static EnumAccessor loginStatsEnumAccessor;
   private static Accessor initialLoginSessionHandlerAccessor;
   private static Enum<?> loginStateEnum$LOGIN_PACKET_EXPECTED;
   private static Enum<?> loginStateEnum$LOGIN_PACKET_RECEIVED;
   private static Enum<?> loginStateEnum$ENCRYPTION_REQUEST_SENT;
   private static Enum<?> loginStateEnum$ENCRYPTION_RESPONSE_RECEIVED;
   private static MethodHandle assertStateMethod;
   private static MethodHandle setCurrentStateField;
   private static MethodHandle getLoginField;
   private static MethodHandle getVerifyField;
   private static MethodHandle getServerField;
   private static MethodHandle getInboundField;
   private static MethodHandle getMcConnectionField;
   private static MethodHandle getCurrentStateField;
   private static MethodHandle authSessionHandler_allArgsConstructor;
   private static boolean authSessionHandlerRequiresServerIdHash;
   private final InitialLoginSessionHandler initialLoginSessionHandler;
   private final RSereneLoginCoreAPI RSereneLoginCoreAPI;
   private final VelocityServer server;
   private final MinecraftConnection mcConnection;
   private final LoginInboundConnection inbound;
   private ServerLoginPacket login;
   private byte[] verify;
   private boolean encrypted = false;

   public RSereneLoginInitialLoginSessionHandler(InitialLoginSessionHandler initialLoginSessionHandler, RSereneLoginCoreAPI RSereneLoginCoreAPI) {
      this.initialLoginSessionHandler = initialLoginSessionHandler;
      this.RSereneLoginCoreAPI = RSereneLoginCoreAPI;

      try {
         this.server = (VelocityServer)getServerField.invoke((InitialLoginSessionHandler)initialLoginSessionHandler);
         this.mcConnection = (MinecraftConnection)getMcConnectionField.invoke((InitialLoginSessionHandler)initialLoginSessionHandler);
         this.inbound = (LoginInboundConnection)getInboundField.invoke((InitialLoginSessionHandler)initialLoginSessionHandler);
      } catch (Throwable e) {
         throw new RuntimeException(e);
      }
   }

   public static void init() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException, NoSuchEnumException {
      Class<InitialLoginSessionHandler> initialLoginSessionHandlerClass = InitialLoginSessionHandler.class;
      initialLoginSessionHandlerAccessor = new Accessor(initialLoginSessionHandlerClass);
      Class<?> loginStateEnum = Class.forName("com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler$LoginState");
      loginStatsEnumAccessor = new EnumAccessor(loginStateEnum);
      loginStateEnum$LOGIN_PACKET_EXPECTED = loginStatsEnumAccessor.findByName("LOGIN_PACKET_EXPECTED");
      loginStateEnum$LOGIN_PACKET_RECEIVED = loginStatsEnumAccessor.findByName("LOGIN_PACKET_RECEIVED");
      loginStateEnum$ENCRYPTION_REQUEST_SENT = loginStatsEnumAccessor.findByName("ENCRYPTION_REQUEST_SENT");
      loginStateEnum$ENCRYPTION_RESPONSE_RECEIVED = loginStatsEnumAccessor.findByName("ENCRYPTION_RESPONSE_RECEIVED");
      Lookup lookup = MethodHandles.lookup();
      assertStateMethod = lookup.unreflect(ReflectUtil.handleAccessible(initialLoginSessionHandlerAccessor.findFirstMethodByName(true, "assertState")));
      Field currentState = ReflectUtil.handleAccessible(initialLoginSessionHandlerClass.getDeclaredField("currentState"));
      getCurrentStateField = lookup.unreflectGetter(currentState);
      setCurrentStateField = lookup.unreflectSetter(currentState);
      getLoginField = lookup.unreflectGetter(
         ReflectUtil.handleAccessible(initialLoginSessionHandlerAccessor.findFirstFieldByType(true, ServerLoginPacket.class))
      );
      getVerifyField = lookup.unreflectGetter(ReflectUtil.handleAccessible(initialLoginSessionHandlerAccessor.findFirstFieldByType(true, byte[].class)));
      getServerField = lookup.unreflectGetter(ReflectUtil.handleAccessible(initialLoginSessionHandlerAccessor.findFirstFieldByType(true, VelocityServer.class)));
      getInboundField = lookup.unreflectGetter(
         ReflectUtil.handleAccessible(initialLoginSessionHandlerAccessor.findFirstFieldByType(true, LoginInboundConnection.class))
      );
      getMcConnectionField = lookup.unreflectGetter(
         ReflectUtil.handleAccessible(initialLoginSessionHandlerAccessor.findFirstFieldByType(true, MinecraftConnection.class))
      );

      Constructor<AuthSessionHandler> authSessionHandlerConstructor;
      try {
         authSessionHandlerConstructor = AuthSessionHandler.class
            .getDeclaredConstructor(VelocityServer.class, LoginInboundConnection.class, GameProfile.class, boolean.class, String.class);
         authSessionHandlerRequiresServerIdHash = true;
      } catch (NoSuchMethodException ignored) {
         authSessionHandlerConstructor = AuthSessionHandler.class
            .getDeclaredConstructor(VelocityServer.class, LoginInboundConnection.class, GameProfile.class, boolean.class);
         authSessionHandlerRequiresServerIdHash = false;
      }

      authSessionHandler_allArgsConstructor = lookup.unreflectConstructor(ReflectUtil.handleAccessible(authSessionHandlerConstructor));
   }

   private void initValues() throws Throwable {
      this.login = (ServerLoginPacket)getLoginField.invoke((InitialLoginSessionHandler)this.initialLoginSessionHandler);
      this.verify = (byte[])getVerifyField.invoke((InitialLoginSessionHandler)this.initialLoginSessionHandler);
   }

   public void handle(EncryptionResponsePacket packet) throws Throwable {
      this.initValues();
      assertStateMethod.invoke((InitialLoginSessionHandler)this.initialLoginSessionHandler, (Enum)loginStateEnum$ENCRYPTION_REQUEST_SENT);
      setCurrentStateField.invoke((InitialLoginSessionHandler)this.initialLoginSessionHandler, (Enum)loginStateEnum$ENCRYPTION_RESPONSE_RECEIVED);
      ServerLoginPacket login = this.login;
      if (login == null) {
         throw new IllegalStateException("No ServerLogin packet received yet.");
      }

      if (this.verify.length == 0) {
         throw new IllegalStateException("No EncryptionRequest packet sent yet.");
      }

      try {
         KeyPair serverKeyPair = this.server.getServerKeyPair();
         if (this.inbound.getIdentifiedKey() != null) {
            IdentifiedKey playerKey = this.inbound.getIdentifiedKey();
            if (!playerKey.verifyDataSignature(packet.getVerifyToken(), new byte[][]{this.verify, Longs.toByteArray(packet.getSalt())})) {
               throw new IllegalStateException("Invalid client public signature.");
            }
         } else {
            byte[] decryptedSharedSecret = EncryptionUtils.decryptRsa(serverKeyPair, packet.getVerifyToken());
            if (!MessageDigest.isEqual(this.verify, decryptedSharedSecret)) {
               throw new IllegalStateException("Unable to successfully decrypt the verification token.");
            }
         }

         byte[] decryptedSharedSecret = EncryptionUtils.decryptRsa(serverKeyPair, packet.getSharedSecret());
         this.encrypted = true;
         String username = login.getUsername();
         String serverId = EncryptionUtils.generateServerId(decryptedSharedSecret, serverKeyPair.getPublic());
         String playerIp = ((InetSocketAddress)this.mcConnection.getRemoteAddress()).getHostString();
         this.RSereneLoginCoreAPI
            .getPlugin()
            .getRunServer()
            .getScheduler()
            .runTaskAsync(
               () -> {
                  LoginAuthResult result = (LoginAuthResult)this.RSereneLoginCoreAPI.getAuthHandler().auth(username, serverId, playerIp);

                  try {
                     if ((Boolean)this.mcConnection.getChannel().eventLoop().submit(() -> {
                        if (this.mcConnection.isClosed()) {
                           return false;
                        }

                        try {
                           this.mcConnection.enableEncryption(decryptedSharedSecret);
                           return true;
                        } catch (GeneralSecurityException var8) {
                           LoggerProvider.getLogger().error("Unable to enable encryption for connection", var8);
                           this.mcConnection.close(true);
                           return false;
                        }
                     }).get()) {
                        if (result.getResult() == Result.ALLOW) {
                           com.rserene.chosen.server.api.profile.GameProfile gameProfile = result.getResponse();

                           try {
                              SkinRestorerResult restorerResult = this.RSereneLoginCoreAPI.getSkinRestorerHandler().doRestorer(result);
                              if (restorerResult.getThrowable() != null) {
                                 LoggerProvider.getLogger().error("An exception occurred while processing the skin repair.", restorerResult.getThrowable());
                              }

                              LoggerProvider.getLogger()
                                 .debug(
                                    String.format(
                                       "Skin restore result of %s is %s.",
                                       result.getBaseServiceAuthenticationResult().getResponse().getName(),
                                       restorerResult.getReason()
                                    )
                                 );
                              if (restorerResult.getResponse() != null) {
                                 gameProfile = restorerResult.getResponse();
                              }
                           } catch (Exception e) {
                              LoggerProvider.getLogger()
                                 .debug(
                                    String.format(
                                       "Skin restore result of %s is %s.", result.getBaseServiceAuthenticationResult().getResponse().getName(), "error"
                                    )
                                 );
                              LoggerProvider.getLogger().debug("An exception occurred while processing the skin repair.", e);
                           }

                           com.rserene.chosen.server.api.profile.GameProfile finalGameProfile = gameProfile;
                           this.mcConnection
                              .getChannel()
                              .eventLoop()
                              .submit(
                                 () -> {
                                    try {
                                       AuthSessionHandler authSessionHandler;
                                       if (authSessionHandlerRequiresServerIdHash) {
                                          authSessionHandler = (AuthSessionHandler)authSessionHandler_allArgsConstructor.invoke(
                                             (VelocityServer)this.server,
                                             (LoginInboundConnection)this.inbound,
                                             (GameProfile)this.generateGameProfile(finalGameProfile),
                                             (boolean)true,
                                             (String)serverId
                                          );
                                       } else {
                                          authSessionHandler = (AuthSessionHandler)authSessionHandler_allArgsConstructor.invoke(
                                             (VelocityServer)this.server,
                                             (LoginInboundConnection)this.inbound,
                                             (GameProfile)this.generateGameProfile(finalGameProfile),
                                             (boolean)true
                                          );
                                       }

                                       this.mcConnection.setActiveSessionHandler(StateRegistry.LOGIN, authSessionHandler);
                                    } catch (Throwable e) {
                                       throw new RuntimeException(e);
                                    }
                                 }
                              )
                              .get();
                        } else {
                           this.inbound.disconnect(Component.text(result.getKickMessage()));
                        }
                     }
                  } catch (Throwable e) {
                     LoggerProvider.getLogger().error("An exception occurred while processing validation results.", e);
                     if (this.isEncrypted()) {
                        this.getInbound().disconnect(Component.text(this.RSereneLoginCoreAPI.getLanguageHandler().getMessage("auth_error", new Pair[0])));
                     }

                     this.mcConnection.close(true);
                  }
               }
            );
      } catch (GeneralSecurityException var9) {
         LoggerProvider.getLogger().error("Unable to enable encryption.", var9);
         this.mcConnection.close(true);
      }
   }

   private GameProfile generateGameProfile(com.rserene.chosen.server.api.profile.GameProfile response) {
      return new GameProfile(
         response.getId(),
         response.getName(),
         response.getPropertyMap().values().stream().map(s -> new Property(s.getName(), s.getValue(), s.getSignature())).collect(Collectors.toList())
      );
   }

   @Generated
   public InitialLoginSessionHandler getInitialLoginSessionHandler() {
      return this.initialLoginSessionHandler;
   }

   @Generated
   public RSereneLoginCoreAPI getRSereneLoginCoreAPI() {
      return this.RSereneLoginCoreAPI;
   }

   @Generated
   public VelocityServer getServer() {
      return this.server;
   }

   @Generated
   public MinecraftConnection getMcConnection() {
      return this.mcConnection;
   }

   @Generated
   public LoginInboundConnection getInbound() {
      return this.inbound;
   }

   @Generated
   public ServerLoginPacket getLogin() {
      return this.login;
   }

   @Generated
   public byte[] getVerify() {
      return this.verify;
   }

   @Generated
   public boolean isEncrypted() {
      return this.encrypted;
   }
}
