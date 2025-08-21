package cn.ilikexff.codepins.ui;

import cn.ilikexff.codepins.i18n.CodePinsBundle;
import cn.ilikexff.codepins.statistics.PinStatistics;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.Map;

/**
 * 图钉统计面板
 * 显示各种维度的图钉统计信息
 */
public class StatisticsPanel extends JBPanel<StatisticsPanel> {
    
    private JBLabel totalPinsLabel;
    private JBLabel singleLinePinsLabel;
    private JBLabel blockPinsLabel;
    private JBLabel uniqueFilesLabel;
    private JBLabel uniqueAuthorsLabel;
    private JBLabel uniqueTagsLabel;
    
    private JPanel tagStatsPanel;
    private JPanel fileStatsPanel;
    private JPanel authorStatsPanel;
    private JPanel timeStatsPanel;
    
    public StatisticsPanel() {
        setLayout(new BorderLayout());
        initComponents();
        refreshStatistics();
    }
    
    private void initComponents() {
        // 创建主滚动面板
        JPanel mainPanel = new JBPanel<>(new BorderLayout());
        
        // 总体统计面板
        JPanel overallPanel = createOverallStatsPanel();
        mainPanel.add(overallPanel, BorderLayout.NORTH);
        
        // 详细统计面板
        JPanel detailsPanel = new JBPanel<>(new GridLayout(2, 2, 10, 10));
        detailsPanel.setBorder(JBUI.Borders.empty(10));
        
        // 标签统计
        tagStatsPanel = createStatsPanel(CodePinsBundle.message("statistics.tag.usage"));
        detailsPanel.add(tagStatsPanel);

        // 文件分布统计
        fileStatsPanel = createStatsPanel(CodePinsBundle.message("statistics.file.distribution"));
        detailsPanel.add(fileStatsPanel);

        // 作者统计
        authorStatsPanel = createStatsPanel(CodePinsBundle.message("statistics.author.stats"));
        detailsPanel.add(authorStatsPanel);

        // 时间分布统计
        timeStatsPanel = createStatsPanel(CodePinsBundle.message("statistics.time.distribution"));
        detailsPanel.add(timeStatsPanel);
        
        mainPanel.add(detailsPanel, BorderLayout.CENTER);
        
        // 添加滚动支持
        JBScrollPane scrollPane = new JBScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createOverallStatsPanel() {
        JPanel panel = new JBPanel<>(new GridLayout(2, 3, 15, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(JBColor.GRAY),
                CodePinsBundle.message("statistics.overall.title"),
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font(Font.SANS_SERIF, Font.BOLD, 14)
        ));
        panel.setBorder(JBUI.Borders.compound(
                panel.getBorder(),
                JBUI.Borders.empty(10)
        ));
        
        // 创建统计标签
        totalPinsLabel = createStatLabel(CodePinsBundle.message("statistics.total.pins"), "0");
        singleLinePinsLabel = createStatLabel(CodePinsBundle.message("statistics.single.pins"), "0");
        blockPinsLabel = createStatLabel(CodePinsBundle.message("statistics.block.pins"), "0");
        uniqueFilesLabel = createStatLabel(CodePinsBundle.message("statistics.unique.files"), "0");
        uniqueAuthorsLabel = createStatLabel(CodePinsBundle.message("statistics.unique.authors"), "0");
        uniqueTagsLabel = createStatLabel(CodePinsBundle.message("statistics.unique.tags"), "0");
        
        panel.add(totalPinsLabel);
        panel.add(singleLinePinsLabel);
        panel.add(blockPinsLabel);
        panel.add(uniqueFilesLabel);
        panel.add(uniqueAuthorsLabel);
        panel.add(uniqueTagsLabel);
        
        return panel;
    }
    
    private JBLabel createStatLabel(String title, String value) {
        JBLabel label = new JBLabel();
        label.setText("<html><div style='text-align: center;'>" +
                "<div style='font-size: 12px; color: #666;'>" + title + "</div>" +
                "<div style='font-size: 18px; font-weight: bold; color: #2196F3;'>" + value + "</div>" +
                "</div></html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(JBUI.Borders.empty(5));
        return label;
    }
    
    private JPanel createStatsPanel(String title) {
        JPanel panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(JBColor.GRAY),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font(Font.SANS_SERIF, Font.BOLD, 12)
        ));
        
        JPanel contentPanel = new JBPanel<>();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        JBScrollPane scrollPane = new JBScrollPane(contentPanel);
        scrollPane.setPreferredSize(new Dimension(200, 150));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 刷新统计数据
     */
    public void refreshStatistics() {
        SwingUtilities.invokeLater(() -> {
            try {
                // 更新总体统计
                updateOverallStats();
                
                // 更新详细统计
                updateTagStats();
                updateFileStats();
                updateAuthorStats();
                updateTimeStats();
                
                // 重绘界面
                revalidate();
                repaint();
            } catch (Exception e) {
                System.err.println("[CodePins] 刷新统计数据异常: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void updateOverallStats() {
        PinStatistics.OverallStats stats = PinStatistics.getOverallStats();
        
        updateStatLabel(totalPinsLabel, "总图钉数", String.valueOf(stats.totalPins));
        updateStatLabel(singleLinePinsLabel, "单行图钉", String.valueOf(stats.singleLinePins));
        updateStatLabel(blockPinsLabel, "代码块图钉", String.valueOf(stats.blockPins));
        updateStatLabel(uniqueFilesLabel, "涉及文件", String.valueOf(stats.uniqueFiles));
        updateStatLabel(uniqueAuthorsLabel, "参与作者", String.valueOf(stats.uniqueAuthors));
        updateStatLabel(uniqueTagsLabel, "标签种类", String.valueOf(stats.uniqueTags));
    }
    
    private void updateStatLabel(JBLabel label, String title, String value) {
        label.setText("<html><div style='text-align: center;'>" +
                "<div style='font-size: 12px; color: #666;'>" + title + "</div>" +
                "<div style='font-size: 18px; font-weight: bold; color: #2196F3;'>" + value + "</div>" +
                "</div></html>");
    }
    
    private void updateTagStats() {
        updateTagDetailStats(tagStatsPanel, PinStatistics.getTagStats());
    }

    private void updateFileStats() {
        updateDetailStats(fileStatsPanel, PinStatistics.getFileStats(), false);
    }

    private void updateAuthorStats() {
        updateDetailStats(authorStatsPanel, PinStatistics.getAuthorStats(), false);
    }

    private void updateTimeStats() {
        updateDetailStats(timeStatsPanel, PinStatistics.getTimeStats(), false);
    }
    
    private void updateDetailStats(JPanel panel, Map<String, Integer> stats, boolean isTagStats) {
        // 获取内容面板
        JBScrollPane scrollPane = (JBScrollPane) panel.getComponent(0);
        JPanel contentPanel = (JPanel) scrollPane.getViewport().getView();

        // 清空现有内容
        contentPanel.removeAll();

        // 添加统计项
        if (stats.isEmpty()) {
            JBLabel noDataLabel = new JBLabel(CodePinsBundle.message("statistics.no.data"));
            noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noDataLabel.setForeground(JBColor.GRAY);
            contentPanel.add(noDataLabel);
        } else {
            int maxCount = stats.values().stream().mapToInt(Integer::intValue).max().orElse(1);

            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                JPanel itemPanel = isTagStats ?
                    createTagStatItem(entry.getKey(), entry.getValue(), maxCount) :
                    createStatItem(entry.getKey(), entry.getValue(), maxCount);
                contentPanel.add(itemPanel);
            }
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void updateTagDetailStats(JPanel panel, Map<String, Integer> stats) {
        updateDetailStats(panel, stats, true);
    }
    
    private JPanel createStatItem(String name, int count, int maxCount) {
        JPanel panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(5, 10));

        // 名称标签
        JBLabel nameLabel = new JBLabel(name);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, 12f));

        // 进度条
        JProgressBar progressBar = new JProgressBar(0, maxCount);
        progressBar.setValue(count);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(100, 6));
        progressBar.setForeground(new JBColor(new Color(33, 150, 243), new Color(100, 181, 246)));

        // 数量标签
        JBLabel countLabel = new JBLabel(String.valueOf(count));
        countLabel.setForeground(JBColor.BLUE);
        countLabel.setFont(countLabel.getFont().deriveFont(Font.BOLD, 11f));
        countLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // 垂直布局：名称在上，进度条在中，数量在下
        JPanel contentPanel = new JBPanel<>(new BorderLayout(0, 3));
        contentPanel.add(nameLabel, BorderLayout.NORTH);
        contentPanel.add(progressBar, BorderLayout.CENTER);
        contentPanel.add(countLabel, BorderLayout.SOUTH);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTagStatItem(String tagName, int count, int maxCount) {
        JPanel panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(2, 5));

        // 获取标签颜色
        Color tagColor = getTagColor(tagName);

        // 标签名称
        JBLabel nameLabel = new JBLabel(tagName);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, 12f));

        // 数量标签
        JBLabel countLabel = new JBLabel(String.valueOf(count));
        countLabel.setForeground(tagColor);
        countLabel.setFont(countLabel.getFont().deriveFont(Font.BOLD, 11f));

        // 进度条，使用标签颜色
        JProgressBar progressBar = new JProgressBar(0, maxCount);
        progressBar.setValue(count);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(50, 8));

