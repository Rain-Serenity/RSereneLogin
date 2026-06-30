package com.rserene.chosen.server.velocity.injector;

import com.google.common.collect.Iterables;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.StateRegistry.PacketMapping;
import com.velocitypowered.proxy.protocol.StateRegistry.PacketRegistry;
import com.velocitypowered.proxy.protocol.StateRegistry.PacketRegistry.ProtocolRegistry;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket;
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket;
import io.netty.util.collection.IntObjectMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import com.rserene.chosen.server.api.internal.injector.Injector;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.main.RSereneLoginCoreAPI;
import com.rserene.chosen.server.api.internal.util.reflect.NoSuchEnumException;
import com.rserene.chosen.server.api.internal.util.reflect.ReflectUtil;
import com.rserene.chosen.server.velocity.injector.handler.RSereneLoginInitialLoginSessionHandler;
import com.rserene.chosen.server.velocity.injector.redirect.auth.RSereneLoginEncryptionResponse;
import com.rserene.chosen.server.velocity.injector.redirect.auth.RSereneLoginServerLogin;
import com.rserene.chosen.server.velocity.injector.redirect.chat.PlayerSessionPacketBlocker;

public class VelocityInjector implements Injector {
   public void inject(RSereneLoginCoreAPI RSereneLoginCoreAPI) throws NoSuchFieldException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchEnumException {
      RSereneLoginInitialLoginSessionHandler.init();
      PacketRegistry serverbound = this.getServerboundPacketRegistry(StateRegistry.LOGIN);
      this.redirectInput(serverbound, EncryptionResponsePacket.class, () -> new RSereneLoginEncryptionResponse(RSereneLoginCoreAPI));
      this.redirectInput(serverbound, ServerLoginPacket.class, () -> new RSereneLoginServerLogin(RSereneLoginCoreAPI));
   }

   public void registerChatSession(Map<Integer, Integer> packetMapping) {
      try {
         PacketRegistry serverbound = this.getServerboundPacketRegistry(StateRegistry.PLAY);
         LinkedList<PacketMapping> playerSessionPacketMapping = new LinkedList<>();

         for (Entry<Integer, Integer> entry : packetMapping.entrySet()) {
            LoggerProvider.getLogger().debug("Register PlayerSessionPacketBlocker for protocol version: " + entry.getKey());
            playerSessionPacketMapping.add(this.createPacketMapping(entry.getValue(), ProtocolVersion.getProtocolVersion(entry.getKey()), false));
         }

         this.registerPacket(
            serverbound, PlayerSessionPacketBlocker.class, PlayerSessionPacketBlocker::new, playerSessionPacketMapping.toArray(new PacketMapping[0])
         );
      } catch (Throwable throwable) {
         LoggerProvider.getLogger().error("Unable to register PlayerSessionPacketBlocker, chat session blocker does not work as expected.", throwable);
      }
   }

   private PacketRegistry getServerboundPacketRegistry(StateRegistry stateRegistry) throws NoSuchFieldException, IllegalAccessException {
      Field serverboundField = ReflectUtil.handleAccessible(StateRegistry.class.getDeclaredField("serverbound"));
      return (PacketRegistry)serverboundField.get(stateRegistry);
   }

   private <T> void redirectInput(PacketRegistry bound, Class<T> originalClass, Supplier<? extends T> supplierRedirect) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
      Field f$packetIdToSupplier = ProtocolRegistry.class.getDeclaredField("packetIdToSupplier");
      f$packetIdToSupplier.setAccessible(true);
      ReflectUtil.handleAccessible(f$packetIdToSupplier);
      Method map$entry$setValueMethod = Entry.class.getMethod("setValue", Object.class);

