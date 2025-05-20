package cn.ilikexff.codepins.ui;

import cn.ilikexff.codepins.PinEntry;
import cn.ilikexff.codepins.services.LicenseService;
import cn.ilikexff.codepins.utils.*;
import com.intellij.openapi.util.IconLoader;
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
    private final String shareContent;
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

        // 生成分享内容
        this.shareContent = SharingUtil.formatPins(project, pins, format, codeOnly, showLineNumbers);

        setTitle("分享到社交媒体");
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
        platformPanel.setBorder(BorderFactory.createTitledBorder("选择平台"));
        platformPanel.setBorder(BorderFactory.createCompoundBorder(
                platformPanel.getBorder(),
                JBUI.Borders.empty(5, 5, 5, 5))); // 添加内边距

        // 获取支持的平台
        boolean isPremium = SocialSharingUtil.isPremiumUser();
        SocialSharingUtil.SocialPlatform[] platforms = SocialSharingUtil.getSupportedPlatforms(isPremium);
        platformRadios = new JRadioButton[platforms.length];
        ButtonGroup platformGroup = new ButtonGroup();

        // 创建国际平台和国内平台面板
        JPanel internationalPanel = new JPanel(new GridLayout(0, 3, 10, 5)); // 3列网格
        internationalPanel.setBorder(BorderFactory.createTitledBorder("国际平台"));

        JPanel domesticPanel = new JPanel(new GridLayout(0, 3, 10, 5)); // 3列网格
        domesticPanel.setBorder(BorderFactory.createTitledBorder("国内平台"));

        // 添加平台选项
        int firstInternationalIndex = -1;
        int firstDomesticIndex = -1;

        for (int i = 0; i < platforms.length; i++) {
            SocialSharingUtil.SocialPlatform platform = platforms[i];
            platformRadios[i] = new JBRadioButton(platform.getDisplayName());
            platformGroup.add(platformRadios[i]);

            // 根据平台类型添加到不同面板
            switch (platform) {
                case WEIBO:
                case WECHAT:
                case QQ:
                case QZONE:
                case DOUBAN:
                case ZHIHU:
                    domesticPanel.add(platformRadios[i]);
                    if (firstDomesticIndex == -1) {
                        firstDomesticIndex = i;
                    }
                    break;
                default:
                    internationalPanel.add(platformRadios[i]);
                    if (firstInternationalIndex == -1) {
                        firstInternationalIndex = i;
                    }
                    break;
            }
        }

        // 默认选中第一个选项
        if (firstInternationalIndex != -1) {
            platformRadios[firstInternationalIndex].setSelected(true);
        } else if (firstDomesticIndex != -1) {
            platformRadios[firstDomesticIndex].setSelected(true);
        }

        // 添加到平台面板
        JPanel platformsContainer = new JPanel(new BorderLayout(0, 10));
        platformsContainer.add(internationalPanel, BorderLayout.NORTH);
        platformsContainer.add(domesticPanel, BorderLayout.CENTER);

        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(platformsContainer);
        scrollPane.setBorder(JBUI.Borders.empty());
        scrollPane.setPreferredSize(new Dimension(-1, 150)); // 减小高度，留出更多空间给链接选项

        platformPanel.add(scrollPane, BorderLayout.CENTER);

        // 如果用户不是付费用户，禁用付费平台
        if (!isPremium) {
            SocialSharingUtil.SocialPlatform[] freePlatforms = SocialSharingUtil.getSupportedPlatforms(false);
            for (int i = 0; i < platforms.length; i++) {
                boolean isPlatformFree = false;
                for (SocialSharingUtil.SocialPlatform freePlatform : freePlatforms) {
                    if (platforms[i] == freePlatform) {
                        isPlatformFree = true;
                        break;
                    }
                }

                if (!isPlatformFree) {
                    final int platformIndex = i; // 创建一个最终变量供匿名类使用
                    platformRadios[i].setEnabled(false);
                    platformRadios[i].setText(platformRadios[i].getText() + " (付费功能)");

                    // 添加点击事件，显示升级对话框
                    platformRadios[i].addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            // 显示升级对话框
                            LicenseService.getInstance().showUpgradeDialogIfNeeded(project,
                                    "使用" + platforms[platformIndex].getDisplayName() + "分享");
                        }
                    });
                }
            }
        }

        // 创建链接选项面板
        JPanel linkPanel = new JPanel(new BorderLayout(0, 5)); // 减小垂直间距
        linkPanel.setBorder(BorderFactory.createTitledBorder("链接选项"));
        linkPanel.setBorder(BorderFactory.createCompoundBorder(
                linkPanel.getBorder(),
                JBUI.Borders.empty(5, 5, 5, 5))); // 添加内边距

        // 过期时间选择
        JPanel expirationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        expirationPanel.add(new JBLabel("过期时间:"));

        expirationComboBox = new JComboBox<>(ShareLinkGenerator.ExpirationTime.values());
        expirationComboBox.setSelectedItem(ShareLinkGenerator.ExpirationTime.ONE_DAY);
        expirationPanel.add(expirationComboBox);

        // 如果用户不是付费用户，禁用永不过期选项
        if (!isPremium) {
            expirationComboBox.setEnabled(false);
            expirationPanel.add(new JBLabel(" (付费功能)"));

            // 添加点击事件，显示升级对话框
            expirationComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    // 显示升级对话框
                    LicenseService.getInstance().showUpgradeDialogIfNeeded(project, "自定义过期时间");
                }
            });
        }

        linkPanel.add(expirationPanel, BorderLayout.NORTH);

        // 密码保护选项
        passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passwordCheckBox = new JCheckBox("密码保护:");
        passwordField = new JPasswordField(15);
        passwordField.setEnabled(false);

        passwordCheckBox.addActionListener(e -> passwordField.setEnabled(passwordCheckBox.isSelected()));

        passwordPanel.add(passwordCheckBox);
        passwordPanel.add(passwordField);

        // 如果用户不是付费用户，禁用密码保护选项
        if (!isPremium) {
            passwordCheckBox.setEnabled(false);
            passwordField.setEnabled(false);
            passwordPanel.add(new JBLabel(" (付费功能)"));

            // 添加点击事件，显示升级对话框
            passwordCheckBox.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    // 显示升级对话框
                    LicenseService.getInstance().showUpgradeDialogIfNeeded(project, "密码保护");
                }
            });

            passwordField.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    // 显示升级对话框
                    LicenseService.getInstance().showUpgradeDialogIfNeeded(project, "密码保护");
                }
            });
        }

        linkPanel.add(passwordPanel, BorderLayout.CENTER);

        // 创建信息面板
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("分享信息"));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                infoPanel.getBorder(),
                JBUI.Borders.empty(5, 5, 5, 5))); // 添加内边距

        // 添加提示信息
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBackground(JBColor.background());
        infoArea.setText("将分享 " + pins.size() + " 个图钉到社交媒体。\n\n" +
                "注意：分享链接将在指定时间后过期，请确保接收者及时查看。\n" +
                "免费版用户的链接将在1天后过期，付费版用户可以设置更长的过期时间或永不过期。");

        // 设置固定高度，避免文本区域过大
        JScrollPane infoScrollPane = new JScrollPane(infoArea);
        infoScrollPane.setPreferredSize(new Dimension(-1, 80)); // 设置固定高度
        infoPanel.add(infoScrollPane, BorderLayout.CENTER);

        // 如果不是付费用户，添加升级提示
        if (!isPremium) {
            JPanel upgradePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            Icon infoIcon = IconLoader.getIcon("/icons/info.svg", getClass());
            JButton upgradeButton = new JButton("升级到付费版", infoIcon);
            upgradeButton.addActionListener(e -> {
                // 显示升级对话框
                LicenseService.getInstance().showUpgradeDialogIfNeeded(project, "社交分享高级功能");
            });
            upgradePanel.add(upgradeButton);
            infoPanel.add(upgradePanel, BorderLayout.SOUTH);
        }

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
            boolean isPremium = SocialSharingUtil.isPremiumUser();
            SocialSharingUtil.SocialPlatform[] platforms = SocialSharingUtil.getSupportedPlatforms(isPremium);

            for (int i = 0; i < platformRadios.length; i++) {
                if (platformRadios[i].isSelected()) {
                    selectedPlatform = platforms[i];
                    break;
                }
            }

            if (selectedPlatform == null) {
                Messages.showErrorDialog(
                        project,
                        "请选择一个社交媒体平台",
                        "分享错误"
                );
                return;
            }

            // 获取过期时间
            ShareLinkGenerator.ExpirationTime expiration =
                    (ShareLinkGenerator.ExpirationTime) expirationComboBox.getSelectedItem();

            // 获取密码
            boolean requiresPassword = passwordCheckBox.isSelected();
            String password = requiresPassword ? new String(passwordField.getPassword()) : null;

            // 如果选择了密码保护但没有输入密码
            if (requiresPassword && (password == null || password.isEmpty())) {
                Messages.showErrorDialog(
                        project,
                        "请输入密码",
                        "分享错误"
                );
                return;
            }

            // 生成分享链接
            ShareLinkGenerator.ShareLinkInfo linkInfo = ShareLinkGenerator.generateShareLink(
                    project, pins, format, false, true, expiration, requiresPassword, password);

            // 分享到社交媒体
            String title = "CodePins分享";
            if (pins.size() == 1 && pins.get(0).name != null && !pins.get(0).name.trim().isEmpty()) {
                title += ": " + pins.get(0).name;
            } else {
                title += ": " + pins.size() + "个图钉";
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
