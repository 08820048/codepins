package cn.ilikexff.codepins.utils;

import cn.ilikexff.codepins.services.LicenseService;
import com.intellij.openapi.project.Project;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 图片生成工具类
 * 用于生成代码卡片图片
 */
public class ImageGenerator {

    // 预定义的主题
    public enum Theme {
        // 基础主题
        DARK("暗色", new Color(40, 44, 52), new Color(171, 178, 191), new Color(97, 218, 251)),
        LIGHT("亮色", new Color(255, 255, 255), new Color(50, 50, 50), new Color(52, 152, 219)),

        // 编辑器主题
        DRACULA("德古拉", new Color(40, 42, 54), new Color(248, 248, 242), new Color(255, 121, 198)),
        MONOKAI("莫诺凯", new Color(39, 40, 34), new Color(248, 248, 242), new Color(249, 38, 114)),
        GITHUB("GitHub", new Color(246, 248, 250), new Color(36, 41, 46), new Color(0, 92, 197)),
        SOLARIZED_LIGHT("日光浴-亮", new Color(253, 246, 227), new Color(101, 123, 131), new Color(38, 139, 210)),
        SOLARIZED_DARK("日光浴-暗", new Color(0, 43, 54), new Color(131, 148, 150), new Color(38, 139, 210)),

        // 新增主题
        NORD("Nord", new Color(46, 52, 64), new Color(216, 222, 233), new Color(94, 129, 172)),
        TOKYO_NIGHT("东京之夜", new Color(26, 27, 38), new Color(169, 177, 214), new Color(125, 207, 255)),
        TOKYO_NIGHT_STORM("东京之夜-风暴", new Color(36, 40, 59), new Color(169, 177, 214), new Color(125, 207, 255)),
        TOKYO_NIGHT_LIGHT("东京之夜-亮", new Color(213, 214, 219), new Color(52, 59, 88), new Color(52, 84, 138)),
        MATERIAL_DARKER("材质-暗", new Color(33, 33, 33), new Color(238, 255, 255), new Color(137, 221, 255)),
        MATERIAL_LIGHTER("材质-亮", new Color(250, 250, 250), new Color(80, 80, 80), new Color(39, 125, 161)),
        MATERIAL_PALENIGHT("材质-淡夜", new Color(41, 45, 62), new Color(167, 178, 205), new Color(137, 221, 255)),
        MATERIAL_OCEAN("材质-海洋", new Color(15, 17, 26), new Color(143, 159, 174), new Color(137, 221, 255)),
        ONE_DARK("One Dark", new Color(40, 44, 52), new Color(171, 178, 191), new Color(229, 192, 123)),
        ONE_LIGHT("One Light", new Color(250, 250, 250), new Color(56, 58, 66), new Color(229, 192, 123)),
        GRUVBOX_DARK("Gruvbox-暗", new Color(40, 40, 40), new Color(235, 219, 178), new Color(250, 189, 47)),
        GRUVBOX_LIGHT("Gruvbox-亮", new Color(251, 241, 199), new Color(60, 56, 54), new Color(204, 36, 29)),
        NIGHT_OWL("夜猿", new Color(1, 22, 39), new Color(214, 222, 235), new Color(127, 219, 202)),
        NIGHT_OWL_LIGHT("夜猿-亮", new Color(255, 255, 255), new Color(64, 74, 83), new Color(49, 120, 198));

        private final String displayName;
        private final Color background;
        private final Color foreground;
        private final Color accent;

        Theme(String displayName, Color background, Color foreground, Color accent) {
            this.displayName = displayName;
            this.background = background;
            this.foreground = foreground;
            this.accent = accent;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Color getBackground() {
            return background;
        }

        public Color getForeground() {
            return foreground;
        }

        public Color getAccent() {
            return accent;
        }
    }

    // 语言颜色映射
    private static final Map<String, Color> LANGUAGE_COLORS = new HashMap<>();

