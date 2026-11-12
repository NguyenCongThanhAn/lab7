package protocol;


import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MimeParser {

    /**
     * Hàm chính: Nhận vào raw body từ server và trả về nội dung HTML/Text đã giải mã sạch sẽ
     */
    public static String parseRawBody(String rawBody) {
        if (rawBody == null || rawBody.isEmpty()) {
            return "";
        }

        // 1. Tìm chuỗi boundary (ví dụ: --0000000000004bc72205b6646d22)
        Pattern boundaryPattern = Pattern.compile("(?m)^(--[a-zA-Z0-9'()+_,-./:=?]+)\\s*$");
        Matcher matcher = boundaryPattern.matcher(rawBody);

        if (matcher.find()) {
            String boundary = matcher.group(1);
            String[] parts = rawBody.split(Pattern.quote(boundary));

            String plainText = null;
            String htmlText = null;

            for (String part : parts) {
                part = part.trim();
                if (part.isEmpty() || part.equals("--")) {
                    continue;
                }

                // Tách phần Header của block và phần Nội dung bằng dòng trống (\r\n\r\n hoặc \n\n)
                String[] headerAndContent = part.split("\\r?\\n\\r?\\n", 2);
                if (headerAndContent.length < 2) {
                    continue;
                }

                String headers = headerAndContent[0].toLowerCase();
                String content = headerAndContent[1];

                // Giải mã nội dung dựa theo Content-Transfer-Encoding
                String decodedContent = decodeTransferEncoding(content, headers);

                if (headers.contains("text/html")) {
                    htmlText = decodedContent;
                } else if (headers.contains("text/plain")) {
                    plainText = decodedContent;
                }
            }

            // Ưu tiên trả về bản HTML đẹp, nếu không có thì trả về bản Text thuần
            if (htmlText != null) return htmlText;
            if (plainText != null) return plainText;
        }

        // Trường hợp email đơn (không có multipart boundary)
        return rawBody;
    }

    /**
     * Kiểm tra kiểu mã hóa (Base64 hoặc Quoted-Printable) để giải mã tương ứng
     */
    private static String decodeTransferEncoding(String content, String headers) {
        if (headers.contains("content-transfer-encoding: base64")) {
            return decodeBase64(content);
        } else if (headers.contains("content-transfer-encoding: quoted-printable")) {
            return decodeQuotedPrintable(content);
        }
        return content;
    }

    /**
     * Giải mã chuỗi Base64 sang tiếng Việt UTF-8
     */
    private static String decodeBase64(String base64Str) {
        try {
            // Xóa toàn bộ ký tự xuống dòng và khoảng trắng thừa trước khi giải mã
            String cleanBase64 = base64Str.replaceAll("\\s+", "");
            byte[] decodedBytes = Base64.getDecoder().decode(cleanBase64);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return base64Str;
        }
    }

    /**
     * Giải mã chuỗi Quoted-Printable (ví dụ: =C4=91 -> đ, =3D -> =) sang UTF-8
     */
    private static String decodeQuotedPrintable(String qpStr) {
        // 1. Nối các dòng bị ngắt mềm bằng dấu '=' ở cuối dòng
        String unfolded = qpStr.replaceAll("=\\r?\\n", "");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < unfolded.length(); i++) {
            char c = unfolded.charAt(i);
            if (c == '=' && i + 2 < unfolded.length()) {
                String hex = unfolded.substring(i + 1, i + 3);
                try {
                    int b = Integer.parseInt(hex, 16);
                    buffer.write(b);
                    i += 2;
                    continue;
                } catch (NumberFormatException ignored) {
                }
            }
            buffer.write((byte) c);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
