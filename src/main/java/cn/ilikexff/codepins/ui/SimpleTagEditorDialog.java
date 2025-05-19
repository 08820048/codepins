package cn.ilikexff.codepins.ui;

import cn.ilikexff.codepins.PinEntry;
import cn.ilikexff.codepins.PinStorage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 简化版标签编辑对话框
 */
public class SimpleTagEditorDialog extends DialogWrapper {

    private final PinEntry pinEntry;
    private final DefaultListModel<String> tagsModel;
    private final JBList<String> tagsList;
    private final JBTextField newTagField;
    private final List<String> currentTags;

    public SimpleTagEditorDialog(Project project, PinEntry pinEntry) {
        super(project);
        this.pinEntry = pinEntry;
        this.currentTags = new ArrayList<>(pinEntry.getTags());

        // 创建标签列表模型和列表
        tagsModel = new DefaultListModel<>();
        for (String tag : currentTags) {
            tagsModel.addElement(tag);
        }

        tagsList = new JBList<>(tagsModel);
        tagsList.setCellRenderer(new TagCellRenderer());

        // 创建新标签输入框
        newTagField = new JBTextField();

        // 设置对话框标题和尺寸
        setTitle("编辑标签");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(JBUI.Borders.empty(10));

        // 添加说明标签
        JLabel instructionLabel = new JLabel("<html><b>标签使用说明：</b><br>" +
                "1. 在下方输入框中输入标签名称，然后按回车或点击添加按钮<br>" +
                "2. 选中列表中的标签，然后点击删除按钮可删除标签</html>");
        instructionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        instructionLabel.setBorder(JBUI.Borders.emptyBottom(10));
        mainPanel.add(instructionLabel);

        // 添加当前标签标签
        JLabel currentTagsLabel = new JLabel("当前标签：");
        currentTagsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(currentTagsLabel);

        // 添加标签列表
        JBScrollPane scrollPane = new JBScrollPane(tagsList);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setPreferredSize(new Dimension(400, 150));
        scrollPane.setMinimumSize(new Dimension(400, 150));
        scrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 150));
        mainPanel.add(scrollPane);

        // 添加删除按钮
        JButton removeButton = new JButton("删除选中标签");
        removeButton.setIcon(IconLoader.getIcon("/icons/trash.svg", getClass()));
        removeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        removeButton.addActionListener(e -> removeSelectedTags());

        JPanel removeButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        removeButtonPanel.add(removeButton);
        removeButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(removeButtonPanel);

        // 添加分隔符
        JSeparator separator = new JSeparator();
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        separator.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(separator);
        mainPanel.add(Box.createVerticalStrut(10));

        // 添加新标签标签
        JLabel newTagLabel = new JLabel("添加新标签：");
        newTagLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(newTagLabel);

        // 添加输入框和按钮面板
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 设置输入框提示
        newTagField.putClientProperty("JTextField.placeholderText", "输入标签名称，按回车添加");
        newTagField.setPreferredSize(new Dimension(300, 30));
        newTagField.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        newTagField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    addNewTag();
                }
            }
        });

        JButton addButton = new JButton("添加");
        addButton.setIcon(IconLoader.getIcon("/icons/plus.svg", getClass()));
        addButton.addActionListener(e -> addNewTag());

        inputPanel.add(newTagField);
        inputPanel.add(Box.createHorizontalStrut(5));
        inputPanel.add(addButton);

        mainPanel.add(inputPanel);

        return mainPanel;
    }

    /**
     * 添加新标签
     */
    private void addNewTag() {
        String tag = newTagField.getText().trim();
        if (!tag.isEmpty() && !tagsModel.contains(tag)) {
            tagsModel.addElement(tag);
            currentTags.add(tag);
            newTagField.setText("");
        }
    }

    /**
     * 删除选中的标签
     */
    private void removeSelectedTags() {
        int[] indices = tagsList.getSelectedIndices();
        for (int i = indices.length - 1; i >= 0; i--) {
            String tag = tagsModel.getElementAt(indices[i]);
            tagsModel.remove(indices[i]);
            currentTags.remove(tag);
        }
    }

    /**
     * 获取当前标签列表
     */
    public List<String> getTags() {
        return currentTags;
    }

    /**
     * 标签单元格渲染器
     */
    private static class TagCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            // 获取标签文本
            String tag = value.toString();

            // 生成标签颜色
            Color tagColor = getTagColor(tag);
            Color textColor;
            Color borderColor;

            if (isSelected) {
                // 选中状态使用更深的颜色
                tagColor = tagColor.darker();
                textColor = new JBColor(Color.WHITE, Color.WHITE);
                borderColor = new JBColor(new Color(100, 100, 100), new Color(100, 100, 100));
            } else {
                // 非选中状态使用适合背景色的文本颜色
                boolean isDark = ColorUtil.isDark(tagColor);
                textColor = isDark ? new JBColor(Color.WHITE, Color.WHITE) : new JBColor(new Color(50, 50, 50), new Color(50, 50, 50));
                borderColor = new JBColor(new Color(tagColor.getRed(), tagColor.getGreen(), tagColor.getBlue(), 100),
                                         new Color(tagColor.getRed(), tagColor.getGreen(), tagColor.getBlue(), 100));
            }

            // 设置样式
            label.setBackground(tagColor);
            label.setForeground(textColor);

            // 添加标签图标
            label.setIcon(IconLoader.getIcon("/icons/tag-small.svg", TagCellRenderer.class));
            label.setIconTextGap(8);

            // 设置圆角边框
            Border tagBorder = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor, 1),
                    JBUI.Borders.empty(6, 10)
            );

            label.setBorder(tagBorder);
            label.setOpaque(true);

            // 设置字体
            label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));

            return label;
        }

        /**
         * 根据标签名称生成颜色
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
}
