package simple_browser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class HttpSocketBrowserGUI extends JFrame {
   private JTextField txtUrl;
   private JComboBox<String> cbMethod;
   private JTextField txtPostData;
   private JEditorPane htmlViewer;
   private JTextArea txtHeaders;
   private DefaultListModel<String> historyListModel;
   private final List<String> history = new ArrayList();

   public HttpSocketBrowserGUI() {
      this.setTitle("Java Socket HTTP Web Browser");
      this.setSize(1000, 700);
      this.setDefaultCloseOperation(3);
      this.setLocationRelativeTo((Component)null);
      JPanel topPanel = new JPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(5, 5, 5, 5);
      gbc.fill = 2;
      this.cbMethod = new JComboBox(new String[]{"GET", "HEAD", "POST", "DOWNLOAD"});
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weightx = 0.1;
      topPanel.add(this.cbMethod, gbc);
      this.txtUrl = new JTextField("https://google.com");
      gbc.gridx = 1;
      gbc.gridy = 0;
      gbc.weightx = 0.7;
      topPanel.add(this.txtUrl, gbc);
      JButton btnGo = new JButton("Gửi (Go)");
      gbc.gridx = 2;
      gbc.gridy = 0;
      gbc.weightx = 0.1;
      topPanel.add(btnGo, gbc);
      JButton btnHistory = new JButton("Lịch sử");
      gbc.gridx = 3;
      gbc.gridy = 0;
      gbc.weightx = 0.1;
      topPanel.add(btnHistory, gbc);
      this.txtPostData = new JTextField("param1=value1&param2=value2");
      this.txtPostData.setBorder(BorderFactory.createTitledBorder("Dữ liệu POST (Body)"));
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.gridwidth = 4;
      gbc.weightx = (double)1.0F;
      topPanel.add(this.txtPostData, gbc);
      this.txtPostData.setVisible(false);
      this.cbMethod.addActionListener((e) -> {
         this.txtPostData.setVisible("POST".equals(this.cbMethod.getSelectedItem()));
         topPanel.revalidate();
      });
      this.htmlViewer = new JEditorPane();
      this.htmlViewer.setEditable(false);
      this.htmlViewer.setContentType("text/html");
      JScrollPane htmlScroll = new JScrollPane(this.htmlViewer);
      htmlScroll.setBorder(BorderFactory.createTitledBorder("Trực quan trang Web (HTML Render)"));
      this.txtHeaders = new JTextArea();
      this.txtHeaders.setEditable(false);
      this.txtHeaders.setFont(new Font("Monospaced", 0, 12));
      JScrollPane headersScroll = new JScrollPane(this.txtHeaders);
      headersScroll.setBorder(BorderFactory.createTitledBorder("HTTP Response Headers"));
      JSplitPane splitPane = new JSplitPane(1, htmlScroll, headersScroll);
      splitPane.setDividerLocation(650);
      this.historyListModel = new DefaultListModel();
      JList<String> listHistory = new JList(this.historyListModel);
      JDialog historyDialog = new JDialog(this, "Lịch sử duyệt web", true);
      historyDialog.setSize(400, 300);
      historyDialog.add(new JScrollPane(listHistory));
      historyDialog.setLocationRelativeTo(this);
      btnHistory.addActionListener((e) -> historyDialog.setVisible(true));
      btnGo.addActionListener((e) -> {
         String url = this.txtUrl.getText().trim();
         String method = (String)this.cbMethod.getSelectedItem();
         String postData = this.txtPostData.getText().trim();
         if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập URL!");
         } else {
            (new Thread(() -> this.executeHttpSocket(url, method, postData))).start();
         }
      });
      this.setLayout(new BorderLayout());
      this.add(topPanel, "North");
      this.add(splitPane, "Center");
   }

   private void executeHttpSocket(String urlStr, String method, String postData) {
      if (!this.history.contains(urlStr)) {
         this.history.add(urlStr);
         SwingUtilities.invokeLater(() -> this.historyListModel.addElement(urlStr));
      }

      try {
         URL url = new URL(urlStr);
         String host = url.getHost();
         String path = url.getPath().isEmpty() ? "/" : url.getPath();
         if (url.getQuery() != null) {
            path = path + "?" + url.getQuery();
         }

         int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
         Socket socket = url.getProtocol().equalsIgnoreCase("https") ? SSLSocketFactory.getDefault().createSocket(host, port) : new Socket(host, port);
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
         InputStream inputStream = socket.getInputStream();
         StringBuilder req = new StringBuilder();
         req.append(method).append(" ").append(path).append(" HTTP/1.1\r\n");
         req.append("Host: ").append(host).append("\r\n");
         req.append("User-Agent: SocketBrowserGUI/1.0\r\n");
         req.append("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n");
         req.append("Connection: close\r\n");
         if ("POST".equals(method)) {
            byte[] postBytes = postData.getBytes("UTF-8");
            req.append("Content-Type: application/x-www-form-urlencoded\r\n");
            req.append("Content-Length: ").append(postBytes.length).append("\r\n");
            req.append("\r\n").append(postData);
         } else {
            req.append("\r\n");
         }

         writer.write(req.toString());
         writer.flush();
         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
         StringBuilder headersBuilder = new StringBuilder();

         String line;
         while((line = reader.readLine()) != null && !line.isEmpty()) {
            headersBuilder.append(line).append("\n");
         }

         String allHeaders = headersBuilder.toString();
         SwingUtilities.invokeLater(() -> this.txtHeaders.setText(allHeaders));
         if ("HEAD".equals(method)) {
            SwingUtilities.invokeLater(() -> this.htmlViewer.setText("<html><body><h3>Phương thức HEAD thành công. Không có Body.</h3></body></html>"));
            socket.close();
            return;
         }

         if ("DOWNLOAD".equals(method)) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(path.substring(path.lastIndexOf(47) + 1).replaceAll("[^a-zA-Z0-9.]", "file")));
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == 0) {
               File fileToSave = fileChooser.getSelectedFile();
               FileOutputStream fos = new FileOutputStream(fileToSave);
               byte[] buffer = new byte[4096];

               int bytesRead;
               while((bytesRead = inputStream.read(buffer)) != -1) {
                  fos.write(buffer, 0, bytesRead);
               }

               fos.close();
               SwingUtilities.invokeLater(() -> this.htmlViewer.setText("<html><body><h2>Tải file thành công!</h2><p>Đã lưu tại: " + fileToSave.getAbsolutePath() + "</p></body></html>"));
            }
         } else {
            StringBuilder bodyBuilder = new StringBuilder();
            char[] charBuffer = new char[4096];

            int charsRead;
            while((charsRead = reader.read(charBuffer)) != -1) {
               bodyBuilder.append(charBuffer, 0, charsRead);
            }

            String htmlContent = bodyBuilder.toString();
            SwingUtilities.invokeLater(() -> {
               if (!htmlContent.trim().startsWith("{") && !htmlContent.trim().startsWith("[")) {
                  this.htmlViewer.setContentType("text/html");
               } else {
                  this.htmlViewer.setContentType("text/plain");
               }

               this.htmlViewer.setText(htmlContent);
               this.htmlViewer.setCaretPosition(0);
            });
         }

         socket.close();
      } catch (Exception e) {
         SwingUtilities.invokeLater(() -> {
            this.txtHeaders.setText("LỖI KẾT NỐI!");
            this.htmlViewer.setContentType("text/html");
            this.htmlViewer.setText("Không thể tải trang webLỗi: " + e.getMessage());
         });
      }

   }

   public static void main(String[] args) {
      SwingUtilities.invokeLater(() -> {
         HttpSocketBrowserGUI browser = new HttpSocketBrowserGUI();
         browser.setVisible(true);
      });
   }
}
