package cn.ilikexff.codepins.git.ui;

import cn.ilikexff.codepins.git.*;
import cn.ilikexff.codepins.core.PinEntry;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Git集成面板
 * 提供Git相关功能的用户界面
 */
public class GitIntegrationPanel extends JBPanel<GitIntegrationPanel> {
    
    private final Project project;
    private final GitIntegrationService gitService;
    private final GitPinCreator pinCreator;
    
    // UI组件
    private JLabel statusLabel;
    private JButton refreshButton;
    private JButton analyzeChangesButton;
    private JButton saveSnapshotButton;
    private JButton viewHistoryButton;
    
    // 功能面板
    private JPanel statusPanel;
    private JPanel actionsPanel;
    private JPanel suggestionsPanel;
    private JPanel historyPanel;
    
    // 数据显示
    private DefaultListModel<PinSuggestion> suggestionsModel;
    private JList<PinSuggestion> suggestionsList;
    private DefaultListModel<PinSnapshot> historyModel;
    private JList<PinSnapshot> historyList;
    
    public GitIntegrationPanel(Project project) {
        this.project = project;
        this.gitService = GitIntegrationService.getInstance(project);
        this.pinCreator = new GitPinCreator(project);

        initializeUI();
        setupEventListeners();
    }
    
    /**
     * 初始化UI
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));
        
        // 创建主要组件
        createStatusPanel();
        createActionsPanel();
        createSuggestionsPanel();
        createHistoryPanel();
        
        // 布局
        JPanel topPanel = new JBPanel<>(new BorderLayout());
        topPanel.add(statusPanel, BorderLayout.NORTH);
        topPanel.add(actionsPanel, BorderLayout.CENTER);
        
        JTabbedPane contentTabs = new JTabbedPane();
        contentTabs.addTab("智能建议", suggestionsPanel);
        contentTabs.addTab("版本历史", historyPanel);
        
        add(topPanel, BorderLayout.NORTH);
        add(contentTabs, BorderLayout.CENTER);
        
        // 初始化状态
        updateStatus();
    }
    
    /**
     * 创建状态面板
     */
    private void createStatusPanel() {
        statusPanel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(JBUI.Borders.empty(5));
        
        statusLabel = new JLabel("正在检查Git状态...");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        
        statusPanel.add(new JLabel("Git状态: "));
        statusPanel.add(statusLabel);
    }
    
    /**
     * 创建操作面板
     */
    private void createActionsPanel() {
        actionsPanel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actionsPanel.setBorder(JBUI.Borders.empty(5));
        
        refreshButton = new JButton("刷新状态");
        refreshButton.setToolTipText("刷新Git仓库状态");
        
        analyzeChangesButton = new JButton("分析变更");
        analyzeChangesButton.setToolTipText("分析当前变更并生成图钉建议");
        
        saveSnapshotButton = new JButton("保存快照");
        saveSnapshotButton.setToolTipText("保存当前图钉状态快照");
        
        viewHistoryButton = new JButton("查看历史");
        viewHistoryButton.setToolTipText("查看图钉版本历史");
        
        actionsPanel.add(refreshButton);
        actionsPanel.add(analyzeChangesButton);
        actionsPanel.add(saveSnapshotButton);
        actionsPanel.add(viewHistoryButton);
    }
    