        // 强制设置进度条颜色，确保在所有主题下都能正确显示
        progressBar.setForeground(tagColor);
        progressBar.setBackground(new JBColor(new Color(240, 240, 240), new Color(60, 63, 65)));

        // 设置UI属性以确保颜色生效
        progressBar.putClientProperty("JProgressBar.largeHeight", Boolean.FALSE);
        progressBar.putClientProperty("JProgressBar.stripeWidth", 4);

        // 强制使用自定义UI来确保颜色生效
        progressBar.setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
            @Override
            protected Color getSelectionForeground() {
                return tagColor;
            }

            @Override
            protected Color getSelectionBackground() {
                return tagColor;
            }

            @Override
            protected void paintDeterminate(Graphics g, JComponent c) {
                if (!(g instanceof Graphics2D)) {
                    return;
                }

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Insets b = progressBar.getInsets();
                int barRectWidth = progressBar.getWidth() - (b.right + b.left);
                int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);

                if (barRectWidth <= 0 || barRectHeight <= 0) {
                    return;
                }

                int cellLength = getCellLength();
                int cellSpacing = getCellSpacing();
                int amountFull = getAmountFull(b, barRectWidth, barRectHeight);

                // 绘制背景
                g2d.setColor(progressBar.getBackground());
                g2d.fillRect(b.left, b.top, barRectWidth, barRectHeight);