    static {
        LANGUAGE_COLORS.put("java", new Color(176, 114, 25));
        LANGUAGE_COLORS.put("python", new Color(53, 114, 165));
        LANGUAGE_COLORS.put("javascript", new Color(241, 224, 90));
        LANGUAGE_COLORS.put("typescript", new Color(49, 120, 198));
        LANGUAGE_COLORS.put("html", new Color(227, 76, 38));
        LANGUAGE_COLORS.put("css", new Color(86, 61, 124));
        LANGUAGE_COLORS.put("c", new Color(85, 85, 85));
        LANGUAGE_COLORS.put("cpp", new Color(243, 75, 125));
        LANGUAGE_COLORS.put("csharp", new Color(23, 134, 0));
        LANGUAGE_COLORS.put("go", new Color(0, 173, 216));
        LANGUAGE_COLORS.put("rust", new Color(222, 165, 132));
        LANGUAGE_COLORS.put("kotlin", new Color(143, 0, 255));
        LANGUAGE_COLORS.put("swift", new Color(255, 172, 69));
        LANGUAGE_COLORS.put("php", new Color(79, 93, 149));
        LANGUAGE_COLORS.put("ruby", new Color(204, 52, 45));
    }

    /**
     * 生成代码卡片图片
     *
     * @param code     代码内容
     * @param language 编程语言
     * @param theme    主题
     * @param width    宽度
     * @return 图片文件
     */
    public static File generateCodeCard(Project project, String code, String language, Theme theme, int width) throws Exception {
        // 创建临时文件
        File tempFile = File.createTempFile("codepins_", ".png");

        // 计算适合的高度
        int height = calculateImageHeight(code, width);

        // 生成图片
        BufferedImage image = createCodeCardImage(project, code, language, theme, width, height);

        // 保存图片
        ImageIO.write(image, "PNG", tempFile);

        return tempFile;
    }

    /**
     * 生成代码卡片图片
     *
     * @param project  当前项目
     * @param code     代码内容
     * @param language 编程语言
     * @param theme    主题
     * @param width    宽度
     * @return 图片字节数组
     */
    public static byte[] generateCodeCardBytes(Project project, String code, String language, Theme theme, int width) throws Exception {
        // 计算适合的高度
        int height = calculateImageHeight(code, width);

        // 生成图片
        BufferedImage image = createCodeCardImage(project, code, language, theme, width, height);

        // 转换为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);

        return baos.toByteArray();
    }

    /**
     * 创建代码卡片图片
     *
     * @param code     代码内容
     * @param language 编程语言
     * @param theme    主题
     * @param width    宽度
     * @param height   高度
     * @return 图片
     */
    /**
     * 计算图片高度
     *
     * @param code 代码内容
     * @param width 图片宽度
     * @return 计算出的高度
     */
    public static int calculateImageHeight(String code, int width) {
        // 初始高度包含顶部空间和底部空间
        int baseHeight = 80; // 顶部和底部的空间

        // 创建一个临时的Graphics2D对象来计算文本高度
        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = tempImage.createGraphics();
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        FontMetrics fontMetrics = g2d.getFontMetrics();

        // 计算行高
        int lineHeight = fontMetrics.getHeight();

        // 计算行数
        String[] lines = code.split("\\n");
        int lineCount = lines.length;

        // 计算文本区域高度
        int textHeight = lineHeight * lineCount;

        // 添加额外的空间用于行间距
        int lineSpacing = 2; // 每行额外的空间
        int totalLineSpacing = (lineCount - 1) * lineSpacing;

        // 释放资源
        g2d.dispose();

        // 计算总高度，确保有足够的空间
        int totalHeight = baseHeight + textHeight + totalLineSpacing;

        // 添加额外的空间用于更好的视觉效果
        totalHeight += 40;

        // 确保高度不会太小
        return Math.max(totalHeight, 200);
    }