    /**
     * 创建建议面板
     */
    private void createSuggestionsPanel() {
        suggestionsPanel = new JBPanel<>(new BorderLayout());
        suggestionsPanel.setBorder(JBUI.Borders.empty(5));
        
        // 标题
        JLabel titleLabel = new JLabel("基于Git变更的智能建议");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        
        // 建议列表
        suggestionsModel = new DefaultListModel<>();
        suggestionsList = new JList<>(suggestionsModel);
        suggestionsList.setCellRenderer(new SuggestionListCellRenderer());
        suggestionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JBScrollPane suggestionsScrollPane = new JBScrollPane(suggestionsList);
        suggestionsScrollPane.setPreferredSize(new Dimension(400, 200));
        
        // 操作按钮
        JPanel suggestionsButtonPanel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT));
        JButton createPinButton = new JButton("创建图钉");
        JButton ignoreButton = new JButton("忽略建议");
        
        createPinButton.addActionListener(e -> createPinFromSuggestion());
        ignoreButton.addActionListener(e -> ignoreSuggestion());
        
        suggestionsButtonPanel.add(createPinButton);
        suggestionsButtonPanel.add(ignoreButton);
        
        suggestionsPanel.add(titleLabel, BorderLayout.NORTH);
        suggestionsPanel.add(suggestionsScrollPane, BorderLayout.CENTER);
        suggestionsPanel.add(suggestionsButtonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 创建历史面板
     */
    private void createHistoryPanel() {
        historyPanel = new JBPanel<>(new BorderLayout());
        historyPanel.setBorder(JBUI.Borders.empty(5));
        
        // 标题
        JLabel titleLabel = new JLabel("图钉版本历史");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        
        // 历史列表
        historyModel = new DefaultListModel<>();
        historyList = new JList<>(historyModel);
        historyList.setCellRenderer(new HistoryListCellRenderer());
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JBScrollPane historyScrollPane = new JBScrollPane(historyList);
        historyScrollPane.setPreferredSize(new Dimension(400, 200));
        
        // 操作按钮
        JPanel historyButtonPanel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT));
        JButton compareButton = new JButton("比较版本");
        JButton restoreButton = new JButton("恢复快照");
        
        compareButton.addActionListener(e -> compareVersions());
        restoreButton.addActionListener(e -> restoreSnapshot());
        
        historyButtonPanel.add(compareButton);
        historyButtonPanel.add(restoreButton);
        
        historyPanel.add(titleLabel, BorderLayout.NORTH);
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);
        historyPanel.add(historyButtonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        refreshButton.addActionListener(e -> updateStatus());
        analyzeChangesButton.addActionListener(e -> analyzeChanges());
        saveSnapshotButton.addActionListener(e -> saveSnapshot());
        viewHistoryButton.addActionListener(e -> loadHistory());
    }
    
    /**
     * 面板激活时调用
     */
    public void onPanelActivated() {
        // 初始化Git服务
        gitService.initialize().thenRun(() -> {
            SwingUtilities.invokeLater(() -> {
                updateStatus();
                loadHistory();
            });
        });
    }
    
    /**
     * 更新Git状态
     */
    private void updateStatus() {
        if (gitService.isGitRepository()) {
            String branch = gitService.getCurrentBranch();
            statusLabel.setText(String.format("Git仓库 (分支: %s)", branch));
            statusLabel.setForeground(new Color(76, 175, 80)); // 绿色
            
            // 启用操作按钮
            analyzeChangesButton.setEnabled(true);
            saveSnapshotButton.setEnabled(true);
            viewHistoryButton.setEnabled(true);
        } else {
            statusLabel.setText("非Git仓库");
            statusLabel.setForeground(new Color(244, 67, 54)); // 红色
            
            // 禁用操作按钮
            analyzeChangesButton.setEnabled(false);
            saveSnapshotButton.setEnabled(false);
            viewHistoryButton.setEnabled(false);
        }
    }
    
    /**
     * 分析变更
     */
    private void analyzeChanges() {
        analyzeChangesButton.setEnabled(false);
        analyzeChangesButton.setText("分析中...");
        
        gitService.analyzeChangesForPins().thenAccept(suggestions -> {
            SwingUtilities.invokeLater(() -> {
                suggestionsModel.clear();
                for (PinSuggestion suggestion : suggestions) {
                    suggestionsModel.addElement(suggestion);
                }
                
                analyzeChangesButton.setEnabled(true);
                analyzeChangesButton.setText("分析变更");
                
                if (suggestions.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "未发现需要添加图钉的变更", "分析结果", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, String.format("发现 %d 个图钉建议", suggestions.size()), "分析结果", JOptionPane.INFORMATION_MESSAGE);
                }
            });
        });
    }
    
    /**
     * 保存快照
     */
    private void saveSnapshot() {
        String commitHash = gitService.getLatestCommitHash();
        String message = JOptionPane.showInputDialog(this, "请输入快照描述:", "保存快照", JOptionPane.QUESTION_MESSAGE);
        
        if (message != null && !message.trim().isEmpty()) {
            gitService.savePinSnapshot(commitHash, message.trim()).thenAccept(success -> {
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        JOptionPane.showMessageDialog(this, "快照保存成功", "保存快照", JOptionPane.INFORMATION_MESSAGE);
                        loadHistory();
                    } else {
                        JOptionPane.showMessageDialog(this, "快照保存失败", "保存快照", JOptionPane.ERROR_MESSAGE);
                    }
                });
            });
        }
    }
    
    /**
     * 加载历史
     */
    private void loadHistory() {
        gitService.getPinHistory().thenAccept(snapshots -> {
            SwingUtilities.invokeLater(() -> {
                historyModel.clear();
                for (PinSnapshot snapshot : snapshots) {
                    historyModel.addElement(snapshot);
                }
            });
        });
    }
    
    /**
     * 从建议创建图钉
     */
    private void createPinFromSuggestion() {
        PinSuggestion selected = suggestionsList.getSelectedValue();
        if (selected != null) {
            // 显示预览对话框
            String preview = pinCreator.getPreviewContent(selected);
            int result = JOptionPane.showConfirmDialog(this,
                "即将创建图钉:\n\n" + preview + "\n\n确定要创建吗？",
                "创建图钉", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                boolean success = pinCreator.createPinFromSuggestion(selected);
                if (success) {
                    JOptionPane.showMessageDialog(this, "图钉创建成功！", "创建图钉", JOptionPane.INFORMATION_MESSAGE);
                    // 从列表中移除已创建的建议
                    suggestionsModel.removeElement(selected);
                } else {
                    JOptionPane.showMessageDialog(this, "图钉创建失败，请检查文件是否存在", "创建图钉", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先选择一个建议", "创建图钉", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * 忽略建议
     */
    private void ignoreSuggestion() {
        PinSuggestion selected = suggestionsList.getSelectedValue();
        if (selected != null) {
            suggestionsModel.removeElement(selected);
        }
    }
    
    /**
     * 比较版本
     */
    private void compareVersions() {
        // 实现版本比较功能
        JOptionPane.showMessageDialog(this, "版本比较功能开发中...", "比较版本", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 恢复快照
     */
    private void restoreSnapshot() {
        // 实现快照恢复功能
        JOptionPane.showMessageDialog(this, "快照恢复功能开发中...", "恢复快照", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 建议列表渲染器
     */
    private static class SuggestionListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof PinSuggestion) {
                PinSuggestion suggestion = (PinSuggestion) value;
                setText(suggestion.getDisplayText());

                // 根据优先级设置颜色
                Color priorityColor;
                switch (suggestion.getPriority()) {
                    case CRITICAL:
                        priorityColor = new Color(244, 67, 54); // 红色
                        break;
                    case HIGH:
                        priorityColor = new Color(255, 152, 0); // 橙色
                        break;
                    case MEDIUM:
                        priorityColor = new Color(255, 193, 7); // 黄色
                        break;
                    case LOW:
                    default:
                        priorityColor = new Color(158, 158, 158); // 灰色
                        break;
                }

                if (!isSelected) {
                    setForeground(priorityColor);
                }
            }

            return this;
        }
    }

    /**
     * 历史列表渲染器
     */
    private static class HistoryListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof PinSnapshot) {
                PinSnapshot snapshot = (PinSnapshot) value;
                setText(snapshot.getDisplayText());

                // 设置图标
                setIcon(new ColorIcon(new Color(76, 175, 80), 8, 8)); // 绿色小圆点
            }

            return this;
        }
    }

    /**
     * 简单的颜色图标
     */
    private static class ColorIcon implements Icon {
        private final Color color;
        private final int width;
        private final int height;

        public ColorIcon(Color color, int width, int height) {
            this.color = color;
            this.width = width;
            this.height = height;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillOval(x, y, width, height);
        }

        @Override
        public int getIconWidth() { return width; }

        @Override
        public int getIconHeight() { return height; }
    }
}
