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
            
            // 创建标签样式
            Border tagBorder = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new JBColor(new Color(100, 100, 100), new Color(100, 100, 100)), 1),
                    JBUI.Borders.empty(3, 8)
            );
            
            label.setBorder(tagBorder);
            label.setOpaque(true);
            
            if (!isSelected) {
                label.setBackground(new JBColor(new Color(240, 240, 240), new Color(60, 63, 65)));
                label.setForeground(new JBColor(new Color(60, 60, 60), new Color(200, 200, 200)));
            }
            
            return label;
        }
    }
}
