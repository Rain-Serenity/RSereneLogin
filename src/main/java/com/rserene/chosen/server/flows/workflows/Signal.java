package com.rserene.chosen.server.flows.workflows;

public enum Signal {
   PASSED,
   TERMINATED;

   // $FF: synthetic method
   private static Signal[] $values() {
      return new Signal[]{PASSED, TERMINATED};
   }
}
