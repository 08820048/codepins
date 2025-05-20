package cn.ilikexff.codepins.ui;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * 自定义搜索文本框
 * 提供现代化的搜索体验，包括搜索图标和清除按钮
 */
public class SearchTextField extends JPanel {

    private final JTextField textField;
    private final JLabel searchIcon;
    private final JLabel clearIcon;
    private final String placeholder;
    private boolean isHovered = false; // 添加悬停状态标记

    public SearchTextField(String placeholder) {
        super(new BorderLayout());
        this.placeholder = placeholder;

        // 创建文本框
        textField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // 如果文本框为空且未获得焦点，绘制占位符文本
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new JBColor(new Color(120, 120, 120), new Color(120, 120, 120)));
                    g2.setFont(getFont().deriveFont(Font.ITALIC));

                    Insets insets = getInsets();
                    g2.drawString(placeholder, insets.left + 5, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                    g2.dispose();
                }
            }
        };

        // 设置文本框样式 - 降低高度，增加精致感
        textField.setBorder(JBUI.Borders.empty(4, 5, 4, 25));
        textField.setOpaque(true);
        textField.setFont(textField.getFont().deriveFont(12f)); // 调整字体大小，增加精致感

        // 添加焦点监听器，用于重绘占位符
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                textField.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                textField.repaint();
            }
        });

        // 创建搜索图标 - 调整边距，增加精致感
        searchIcon = new JLabel(IconLoader.getIcon("/icons/search.svg", getClass()));
        searchIcon.setBorder(JBUI.Borders.empty(0, 10, 0, 0)); // 增加左边距，使图标与文本保持适当距离

        // 创建清除图标 - 调整边距，增加精致感
        clearIcon = new JLabel(IconLoader.getIcon("/icons/x-circle.svg", getClass()));
        clearIcon.setBorder(JBUI.Borders.empty(0, 0, 0, 10)); // 增加右边距，使图标与文本保持适当距离
        clearIcon.setVisible(false);

        // 添加清除按钮点击事件
        clearIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                textField.setText("");
                textField.requestFocus();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
        });

        // 监听文本变化，控制清除按钮的显示
        textField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateClearButton();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateClearButton();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateClearButton();
            }

            private void updateClearButton() {
                clearIcon.setVisible(!textField.getText().isEmpty());
            }
        });

        // 设置面板样式 - 增加高级感
        setBorder(createBorder(false));
        setOpaque(false); // 设置为透明，增加现代感
        setBackground(new Color(0, 0, 0, 0)); // 透明背景
        textField.setBackground(new Color(255, 255, 255, 240)); // 轻微半透明的背景，增加现代感

        // 添加组件
        add(searchIcon, BorderLayout.WEST);
        add(textField, BorderLayout.CENTER);
        add(clearIcon, BorderLayout.EAST);

        // 添加鼠标悬停效果
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                setBorder(createBorder(textField.hasFocus()));
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                setBorder(createBorder(textField.hasFocus()));
                repaint();
            }
        });

        // 添加焦点监听器，用于改变边框样式
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                setBorder(createBorder(true));
            }

            @Override
            public void focusLost(FocusEvent e) {
                setBorder(createBorder(false));
            }
        });
    }

    /**
     * 创建边框 - 圆角边框，增加高级感
     */
    private Border createBorder(boolean focused) {
        // 调整边框颜色，增加高级感
        Color borderColor;

        if (focused) {
            // 聚焦状态 - 亮蓝色
            borderColor = new JBColor(new Color(100, 150, 200), new Color(100, 150, 200));
        } else if (isHovered) {
            // 悬停状态 - 浅灰色，稍深
            borderColor = new JBColor(new Color(180, 180, 180, 200), new Color(100, 100, 100, 200));
        } else {
            // 普通状态 - 浅灰色
            borderColor = new JBColor(new Color(200, 200, 200, 150), new Color(80, 80, 80, 150));
        }

        // 创建圆角边框，半径为8像素
        return BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(1, 1, 1, 1), // 外边距
                        BorderFactory.createLineBorder(borderColor, 1, true) // 圆角边框，第三个参数true表示圆角
                ),
                JBUI.Borders.empty(0)
        );
    }

    /**
     * 添加文档监听器
     */
    public void addDocumentListener(DocumentListener listener) {
        textField.getDocument().addDocumentListener(listener);
    }

    /**
     * 获取文本
     */
    public String getText() {
        return textField.getText();
    }

    /**
     * 设置文本
     */
    public void setText(String text) {
        textField.setText(text);
    }

    /**
     * 请求焦点
     */
    @Override
    public void requestFocus() {
        textField.requestFocus();
    }

    /**
     * 重写组件绘制方法，添加阴影效果
     */
    @Override
    protected void paintComponent(Graphics g) {
        // 创建圆角矩形路径
        int width = getWidth();
        int height = getHeight();
        int cornerRadius = 8; // 圆角半径

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制背景
        if (textField.hasFocus() || isHovered) {
            // 聚焦或悬停状态时绘制阴影
            g2.setColor(new Color(0, 0, 0, 10)); // 非常淡的阴影颜色
            for (int i = 0; i < 3; i++) { // 多层阴影，增加深度感
                g2.fill(new RoundRectangle2D.Float(i + 1, i + 1, width - (i * 2) - 2, height - (i * 2) - 2, cornerRadius, cornerRadius));
            }
        }

        // 绘制背景
        g2.setColor(new Color(255, 255, 255, 240)); // 半透明的白色背景
        g2.fill(new RoundRectangle2D.Float(1, 1, width - 2, height - 2, cornerRadius - 1, cornerRadius - 1));

        g2.dispose();
        super.paintComponent(g);
    }
}
