package protocol;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class MailProtocolClient implements AutoCloseable {
   private SSLSocket socket;
   private BufferedReader reader;
   private PrintWriter writer;
   private boolean debug = true;

   public void connect(String host, int port) throws Exception {
      SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
      this.socket = (SSLSocket)factory.createSocket(host, port);
      this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
      this.writer = new PrintWriter(this.socket.getOutputStream(), true);
      String welcome = this.reader.readLine();
      if (this.debug) {
         System.out.println("SERVER (Chào mừng): " + welcome);
      }

   }

   public String executeCommand(String command) throws Exception {
      if (this.debug) {
         System.out.println("CLIENT: " + command);
      }

      this.writer.println(command);
      String response = this.reader.readLine();
      if (this.debug) {
         System.out.println("SERVER: " + response);
      }

      return response;
   }

   public String executeSmtpEhlo(String command) throws Exception {
      if (this.debug) {
         System.out.println("CLIENT: " + command);
      }

      this.writer.println(command);
      StringBuilder fullResponse = new StringBuilder();

      String line;
      while((line = this.reader.readLine()) != null) {
         fullResponse.append(line).append("\n");
         if (this.debug) {
            System.out.println("SERVER: " + line);
         }

         if (line.length() >= 4 && line.charAt(3) == ' ') {
            break;
         }
      }

      return fullResponse.toString();
   }

   public String sendEmailData(String rawEmailContent) throws Exception {
      if (this.debug) {
         System.out.println("CLIENT: [Đang truyền nội dung email...]");
      }

      this.writer.print(rawEmailContent);
      if (!rawEmailContent.endsWith("\r\n")) {
         this.writer.println();
      }

      if (this.debug) {
         System.out.println("CLIENT: .");
      }

      this.writer.println(".");
      String response = this.reader.readLine();
      if (this.debug) {
         System.out.println("SERVER: " + response);
      }

      return response;
   }

   public String receiveBulkData(String command) throws Exception {
      if (this.debug) {
         System.out.println("CLIENT: " + command);
      }

      this.writer.println(command);
      String status = this.reader.readLine();
      if (this.debug) {
         System.out.println("SERVER: " + status);
      }

      if (status != null && status.startsWith("+OK")) {
         StringBuilder bulkData = new StringBuilder();

         String line;
         while((line = this.reader.readLine()) != null && !line.equals(".")) {
            bulkData.append(line).append("\r\n");
         }

         return bulkData.toString();
      } else {
         return status;
      }
   }

   public void close() throws Exception {
      if (this.reader != null) {
         this.reader.close();
      }

      if (this.writer != null) {
         this.writer.close();
      }

      if (this.socket != null && !this.socket.isClosed()) {
         this.socket.close();
      }

      if (this.debug) {
         System.out.println(">>> Đã đóng Socket kết nối an toàn.");
      }

   }

   public void setDebug(boolean debug) {
      this.debug = debug;
   }
}
