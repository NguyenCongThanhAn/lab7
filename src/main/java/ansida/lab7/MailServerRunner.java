package ansida.lab7;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

public class MailServerRunner {
   public static void main(String[] args) {
      GreenMail greenMail = new GreenMail(ServerSetupTest.SMTP_POP3);
      System.out.println("Đang khởi động GreenMail Server...");
      greenMail.start();
      System.out.println("SMTP Server đang chạy tại cổng: " + ServerSetupTest.SMTP.getPort());
      System.out.println("POP3 Server đang chạy tại cổng: " + ServerSetupTest.POP3.getPort());
      String senderEmail = "sender@example.com";
      String sUsername = "sender@example.com";
      String sPass = "sender123";
      String receiverEmail = "receiver@example.com";
      String rUsername = "receiver@example.com";
      String rPass = "receiver123";
      greenMail.setUser(senderEmail, sUsername, sPass);
      greenMail.setUser(receiverEmail, rUsername, rPass);
      System.out.println("\n[Đã tạo tài khoản test]");
      System.out.println("Sender User/Email: " + sUsername);
      System.out.println("Receiver User/Email: " + rUsername);
      System.out.println("Sender Password: " + sPass);
      System.out.println("Receiver Password: " + rPass);
      System.out.println("\nServer đang sẵn sàng nhận kết nối Socket từ xa. Nhấn Ctrl+C để tắt.");

      while(true) {
         try {
            Thread.sleep(1000L);
         } catch (InterruptedException var11) {
            greenMail.stop();
            return;
         }
      }
   }
}
