package cn.ilikexff.codepins.ai.ui;

import cn.ilikexff.codepins.ai.SmartSuggestion;
import cn.ilikexff.codepins.ai.SuggestionLearningEngine;
import com.intellij.ui.components.*;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * 智能建议配置面板
 * 允许用户配置建议类型、敏感度等参数
 */
public class SuggestionConfigPanel extends JBPanel<SuggestionConfigPanel> {
    
    private final SuggestionLearningEngine learningEngine;
    private final Map<SmartSuggestion.SuggestionType, JCheckBox> typeCheckBoxes;
    private final JBSlider confidenceSlider;
    private final JBLabel confidenceLabel;
    private final JBLabel statisticsLabel;
    private final JButton resetButton;
    
    public SuggestionConfigPanel() {
        this.learningEngine = SuggestionLearningEngine.getInstance();
        this.typeCheckBoxes = new HashMap<>();
        this.confidenceSlider = new JBSlider(0, 100, 50);
        this.confidenceLabel = new JBLabel();
        this.statisticsLabel = new JBLabel();
        this.resetButton = new JButton("重置学习数据");
        
        initializeUI();
        loadCurrentSettings();
        setupListeners();
    }
    
    /**
     * 初始化UI
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));
        
        // 主面板
        JPanel mainPanel = new JBPanel<>(new BorderLayout());
        
        // 建议类型配置
        JPanel typePanel = createTypeConfigPanel();
        
        // 置信度配置
        JPanel confidencePanel = createConfidencePanel();
        
        // 统计信息
        JPanel statsPanel = createStatisticsPanel();
        
        // 操作按钮
        JPanel buttonPanel = createButtonPanel();
        
        mainPanel.add(typePanel, BorderLayout.NORTH);
        mainPanel.add(confidencePanel, BorderLayout.CENTER);
        mainPanel.add(statsPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 创建建议类型配置面板
     */
    private JPanel createTypeConfigPanel() {
        JPanel panel = new JBPanel<>(new GridLayout(0, 2, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("建议类型"));
        
        // 为每种建议类型创建复选框
        for (SmartSuggestion.SuggestionType type : SmartSuggestion.SuggestionType.values()) {
            JCheckBox checkBox = new JCheckBox(type.getDisplayName(), true);
            checkBox.setToolTipText(type.getDescription());
            typeCheckBoxes.put(type, checkBox);
            panel.add(checkBox);
        }
        
        return panel;
    }
    
    /**
     * 创建置信度配置面板
     */
    private JPanel createConfidencePanel() {
        JPanel panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("置信度阈值"));
        
        // 滑块配置
        confidenceSlider.setMajorTickSpacing(25);
        confidenceSlider.setMinorTickSpacing(5);
        confidenceSlider.setPaintTicks(true);
        confidenceSlider.setPaintLabels(true);
        
        // 标签字典
        java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
        labelTable.put(0, new JLabel("0%"));
        labelTable.put(25, new JLabel("25%"));
        labelTable.put(50, new JLabel("50%"));
        labelTable.put(75, new JLabel("75%"));
        labelTable.put(100, new JLabel("100%"));
        confidenceSlider.setLabelTable(labelTable);
        
        // 当前值标签
        confidenceLabel.setText("当前阈值: 50%");
        confidenceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 说明文本
        JBLabel descLabel = new JBLabel("<html><small>只显示置信度高于此阈值的建议</small></html>");
        descLabel.setForeground(Color.GRAY);
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(confidenceLabel, BorderLayout.NORTH);
        panel.add(confidenceSlider, BorderLayout.CENTER);
        panel.add(descLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 创建统计信息面板
     */
    private JPanel createStatisticsPanel() {
        JPanel panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("学习统计"));
        
        statisticsLabel.setVerticalAlignment(SwingConstants.TOP);
        statisticsLabel.setText("加载统计信息中...");
        
        JBScrollPane scrollPane = new JBScrollPane(statisticsLabel);
        scrollPane.setPreferredSize(new Dimension(0, 100));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JBPanel<>(new FlowLayout(FlowLayout.RIGHT));
        
        JButton applyButton = new JButton("应用设置");
        applyButton.addActionListener(e -> applySettings());
        
        resetButton.addActionListener(e -> resetLearningData());
        
        panel.add(resetButton);
        panel.add(applyButton);
        
        return panel;
    }
    
    /**
     * 设置监听器
     */
    private void setupListeners() {
        // 置信度滑块监听器
        confidenceSlider.addChangeListener(e -> {
            int value = confidenceSlider.getValue();
            confidenceLabel.setText("当前阈值: " + value + "%");
        });
        
        // 类型复选框监听器
        for (Map.Entry<SmartSuggestion.SuggestionType, JCheckBox> entry : typeCheckBoxes.entrySet()) {
            entry.getValue().addActionListener(e -> updateTypeSettings());
        }
    }
    
    /**
     * 加载当前设置
     */
    private void loadCurrentSettings() {
        SuggestionLearningEngine.LearningStatistics stats = learningEngine.getStatistics();
        
        // 设置置信度滑块
        int confidencePercent = (int) (stats.confidenceThreshold * 100);
        confidenceSlider.setValue(confidencePercent);
        confidenceLabel.setText("当前阈值: " + confidencePercent + "%");
        
        // 更新统计信息
        updateStatistics();
    }
    
    /**
     * 更新类型设置
     */
    private void updateTypeSettings() {
        // 实时应用类型启用/禁用设置
        for (Map.Entry<SmartSuggestion.SuggestionType, JCheckBox> entry : typeCheckBoxes.entrySet()) {
            SmartSuggestion.SuggestionType type = entry.getKey();
            boolean enabled = entry.getValue().isSelected();
            
            if (enabled) {
                learningEngine.enableSuggestionType(type);
            } else {
                learningEngine.disableSuggestionType(type);
            }
        }
    }
    
    /**
     * 应用设置
     */
    private void applySettings() {
        // 应用置信度设置
        double confidenceThreshold = confidenceSlider.getValue() / 100.0;
        learningEngine.setConfidenceThreshold(confidenceThreshold);
        
        // 应用类型设置
        updateTypeSettings();
        
        // 显示成功消息
        JOptionPane.showMessageDialog(this, "设置已应用", "成功", JOptionPane.INFORMATION_MESSAGE);
        
        // 更新统计信息
        updateStatistics();
    }
    
    /**
     * 重置学习数据
     */
    private void resetLearningData() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "确定要重置所有学习数据吗？这将清除用户偏好和历史反馈。",
            "确认重置",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            learningEngine.resetLearning();
            loadCurrentSettings();
            JOptionPane.showMessageDialog(this, "学习数据已重置", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * 更新统计信息
     */
    private void updateStatistics() {
        SuggestionLearningEngine.LearningStatistics stats = learningEngine.getStatistics();
        
        StringBuilder html = new StringBuilder("<html>");
        html.append("<b>学习统计信息:</b><br>");
        html.append("总建议数: ").append(stats.totalSuggestions).append("<br>");
        html.append("已应用: ").append(stats.appliedSuggestions).append("<br>");
        html.append("已忽略: ").append(stats.dismissedSuggestions).append("<br>");
        html.append("应用率: ").append(String.format("%.1f%%", stats.applyRate * 100)).append("<br><br>");
        
        html.append("<b>类型偏好权重:</b><br>");
        if (stats.typePreferences != null) {
            for (Map.Entry<String, Double> entry : stats.typePreferences.entrySet()) {
                html.append(entry.getKey()).append(": ")
                    .append(String.format("%.2f", entry.getValue())).append("<br>");
            }
        }
        
        html.append("</html>");
        
        statisticsLabel.setText(html.toString());
    }
    
    /**
     * 刷新面板数据
     */
    public void refresh() {
        loadCurrentSettings();
    }
}
