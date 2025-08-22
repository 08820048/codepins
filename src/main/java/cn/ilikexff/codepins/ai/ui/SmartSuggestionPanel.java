package cn.ilikexff.codepins.ai.ui;

import cn.ilikexff.codepins.ai.SmartSuggestion;
import cn.ilikexff.codepins.ai.SmartSuggestionService;
import cn.ilikexff.codepins.ai.SmartAnalysisManager;
import cn.ilikexff.codepins.i18n.CodePinsBundle;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.*;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 智能建议面板
 * 显示AI分析生成的代码建议
 */
public class SmartSuggestionPanel extends JBPanel<SmartSuggestionPanel> {
    
    private final Project project;
    private final SmartSuggestionService suggestionService;
    private final SmartAnalysisManager analysisManager;
    private final DefaultListModel<SmartSuggestion> listModel;
    private final JBList<SmartSuggestion> suggestionList;
    private final JBLabel statusLabel;
    private final JBLabel detailLabel;
    private final JButton applyButton;
    private final JButton dismissButton;
    private final JButton refreshButton;
    
    private String currentFilePath;
    
    public SmartSuggestionPanel(Project project) {
        this.project = project;
        this.suggestionService = SmartSuggestionService.getInstance(project);
        this.analysisManager = SmartAnalysisManager.getInstance(project);
        this.listModel = new DefaultListModel<>();
        this.suggestionList = new JBList<>(listModel);
        this.statusLabel = new JBLabel();
        this.detailLabel = new JBLabel();
        this.applyButton = new JButton("应用建议");
        this.dismissButton = new JButton("忽略");
        this.refreshButton = new JButton("刷新分析");
        
        initializeUI();
        setupListeners();
        updateStatus();

        // 启动自动分析
        analysisManager.enableAutoAnalysis();
    }

    /**
     * 激活面板时调用
     */
    public void onPanelActivated() {
        // 获取当前文件并更新UI
        updateCurrentFile();
        // 分析当前文件
        analysisManager.analyzeCurrentFile();
    }

    /**
     * 更新当前文件
     */
    private void updateCurrentFile() {
        com.intellij.openapi.fileEditor.FileEditorManager editorManager =
            com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project);
        com.intellij.openapi.vfs.VirtualFile[] selectedFiles = editorManager.getSelectedFiles();

