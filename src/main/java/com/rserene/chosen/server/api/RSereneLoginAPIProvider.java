package com.rserene.chosen.server.api;

import lombok.Generated;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public class RSereneLoginAPIProvider {
   private static RSereneLoginAPI api;

   @ApiStatus.Internal
   public static synchronized void setApi(RSereneLoginAPI api) {
      if (RSereneLoginAPIProvider.api != null) {
         throw new UnsupportedOperationException("duplicate api.");
      } else {
         RSereneLoginAPIProvider.api = api;
      }
   }

   @Generated
   public static RSereneLoginAPI getApi() {
      return api;
   }
}
