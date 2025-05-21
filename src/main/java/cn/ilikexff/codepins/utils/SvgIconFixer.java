package cn.ilikexff.codepins.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SVG图标修复工具类
 * 用于解决IntelliJ IDEA 2024.1+版本中SVG图标显示为黑点的问题
 */
public class SvgIconFixer {
    private static final Logger LOG = Logger.getInstance(SvgIconFixer.class);
    private static final Map<String, Icon> ICON_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 加载并修复SVG图标
     * 
     * @param path 图标路径
     * @param contextClass 上下文类
     * @return 修复后的图标
     */
    public static Icon loadIcon(String path, Class<?> contextClass) {
        // 首先尝试从缓存中获取
        String cacheKey = path + "#" + contextClass.getName();
        if (ICON_CACHE.containsKey(cacheKey)) {
            return ICON_CACHE.get(cacheKey);
        }
        
        try {
            // 尝试直接加载图标
            Icon icon = IconLoader.getIcon(path, contextClass);
            
            // 验证图标是否正确加载
            if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                ICON_CACHE.put(cacheKey, icon);
                return icon;
            }
            
            // 如果图标尺寸无效，尝试修复SVG
            Icon fixedIcon = fixSvgIcon(path, contextClass);
            if (fixedIcon != null) {
                ICON_CACHE.put(cacheKey, fixedIcon);
                return fixedIcon;
            }
            
            // 如果修复失败，返回备用图标
            Icon fallbackIcon = createFallbackIcon(16);
            ICON_CACHE.put(cacheKey, fallbackIcon);
            return fallbackIcon;
        } catch (Exception ex) {
            LOG.warn("Failed to load icon: " + path + ", error: " + ex.getMessage(), ex);
            Icon fallbackIcon = createFallbackIcon(16);
            ICON_CACHE.put(cacheKey, fallbackIcon);
            return fallbackIcon;
        }
    }
    
    /**
     * 修复SVG图标
     * 
     * @param path 图标路径
     * @param contextClass 上下文类
     * @return 修复后的图标，如果修复失败则返回null
     */
    private static Icon fixSvgIcon(String path, Class<?> contextClass) {
        try {
            // 读取SVG文件内容
            try (InputStream is = contextClass.getResourceAsStream(path)) {
                if (is == null) {
                    LOG.warn("SVG file not found: " + path);
                    return null;
                }
                
                byte[] bytes = is.readAllBytes();
                String svgContent = new String(bytes, StandardCharsets.UTF_8);
                
                // 确保SVG有正确的尺寸属性
                if (!svgContent.contains("width=") || !svgContent.contains("height=")) {
                    LOG.warn("SVG file missing width or height attributes: " + path);
                    return null;
                }
                
                // 尝试再次加载图标
                return IconLoader.getIcon(path, contextClass);
            }
        } catch (Exception ex) {
            LOG.warn("Failed to fix SVG icon: " + path + ", error: " + ex.getMessage(), ex);
            return null;
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
    public static void clearCache() {
        ICON_CACHE.clear();
    }
}