                // 绘制进度
                if (amountFull > 0) {
                    g2d.setColor(tagColor);
                    g2d.fillRect(b.left, b.top, amountFull, barRectHeight);
                }
            }
        });

        JPanel leftPanel = new JBPanel<>(new BorderLayout());
        leftPanel.add(nameLabel, BorderLayout.WEST);
        leftPanel.add(progressBar, BorderLayout.CENTER);

        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(countLabel, BorderLayout.EAST);

        return panel;
    }

    /**
     * 根据标签名称生成颜色（与其他组件保持一致）
     */
    private Color getTagColor(String tag) {
        // 使用标签的哈希值生成颜色，确保相同标签有相同颜色
        int hash = tag.hashCode();

        // 现代感强的色调
        Color[] lightPalette = {
                new Color(79, 195, 247),  // 浅蓝
                new Color(129, 199, 132), // 浅绿
                new Color(255, 183, 77),  // 浅橙
                new Color(240, 98, 146),  // 浅红
                new Color(149, 117, 205), // 浅紫
                new Color(224, 224, 224), // 浅灰
                new Color(77, 208, 225),  // 浅青
                new Color(174, 213, 129)  // 浅黄绿
        };

        Color[] darkPalette = {
                new Color(41, 121, 255),  // 深蓝
                new Color(67, 160, 71),   // 深绿
                new Color(255, 152, 0),   // 深橙
                new Color(233, 30, 99),   // 深红
                new Color(103, 58, 183),  // 深紫
                new Color(117, 117, 117), // 深灰
                new Color(0, 172, 193),   // 深青
                new Color(104, 159, 56)   // 深黄绿
        };

        int index = Math.abs(hash) % lightPalette.length;
        return new JBColor(lightPalette[index], darkPalette[index]);
    }
}
