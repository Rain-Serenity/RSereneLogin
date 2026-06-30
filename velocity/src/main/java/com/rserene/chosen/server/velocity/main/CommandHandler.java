package com.rserene.chosen.server.velocity.main;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.SimpleCommand.Invocation;
import com.rserene.chosen.server.velocity.impl.VelocitySender;
import java.util.List;

public class CommandHandler {
   private final RSereneLoginVelocity multiLoginVelocity;
   private final SimpleCommand simpleCommand = new SimpleCommand() {
      public void execute(Invocation invocation) {
         String[] arguments = (String[])invocation.arguments();
         String[] ns = new String[arguments.length + 1];
         System.arraycopy(arguments, 0, ns, 1, arguments.length);
         ns[0] = invocation.alias();
         CommandHandler.this.multiLoginVelocity.getRSereneLoginCoreAPI().getCommandHandler().execute(new VelocitySender(invocation.source()), ns);
      }

      public List<String> suggest(Invocation invocation) {
         String[] arguments = (String[])invocation.arguments();
         String[] ns = new String[arguments.length + 1];
         System.arraycopy(arguments, 0, ns, 1, arguments.length);
         ns[0] = invocation.alias();
         return CommandHandler.this.multiLoginVelocity.getRSereneLoginCoreAPI().getCommandHandler().tabComplete(new VelocitySender(invocation.source()), ns);
      }
   };

   public CommandHandler(RSereneLoginVelocity multiLoginVelocity) {
      this.multiLoginVelocity = multiLoginVelocity;
   }

   public void register(String cmdName) {
      CommandManager commandManager = this.multiLoginVelocity.getServer().getCommandManager();
      commandManager.register(commandManager.metaBuilder(cmdName).aliases("rsl").build(), this.simpleCommand);
   }
}
