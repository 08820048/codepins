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

/**
 * 自定义搜索文本框
 * 提供现代化的搜索体验，包括搜索图标和清除按钮
 */
public class SearchTextField extends JPanel {
    
    private final JTextField textField;
    private final JLabel searchIcon;
    private final JLabel clearIcon;
    private final String placeholder;
    
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
        
        // 设置文本框样式
        textField.setBorder(JBUI.Borders.empty(6, 5, 6, 25));
        textField.setOpaque(true);
        
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
        
        // 创建搜索图标
        searchIcon = new JLabel(IconLoader.getIcon("/icons/search.svg", getClass()));
        searchIcon.setBorder(JBUI.Borders.empty(0, 8, 0, 0));
        
        // 创建清除图标
        clearIcon = new JLabel(IconLoader.getIcon("/icons/x-circle.svg", getClass()));
        clearIcon.setBorder(JBUI.Borders.empty(0, 0, 0, 8));
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
        
        // 设置面板样式
        setBorder(createBorder(false));
        setOpaque(true);
        setBackground(textField.getBackground());
        
        // 添加组件
        add(searchIcon, BorderLayout.WEST);
        add(textField, BorderLayout.CENTER);
        add(clearIcon, BorderLayout.EAST);
        
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
     * 创建边框
     */
    private Border createBorder(boolean focused) {
        Color borderColor = focused 
                ? new JBColor(new Color(100, 150, 200), new Color(100, 150, 200))
                : new JBColor(new Color(200, 200, 200, 100), new Color(80, 80, 80, 100));
        
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
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
}
