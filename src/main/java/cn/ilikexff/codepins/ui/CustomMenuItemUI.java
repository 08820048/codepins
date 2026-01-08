package cn.ilikexff.codepins.ui;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.plaf.basic.BasicMenuItemUI;
import java.awt.*;
import java.util.WeakHashMap;

/**
 * 自定义菜单项UI，为菜单项添加更明显的悬停效果
 */
public class CustomMenuItemUI extends BasicMenuItemUI {

    // 悬停背景色：亮色主题下使用浅蓝色，暗色主题下使用深蓝色
    private static final Color HOVER_BG = new JBColor(
            new Color(210, 230, 250), // 亮色主题
            new Color(45, 90, 148, 200) // 暗色主题
    );

    // 选中背景色：亮色主题下使用深蓝色，暗色主题下使用更深的蓝色
    private static final Color SELECTED_BG = new JBColor(
            new Color(180, 210, 240), // 亮色主题
            new Color(55, 110, 180, 220) // 暗色主题
    );

    // 文本颜色：亮色主题下使用深色，暗色主题下使用浅色
    private static final Color TEXT_COLOR = new JBColor(
            new Color(30, 30, 30), // 亮色主题
            new Color(220, 220, 220) // 暗色主题
    );

    // 状态跟踪：避免重复绘制背景
    private static final WeakHashMap<JMenuItem, Boolean> armedStateMap = new WeakHashMap<>();

    /**
     * 应用自定义UI到菜单项
     *
     * @param menuItem 要应用UI的菜单项
     */
    public static void apply(JMenuItem menuItem) {
        menuItem.setUI(new CustomMenuItemUI());
        menuItem.setOpaque(true);
        menuItem.setForeground(TEXT_COLOR);
        menuItem.setBorder(JBUI.Borders.empty(5, 8));
    }

    @Override
    protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = menuItem.getWidth();
        int height = menuItem.getHeight();

        // 获取当前状态
        boolean isArmed = menuItem.isArmed() || (menuItem instanceof JMenu && menuItem.isSelected());
        boolean isPressed = menuItem.getModel().isPressed();

        // 检查状态是否改变，避免重复绘制
        Boolean lastState = armedStateMap.get(menuItem);
        boolean shouldDraw = (lastState == null || lastState != isArmed);

        if (shouldDraw) {
            armedStateMap.put(menuItem, isArmed);

            // 设置背景颜色
            if (isArmed) {
                // 选中或悬停状态
                g2.setColor(SELECTED_BG);
            } else if (isPressed) {
                // 按下状态
                g2.setColor(SELECTED_BG.darker());
            } else {
                // 正常状态 - 透明背景
                g2.setColor(new Color(0, 0, 0, 0));
            }

            // 绘制圆角矩形背景
            g2.fillRoundRect(2, 1, width - 4, height - 2, 6, 6);
        }

        g2.dispose();
    }
}
