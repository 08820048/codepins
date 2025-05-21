package cn.ilikexff.codepins.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.scale.ScaleContext;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * 图标工具类
 * 用于加载和管理图标
 */
public class IconUtil {
    private static final Logger LOG = Logger.getInstance(IconUtil.class);

    /**
     * 加载图标
     * 使用兼容的方式加载图标，适应不同版本的 IntelliJ Platform
     *
     * @param path 图标路径
     * @param contextClass 上下文类
     * @return 图标
     */
    public static Icon loadIcon(String path, Class<?> contextClass) {
        // 使用SVG图标修复工具加载图标
        if (path.toLowerCase().endsWith(".svg")) {
            return SvgIconFixer.loadIcon(path, contextClass);
        }

        try {
            // 使用标准的 IconLoader.getIcon 方法加载非SVG图标
            Icon icon = IconLoader.getIcon(path, contextClass);

            // 验证图标是否正确加载
            if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
                LOG.warn("Icon loaded with invalid dimensions: " + path +
                         " (width=" + icon.getIconWidth() + ", height=" + icon.getIconHeight() + ")");
                return createFallbackIcon(16);
            }

            return icon;
        } catch (Exception ex) {
            // 如果加载失败，记录详细错误并返回一个备用图标
            LOG.warn("Failed to load icon: " + path + ", error: " + ex.getMessage(), ex);
            return createFallbackIcon(16);
        }
    }

    /**
     * 创建一个备用图标，当原始图标加载失败时使用
     *
     * @param size 图标大小
     * @return 备用图标
     */
    private static Icon createFallbackIcon(int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制一个简单的圆形图标，而不是空白
                g2d.setColor(new Color(100, 100, 100, 200));
                g2d.fillOval(x, y, size, size);

                g2d.setColor(new Color(200, 200, 200));
                g2d.drawOval(x, y, size - 1, size - 1);

                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * 清除图标缓存
     */
    public static void clearIconCache() {
        SvgIconFixer.clearCache();
    }
}
