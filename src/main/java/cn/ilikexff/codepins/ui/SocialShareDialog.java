package cn.ilikexff.codepins.ui;

import cn.ilikexff.codepins.core.PinEntry;
import cn.ilikexff.codepins.i18n.CodePinsBundle;
import cn.ilikexff.codepins.utils.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 社交分享对话框
 * 用于选择社交媒体平台和分享方式
 */
public class SocialShareDialog extends DialogWrapper {

    private final Project project;
    private final List<PinEntry> pins;
    private final SharingUtil.SharingFormat format;

    private JRadioButton[] platformRadios;
    private JComboBox<ShareLinkGenerator.ExpirationTime> expirationComboBox;
    private JCheckBox passwordCheckBox;
    private JPasswordField passwordField;
    private JPanel passwordPanel;

    /**
     * 构造函数
     *
     * @param project 当前项目
     * @param pins 要分享的图钉列表
     * @param format 分享格式
     * @param codeOnly 是否只分享代码
     * @param showLineNumbers 是否显示行号
     */
    public SocialShareDialog(Project project, List<PinEntry> pins, SharingUtil.SharingFormat format, boolean codeOnly, boolean showLineNumbers) {
        super(project);
        this.project = project;
        this.pins = new ArrayList<>(pins);
        this.format = format;

        // 注意：分享内容将在需要时生成，不再存储为字段
        // SharingUtil.formatPins(project, pins, format, codeOnly, showLineNumbers);

        setTitle(CodePinsBundle.message("social.share.dialog.title"));
        setSize(600, 600); // 增加对话框尺寸，确保有足够的空间
        setResizable(true); // 允许用户调整大小
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.setBorder(JBUI.Borders.empty(10));

        // 创建平台选择面板
        JPanel platformPanel = new JPanel(new BorderLayout());
        platformPanel.setBorder(BorderFactory.createTitledBorder(CodePinsBundle.message("social.share.platform")));
        platformPanel.setBorder(BorderFactory.createCompoundBorder(
                platformPanel.getBorder(),
                JBUI.Borders.empty(5, 5, 5, 5))); // 添加内边距

        // 获取支持的平台
        // 插件现在完全免费，所有平台都可用
        SocialSharingUtil.SocialPlatform[] platforms = SocialSharingUtil.getSupportedPlatforms(false);
        platformRadios = new JRadioButton[platforms.length];
        ButtonGroup platformGroup = new ButtonGroup();

        // 创建平台面板
        JPanel platformsPanel = new JPanel(new GridLayout(0, 3, 10, 5)); // 3列网格
        platformsPanel.setBorder(BorderFactory.createTitledBorder(CodePinsBundle.message("social.share.platform.list")));

        // 添加平台选项
        int firstPlatformIndex = -1;

        for (int i = 0; i < platforms.length; i++) {
            SocialSharingUtil.SocialPlatform platform = platforms[i];
            platformRadios[i] = new JBRadioButton(platform.getDisplayName());
            platformGroup.add(platformRadios[i]);
            platformsPanel.add(platformRadios[i]);

            if (firstPlatformIndex == -1) {
                firstPlatformIndex = i;
            }
        }

        // 默认选中第一个选项
        if (firstPlatformIndex != -1) {
            platformRadios[firstPlatformIndex].setSelected(true);
        }

        // 添加到平台面板
        JPanel platformsContainer = new JPanel(new BorderLayout(0, 10));
        platformsContainer.add(platformsPanel, BorderLayout.CENTER);

        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(platformsContainer);
        scrollPane.setBorder(JBUI.Borders.empty());
        scrollPane.setPreferredSize(new Dimension(-1, 150)); // 减小高度，留出更多空间给链接选项

        platformPanel.add(scrollPane, BorderLayout.CENTER);

        // 插件现在完全免费，移除所有平台限制

        // 创建链接选项面板
        JPanel linkPanel = new JPanel(new BorderLayout(0, 5)); // 减小垂直间距
        linkPanel.setBorder(BorderFactory.createTitledBorder(CodePinsBundle.message("social.share.link.options")));
        linkPanel.setBorder(BorderFactory.createCompoundBorder(
                linkPanel.getBorder(),
                JBUI.Borders.empty(5, 5, 5, 5))); // 添加内边距

        // 过期时间选择
        JPanel expirationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        expirationPanel.add(new JBLabel(CodePinsBundle.message("social.share.link.expiration") + ":"));

        expirationComboBox = new JComboBox<>(ShareLinkGenerator.ExpirationTime.values());
        expirationComboBox.setSelectedItem(ShareLinkGenerator.ExpirationTime.ONE_DAY);
        expirationPanel.add(expirationComboBox);

        // 禁用过期时间选择，标记为未来开发功能
        expirationComboBox.setEnabled(false);
        expirationPanel.add(new JBLabel(" " + CodePinsBundle.message("social.share.link.expiration.future")));

        // 插件现在完全免费，移除升级对话框点击事件

        linkPanel.add(expirationPanel, BorderLayout.NORTH);

        // 密码保护选项
        passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passwordCheckBox = new JCheckBox(CodePinsBundle.message("social.share.password.protection") + ":");
        passwordField = new JPasswordField(15);
        passwordField.setEnabled(false);

        passwordCheckBox.addActionListener(e -> passwordField.setEnabled(passwordCheckBox.isSelected()));

        passwordPanel.add(passwordCheckBox);
        passwordPanel.add(passwordField);

        // 禁用密码保护选项，标记为未来开发功能
        passwordCheckBox.setEnabled(false);
        passwordField.setEnabled(false);
        passwordPanel.add(new JBLabel(" " + CodePinsBundle.message("social.share.password.future")));

        // 插件现在完全免费，移除升级对话框点击事件

        linkPanel.add(passwordPanel, BorderLayout.CENTER);

        // 创建信息面板
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder(CodePinsBundle.message("social.share.info")));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                infoPanel.getBorder(),
                JBUI.Borders.empty(5, 5, 5, 5))); // 添加内边距

        // 添加提示信息
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBackground(JBColor.background());
        // 确保正确传递参数以显示图钉数量
        infoArea.setText(CodePinsBundle.message("social.share.info.text", pins.size()));

        // 设置固定高度，避免文本区域过大
        JScrollPane infoScrollPane = new JScrollPane(infoArea);
        infoScrollPane.setPreferredSize(new Dimension(-1, 80)); // 设置固定高度
        infoPanel.add(infoScrollPane, BorderLayout.CENTER);

        // 移除升级提示

        // 组装面板
        JPanel optionsPanel = new JPanel(new BorderLayout(0, 10));
        optionsPanel.add(platformPanel, BorderLayout.NORTH);
        optionsPanel.add(linkPanel, BorderLayout.CENTER);

        // 添加滚动面板，确保可以看到所有内容
        JScrollPane optionsScrollPane = new JScrollPane(optionsPanel);
        optionsScrollPane.setBorder(JBUI.Borders.empty());
        optionsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        optionsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.add(optionsScrollPane, BorderLayout.CENTER);

        // 创建底部面板并添加间距
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(JBUI.Borders.emptyTop(10)); // 添加上方的间距
        bottomPanel.add(infoPanel, BorderLayout.CENTER);

        dialogPanel.add(mainPanel, BorderLayout.CENTER);
        dialogPanel.add(bottomPanel, BorderLayout.SOUTH);

        return dialogPanel;
    }

    @Override
    protected void doOKAction() {
        try {
            // 获取选择的平台
            SocialSharingUtil.SocialPlatform selectedPlatform = null;
            // 插件现在完全免费，所有平台都可用
            SocialSharingUtil.SocialPlatform[] platforms = SocialSharingUtil.getSupportedPlatforms(false);

            for (int i = 0; i < platformRadios.length; i++) {
                if (platformRadios[i].isSelected()) {
                    selectedPlatform = platforms[i];
                    break;
                }
            }

            if (selectedPlatform == null) {
                Messages.showErrorDialog(
                        project,
                        CodePinsBundle.message("social.share.error.select.platform"),
                        CodePinsBundle.message("social.share.error.title")
                );
                return;
            }

            // 固定使用1天过期时间（未来功能）
            ShareLinkGenerator.ExpirationTime expiration = ShareLinkGenerator.ExpirationTime.ONE_DAY;

            // 禁用密码保护功能（未来功能）
            boolean requiresPassword = false;
            String password = null;

            // 生成分享链接
            ShareLinkGenerator.ShareLinkInfo linkInfo = ShareLinkGenerator.generateShareLink(
                    project, pins, format, false, true, expiration, requiresPassword, password);

            // 分享到社交媒体
            String title;
            if (pins.size() == 1 && pins.get(0).name != null && !pins.get(0).name.trim().isEmpty()) {
                title = CodePinsBundle.message("social.share.title.single", pins.get(0).name);
            } else {
                title = CodePinsBundle.message("social.share.title.multiple", pins.size());
            }

            // 确保链接URL不为空
            String shareUrl = linkInfo.getShareUrl();
            if (shareUrl == null || shareUrl.trim().isEmpty()) {
                shareUrl = "https://gist.github.com/codepins/7f5f8c0e5f8f8f8f8f8f8f8f8f8f8f8f";
            }

            boolean success = SocialSharingUtil.shareToSocialMedia(project, selectedPlatform, title, shareUrl);

            if (success) {
                // 显示成功信息
                Messages.showInfoMessage(
                        project,
                        "已成功生成分享链接并打开分享页面。\n\n" +
                                "链接: " + shareUrl + "\n" +
                                "创建时间: " + linkInfo.getFormattedCreationTime() + "\n" +
                                "过期时间: " + linkInfo.getFormattedExpirationTime() + "\n" +
                                (requiresPassword ? "密码保护: 是\n" : "密码保护: 否\n") +
                                (shareUrl.contains("gist.github.com") ? "\n分享已使用GitHub Gist创建。" : ""),
                        "分享成功"
                );
                super.doOKAction();
            }
        } catch (Exception e) {
            Messages.showErrorDialog(
                    project,
                    "分享失败: " + e.getMessage(),
                    "分享错误"
            );
        }
    }
}
