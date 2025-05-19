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
 * 标签编辑对话框
 * 用于添加、删除和编辑图钉标签
 */
public class TagEditorDialog extends DialogWrapper {

    private final PinEntry pinEntry;
    private final DefaultListModel<String> tagsModel;
    private final JBList<String> tagsList;
    private final JBTextField newTagField;
    private final JPanel suggestionsPanel;
    private final DefaultListModel<String> suggestionsModel;
    private final JBList<String> suggestionsList;
    private final List<String> currentTags;

    public TagEditorDialog(Project project, PinEntry pinEntry) {
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

        // 创建建议面板
        suggestionsPanel = new JPanel(new BorderLayout());
        suggestionsModel = new DefaultListModel<>();
        suggestionsList = new JBList<>(suggestionsModel);
        suggestionsList.setCellRenderer(new TagCellRenderer());

        // 设置对话框标题和尺寸
        setTitle("编辑标签");
        setSize(400, 300);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(400, 300));

        // 添加使用说明面板
        JPanel instructionPanel = new JPanel();
        instructionPanel.setLayout(new BoxLayout(instructionPanel, BoxLayout.Y_AXIS));
        instructionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()),
                JBUI.Borders.empty(5, 5, 10, 5)
        ));

        JLabel instructionTitle = new JLabel("标签使用说明");
        instructionTitle.setFont(instructionTitle.getFont().deriveFont(Font.BOLD));
        instructionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel instructionText = new JLabel("• 在下方输入框中输入标签名称，然后点击“添加”按钮");
        instructionText.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel instructionText2 = new JLabel("• 双击标签列表中的标签可删除该标签");
        instructionText2.setAlignmentX(Component.LEFT_ALIGNMENT);

        instructionPanel.add(instructionTitle);
        instructionPanel.add(Box.createVerticalStrut(5));
        instructionPanel.add(instructionText);
        instructionPanel.add(Box.createVerticalStrut(3));
        instructionPanel.add(instructionText2);

        // 当前标签面板
        JPanel currentTagsPanel = new JPanel(new BorderLayout());
        currentTagsPanel.setBorder(JBUI.Borders.empty(10, 5, 5, 5));

        JLabel currentTagsLabel = new JLabel("当前标签:");
        currentTagsLabel.setFont(currentTagsLabel.getFont().deriveFont(Font.BOLD));
        currentTagsPanel.add(currentTagsLabel, BorderLayout.NORTH);

        JBScrollPane tagsScrollPane = new JBScrollPane(tagsList);
        tagsScrollPane.setPreferredSize(new Dimension(380, 120));
        currentTagsPanel.add(tagsScrollPane, BorderLayout.CENTER);

        // 添加删除按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton removeButton = new JButton("删除选中标签");
        removeButton.setIcon(IconLoader.getIcon("/icons/trash.svg", getClass()));
        removeButton.addActionListener(e -> removeSelectedTags());
        buttonPanel.add(removeButton);
        currentTagsPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 新标签面板
        JPanel newTagPanel = new JPanel(new BorderLayout());
        newTagPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border()),
                JBUI.Borders.empty(10, 5, 5, 5)
        ));

        JLabel newTagLabel = new JLabel("添加新标签:");
        newTagLabel.setFont(newTagLabel.getFont().deriveFont(Font.BOLD));
        newTagPanel.add(newTagLabel, BorderLayout.NORTH);

        // 输入框和添加按钮的面板
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(JBUI.Borders.empty(5, 0, 5, 0));

        // 设置输入框的提示文本
        newTagField.putClientProperty("JTextField.placeholderText", "输入标签名称，回车或点击添加按钮");
        inputPanel.add(newTagField, BorderLayout.CENTER);

        JButton addButton = new JButton("添加");
        addButton.setIcon(IconLoader.getIcon("/icons/plus.svg", getClass()));
        addButton.addActionListener(e -> addNewTag());
        inputPanel.add(addButton, BorderLayout.EAST);

        newTagPanel.add(inputPanel, BorderLayout.CENTER);

        // 建议面板
        suggestionsPanel.setBorder(JBUI.Borders.empty(5));
        suggestionsPanel.add(new JLabel("建议标签:"), BorderLayout.NORTH);

        JBScrollPane suggestionsScrollPane = new JBScrollPane(suggestionsList);
        suggestionsScrollPane.setPreferredSize(new Dimension(380, 100));
        suggestionsPanel.add(suggestionsScrollPane, BorderLayout.CENTER);

        // 初始隐藏建议面板
        suggestionsPanel.setVisible(false);

        // 组装主面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(instructionPanel, BorderLayout.NORTH);
        topPanel.add(currentTagsPanel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(newTagPanel, BorderLayout.CENTER);
        mainPanel.add(suggestionsPanel, BorderLayout.SOUTH);

        // 设置事件监听器
        setupEventListeners();

        return mainPanel;
    }

    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 输入框键盘监听器
        newTagField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    addNewTag();
                } else {
                    updateSuggestions();
                }
            }
        });

        // 建议列表鼠标监听器
        suggestionsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = suggestionsList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        String tag = suggestionsModel.getElementAt(index);
                        newTagField.setText(tag);
                        addNewTag();
                    }
                }
            }
        });

        // 标签列表鼠标监听器
        tagsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    removeSelectedTags();
                }
            }
        });
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
            updateSuggestions();
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
     * 更新标签建议
     */
    private void updateSuggestions() {
        String input = newTagField.getText().trim().toLowerCase();
        suggestionsModel.clear();

        if (input.isEmpty()) {
            suggestionsPanel.setVisible(false);
            return;
        }

        // 获取所有标签
        Set<String> allTags = PinStorage.getAllTags();
        boolean hasSuggestions = false;

        // 添加匹配的标签
        for (String tag : allTags) {
            if (tag.toLowerCase().contains(input) && !currentTags.contains(tag)) {
                suggestionsModel.addElement(tag);
                hasSuggestions = true;
            }
        }

        suggestionsPanel.setVisible(hasSuggestions);
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
