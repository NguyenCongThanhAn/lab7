package pop3;

import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.table.DefaultTableModel;

public class Pop3UI extends JFrame {
   private Socket socket;
   private BufferedReader in;
   private PrintWriter out;
   private CardLayout cardLayout;
   private JButton btnLogout;
   private JButton btnReadMail;
   private JTextField hostTxt;
   private JPanel inboxPanel;
   private JScrollPane jScrollPane1;
   private JButton loginBtn;
   private JPanel loginPanel;
   private JPanel mainContainer;
   private JPasswordField passwordTf;
   private JTextField portTxt;
   private JButton refreshBtn;
   private JTable tblEmails;
   private JTextField usernameTf;

   public Pop3UI() {
      this.initComponents();
      this.cardLayout = (CardLayout)this.mainContainer.getLayout();
   }

   public void setConnection(Socket socket, BufferedReader in, PrintWriter out) {
      this.socket = socket;
      this.in = in;
      this.out = out;
   }

   public PrintWriter getWriter() {
      return this.out;
   }

   public BufferedReader getReader() {
      return this.in;
   }

   public JPanel getInboxPanel() {
      return this.inboxPanel;
   }

   public void switchPanel(String panelName) {
      this.cardLayout.next(this.mainContainer);
   }

   public void initMailBox() {
      if (this != null) {
         BufferedReader in = this.getReader();
         PrintWriter out = this.getWriter();
         DefaultTableModel tableModel = (DefaultTableModel)this.tblEmails.getModel();
         tableModel.setRowCount(0);

         try {
            out.print("LIST\r\n");
            out.flush();
            String firstResponse = in.readLine();
            System.out.println("Server LIST Status: " + firstResponse);
            String line;
            if (firstResponse != null && firstResponse.startsWith("+OK")) {
               while((line = in.readLine()) != null && !line.equals(".")) {
                  String[] mailInfo = line.split("\\s+");
                  if (mailInfo.length >= 2) {
                     String id = mailInfo[0];
                     String size = mailInfo[1] + " Bytes";
                     tableModel.addRow(new Object[]{id, size});
                  }
               }
            } else {
               JOptionPane.showMessageDialog(this, "Không thể lấy danh sách thư từ máy chủ!", "Lỗi", 0);
            }
         } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Lỗi đọc dữ liệu Socket: " + e.getMessage(), "Lỗi mạng", 0);
         }

      }
   }

   private void initComponents() {
      this.mainContainer = new JPanel();
      this.loginPanel = new JPanel();
      this.usernameTf = new JTextField();
      this.passwordTf = new JPasswordField();
      this.loginBtn = new JButton();
      this.hostTxt = new JTextField();
      this.portTxt = new JTextField();
      this.inboxPanel = new JPanel();
      this.jScrollPane1 = new JScrollPane();
      this.tblEmails = new JTable();
      this.refreshBtn = new JButton();
      this.btnReadMail = new JButton();
      this.btnLogout = new JButton();
      this.setDefaultCloseOperation(0);
      this.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent evt) {
            Pop3UI.this.formWindowClosing(evt);
         }
      });
      this.mainContainer.setLayout(new CardLayout());
      this.usernameTf.setText("receiver@example.com");
      this.passwordTf.setText("receiver123");
      this.loginBtn.setText("Login");
      this.loginBtn.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            Pop3UI.this.loginBtnActionPerformed(evt);
         }
      });
      this.hostTxt.setText("localhost");
      this.portTxt.setText("3110");
      GroupLayout loginPanelLayout = new GroupLayout(this.loginPanel);
      this.loginPanel.setLayout(loginPanelLayout);
      loginPanelLayout.setHorizontalGroup(loginPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(loginPanelLayout.createSequentialGroup().addContainerGap().addGroup(loginPanelLayout.createParallelGroup(Alignment.LEADING).addComponent(this.loginBtn, -1, -1, 32767).addComponent(this.hostTxt).addComponent(this.portTxt).addComponent(this.usernameTf).addComponent(this.passwordTf, -1, 388, 32767)).addContainerGap()));
      loginPanelLayout.setVerticalGroup(loginPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(loginPanelLayout.createSequentialGroup().addContainerGap().addComponent(this.hostTxt, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.portTxt, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.usernameTf, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.passwordTf, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED, 159, 32767).addComponent(this.loginBtn).addContainerGap()));
      this.mainContainer.add(this.loginPanel, "card2");
      this.tblEmails.setModel(new DefaultTableModel(new Object[0][], new String[]{"STT", "Kích thước"}) {
         Class[] types = new Class[]{Integer.class, String.class};
         boolean[] canEdit = new boolean[]{false, false};

         public Class getColumnClass(int columnIndex) {
            return this.types[columnIndex];
         }

         public boolean isCellEditable(int rowIndex, int columnIndex) {
            return this.canEdit[columnIndex];
         }
      });
      this.jScrollPane1.setViewportView(this.tblEmails);
      this.refreshBtn.setText("Làm mới");
      this.refreshBtn.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            Pop3UI.this.refreshBtnActionPerformed(evt);
         }
      });
      this.btnReadMail.setText("Xem nội dung thư");
      this.btnReadMail.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            Pop3UI.this.btnReadMailActionPerformed(evt);
         }
      });
      this.btnLogout.setText("Đăng Xuất");
      this.btnLogout.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            Pop3UI.this.btnLogoutActionPerformed(evt);
         }
      });
      GroupLayout inboxPanelLayout = new GroupLayout(this.inboxPanel);
      this.inboxPanel.setLayout(inboxPanelLayout);
      inboxPanelLayout.setHorizontalGroup(inboxPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(inboxPanelLayout.createSequentialGroup().addContainerGap().addGroup(inboxPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(inboxPanelLayout.createSequentialGroup().addComponent(this.refreshBtn).addPreferredGap(ComponentPlacement.RELATED, 59, 32767).addComponent(this.btnReadMail).addGap(42, 42, 42).addComponent(this.btnLogout)).addComponent(this.jScrollPane1, -2, 0, 32767)).addContainerGap()));
      inboxPanelLayout.setVerticalGroup(inboxPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(inboxPanelLayout.createSequentialGroup().addContainerGap().addComponent(this.jScrollPane1, -1, 247, 32767).addGap(18, 18, 18).addGroup(inboxPanelLayout.createParallelGroup(Alignment.BASELINE).addComponent(this.refreshBtn).addComponent(this.btnReadMail).addComponent(this.btnLogout)).addContainerGap()));
      this.mainContainer.add(this.inboxPanel, "card3");
      GroupLayout layout = new GroupLayout(this.getContentPane());
      this.getContentPane().setLayout(layout);
      layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(this.mainContainer, -1, -1, 32767));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(this.mainContainer, -1, -1, 32767));
      this.pack();
   }

   private void loginBtnActionPerformed(ActionEvent evt) {
      String host = this.hostTxt.getText().trim();
      String portText = this.portTxt.getText().trim();
      String username = this.usernameTf.getText().trim();
      String password = new String(this.passwordTf.getPassword());
      if (!host.isEmpty() && !portText.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
         int port;
         try {
            port = Integer.parseInt(portText);
         } catch (NumberFormatException var15) {
            JOptionPane.showMessageDialog(this, "Cổng (Port) phải là một số nguyên hợp lệ!", "Lỗi", 0);
            return;
         }

         Socket socket = null;
         BufferedReader in = null;
         PrintWriter out = null;

         try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            String welcomeMessage = in.readLine();
            System.out.println("Server Welcome: " + welcomeMessage);
            if (welcomeMessage == null || !welcomeMessage.startsWith("+OK")) {
               throw new IOException("Server không phản hồi đúng chuẩn POP3.");
            }

            out.print("USER " + username + "\r\n");
            out.flush();
            String resUser = in.readLine();
            System.out.println("Server USER: " + resUser);
            if (resUser == null || !resUser.startsWith("+OK")) {
               JOptionPane.showMessageDialog(this, "Tài khoản không tồn tại trên Server!", "Lỗi xác thực", 0);
               socket.close();
               return;
            }

            out.print("PASS " + password + "\r\n");
            out.flush();
            String resPass = in.readLine();
            System.out.println("Server PASS: " + resPass);
            if (resPass != null && resPass.startsWith("+OK")) {
               JOptionPane.showMessageDialog(this, "Đăng nhập thành công!", "Thông báo", 1);
               System.out.println(this != null);
               if (this != null) {
                  this.setConnection(socket, in, out);
                  this.switchPanel("inboxPanel");
                  this.initMailBox();
               }
            } else {
               JOptionPane.showMessageDialog(this, "Sai mật khẩu! Vui lòng kiểm tra lại.", "Lỗi xác thực", 0);
               socket.close();
            }
         } catch (ConnectException var16) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến máy chủ! Sai Host hoặc Server chưa bật.", "Lỗi kết nối", 0);
         } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Lỗi truyền tin Socket: " + e.getMessage(), "Lỗi Hệ Thống", 0);

            try {
               if (socket != null && !socket.isClosed()) {
                  socket.close();
               }
            } catch (IOException ex) {
               ex.printStackTrace();
            }
         }

      } else {
         JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin kết nối!", "Cảnh báo", 2);
      }
   }

   private void formWindowClosing(WindowEvent evt) {
      try {
         if (this.out != null && this.socket != null && !this.socket.isClosed()) {
            this.out.print("QUIT\r\n");
            this.out.flush();
            this.socket.close();
         }
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         System.exit(0);
      }

   }

   private void btnReadMailActionPerformed(ActionEvent evt) {
      int selectedRow = this.tblEmails.getSelectedRow();
      if (selectedRow == -1) {
         JOptionPane.showMessageDialog(this, "Vui lòng chọn một email từ danh sách để đọc!", "Thông báo", 2);
      } else {
         String mailId = this.tblEmails.getValueAt(selectedRow, 0).toString();
         if (this != null) {
            BufferedReader in = this.getReader();
            PrintWriter out = this.getWriter();

            try {
               out.print("RETR " + mailId + "\r\n");
               out.flush();
               String response = in.readLine();
               if (response != null && response.startsWith("+OK")) {
                  StringBuilder emailContent = new StringBuilder();

                  String line;
                  while((line = in.readLine()) != null && !line.equals(".")) {
                     emailContent.append(line).append("\n");
                  }

                  JTextArea textArea = new JTextArea(emailContent.toString());
                  textArea.setEditable(false);
                  textArea.setRows(15);
                  textArea.setColumns(40);
                  JScrollPane scrollPane = new JScrollPane(textArea);
                  JOptionPane.showMessageDialog(this, scrollPane, "Nội dung Email số " + mailId, -1);
               } else {
                  JOptionPane.showMessageDialog(this, "Không thể tải email này từ Server!", "Lỗi", 0);
               }
            } catch (IOException e) {
               JOptionPane.showMessageDialog(this, "Lỗi kết nối Socket: " + e.getMessage(), "Lỗi mạng", 0);
            }

         }
      }
   }

   private void refreshBtnActionPerformed(ActionEvent evt) {
      this.initMailBox();
   }

   private void btnLogoutActionPerformed(ActionEvent evt) {
      Pop3UI parentFrame = this;
      if (this != null) {
         try {
            parentFrame.getWriter().print("QUIT\r\n");
            parentFrame.getWriter().flush();
            parentFrame.getReader().close();
            parentFrame.getWriter().close();
         } catch (Exception e) {
            e.printStackTrace();
         } finally {
            this.setConnection((Socket)null, (BufferedReader)null, (PrintWriter)null);
            this.switchPanel("loginPanel");
         }

      }
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
         Logger.getLogger(Pop3UI.class.getName()).log(Level.SEVERE, (String)null, ex);
      } catch (InstantiationException ex) {
         Logger.getLogger(Pop3UI.class.getName()).log(Level.SEVERE, (String)null, ex);
      } catch (IllegalAccessException ex) {
         Logger.getLogger(Pop3UI.class.getName()).log(Level.SEVERE, (String)null, ex);
      } catch (UnsupportedLookAndFeelException ex) {
         Logger.getLogger(Pop3UI.class.getName()).log(Level.SEVERE, (String)null, ex);
      }

      EventQueue.invokeLater(new Runnable() {
         public void run() {
            (new Pop3UI()).setVisible(true);
         }
      });
   }
}