    /**
     * 创建预览图片
     *
     * @param code 代码内容
     * @param language 编程语言
     * @param theme 主题
     * @param width 宽度
     * @return 预览图片
     */
    public static BufferedImage createPreviewImage(Project project, String code, String language, Theme theme, int width) {
        // 计算适合的高度
        int height = calculateImageHeight(code, width);

        // 创建图片
        return createCodeCardImage(project, code, language, theme, width, height);
    }

    /**
     * 创建代码卡片图片
     */
    private static BufferedImage createCodeCardImage(Project project, String code, String language, Theme theme, int width, int height) {
        // 创建图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 绘制背景
        g2d.setColor(theme.getBackground());
        g2d.fillRect(0, 0, width, height);

        // 绘制背景渐变
        Paint originalPaint = g2d.getPaint();
        GradientPaint gradientPaint = new GradientPaint(
                0, 0,
                adjustColorBrightness(theme.getBackground(), 1.1f),
                width, height,
                theme.getBackground());
        g2d.setPaint(gradientPaint);
        g2d.fillRect(0, 0, width, height);
        g2d.setPaint(originalPaint);

        // 绘制窗口控制按钮
        int buttonSize = 12;
        int buttonMargin = 8;
        int buttonY = 20;

        // 绘制按钮光晕效果
        // 红色按钮
        drawGlowingButton(g2d, buttonMargin, buttonY, buttonSize, new Color(255, 95, 86));

        // 黄色按钮
        drawGlowingButton(g2d, buttonMargin * 2 + buttonSize, buttonY, buttonSize, new Color(255, 189, 46));

        // 绿色按钮
        drawGlowingButton(g2d, buttonMargin * 3 + buttonSize * 2, buttonY, buttonSize, new Color(39, 201, 63));

        // 绘制语言文本
        int languageLabelX = width - 100;
        int languageLabelY = 25;

        // 获取语言颜色
        Color languageColor = LANGUAGE_COLORS.getOrDefault(language.toLowerCase(), theme.getAccent());

        // 绘制语言文本
        g2d.setColor(languageColor);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fontMetrics = g2d.getFontMetrics();
        String languageText = language.toUpperCase();
        int textWidth = fontMetrics.stringWidth(languageText);
        g2d.drawString(languageText, languageLabelX - textWidth, languageLabelY);

        // 绘制代码
        g2d.setColor(theme.getForeground());
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        fontMetrics = g2d.getFontMetrics();

        // 分割代码行
        String[] lines = code.split("\\n");
        int lineHeight = fontMetrics.getHeight();
        int codeStartY = 60;
        int codeStartX = 30;
        int currentY = codeStartY;

        // 创建语法高亮颜色
        SyntaxHighlighter.SyntaxColors syntaxColors = new SyntaxHighlighter.SyntaxColors(theme.getForeground());

        // 检查语言是否支持
        boolean isLanguageSupported = SyntaxHighlighter.isLanguageSupported(language);

        // 绘制每一行代码
        for (String line : lines) {
            if (isLanguageSupported) {
                // 使用增强的语法高亮
                Color lineColor = SyntaxHighlighter.getLineColor(line, language, syntaxColors);
                g2d.setColor(lineColor);
            } else {
                // 如果语言不支持，使用简单的语法高亮
                if (line.trim().startsWith("//") || line.trim().startsWith("#")) {
                    // 注释
                    g2d.setColor(syntaxColors.commentColor);
                } else if (line.contains("\"") || line.contains("'")) {
                    // 字符串
                    g2d.setColor(syntaxColors.stringColor);
                } else if (line.contains("class ") || line.contains("function ") || line.contains("def ")) {
                    // 关键字
                    g2d.setColor(syntaxColors.keywordColor);
                } else {
                    // 普通文本
                    g2d.setColor(theme.getForeground());
                }
            }

            g2d.drawString(line, codeStartX, currentY);
            currentY += lineHeight;
        }

        // 绘制水印
        if (!WatermarkManager.removeWatermark(project)) {
            // 非付费用户或付费用户选择显示水印
            g2d.setColor(new Color(theme.getForeground().getRed(),
                                   theme.getForeground().getGreen(),
                                   theme.getForeground().getBlue(), 50)); // 半透明
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            String watermark = "Generated by CodePins - Code Bookmarks";
            fontMetrics = g2d.getFontMetrics();
            textWidth = fontMetrics.stringWidth(watermark);
            g2d.drawString(watermark, width - textWidth - 20, height - 20);
        }

        // 绘制底部装饰条
        int decorationHeight = 5;
        GradientPaint bottomGradient = new GradientPaint(
                0, height - decorationHeight,
                theme.getAccent(),
                width, height - decorationHeight,
                adjustColorBrightness(theme.getAccent(), 1.5f));
        g2d.setPaint(bottomGradient);
        g2d.fillRect(0, height - decorationHeight, width, decorationHeight);

        // 释放资源
        g2d.dispose();

        return image;
    }

