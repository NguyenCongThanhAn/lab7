package GUI;

import callback.ViewCallback;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import protocol.EmailRecord;

public class MailViewPanel extends JPanel {
   private ViewCallback viewCallback;
   private JButton btnBack;
   private JScrollPane jScrollPane1;
   private JLabel lblDate;
   private JLabel lblFrom;
   private JTextArea txtAreaBody;
   private JTextField txtSubject;

   public void setViewCallback(ViewCallback callback) {
      this.viewCallback = callback;
   }

   public void setEmailDetails(EmailRecord email) {
      if (email == null) {
         this.clearFields();
      } else {
         this.lblFrom.setText("Từ: " + email.getSender());
         this.txtSubject.setText(email.getSubject());
         this.lblDate.setText("Ngày: " + email.getReceivedDate());
         this.txtAreaBody.setText(email.getBodyContent());
         this.txtAreaBody.setCaretPosition(0);
      }
   }

   public void clearFields() {
      this.lblFrom.setText("Từ: ");
      this.txtSubject.setText("");
      this.lblDate.setText("Ngày: ");
      this.txtAreaBody.setText("");
   }

   public MailViewPanel() {
      this.initComponents();
   }

   private void initComponents() {
      this.lblFrom = new JLabel();
      this.lblDate = new JLabel();
      this.txtSubject = new JTextField();
      this.jScrollPane1 = new JScrollPane();
      this.txtAreaBody = new JTextArea();
      this.btnBack = new JButton();
      this.lblFrom.setText("From:");
      this.lblDate.setText("Date:");
      this.txtSubject.setEditable(false);
      this.txtSubject.setText("Subject");
      this.txtAreaBody.setEditable(false);
      this.txtAreaBody.setColumns(20);
      this.txtAreaBody.setRows(5);
      this.jScrollPane1.setViewportView(this.txtAreaBody);
      this.btnBack.setText("Back");
      this.btnBack.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            MailViewPanel.this.btnBackActionPerformed(evt);
         }
      });
      GroupLayout layout = new GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(this.jScrollPane1, -1, 388, 32767).addGroup(layout.createSequentialGroup().addComponent(this.lblFrom).addPreferredGap(ComponentPlacement.RELATED, -1, 32767).addComponent(this.btnBack)).addGroup(layout.createSequentialGroup().addComponent(this.lblDate).addGap(0, 0, 32767)).addComponent(this.txtSubject)).addContainerGap()));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.btnBack).addComponent(this.lblFrom)).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.lblDate).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.txtSubject, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.jScrollPane1, -1, 209, 32767).addContainerGap()));
   }

   private void btnBackActionPerformed(ActionEvent evt) {
      this.viewCallback.onBackRequested();
   }
}
