package GUI;

import callback.ComposeCallback;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class ComposePanel extends JPanel {
   private ComposeCallback composeCallback;
   private JButton btnDraft;
   private JButton btnSend;
   private JLabel jLabel1;
   private JLabel jLabel2;
   private JLabel jLabel3;
   private JScrollPane jScrollPane1;
   private JTextArea txtAreaBody;
   private JTextField txtSubject;
   private JTextField txtTo;

   public void setComposeCallback(ComposeCallback callback) {
      this.composeCallback = callback;
   }

   public void clearFields() {
      this.txtTo.setText("");
      this.txtSubject.setText("");
      this.txtAreaBody.setText("");
   }

   public ComposePanel() {
      this.initComponents();
   }

   private void initComponents() {
      this.txtTo = new JTextField();
      this.txtSubject = new JTextField();
      this.jScrollPane1 = new JScrollPane();
      this.txtAreaBody = new JTextArea();
      this.btnSend = new JButton();
      this.btnDraft = new JButton();
      this.jLabel1 = new JLabel();
      this.jLabel2 = new JLabel();
      this.jLabel3 = new JLabel();
      this.txtAreaBody.setColumns(20);
      this.txtAreaBody.setRows(5);
      this.jScrollPane1.setViewportView(this.txtAreaBody);
      this.btnSend.setText("Send");
      this.btnSend.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            ComposePanel.this.btnSendActionPerformed(evt);
         }
      });
      this.btnDraft.setText("Draft");
      this.btnDraft.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            ComposePanel.this.btnDraftActionPerformed(evt);
         }
      });
      this.jLabel1.setText("To: ");
      this.jLabel2.setText("Subject");
      this.jLabel3.setText("Body: ");
      GroupLayout layout = new GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING, layout.createSequentialGroup().addComponent(this.btnDraft).addPreferredGap(ComponentPlacement.RELATED, 264, 32767).addComponent(this.btnSend)).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(this.jLabel1).addComponent(this.jLabel2).addComponent(this.jLabel3)).addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(this.jScrollPane1, Alignment.TRAILING).addComponent(this.txtSubject).addComponent(this.txtTo)))).addContainerGap()));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.txtTo, -2, -1, -2).addComponent(this.jLabel1)).addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.txtSubject, -2, -1, -2).addComponent(this.jLabel2)).addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(this.jScrollPane1, -2, 203, -2).addComponent(this.jLabel3)).addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.btnSend).addComponent(this.btnDraft)).addContainerGap()));
   }

   private void btnSendActionPerformed(ActionEvent evt) {
      String toEmail = this.txtTo.getText().trim();
      String subject = this.txtSubject.getText().trim();
      String body = this.txtAreaBody.getText();
      if (!toEmail.isEmpty() && !subject.isEmpty() && !body.isEmpty()) {
         if (!toEmail.contains("@")) {
            JOptionPane.showMessageDialog(this, "Địa chỉ email người nhận không hợp lệ!");
         } else {
            if (this.composeCallback != null) {
               this.composeCallback.onSendEmailRequested(toEmail, subject, body);
            }

         }
      } else {
         JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ Người nhận, Tiêu đề và Nội dung!");
      }
   }

   private void btnDraftActionPerformed(ActionEvent evt) {
      this.composeCallback.onDraftRequest();
   }
}
