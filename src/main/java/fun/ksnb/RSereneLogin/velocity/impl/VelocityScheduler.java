package fun.ksnb.rserenelogin.velocity.impl;

import com.rserene.chosen.server.api.internal.plugin.BaseScheduler;

public class VelocityScheduler extends BaseScheduler {
   public void runTask(Runnable run, long delay) {
      this.runTaskAsync(run, delay);
   }
}
