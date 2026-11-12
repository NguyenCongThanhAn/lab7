package GUI;

import ansida.lab7.EmailStorageManager;
import callback.ComposeCallback;
import callback.MailboxCallback;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.table.DefaultTableModel;
import protocol.AutoMailProtocolClient;
import protocol.EmailRecord;

public class MailClient extends JFrame {
   private String currentUserEmail = null;
   private String currentAppPassword = null;
   private AutoMailProtocolClient networkClient = null;
   private DefaultTableModel emailTableModel;
   private List<EmailRecord> cachedEmailList = new ArrayList();
   private CardLayout cardLayout;
   private JButton btnDraft;
   private JButton btnLogout;
   private JButton btnNew;
   private JButton btnRefresh;
   private ComposePanel composePanel1;
   private LoginPanel loginPanel1;
   private MailViewPanel mailViewPanel1;
   private MailboxPanel mailboxPanel1;
   private JPanel mainContainer;
   private JPanel mainPanel1;

   public MailClient() {
      this.initComponents();
      this.cardLayout = (CardLayout)this.mainContainer.getLayout();
      this.loginPanel1.setLoginCallback((email, appPassword) -> {
         this.currentUserEmail = email;
         this.currentAppPassword = appPassword;
         this.cardLayout.show(this.mainContainer, "card2");
         this.mainContainer.revalidate();
         this.mainContainer.repaint();
         List<EmailRecord> cachedMails = EmailStorageManager.getEmailsForUser(this.currentUserEmail);
         this.mailboxPanel1.setEmailList(cachedMails);
         this.fetchAndRefreshInbox();
      });
      this.composePanel1.setComposeCallback(new ComposeCallback() {
         public void onSendEmailRequested(String toEmail, String subject, String body) {
            if (!toEmail.trim().isEmpty() && !subject.trim().isEmpty()) {
               System.out.println("[JFrame] Đang tiến hành gửi mail tới: " + toEmail);
               (new Thread(() -> {
                  try (AutoMailProtocolClient smtpClient = new AutoMailProtocolClient()) {
                     smtpClient.autoConnectSMTP(MailClient.this.currentUserEmail);
                     smtpClient.sendEmail(MailClient.this.currentUserEmail, MailClient.this.currentAppPassword, toEmail, subject, body);
                     SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(MailClient.this, "Đã gửi email thành công!", "Thành công", 1);
                        MailClient.this.cardLayout.show(MailClient.this.mainContainer, "card2");
                        MailClient.this.composePanel1.clearFields();
                        MailClient.this.mainContainer.revalidate();
                        MailClient.this.mainContainer.repaint();
                     });
                  } catch (Exception e) {
                     SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MailClient.this, "Gửi email thất bại! Lỗi: " + e.getMessage(), "Lỗi kết nối", 0));
                     e.printStackTrace();
                  }

               })).start();
            } else {
               JOptionPane.showMessageDialog(MailClient.this, "Vui lòng điền đầy đủ Người nhận và Tiêu đề!", "Thông báo", 2);
            }
         }

         public void onDraftRequest() {
            MailClient.this.cardLayout.show(MailClient.this.mainContainer, "card2");
         }
      });
      this.mailboxPanel1.setCallback(new MailboxCallback() {
         public void onOpenMail(EmailRecord email) {
            MailClient.this.mailViewPanel1.clearFields();
            MailClient.this.mailViewPanel1.setEmailDetails(email);
            MailClient.this.cardLayout.show(MailClient.this.mainContainer, "card4");
         }

         public void onDeleteMail(EmailRecord email) {
            throw new UnsupportedOperationException("Not supported yet.");
         }
      });
      this.mailViewPanel1.setViewCallback(() -> this.cardLayout.show(this.mainContainer, "card2"));
   }

   private void fetchAndRefreshInbox() {
      (new Thread(() -> {
         System.out.println(">>> Đang bắt đầu tiến trình tải thư mới ngầm...");

         try {
            try (AutoMailProtocolClient popClient = new AutoMailProtocolClient()) {
               popClient.autoConnectPOP3(this.currentUserEmail);
               popClient.executeCommand("USER " + this.currentUserEmail);
               String passResponse = popClient.executeCommand("PASS " + this.currentAppPassword);
               if (passResponse.startsWith("+OK")) {
                  String statRes = popClient.executeCommand("STAT");
                  String[] statParts = statRes.split(" ");
                  int totalMails = Integer.parseInt(statParts[1]);
                  System.out.println("[+] Tìm thấy tổng cộng " + totalMails + " thư trên Mail Server.");

                  for(int i = 1; i <= Math.min(totalMails, 10); ++i) {
                     String rawEmailContent = popClient.receiveBulkData("RETR " + i);
                     EmailStorageManager.processAndStoreEmail(rawEmailContent);
                  }

                  popClient.executeCommand("QUIT");
                  SwingUtilities.invokeLater(() -> {
                     List<EmailRecord> updatedList = EmailStorageManager.getEmailsForUser(this.currentUserEmail);
                     this.mailboxPanel1.setEmailList(updatedList);
                     System.out.println("[GUI Info] Đã làm mới danh sách hộp thư thành công.");
                  });
                  return;
               }

               System.err.println("[-] Đăng nhập POP3 thất bại. Kiểm tra lại App Password.");
            }

         } catch (Exception e) {
            System.err.println("[-] Lỗi xảy ra trong quá trình nhận thư: " + e.getMessage());
            e.printStackTrace();
         }
      })).start();
   }

   private void initComponents() {
      this.mainContainer = new JPanel();
      this.loginPanel1 = new LoginPanel();
      this.mainPanel1 = new JPanel();
      this.btnNew = new JButton();
      this.btnDraft = new JButton();
      this.btnLogout = new JButton();
      this.btnRefresh = new JButton();
      this.mailboxPanel1 = new MailboxPanel();
      this.composePanel1 = new ComposePanel();
      this.mailViewPanel1 = new MailViewPanel();
      this.setDefaultCloseOperation(3);
      this.mainContainer.setLayout(new CardLayout());
      this.loginPanel1.setName("loginPanel");
      this.mainContainer.add(this.loginPanel1, "card1");
      this.btnNew.setText("New Mail");
      this.btnNew.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            MailClient.this.btnNewActionPerformed(evt);
         }
      });
      this.btnDraft.setText("Draft");
      this.btnDraft.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            MailClient.this.btnDraftActionPerformed(evt);
         }
      });
      this.btnLogout.setText("Log out");
      this.btnLogout.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            MailClient.this.btnLogoutActionPerformed(evt);
         }
      });
      this.btnRefresh.setText("Refresh");
      this.btnRefresh.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            MailClient.this.btnRefreshActionPerformed(evt);
         }
      });
      GroupLayout mainPanel1Layout = new GroupLayout(this.mainPanel1);
      this.mainPanel1.setLayout(mainPanel1Layout);
      mainPanel1Layout.setHorizontalGroup(mainPanel1Layout.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING, mainPanel1Layout.createSequentialGroup().addGroup(mainPanel1Layout.createParallelGroup(Alignment.LEADING).addGroup(mainPanel1Layout.createSequentialGroup().addGap(0, 1, 32767).addComponent(this.btnNew)).addComponent(this.btnRefresh, -1, -1, 32767).addComponent(this.btnDraft, -1, -1, 32767).addComponent(this.btnLogout, -1, -1, 32767)).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.mailboxPanel1, -2, 334, -2)));
      mainPanel1Layout.setVerticalGroup(mainPanel1Layout.createParallelGroup(Alignment.LEADING).addGroup(mainPanel1Layout.createSequentialGroup().addContainerGap(-1, 32767).addComponent(this.btnNew).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.btnDraft).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.btnLogout).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.btnRefresh).addContainerGap()).addComponent(this.mailboxPanel1, -1, -1, 32767));
      this.mainContainer.add(this.mainPanel1, "card2");
      this.mainContainer.add(this.composePanel1, "card3");
      this.mainContainer.add(this.mailViewPanel1, "card4");
      GroupLayout layout = new GroupLayout(this.getContentPane());
      this.getContentPane().setLayout(layout);
      layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(this.mainContainer, -1, -1, 32767).addContainerGap()));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addComponent(this.mainContainer, -1, -1, 32767).addContainerGap()));
      this.pack();
   }

   private void btnNewActionPerformed(ActionEvent evt) {
      this.composePanel1.clearFields();
      this.cardLayout.show(this.mainContainer, "card3");
   }

   private void btnRefreshActionPerformed(ActionEvent evt) {
      this.fetchAndRefreshInbox();
   }

   private void btnDraftActionPerformed(ActionEvent evt) {
      this.cardLayout.show(this.mainContainer, "card3");
   }

   private void btnLogoutActionPerformed(ActionEvent evt) {
      this.cardLayout.show(this.mainContainer, "card1");
   }

   public static void main(String[] args) {
      try {
         for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
               UIManager.setLookAndFeel(info.getClassName());
               break;
            }
         }
      } catch (ClassNotFoundException ex) {
         Logger.getLogger(MailClient.class.getName()).log(Level.SEVERE, (String)null, ex);
      } catch (InstantiationException ex) {
         Logger.getLogger(MailClient.class.getName()).log(Level.SEVERE, (String)null, ex);
      } catch (IllegalAccessException ex) {
         Logger.getLogger(MailClient.class.getName()).log(Level.SEVERE, (String)null, ex);
      } catch (UnsupportedLookAndFeelException ex) {
         Logger.getLogger(MailClient.class.getName()).log(Level.SEVERE, (String)null, ex);
      }

      EventQueue.invokeLater(new Runnable() {
         public void run() {
             FlatLightLaf.setup();
            (new MailClient()).setVisible(true);
         }
      });
   }
}
