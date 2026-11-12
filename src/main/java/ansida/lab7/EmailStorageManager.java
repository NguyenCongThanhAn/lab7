package ansida.lab7;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import protocol.EmailRecord;

public class EmailStorageManager {
   private static final String DB_URL = "jdbc:sqlite:network_emails.db";
   private static final String STORAGE_DIR = "./downloaded_emails";

   private static void initDatabase() {
      String sql = "CREATE TABLE IF NOT EXISTS emails (id INTEGER PRIMARY KEY AUTOINCREMENT, mail_uid TEXT UNIQUE, sender TEXT, subject TEXT, received_date TEXT, body_content TEXT, raw_source TEXT, saved_file_path TEXT);";

      try {
         Connection conn = DriverManager.getConnection("jdbc:sqlite:network_emails.db");

         try {
            Statement stmt = conn.createStatement();

            try {
               stmt.execute(sql);
               System.out.println("[DB Info] Đã kiểm tra và khởi tạo Cơ sở dữ liệu SQLite thành công.");
            } catch (Throwable var7) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var8) {
            if (conn != null) {
               try {
                  conn.close();
               } catch (Throwable var5) {
                  var8.addSuppressed(var5);
               }
            }

            throw var8;
         }

         if (conn != null) {
            conn.close();
         }
      } catch (Exception e) {
         System.err.println("[-] Lỗi khởi tạo cấu trúc SQLite: " + e.getMessage());
      }

   }

   public static void processAndStoreEmail(String rawEmailContent) {
      if (rawEmailContent != null && !rawEmailContent.trim().isEmpty()) {
         String mailUid = UUID.randomUUID().toString();
         String sender = parseHeader(rawEmailContent, "From:");
         String subject = parseHeader(rawEmailContent, "Subject:");
         String date = parseHeader(rawEmailContent, "Date:");
         String body = parseEmailBody(rawEmailContent);
         String filePath = saveToFileSystem(mailUid, rawEmailContent);
         if (filePath != null) {
            saveToDatabase(mailUid, sender, subject, date, body, rawEmailContent, filePath);
         }

      } else {
         System.err.println("[-] Dữ liệu email trống, bỏ qua lưu trữ.");
      }
   }

