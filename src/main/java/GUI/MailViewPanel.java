package GUI;

import javax.swing.JEditorPane;

public class MailViewPanel extends javax.swing.JPanel {

    private callback.ViewCallback viewCallback;
    // Khai báo thêm JEditorPane để hiển thị HTML
    private JEditorPane editorPaneBody;

    public void setViewCallback(callback.ViewCallback callback) {
        this.viewCallback = callback;
    }

    public MailViewPanel() {
        initComponents();
        setupHtmlViewer(); // Cấu hình trình xem HTML ngay khi khởi tạo
    }

    /**
     * Thay thế JTextArea bằng JEditorPane bên trong jScrollPane1
     * mà không ảnh hưởng đến code tự sinh của NetBeans
     */
    private void setupHtmlViewer() {
        editorPaneBody = new JEditorPane();
        editorPaneBody.setContentType("text/html; charset=UTF-8");
        editorPaneBody.setEditable(false);
        
        // Đưa editorPaneBody vào khung cuộn thay cho txtAreaBody
        jScrollPane1.setViewportView(editorPaneBody);
    }

        public void setEmailDetails(protocol.EmailRecord email) {
        if (email == null) {
            clearFields();
            return;
        }

        // 1. Cập nhật Người gửi (JLabel)
        lblFrom.setText("Từ: " + email.getSender());

        // 2. Cập nhật Tiêu đề (JTextField)
        txtSubject.setText(email.getSubject());

        // 3. Cập nhật Ngày nhận (JLabel)
        lblDate.setText("Ngày: " + email.getReceivedDate());

        // 4. Lấy dữ liệu thô và GỌI BỘ GIẢI MÃ MIME
        String rawContent = email.getBodyContent();
        String content = protocol.MimeParser.parseRawBody(rawContent);

        if (content == null) {
            content = "";
        }

        // Nếu sau khi giải mã vẫn là chữ thuần (không có thẻ html), đổi dấu \n thành <br>
        String lowerContent = content.toLowerCase();
        if (!lowerContent.contains("<html") && !lowerContent.contains("<body") && !lowerContent.contains("<div")) {
            content = "<html><body style='font-family: Sans-Serif; font-size: 12px;'>"
                    + content.replace("\n", "<br>")
                    + "</body></html>";
        }

        // 5. Hiển thị nội dung sạch lên JEditorPane và cuộn lên đầu trang
        editorPaneBody.setText(content);
        editorPaneBody.setCaretPosition(0);
    }


    /**
     * Hàm xóa trống giao diện khi không có email nào được chọn
     */
    public void clearFields() {
        lblFrom.setText("Từ: ");
        txtSubject.setText("");
        lblDate.setText("Ngày: ");
        editorPaneBody.setText("");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        lblFrom = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        txtSubject = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtAreaBody = new javax.swing.JTextArea();
        btnBack = new javax.swing.JButton();

        lblFrom.setText("From:");

        lblDate.setText("Date:");

        txtSubject.setEditable(false);
        txtSubject.setText("Subject");

        txtAreaBody.setEditable(false);
        txtAreaBody.setColumns(20);
        txtAreaBody.setRows(5);
        jScrollPane1.setViewportView(txtAreaBody);

        btnBack.setText("Back");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblFrom)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnBack))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblDate)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(txtSubject))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBack)
                    .addComponent(lblFrom))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtSubject, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>                        

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {                                        
        if (viewCallback != null) {
            viewCallback.onBackRequested();
        }
    }                                       

    // Variables declaration - do not modify                     
    private javax.swing.JButton btnBack;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblFrom;
    private javax.swing.JTextArea txtAreaBody;
    private javax.swing.JTextField txtSubject;
    // End of variables declaration                   
}
