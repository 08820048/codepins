package cn.ilikexff.codepins;

import com.intellij.openapi.editor.Document;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 工具类：用于生成图钉信息的富样式 Tooltip 文本
 * 支持多语言、主题色切换、自定义钉类型
 */
public class PinTooltipUtil {

    /**
     * 枚举：可扩展的钉类型
     */
    public enum PinType {
        DEFAULT, WARNING, INFO, TASK
    }

    /**
     * 主题配置：颜色方案可切换（可拓展为枚举配置）
     */
    public static class Theme {
        public String bgColor = "rgba(43,43,43,0.88)";
        public String titleColor = "#CCCCCC";
        public String valueColor = "#FFFFFF";
        public String pathColor = "#FFCB6B";
        public String lineColor = "#F78C6C";
        public String noteColor = "#40BFFF";
        public String timeColor = "#82AAFF";
        public String authorColor = "#C792EA";
    }

    /**
     * 构建 Tooltip 文本
     */
    public static String buildTooltip(PinEntry entry, Document doc, Locale locale, PinType type, Theme theme) {
        ResourceBundle bundle = null;
        try {
            bundle = ResourceBundle.getBundle("messages.CodePinsBundle", locale);
        } catch (Exception ignored) {}

        String note = entry.note != null ? escapeHtml(entry.note) : "-";
        int line = entry.getCurrentLine(doc);
        String time = formatTimestamp(entry.timestamp);
        String author = entry.author != null ? entry.author : "-";

        StringBuilder html = new StringBuilder();
        html.append("<html><div style='background:")
                .append(theme.bgColor)
                .append("; padding:10px; border-radius:80px; font-family:monospace; font-size:10px; color:")
                .append(theme.valueColor)
                .append("; line-height:1.7;'>");

        html.append("<span><b style='color:")
                .append(theme.pathColor).append(";'>")
                .append(get(bundle, "tooltip.path", "Path")).append(":</b> ")
                .append(entry.filePath).append("</span><br/>");

        html.append("<span><b style='color:")
                .append(theme.lineColor).append(";'>")
                .append(get(bundle, "tooltip.line", "Line")).append(":</b> ")
                .append(line + 1).append("</span><br/>");

        html.append("<span><b style='color:")
                .append(theme.noteColor).append(";'>")
                .append(get(bundle, "tooltip.note", "Note")).append(":</b> ")
                .append(note).append("</span><br/>");

        html.append("<span><b style='color:")
                .append(theme.timeColor).append(";'>")
                .append(get(bundle, "tooltip.createdAt", "Created At")).append(":</b> ")
                .append(time).append("</span><br/>");

        html.append("<span><b style='color:")
                .append(theme.authorColor).append(";'>")
                .append(get(bundle, "tooltip.author", "Author")).append(":</b> ")
                .append(author).append("</span><br/>");

        // ✅ 仅非 DEFAULT 类型时添加类型说明
        if (type != PinType.DEFAULT) {
            html.append("<div style='margin-top:6px;'><i style='opacity:0.5;'>[")
                    .append(type.name()).append("]</i></div>");
        }

        html.append("</div></html>");
        return html.toString();
    }

    /** 获取多语言内容（若不存在则回退） */
    private static String get(ResourceBundle bundle, String key, String fallback) {
        return (bundle != null && bundle.containsKey(key)) ? bundle.getString(key) : fallback;
    }

    private static String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(timestamp));
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