   private static String saveToFileSystem(String mailUid, String content) {
      try {
         Path dirPath = Paths.get("./downloaded_emails");
         if (!Files.exists(dirPath, new LinkOption[0])) {
            Files.createDirectories(dirPath);
         }

         Path filePath = dirPath.resolve("email_" + mailUid + ".txt");
         BufferedWriter writer = Files.newBufferedWriter(filePath);

         try {
            writer.write(content);
         } catch (Throwable var8) {
            if (writer != null) {
               try {
                  writer.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (writer != null) {
            writer.close();
         }

         return filePath.toAbsolutePath().toString();
      } catch (IOException e) {
         System.err.println(" -> [Lỗi] Không thể ghi file ra máy: " + e.getMessage());
         return null;
      }
   }

   private static void saveToDatabase(String mailUid, String sender, String subject, String date, String body, String rawSource, String filePath) {
      String sql = "INSERT INTO emails (mail_uid, sender, subject, received_date, body_content, raw_source, saved_file_path) VALUES (?, ?, ?, ?, ?, ?, ?)";

      try {
         Connection conn = DriverManager.getConnection("jdbc:sqlite:network_emails.db");

         try {
            PreparedStatement pstmt = conn.prepareStatement(sql);

            try {
               pstmt.setString(1, mailUid);
               pstmt.setString(2, sender);
               pstmt.setString(3, subject);
               pstmt.setString(4, date);
               pstmt.setString(5, body);
               pstmt.setString(6, rawSource);
               pstmt.setString(7, filePath);
               pstmt.executeUpdate();
               System.out.println("[DB Info] Đã đồng bộ thành công email mới vào SQLite.");
            } catch (Throwable var14) {
               if (pstmt != null) {
                  try {
                     pstmt.close();
                  } catch (Throwable var13) {
                     var14.addSuppressed(var13);
                  }
               }

               throw var14;
            }

            if (pstmt != null) {
               pstmt.close();
            }
         } catch (Throwable var15) {
            if (conn != null) {
               try {
                  conn.close();
               } catch (Throwable var12) {
                  var15.addSuppressed(var12);
               }
            }

            throw var15;
         }

         if (conn != null) {
            conn.close();
         }
      } catch (Exception e) {
         System.err.println(" -> [Lỗi SQL] Không thể lưu vào SQLite: " + e.getMessage());
      }

   }

   public static List<EmailRecord> getEmailsForUser(String currentUserEmail) {
      List<EmailRecord> emailList = new ArrayList();
      String sql = "SELECT id, mail_uid, sender, subject, received_date, body_content, raw_source, saved_file_path FROM emails WHERE raw_source LIKE ? ORDER BY id DESC";

      try {
         Connection conn = DriverManager.getConnection("jdbc:sqlite:network_emails.db");

         try {
            PreparedStatement pstmt = conn.prepareStatement(sql);

            try {
               pstmt.setString(1, "%" + currentUserEmail + "%");
               ResultSet rs = pstmt.executeQuery();

               try {
                  while(rs.next()) {
                     EmailRecord email = new EmailRecord(rs.getInt("id"), rs.getString("mail_uid"), rs.getString("sender"), rs.getString("subject"), rs.getString("received_date"), rs.getString("body_content"), rs.getString("raw_source"), rs.getString("saved_file_path"));
                     emailList.add(email);
                  }
               } catch (Throwable var11) {
                  if (rs != null) {
                     try {
                        rs.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                     }
                  }

                  throw var11;
               }

               if (rs != null) {
                  rs.close();
               }
            } catch (Throwable var12) {
               if (pstmt != null) {
                  try {
                     pstmt.close();
                  } catch (Throwable var9) {
                     var12.addSuppressed(var9);
                  }
               }

               throw var12;
            }

            if (pstmt != null) {
               pstmt.close();
            }
         } catch (Throwable var13) {
            if (conn != null) {
               try {
                  conn.close();
               } catch (Throwable var8) {
                  var13.addSuppressed(var8);
               }
            }

            throw var13;
         }

         if (conn != null) {
            conn.close();
         }
      } catch (Exception e) {
         System.err.println("[-] Lỗi khi lấy danh sách EmailRecord từ SQLite: " + e.getMessage());
      }

      return emailList;
   }

   public static void deleteEmailFromDatabase(int id) {
      String sql = "DELETE FROM emails WHERE id = ?";

      try {
         Connection conn = DriverManager.getConnection("jdbc:sqlite:network_emails.db");

         try {
            PreparedStatement pstmt = conn.prepareStatement(sql);

            try {
               pstmt.setInt(1, id);
               pstmt.executeUpdate();
               System.out.println("[DB Info] Đã xóa thành công Email ID: " + id + " trong SQLite.");
            } catch (Throwable var8) {
               if (pstmt != null) {
                  try {
                     pstmt.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (pstmt != null) {
               pstmt.close();
            }
         } catch (Throwable var9) {
            if (conn != null) {
               try {
                  conn.close();
               } catch (Throwable var6) {
                  var9.addSuppressed(var6);
               }
            }

            throw var9;
         }

         if (conn != null) {
            conn.close();
         }
      } catch (Exception e) {
         System.err.println("[-] Lỗi khi xóa Email khỏi SQLite: " + e.getMessage());
      }

   }

   private static String parseHeader(String rawContent, String headerName) {
      String[] lines = rawContent.split("\r?\n");

      for(String line : lines) {
         if (line.toLowerCase().startsWith(headerName.toLowerCase())) {
            String rawValue = line.substring(headerName.length()).trim();
            return decodeMimeHeader(rawValue);
         }
      }

      return "Không rõ";
   }

   private static String parseEmailBody(String rawContent) {
      int boundaryIndex = rawContent.indexOf("\r\n\r\n");
      if (boundaryIndex == -1) {
         boundaryIndex = rawContent.indexOf("\n\n");
      }

      return boundaryIndex != -1 ? rawContent.substring(boundaryIndex).trim() : rawContent;
   }

   public static String decodeMimeHeader(String rawHeader) {
      if (rawHeader != null && rawHeader.startsWith("=?")) {
         try {
            String[] parts = rawHeader.split("\\?");
            if (parts.length >= 4) {
               String encoding = parts[1];
               String type = parts[2];
               String encodedText = parts[3];
               if (type.equalsIgnoreCase("B")) {
                  byte[] decodedBytes = Base64.getDecoder().decode(encodedText);
                  return new String(decodedBytes, encoding);
               }
            }
         } catch (Exception e) {
            System.err.println("[-] Lỗi giải mã MIME thủ công: " + e.getMessage());
         }

         return rawHeader;
      } else {
         return rawHeader;
      }
   }

   static {
      initDatabase();
   }
}
