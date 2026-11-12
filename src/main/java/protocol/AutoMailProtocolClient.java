package protocol;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Base64;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class AutoMailProtocolClient implements AutoCloseable {
   private SSLSocket socket;
   private BufferedReader reader;
   private PrintWriter writer;
   private boolean debug = true;

   public void autoConnectSMTP(String emailAddress) throws Exception {
      String domain = this.extractDomain(emailAddress);
      String smtpHost;
      if (domain.equalsIgnoreCase("gmail.com")) {
         smtpHost = "smtp.gmail.com";
      } else if (domain.equalsIgnoreCase("yahoo.com")) {
         smtpHost = "://yahoo.com";
      } else if (!domain.equalsIgnoreCase("live.com") && !domain.equalsIgnoreCase("hotmail.com") && !domain.equalsIgnoreCase("outlook.com")) {
         smtpHost = "smtp." + domain;
      } else {
         smtpHost = "://office365.com";
      }

      System.out.println(">>> Tự động nhận diện SMTP Server: " + smtpHost);
      this.connect(smtpHost, 465);
   }

   public void autoConnectPOP3(String emailAddress) throws Exception {
      String domain = this.extractDomain(emailAddress);
      String popHost;
      if (!domain.equalsIgnoreCase("live.com") && !domain.equalsIgnoreCase("hotmail.com") && !domain.equalsIgnoreCase("outlook.com")) {
         popHost = "pop." + domain;
      } else {
         popHost = "://office365.com";
      }

      System.out.println(">>> Tự động nhận diện POP3 Server: " + popHost);
      this.connect(popHost, 995);
   }

   private void connect(String host, int port) throws Exception {
      SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
      this.socket = (SSLSocket)factory.createSocket(host, port);
      this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
      this.writer = new PrintWriter(this.socket.getOutputStream(), true);
      String welcome = this.reader.readLine();
      if (this.debug) {
         System.out.println("SERVER (Chào mừng): " + welcome);
      }

   }

   private String extractDomain(String email) {
      if (email != null && email.contains("@")) {
         return email.substring(email.indexOf("@") + 1).trim();
      } else {
         throw new IllegalArgumentException("Địa chỉ Email không hợp lệ!");
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
      this.writer.print(rawEmailContent);
      if (!rawEmailContent.endsWith("\r\n")) {
         this.writer.println();
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

   public void sendEmail(String fromEmail, String appPassword, String toEmail, String subject, String body) throws Exception {
      String var10001 = this.extractDomain(fromEmail);
      this.executeSmtpEhlo("EHLO " + var10001);
      this.executeCommand("AUTH LOGIN");
      String base64User = Base64.getEncoder().encodeToString(fromEmail.getBytes("UTF-8"));
      String base64Pass = Base64.getEncoder().encodeToString(appPassword.getBytes("UTF-8"));
      this.executeCommand(base64User);
      String authResponse = this.executeCommand(base64Pass);
      if (!authResponse.startsWith("235")) {
         throw new Exception("Xác thực tài khoản SMTP thất bại! Phản hồi: " + authResponse);
      } else {
         this.executeCommand("MAIL FROM:<" + fromEmail + ">");
         this.executeCommand("RCPT TO:<" + toEmail + ">");
         this.executeCommand("DATA");
         String rawEmailContent = "From: " + fromEmail + "\r\nTo: " + toEmail + "\r\nSubject: " + subject + "\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\n" + body;
         this.sendEmailData(rawEmailContent);
         this.executeCommand("QUIT");
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

      System.out.println(">>> Đã đóng Socket.");
   }
}
