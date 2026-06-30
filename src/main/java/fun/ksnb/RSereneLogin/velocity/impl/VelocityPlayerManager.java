package fun.ksnb.rserenelogin.velocity.impl;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import com.rserene.chosen.server.api.internal.plugin.IPlayerManager;

public class VelocityPlayerManager implements IPlayerManager {
   private final ProxyServer server;

   public VelocityPlayerManager(ProxyServer server) {
      this.server = server;
   }

   public Set<IPlayer> getPlayers(String name) {
      return (Set)this.server.getAllPlayers().stream().filter((p) -> p.getUsername().equalsIgnoreCase(name)).map(VelocityPlayer::new).collect(Collectors.toSet());
   }

   public IPlayer getPlayer(UUID uuid) {
      Optional<Player> player = this.server.getPlayer(uuid);
      return (IPlayer)player.map(VelocityPlayer::new).orElse(null);
   }

   public Set<IPlayer> getOnlinePlayers() {
      return (Set)this.server.getAllPlayers().stream().map(VelocityPlayer::new).collect(Collectors.toSet());
   }
}
