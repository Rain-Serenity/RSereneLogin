package com.rserene.chosen.server.loader.main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.main.RSereneLoginCoreAPI;
import com.rserene.chosen.server.api.internal.plugin.IPlugin;
import com.rserene.chosen.server.api.internal.util.IOUtil;
import com.rserene.chosen.server.flows.workflows.ParallelFlows;
import com.rserene.chosen.server.flows.workflows.Signal;
import com.rserene.chosen.server.loader.classloader.IExtURLClassLoader;
import com.rserene.chosen.server.loader.classloader.PriorAllURLClassLoader;
import com.rserene.chosen.server.loader.exception.InitialFailedException;
import com.rserene.chosen.server.loader.library.Library;
import com.rserene.chosen.server.loader.task.LibraryDownloadFlows;

public class PluginLoader {
   public static final String nestJarName = "RSereneLogin-Core.JarFile";
    public static final String coreClassName = "moe.caa.rserenelogin.core.main.LiLoginCore";
   public static final Map<Library, String> libraryDigestMap;
   public static final Set<Library> libraries;
   public static final List<String> repositories;
   private final File librariesFolder;
   private final IPlugin plugin;
   private final AtomicBoolean loaded = new AtomicBoolean(false);
   private IExtURLClassLoader pluginClassLoader = new PriorAllURLClassLoader(new URL[0], PluginLoader.class.getClassLoader(), (Set)Stream.of("moe.caa.rserenelogin.", "com.rserene.chosen.server.", "java.", "net.minecraft.", "com.mojang.", "org.bukkit.").collect(Collectors.toSet()));
   private RSereneLoginCoreAPI coreObject;

   public PluginLoader(IPlugin plugin) {
      this.plugin = plugin;
      this.librariesFolder = new File(plugin.getDataFolder(), "lib");
   }

   public synchronized void load(String... additions) throws Exception {
      if (this.loaded.getAndSet(true)) {
         throw new UnsupportedOperationException("Repeated call.");
      } else {
         IOUtil.removeAllFiles(this.plugin.getTempFolder());
         this.generateFolder();
         List<Library> needDownload = new ArrayList();

         for(Library library : libraries) {
            File file = new File(this.librariesFolder, library.getFileName());
            if (file.exists() && file.length() != 0L) {
               String sha256 = this.getSha256(file);
               LoggerProvider.getLogger().debug(String.format("The digest value of calculation file %s is %s.", file.getName(), sha256));
               if (sha256.equals(libraryDigestMap.get(library))) {
                  this.pluginClassLoader.addURL(file.toURI().toURL());
                  continue;
               }

               LoggerProvider.getLogger().warn(String.format("Failed to validate digest value of file %s, it will be re-downloaded.", file.getAbsolutePath()));
            }

            needDownload.add(library);
         }

         if (needDownload.size() != 0) {
            LoggerProvider.getLogger().info(String.format("Downloading %d missing files...", needDownload.size()));
            ParallelFlows<Void> downloadFlows = new ParallelFlows<Void>((List)needDownload.stream().map((libraryx) -> new LibraryDownloadFlows(libraryx, this.librariesFolder, this.plugin.getTempFolder())).collect(Collectors.toList()));
            Signal run = downloadFlows.run(null);
            if (run == Signal.TERMINATED) {
               throw new InitialFailedException("Failed to download the missing file.");
            }
         }

         for(Library library : needDownload) {
            File file = new File(this.librariesFolder, library.getFileName());
            String sha256 = this.getSha256(file);
            LoggerProvider.getLogger().debug(String.format("The digest value of calculation file %s is %s.", file.getName(), sha256));
            if (!sha256.equals(libraryDigestMap.get(library))) {
               throw new InitialFailedException(String.format("Failed to validate the digest value of the file %s that was just downloaded.", file.getAbsolutePath()));
            }

            this.pluginClassLoader.addURL(file.toURI().toURL());
         }

         this.loadNestJar("RSereneLogin-Core.JarFile", this.pluginClassLoader);

         for(String addition : additions) {
            this.loadNestJar(addition, this.pluginClassLoader);
         }

         this.loadCore();
      }
   }

