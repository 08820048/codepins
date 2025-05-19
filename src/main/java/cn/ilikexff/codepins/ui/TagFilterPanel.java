package cn.ilikexff.codepins.ui;

import cn.ilikexff.codepins.PinEntry;
import cn.ilikexff.codepins.PinStorage;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 标签筛选面板
 * 用于显示和选择标签进行筛选
 */
public class TagFilterPanel extends JPanel {
    
    private final List<String> selectedTags = new ArrayList<>();
    private final Consumer<List<String>> onTagSelectionChanged;
    private final JPanel tagsContainer;
    
    public TagFilterPanel(Consumer<List<String>> onTagSelectionChanged) {
        this.onTagSelectionChanged = onTagSelectionChanged;
        
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(5, 10));
        
        // 创建标题
        JLabel titleLabel = new JLabel("按标签筛选:");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        add(titleLabel, BorderLayout.NORTH);
        
        // 创建标签容器
        tagsContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        tagsContainer.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(tagsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // 添加清除按钮
        JButton clearButton = new JButton("清除筛选");
        clearButton.addActionListener(e -> {
            selectedTags.clear();
            refreshTagsView();
            onTagSelectionChanged.accept(selectedTags);
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(clearButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 初始化标签视图
        refreshTagsView();
    }
    
    /**
     * 刷新标签视图
     */
    public void refreshTagsView() {
        tagsContainer.removeAll();
        
        Set<String> allTags = PinStorage.getAllTags();
        if (allTags.isEmpty()) {
            JLabel emptyLabel = new JLabel("暂无标签");
            emptyLabel.setForeground(JBColor.GRAY);
            tagsContainer.add(emptyLabel);
        } else {
            for (String tag : allTags) {
                JLabel tagLabel = createTagLabel(tag, selectedTags.contains(tag));
                tagsContainer.add(tagLabel);
            }
        }
        
        tagsContainer.revalidate();
        tagsContainer.repaint();
    }
    
    /**
     * 创建标签标签
     */
    private JLabel createTagLabel(String tag, boolean selected) {
        JLabel tagLabel = new JLabel(tag);
        tagLabel.setFont(tagLabel.getFont().deriveFont(Font.PLAIN, 12f));
        
        // 设置颜色和样式
        Color bgColor = selected ? getSelectedTagColor(tag) : getTagColor(tag);
        Color fgColor = selected ? 
                new JBColor(new Color(255, 255, 255), new Color(255, 255, 255)) : 
                new JBColor(new Color(50, 50, 50), new Color(200, 200, 200));
        
        tagLabel.setForeground(fgColor);
        tagLabel.setBackground(bgColor);
        tagLabel.setOpaque(true);
        
        // 设置边框和内边距
        tagLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new JBColor(new Color(100, 100, 100, 50), new Color(100, 100, 100, 50)), 1),
                JBUI.Borders.empty(3, 8)
        ));
        
        // 添加鼠标点击事件
        tagLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (selected) {
                    selectedTags.remove(tag);
                } else {
                    selectedTags.add(tag);
                }
                refreshTagsView();
                onTagSelectionChanged.accept(selectedTags);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                tagLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                tagLabel.setCursor(Cursor.getDefaultCursor());
            }
        });
        
        return tagLabel;
    }
    
    /**
     * 获取标签颜色
     */
    private Color getTagColor(String tag) {
        // 使用标签的哈希值生成颜色，确保相同标签有相同颜色
        int hash = tag.hashCode();
        
        // 预定义的淡色调色板
        Color[] lightPalette = {
                new Color(230, 240, 255), // 淡蓝
                new Color(230, 255, 230), // 淡绿
                new Color(255, 240, 230), // 淡橙
                new Color(255, 230, 240), // 淡红
                new Color(240, 230, 255), // 淡紫
                new Color(255, 255, 230), // 淡黄
                new Color(230, 255, 255)  // 淡青
        };
        
        Color[] darkPalette = {
                new Color(40, 50, 70),    // 深蓝
                new Color(40, 70, 50),    // 深绿
                new Color(70, 50, 40),    // 深橙
                new Color(70, 40, 50),    // 深红
                new Color(50, 40, 70),    // 深紫
                new Color(70, 70, 40),    // 深黄
                new Color(40, 70, 70)     // 深青
        };
        
        int index = Math.abs(hash) % lightPalette.length;
        return new JBColor(lightPalette[index], darkPalette[index]);
    }
    
    /**
     * 获取选中标签的颜色
     */
    private Color getSelectedTagColor(String tag) {
        // 使用标签的哈希值生成颜色，确保相同标签有相同颜色
        int hash = tag.hashCode();
        
        // 预定义的深色调色板（选中状态）
        Color[] lightPalette = {
                new Color(100, 140, 230), // 蓝
                new Color(100, 200, 130), // 绿
                new Color(230, 140, 100), // 橙
                new Color(230, 100, 140), // 红
                new Color(170, 100, 230), // 紫
                new Color(200, 200, 100), // 黄
                new Color(100, 200, 200)  // 青
        };
        
        Color[] darkPalette = {
                new Color(80, 120, 200),  // 蓝
                new Color(80, 180, 110),  // 绿
                new Color(200, 120, 80),  // 橙
                new Color(200, 80, 120),  // 红
                new Color(150, 80, 200),  // 紫
                new Color(180, 180, 80),  // 黄
                new Color(80, 180, 180)   // 青
        };
        
        int index = Math.abs(hash) % lightPalette.length;
        return new JBColor(lightPalette[index], darkPalette[index]);
    }
    
    /**
     * 获取当前选中的标签
     */
    public List<String> getSelectedTags() {
        return new ArrayList<>(selectedTags);
    }
}
