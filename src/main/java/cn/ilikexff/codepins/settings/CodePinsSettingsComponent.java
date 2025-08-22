package cn.ilikexff.codepins.settings;


import cn.ilikexff.codepins.i18n.CodePinsBundle;
import cn.ilikexff.codepins.template.ui.TemplateManagementPanel;
import cn.ilikexff.codepins.ai.ui.SuggestionConfigPanel;
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
    private final JBCheckBox confirmDeleteCheckBox = new JBCheckBox(CodePinsBundle.message("settings.general.confirm.delete"));
    private final JBTextField previewHeightTextField = new JBTextField();
    private final JBCheckBox showNoteDialogOnQuickAddCheckBox = new JBCheckBox(CodePinsBundle.message("settings.pin.add.show.note.dialog"));
    
    // 注释指令添加图钉设置控件
    private final JBCheckBox showNoteDialogOnCommentPinCheckBox = new JBCheckBox(CodePinsBundle.message("settings.comment.show.note.dialog"));
    private final JBCheckBox autoAddQuickTagCheckBox = new JBCheckBox(CodePinsBundle.message("settings.comment.auto.add.tag"));
    private final JBCheckBox useCompletionSymbolCheckBox = new JBCheckBox(CodePinsBundle.message("settings.comment.use.completion.symbol"));
    private final JBTextField completionSymbolTextField = new JBTextField();

    public CodePinsSettingsComponent() {
        // 创建常规设置面板
        JPanel generalPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel(CodePinsBundle.message("settings.general.preview.height")), previewHeightTextField, 1, false)
                .addComponent(confirmDeleteCheckBox)
                .getPanel();
        generalPanel.setBorder(BorderFactory.createTitledBorder(CodePinsBundle.message("settings.general")));
        
        // 创建图钉添加设置面板
        JPanel pinAddPanel = FormBuilder.createFormBuilder()
                .addComponent(showNoteDialogOnQuickAddCheckBox)
                .addComponent(new JBLabel("<html><small>" + CodePinsBundle.message("settings.pin.add.show.note.dialog.desc") + "</small></html>"))
                .getPanel();
        pinAddPanel.setBorder(BorderFactory.createTitledBorder(CodePinsBundle.message("settings.pin.add")));
        
        // 创建注释指令设置面板
        JPanel commentPinPanel = FormBuilder.createFormBuilder()
                .addComponent(showNoteDialogOnCommentPinCheckBox)
                .addComponent(new JBLabel("<html><small>" + CodePinsBundle.message("settings.comment.show.note.dialog.desc") + "</small></html>"))
                .addComponent(autoAddQuickTagCheckBox)
                .addComponent(new JBLabel("<html><small>" + CodePinsBundle.message("settings.comment.auto.add.tag.desc") + "</small></html>"))
                .addComponent(useCompletionSymbolCheckBox)
                .addLabeledComponent(new JBLabel(CodePinsBundle.message("settings.comment.completion.symbol")), completionSymbolTextField, 1, false)
                .addComponent(new JBLabel("<html><small>" + CodePinsBundle.message("settings.comment.completion.symbol.desc") + "</small></html>"))
                .getPanel();
        commentPinPanel.setBorder(BorderFactory.createTitledBorder(CodePinsBundle.message("settings.comment")));



        // 创建快捷键信息面板
        JPanel shortcutsInfoPanel = new JPanel(new BorderLayout());
        shortcutsInfoPanel.setBorder(BorderFactory.createTitledBorder(CodePinsBundle.message("settings.shortcuts")));

        JBTextArea shortcutsInfoText = new JBTextArea(
                CodePinsBundle.message("settings.shortcuts.info")
        );
        shortcutsInfoText.setEditable(false);
        shortcutsInfoText.setBackground(shortcutsInfoPanel.getBackground());
        shortcutsInfoText.setBorder(JBUI.Borders.empty(10));
        shortcutsInfoText.setLineWrap(true);
        shortcutsInfoText.setWrapStyleWord(true);

        JButton openKeyMapSettingsButton = new JButton(CodePinsBundle.message("settings.shortcuts.open"));
        openKeyMapSettingsButton.addActionListener(e -> openKeyMapSettings());

        shortcutsInfoPanel.add(shortcutsInfoText, BorderLayout.CENTER);
        shortcutsInfoPanel.add(openKeyMapSettingsButton, BorderLayout.SOUTH);

        // 创建捐赠支持面板
        JPanel donationPanel = createDonationPanel();

        // 创建快捷键信息面板的标签面板
        JPanel labeledShortcutsPanel = new JPanel(new BorderLayout());
        JLabel shortcutsLabel = new JBLabel(CodePinsBundle.message("settings.shortcuts") + ":");
        labeledShortcutsPanel.add(shortcutsLabel, BorderLayout.NORTH);
        labeledShortcutsPanel.add(shortcutsInfoPanel, BorderLayout.CENTER);

        // 创建模板管理面板
        TemplateManagementPanel templatePanel = new TemplateManagementPanel();
        templatePanel.setBorder(BorderFactory.createTitledBorder(CodePinsBundle.message("settings.templates")));

        // 创建智能建议配置面板
        SuggestionConfigPanel suggestionConfigPanel = new SuggestionConfigPanel();
        suggestionConfigPanel.setBorder(BorderFactory.createTitledBorder("智能建议配置"));

        // 创建主面板
        mainPanel = FormBuilder.createFormBuilder()
                .addComponent(donationPanel)
                .addComponent(generalPanel)
                .addComponent(pinAddPanel)
                .addComponent(commentPinPanel)
                .addComponent(labeledShortcutsPanel)
                .addComponent(templatePanel)
                .addComponent(suggestionConfigPanel)
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
        panel.setBorder(BorderFactory.createTitledBorder(CodePinsBundle.message("settings.support")));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(JBUI.Borders.empty(10));

        // 状态标签
        JLabel statusLabel = new JBLabel("<html>" +
                "<div style='color: #4CAF50; font-weight: bold;'>" + CodePinsBundle.message("settings.support.free") + "</div>" +
                "<br/>" + CodePinsBundle.message("settings.support.thanks") +
                "<br/><br/>" +
                "<div style='color: #2196F3; font-weight: bold;'>" + CodePinsBundle.message("settings.support.contribute") + "</div>" +
                "<br/>" + CodePinsBundle.message("settings.support.contribute.desc").replace("\n", "<br/>") +
                "</html>");
        contentPanel.add(statusLabel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // GitHub 按钮
        JButton githubButton = new JButton(CodePinsBundle.message("settings.support.github"));
        githubButton.addActionListener(e -> {
            BrowserUtil.browse("https://github.com/08820048/codepins");
        });

        // 参与贡献按钮
        JButton contributeButton = new JButton(CodePinsBundle.message("settings.support.contribute.button"));
        contributeButton.addActionListener(e -> {
            BrowserUtil.browse("https://github.com/08820048/codepins/blob/main/CONTRIBUTING.md");
        });

        // 问题报告按钮
        JButton issueButton = new JButton(CodePinsBundle.message("settings.support.issue"));
        issueButton.addActionListener(e -> {
            BrowserUtil.browse("https://github.com/08820048/codepins/issues");
        });
        
        // 捐赠按钮
        JButton donateButton = new JButton(CodePinsBundle.message("settings.support.donate"));
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
        buttonPanel.add(issueButton);
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
