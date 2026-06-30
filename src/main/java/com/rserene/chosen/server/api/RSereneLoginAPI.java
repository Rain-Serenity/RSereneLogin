package com.rserene.chosen.server.api;

import java.util.Collection;
import java.util.UUID;
import com.rserene.chosen.server.api.data.RSereneLoginPlayerData;
import com.rserene.chosen.server.api.service.IService;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public interface RSereneLoginAPI {
   @NotNull Collection<? extends IService> getServices();

   @Nullable RSereneLoginPlayerData getPlayerData(@NotNull UUID var1);
}
