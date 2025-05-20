package cn.ilikexff.codepins.utils;

import cn.ilikexff.codepins.PinEntry;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 分享工具类
 * 用于将图钉分享为不同格式
 */
public class SharingUtil {

    /**
     * 分享格式枚举
     */
    public enum SharingFormat {
        MARKDOWN("Markdown"),
        HTML("HTML"),
        JSON("JSON"),
        CODE_ONLY("仅代码"),
        IMAGE("图片"),
        SVG("SVG");

        private final String displayName;

        SharingFormat(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 将图钉格式化为指定格式并复制到剪贴板
     *
     * @param project 当前项目
     * @param pin 要分享的图钉
     * @param format 分享格式
     * @return 是否成功
     */
    public static boolean copyPinToClipboard(Project project, PinEntry pin, SharingFormat format) {
        return copyPinToClipboard(project, pin, format, false);
    }

    /**
     * 将图钉格式化为指定格式并复制到剪贴板
     *
     * @param project 当前项目
     * @param pin 要分享的图钉
     * @param format 分享格式
     * @param codeOnly 是否只分享代码
     * @return 是否成功
     */
    public static boolean copyPinToClipboard(Project project, PinEntry pin, SharingFormat format, boolean codeOnly) {
        return copyPinToClipboard(project, pin, format, codeOnly, true);
    }

    /**
     * 将图钉格式化为指定格式并复制到剪贴板
     *
     * @param project 当前项目
     * @param pin 要分享的图钉
     * @param format 分享格式
     * @param codeOnly 是否只分享代码
     * @param showLineNumbers 是否显示行号
     * @return 是否成功
     */
    public static boolean copyPinToClipboard(Project project, PinEntry pin, SharingFormat format, boolean codeOnly, boolean showLineNumbers) {
        try {
            String content = formatPin(project, pin, format, codeOnly);
            if (content != null) {
                StringSelection selection = new StringSelection(content);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                return true;
            }
            return false;
        } catch (Exception e) {
            Messages.showErrorDialog(
                    project,
                    "复制到剪贴板失败: " + e.getMessage(),
                    "分享错误"
            );
            return false;
        }
    }

    /**
     * 将多个图钉格式化为指定格式并复制到剪贴板
     *
     * @param project 当前项目
     * @param pins 要分享的图钉列表
     * @param format 分享格式
     * @return 是否成功
     */
    public static boolean copyPinsToClipboard(Project project, List<PinEntry> pins, SharingFormat format) {
        return copyPinsToClipboard(project, pins, format, false);
    }

    /**
     * 将多个图钉格式化为指定格式并复制到剪贴板
     *
     * @param project 当前项目
     * @param pins 要分享的图钉列表
     * @param format 分享格式
     * @param codeOnly 是否只分享代码
     * @return 是否成功
     */
    public static boolean copyPinsToClipboard(Project project, List<PinEntry> pins, SharingFormat format, boolean codeOnly) {
        return copyPinsToClipboard(project, pins, format, codeOnly, true);
    }

    /**
     * 将多个图钉格式化为指定格式并复制到剪贴板
     *
     * @param project 当前项目
     * @param pins 要分享的图钉列表
     * @param format 分享格式
     * @param codeOnly 是否只分享代码
     * @param showLineNumbers 是否显示行号
     * @return 是否成功
     */
    public static boolean copyPinsToClipboard(Project project, List<PinEntry> pins, SharingFormat format, boolean codeOnly, boolean showLineNumbers) {
        try {
            String content = formatPins(project, pins, format, codeOnly, showLineNumbers);
            if (content != null) {
                StringSelection selection = new StringSelection(content);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                return true;
            }
            return false;
        } catch (Exception e) {
            Messages.showErrorDialog(
                    project,
                    "复制到剪贴板失败: " + e.getMessage(),
                    "分享错误"
            );
            return false;
        }
    }

    /**
     * 将图钉导出为指定格式的文件
     *
     * @param project 当前项目
     * @param pin 要导出的图钉
     * @param file 导出文件
     * @param format 导出格式
     * @return 是否成功
     */
    public static boolean exportPinToFile(Project project, PinEntry pin, File file, SharingFormat format) {
        return exportPinToFile(project, pin, file, format, false);
    }

    /**
     * 将图钉导出为指定格式的文件
     *
     * @param project 当前项目
     * @param pin 要导出的图钉
     * @param file 导出文件
     * @param format 导出格式
     * @param codeOnly 是否只导出代码
     * @return 是否成功
     */
    public static boolean exportPinToFile(Project project, PinEntry pin, File file, SharingFormat format, boolean codeOnly) {
        return exportPinToFile(project, pin, file, format, codeOnly, true);
    }

    /**
     * 将图钉导出为指定格式的文件
     *
     * @param project 当前项目
     * @param pin 要导出的图钉
     * @param file 导出文件
     * @param format 导出格式
     * @param codeOnly 是否只导出代码
     * @param showLineNumbers 是否显示行号
     * @return 是否成功
     */
    public static boolean exportPinToFile(Project project, PinEntry pin, File file, SharingFormat format, boolean codeOnly, boolean showLineNumbers) {
        try {
            String content = formatPin(project, pin, format, codeOnly);
            return writeToFile(file, content);
        } catch (Exception e) {
            Messages.showErrorDialog(
                    project,
                    "导出到文件失败: " + e.getMessage(),
                    "分享错误"
            );
            return false;
        }
    }

    /**
     * 将多个图钉导出为指定格式的文件
     *
     * @param project 当前项目
     * @param pins 要导出的图钉列表
     * @param file 导出文件
     * @param format 导出格式
     * @return 是否成功
     */
    public static boolean exportPinsToFile(Project project, List<PinEntry> pins, File file, SharingFormat format) {
        return exportPinsToFile(project, pins, file, format, false);
    }

    /**
     * 将多个图钉导出为指定格式的文件
     *
     * @param project 当前项目
     * @param pins 要导出的图钉列表
     * @param file 导出文件
     * @param format 导出格式
     * @param codeOnly 是否只导出代码
     * @return 是否成功
     */
    public static boolean exportPinsToFile(Project project, List<PinEntry> pins, File file, SharingFormat format, boolean codeOnly) {
        return exportPinsToFile(project, pins, file, format, codeOnly, true);
    }

    /**
     * 将多个图钉导出为指定格式的文件
     *
     * @param project 当前项目
     * @param pins 要导出的图钉列表
     * @param file 导出文件
     * @param format 导出格式
     * @param codeOnly 是否只导出代码
     * @param showLineNumbers 是否显示行号
     * @return 是否成功
     */
    public static boolean exportPinsToFile(Project project, List<PinEntry> pins, File file, SharingFormat format, boolean codeOnly, boolean showLineNumbers) {
        try {
            String content = formatPins(project, pins, format, codeOnly, showLineNumbers);
            return writeToFile(file, content);
        } catch (Exception e) {
            Messages.showErrorDialog(
                    project,
                    "导出到文件失败: " + e.getMessage(),
                    "分享错误"
            );
            return false;
        }
    }

    /**
     * 格式化单个图钉
     *
     * @param project 当前项目
     * @param pin 要格式化的图钉
     * @param format 格式
     * @return 格式化后的内容
     */
    public static String formatPin(Project project, PinEntry pin, SharingFormat format) {
        return formatPin(project, pin, format, false);
    }

    /**
     * 格式化单个图钉
     *
     * @param project 当前项目
     * @param pin 要格式化的图钉
     * @param format 格式
     * @param codeOnly 是否只包含代码
     * @return 格式化后的内容
     */
    public static String formatPin(Project project, PinEntry pin, SharingFormat format, boolean codeOnly) {
        return formatPin(project, pin, format, codeOnly, true);
    }

    /**
     * 格式化单个图钉
     *
     * @param project 当前项目
     * @param pin 要格式化的图钉
     * @param format 格式
     * @param codeOnly 是否只包含代码
     * @param showLineNumbers 是否显示行号
     * @return 格式化后的内容
     */
    public static String formatPin(Project project, PinEntry pin, SharingFormat format, boolean codeOnly, boolean showLineNumbers) {
        // 如果是仅代码格式或者codeOnly标记为true，则只返回代码
        if (format == SharingFormat.CODE_ONLY || codeOnly) {
            String code = getCodeSnippet(project, pin);
            String language = getFileLanguage(pin.filePath);

            // 根据语言添加相应的注释格式
            StringBuilder codeWithComment = new StringBuilder();

            // 添加插件信息注释
            String pluginInfo = "Generated by CodePins - Code Bookmarks (https://plugins.jetbrains.com/plugin/27300-codepins--code-bookmarks)";

            switch (language) {
                case "java":
                case "javascript":
                case "typescript":
                case "c":
                case "cpp":
                case "csharp":
                case "go":
                case "swift":
                case "kotlin":
                case "scala":
                case "rust":
                case "php":
                    // 使用 // 注释
                    codeWithComment.append("// ").append(pluginInfo).append("\n");
                    break;

                case "python":
                case "ruby":
                case "perl":
                case "r":
                case "shell":
                case "bash":
                case "yaml":
                    // 使用 # 注释
                    codeWithComment.append("# ").append(pluginInfo).append("\n");
                    break;

                case "html":
                case "xml":
                    // 使用 <!-- --> 注释
                    codeWithComment.append("<!-- ").append(pluginInfo).append(" -->\n");
                    break;

                case "css":
                    // 使用 /* */ 注释
                    codeWithComment.append("/* ").append(pluginInfo).append(" */\n");
                    break;

                case "sql":
                    // 使用 -- 注释
                    codeWithComment.append("-- ").append(pluginInfo).append("\n");
                    break;

                case "lua":
                    // 使用 -- 注释
                    codeWithComment.append("-- ").append(pluginInfo).append("\n");
                    break;

                default:
                    // 默认使用 // 注释
                    codeWithComment.append("// ").append(pluginInfo).append("\n");
            }

            // 添加代码
            codeWithComment.append(code);
            return codeWithComment.toString();
        }

        switch (format) {
            case MARKDOWN:
                return formatPinAsMarkdown(project, pin, showLineNumbers);
            case HTML:
                return formatPinAsHTML(project, pin, showLineNumbers);
            case JSON:
                return formatPinAsJSON(project, pin);
            default:
                return null;
        }
    }

    /**
     * 格式化多个图钉
     *
     * @param project 当前项目
     * @param pins 要格式化的图钉列表
     * @param format 格式
     * @return 格式化后的内容
     */
    public static String formatPins(Project project, List<PinEntry> pins, SharingFormat format) {
        return formatPins(project, pins, format, false);
    }

    /**
     * 格式化多个图钉
     *
     * @param project 当前项目
     * @param pins 要格式化的图钉列表
     * @param format 格式
     * @param codeOnly 是否只包含代码
     * @return 格式化后的内容
     */
    public static String formatPins(Project project, List<PinEntry> pins, SharingFormat format, boolean codeOnly) {
        return formatPins(project, pins, format, codeOnly, true);
    }

    /**
     * 格式化多个图钉
     *
     * @param project 当前项目
     * @param pins 要格式化的图钉列表
     * @param format 格式
     * @param codeOnly 是否只包含代码
     * @param showLineNumbers 是否显示行号
     * @return 格式化后的内容
     */
    public static String formatPins(Project project, List<PinEntry> pins, SharingFormat format, boolean codeOnly, boolean showLineNumbers) {
        // 如果是仅代码格式或者codeOnly标记为true，则只返回代码
        if (format == SharingFormat.CODE_ONLY || codeOnly) {
            StringBuilder codeContent = new StringBuilder();

            // 检测所有图钉的语言，如果都是同一种语言，则使用该语言的注释格式
            String commonLanguage = null;
            boolean allSameLanguage = true;

            // 首先检查是否所有图钉都是同一种语言
            for (PinEntry pin : pins) {
                if (pin.isBlock) {
                    String language = getFileLanguage(pin.filePath);
                    if (commonLanguage == null) {
                        commonLanguage = language;
                    } else if (!commonLanguage.equals(language)) {
                        allSameLanguage = false;
                        break;
                    }
                }
            }

            // 添加插件信息注释
            String pluginInfo = "Generated by CodePins - Code Bookmarks (https://plugins.jetbrains.com/plugin/27300-codepins--code-bookmarks)";

            if (allSameLanguage && commonLanguage != null) {
                // 根据共同语言添加注释
                switch (commonLanguage) {
                    case "java":
                    case "javascript":
                    case "typescript":
                    case "c":
                    case "cpp":
                    case "csharp":
                    case "go":
                    case "swift":
                    case "kotlin":
                    case "scala":
                    case "rust":
                    case "php":
                        // 使用 // 注释
                        codeContent.append("// ").append(pluginInfo).append("\n\n");
                        break;

                    case "python":
                    case "ruby":
                    case "perl":
                    case "r":
                    case "shell":
                    case "bash":
                    case "yaml":
                        // 使用 # 注释
                        codeContent.append("# ").append(pluginInfo).append("\n\n");
                        break;

                    case "html":
                    case "xml":
                        // 使用 <!-- --> 注释
                        codeContent.append("<!-- ").append(pluginInfo).append(" -->\n\n");
                        break;

                    case "css":
                        // 使用 /* */ 注释
                        codeContent.append("/* ").append(pluginInfo).append(" */\n\n");
                        break;

                    case "sql":
                    case "lua":
                        // 使用 -- 注释
                        codeContent.append("-- ").append(pluginInfo).append("\n\n");
                        break;

                    default:
                        // 默认使用 // 注释
                        codeContent.append("// ").append(pluginInfo).append("\n\n");
                }
            } else {
                // 如果有多种语言，使用通用的多行注释
                codeContent.append("/*\n * ").append(pluginInfo).append("\n */\n\n");
            }

            // 添加所有代码块
            for (int i = 0; i < pins.size(); i++) {
                PinEntry pin = pins.get(i);
                if (pin.isBlock) { // 只包含代码块图钉
                    String code = getCodeSnippet(project, pin);
                    if (code != null && !code.trim().isEmpty()) {
                        // 添加文件名注释
                        String fileName = new File(pin.filePath).getName();
                        if (!allSameLanguage) {
                            codeContent.append("// From: ").append(fileName).append("\n");
                        }

                        codeContent.append(code);
                        if (i < pins.size() - 1) {
                            codeContent.append("\n\n");
                        }
                    }
                }
            }
            return codeContent.toString();
        }

        StringBuilder content = new StringBuilder();

        switch (format) {
            case MARKDOWN:
                content.append("# CodePins 分享\n\n");
                content.append("> 分享时间: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
                content.append("> 项目: ").append(project.getName()).append("\n\n");

                for (int i = 0; i < pins.size(); i++) {
                    content.append("## 图钉 ").append(i + 1).append("\n\n");
                    content.append(formatPinAsMarkdown(project, pins.get(i), showLineNumbers)).append("\n\n");
                    if (i < pins.size() - 1) {
                        content.append("---\n\n");
                    }
                }

                // 添加插件信息
                content.append("\n---\n\n");
                content.append("*Generated by [CodePins - Code Bookmarks](https://plugins.jetbrains.com/plugin/27300-codepins--code-bookmarks) - The Modern Code Bookmarking Solution for JetBrains IDEs*\n");
                break;

            case HTML:
                content.append("<!DOCTYPE html>\n<html>\n<head>\n");
                content.append("<meta charset=\"UTF-8\">\n");
                content.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
                content.append("<title>CodePins 分享</title>\n");

                // 添加代码高亮库 highlight.js
                content.append("<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.7.0/styles/atom-one-dark.min.css\">\n");
                content.append("<script src=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.7.0/highlight.min.js\"></script>\n");

                content.append("<style>\n");
                // 现代化的CSS样式
                content.append(":root { --primary-color: #3498db; --secondary-color: #2c3e50; --accent-color: #e74c3c; --bg-color: #ffffff; --text-color: #333333; --light-gray: #f5f5f5; --border-color: #e0e0e0; }\n");
                content.append("@media (prefers-color-scheme: dark) { :root { --primary-color: #61dafb; --secondary-color: #282c34; --accent-color: #e74c3c; --bg-color: #1a1a1a; --text-color: #f0f0f0; --light-gray: #2c2c2c; --border-color: #444444; } }\n");
                content.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif; line-height: 1.6; color: var(--text-color); background-color: var(--bg-color); max-width: 900px; margin: 0 auto; padding: 20px; transition: all 0.3s ease; position: relative; }\n");
                content.append("h1, h2, h3, h4 { color: var(--primary-color); margin-top: 1.5em; margin-bottom: 0.5em; }\n");
                content.append("h1 { font-size: 2.2em; border-bottom: 2px solid var(--primary-color); padding-bottom: 10px; }\n");
                content.append("h2 { font-size: 1.8em; }\n");
                content.append("pre { background-color: var(--light-gray); padding: 15px; border-radius: 8px; overflow-x: auto; box-shadow: 0 2px 5px rgba(0,0,0,0.1); margin: 20px 0; }\n");
                content.append("code { font-family: 'Fira Code', Consolas, Monaco, 'Andale Mono', monospace; font-size: 0.9em; }\n");
                content.append(".pin { border: 1px solid var(--border-color); border-radius: 10px; padding: 20px; margin-bottom: 30px; box-shadow: 0 3px 10px rgba(0,0,0,0.08); transition: transform 0.2s ease, box-shadow 0.2s ease; }\n");
                content.append(".pin:hover { transform: translateY(-3px); box-shadow: 0 5px 15px rgba(0,0,0,0.1); }\n");
                content.append(".pin-header { border-bottom: 1px solid var(--border-color); padding-bottom: 15px; margin-bottom: 15px; }\n");
                content.append(".pin-meta { color: #888; font-size: 0.9em; margin: 5px 0; }\n");
                content.append(".pin-tags { margin-top: 10px; }\n");
                content.append(".pin-tags span { background-color: var(--light-gray); color: var(--primary-color); padding: 3px 8px; border-radius: 15px; margin-right: 8px; font-size: 0.8em; display: inline-block; margin-bottom: 5px; }\n");
                content.append(".pin-code { margin-top: 20px; }\n");
                content.append(".pin-code h4 { margin-bottom: 10px; }\n");
                content.append(".pin-note { background-color: rgba(52, 152, 219, 0.1); border-left: 4px solid var(--primary-color); padding: 10px 15px; margin: 15px 0; border-radius: 0 5px 5px 0; }\n");

                // 代码头部样式
                content.append(".code-header { display: flex; justify-content: space-between; background-color: var(--secondary-color); color: #fff; padding: 5px 10px; border-radius: 5px 5px 0 0; font-size: 0.8em; }\n");
                content.append(".code-language { font-weight: bold; background-color: var(--primary-color); padding: 2px 6px; border-radius: 3px; }\n");
                content.append(".code-filename { opacity: 0.8; }\n");
                content.append("pre { margin-top: 0 !important; border-top-left-radius: 0 !important; border-top-right-radius: 0 !important; }\n");

                // 行号样式
                content.append(".with-line-numbers { counter-reset: line; }\n");
                content.append(".with-line-numbers code { position: relative; padding-left: 3.5em; }\n");
                content.append(".line-number { display: inline-block; width: 3em; text-align: right; color: #888; margin-right: 1em; padding-right: 0.5em; border-right: 1px solid #ddd; user-select: none; }\n");

                // 添加水印样式
                content.append(".watermark { position: fixed; bottom: 20px; right: 20px; opacity: 0.1; font-size: 16px; font-weight: bold; color: var(--primary-color); pointer-events: none; z-index: 1000; }\n");
                content.append(".watermark img { width: 100px; height: auto; }\n");
                content.append(".footer { text-align: center; margin-top: 40px; padding-top: 20px; border-top: 1px solid var(--border-color); font-size: 0.9em; color: #888; }\n");

                content.append("@media (max-width: 768px) { body { padding: 15px; } .pin { padding: 15px; } }\n");
                content.append("</style>\n");
                content.append("</head>\n<body>\n");

                // 添加水印
                content.append("<div class=\"watermark\">Powered by CodePins - Code Bookmarks</div>\n");

                content.append("<h1>CodePins 分享</h1>\n");
                content.append("<div class=\"pin-meta\">分享时间: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("</div>\n");
                content.append("<div class=\"pin-meta\">项目: ").append(project.getName()).append("</div>\n");

                for (int i = 0; i < pins.size(); i++) {
                    content.append("<div class=\"pin\">\n");
                    content.append("<h2>图钉 ").append(i + 1).append("</h2>\n");
                    content.append(formatPinAsHTML(project, pins.get(i), showLineNumbers)).append("\n");
                    content.append("</div>\n");
                }

                // 添加页脚
                content.append("<div class=\"footer\">\n");
                content.append("  <p>Generated by <a href=\"https://plugins.jetbrains.com/plugin/27300-codepins--code-bookmarks\" target=\"_blank\"><strong>CodePins - Code Bookmarks</strong></a> - The Modern Code Bookmarking Solution for JetBrains IDEs</p>\n");
                content.append("</div>\n");

                // 添加代码高亮初始化脚本
                content.append("<script>\n");
                content.append("  document.addEventListener('DOMContentLoaded', (event) => {\n");
                content.append("    document.querySelectorAll('pre code').forEach((block) => {\n");
                content.append("      hljs.highlightElement(block);\n");
                content.append("    });\n");
                content.append("  });\n");
                content.append("</script>\n");

                content.append("</body>\n</html>");
                break;

            case JSON:
                // 创建结构化的JSON数据
                content.append("{");

                // 添加元数据
                content.append("\"metadata\": {");
                content.append("\"generator\": \"CodePins - Code Bookmarks\", ");
                content.append("\"plugin_url\": \"https://plugins.jetbrains.com/plugin/27300-codepins--code-bookmarks\", ");
                content.append("\"generated_at\": \"").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\", ");
                content.append("\"project\": \"").append(escapeJson(project.getName())).append("\"");
                content.append("}, ");

                // 添加图钉数组
                content.append("\"pins\": [");

                for (int i = 0; i < pins.size(); i++) {
                    // 使用单个图钉的JSON格式化方法，但需要去掉外层的花括号
                    String pinJson = formatPinAsJSON(project, pins.get(i));
                    // 去掉外层的花括号
                    pinJson = pinJson.substring(1, pinJson.length() - 1);
                    content.append("{").append(pinJson).append("}");

                    if (i < pins.size() - 1) {
                        content.append(", ");
                    }
                }

                content.append("]");
                content.append("}");
                break;

            default:
                return null;
        }

        return content.toString();
    }

    /**
     * 将图钉格式化为Markdown
     *
     * @param project 当前项目
     * @param pin 要格式化的图钉
     * @return 格式化后的Markdown内容
     */
    private static String formatPinAsMarkdown(Project project, PinEntry pin) {
        return formatPinAsMarkdown(project, pin, true);
    }

    /**
     * 将图钉格式化为Markdown
     *
     * @param project 当前项目
     * @param pin 要格式化的图钉
     * @param showLineNumbers 是否显示行号
     * @return 格式化后的Markdown内容
     */
    private static String formatPinAsMarkdown(Project project, PinEntry pin, boolean showLineNumbers) {
        StringBuilder md = new StringBuilder();

        // 获取文件名
        String fileName = new File(pin.filePath).getName();

        // 获取代码片段
        String codeSnippet = getCodeSnippet(project, pin);
        String language = getFileLanguage(pin.filePath);

        // 格式化为Markdown
        md.append("### ").append(fileName).append("\n\n");
        md.append("**路径:** `").append(pin.filePath).append("`\n");

        // 行号信息
        Document doc = pin.marker.getDocument();
        if (pin.isBlock) {
            int startLine = doc.getLineNumber(pin.marker.getStartOffset()) + 1;
            int endLine = doc.getLineNumber(pin.marker.getEndOffset()) + 1;
            md.append("**位置:** 第 ").append(startLine);
            if (startLine != endLine) {
                md.append("-").append(endLine);
            }
            md.append(" 行\n");
        } else {
            int line = doc.getLineNumber(pin.marker.getStartOffset()) + 1;
            md.append("**位置:** 第 ").append(line).append(" 行\n");
        }

        // 时间信息
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        md.append("**创建时间:** ").append(sdf.format(new Date(pin.timestamp))).append("\n");

        // 作者信息
        md.append("**作者:** ").append(pin.author).append("\n");

        // 标签信息
        if (!pin.getTags().isEmpty()) {
            md.append("**标签:** ");
            for (String tag : pin.getTags()) {
                md.append("`").append(tag).append("` ");
            }
            md.append("\n");
        }

        // 备注信息
        if (pin.note != null && !pin.note.trim().isEmpty()) {
            md.append("\n#### 备注\n\n");
            md.append(pin.note).append("\n");
        }

        // 代码片段
        if (codeSnippet != null && !codeSnippet.trim().isEmpty()) {
            md.append("\n#### 代码\n\n");

            // 如果需要显示行号，则添加行号
            if (showLineNumbers) {
                // 分割代码行
                String[] lines = codeSnippet.split("\\n");

                // 获取起始行号
                // 使用已有的doc变量
                int startLine = doc.getLineNumber(pin.marker.getStartOffset()) + 1;

                // 构建带行号的代码
                StringBuilder codeWithLineNumbers = new StringBuilder();
                for (int i = 0; i < lines.length; i++) {
                    int lineNumber = startLine + i;
                    codeWithLineNumbers.append(String.format("%4d | %s\n", lineNumber, lines[i]));
                }

                md.append("```").append(language).append("\n");
                md.append(codeWithLineNumbers);
                md.append("```\n");
            } else {
                // 不显示行号
                md.append("```").append(language).append("\n");
                md.append(codeSnippet).append("\n");
                md.append("```\n");
            }
        }

        return md.toString();
    }

    /**
     * 将图钉格式化为HTML
     *
     * @param project 当前项目
     * @param pin 要格式化的图钉
     * @return 格式化后的HTML内容
     */
    private static String formatPinAsHTML(Project project, PinEntry pin) {
        return formatPinAsHTML(project, pin, true);
    }

    /**
     * 将图钉格式化为HTML
     *
     * @param project 当前项目
     * @param pin 要格式化的图钉
     * @param showLineNumbers 是否显示行号
     * @return 格式化后的HTML内容
     */
    private static String formatPinAsHTML(Project project, PinEntry pin, boolean showLineNumbers) {
        StringBuilder html = new StringBuilder();

        // 获取文件名
        String fileName = new File(pin.filePath).getName();

        // 获取代码片段
        String codeSnippet = getCodeSnippet(project, pin);
        String language = getFileLanguage(pin.filePath);

        // 格式化为HTML
        html.append("<div class=\"pin-header\">\n");
        html.append("<h3>").append(fileName).append("</h3>\n");
        html.append("<p class=\"pin-meta\">路径: <code>").append(pin.filePath).append("</code></p>\n");

        // 行号信息
        Document doc = pin.marker.getDocument();
        if (pin.isBlock) {
            int startLine = doc.getLineNumber(pin.marker.getStartOffset()) + 1;
            int endLine = doc.getLineNumber(pin.marker.getEndOffset()) + 1;
            html.append("<p class=\"pin-meta\">位置: 第 ").append(startLine);
            if (startLine != endLine) {
                html.append("-").append(endLine);
            }
            html.append(" 行</p>\n");
        } else {
            int line = doc.getLineNumber(pin.marker.getStartOffset()) + 1;
            html.append("<p class=\"pin-meta\">位置: 第 ").append(line).append(" 行</p>\n");
        }

        // 时间信息
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        html.append("<p class=\"pin-meta\">创建时间: ").append(sdf.format(new Date(pin.timestamp))).append("</p>\n");

        // 作者信息
        html.append("<p class=\"pin-meta\">作者: ").append(pin.author).append("</p>\n");

        // 标签信息
        if (!pin.getTags().isEmpty()) {
            html.append("<p class=\"pin-tags\">标签: ");
            for (String tag : pin.getTags()) {
                html.append("<span>").append(tag).append("</span> ");
            }
            html.append("</p>\n");
        }
        html.append("</div>\n");

        // 备注信息
        if (pin.note != null && !pin.note.trim().isEmpty()) {
            html.append("<div class=\"pin-note\">\n");
            html.append("<h4>备注</h4>\n");
            html.append("<p>").append(pin.note.replace("\n", "<br>")).append("</p>\n");
            html.append("</div>\n");
        }

        // 代码片段
        if (codeSnippet != null && !codeSnippet.trim().isEmpty()) {
            html.append("<div class=\"pin-code\">\n");
            html.append("<h4>代码</h4>\n");

            // 添加文件名和语言信息
            html.append("<div class=\"code-header\">\n");
            html.append("  <span class=\"code-language\">").append(language.toUpperCase()).append("</span>\n");
            html.append("  <span class=\"code-filename\">").append(fileName).append("</span>\n");
            html.append("</div>\n");

            // 如果需要显示行号，则添加行号
            if (showLineNumbers) {
                // 分割代码行
                String[] lines = codeSnippet.split("\\n");

                // 获取起始行号
                // 使用已有的doc变量
                int startLine = doc.getLineNumber(pin.marker.getStartOffset()) + 1;

                // 构建带行号的代码
                StringBuilder codeWithLineNumbers = new StringBuilder();
                for (int i = 0; i < lines.length; i++) {
                    int lineNumber = startLine + i;
                    codeWithLineNumbers.append("<span class=\"line-number\">").append(lineNumber).append("</span>");
                    codeWithLineNumbers.append(escapeHtml(lines[i])).append("\n");
                }

                // 使用highlight.js支持的结构，并添加行号类
                html.append("<pre class=\"with-line-numbers\"><code class=\"language-").append(language).append("\">")
                    .append(codeWithLineNumbers)
                    .append("</code></pre>\n");
            } else {
                // 不显示行号
                html.append("<pre><code class=\"language-").append(language).append("\">")
                    .append(escapeHtml(codeSnippet))
                    .append("</code></pre>\n");
            }

            html.append("</div>\n");
        }

        return html.toString();
    }

    /**
     * 将图钉格式化为JSON
     *
     * @param project 当前项目
     * @param pin 要格式化的图钉
     * @return 格式化后的JSON内容
     */
    private static String formatPinAsJSON(Project project, PinEntry pin) {
        StringBuilder json = new StringBuilder();
        json.append("{");

        // 添加元数据
        json.append("\"metadata\": {");
        json.append("\"generator\": \"CodePins - Code Bookmarks\", ");
        json.append("\"plugin_url\": \"https://plugins.jetbrains.com/plugin/27300-codepins--code-bookmarks\", ");
        json.append("\"generated_at\": \"").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\"");
        json.append("}, ");

        // 文件信息
        json.append("\"file\": {");
        json.append("\"path\": \"").append(escapeJson(pin.filePath)).append("\", ");
        json.append("\"name\": \"").append(escapeJson(new File(pin.filePath).getName())).append("\"");
        json.append("}, ");

        // 位置信息
        Document doc = pin.marker.getDocument();
        json.append("\"location\": {");
        if (pin.isBlock) {
            int startLine = doc.getLineNumber(pin.marker.getStartOffset()) + 1;
            int endLine = doc.getLineNumber(pin.marker.getEndOffset()) + 1;
            json.append("\"start_line\": ").append(startLine).append(", ");
            json.append("\"end_line\": ").append(endLine);
        } else {
            int line = doc.getLineNumber(pin.marker.getStartOffset()) + 1;
            json.append("\"line\": ").append(line);
        }
        json.append("}, ");

        // 其他信息
        json.append("\"author\": \"").append(escapeJson(pin.author)).append("\", ");
        json.append("\"timestamp\": ").append(pin.timestamp).append(", ");
        json.append("\"is_block\": ").append(pin.isBlock).append(", ");

        // 标签
        json.append("\"tags\": [");
        List<String> tags = pin.getTags();
        for (int i = 0; i < tags.size(); i++) {
            json.append("\"").append(escapeJson(tags.get(i))).append("\"");
            if (i < tags.size() - 1) {
                json.append(", ");
            }
        }
        json.append("], ");

        // 备注
        json.append("\"note\": \"").append(pin.note != null ? escapeJson(pin.note) : "").append("\", ");

        // 代码内容
        String codeSnippet = getCodeSnippet(project, pin);
        json.append("\"code\": \"").append(escapeJson(codeSnippet)).append("\"");

        json.append("}");
        return json.toString();
    }

