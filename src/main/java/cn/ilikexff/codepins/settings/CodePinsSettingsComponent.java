package cn.ilikexff.codepins.settings;


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


}
