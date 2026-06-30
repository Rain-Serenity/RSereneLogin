package com.rserene.chosen.server.api.internal.logger;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public enum Level {
   DEBUG,
   INFO,
   WARN,
   ERROR;

   // $FF: synthetic method
   private static Level[] $values() {
      return new Level[]{DEBUG, INFO, WARN, ERROR};
   }
}