      for (Object protocolRegistry : this.getProtocolRegistries(bound)) {
         Map<?, ?> packetIdToSupplier = (Map<?, ?>)f$packetIdToSupplier.get(protocolRegistry);

         for (Entry<?, ?> e : packetIdToSupplier.entrySet()) {
            MinecraftPacket minecraftPacketObject = (MinecraftPacket)((Supplier)e.getValue()).get();
            if (minecraftPacketObject.getClass().equals(originalClass)) {
               map$entry$setValueMethod.invoke(e, supplierRedirect);
            }
         }
      }
   }

   private <T> void redirectOutput(PacketRegistry bound, Class<T> originalClass, Class<? extends T> appendClass) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
      Field f$packetClassToId = ProtocolRegistry.class.getDeclaredField("packetClassToId");
      ReflectUtil.handleAccessible(f$packetClassToId);
      Method map$putMethod = Map.class.getMethod("put", Object.class, Object.class);

      for (Object protocolRegistry : this.getProtocolRegistries(bound)) {
         Map<?, ?> packetClassToId = (Map<?, ?>)f$packetClassToId.get(protocolRegistry);
         if (packetClassToId.containsKey(originalClass)) {
            map$putMethod.invoke(packetClassToId, appendClass, packetClassToId.get(originalClass));
         }
      }
   }

   private Collection<?> getProtocolRegistries(PacketRegistry bound) throws NoSuchFieldException, IllegalAccessException {
      return this.getProtocolRegistriesMap(bound).values();
   }

   private Map<?, ?> getProtocolRegistriesMap(PacketRegistry bound) throws NoSuchFieldException, IllegalAccessException {
      Field f$versions = PacketRegistry.class.getDeclaredField("versions");
      ReflectUtil.handleAccessible(f$versions);
      return (Map<?, ?>)f$versions.get(bound);
   }

   private PacketMapping createPacketMapping(int id, ProtocolVersion protocolVersion, ProtocolVersion lastValidProtocolVersion, boolean packetDecoding) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
      Constructor<PacketMapping> constructor = ReflectUtil.handleAccessible(
         PacketMapping.class.getDeclaredConstructor(int.class, ProtocolVersion.class, ProtocolVersion.class, boolean.class)
      );
      return constructor.newInstance(id, protocolVersion, lastValidProtocolVersion, packetDecoding);
   }

   private PacketMapping createPacketMapping(int id, ProtocolVersion protocolVersion, boolean packetDecoding) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
      return this.createPacketMapping(id, protocolVersion, null, packetDecoding);
   }

   private <P extends MinecraftPacket> void registerPacket(PacketRegistry packetRegistry, Class<P> clazz, Supplier<P> packetSupplier, PacketMapping[] mappings) throws IllegalAccessException {
      try {
         this.register(packetRegistry, clazz, packetSupplier, mappings);
      } catch (NoSuchFieldException e) {
         throw new RuntimeException(e);
      }
   }

   <P extends MinecraftPacket> void register(PacketRegistry bound, Class<P> clazz, Supplier<P> packetSupplier, PacketMapping... mappings) throws NoSuchFieldException, IllegalAccessException {
      if (mappings.length == 0) {
         throw new IllegalArgumentException("At least one mapping must be provided.");
      }

      for (int i = 0; i < mappings.length; i++) {
         PacketMapping current = mappings[i];
         PacketMapping next = i + 1 < mappings.length ? mappings[i + 1] : current;
         Field protocolVersion = current.getClass().getDeclaredField("protocolVersion");
         protocolVersion.setAccessible(true);
         ProtocolVersion from = (ProtocolVersion)protocolVersion.get(current);
         Field lastValidProtocolVersion = current.getClass().getDeclaredField("lastValidProtocolVersion");
         lastValidProtocolVersion.setAccessible(true);
         ProtocolVersion lastValid = (ProtocolVersion)lastValidProtocolVersion.get(current);
         if (lastValid != null) {
            if (next != current) {
               throw new IllegalArgumentException("Cannot add a mapping after last valid mapping");
            }

            if (from.greaterThan(lastValid)) {
               throw new IllegalArgumentException("Last mapping version cannot be higher than highest mapping version");
            }
         }

         Field nextProtocolVersion = next.getClass().getDeclaredField("protocolVersion");
         nextProtocolVersion.setAccessible(true);
         ProtocolVersion to = current == next
            ? (lastValid != null ? lastValid : (ProtocolVersion)Iterables.getLast(ProtocolVersion.SUPPORTED_VERSIONS))
            : (ProtocolVersion)nextProtocolVersion.get(next);
         ProtocolVersion lastInList = lastValid != null ? lastValid : (ProtocolVersion)Iterables.getLast(ProtocolVersion.SUPPORTED_VERSIONS);
         if (from.noLessThan(to) && from != lastInList) {
            throw new IllegalArgumentException(String.format("Next mapping version (%s) should be lower then current (%s)", to, from));
         }

         for (ProtocolVersion protocol : EnumSet.range(from, to)) {
            if (protocol == to && next != current) {
               break;
            }

            ProtocolRegistry registry = (ProtocolRegistry)this.getProtocolRegistriesMap(bound).get(protocol);
            if (registry == null) {
               throw new IllegalArgumentException("Unknown protocol version " + protocolVersion);
            }

            Field packetIdToSupplier = registry.getClass().getDeclaredField("packetIdToSupplier");
            packetIdToSupplier.setAccessible(true);
            IntObjectMap<Supplier<? extends MinecraftPacket>> supplierIntObjectMap = (IntObjectMap<Supplier<? extends MinecraftPacket>>)packetIdToSupplier.get(
               registry
            );
            Field idField = current.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            if (!supplierIntObjectMap.containsKey(idField.getInt(current))) {
               Field packetClassToIdField = registry.getClass().getDeclaredField("packetClassToId");
               packetClassToIdField.setAccessible(true);
               Map<Class<? extends MinecraftPacket>, Integer> packetClassToId = (Map<Class<? extends MinecraftPacket>, Integer>)packetClassToIdField.get(
                  registry
               );
               if (packetClassToId.containsKey(clazz)) {
                  throw new IllegalArgumentException(clazz.getSimpleName() + " is already registered for version " + registry.version);
               }

               Field encodeOnly = current.getClass().getDeclaredField("encodeOnly");
               encodeOnly.setAccessible(true);
               if (!encodeOnly.getBoolean(current)) {
                  supplierIntObjectMap.put(idField.getInt(current), packetSupplier);
               }

               packetClassToId.put(clazz, idField.getInt(current));
            }
         }
      }
   }
}
