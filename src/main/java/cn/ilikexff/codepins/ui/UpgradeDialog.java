package cn.ilikexff.codepins.ui;

import cn.ilikexff.codepins.services.LicenseService;
import cn.ilikexff.codepins.utils.IconUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * 升级对话框
 * 用于提示用户升级到付费版
 */
public class UpgradeDialog extends DialogWrapper {

    private final Project project;
    private final String featureName;

    /**
     * 构造函数
     *
     * @param project     当前项目
     * @param featureName 功能名称
     */
    public UpgradeDialog(Project project, String featureName) {
        super(project);
        this.project = project;
        this.featureName = featureName;

        setTitle("升级到CodePins专业版");
        setSize(600, 400);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.setBorder(JBUI.Borders.empty(10));

        // 创建标题面板
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(JBUI.Borders.emptyBottom(10));

        // 加载图标
        Icon icon = IconUtil.loadIcon("/icons/logo.svg", getClass());
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(JBUI.Borders.emptyRight(10));

        // 创建标题标签
        JLabel titleLabel = new JBLabel("升级到CodePins专业版，解锁更多强大功能");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16));

        // 添加到标题面板
        titlePanel.add(iconLabel, BorderLayout.WEST);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        // 创建内容面板
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(JBUI.Borders.empty(10));

        // 创建功能列表
        JPanel featuresPanel = new JPanel();
        featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));

        // 添加功能项
        addFeatureItem(featuresPanel, "无限图钉", "免费版限制100个图钉，专业版无数量限制");
        addFeatureItem(featuresPanel, "无限标签", "免费版限制10种不同标签和每个图钉3个标签，专业版无限制");
        addFeatureItem(featuresPanel, "更多社交分享平台", "支持7个国际社交媒体平台，包括Reddit、Telegram、Hacker News等");
        addFeatureItem(featuresPanel, "自定义水印", "自定义或完全移除分享图片中的水印");
        addFeatureItem(featuresPanel, "高级分享选项（即将推出）", "密码保护、链接有效期限制等高级分享选项");
        addFeatureItem(featuresPanel, "图片分享高级功能", "更多图片格式、更高分辨率、自定义样式");
        addFeatureItem(featuresPanel, "优先支持", "获得优先的技术支持和问题解决");
        addFeatureItem(featuresPanel, "未来高级功能", "自动获得所有未来的高级功能");

        // 高亮当前功能
        if (featureName != null && !featureName.isEmpty()) {
            JPanel currentFeaturePanel = new JPanel(new BorderLayout());
            currentFeaturePanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("您正在尝试使用的功能"),
                    JBUI.Borders.empty(5)
            ));

            JLabel currentFeatureLabel = new JBLabel("<html><b>" + featureName + "</b> 是CodePins专业版的功能</html>");
            currentFeatureLabel.setForeground(new Color(0, 122, 204));
            currentFeaturePanel.add(currentFeatureLabel, BorderLayout.CENTER);

            featuresPanel.add(Box.createVerticalStrut(10));
            featuresPanel.add(currentFeaturePanel);
        }

        // 添加滚动面板
        JBScrollPane scrollPane = new JBScrollPane(featuresPanel);
        scrollPane.setBorder(JBUI.Borders.empty());
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // 创建底部面板
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(JBUI.Borders.emptyTop(10));

        // 创建价格信息
        JLabel priceLabel = new JBLabel("<html>CodePins专业版 - 每年仅需 $19.99<br>一次性购买 $49.99</html>");
        priceLabel.setFont(priceLabel.getFont().deriveFont(Font.BOLD));

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // 创建升级按钮 - 使用默认按钮样式
        JButton upgradeButton = new JButton("立即升级");
        upgradeButton.addActionListener(e -> {
            BrowserUtil.browse("https://plugins.jetbrains.com/plugin/27300-codepins--code-bookmarks/pricing");
            close(OK_EXIT_CODE);
        });

        // 添加到按钮面板
        buttonPanel.add(upgradeButton);

        // 添加到底部面板
        bottomPanel.add(priceLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // 组装面板
        dialogPanel.add(titlePanel, BorderLayout.NORTH);
        dialogPanel.add(contentPanel, BorderLayout.CENTER);
        dialogPanel.add(bottomPanel, BorderLayout.SOUTH);

        return dialogPanel;
    }

    /**
     * 添加功能项
     *
     * @param panel       面板
     * @param title       标题
     * @param description 描述
     */
    private void addFeatureItem(JPanel panel, String title, String description) {
        JPanel featurePanel = new JPanel(new BorderLayout());
        featurePanel.setBorder(JBUI.Borders.emptyBottom(5));

        // 创建标题标签
        JLabel titleLabel = new JBLabel("<html><b>" + title + "</b></html>");

        // 创建描述标签
        JLabel descLabel = new JBLabel("<html><font color='gray'>" + description + "</font></html>");

        // 添加到功能面板
        featurePanel.add(titleLabel, BorderLayout.NORTH);
        featurePanel.add(descLabel, BorderLayout.CENTER);

        // 添加到主面板
        panel.add(featurePanel);
        panel.add(Box.createVerticalStrut(5));
    }

    /**
     * 显示升级对话框
     *
     * @param project     当前项目
     * @param featureName 功能名称
     * @return 是否点击了升级按钮
     */
    public static boolean showDialog(Project project, String featureName) {
        // 检查是否已经是付费用户
        if (LicenseService.getInstance().isPremiumUser()) {
            return true; // 已经是付费用户，不需要显示对话框
        }

        // 显示升级对话框
        UpgradeDialog dialog = new UpgradeDialog(project, featureName);
        return dialog.showAndGet();
    }
}
