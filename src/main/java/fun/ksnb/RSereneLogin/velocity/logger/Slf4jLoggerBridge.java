package fun.ksnb.rserenelogin.velocity.logger;

import com.rserene.chosen.server.api.internal.logger.Level;
import com.rserene.chosen.server.api.internal.logger.bridges.BaseLoggerBridge;
import org.slf4j.Logger;

public class Slf4jLoggerBridge extends BaseLoggerBridge {
   private final Logger logger;

   public Slf4jLoggerBridge(Logger logger) {
      this.logger = logger;
   }

   public void log(Level level, String message, Throwable throwable) {
      if (level == Level.DEBUG) {
         this.logger.debug(message, throwable);
      } else if (level == Level.INFO) {
         this.logger.info(message, throwable);
      } else if (level == Level.WARN) {
         this.logger.warn(message, throwable);
      } else if (level == Level.ERROR) {
         this.logger.error(message, throwable);
      }

   }
}
