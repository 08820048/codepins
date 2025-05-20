package cn.ilikexff.codepins.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * 动画工具类
 * 提供各种 UI 动画效果
 */
public class AnimationUtil {

    /**
     * 淡入效果
     *
     * @param component 要应用动画的组件
     * @param duration 动画持续时间（毫秒）
     */
    public static void fadeIn(JComponent component, int duration) {
        fadeIn(component, duration, null);
    }

    /**
     * 淡入效果，带完成回调
     *
     * @param component 要应用动画的组件
     * @param duration 动画持续时间（毫秒）
     * @param onComplete 动画完成后的回调
     */
    public static void fadeIn(JComponent component, int duration, Runnable onComplete) {
        component.setVisible(true);
        component.setOpaque(false);

        // 使用透明度合成模式实现淡入效果
        float startAlpha = 0.0f;
        component.putClientProperty("alpha", startAlpha);

        animateProperty(
                duration,
                progress -> {
                    component.putClientProperty("alpha", progress);
                    component.repaint();
                },
                () -> {
                    component.putClientProperty("alpha", 1.0f);
                    component.repaint();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
        );
    }

    /**
     * 淡出效果
     *
     * @param component 要应用动画的组件
     * @param duration 动画持续时间（毫秒）
     */
    public static void fadeOut(JComponent component, int duration) {
        fadeOut(component, duration, null);
    }

    /**
     * 淡出效果，带完成回调
     *
     * @param component 要应用动画的组件
     * @param duration 动画持续时间（毫秒）
     * @param onComplete 动画完成后的回调
     */
    public static void fadeOut(JComponent component, int duration, Runnable onComplete) {
        component.setOpaque(false);
        component.putClientProperty("alpha", 1.0f);

        animateProperty(
                duration,
                progress -> {
                    component.putClientProperty("alpha", 1.0f - progress);
                    component.repaint();
                },
                () -> {
                    component.putClientProperty("alpha", 0.0f);
                    component.setVisible(false);
                    component.repaint();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
        );
    }

    /**
     * 高度展开动画
     *
     * @param component 要应用动画的组件
     * @param targetHeight 目标高度
     * @param duration 动画持续时间（毫秒）
     */
    public static void expandHeight(JComponent component, int targetHeight, int duration) {
        expandHeight(component, targetHeight, duration, null);
    }

    /**
     * 高度展开动画，带完成回调
     *
     * @param component 要应用动画的组件
     * @param targetHeight 目标高度
     * @param duration 动画持续时间（毫秒）
     * @param onComplete 动画完成后的回调
     */
    public static void expandHeight(JComponent component, int targetHeight, int duration, Runnable onComplete) {
        int startHeight = component.getHeight();

        animateProperty(
                duration,
                progress -> {
                    int currentHeight = startHeight + (int)((targetHeight - startHeight) * progress);
                    component.setPreferredSize(new Dimension(component.getWidth(), currentHeight));
                    component.revalidate();
                },
                () -> {
                    component.setPreferredSize(new Dimension(component.getWidth(), targetHeight));
                    component.revalidate();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
        );
    }

    /**
     * 高度收缩动画
     *
     * @param component 要应用动画的组件
     * @param targetHeight 目标高度
     * @param duration 动画持续时间（毫秒）
     */
    public static void collapseHeight(JComponent component, int targetHeight, int duration) {
        collapseHeight(component, targetHeight, duration, null);
    }

    /**
     * 高度收缩动画，带完成回调
     *
     * @param component 要应用动画的组件
     * @param targetHeight 目标高度
     * @param duration 动画持续时间（毫秒）
     * @param onComplete 动画完成后的回调
     */
    public static void collapseHeight(JComponent component, int targetHeight, int duration, Runnable onComplete) {
        int startHeight = component.getHeight();

        animateProperty(
                duration,
                progress -> {
                    int currentHeight = startHeight - (int)((startHeight - targetHeight) * progress);
                    component.setPreferredSize(new Dimension(component.getWidth(), currentHeight));
                    component.revalidate();
                },
                () -> {
                    component.setPreferredSize(new Dimension(component.getWidth(), targetHeight));
                    component.revalidate();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
        );
    }

    /**
     * 缩放动画
     *
     * @param component 要应用动画的组件
     * @param startScale 起始缩放比例
     * @param endScale 结束缩放比例
     * @param duration 动画持续时间（毫秒）
     */
    public static void scale(JComponent component, float startScale, float endScale, int duration) {
        scale(component, startScale, endScale, duration, null);
    }

    /**
     * 缩放动画，带完成回调
     *
     * @param component 要应用动画的组件
     * @param startScale 起始缩放比例
     * @param endScale 结束缩放比例
     * @param duration 动画持续时间（毫秒）
     * @param onComplete 动画完成后的回调
     */
    public static void scale(JComponent component, float startScale, float endScale, int duration, Runnable onComplete) {
        // 保存原始大小
        Dimension originalSize = component.getSize();
        component.putClientProperty("originalSize", originalSize);

        animateProperty(
                duration,
                progress -> {
                    float currentScale = startScale + (endScale - startScale) * progress;
                    component.putClientProperty("scale", currentScale);

                    // 应用缩放效果
                    int newWidth = (int)(originalSize.width * currentScale);
                    int newHeight = (int)(originalSize.height * currentScale);
                    component.setPreferredSize(new Dimension(newWidth, newHeight));

                    component.revalidate();
                    component.repaint();
                },
                () -> {
                    // 动画结束时恢复原始大小
                    if (endScale == 1.0f) {
                        component.setPreferredSize(originalSize);
                    } else {
                        int newWidth = (int)(originalSize.width * endScale);
                        int newHeight = (int)(originalSize.height * endScale);
                        component.setPreferredSize(new Dimension(newWidth, newHeight));
                    }

                    component.revalidate();
                    component.repaint();

                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
        );
    }

    /**
     * 按钮点击动画
     *
     * @param button 要应用动画的按钮
     */
    public static void buttonClickEffect(AbstractButton button) {
        // 保存原始背景色
        Color originalBackground = button.getBackground();
        Color pressedColor = originalBackground.darker();

        // 先改变颜色，再恢复
        button.setBackground(pressedColor);

        // 创建定时器，延迟恢复原来的颜色
        Timer timer = new Timer(100, e -> {
            button.setBackground(originalBackground);
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * 属性动画
     *
     * @param duration 动画持续时间（毫秒）
     * @param updateFunc 更新函数，接收 0.0-1.0 之间的进度值
     * @param onComplete 动画完成后的回调
     */
    private static void animateProperty(int duration, Consumer<Float> updateFunc, Runnable onComplete) {
        final int fps = 60;
        final int totalFrames = duration * fps / 1000;
        final Timer timer = new Timer(1000 / fps, null);

        final float[] frame = {0};

        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame[0]++;

                if (frame[0] >= totalFrames) {
                    timer.stop();
                    updateFunc.accept(1.0f);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                    return;
                }

                float progress = frame[0] / (float) totalFrames;
                // 使用缓动函数使动画更自然
                float easedProgress = easeInOutQuad(progress);
                updateFunc.accept(easedProgress);
            }
        });

        timer.start();
    }

    /**
     * 组件悬停效果
     * 适用于任何Component类型的组件
     *
     * @param component 要应用效果的组件
     */
    public static void hoverEffect(Component component) {
        // 在列表项上应用简单的颜色变化效果
        if (component instanceof JPanel) {
            JPanel panel = (JPanel) component;
            Color originalBackground = panel.getBackground();

            // 创建一个稍亮一点的颜色
            Color brighterColor = new Color(
                    Math.min(originalBackground.getRed() + 10, 255),
                    Math.min(originalBackground.getGreen() + 10, 255),
                    Math.min(originalBackground.getBlue() + 10, 255),
                    originalBackground.getAlpha()
            );

            panel.setBackground(brighterColor);
            panel.repaint();

            // 创建定时器，延迟恢复原来的颜色
            Timer timer = new Timer(300, e -> {
                panel.setBackground(originalBackground);
                panel.repaint();
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    /**
     * 二次缓动函数
     *
     * @param t 进度 (0.0 到 1.0)
     * @return 缓动后的进度值
     */
    private static float easeInOutQuad(float t) {
        return t < 0.5f ? 2 * t * t : 1 - (float)Math.pow(-2 * t + 2, 2) / 2;
    }
}
