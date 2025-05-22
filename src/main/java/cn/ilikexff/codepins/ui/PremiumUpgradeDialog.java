package cn.ilikexff.codepins.ui;

import cn.ilikexff.codepins.services.LicenseService;
import cn.ilikexff.codepins.utils.IconUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.List;

/**
 * 高级升级对话框
 * 提供更具吸引力的专业版升级体验
 */
public class PremiumUpgradeDialog extends DialogWrapper {

    private final Project project;
    private final String featureName;
    private final Color accentColor = new JBColor(new Color(53, 114, 240), new Color(53, 114, 240));
    private final Color textColor = UIUtil.getLabelForeground();
    private final Color bgColor = UIUtil.getPanelBackground();
    private final Color cardBgColor = new JBColor(
            new Color(250, 250, 250), // 亮色主题
            new Color(50, 50, 55)     // 暗色主题
    );
    private final Color buttonHoverColor = new JBColor(
            new Color(45, 100, 220),  // 亮色主题
            new Color(65, 125, 255)   // 暗色主题
    );
    private final Color buttonPressedColor = new JBColor(
            new Color(35, 90, 200),   // 亮色主题
            new Color(55, 115, 235)   // 暗色主题
    );
    private final Color linkColor = new JBColor(
            new Color(53, 114, 240),  // 亮色主题
            new Color(88, 157, 246)   // 暗色主题
    );

    // 主要特性列表 - 2x2网格中显示的4个核心功能
    private final List<Feature> mainFeatures = Arrays.asList(
            new Feature("无限图钉", "免费版限制100个图钉，专业版无数量限制", "/icons/pin-off.svg"),
            new Feature("无限标签", "免费版限制10种不同标签和每个图钉3个标签，专业版无限制", "/icons/tags.svg"),
            new Feature("高级分享选项(即将推出)", "密码保护、链接有效期限制等高级分享选项", "/icons/lock.svg"),
            new Feature("更多社交分享平台", "支持7个国际社交媒体平台，包括Reddit、Telegram等", "/icons/waypoints.svg")
    );

    // 额外特性列表 - 点击"显示更多"后显示
    private final List<Feature> extraFeatures = Arrays.asList(
            new Feature("自定义水印", "自定义或完全移除分享图片中的水印", "/icons/image.svg"),
            new Feature("优先支持", "获得优先的技术支持和问题解决", "/icons/headphones.svg"),
            new Feature("未来高级功能", "自动获得所有未来的高级功能", "/icons/zap.svg")

    );

    private boolean showingMoreFeatures = false;
    private JPanel extraFeaturesPanel;
    private JLabel toggleLabel;
    // 添加一个变量来存储原始窗口高度
    private int originalWindowHeight;
    private JPanel contentPanel;
    private JPanel togglePanel;
    private JPanel bottomPanel;
    private int originalTogglePanelY;
    private int originalBottomPanelY;

    /**
     * 构造函数
     *
     * @param project     当前项目
     * @param featureName 功能名称
     */
    public PremiumUpgradeDialog(Project project, String featureName) {
        super(project);
        this.project = project;
        this.featureName = featureName;
        this.originalWindowHeight = 0; // 初始化原始高度

        setTitle("升级到 CodePins 专业版");
        setResizable(false);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout(0, 20));
        dialogPanel.setBorder(JBUI.Borders.empty(20));
        dialogPanel.setBackground(bgColor);

        // 添加标题
        dialogPanel.add(createTitlePanel(), BorderLayout.NORTH);
        
        // 添加内容面板
        dialogPanel.add(createContentPanel(), BorderLayout.CENTER);
        
        // 添加底部面板
        dialogPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        // 设置首选大小
        dialogPanel.setPreferredSize(new Dimension(550, 400));

