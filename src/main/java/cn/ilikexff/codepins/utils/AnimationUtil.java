package cn.ilikexff.codepins.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 动画工具类
 * 用于实现UI动画效果
 */
public class AnimationUtil {

    /**
     * 按钮点击效果
     * 
     * @param button 按钮组件
     */
    public static void buttonClickEffect(AbstractButton button) {
        if (button == null) return;
        
        Color originalBackground = button.getBackground();
        Color originalForeground = button.getForeground();
        
        // 保存原始边框
        javax.swing.border.Border originalBorder = button.getBorder();
        
        // 创建高亮边框
        javax.swing.border.Border highlightBorder = BorderFactory.createLineBorder(new Color(0, 120, 215), 1);
        
        // 设置高亮效果
        button.setBackground(new Color(230, 240, 250));
        button.setForeground(new Color(0, 90, 160));
        button.setBorder(highlightBorder);
        
        // 创建定时器，在一段时间后恢复原始状态
        Timer timer = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                button.setBackground(originalBackground);
                button.setForeground(originalForeground);
                button.setBorder(originalBorder);
            }
        });
        
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * 淡入效果
     * 
     * @param component 组件
     * @param duration 持续时间（毫秒）
     */
    public static void fadeIn(JComponent component, int duration) {
        if (component == null) return;
        
        // 设置初始透明度
        component.setOpaque(false);
        component.setVisible(true);
        
        // 创建定时器，逐渐增加透明度
        final float[] alpha = {0.0f};
        final int steps = 10;
        final int delay = duration / steps;
        
        Timer timer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha[0] += 1.0f / steps;
                if (alpha[0] >= 1.0f) {
                    alpha[0] = 1.0f;
                    ((Timer)e.getSource()).stop();
                    component.setOpaque(true);
                    component.repaint();
                } else {
                    component.setOpaque(false);
                    component.repaint();
                }
            }
        });
        
        timer.start();
    }
    
    /**
     * 淡出效果
     * 
     * @param component 组件
     * @param duration 持续时间（毫秒）
     */
    public static void fadeOut(JComponent component, int duration) {
        if (component == null) return;
        
        // 设置初始透明度
        component.setOpaque(true);
        component.setVisible(true);
        
        // 创建定时器，逐渐减少透明度
        final float[] alpha = {1.0f};
        final int steps = 10;
        final int delay = duration / steps;
        
        Timer timer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha[0] -= 1.0f / steps;
                if (alpha[0] <= 0.0f) {
                    alpha[0] = 0.0f;
                    ((Timer)e.getSource()).stop();
                    component.setVisible(false);
                    component.repaint();
                } else {
                    component.setOpaque(false);
                    component.repaint();
                }
            }
        });
        
        timer.start();
    }
    
    /**
     * 滑入效果
     * 
     * @param component 组件
     * @param direction 方向（SwingConstants.LEFT, RIGHT, TOP, BOTTOM）
     * @param duration 持续时间（毫秒）
     */
    public static void slideIn(JComponent component, int direction, int duration) {
        if (component == null) return;
        
        // 保存原始位置
        Rectangle originalBounds = component.getBounds();
        Rectangle targetBounds = new Rectangle(originalBounds);
        
        // 设置初始位置（在屏幕外）
        switch (direction) {
            case SwingConstants.LEFT:
                component.setBounds(new Rectangle(
                        -originalBounds.width,
                        originalBounds.y,
                        originalBounds.width,
                        originalBounds.height
                ));
                break;
            case SwingConstants.RIGHT:
                component.setBounds(new Rectangle(
                        component.getParent().getWidth(),
                        originalBounds.y,
                        originalBounds.width,
                        originalBounds.height
                ));
                break;
            case SwingConstants.TOP:
                component.setBounds(new Rectangle(
                        originalBounds.x,
                        -originalBounds.height,
                        originalBounds.width,
                        originalBounds.height
                ));
                break;
            case SwingConstants.BOTTOM:
                component.setBounds(new Rectangle(
                        originalBounds.x,
                        component.getParent().getHeight(),
                        originalBounds.width,
                        originalBounds.height
                ));
                break;
        }
        
        component.setVisible(true);
        
        // 创建定时器，逐渐移动到目标位置
        final int steps = 10;
        final int delay = duration / steps;
        final int[] step = {0};
        
        Timer timer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                step[0]++;
                if (step[0] >= steps) {
                    component.setBounds(targetBounds);
                    ((Timer)e.getSource()).stop();
                } else {
                    float progress = (float)step[0] / steps;
                    int x = (int)(component.getBounds().x + (targetBounds.x - component.getBounds().x) * progress);
                    int y = (int)(component.getBounds().y + (targetBounds.y - component.getBounds().y) * progress);
                    
                    component.setBounds(new Rectangle(
                            x, y, originalBounds.width, originalBounds.height
                    ));
                }
                component.repaint();
            }
        });
        
        timer.start();
    }
}
