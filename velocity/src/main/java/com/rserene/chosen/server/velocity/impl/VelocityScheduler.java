package com.rserene.chosen.server.velocity.impl;

import com.rserene.chosen.server.api.internal.plugin.BaseScheduler;

public class VelocityScheduler extends BaseScheduler {
   @Override
   public void runTask(Runnable run, long delay) {
      this.runTaskAsync(run, delay);
   }
}
