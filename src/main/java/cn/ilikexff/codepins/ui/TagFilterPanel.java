package cn.ilikexff.codepins.ui;

import cn.ilikexff.codepins.core.PinEntry;
import cn.ilikexff.codepins.core.PinStorage;
import cn.ilikexff.codepins.i18n.CodePinsBundle;
import cn.ilikexff.codepins.services.LicenseService;
import cn.ilikexff.codepins.utils.IconUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()),
                JBUI.Borders.empty(8, 10, 10, 10)
        ));

        // 创建标题和清除按钮的顶部面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(JBUI.Borders.emptyBottom(8));

        // 标题带图标
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        titlePanel.setOpaque(false);

        JLabel iconLabel = new JLabel(IconUtil.loadIcon("/icons/filter.svg", getClass()));
        JLabel titleLabel = new JLabel(CodePinsBundle.message("tag.filter.panel"));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));

        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);

        // 清除按钮
        JButton clearButton = new JButton(CodePinsBundle.message("tag.clear.filter"));
        clearButton.setFont(clearButton.getFont().deriveFont(11f));
        clearButton.setBorder(JBUI.Borders.empty(2, 8));
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> {
            // 添加按钮动画效果
            AnimationUtil.buttonClickEffect(clearButton);
            selectedTags.clear();
            refreshTagsView();
            onTagSelectionChanged.accept(selectedTags);
        });

        // 添加新建标签按钮
        JButton addTagButton = new JButton(CodePinsBundle.message("tag.create"));
        addTagButton.setFont(addTagButton.getFont().deriveFont(11f));
        addTagButton.setBorder(JBUI.Borders.empty(2, 8));
        addTagButton.setFocusPainted(false);
        addTagButton.setIcon(IconUtil.loadIcon("/icons/plus.svg", getClass()));
        addTagButton.addActionListener(e -> {
            // 添加按钮动画效果
            AnimationUtil.buttonClickEffect(addTagButton);
            openAddTagDialog();
        });

        // 创建按钮面板来容纳清除和新建标签按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(addTagButton);
        buttonPanel.add(clearButton);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 创建标签容器
        tagsContainer = new JPanel();
        tagsContainer.setLayout(new WrapLayout(FlowLayout.LEFT, 6, 6));
        tagsContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(tagsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);

        // 初始化标签视图
        refreshTagsView();
    }

    /**
     * 刷新标签视图
     */
    public void refreshTagsView() {
        tagsContainer.removeAll();

        // 获取标签限制信息
        Map<String, Integer> tagsInfo = PinStorage.getTagsCountInfo();
        int currentTagTypes = tagsInfo.get("current");
        int maxTagTypes = tagsInfo.get("max");
        boolean isPremiumUser = LicenseService.getInstance().isPremiumUser();

        // 添加标签计数信息
        if (!isPremiumUser && maxTagTypes != -1) {
            JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            countPanel.setOpaque(false);

            // 创建标签计数标签
            JLabel countLabel = new JLabel("标签: " + currentTagTypes + "/" + maxTagTypes);
            countLabel.setFont(countLabel.getFont().deriveFont(Font.PLAIN, 11f));

            // 根据使用比例设置颜色
            float usageRatio = (float) currentTagTypes / maxTagTypes;
            if (usageRatio >= 0.9) {
                countLabel.setForeground(new Color(217, 83, 79)); // 红色
            } else if (usageRatio >= 0.7) {
                countLabel.setForeground(new Color(240, 173, 78)); // 黄色
            } else {
                countLabel.setForeground(JBColor.GRAY);
            }

            countPanel.add(countLabel);

            // 插件现在完全免费，移除升级链接

            tagsContainer.add(countPanel);
        }

        Set<String> allTags = PinStorage.getAllTags();
        if (allTags.isEmpty()) {
            JLabel emptyLabel = new JLabel(CodePinsBundle.message("tag.empty"));
            emptyLabel.setForeground(JBColor.GRAY);
            tagsContainer.add(emptyLabel);
        } else {
            for (String tag : allTags) {
                JComponent tagLabel = createTagLabel(tag, selectedTags.contains(tag));
                tagsContainer.add(tagLabel);
            }
        }

        tagsContainer.revalidate();
        tagsContainer.repaint();
    }

    /**
     * 创建标签标签
     */
    private JComponent createTagLabel(String tag, boolean selected) {
        // 使用JPanel包装标签和删除按钮
        JPanel tagPanel = new JPanel(new BorderLayout(5, 0));
        tagPanel.setOpaque(true);
        
        JLabel tagLabel = new JLabel(tag);
        tagLabel.setFont(tagLabel.getFont().deriveFont(selected ? Font.BOLD : Font.PLAIN, 12f));

        // 生成标签颜色
        Color tagColor = selected ? getSelectedTagColor(tag) : getTagColor(tag);

        // 根据背景色决定文本颜色
        boolean isDark = ColorUtil.isDark(tagColor);
        Color textColor = isDark ? new JBColor(Color.WHITE, Color.WHITE) : new JBColor(new Color(50, 50, 50), new Color(50, 50, 50));

        tagLabel.setForeground(textColor);
        tagPanel.setBackground(tagColor);

        // 添加标签图标
        tagLabel.setIcon(IconUtil.loadIcon("/icons/tag-small.svg", getClass()));
        tagLabel.setIconTextGap(6);
        
        // 创建删除按钮
        JLabel deleteButton = new JLabel("×");
        deleteButton.setForeground(textColor);
        deleteButton.setToolTipText("删除标签");
        deleteButton.setFont(deleteButton.getFont().deriveFont(Font.BOLD));
        
        // 添加删除按钮的鼠标事件
        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 弹出确认对话框
                int result = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(TagFilterPanel.this),
                    "确定要删除标签 \"" + tag + "\" 吗？", "删除标签", JOptionPane.YES_NO_OPTION);
                
                if (result == JOptionPane.YES_OPTION) {
                    // 如果标签已被选中，先移除选中状态
                    selectedTags.remove(tag);
                    
                    // 从所有标签集合中删除
                    // 注意：需要修改PinStorage类添加删除全局标签的方法
                    PinStorage.removeGlobalTag(tag);
                    
                    // 从所有图钉中移除该标签
                    for (PinEntry pin : PinStorage.getPins()) {
                        if (pin.hasTag(tag)) {
                            List<String> tags = new ArrayList<>(pin.getTags());
                            tags.remove(tag);
                            PinStorage.updateTags(pin, tags);
                        }
                    }
                    
                    // 刷新标签视图
                    refreshTagsView();
                    onTagSelectionChanged.accept(selectedTags);
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                deleteButton.setForeground(isDark ? new Color(255, 200, 200) : new Color(200, 0, 0));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                deleteButton.setCursor(Cursor.getDefaultCursor());
                deleteButton.setForeground(textColor);
            }
        });
        
        // 组装标签面板
        tagPanel.add(tagLabel, BorderLayout.CENTER);
        tagPanel.add(deleteButton, BorderLayout.EAST);

        // 设置圆角边框
        Color borderColor = new JBColor(
                new Color(tagColor.getRed(), tagColor.getGreen(), tagColor.getBlue(), selected ? 150 : 100),
                new Color(tagColor.getRed(), tagColor.getGreen(), tagColor.getBlue(), selected ? 150 : 100)
        );

        tagPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                JBUI.Borders.empty(4, 8)
        ));

        // 为整个标签面板添加鼠标点击事件
        tagPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) { // 左键点击
                    // 添加标签点击动画效果
                    AnimationUtil.scale(tagPanel, 1.0f, 0.95f, 50, () -> {
                        AnimationUtil.scale(tagPanel, 0.95f, 1.0f, 100, () -> {
                            if (selected) {
                                selectedTags.remove(tag);
                            } else {
                                selectedTags.add(tag);
                            }
                            refreshTagsView();
                            onTagSelectionChanged.accept(selectedTags);
                        });
                    });
                } else if (e.getButton() == MouseEvent.BUTTON3) { // 右键点击
                    // 显示右键菜单
                    JPopupMenu popupMenu = createTagContextMenu(tag);
                    popupMenu.show(tagPanel, e.getX(), e.getY());
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                tagPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                // 鼠标悬停效果
                if (!selected) {
                    tagPanel.setBackground(tagColor.brighter());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tagPanel.setCursor(Cursor.getDefaultCursor());
                // 鼠标离开恢复原样式
                if (!selected) {
                    tagPanel.setBackground(tagColor);
                }
            }
        });

        return tagPanel;
    }
    
    /**
     * 创建标签右键菜单
     */
    private JPopupMenu createTagContextMenu(String tag) {
        JPopupMenu menu = new JPopupMenu();
        
        // 编辑标签菜单项
        JMenuItem editItem = new JMenuItem("编辑标签");
        editItem.setIcon(IconUtil.loadIcon("/icons/edit.svg", getClass()));
        editItem.addActionListener(e -> {
            // 创建一个对话框让用户输入新的标签名称
            String newTag = JOptionPane.showInputDialog(
                SwingUtilities.getWindowAncestor(TagFilterPanel.this),
                "编辑标签名称:", 
                tag);
            
            if (newTag != null && !newTag.trim().isEmpty() && !newTag.equals(tag)) {
                // 清除旧标签
                selectedTags.remove(tag);
                
                // 更新所有包含此标签的图钉
                for (PinEntry pin : PinStorage.getPins()) {
                    if (pin.hasTag(tag)) {
                        List<String> tags = new ArrayList<>(pin.getTags());
                        tags.remove(tag);
                        tags.add(newTag.trim());
                        PinStorage.updateTags(pin, tags);
                    }
                }
                
                // 从全局标签中移除旧标签并添加新标签
                PinStorage.removeGlobalTag(tag);
                PinStorage.addGlobalTag(newTag.trim());
                
                // 如果编辑的是已选中的标签，更新选中状态
                if (selectedTags.contains(tag)) {
                    selectedTags.remove(tag);
                    selectedTags.add(newTag.trim());
                }
                
                // 刷新标签视图
                refreshTagsView();
                onTagSelectionChanged.accept(selectedTags);
            }
        });
        menu.add(editItem);
        
        // 删除标签菜单项
        JMenuItem deleteItem = new JMenuItem("删除标签");
        deleteItem.setIcon(IconUtil.loadIcon("/icons/trash.svg", getClass()));
        deleteItem.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(TagFilterPanel.this),
                "确定要删除标签 \"" + tag + "\" 吗？", "删除标签", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                // 如果标签已被选中，先移除选中状态
                selectedTags.remove(tag);
                
                // 从所有图钉中移除该标签
                for (PinEntry pin : PinStorage.getPins()) {
                    if (pin.hasTag(tag)) {
                        List<String> tags = new ArrayList<>(pin.getTags());
                        tags.remove(tag);
                        PinStorage.updateTags(pin, tags);
                    }
                }
                
                // 从全局标签集合中删除
                PinStorage.removeGlobalTag(tag);
                
                // 刷新标签视图
                refreshTagsView();
                onTagSelectionChanged.accept(selectedTags);
            }
        });
        menu.add(deleteItem);
        
        return menu;
    }

    /**
     * 获取标签颜色
     */
    private Color getTagColor(String tag) {
        // 使用标签的哈希值生成颜色，确保相同标签有相同颜色
        int hash = tag.hashCode();

        // 现代感强的色调
        Color[] lightPalette = {
                new Color(79, 195, 247),  // 浅蓝
                new Color(129, 199, 132), // 浅绿
                new Color(255, 183, 77),  // 浅橙
                new Color(240, 98, 146),  // 浅红
                new Color(149, 117, 205), // 浅紫
                new Color(224, 224, 224), // 浅灰
                new Color(77, 208, 225),  // 浅青
                new Color(174, 213, 129)  // 浅黄绿
        };

        Color[] darkPalette = {
                new Color(41, 121, 255),  // 深蓝
                new Color(67, 160, 71),   // 深绿
                new Color(255, 152, 0),   // 深橙
                new Color(233, 30, 99),   // 深红
                new Color(103, 58, 183),  // 深紫
                new Color(117, 117, 117), // 深灰
                new Color(0, 172, 193),   // 深青
                new Color(104, 159, 56)   // 深黄绿
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

        // 选中状态使用更高饱和度的颜色
        Color[] lightPalette = {
                new Color(3, 169, 244),   // 蓝
                new Color(76, 175, 80),   // 绿
                new Color(255, 152, 0),   // 橙
                new Color(233, 30, 99),   // 红
                new Color(103, 58, 183),  // 紫
                new Color(158, 158, 158), // 灰
                new Color(0, 188, 212),   // 青
                new Color(139, 195, 74)   // 黄绿
        };

        Color[] darkPalette = {
                new Color(33, 150, 243),  // 蓝
                new Color(46, 125, 50),   // 绿
                new Color(239, 108, 0),   // 橙
                new Color(216, 27, 96),   // 红
                new Color(94, 53, 177),   // 紫
                new Color(97, 97, 97),    // 灰
                new Color(0, 151, 167),   // 青
                new Color(85, 139, 47)    // 黄绿
        };

        int index = Math.abs(hash) % lightPalette.length;
        return new JBColor(lightPalette[index], darkPalette[index]);
    }

    /**
     * 颜色工具类
     */
    private static class ColorUtil {
        /**
         * 判断颜色是否为深色
         */
        public static boolean isDark(Color color) {
            // 使用人眼对不同颜色的敏感度公式
            double brightness = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
            return brightness < 0.5;
        }
    }

    /**
     * 获取当前选中的标签
     */
    public List<String> getSelectedTags() {
        return new ArrayList<>(selectedTags);
    }

    /**
     * 打开添加标签对话框
     */
    private void openAddTagDialog() {
        // 创建一个临时的空PinEntry对象，仅用于打开对话框
        PinEntry dummyPin = new PinEntry();
        
        SimpleTagEditorDialog dialog = new SimpleTagEditorDialog(
            SwingUtilities.getWindowAncestor(this) instanceof com.intellij.openapi.project.Project ? 
            (com.intellij.openapi.project.Project) SwingUtilities.getWindowAncestor(this) : 
            com.intellij.openapi.project.ProjectManager.getInstance().getOpenProjects()[0],
            dummyPin
        );
        
        dialog.setTitle(CodePinsBundle.message("tag.create.custom"));
        
        if (dialog.showAndGet()) {
            List<String> newTags = dialog.getTags();
            // 将新创建的标签添加到所有标签集合中
            for (String tag : newTags) {
                if (!PinStorage.getAllTags().contains(tag)) {
                    // 添加到全局标签集合中
                    PinStorage.addGlobalTag(tag);
                }
            }
            
            // 清空当前选中的标签，以便显示全部图钉
            selectedTags.clear();
            
            // 刷新标签视图
            refreshTagsView();
            
            // 通知监听器标签选择已更改（显示所有图钉）
            onTagSelectionChanged.accept(selectedTags);
        }
    }
}
