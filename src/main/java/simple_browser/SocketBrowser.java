package simple_browser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocketBrowser {
   public static void main(String[] args) {
      Scanner scanner = new Scanner(System.in);
      System.out.println("=== SIMPLE SOCKET WEB BROWSER ===");

      while(true) {
         try {
            System.out.print("\nNhập URL HTTP (ví dụ: http://example.com): ");
            String urlString = scanner.nextLine().trim();
            System.out.print("Chọn phương thức (GET, POST, HEAD): ");
            String method = scanner.nextLine().trim().toUpperCase();
            if (!method.equals("GET") && !method.equals("POST") && !method.equals("HEAD")) {
               System.out.println("Phương thức không hợp lệ! Vui lòng chọn GET, POST hoặc HEAD.");
               continue;
            }

            if (!urlString.startsWith("http://")) {
               urlString = "http://" + urlString;
            }

            URL url = new URL(urlString);
            String host = url.getHost();
            String path = url.getPath().isEmpty() ? "/" : url.getPath();
            if (url.getQuery() != null) {
               path = path + "?" + url.getQuery();
            }

            int port = url.getPort() == -1 ? 80 : url.getPort();
            System.out.println("-> Đang kết nối tới " + host + " qua cổng " + port + "...");
            Socket socket = new Socket(host, port);
            socket.setSoTimeout(5000);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out.print(method + " " + path + " HTTP/1.1\r\n");
            out.print("Host: " + host + "\r\n");
            out.print("User-Agent: Java-Socket-Browser/1.0\r\n");
            out.print("Accept: text/html\r\n");
            out.print("Connection: close\r\n");
            if (method.equals("POST")) {
               String postData = "param1=value1&param2=value2";
               out.print("Content-Type: application/x-www-form-urlencoded\r\n");
               out.print("Content-Length: " + postData.length() + "\r\n");
               out.print("\r\n");
               out.print(postData);
            } else {
               out.print("\r\n");
            }

            out.flush();
            boolean isHeader = true;
            StringBuilder headerBuilder = new StringBuilder();
            StringBuilder bodyBuilder = new StringBuilder();

            String line;
            while((line = in.readLine()) != null) {
               if (isHeader) {
                  if (line.isEmpty()) {
                     isHeader = false;
                  } else {
                     headerBuilder.append(line).append("\n");
                  }
               } else {
                  bodyBuilder.append(line).append("\n");
               }
            }

            in.close();
            out.close();
            socket.close();
            String headers = headerBuilder.toString();
            String body = bodyBuilder.toString();
            String statusLine = headers.split("\n")[0];
            System.out.println("\nStatus từ Server: " + statusLine);
            if (method.equals("HEAD")) {
               System.out.println("\n--- THÔNG TIN TÀI NGUYÊN (HEAD Headers) ---");
               System.out.println(headers);
            } else {
               System.out.println("Chiều dài trang HTML nhận được (số ký tự): " + body.length());
               int pCount = countTags(body, "p");
               int divCount = countTags(body, "div");
               int spanCount = countTags(body, "span");
               int imgCount = countTags(body, "img");
               int imagCount = countTags(body, "imag");
               System.out.println("\n--- THỐNG KÊ THẺ HTML ---");
               System.out.println("Số lượng thẻ <p>: " + pCount);
               System.out.println("Số lượng thẻ <div>: " + divCount);
               System.out.println("Số lượng thẻ <span>: " + spanCount);
               System.out.println("Số lượng thẻ <img>: " + (imgCount + imagCount));
               String title = extractTitle(body);
               System.out.println("\n--- TIÊU ĐỀ TRANG WEB ---");
               System.out.println(title);
            }
         } catch (Exception e) {
            System.out.println("Có lỗi xảy ra: " + e.getMessage());
         }

         System.out.print("\nBạn có muốn tiếp tục không? (y/n): ");
         String cont = scanner.nextLine().trim();
         if (!cont.equalsIgnoreCase("y")) {
            scanner.close();
            System.out.println("Đã đóng Browser.");
            return;
         }
      }
   }

   private static int countTags(String html, String tagName) {
      String regex = "<" + tagName + "(\\s|>)";
      Pattern pattern = Pattern.compile(regex, 2);
      Matcher matcher = pattern.matcher(html);

      int count;
      for(count = 0; matcher.find(); ++count) {
      }

      return count;
   }

   private static String extractTitle(String html) {
      Pattern pattern = Pattern.compile("<title>(.*?)</title>", 34);
      Matcher matcher = pattern.matcher(html);
      return matcher.find() ? matcher.group(1).trim() : "Không tìm thấy tiêu đề (<title>)";
   }
}
