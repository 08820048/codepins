package cn.ilikexff.codepins.ui;

import cn.ilikexff.codepins.services.LicenseService;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

/**
 * 许可证状态面板
 * 用于显示许可证状态和升级按钮
 */
public class LicenseStatusPanel extends JPanel {

    private final JLabel statusLabel;
    private final JButton upgradeButton;
    private final LicenseService licenseService;

    /**
     * 构造函数
     */
    public LicenseStatusPanel() {
        super(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));

        // 获取许可证服务
        licenseService = LicenseService.getInstance();

        // 创建标题面板
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(JBUI.Borders.emptyBottom(10));

        // 加载图标
        Icon icon = IconLoader.getIcon("/icons/logo.svg", getClass());
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(JBUI.Borders.emptyRight(10));

        // 创建标题标签
        JLabel titleLabel = new JBLabel("CodePins许可证状态");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16));

        // 添加到标题面板
        titlePanel.add(iconLabel, BorderLayout.WEST);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        // 创建状态面板
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(JBUI.Borders.empty(10));

        // 创建状态标签
        statusLabel = new JBLabel();
        updateStatusLabel();

        // 添加到状态面板
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // 创建刷新按钮
        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> {
            licenseService.checkLicense();
            updateStatusLabel();
        });

        // 创建升级按钮
        upgradeButton = new JButton("升级到专业版");
        upgradeButton.setBackground(new Color(0, 122, 204));
        upgradeButton.setForeground(Color.WHITE);
        upgradeButton.addActionListener(e -> {
            BrowserUtil.browse("https://plugins.jetbrains.com/plugin/27300-codepins--code-bookmarks/pricing");
        });

        // 根据许可证状态显示或隐藏升级按钮
        updateUpgradeButton();

        // 添加到按钮面板
        buttonPanel.add(refreshButton);
        buttonPanel.add(upgradeButton);

        // 组装面板
        add(titlePanel, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 更新状态标签
     */
    private void updateStatusLabel() {
        LicenseService.LicenseStatus status = licenseService.getLicenseStatus();
        String statusText = licenseService.getLicenseStatusDescription();

        // 设置状态标签
        switch (status) {
            case VALID:
                statusLabel.setText("<html><font color='green'>" + statusText + "</font></html>");
                break;
            case EXPIRED:
                statusLabel.setText("<html><font color='orange'>" + statusText + "</font></html>");
                break;
            case INVALID:
                statusLabel.setText("<html><font color='gray'>" + statusText + "</font></html>");
                break;
            case NOT_CHECKED:
                statusLabel.setText("<html><font color='gray'>" + statusText + "</font></html>");
                break;
        }

        // 更新升级按钮
        updateUpgradeButton();
    }

    /**
     * 更新升级按钮
     */
    private void updateUpgradeButton() {
        if (upgradeButton != null) {
            try {
                upgradeButton.setVisible(!licenseService.isPremiumUser());
            } catch (Exception e) {
                // 如果许可证检查失败，默认显示升级按钮
                upgradeButton.setVisible(true);
            }
        }
    }
}
