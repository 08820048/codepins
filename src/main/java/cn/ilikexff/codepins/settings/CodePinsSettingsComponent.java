package cn.ilikexff.codepins.settings;

import cn.ilikexff.codepins.ui.LicenseStatusPanel;
import cn.ilikexff.codepins.utils.IconUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UI;
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

    public CodePinsSettingsComponent() {
        // 创建常规设置面板
        JPanel generalPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("预览窗口高度:"), previewHeightTextField, 1, false)
                .addComponent(confirmDeleteCheckBox)
                .getPanel();
        generalPanel.setBorder(BorderFactory.createTitledBorder("常规设置"));



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

        // 创建许可证状态面板
        JPanel licensePanel;
        try {
            LicenseStatusPanel licenseStatusPanel = new LicenseStatusPanel();
            licenseStatusPanel.setBorder(BorderFactory.createTitledBorder("许可证状态"));
            licensePanel = licenseStatusPanel;
        } catch (Exception e) {
            // 如果许可证面板创建失败，使用简单面板代替
            licensePanel = createSimpleLicensePanel();
        }

        // 创建快捷键信息面板的标签面板
        JPanel labeledShortcutsPanel = new JPanel(new BorderLayout());
        JLabel shortcutsLabel = new JBLabel("快捷键信息:");
        labeledShortcutsPanel.add(shortcutsLabel, BorderLayout.NORTH);
        labeledShortcutsPanel.add(shortcutsInfoPanel, BorderLayout.CENTER);

        // 创建主面板
        mainPanel = FormBuilder.createFormBuilder()
                .addComponent(licensePanel)
                .addComponent(generalPanel)
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
     * 创建简单的许可证面板
     * 当LicenseStatusPanel创建失败时使用
     *
     * @return 简单的许可证面板
     */
    private JPanel createSimpleLicensePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("许可证状态"));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(JBUI.Borders.empty(10));

        JLabel statusLabel = new JBLabel("<html><font color='gray'>您正在使用CodePins免费版</font></html>");
        contentPanel.add(statusLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton upgradeButton = new JButton("立即升级");

        // 加载图标
        Icon upgradeIcon = IconUtil.loadIcon("/icons/logo.svg", getClass());
        if (upgradeIcon != null) {
            upgradeButton.setIcon(upgradeIcon);
        }

        upgradeButton.addActionListener(e -> {
            BrowserUtil.browse("https://plugins.jetbrains.com/plugin/27300-codepins--code-bookmarks/pricing");
        });
        buttonPanel.add(upgradeButton);

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


}
