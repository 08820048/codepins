package cn.ilikexff.codepins;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 自定义悬浮预览组件，用于替代标准工具提示
 * 提供更可靠的图钉信息预览
 */
public class PinHoverPreview {

    private static JBPopup currentPopup;
    private static final int INFO_PREVIEW_DELAY_MS = 300; // 基本信息预览延迟时间
    private static final int CODE_PREVIEW_DELAY_MS = 3000; // 代码预览延迟时间（3秒）
    private static Timer hoverTimer;
    private static Timer codePreviewTimer; // 代码预览计时器
    private static PinEntry currentPin; // 当前悬停的图钉
    private static Project currentProject; // 当前项目

    /**
     * 显示图钉预览弹窗
     * @param pin 图钉对象
     * @param project 项目
     * @param component 触发组件
     * @param x 显示位置X坐标
     * @param y 显示位置Y坐标
     */
    public static void showPreview(PinEntry pin, Project project, Component component, int x, int y) {
        // 保存当前图钉和项目，用于代码预览
        currentPin = pin;
        currentProject = project;

        // 如果已有弹窗，先关闭
        hidePreview();

        // 停止所有计时器
        if (hoverTimer != null) {
            hoverTimer.stop();
        }
        if (codePreviewTimer != null) {
            codePreviewTimer.stop();
        }

        // 使用 Timer 延迟显示基本信息预览，并确保在 EDT 线程上执行
        final PinEntry finalPin = pin; // 创建一个最终变量供 lambda 使用
        final Component finalComponent = component;
        final int finalX = x;
        final int finalY = y;

        hoverTimer = new Timer(INFO_PREVIEW_DELAY_MS, e -> {
            // 在 EDT 线程上执行
            SwingUtilities.invokeLater(() -> {
                try {
                    System.out.println("[CodePins] 尝试显示基本信息预览，图钉路径: " + finalPin.filePath);

                    // 创建预览面板（已在内部正确处理 ReadAction）
                    JPanel panel = createPreviewPanel(finalPin);
                    if (panel == null) {
                        System.out.println("[CodePins] 创建预览面板失败");
                        return;
                    }

                    // 创建弹窗
                    currentPopup = JBPopupFactory.getInstance()
                            .createComponentPopupBuilder(panel, null)
                            .setRequestFocus(false)
                            .setResizable(false)
                            .setMovable(false)
                            .setCancelOnClickOutside(true)
                            .setCancelOnWindowDeactivation(true)
                            .createPopup();

                    // 显示弹窗
                    currentPopup.showInScreenCoordinates(finalComponent, new Point(finalX, finalY));
                    System.out.println("[CodePins] 基本信息预览显示成功");

                    // 创建并启动代码预览计时器（3秒后显示代码预览）
                    startCodePreviewTimer(finalComponent, finalX, finalY);
                } catch (Exception ex) {
                    System.out.println("[CodePins] 显示基本信息预览异常: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        });

        hoverTimer.setRepeats(false);
        hoverTimer.start();
    }

    /**
     * 启动代码预览计时器
     */
    private static void startCodePreviewTimer(Component component, int x, int y) {
        if (codePreviewTimer != null) {
            codePreviewTimer.stop();
        }

        codePreviewTimer = new Timer(CODE_PREVIEW_DELAY_MS - INFO_PREVIEW_DELAY_MS, e -> {
            // 在 EDT 线程上执行
            SwingUtilities.invokeLater(() -> {
                try {
                    if (currentPin == null || currentProject == null) {
                        System.out.println("[CodePins] 无法显示代码预览：当前图钉或项目为空");
                        return;
                    }

                    System.out.println("[CodePins] 尝试显示代码预览，图钉路径: " + currentPin.filePath);

                    // 关闭当前预览
                    if (currentPopup != null && !currentPopup.isDisposed()) {
                        currentPopup.cancel();
                        currentPopup = null;
                    }

                    // 显示代码预览
                    CodePreviewUtil.showPreviewPopup(currentProject, currentPin);
                    System.out.println("[CodePins] 代码预览显示成功");
                } catch (Exception ex) {
                    System.out.println("[CodePins] 显示代码预览异常: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        });

        codePreviewTimer.setRepeats(false);
        codePreviewTimer.start();
    }

    /**
     * 隐藏当前预览弹窗
     */
    public static void hidePreview() {
        if (currentPopup != null && !currentPopup.isDisposed()) {
            currentPopup.cancel();
            currentPopup = null;
            System.out.println("[CodePins] 隐藏自定义悬浮预览");
        }

        if (hoverTimer != null) {
            hoverTimer.stop();
        }

        if (codePreviewTimer != null) {
            codePreviewTimer.stop();
        }

        // 清除当前图钉和项目引用
        currentPin = null;
        currentProject = null;
    }

    /**
     * 创建预览面板
     */
    private static JPanel createPreviewPanel(PinEntry pin) {
        try {
            if (pin == null) {
                return createErrorPanel("图钉对象为空");
            }

            // 将所有文档访问操作包装在 ReadAction 中
            return com.intellij.openapi.application.ReadAction.compute(() -> {
                try {
                    // 首先检查 marker 是否有效
                    if (pin.marker == null || !pin.marker.isValid()) {
                        System.out.println("[CodePins] 创建预览面板失败: marker 无效");
                        return createErrorPanel("图钉标记无效");
                    }

                    // 获取文档
                    Document doc = pin.marker.getDocument();
                    if (doc == null) {
                        System.out.println("[CodePins] 创建预览面板失败: 无法获取文档");
                        return createErrorPanel("无法获取文档");
                    }

                    // 获取基本信息
                    int line = pin.getCurrentLine(doc);
                    String note = pin.note != null && !pin.note.isEmpty() ? pin.note : "-";
                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(pin.timestamp));
                    String author = pin.author != null ? pin.author : "-";

                    System.out.println("[CodePins] 成功获取图钉信息，行号: " + (line + 1));

                    // 创建面板 - 使用更现代的设计
                    JPanel panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBorder(JBUI.Borders.empty(15)); // 增加内边距

                    // 使用渐变背景色，增强视觉效果
                    Color darkBg = new JBColor(new Color(40, 44, 52, 245), new Color(40, 44, 52, 245));
                    panel.setBackground(darkBg);

                    // 添加标题栏 - 显示图钉类型
                    JPanel titlePanel = new JPanel();
                    titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
                    titlePanel.setOpaque(false);
                    titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    titlePanel.setBorder(JBUI.Borders.emptyBottom(10));

                    // 创建标题标签
                    JLabel titleLabel = new JLabel(pin.isBlock ? "📌 代码块图钉" : "📌 单行图钉");
                    titleLabel.setForeground(new JBColor(new Color(255, 255, 255), new Color(255, 255, 255)));
                    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14.0f));
                    titlePanel.add(titleLabel);
                    titlePanel.add(Box.createHorizontalGlue());
                    panel.add(titlePanel);

                    // 添加分隔线
                    JSeparator separator = new JSeparator();
                    separator.setForeground(new JBColor(new Color(70, 75, 85), new Color(70, 75, 85)));
                    separator.setAlignmentX(Component.LEFT_ALIGNMENT);
                    panel.add(separator);
                    panel.add(Box.createVerticalStrut(10)); // 分隔线后的空间

                    // 添加文件路径
                    addInfoRow(panel, "路径", pin.filePath, new JBColor(new Color(255, 203, 107), new Color(255, 203, 107)));

                    // 添加行号
                    addInfoRow(panel, "行号", String.valueOf(line + 1), new JBColor(new Color(247, 140, 108), new Color(247, 140, 108)));

                    // 添加备注
                    addInfoRow(panel, "备注", note, new JBColor(new Color(64, 191, 255), new Color(64, 191, 255)));

                    // 添加创建时间
                    addInfoRow(panel, "创建于", time, new JBColor(new Color(130, 170, 255), new Color(130, 170, 255)));

                    // 添加作者
                    addInfoRow(panel, "作者", author, new JBColor(new Color(199, 146, 234), new Color(199, 146, 234)));

                    return panel;
                } catch (Exception e) {
                    System.out.println("[CodePins] 创建预览面板异常: " + e.getMessage());
                    e.printStackTrace();
                    return createErrorPanel("创建预览面板异常: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.out.println("[CodePins] 创建预览面板外部异常: " + e.getMessage());
            e.printStackTrace();
            return createErrorPanel("创建预览面板异常: " + e.getMessage());
        }
    }

    /**
     * 添加信息行
     */
    private static void addInfoRow(JPanel panel, String label, String value, Color labelColor) {
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.setOpaque(false);
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowPanel.setBorder(JBUI.Borders.emptyBottom(8)); // 增加行间距

        // 标签组件
        JLabel labelComponent = new JLabel(label + ": ");
        labelComponent.setForeground(labelColor);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD, 13.0f)); // 增大字体

        // 值组件 - 使用更高对比度的颜色
        JLabel valueComponent = new JLabel(value);
        valueComponent.setForeground(new JBColor(new Color(255, 255, 255), new Color(255, 255, 255))); // 纯白色
        valueComponent.setFont(valueComponent.getFont().deriveFont(13.0f)); // 增大字体

        // 如果值是路径，添加特殊样式
        if (label.equals("路径")) {
            // 路径使用特殊颜色和字体
            valueComponent.setForeground(new JBColor(new Color(255, 230, 180), new Color(255, 230, 180)));
            valueComponent.setFont(valueComponent.getFont().deriveFont(Font.PLAIN, 12.0f));
        } else if (label.equals("行号")) {
            // 行号使用稍大一点的字体和高亮颜色
            valueComponent.setForeground(new JBColor(new Color(255, 200, 180), new Color(255, 200, 180)));
            valueComponent.setFont(valueComponent.getFont().deriveFont(Font.BOLD, 14.0f));
        } else if (label.equals("备注") && !value.equals("-")) {
            // 备注使用特殊颜色
            valueComponent.setForeground(new JBColor(new Color(180, 255, 200), new Color(180, 255, 200)));
        }

        rowPanel.add(labelComponent);
        rowPanel.add(valueComponent);
        rowPanel.add(Box.createHorizontalGlue());

        panel.add(rowPanel);
    }

    /**
     * 创建错误面板
     */
    private static JPanel createErrorPanel(String errorMessage) {
        // 创建面板使用垂直布局
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(JBUI.Borders.empty(15));
        panel.setBackground(new JBColor(new Color(50, 40, 40, 245), new Color(50, 40, 40, 245))); // 深红色背景

        // 添加错误图标
        JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.X_AXIS));
        iconPanel.setOpaque(false);
        iconPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLabel = new JLabel("⚠️"); // 警告图标
        iconLabel.setFont(iconLabel.getFont().deriveFont(18.0f));
        iconPanel.add(iconLabel);
        iconPanel.add(Box.createHorizontalStrut(10));

        JLabel titleLabel = new JLabel("图钉信息加载失败");
        titleLabel.setForeground(new JBColor(new Color(255, 180, 180), new Color(255, 180, 180)));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14.0f));
        iconPanel.add(titleLabel);
        iconPanel.add(Box.createHorizontalGlue());

        panel.add(iconPanel);
        panel.add(Box.createVerticalStrut(10));

        // 添加分隔线
        JSeparator separator = new JSeparator();
        separator.setForeground(new JBColor(new Color(100, 70, 70), new Color(100, 70, 70)));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(separator);
        panel.add(Box.createVerticalStrut(10));

        // 添加错误信息
        JLabel errorLabel = new JLabel(errorMessage);
        errorLabel.setForeground(new JBColor(new Color(255, 255, 255), new Color(255, 255, 255)));
        errorLabel.setFont(errorLabel.getFont().deriveFont(13.0f));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(errorLabel);

        return panel;
    }
}