    /**
     * 转义JSON字符串
     */
    private static String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    /**
     * 获取代码片段
     *
     * @param project 当前项目
     * @param pin 图钉
     * @return 代码片段
     */
    public static String getCodeSnippet(Project project, PinEntry pin) {
        try {
            Document doc = pin.marker.getDocument();
            int startOffset = pin.marker.getStartOffset();
            int endOffset = pin.marker.getEndOffset();

            if (startOffset >= 0 && endOffset >= startOffset && endOffset <= doc.getTextLength()) {
                return doc.getText(new com.intellij.openapi.util.TextRange(startOffset, endOffset));
            }
        } catch (Exception e) {
            System.out.println("[CodePins] 获取代码片段失败: " + e.getMessage());
        }
        return "";
    }

    /**
     * 获取文件语言
     *
     * @param filePath 文件路径
     * @return 语言标识
     */
    public static String getFileLanguage(String filePath) {
        try {
            String extension = filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase();
            switch (extension) {
                case "java": return "java";
                case "py": return "python";
                case "js": return "javascript";
                case "ts": return "typescript";
                case "html": return "html";
                case "css": return "css";
                case "c": return "c";
                case "cpp": case "cc": case "cxx": return "cpp";
                case "go": return "go";
                case "rs": return "rust";
                case "kt": return "kotlin";
                case "swift": return "swift";
                case "rb": return "ruby";
                case "php": return "php";
                case "sh": return "bash";
                case "sql": return "sql";
                case "xml": return "xml";
                case "json": return "json";
                case "yml": case "yaml": return "yaml";
                case "md": return "markdown";
                default: return extension;
            }
        } catch (Exception e) {
            return "text";
        }
    }

    /**
     * 转义HTML特殊字符
     *
     * @param text 原始文本
     * @return 转义后的文本
     */
    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }

    /**
     * 写入文件
     *
     * @param file 文件
     * @param content 内容
     * @return 是否成功
     */
    private static boolean writeToFile(File file, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
            return true;
        } catch (Exception e) {
            System.out.println("[CodePins] 写入文件失败: " + e.getMessage());
            return false;
        }
    }
}
