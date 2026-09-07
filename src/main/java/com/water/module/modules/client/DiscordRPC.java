package com.water.module.modules.client;

import com.water.module.Category;
import com.water.module.Module;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.MinecraftClient;

public final class DiscordRPC extends Module {
   private ScheduledExecutorService field_a_1;
   private volatile boolean g = false;
   private volatile long field_a_2 = 0L;
   private volatile long b = 0L;
   private Socket field_a_3;
   private OutputStream field_a_4;
   private InputStream field_a_5;

   public DiscordRPC() {
      super("DiscordRPC", Category.e);
   }

   @Override
   public void onEnable() {
      if (this.field_a_1 == null || this.field_a_1.isShutdown()) {
         if (this.field_a_2 == 0L) {
            this.field_a_2 = System.currentTimeMillis() / 1000L;
         }

         this.field_a_1 = Executors.newSingleThreadScheduledExecutor(r -> {
            Runnable var1 = new Thread(r, "water-drpc");
            var1.setDaemon(true);
            return var1;
         });
         this.field_a_1.submit(this::i);
         this.field_a_1.scheduleAtFixedRate(this::l, 5L, 15L, TimeUnit.SECONDS);
      }
   }

   @Override
   public void onTick() {
      if (this.isEnabled()) {
         if (this.field_a_1 == null || this.field_a_1.isShutdown()) {
            this.onEnable();
         }
      }
   }

   @Override
   public void onDisable() {
      if (this.field_a_1 != null) {
         this.field_a_1.shutdownNow();
      }

      this.m();
      this.j();
   }

   private void i() {
      long var1 = System.currentTimeMillis();
      if (!this.g && var1 - this.b >= 5000L) {
         this.b = var1;
         this.j();

         for (int var4 = 0; var4 <= 9; var4++) {
            try {
               String var2 = this.a(var4);
               if (var2 == null) {
                  break;
               }

               this.j(var2);
               if (this.g) {
                  this.k();
                  this.l();
                  return;
               }
            } catch (Exception var3) {
            }
         }
      }
   }

   private String a(int index) {
      String var2 = System.getProperty("os.name", "").toLowerCase();
      if (var2.contains("win")) {
         return "\\\\.\\pipe\\discord-ipc-" + index;
      } else {
         String[] var5 = new String[]{System.getenv("XDG_RUNTIME_DIR"), System.getenv("TMPDIR"), System.getenv("TMP"), System.getenv("TEMP"), "/tmp"};

         for (String var4 : var5) {
            if (var4 != null) {
               File var7 = new File(var4, "discord-ipc-" + index);
               if (var7.exists()) {
                  return var7.getAbsolutePath();
               }
            }
         }

         return null;
      }
   }

   private void j(String path) {
      try {
         String var2 = System.getProperty("os.name", "").toLowerCase();
         if (var2.contains("win")) {
            this.k(path);
         } else {
            this.l(path);
         }
      } catch (Exception var3) {
         this.g = false;
      }
   }

   private void k(String path) throws Exception {
      final String var2 = new RandomAccessFile(path, "rw");
      this.field_a_4 = new FileOutputStream(var2.getFD());
      this.field_a_5 = new InputStream() {
         @Override
         public int read() throws IOException {
            return var2.read();
         }

         @Override
         public int read(byte[] b, int off, int len) throws IOException {
            return var2.read(b, off, len);
         }
      };
      this.g = true;
   }

   private void l(String path) throws Exception {
      try {
         Class var2 = Class.forName("java.net.UnixDomainSocketAddress");
         String var5 = var2.getMethod("of", String.class).invoke(null, path);
         var2 = Class.forName("java.nio.channels.SocketChannel");
         Object var3 = var2.getMethod("open", Class.forName("java.net.ProtocolFamily"))
            .invoke(null, Enum.valueOf(Class.forName("java.net.StandardProtocolFamily"), "UNIX"));
         var2.getMethod("connect", Class.forName("java.net.SocketAddress")).invoke(var3, var5);
         this.field_a_3 = (Socket)var2.getMethod("socket").invoke(var3);
         this.field_a_4 = this.field_a_3.getOutputStream();
         this.field_a_5 = this.field_a_3.getInputStream();
         this.g = true;
      } catch (Exception var4) {
         this.g = false;
      }
   }

   private void j() {
      this.g = false;

      try {
         if (this.field_a_4 != null) {
            this.field_a_4.close();
         }
      } catch (Exception var3) {
      }

      try {
         if (this.field_a_5 != null) {
            this.field_a_5.close();
         }
      } catch (Exception var2) {
      }

      try {
         if (this.field_a_3 != null) {
            this.field_a_3.close();
         }
      } catch (Exception var1) {
      }

      this.field_a_4 = null;
      this.field_a_5 = null;
      this.field_a_3 = null;
   }

   private void k() throws Exception {
      String var1 = "{\"v\":1,\"client_id\":\"1529221242077450381\"}";
      this.a(0, var1);
   }

   private void a(int opcode, String json) throws Exception {
      String var5 = json.getBytes(StandardCharsets.UTF_8);
      byte[] var3 = new byte[8];
      var3[0] = (byte)(opcode & 0xFF);
      var3[1] = (byte)(opcode >> 8 & 0xFF);
      var3[2] = (byte)(opcode >> 16 & 0xFF);
      var3[3] = (byte)(opcode >> 24 & 0xFF);
      opcode = var5.length;
      var3[4] = (byte)(opcode & 0xFF);
      var3[5] = (byte)(opcode >> 8 & 0xFF);
      var3[6] = (byte)(opcode >> 16 & 0xFF);
      var3[7] = (byte)(opcode >> 24 & 0xFF);
      this.field_a_4.write(var3);
      this.field_a_4.write(var5);
      this.field_a_4.flush();
   }

   private void l() {
      if (!this.g) {
         this.i();
      } else {
         try {
            MinecraftClient var1 = MinecraftClient.getInstance();
            String var2 = "Playing on Water Client";
            String var5 = var1 != null && var1.getCurrentServerEntry() != null ? var1.getCurrentServerEntry().address : "Singleplayer";
            String var3 = String.valueOf(System.currentTimeMillis());
            String var6 = "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":"
               + ProcessHandle.current().pid()
               + ",\"activity\":{\"details\":\""
               + c(var2)
               + "\",\"state\":\""
               + c(var5)
               + "\",\"timestamps\":{\"start\":"
               + this.field_a_2
               + "},\"assets\":{\"large_image\":\"content\",\"large_text\":\"Water Client\",\"small_image\":\"minecraft\",\"small_text\":\"Minecraft\"}}},\"nonce\":\""
               + var3
               + "\"}";
            this.a(1, var6);
         } catch (Exception var4) {
            this.j();
         }
      }
   }

   private void m() {
      if (this.g) {
         try {
            String var1 = String.valueOf(System.currentTimeMillis());
            var1 = "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":" + ProcessHandle.current().pid() + ",\"activity\":null},\"nonce\":\"" + var1 + "\"}";
            this.a(1, var1);
         } catch (Exception var2) {
         }
      }
   }

   private static String c(String s) {
      return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
   }
}