    /**
     * 绘制发光按钮
     *
     * @param g2d 图形上下文
     * @param x 按钮位置
     * @param y 按钮位置
     * @param size 按钮大小
     * @param color 按钮颜色
     */
    private static void drawGlowingButton(Graphics2D g2d, int x, int y, int size, Color color) {
        // 保存原始设置
        Paint originalPaint = g2d.getPaint();
        Composite originalComposite = g2d.getComposite();

        // 绘制光晕效果
        Color lighterColor = adjustColorBrightness(color, 1.5f);
        RadialGradientPaint gradient = new RadialGradientPaint(
                new Point2D.Float(x + size/2.0f, y + size/2.0f),
                size * 0.8f,
                new float[] {0.0f, 1.0f},
                new Color[] {lighterColor, color}
        );

        g2d.setPaint(gradient);
        g2d.fillOval(x, y, size, size);

        // 绘制高光
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x + size/4, y + size/4, size/4, size/4);

        // 恢复原始设置
        g2d.setPaint(originalPaint);
        g2d.setComposite(originalComposite);
    }

    /**
     * 调整颜色亮度
     *
     * @param color 原始颜色
     * @param factor 亮度因子，大于1增亮，小于1变暗
     * @return 调整后的颜色
     */
    private static Color adjustColorBrightness(Color color, float factor) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hsb[2] = Math.min(1.0f, hsb[2] * factor); // 调整亮度
        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }

    /**
     * 生成SVG格式的代码卡片
     *
     * @param code     代码内容
     * @param language 编程语言
     * @param theme    主题
     * @param width    宽度
     * @return SVG内容
     */
    public static String generateSVG(Project project, String code, String language, Theme theme, int width) throws Exception {
        // 计算适合的高度
        int height = calculateImageHeight(code, width);

        // 创建SVG内容
        StringBuilder svg = new StringBuilder();

        // SVG头部
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        svg.append("<svg width=\"").append(width).append("\" height=\"").append(height)
           .append("\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n");

        // 背景
        Color bgColor = theme.getBackground();
        svg.append("<rect x=\"0\" y=\"0\" width=\"").append(width).append("\" height=\"").append(height)
           .append("\" fill=\"rgb(").append(bgColor.getRed()).append(",").append(bgColor.getGreen()).append(",").append(bgColor.getBlue()).append(")\" />\n");

        // 背景渐变
        Color lighterBg = adjustColorBrightness(bgColor, 1.1f);
        svg.append("<defs>\n");
        svg.append("  <linearGradient id=\"bgGradient\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"100%\">\n");
        svg.append("    <stop offset=\"0%\" style=\"stop-color:rgb(").append(lighterBg.getRed()).append(",").append(lighterBg.getGreen()).append(",").append(lighterBg.getBlue()).append(");stop-opacity:1\" />\n");
        svg.append("    <stop offset=\"100%\" style=\"stop-color:rgb(").append(bgColor.getRed()).append(",").append(bgColor.getGreen()).append(",").append(bgColor.getBlue()).append(");stop-opacity:1\" />\n");
        svg.append("  </linearGradient>\n");
        svg.append("</defs>\n");

        svg.append("<rect x=\"0\" y=\"0\" width=\"").append(width).append("\" height=\"").append(height)
           .append("\" fill=\"url(#bgGradient)\" />\n");

        // 窗口控制按钮
        int buttonSize = 12;
        int buttonMargin = 8;
        int buttonY = 20;

        // 红色按钮
        svg.append("<circle cx=\"").append(buttonMargin + buttonSize/2).append("\" cy=\"").append(buttonY + buttonSize/2)
           .append("\" r=\"").append(buttonSize/2).append("\" fill=\"rgb(255,95,86)\" />\n");

        // 黄色按钮
        svg.append("<circle cx=\"").append(buttonMargin*2 + buttonSize + buttonSize/2).append("\" cy=\"").append(buttonY + buttonSize/2)
           .append("\" r=\"").append(buttonSize/2).append("\" fill=\"rgb(255,189,46)\" />\n");

        // 绿色按钮
        svg.append("<circle cx=\"").append(buttonMargin*3 + buttonSize*2 + buttonSize/2).append("\" cy=\"").append(buttonY + buttonSize/2)
           .append("\" r=\"").append(buttonSize/2).append("\" fill=\"rgb(39,201,63)\" />\n");

        // 语言文本
        int languageLabelX = width - 100;
        int languageLabelY = 25;

        // 获取语言颜色
        Color languageColor = LANGUAGE_COLORS.getOrDefault(language.toLowerCase(), theme.getAccent());

        // 语言文本
        String languageText = language.toUpperCase();
        svg.append("<text x=\"").append(languageLabelX).append("\" y=\"").append(languageLabelY)
           .append("\" font-family=\"SansSerif\" font-size=\"14\" font-weight=\"bold\" fill=\"rgb(")
           .append(languageColor.getRed()).append(",").append(languageColor.getGreen()).append(",").append(languageColor.getBlue())
           .append(")\" text-anchor=\"end\">").append(languageText).append("</text>\n");

        // 代码
        int lineHeight = 18; // 估计行高
        int codeStartY = 60;
        int codeStartX = 30;
        int currentY = codeStartY;

        // 分割代码行
        String[] lines = code.split("\\n");

        // 创建语法高亮颜色
        SyntaxHighlighter.SyntaxColors syntaxColors = new SyntaxHighlighter.SyntaxColors(theme.getForeground());

        // 检查语言是否支持
        boolean isLanguageSupported = SyntaxHighlighter.isLanguageSupported(language);

        // 绘制每一行代码
        for (String line : lines) {
            // 转义XML特殊字符
            String escapedLine = line.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");

            // 确定颜色
            Color lineColor;
            if (isLanguageSupported) {
                // 使用增强的语法高亮
                lineColor = SyntaxHighlighter.getLineColor(line, language, syntaxColors);
            } else {
                // 如果语言不支持，使用简单的语法高亮
                if (line.trim().startsWith("//") || line.trim().startsWith("#")) {
                    // 注释
                    lineColor = syntaxColors.commentColor;
                } else if (line.contains("\"") || line.contains("'")) {
                    // 字符串
                    lineColor = syntaxColors.stringColor;
                } else if (line.contains("class ") || line.contains("function ") || line.contains("def ")) {
                    // 关键字
                    lineColor = syntaxColors.keywordColor;
                } else {
                    // 普通文本
                    lineColor = theme.getForeground();
                }
            }

            // 添加代码行
            svg.append("<text x=\"").append(codeStartX).append("\" y=\"").append(currentY)
               .append("\" font-family=\"monospace\" font-size=\"14\" fill=\"rgb(").append(lineColor.getRed()).append(",").append(lineColor.getGreen()).append(",").append(lineColor.getBlue()).append(")\">").append(escapedLine).append("</text>\n");

            currentY += lineHeight;
        }

        // 水印
        if (!WatermarkManager.removeWatermark(project)) {
            // 非付费用户或付费用户选择显示水印
            Color fgColor = theme.getForeground();
            String watermark = "Generated by CodePins - Code Bookmarks";
            svg.append("<text x=\"").append(width - 20).append("\" y=\"").append(height - 20)
               .append("\" font-family=\"SansSerif\" font-size=\"12\" font-weight=\"bold\" fill=\"rgba(").append(fgColor.getRed()).append(",").append(fgColor.getGreen()).append(",").append(fgColor.getBlue()).append(",0.2)\" text-anchor=\"end\">").append(watermark).append("</text>\n");
        }

        // 底部装饰条
        int decorationHeight = 5;
        Color accentColor = theme.getAccent();
        Color lighterAccent = adjustColorBrightness(accentColor, 1.5f);

        svg.append("<defs>\n");
        svg.append("  <linearGradient id=\"bottomGradient\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"0%\">\n");
        svg.append("    <stop offset=\"0%\" style=\"stop-color:rgb(").append(accentColor.getRed()).append(",").append(accentColor.getGreen()).append(",").append(accentColor.getBlue()).append(");stop-opacity:1\" />\n");
        svg.append("    <stop offset=\"100%\" style=\"stop-color:rgb(").append(lighterAccent.getRed()).append(",").append(lighterAccent.getGreen()).append(",").append(lighterAccent.getBlue()).append(");stop-opacity:1\" />\n");
        svg.append("  </linearGradient>\n");
        svg.append("</defs>\n");

        svg.append("<rect x=\"0\" y=\"").append(height - decorationHeight).append("\" width=\"").append(width).append("\" height=\"").append(decorationHeight)
           .append("\" fill=\"url(#bottomGradient)\" />\n");

        // 元数据
        String metadata = "Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) +
                          " | Plugin: https://plugins.jetbrains.com/plugin/27300-codepins--code-bookmarks";
        svg.append("<metadata>\n");
        svg.append("  <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n");
        svg.append("    <rdf:Description dc:title=\"CodePins Code Share\" dc:description=\"Code shared via CodePins plugin\" dc:creator=\"CodePins\" dc:date=\"").append(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).append("\" />\n");
        svg.append("  </rdf:RDF>\n");
        svg.append("</metadata>\n");

        // SVG结尾
        svg.append("</svg>");

        return svg.toString();
    }

    /**
     * 将SVG转换为PNG
     *
     * @param svg    SVG内容
     * @param width  宽度
     * @param height 高度
     * @return PNG图片字节数组
     */
    public static byte[] convertSVGToPNG(String svg, float width, float height) throws Exception {
        // 创建转换器
        org.apache.batik.transcoder.image.PNGTranscoder transcoder = new org.apache.batik.transcoder.image.PNGTranscoder();

        // 设置转换参数
        transcoder.addTranscodingHint(org.apache.batik.transcoder.image.PNGTranscoder.KEY_WIDTH, width);
        transcoder.addTranscodingHint(org.apache.batik.transcoder.image.PNGTranscoder.KEY_HEIGHT, height);

        // 创建输入流
        org.apache.batik.transcoder.TranscoderInput input = new org.apache.batik.transcoder.TranscoderInput(
                new java.io.StringReader(svg));

        // 创建输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        org.apache.batik.transcoder.TranscoderOutput output = new org.apache.batik.transcoder.TranscoderOutput(outputStream);

        // 执行转换
        transcoder.transcode(input, output);

        // 关闭输出流
        outputStream.flush();
        outputStream.close();

        return outputStream.toByteArray();
    }
}