   private void loadNestJar(String nestJarName, IExtURLClassLoader classLoader) throws IOException {
      File output = File.createTempFile(nestJarName + ".", ".jar", this.plugin.getTempFolder());
      if (!output.exists()) {
         Files.createFile(output.toPath());
      }

      output.deleteOnExit();
      InputStream is = PluginLoader.class.getClassLoader().getResourceAsStream(nestJarName);

      try {
         FileOutputStream fos = new FileOutputStream(output);

         try {
            IOUtil.copy((InputStream)Objects.requireNonNull(is, nestJarName), fos);
         } catch (Throwable var10) {
            try {
               fos.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }

            throw var10;
         }

         fos.close();
      } catch (Throwable var11) {
         if (is != null) {
            try {
               is.close();
            } catch (Throwable var8) {
               var11.addSuppressed(var8);
            }
         }

         throw var11;
      }

      if (is != null) {
         is.close();
      }

      classLoader.addURL(output.toURI().toURL());
   }

   private void loadCore() throws Exception {
      Class<?> coreClass = this.findClass("moe.caa.rserenelogin.core.main.LiLoginCore");

      for(Constructor<?> constructor : coreClass.getDeclaredConstructors()) {
         Class<?>[] parameterTypes = constructor.getParameterTypes();
         if (parameterTypes.length == 1 && parameterTypes[0] == IPlugin.class) {
            this.coreObject = (RSereneLoginCoreAPI)constructor.newInstance(this.plugin);
            return;
         }
      }

      throw new RuntimeException("Not found constructor");
   }

   public Class<?> findClass(String name) throws ClassNotFoundException {
      return Class.forName(name, true, this.pluginClassLoader.self());
   }

   public synchronized void close() throws Exception {
      if (this.pluginClassLoader != null) {
         this.pluginClassLoader.self().close();
      }

      this.plugin.getRunServer().getScheduler().shutdown();
      this.coreObject = null;
      this.pluginClassLoader = null;
      IOUtil.removeAllFiles(this.plugin.getTempFolder());
   }

   private void generateFolder() throws IOException {
      if (!this.librariesFolder.exists() && !this.librariesFolder.mkdirs()) {
         throw new IOException(String.format("Unable to create folder: %s", this.librariesFolder.getAbsolutePath()));
      } else if (!this.plugin.getTempFolder().exists() && !this.plugin.getTempFolder().mkdirs()) {
         throw new IOException(String.format("Unable to create folder: %s", this.plugin.getTempFolder().getAbsolutePath()));
      }
   }

   private String getSha256(File file) throws Exception {
      FileInputStream fis = new FileInputStream(file);

      String var17;
      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();

         try {
            byte[] buff = new byte[1024];

            int n;
            while((n = fis.read(buff)) > 0) {
               baos.write(buff, 0, n);
            }

            byte[] digest = MessageDigest.getInstance("SHA-256").digest(baos.toByteArray());
            StringBuilder sb = new StringBuilder();

            for(byte aByte : digest) {
               String temp = Integer.toHexString(aByte & 255);
               if (temp.length() == 1) {
                  sb.append("0");
               }

               sb.append(temp);
            }

            var17 = sb.toString();
         } catch (Throwable var15) {
            try {
               baos.close();
            } catch (Throwable var14) {
               var15.addSuppressed(var14);
            }

            throw var15;
         }

         baos.close();
      } catch (Throwable var16) {
         try {
            fis.close();
         } catch (Throwable var13) {
            var16.addSuppressed(var13);
         }

         throw var16;
      }

