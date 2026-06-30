package com.rserene.chosen.server.velocity.injector.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class IdentifiedKeyInvocationHandler implements InvocationHandler {
   private final Object obj;

   public IdentifiedKeyInvocationHandler(Object obj) {
      this.obj = obj;
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.getName().equals("hasExpired")) {
         return false;
      } else if (method.getName().equals("isSignatureValid")) {
         return true;
      } else {
         return method.getName().equals("internalAddHolder") ? true : method.invoke(this.obj, args);
      }
   }
}
