package fun.ksnb.rserenelogin.velocity.impl;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.console.VelocityConsole;
import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import com.rserene.chosen.server.api.internal.plugin.ISender;
import net.kyori.adventure.text.Component;

public class VelocitySender implements ISender {
   private final CommandSource commandSource;

   public VelocitySender(CommandSource commandSource) {
      this.commandSource = commandSource;
   }

   public boolean isPlayer() {
      return this.commandSource instanceof Player;
   }

   public boolean isConsole() {
      return this.commandSource instanceof VelocityConsole;
   }

   public boolean hasPermission(String permission) {
      return this.commandSource.hasPermission(permission);
   }

   public void sendMessagePL(String message) {
      for(String s : message.split("\\r?\\n")) {
         this.commandSource.sendMessage(Component.text(s));
      }

   }

   public String getName() {
      return "CONSOLE";
   }

   public IPlayer getAsPlayer() {
      return new VelocityPlayer((Player)this.commandSource);
   }
}
