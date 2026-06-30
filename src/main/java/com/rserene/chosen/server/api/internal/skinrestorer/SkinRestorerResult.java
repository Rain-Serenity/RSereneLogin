package com.rserene.chosen.server.api.internal.skinrestorer;

import com.rserene.chosen.server.api.profile.GameProfile;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface SkinRestorerResult {
   Reason getReason();

   GameProfile getResponse();

   Throwable getThrowable();

   public static enum Reason {
      NO_SKIN,
      NO_RESTORER,
      USE_CACHE,
      SIGNATURE_VALID,
      BAD_SKIN,
      RESTORER_SUCCEED,
      RESTORER_ASYNC,
      RESTORER_FAILED;

      // $FF: synthetic method
      private static Reason[] $values() {
         return new Reason[]{NO_SKIN, NO_RESTORER, USE_CACHE, SIGNATURE_VALID, BAD_SKIN, RESTORER_SUCCEED, RESTORER_ASYNC, RESTORER_FAILED};
      }
   }
}
