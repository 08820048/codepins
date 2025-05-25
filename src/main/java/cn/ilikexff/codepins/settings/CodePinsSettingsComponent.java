package cn.ilikexff.codepins.settings;


import cn.ilikexff.codepins.utils.IconUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * CodePins 设置组件
 * 用于显示和管理 CodePins 的设置项
 */
public class CodePinsSettingsComponent {
    private final JPanel mainPanel;
    private final JBCheckBox confirmDeleteCheckBox = new JBCheckBox("删除图钉时确认");
    private final JBTextField previewHeightTextField = new JBTextField();
    private final JBCheckBox showNoteDialogOnQuickAddCheckBox = new JBCheckBox("快捷添加图钉时显示备注和标签对话框");
    
    // 注释指令添加图钉设置控件
    private final JBCheckBox showNoteDialogOnCommentPinCheckBox = new JBCheckBox("注释指令添加图钉时显示备注和标签对话框");
    private final JBCheckBox autoAddQuickTagCheckBox = new JBCheckBox("自动添加“快捷添加”标签");
    private final JBCheckBox useCompletionSymbolCheckBox = new JBCheckBox("使用完成指令符号");
    private final JBTextField completionSymbolTextField = new JBTextField();

    public CodePinsSettingsComponent() {
        // 创建常规设置面板
        JPanel generalPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("预览窗口高度:"), previewHeightTextField, 1, false)
                .addComponent(confirmDeleteCheckBox)
                .getPanel();
        generalPanel.setBorder(BorderFactory.createTitledBorder("常规设置"));
        
        // 创建图钉添加设置面板
        JPanel pinAddPanel = FormBuilder.createFormBuilder()
                .addComponent(showNoteDialogOnQuickAddCheckBox)
                .addComponent(new JBLabel("<html><small>开启后，使用选择文本浮动按钮添加图钉时，将显示备注和标签对话框</small></html>"))
                .getPanel();
        pinAddPanel.setBorder(BorderFactory.createTitledBorder("图钉添加设置"));
        
        // 创建注释指令设置面板
        JPanel commentPinPanel = FormBuilder.createFormBuilder()
                .addComponent(showNoteDialogOnCommentPinCheckBox)
                .addComponent(new JBLabel("<html><small>开启后，使用注释指令添加图钉时，将显示备注和标签对话框</small></html>"))
                .addComponent(autoAddQuickTagCheckBox)
                .addComponent(new JBLabel("<html><small>开启后，使用注释指令添加图钉时，自动添加“快捷添加”标签</small></html>"))
                .addComponent(useCompletionSymbolCheckBox)
                .addLabeledComponent(new JBLabel("完成指令符号:"), completionSymbolTextField, 1, false)
                .addComponent(new JBLabel("<html><small>开启后，只有在注释指令后输入完成符号时才会触发图钉添加，避免自动保存导致过早触发</small></html>"))
                .getPanel();
        commentPinPanel.setBorder(BorderFactory.createTitledBorder("注释指令设置"));



        // 创建快捷键信息面板
        JPanel shortcutsInfoPanel = new JPanel(new BorderLayout());
        shortcutsInfoPanel.setBorder(BorderFactory.createTitledBorder("快捷键设置"));

        JBTextArea shortcutsInfoText = new JBTextArea(
                "CodePins 提供以下默认快捷键：\n\n" +
                "- 添加图钉: Alt+Shift+P\n" +
                "- 导航到下一个图钉: Alt+Shift+Right\n" +
                "- 导航到上一个图钉: Alt+Shift+Left\n" +
                "- 切换图钉工具窗口: Alt+Shift+T\n\n" +
                "您可以在 IDE 的'设置 > 键盘快捷键'中自定义这些快捷键。\n" +
                "搜索 \"CodePins\" 以找到所有相关操作。"
        );
        shortcutsInfoText.setEditable(false);
        shortcutsInfoText.setBackground(shortcutsInfoPanel.getBackground());
        shortcutsInfoText.setBorder(JBUI.Borders.empty(10));
        shortcutsInfoText.setLineWrap(true);
        shortcutsInfoText.setWrapStyleWord(true);

        JButton openKeyMapSettingsButton = new JButton("打开键盘快捷键设置");
        openKeyMapSettingsButton.addActionListener(e -> openKeyMapSettings());

        shortcutsInfoPanel.add(shortcutsInfoText, BorderLayout.CENTER);
        shortcutsInfoPanel.add(openKeyMapSettingsButton, BorderLayout.SOUTH);

        // 创建捐赠支持面板
        JPanel donationPanel = createDonationPanel();

