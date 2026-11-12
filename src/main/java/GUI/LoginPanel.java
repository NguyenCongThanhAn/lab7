package GUI;

import callback.LoginCallback;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import protocol.AutoMailProtocolClient;

public class LoginPanel extends JPanel {
   private LoginCallback loginCallback;
   private JButton btnLogin;
   private JLabel jLabel1;
   private JLabel jLabel2;
   private JPasswordField jPasswordField1;
   private JTextField txtEmail;
   private JPasswordField txtPassword;

   public LoginPanel() {
      this.initComponents();
   }

   public void setLoginCallback(LoginCallback callback) {
      this.loginCallback = callback;
   }

   private void initComponents() {
      this.jPasswordField1 = new JPasswordField();
      this.txtEmail = new JTextField();
      this.txtPassword = new JPasswordField();
      this.btnLogin = new JButton();
      this.jLabel1 = new JLabel();
      this.jLabel2 = new JLabel();
      this.jPasswordField1.setText("jPasswordField1");
      this.btnLogin.setText("Login");
      this.btnLogin.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            LoginPanel.this.btnLoginActionPerformed(evt);
         }
      });
      this.jLabel1.setText("Email:");
      this.jLabel2.setText("Password: ");
      GroupLayout layout = new GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(61, 61, 61).addGroup(layout.createParallelGroup(Alignment.TRAILING).addComponent(this.jLabel1).addComponent(this.jLabel2)).addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(this.txtPassword, -2, 178, -2).addComponent(this.txtEmail, -2, 178, -2)).addContainerGap(100, 32767)).addGroup(Alignment.TRAILING, layout.createSequentialGroup().addContainerGap(-1, 32767).addComponent(this.btnLogin).addGap(158, 158, 158)));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING, layout.createSequentialGroup().addGap(53, 53, 53).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.txtEmail, -2, -1, -2).addComponent(this.jLabel1)).addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.txtPassword, -2, -1, -2).addComponent(this.jLabel2)).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.btnLogin).addContainerGap(168, 32767)));
   }

   private void btnLoginActionPerformed(ActionEvent evt) {
      String email = this.txtEmail.getText().trim();
      String password = (new String(this.txtPassword.getPassword())).trim();
      if (!email.isEmpty() && !password.isEmpty()) {
         this.btnLogin.setEnabled(false);
         this.btnLogin.setText("Đang xác thực...");
         (new Thread(() -> {
            try (AutoMailProtocolClient client = new AutoMailProtocolClient()) {
               client.autoConnectPOP3(email);
               client.executeCommand("USER " + email);
               String response = client.executeCommand("PASS " + password);
               if (response != null && response.startsWith("+OK")) {
                  SwingUtilities.invokeLater(() -> {
                     this.btnLogin.setEnabled(true);
                     this.btnLogin.setText("Đăng nhập");
                     if (this.loginCallback != null) {
                        this.loginCallback.onLoginSuccess(email, password);
                     }

                  });
               } else {
                  SwingUtilities.invokeLater(() -> {
                     this.btnLogin.setEnabled(true);
                     this.btnLogin.setText("Đăng nhập");
                     JOptionPane.showMessageDialog(this, "Mật khẩu ứng dụng hoặc Email không chính xác!", "Lỗi đăng nhập", 0);
                  });
               }

               client.executeCommand("QUIT");
            } catch (Exception e) {
               SwingUtilities.invokeLater(() -> {
                  this.btnLogin.setEnabled(true);
                  this.btnLogin.setText("Đăng nhập");
                  JOptionPane.showMessageDialog(this, "Không thể kết nối tới máy chủ Mail! Lỗi: " + e.getMessage(), "Lỗi kết nối", 0);
               });
            }

         })).start();
      } else {
         JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
      }
   }
}