        if (selectedFiles.length > 0) {
            String filePath = selectedFiles[0].getPath();
            System.out.println("[SmartSuggestion] 当前文件: " + filePath);
            updateFile(filePath);
        } else {
            System.out.println("[SmartSuggestion] 没有打开的文件");
            updateFile(null);
        }
    }
    
    /**
     * 初始化UI
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(5));
        
        // 顶部状态栏
        JPanel statusPanel = createStatusPanel();
        
        // 中间建议列表
        JPanel listPanel = createListPanel();
        
        // 底部详情和操作
        JPanel bottomPanel = createBottomPanel();
        
        add(statusPanel, BorderLayout.NORTH);
        add(listPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 创建状态面板
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("智能建议"));
        
        statusLabel.setText("准备就绪");
        statusLabel.setForeground(JBColor.GRAY);
        
        refreshButton.setPreferredSize(new Dimension(80, 25));
        
        panel.add(statusLabel, BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 创建列表面板
     */
    private JPanel createListPanel() {
        JPanel panel = new JBPanel<>(new BorderLayout());
        
        // 设置列表
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setCellRenderer(new SuggestionListCellRenderer());
        
        JBScrollPane scrollPane = new JBScrollPane(suggestionList);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建底部面板
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JBPanel<>(new BorderLayout());
        
        // 详情面板
        JPanel detailPanel = new JBPanel<>(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("详细信息"));
        
        detailLabel.setVerticalAlignment(SwingConstants.TOP);
        detailLabel.setText("选择建议查看详情");
        
        JBScrollPane detailScrollPane = new JBScrollPane(detailLabel);
        detailScrollPane.setPreferredSize(new Dimension(0, 80));
        detailPanel.add(detailScrollPane, BorderLayout.CENTER);
        
        // 操作按钮面板
        JPanel buttonPanel = new JBPanel<>(new FlowLayout(FlowLayout.RIGHT));
        
        applyButton.setEnabled(false);
        dismissButton.setEnabled(false);
        
        buttonPanel.add(dismissButton);
        buttonPanel.add(applyButton);
        
        panel.add(detailPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 设置监听器
     */
    private void setupListeners() {
        // 列表选择监听器
        suggestionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelection();
            }
        });
        
        // 应用按钮
        applyButton.addActionListener(e -> applySuggestion());
        
        // 忽略按钮
        dismissButton.addActionListener(e -> dismissSuggestion());
        
        // 刷新按钮
        refreshButton.addActionListener(e -> refreshSuggestions());
        
        // 建议服务监听器
        suggestionService.addListener(new SmartSuggestionService.SmartSuggestionListener() {
            @Override
            public void onSuggestionsUpdated(String filePath, List<SmartSuggestion> suggestions) {
                System.out.println("[SmartSuggestionPanel] 收到建议更新: " + filePath + ", 建议数: " + suggestions.size());
                SwingUtilities.invokeLater(() -> {
                    // 更新当前文件路径并显示建议
                    currentFilePath = filePath;
                    System.out.println("[SmartSuggestionPanel] 更新当前文件为: " + currentFilePath);
                    updateSuggestions(suggestions);
                });
            }

            @Override
            public void onSuggestionApplied(SmartSuggestion suggestion) {
                SwingUtilities.invokeLater(() -> {
                    listModel.removeElement(suggestion);
                    updateStatus();
                });
            }
        });
    }
    
    /**
     * 更新文件建议
     */
    public void updateFile(String filePath) {
        this.currentFilePath = filePath;
        
        if (filePath != null) {
            List<SmartSuggestion> suggestions = suggestionService.getUnappliedSuggestions(filePath);
            updateSuggestions(suggestions);
        } else {
            listModel.clear();
            updateStatus();
        }
    }
    
    /**
     * 更新建议列表
     */
    private void updateSuggestions(List<SmartSuggestion> suggestions) {
        System.out.println("[SmartSuggestionPanel] 开始更新建议列表，建议数: " + suggestions.size());
        listModel.clear();

        int addedCount = 0;
        for (SmartSuggestion suggestion : suggestions) {
            if (!suggestion.isApplied()) {
                listModel.addElement(suggestion);
                addedCount++;
                System.out.println("[SmartSuggestionPanel] 添加建议: " + suggestion.getTitle());
            }
        }

        System.out.println("[SmartSuggestionPanel] 实际添加建议数: " + addedCount);
        updateStatus();
    }
    
    /**
     * 更新选择状态
     */
    private void updateSelection() {
        SmartSuggestion selected = suggestionList.getSelectedValue();
        
        if (selected != null) {
            detailLabel.setText("<html>" + selected.getDetailedInfo().replace("\n", "<br>") + "</html>");
            applyButton.setEnabled(selected.isApplicableForPin());
            dismissButton.setEnabled(true);
        } else {
            detailLabel.setText("选择建议查看详情");
            applyButton.setEnabled(false);
            dismissButton.setEnabled(false);
        }
    }
    
    /**
     * 更新状态
     */
    private void updateStatus() {
        int totalCount = listModel.getSize();
        int highPriorityCount = 0;
        
        for (int i = 0; i < listModel.getSize(); i++) {
            SmartSuggestion suggestion = listModel.getElementAt(i);
            if (suggestion.getPriority().getLevel() >= SmartSuggestion.Priority.HIGH.getLevel()) {
                highPriorityCount++;
            }
        }
        
        if (totalCount == 0) {
            statusLabel.setText("无建议");
            statusLabel.setForeground(JBColor.GRAY);
        } else {
            String text = String.format("共 %d 条建议", totalCount);
            if (highPriorityCount > 0) {
                text += String.format("，其中 %d 条高优先级", highPriorityCount);
            }
            statusLabel.setText(text);
            statusLabel.setForeground(highPriorityCount > 0 ? JBColor.RED : JBColor.BLUE);
        }
    }
    
    /**
     * 应用建议
     */
    private void applySuggestion() {
        SmartSuggestion selected = suggestionList.getSelectedValue();
        if (selected == null || !selected.isApplicableForPin()) {
            return;
        }
        
        // 创建应用建议的对话框
        SmartSuggestionApplyDialog dialog = new SmartSuggestionApplyDialog(project, selected);
        if (dialog.showAndGet()) {
            // 建议已应用，从列表中移除
            listModel.removeElement(selected);
            updateStatus();
        }
    }
    
    /**
     * 忽略建议
     */
    private void dismissSuggestion() {
        SmartSuggestion selected = suggestionList.getSelectedValue();
        if (selected == null) {
            return;
        }
        
        // 标记为已应用（实际上是忽略）
        suggestionService.markSuggestionApplied(selected.getId(), "dismissed");
        listModel.removeElement(selected);
        updateStatus();
    }
    
    /**
     * 刷新建议
     */
    private void refreshSuggestions() {
        if (currentFilePath != null) {
            // 触发重新分析
            refreshButton.setEnabled(false);
            refreshButton.setText("分析中...");

            // 在后台线程中执行分析
            SwingUtilities.invokeLater(() -> {
                // 触发当前文件的重新分析
                analysisManager.analyzeCurrentFile();

                // 延迟恢复按钮状态
                Timer timer = new Timer(2000, e -> {
                    refreshButton.setEnabled(true);
                    refreshButton.setText("刷新分析");
                });
                timer.setRepeats(false);
                timer.start();
            });
        }
    }
    
    /**
     * 建议列表单元格渲染器
     */
    private static class SuggestionListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof SmartSuggestion) {
                SmartSuggestion suggestion = (SmartSuggestion) value;
                
                // 设置显示文本
                setText(suggestion.getDisplayText());
                
                // 根据优先级设置颜色
                if (!isSelected) {
                    switch (suggestion.getPriority()) {
                        case CRITICAL:
                            setForeground(new Color(156, 39, 176)); // 紫色
                            break;
                        case HIGH:
                            setForeground(new Color(244, 67, 54)); // 红色
                            break;
                        case MEDIUM:
                            setForeground(new Color(255, 152, 0)); // 橙色
                            break;
                        case LOW:
                            setForeground(new Color(76, 175, 80)); // 绿色
                            break;
                    }
                }
                
                // 设置工具提示
                setToolTipText("<html><b>" + suggestion.getTitle() + "</b><br>" +
                              suggestion.getDescription() + "<br>" +
                              "置信度: " + String.format("%.1f%%", suggestion.getConfidence() * 100) +
                              "</html>");
            }
            
            return this;
        }
    }
}
