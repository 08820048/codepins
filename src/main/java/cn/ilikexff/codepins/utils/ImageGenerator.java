package cn.ilikexff.codepins.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 图片生成工具类
 * 用于生成代码卡片图片
 */
public class ImageGenerator {

    // 预定义的主题
    public enum Theme {
        DARK("暗色", new Color(40, 44, 52), new Color(171, 178, 191), new Color(97, 218, 251)),
        LIGHT("亮色", new Color(255, 255, 255), new Color(50, 50, 50), new Color(52, 152, 219)),
        DRACULA("德古拉", new Color(40, 42, 54), new Color(248, 248, 242), new Color(255, 121, 198)),
        MONOKAI("莫诺凯", new Color(39, 40, 34), new Color(248, 248, 242), new Color(249, 38, 114)),
        GITHUB("GitHub", new Color(246, 248, 250), new Color(36, 41, 46), new Color(0, 92, 197)),
        SOLARIZED("日光浴", new Color(253, 246, 227), new Color(101, 123, 131), new Color(38, 139, 210));

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
    public static File generateCodeCard(String code, String language, Theme theme, int width) throws Exception {
        // 创建临时文件
        File tempFile = File.createTempFile("codepins_", ".png");

        // 计算适合的高度
        int height = calculateImageHeight(code, width);

        // 生成图片
        BufferedImage image = createCodeCardImage(code, language, theme, width, height);

        // 保存图片
        ImageIO.write(image, "PNG", tempFile);

        return tempFile;
    }

    /**
     * 生成代码卡片图片
     *
     * @param code     代码内容
     * @param language 编程语言
     * @param theme    主题
     * @param width    宽度
     * @return 图片字节数组
     */
    public static byte[] generateCodeCardBytes(String code, String language, Theme theme, int width) throws Exception {
        // 计算适合的高度
        int height = calculateImageHeight(code, width);

        // 生成图片
        BufferedImage image = createCodeCardImage(code, language, theme, width, height);

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
    private static int calculateImageHeight(String code, int width) {
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
     * 创建代码卡片图片
     */
    private static BufferedImage createCodeCardImage(String code, String language, Theme theme, int width, int height) {
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

        // 绘制语言标签
        int languageLabelX = width - 150;
        int languageLabelY = 20;
        int languageLabelWidth = 120;
        int languageLabelHeight = 24;

        // 获取语言颜色
        Color languageColor = LANGUAGE_COLORS.getOrDefault(language.toLowerCase(), theme.getAccent());

        // 绘制语言标签背景
        // 先绘制光晕效果
        Color lighterColor = adjustColorBrightness(languageColor, 1.3f);
        GradientPaint labelGradient = new GradientPaint(
                languageLabelX, languageLabelY,
                lighterColor,
                languageLabelX, languageLabelY + languageLabelHeight,
                languageColor);
        g2d.setPaint(labelGradient);

        RoundRectangle2D.Double languageRect = new RoundRectangle2D.Double(
                languageLabelX, languageLabelY, languageLabelWidth, languageLabelHeight, 12, 12);
        g2d.fill(languageRect);
        g2d.setPaint(originalPaint);

        // 绘制标签边框
        g2d.setColor(adjustColorBrightness(languageColor, 0.8f));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(languageRect);

        // 绘制语言文本
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fontMetrics = g2d.getFontMetrics();
        String languageText = language.toUpperCase();
        int textWidth = fontMetrics.stringWidth(languageText);
        g2d.drawString(languageText, languageLabelX + (languageLabelWidth - textWidth) / 2,
                languageLabelY + languageLabelHeight - (languageLabelHeight - fontMetrics.getHeight()) / 2 - fontMetrics.getDescent());

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

        // 绘制每一行代码
        for (String line : lines) {
            // 简单的语法高亮（仅作示例，实际应使用更复杂的解析器）
            if (line.trim().startsWith("//") || line.trim().startsWith("#")) {
                // 注释
                g2d.setColor(new Color(92, 99, 112));
            } else if (line.contains("\"") || line.contains("'")) {
                // 字符串
                g2d.setColor(new Color(152, 195, 121));
            } else if (line.contains("class ") || line.contains("function ") || line.contains("def ")) {
                // 关键字
                g2d.setColor(new Color(198, 120, 221));
            } else {
                // 普通文本
                g2d.setColor(theme.getForeground());
            }

            g2d.drawString(line, codeStartX, currentY);
            currentY += lineHeight;
        }

        // 绘制水印
        g2d.setColor(new Color(theme.getForeground().getRed(),
                               theme.getForeground().getGreen(),
                               theme.getForeground().getBlue(), 50)); // 半透明
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        String watermark = "Generated by CodePins - Code Bookmarks";
        fontMetrics = g2d.getFontMetrics();
        textWidth = fontMetrics.stringWidth(watermark);
        g2d.drawString(watermark, width - textWidth - 20, height - 20);

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

}