        return dialogPanel;
    }

    /**
     * 创建标题面板
     */
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout(12, 0));
        titlePanel.setBackground(bgColor);
        titlePanel.setBorder(JBUI.Borders.emptyBottom(12));
        
        // 图标
        Icon crownIcon = IconUtil.loadIcon("/icons/crown.svg", getClass());
        JLabel iconLabel = new JLabel(crownIcon);
        
        // 标题
        JLabel titleLabel = new JLabel("升级到 CodePins 专业版");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        
        titlePanel.add(iconLabel, BorderLayout.WEST);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        // 如果有特定功能名称，添加提示
        if (featureName != null && !featureName.isEmpty()) {
            JLabel subtitleLabel = new JLabel("您正在尝试使用「" + featureName + "」，这是专业版专属功能");
            subtitleLabel.setForeground(new Color(255, 107, 0));
            subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 12f));
            subtitleLabel.setBorder(JBUI.Borders.emptyTop(4));
            
            JPanel textPanel = new JPanel(new BorderLayout(0, 0));
            textPanel.setBackground(bgColor);
            textPanel.add(titleLabel, BorderLayout.NORTH);
            textPanel.add(subtitleLabel, BorderLayout.CENTER);
            
            titlePanel.add(textPanel, BorderLayout.CENTER);
        }
        
        return titlePanel;
    }

    /**
     * 创建内容面板
     */
    private JPanel createContentPanel() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(bgColor);
        
        // 添加主要特性网格
        JPanel mainFeaturesGrid = new JPanel(new GridLayout(2, 2, 12, 12));
        mainFeaturesGrid.setBackground(bgColor);
        mainFeaturesGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // 添加主要特性卡片
        for (Feature feature : mainFeatures) {
            mainFeaturesGrid.add(createFeatureCard(feature));
        }
        
        contentPanel.add(mainFeaturesGrid);
        
        // 添加"显示更多"链接
        togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        togglePanel.setBackground(bgColor);
        togglePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        toggleLabel = new JLabel("显示更多功能 ▼");
        toggleLabel.setForeground(linkColor);
        toggleLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleLabel.setFont(toggleLabel.getFont().deriveFont(Font.PLAIN, 12f));
        
        toggleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleExtraFeatures();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                toggleLabel.setText(toggleLabel.getText().replace("▼", "▼").replace("▲", "▲"));
                toggleLabel.setFont(toggleLabel.getFont().deriveFont(Font.BOLD));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                toggleLabel.setFont(toggleLabel.getFont().deriveFont(Font.PLAIN));
            }
        });
        
        togglePanel.add(toggleLabel);
        contentPanel.add(togglePanel);
        
        // 创建额外特性面板（初始隐藏）
        extraFeaturesPanel = new JPanel();
        extraFeaturesPanel.setLayout(new BoxLayout(extraFeaturesPanel, BoxLayout.Y_AXIS));
        extraFeaturesPanel.setBackground(bgColor);
        extraFeaturesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        extraFeaturesPanel.setVisible(false);
        
        // 添加额外特性
        for (Feature feature : extraFeatures) {
            extraFeaturesPanel.add(createExtraFeatureItem(feature));
        }
        
        contentPanel.add(extraFeaturesPanel);
        
        // 在组件显示后记录原始位置
        SwingUtilities.invokeLater(() -> {
            if (togglePanel != null) {
                originalTogglePanelY = togglePanel.getY();
            }
        });
        
        return contentPanel;
    }

    /**
     * 切换额外特性的显示/隐藏
     */
    private void toggleExtraFeatures() {
        showingMoreFeatures = !showingMoreFeatures;
        
        if (showingMoreFeatures) {
            // 显示额外特性
            toggleLabel.setText("隐藏更多功能 ▲");
            extraFeaturesPanel.setVisible(true);
        } else {
            // 隐藏额外特性
            toggleLabel.setText("显示更多功能 ▼");
            extraFeaturesPanel.setVisible(false);
            
            // 重置布局 - 关键修复
            resetLayout();
        }
        
        // 重新布局
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /**
     * 重置布局到初始状态
     */
    private void resetLayout() {
        // 使用SwingUtilities.invokeLater确保在EDT线程上执行，并在所有挂起的事件处理完成后运行
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // 移除并重新添加组件以重置布局
                Container parent = contentPanel.getParent();
                if (parent != null) {
                    parent.remove(contentPanel);
                    parent.add(contentPanel);
                    
                    // 强制重新布局
                    parent.revalidate();
                    parent.repaint();
                }
            }
        });
    }

    /**
     * 创建底部面板
     */
    private JPanel createBottomPanel() {
        bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.setBackground(bgColor);
        bottomPanel.setBorder(JBUI.Borders.emptyTop(8));
        
        // 价格信息
        JLabel priceLabel = new JLabel("年度订阅: $19.99/年    永久授权: $49.99");
        priceLabel.setForeground(new Color(120, 120, 120));
        priceLabel.setFont(priceLabel.getFont().deriveFont(Font.PLAIN, 12f));
        
        // 升级按钮
        JButton upgradeButton = createPremiumButton("立即升级", null);
        upgradeButton.addActionListener(e -> {
            BrowserUtil.browse("https://plugins.jetbrains.com/plugin/27300-codepins--code-bookmarks/pricing");
            close(OK_EXIT_CODE);
        });
        
        // 组装底部面板
        bottomPanel.add(priceLabel, BorderLayout.NORTH);
        bottomPanel.add(upgradeButton, BorderLayout.CENTER);
        
        // 在组件显示后记录原始位置
        SwingUtilities.invokeLater(() -> {
            if (bottomPanel != null) {
                originalBottomPanelY = bottomPanel.getY();
            }
        });
        
        return bottomPanel;
    }

    /**
     * 创建特性卡片
     */
    private JPanel createFeatureCard(Feature feature) {
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制圆角矩形背景
                g2.setColor(cardBgColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                
                // 绘制边框
                g2.setColor(new JBColor(new Color(230, 230, 230), new Color(60, 60, 65)));
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                
                g2.dispose();
            }
        };
        
        card.setOpaque(false);
        card.setBorder(JBUI.Borders.empty(12));
        
        // 图标
        Icon icon = IconUtil.loadIcon(feature.iconPath, getClass());
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setVerticalAlignment(JLabel.TOP);
        iconLabel.setBorder(JBUI.Borders.emptyTop(2));
        
        // 文本内容
        JPanel textPanel = new JPanel(new BorderLayout(0, 4));
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(feature.title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        
        JLabel descLabel = new JLabel("<html><div style='color: #808080; font-size: 11px;'>" + 
                feature.description + "</div></html>");
        
        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(descLabel, BorderLayout.CENTER);
        
        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        
        return card;
    }

    /**
     * 创建额外特性项（无卡片边框的简洁样式）
     */
    private JPanel createExtraFeatureItem(Feature feature) {
        JPanel item = new JPanel(new BorderLayout(12, 0));
        item.setBackground(bgColor);
        item.setBorder(JBUI.Borders.empty(6, 12, 6, 12));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        // 图标
        Icon icon = IconUtil.loadIcon(feature.iconPath, getClass());
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setVerticalAlignment(JLabel.TOP);
        
        // 文本内容
        JPanel textPanel = new JPanel(new BorderLayout(0, 2));
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(feature.title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        
        JLabel descLabel = new JLabel("<html><div style='color: #808080; font-size: 11px;'>" + 
                feature.description + "</div></html>");
        
        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(descLabel, BorderLayout.CENTER);
        
        item.add(iconLabel, BorderLayout.WEST);
        item.add(textPanel, BorderLayout.CENTER);
        
        return item;
    }

    /**
     * 创建高级按钮
     */
    private JButton createPremiumButton(String text, String iconPath) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制渐变背景
                Color buttonColor;
                if (getModel().isPressed()) {
                    buttonColor = buttonPressedColor;
                } else if (getModel().isRollover()) {
                    buttonColor = buttonHoverColor;
                } else {
                    buttonColor = accentColor;
                }
                
                g2.setColor(buttonColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        // 设置图标
        if (iconPath != null) {
            button.setIcon(IconUtil.loadIcon(iconPath, getClass()));
            button.setIconTextGap(8);
        }
        
        // 设置样式
        button.setForeground(Color.WHITE);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(JBUI.Borders.empty(8, 16));
        
        return button;
    }

    /**
     * 特性数据类
     */
    private static class Feature {
        final String title;
        final String description;
        final String iconPath;
        
        Feature(String title, String description, String iconPath) {
            this.title = title;
            this.description = description;
            this.iconPath = iconPath;
        }
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
        PremiumUpgradeDialog dialog = new PremiumUpgradeDialog(project, featureName);
        return dialog.showAndGet();
    }
}
