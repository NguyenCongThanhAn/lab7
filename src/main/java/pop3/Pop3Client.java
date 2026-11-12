package pop3;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Pop3Client {
   private static final String CRLF = "\r\n";

   public static void main(String[] args) {
      String mailServer = "localhost";
      int port = 3110;
      String username = "receiver@example.com";
      String password = "receiver123";

      try {
         Socket socket = new Socket(mailServer, port);

         try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            try {
               DataOutputStream writer = new DataOutputStream(socket.getOutputStream());

               try {
                  System.out.println("Server: " + reader.readLine());
                  sendCmd(writer, "USER " + username);
                  System.out.println("Server: " + reader.readLine());
                  sendCmd(writer, "PASS " + password);
                  System.out.println("Server: " + reader.readLine());
                  sendCmd(writer, "STAT");
                  String statResponse = reader.readLine();
                  System.out.println("Server: " + statResponse);
                  if (statResponse.startsWith("+OK")) {
                     String[] parts = statResponse.split(" ");
                     int totalMails = Integer.parseInt(parts[1]);
                     if (totalMails <= 0) {
                        System.out.println("Hộp thư trống.");
                     } else {
                        System.out.println("\n--- Đang tải nội dung Email số 1 ---");
                        sendCmd(writer, "RETR 1");
                        String line = reader.readLine();
                        System.out.println("Server: " + line);

                        while((line = reader.readLine()) != null && !line.equals(".")) {
                           System.out.println(line);
                        }

                        System.out.println("--- Kết thúc Email ---\n");
                     }
                  }

                  sendCmd(writer, "QUIT");
                  System.out.println("Server: " + reader.readLine());
               } catch (Throwable var15) {
                  try {
                     writer.close();
                  } catch (Throwable var14) {
                     var15.addSuppressed(var14);
                  }

                  throw var15;
               }

               writer.close();
            } catch (Throwable var16) {
               try {
                  reader.close();
               } catch (Throwable var13) {
                  var16.addSuppressed(var13);
               }

               throw var16;
            }

            reader.close();
         } catch (Throwable var17) {
            try {
               socket.close();
            } catch (Throwable var12) {
               var17.addSuppressed(var12);
            }

            throw var17;
         }

         socket.close();
      } catch (Exception e) {
         System.err.println("Lỗi kết nối hoặc xử lý dữ liệu: " + e.getMessage());
         e.printStackTrace();
      }

   }

   private static void sendCmd(DataOutputStream writer, String cmd) throws Exception {
      System.out.println("Client: " + cmd);
      writer.writeBytes(cmd + "\r\n");
      writer.flush();
   }
}
