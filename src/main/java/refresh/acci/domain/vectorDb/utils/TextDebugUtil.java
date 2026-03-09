package refresh.acci.domain.vectorDb.utils;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class TextDebugUtil {
    private static final HexFormat HEX = HexFormat.of();

    // String을 UTF-8로 인코딩한 바이트를 hex로
    public static String utf8Hex(String s, int maxBytes) {
        if (s == null) return "null";
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        int n = Math.min(b.length, Math.max(0, maxBytes));
        return HEX.formatHex(b, 0, n) + (b.length > n ? "...(+" + (b.length - n) + " bytes)" : "");
    }

    // 코드포인트를 U+XXXX 형태로 덤프 (문자 레벨에서 이상한 값 찾기)
    public static String codePointDump(String s, int maxCps) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder();
        int[] cps = s.codePoints().limit(maxCps).toArray();
        for (int i = 0; i < cps.length; i++) {
            int cp = cps[i];
            if (i > 0) sb.append(' ');
            sb.append(String.format("U+%04X", cp));
        }
        long total = s.codePoints().count();
        if (total > cps.length) sb.append(" ...(+").append(total - cps.length).append(" cps)");
        return sb.toString();
    }

    // 특정 패턴(예: "관련 법규") 근처를 잘라서 같이 보기
    public static String snippetAround(String s, String needle, int radius) {
        if (s == null) return "null";
        int idx = s.indexOf(needle);
        if (idx < 0) return s.substring(0, Math.min(s.length(), radius * 2)) + (s.length() > radius * 2 ? "..." : "");
        int start = Math.max(0, idx - radius);
        int end = Math.min(s.length(), idx + needle.length() + radius);
        return s.substring(start, end);
    }

    private TextDebugUtil() {}
}