      fis.close();
      return var17;
   }

   @Generated
   public IExtURLClassLoader getPluginClassLoader() {
      return this.pluginClassLoader;
   }

   @Generated
   public RSereneLoginCoreAPI getCoreObject() {
      return this.coreObject;
   }

   static {
      try {
         InputStream resourceAsStream = PluginLoader.class.getClassLoader().getResourceAsStream(".digests");

         try {
            InputStreamReader isr = new InputStreamReader(resourceAsStream);

            try {
               LineNumberReader lnr = new LineNumberReader(isr);

               try {
                  Map<Library, String> tMap = new HashMap();
                  lnr.lines().filter((s) -> !s.trim().isEmpty() && s.charAt(0) != '#').map((s) -> s.split("=")).forEach((ss) -> tMap.put(Library.of(ss[0], ":"), ss[1]));
                  libraryDigestMap = Collections.unmodifiableMap(tMap);
               } catch (Throwable var18) {
                  try {
                     lnr.close();
                  } catch (Throwable var13) {
                     var18.addSuppressed(var13);
                  }

                  throw var18;
               }

               lnr.close();
            } catch (Throwable var19) {
               try {
                  isr.close();
               } catch (Throwable var12) {
                  var19.addSuppressed(var12);
               }

               throw var19;
            }

            isr.close();
         } catch (Throwable var22) {
            if (resourceAsStream != null) {
               try {
                  resourceAsStream.close();
               } catch (Throwable var11) {
                  var22.addSuppressed(var11);
               }
            }

            throw var22;
         }

         if (resourceAsStream != null) {
            resourceAsStream.close();
         }
      } catch (Exception e) {
         throw new InitialFailedException(e);
      }

      try {
         InputStream resourceAsStream = PluginLoader.class.getClassLoader().getResourceAsStream("libraries");

         try {
            InputStreamReader isr = new InputStreamReader(resourceAsStream);

            try {
               LineNumberReader lnr = new LineNumberReader(isr);

               try {
                  libraries = (Set)lnr.lines().filter((s) -> !s.trim().isEmpty() && s.charAt(0) != '#').map((ss) -> Library.of(ss, "\\s+")).collect(Collectors.toUnmodifiableSet());
               } catch (Throwable var16) {
                  try {
                     lnr.close();
                  } catch (Throwable var10) {
                     var16.addSuppressed(var10);
                  }

                  throw var16;
               }

               lnr.close();
            } catch (Throwable var17) {
               try {
                  isr.close();
               } catch (Throwable var9) {
                  var17.addSuppressed(var9);
               }

               throw var17;
            }

            isr.close();
         } catch (Throwable var21) {
            if (resourceAsStream != null) {
               try {
                  resourceAsStream.close();
               } catch (Throwable var8) {
                  var21.addSuppressed(var8);
               }
            }

            throw var21;
         }

         if (resourceAsStream != null) {
            resourceAsStream.close();
         }
      } catch (Exception e) {
         throw new InitialFailedException(e);
      }

      try {
         InputStream resourceAsStream = PluginLoader.class.getClassLoader().getResourceAsStream("repositories");

         try {
            InputStreamReader isr = new InputStreamReader(resourceAsStream);

            try {
               LineNumberReader lnr = new LineNumberReader(isr);

               try {
                   LinkedList<String> tList = new LinkedList<>();
                   Stream<String> var10000 = lnr.lines().filter((s) -> !s.trim().isEmpty() && s.charAt(0) != '#').map((s) -> s.endsWith("/") ? s : s + "/");
                   Objects.requireNonNull(tList);
                   var10000.forEach(tList::add);
                  repositories = Collections.unmodifiableList(tList);
               } catch (Throwable var14) {
                  try {
                     lnr.close();
                  } catch (Throwable var7) {
                     var14.addSuppressed(var7);
                  }

                  throw var14;
               }

               lnr.close();
            } catch (Throwable var15) {
               try {
                  isr.close();
               } catch (Throwable var6) {
                  var15.addSuppressed(var6);
               }

               throw var15;
            }

            isr.close();
         } catch (Throwable var20) {
            if (resourceAsStream != null) {
               try {
                  resourceAsStream.close();
               } catch (Throwable var5) {
                  var20.addSuppressed(var5);
               }
            }

            throw var20;
         }

         if (resourceAsStream != null) {
            resourceAsStream.close();
         }
      } catch (Exception e) {
         throw new InitialFailedException(e);
      }

      for(Library library : libraries) {
         if (!libraryDigestMap.containsKey(library)) {
            throw new InitialFailedException("Missing digest for file " + library.getFileName() + ".");
         }
      }

   }
}
