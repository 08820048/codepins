package cn.ilikexff.codepins.settings;

import cn.ilikexff.codepins.ui.LicenseStatusPanel;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * CodePins 设置组件
 * 用于显示和管理 CodePins 的设置项
 */
public class CodePinsSettingsComponent {
    private final JPanel mainPanel;
    private final JBCheckBox autoShowPreviewCheckBox = new JBCheckBox("自动显示代码预览");
    private final JBCheckBox confirmDeleteCheckBox = new JBCheckBox("删除图钉时确认");
    private final JBTextField maxPinsTextField = new JBTextField();
    private final JBTextField previewHeightTextField = new JBTextField();

    public CodePinsSettingsComponent() {
        // 创建常规设置面板
        JPanel generalPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("最大图钉数量:"), maxPinsTextField, 1, false)
                .addLabeledComponent(new JBLabel("预览窗口高度:"), previewHeightTextField, 1, false)
                .addComponent(autoShowPreviewCheckBox)
                .addComponent(confirmDeleteCheckBox)
                .getPanel();
        generalPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(JBColor.border()),
                "常规设置",
                TitledBorder.LEFT,
                TitledBorder.TOP
        ));

        // 创建快捷键信息面板
        JPanel shortcutsInfoPanel = new JPanel(new BorderLayout());
        shortcutsInfoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(JBColor.border()),
                "快捷键设置",
                TitledBorder.LEFT,
                TitledBorder.TOP
        ));

        JTextArea shortcutsInfoText = new JTextArea(
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
        LicenseStatusPanel licenseStatusPanel = new LicenseStatusPanel();
        licenseStatusPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(JBColor.border()),
                "许可证状态",
                TitledBorder.LEFT,
                TitledBorder.TOP
        ));

        // 创建主面板
        mainPanel = FormBuilder.createFormBuilder()
                .addComponent(licenseStatusPanel)
                .addComponent(generalPanel)
                .addComponent(UI.PanelFactory.panel(shortcutsInfoPanel).withLabel("快捷键信息:").createPanel())
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

    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return autoShowPreviewCheckBox;
    }

    @NotNull
    public String getMaxPinsCount() {
        return maxPinsTextField.getText();
    }

    public void setMaxPinsCount(@NotNull String newText) {
        maxPinsTextField.setText(newText);
    }

    @NotNull
    public String getPreviewHeight() {
        return previewHeightTextField.getText();
    }

    public void setPreviewHeight(@NotNull String newText) {
        previewHeightTextField.setText(newText);
    }

    public boolean getAutoShowPreview() {
        return autoShowPreviewCheckBox.isSelected();
    }

    public void setAutoShowPreview(boolean newStatus) {
        autoShowPreviewCheckBox.setSelected(newStatus);
    }

    public boolean getConfirmDelete() {
        return confirmDeleteCheckBox.isSelected();
    }

    public void setConfirmDelete(boolean newStatus) {
        confirmDeleteCheckBox.setSelected(newStatus);
    }
}