        // 创建快捷键信息面板的标签面板
        JPanel labeledShortcutsPanel = new JPanel(new BorderLayout());
        JLabel shortcutsLabel = new JBLabel("快捷键信息:");
        labeledShortcutsPanel.add(shortcutsLabel, BorderLayout.NORTH);
        labeledShortcutsPanel.add(shortcutsInfoPanel, BorderLayout.CENTER);

        // 创建主面板
        mainPanel = FormBuilder.createFormBuilder()
                .addComponent(donationPanel)
                .addComponent(generalPanel)
                .addComponent(pinAddPanel)
                .addComponent(commentPinPanel)
                .addComponent(labeledShortcutsPanel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    /**
     * 打开键盘快捷键设置
     */
    private void openKeyMapSettings() {
        com.intellij.openapi.options.ShowSettingsUtil.getInstance().showSettingsDialog(
                null, "preferences.keymap"
        );
    }

    /**
     * 创建捐赠支持面板
     *
     * @return 捐赠面板
     */
    private JPanel createDonationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("支持开发"));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(JBUI.Borders.empty(10));

        // 状态标签
        JLabel statusLabel = new JBLabel("<html>" +
                "<div style='color: #4CAF50; font-weight: bold;'>✓ CodePins 现在完全免费开源！</div>" +
                "<br/>感谢您使用 CodePins！如果这个插件对您有帮助，" +
                "<br/>请考虑通过以下方式支持我们的开发：" +
                "<br/><br/>" +
                "<div style='color: #2196F3; font-weight: bold;'>🤝 欢迎加入开源贡献！</div>" +
                "<br/>我们诚挚邀请您一起维护这个开源项目：" +
                "<br/>• 🐛 报告 Bug 和提出改进建议" +
                "<br/>• 💡 贡献新功能和代码优化" +
                "<br/>• 📖 完善文档和使用指南" +
                "<br/>• 🌍 帮助翻译到更多语言" +
                "<br/>• 📢 向其他开发者推荐 CodePins" +
                "</html>");
        contentPanel.add(statusLabel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // GitHub 按钮
        JButton githubButton = new JButton("⭐ GitHub");
        githubButton.addActionListener(e -> {
            BrowserUtil.browse("https://github.com/08820048/codepins");
        });

        // 参与贡献按钮
        JButton contributeButton = new JButton("🤝 参与贡献");
        contributeButton.addActionListener(e -> {
            BrowserUtil.browse("https://github.com/08820048/codepins/blob/main/CONTRIBUTING.md");
        });

        // 捐赠按钮
        JButton donateButton = new JButton("☕ 请我喝咖啡");
        donateButton.addActionListener(e -> {
            BrowserUtil.browse("https://docs.codepins.cn/donate");
        });

        // 加载图标
        Icon heartIcon = IconUtil.loadIcon("/icons/logo.svg", getClass());
        if (heartIcon != null) {
            donateButton.setIcon(heartIcon);
        }

        buttonPanel.add(githubButton);
        buttonPanel.add(contributeButton);
        buttonPanel.add(donateButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return previewHeightTextField;
    }

    @NotNull
    public String getPreviewHeight() {
        return previewHeightTextField.getText();
    }

    public void setPreviewHeight(@NotNull String newText) {
        previewHeightTextField.setText(newText);
    }

    public boolean getConfirmDelete() {
        return confirmDeleteCheckBox.isSelected();
    }

    public void setConfirmDelete(boolean newStatus) {
        confirmDeleteCheckBox.setSelected(newStatus);
    }
    
    public boolean getShowNoteDialogOnQuickAdd() {
        return showNoteDialogOnQuickAddCheckBox.isSelected();
    }
    
    public void setShowNoteDialogOnQuickAdd(boolean newStatus) {
        showNoteDialogOnQuickAddCheckBox.setSelected(newStatus);
    }
    
    public boolean getShowNoteDialogOnCommentPin() {
        return showNoteDialogOnCommentPinCheckBox.isSelected();
    }
    
    public void setShowNoteDialogOnCommentPin(boolean newStatus) {
        showNoteDialogOnCommentPinCheckBox.setSelected(newStatus);
    }
    
    public boolean getAutoAddQuickTag() {
        return autoAddQuickTagCheckBox.isSelected();
    }
    
    public void setAutoAddQuickTag(boolean newStatus) {
        autoAddQuickTagCheckBox.setSelected(newStatus);
    }
    
    public boolean getUseCompletionSymbol() {
        return useCompletionSymbolCheckBox.isSelected();
    }
    
    public void setUseCompletionSymbol(boolean newStatus) {
        useCompletionSymbolCheckBox.setSelected(newStatus);
    }
    
    @NotNull
    public String getCompletionSymbol() {
        return completionSymbolTextField.getText();
    }
    
    public void setCompletionSymbol(@NotNull String newText) {
        completionSymbolTextField.setText(newText);
    }
}
