package com.rserene.chosen.server.api.internal.handle;

import lombok.Generated;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class HandleResult {
   private final Type type;
   private final String kickMessage;

   @Generated
   public Type getType() {
      return this.type;
   }

   @Generated
   public String getKickMessage() {
      return this.kickMessage;
   }

   @Generated
   public HandleResult(Type type, String kickMessage) {
      this.type = type;
      this.kickMessage = kickMessage;
   }

   public static enum Type {
      NONE,
      KICK;

      // $FF: synthetic method
      private static Type[] $values() {
         return new Type[]{NONE, KICK};
      }
   }
}
