package GUI;

import callback.MailboxCallback;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.GroupLayout.Alignment;
import javax.swing.table.DefaultTableModel;
import protocol.EmailRecord;

public class MailboxPanel extends JPanel {
   private MailboxCallback callback;
   private List<EmailRecord> emailList = new ArrayList();
   private DefaultTableModel tableModel;
   private JScrollPane jScrollPane1;
   private JTable mailTable;

   public MailboxPanel() {
      this.initComponents();
      this.tableModel = (DefaultTableModel)this.mailTable.getModel();
   }

   public void setCallback(MailboxCallback callback) {
      this.callback = callback;
   }

   public void addEmail(EmailRecord email) {
      this.emailList.add(email);
      this.tableModel.addRow(email.toRowData());
   }

   public void setEmailList(List<EmailRecord> list) {
      this.tableModel.setRowCount(0);
      this.emailList.clear();

      for(EmailRecord email : list) {
         this.addEmail(email);
      }

   }

   private void executeOpen() {
      int selectedRow = this.mailTable.getSelectedRow();
      if (selectedRow != -1 && this.callback != null) {
         int modelRow = this.mailTable.convertRowIndexToModel(selectedRow);
         this.callback.onOpenMail((EmailRecord)this.emailList.get(modelRow));
      }

   }

   private void executeDelete() {
      int selectedRow = this.mailTable.getSelectedRow();
      if (selectedRow != -1 && this.callback != null) {
         int modelRow = this.mailTable.convertRowIndexToModel(selectedRow);
         EmailRecord selectedEmail = (EmailRecord)this.emailList.get(modelRow);
         int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa email này?", "Xác nhận", 0);
         if (confirm == 0) {
            this.tableModel.removeRow(modelRow);
            this.emailList.remove(modelRow);
            this.callback.onDeleteMail(selectedEmail);
         }
      }

   }

   private void initComponents() {
      this.jScrollPane1 = new JScrollPane();
      this.mailTable = new JTable();
      this.mailTable.setModel(new DefaultTableModel(new Object[0][], new String[]{"ID", "Người gửi", "Tiêu đề", "Ngày nhận"}) {
         Class[] types = new Class[]{Integer.class, String.class, String.class, String.class};
         boolean[] canEdit = new boolean[]{false, false, false, false};

         public Class getColumnClass(int columnIndex) {
            return this.types[columnIndex];
         }

         public boolean isCellEditable(int rowIndex, int columnIndex) {
            return this.canEdit[columnIndex];
         }
      });
      this.mailTable.setColumnSelectionAllowed(true);
      this.mailTable.getTableHeader().setReorderingAllowed(false);
      this.mailTable.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent evt) {
            MailboxPanel.this.mailTableMouseClicked(evt);
         }
      });
      this.mailTable.addKeyListener(new KeyAdapter() {
         public void keyPressed(KeyEvent evt) {
            MailboxPanel.this.mailTableKeyPressed(evt);
         }
      });
      this.jScrollPane1.setViewportView(this.mailTable);
      this.mailTable.getColumnModel().getSelectionModel().setSelectionMode(0);
      if (this.mailTable.getColumnModel().getColumnCount() > 0) {
         this.mailTable.getColumnModel().getColumn(0).setMinWidth(0);
         this.mailTable.getColumnModel().getColumn(0).setPreferredWidth(0);
         this.mailTable.getColumnModel().getColumn(0).setMaxWidth(0);
      }

      GroupLayout layout = new GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(this.jScrollPane1, -1, 388, 32767).addContainerGap()));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(this.jScrollPane1, -1, 288, 32767).addContainerGap()));
   }

   private void mailTableMouseClicked(MouseEvent evt) {
      if (evt.getClickCount() == 2) {
         this.executeOpen();
      }

   }

   private void mailTableKeyPressed(KeyEvent evt) {
      if (evt.getKeyCode() == 127) {
         this.executeDelete();
      } else if (evt.getKeyCode() == 10) {
         evt.consume();
         this.executeOpen();
      }

   }
}
