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
     * 注意：调用此方法前必须确保在 ReadAction 中
     */
    public static String buildTooltip(PinEntry entry, Document doc, Locale locale, PinType type, Theme theme) {
        try {
            System.out.println("[CodePins] 开始构建工具提示，图钉路径: " + entry.filePath);

            // 验证参数
            if (entry == null) {
                System.out.println("[CodePins] 工具提示构建失败: entry 为空");
                return createErrorTooltip("图钉对象为空");
            }

            if (doc == null) {
                System.out.println("[CodePins] 工具提示构建失败: doc 为空");
                return createErrorTooltip("文档对象为空");
            }

            if (theme == null) {
                theme = new Theme(); // 使用默认主题
                System.out.println("[CodePins] 使用默认主题");
            }

            // 加载语言包
            ResourceBundle bundle = null;
            try {
                bundle = ResourceBundle.getBundle("messages.CodePinsBundle", locale);
                System.out.println("[CodePins] 成功加载语言包");
            } catch (Exception e) {
                System.out.println("[CodePins] 加载语言包失败: " + e.getMessage());
            }

            // 准备数据
            String note = entry.note != null ? escapeHtml(entry.note) : "-";

            // 安全获取行号
            int line;
            try {
                line = entry.getCurrentLine(doc);
                System.out.println("[CodePins] 获取当前行号成功: " + (line + 1));
            } catch (Exception e) {
                System.out.println("[CodePins] 获取行号失败: " + e.getMessage());
                line = 0; // 默认值
            }

            String time = formatTimestamp(entry.timestamp);
            String author = entry.author != null ? escapeHtml(entry.author) : "-";

            // 构建 HTML
            StringBuilder html = new StringBuilder();
            html.append("<html><div style='background:")
                    .append(theme.bgColor)
                    .append("; padding:10px; border-radius:8px; font-family:monospace; font-size:12px; color:")
                    .append(theme.valueColor)
                    .append("; line-height:1.5;'>");

            html.append("<span><b style='color:")
                    .append(theme.pathColor).append(";'>")
                    .append(get(bundle, "tooltip.path", "Path")).append(":</b> ")
                    .append(escapeHtml(entry.filePath)).append("</span><br/>");

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
            String result = html.toString();
            System.out.println("[CodePins] 工具提示构建成功，长度: " + result.length());
            return result;
        } catch (Exception e) {
            // 如果发生异常，记录详细错误并返回一个简化的提示
            System.out.println("[CodePins] 工具提示构建异常: " + e.getMessage());
            e.printStackTrace();
            return createErrorTooltip(e.getMessage());
        }
    }

    /**
     * 创建错误提示
     */
    private static String createErrorTooltip(String errorMessage) {
        return "<html><div style='padding:8px; background-color:#f8f8f8; color:#333; border:1px solid #ccc; border-radius:4px;'>"
                + "图钉信息加载失败"
                + (errorMessage != null && !errorMessage.isEmpty() ? ": " + escapeHtml(errorMessage) : "")
                + "</div></html>";
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
