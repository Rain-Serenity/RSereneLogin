package com.rserene.chosen.server.loader.task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.List;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.util.IOUtil;
import com.rserene.chosen.server.flows.workflows.BaseFlows;
import com.rserene.chosen.server.flows.workflows.Signal;
import com.rserene.chosen.server.loader.exception.InitialFailedException;
import com.rserene.chosen.server.loader.library.Library;
import com.rserene.chosen.server.loader.main.PluginLoader;

public class LibraryDownloadFlows extends BaseFlows<Void> {
   private final Library library;
   private final File librariesFolder;
   private final File tempLibrariesFolder;

   public LibraryDownloadFlows(Library library, File librariesFolder, File tempLibrariesFolder) {
      this.library = library;
      this.librariesFolder = librariesFolder;
      this.tempLibrariesFolder = tempLibrariesFolder;
   }

   private static byte[] getBytes(URL url) throws IOException {
      HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
      httpURLConnection.setDoInput(true);
      httpURLConnection.setDoOutput(false);
      httpURLConnection.setConnectTimeout(10000);
      httpURLConnection.connect();
      if (httpURLConnection.getResponseCode() == 200) {
         InputStream input = httpURLConnection.getInputStream();

         byte[] var4;
         try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            try {
               IOUtil.copy(input, output);
               var4 = output.toByteArray();
            } catch (Throwable var8) {
               try {
                  output.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }

               throw var8;
            }

            output.close();
         } catch (Throwable var9) {
            if (input != null) {
               try {
                  input.close();
               } catch (Throwable var6) {
                  var9.addSuppressed(var6);
               }
            }

            throw var9;
         }

         if (input != null) {
            input.close();
         }

         return var4;
      } else {
         throw new IOException("" + httpURLConnection.getResponseCode());
      }
   }

   public Signal run(Void unused) {
      File output = new File(this.librariesFolder, this.library.getFileName());
      File tmp = new File(this.tempLibrariesFolder, this.library.getFileName());
      byte[] bytes = null;
      List<Exception> exceptions = new ArrayList();

      for(String repository : PluginLoader.repositories) {
         String downloadUrl = repository + this.library.getDownloadUrl();
         LoggerProvider.getLogger().debug("Downloading from " + downloadUrl);

         try {
            bytes = getBytes(new URL(downloadUrl));
            break;
         } catch (Exception t) {
            String cause = String.format("Download from %s failed.", downloadUrl);
            exceptions.add(new InitialFailedException(cause, t));
         }
      }

      if (bytes == null) {
         String cause = String.format("Unable to download file %s.", this.library.getFileName());
         exceptions.forEach((e) -> LoggerProvider.getLogger().error((Throwable)(new InitialFailedException(cause, e))));
         return Signal.TERMINATED;
      } else {
         try {
            if (!tmp.exists()) {
               Files.createFile(tmp.toPath());
            } else {
               FileWriter fw = new FileWriter(tmp);

               try {
                  fw.write("");
                  fw.flush();
               } catch (Throwable var12) {
                  try {
                     fw.close();
                  } catch (Throwable var11) {
                     var12.addSuppressed(var11);
                  }

                  throw var12;
               }

               fw.close();
            }

            if (output.exists()) {
               Files.delete(output.toPath());
            }

            Files.write(tmp.toPath(), bytes, new OpenOption[0]);
            Files.move(tmp.toPath(), output.toPath());
            LoggerProvider.getLogger().info("Downloaded " + output.getName());
         } catch (Throwable t) {
            LoggerProvider.getLogger().error("Unable to process file " + this.library.getFileName(), t);
            return Signal.TERMINATED;
         }

         return Signal.PASSED;
      }
   }
}
