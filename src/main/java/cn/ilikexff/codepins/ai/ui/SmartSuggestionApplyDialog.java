package cn.ilikexff.codepins.ai.ui;

import cn.ilikexff.codepins.ai.SmartSuggestion;
import cn.ilikexff.codepins.ai.SmartSuggestionService;
import cn.ilikexff.codepins.core.PinEntry;
import cn.ilikexff.codepins.core.PinStorage;
import cn.ilikexff.codepins.template.PinTemplate;
import cn.ilikexff.codepins.template.TemplateVariableProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.*;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 智能建议应用对话框
 * 将AI建议转换为图钉
 */
public class SmartSuggestionApplyDialog extends DialogWrapper {
    
    private final Project project;
    private final SmartSuggestion suggestion;
    private final SmartSuggestionService suggestionService;
    
    private JBTextField titleField;
    private JBTextArea descriptionArea;
    private JBTextField tagsField;
    private JBLabel suggestionInfoLabel;
    private JBLabel previewLabel;
    
    public SmartSuggestionApplyDialog(Project project, SmartSuggestion suggestion) {
        super(project);
        this.project = project;
        this.suggestion = suggestion;
        this.suggestionService = SmartSuggestionService.getInstance(project);
        
        setTitle("应用智能建议");
        setSize(500, 400);
        
        init();
        initializeFields();
    }
    
    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JBPanel<>(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(480, 350));
        
        // 建议信息面板
        JPanel suggestionPanel = createSuggestionInfoPanel();
        
        // 图钉配置面板
        JPanel configPanel = createConfigPanel();
        
        // 预览面板
        JPanel previewPanel = createPreviewPanel();
        
        mainPanel.add(suggestionPanel, BorderLayout.NORTH);
        mainPanel.add(configPanel, BorderLayout.CENTER);
        mainPanel.add(previewPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    /**
     * 创建建议信息面板
     */
    private JPanel createSuggestionInfoPanel() {
        JPanel panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("建议信息"));
        panel.setPreferredSize(new Dimension(0, 80));
        
        suggestionInfoLabel = new JBLabel();
        suggestionInfoLabel.setVerticalAlignment(SwingConstants.TOP);
        
        JBScrollPane scrollPane = new JBScrollPane(suggestionInfoLabel);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建配置面板
     */
    private JPanel createConfigPanel() {
        JPanel panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("图钉配置"));
        
        // 表单面板
        JPanel formPanel = new JBPanel<>(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 标题
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JBLabel("标题:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        titleField = new JBTextField();
        formPanel.add(titleField, gbc);
        
        // 描述
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JBLabel("描述:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        descriptionArea = new JBTextArea(4, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JBScrollPane descScrollPane = new JBScrollPane(descriptionArea);
        formPanel.add(descScrollPane, gbc);
        
        // 标签
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        formPanel.add(new JBLabel("标签:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        tagsField = new JBTextField();
        tagsField.putClientProperty("JTextField.placeholderText", "用逗号分隔多个标签");
        formPanel.add(tagsField, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建预览面板
     */
    private JPanel createPreviewPanel() {
        JPanel panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("预览"));
        panel.setPreferredSize(new Dimension(0, 60));
        
        previewLabel = new JBLabel();
        previewLabel.setVerticalAlignment(SwingConstants.TOP);
        previewLabel.setForeground(JBColor.GRAY);
        
        JBScrollPane scrollPane = new JBScrollPane(previewLabel);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 初始化字段
     */
    private void initializeFields() {
        // 设置建议信息
        String suggestionInfo = String.format(
            "<html><b>类型:</b> %s<br>" +
            "<b>优先级:</b> %s<br>" +
            "<b>置信度:</b> %.1f%%<br>" +
            "<b>位置:</b> %s:%d<br>" +
            "<b>原因:</b> %s</html>",
            suggestion.getType().getDisplayName(),
            suggestion.getPriority().getDisplayName(),
            suggestion.getConfidence() * 100,
            getFileName(suggestion.getFilePath()),
            suggestion.getStartLine() + 1,
            suggestion.getReason() != null ? suggestion.getReason() : suggestion.getDescription()
        );
        suggestionInfoLabel.setText(suggestionInfo);
        
        // 设置默认值
        titleField.setText(suggestion.getTitle());
        descriptionArea.setText(suggestion.getDescription());
        
        // 设置默认标签
        String defaultTags = getDefaultTags();
        tagsField.setText(defaultTags);
        
        // 设置监听器
        setupFieldListeners();
        
        // 更新预览
        updatePreview();
    }
    
    /**
     * 设置字段监听器
     */
    private void setupFieldListeners() {
        // 标题变化监听器
        titleField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
        });
        
        // 描述变化监听器
        descriptionArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
        });
    }
    
    /**
     * 更新预览
     */
    private void updatePreview() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        
        if (title.isEmpty() && description.isEmpty()) {
            previewLabel.setText("输入标题和描述查看预览");
            return;
        }
        
        // 生成预览内容
        Map<String, String> variables = new HashMap<>();
        variables.put("title", title);
        variables.put("description", description);
        
        String template = title.isEmpty() ? description : (description.isEmpty() ? title : title + ": " + description);
        String processed = TemplateVariableProcessor.processTemplate(template, variables);
        
        previewLabel.setText("<html><b>图钉内容:</b><br>" + processed + "</html>");
    }
    
    /**
     * 获取默认标签
     */
    private String getDefaultTags() {
        StringBuilder tags = new StringBuilder();
        
        // 根据建议类型添加标签
        tags.append(suggestion.getType().getDisplayName());
        
        // 根据优先级添加标签
        if (suggestion.getPriority().getLevel() >= SmartSuggestion.Priority.HIGH.getLevel()) {
            tags.append(", ").append(suggestion.getPriority().getDisplayName());
        }
        
        // 添加AI标签
        tags.append(", AI建议");
        
        return tags.toString();
    }
    
    /**
     * 获取文件名
     */
    private String getFileName(String filePath) {
        if (filePath == null) return "未知文件";
        int lastSlash = filePath.lastIndexOf('/');
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }
    
    @Override
    protected void doOKAction() {
        if (applyToPin()) {
            super.doOKAction();
        }
    }
    
    /**
     * 应用到图钉
     */
    private boolean applyToPin() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String tagsText = tagsField.getText().trim();
        
        if (title.isEmpty() && description.isEmpty()) {
            showErrorMessage("请输入标题或描述");
            return false;
        }
        
        try {
            // 获取文件和文档
            VirtualFile file = getVirtualFile(suggestion.getFilePath());
            if (file == null) {
                showErrorMessage("无法找到文件: " + suggestion.getFilePath());
                return false;
            }
            
            Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document == null) {
                showErrorMessage("无法获取文档");
                return false;
            }
            
            // 创建范围标记
            int startOffset = document.getLineStartOffset(suggestion.getStartLine());
            int endOffset = suggestion.getEndLine() < document.getLineCount() - 1 ? 
                           document.getLineStartOffset(suggestion.getEndLine() + 1) - 1 :
                           document.getTextLength();
            
            RangeMarker marker = document.createRangeMarker(startOffset, endOffset);
            marker.setGreedyToLeft(true);
            marker.setGreedyToRight(true);
            
            // 生成图钉内容
            String pinContent = title.isEmpty() ? description : (description.isEmpty() ? title : title + ": " + description);
            
            // 解析标签
            java.util.List<String> tags = tagsText.isEmpty() ? 
                java.util.Collections.emptyList() : 
                Arrays.asList(tagsText.split(",\\s*"));
            
            // 创建图钉
            PinEntry pin = new PinEntry(
                suggestion.getFilePath(),
                marker,
                pinContent,
                System.currentTimeMillis(),
                System.getProperty("user.name"),
                suggestion.getStartLine() != suggestion.getEndLine(), // 多行图钉
                tags
            );
            
            // 保存图钉
            if (PinStorage.addPin(pin)) {
                // 标记建议为已应用
                suggestionService.markSuggestionApplied(suggestion.getId(), pin.getId());
                
                // 显示成功消息
                showSuccessMessage("已成功创建图钉");
                return true;
            } else {
                showErrorMessage("创建图钉失败");
                return false;
            }
            
        } catch (Exception e) {
            showErrorMessage("应用建议时发生错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取虚拟文件
     */
    private VirtualFile getVirtualFile(String filePath) {
        return com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath(filePath);
    }
    
    /**
     * 显示错误消息
     */
    private void showErrorMessage(String message) {
        com.intellij.openapi.ui.Messages.showErrorDialog(project, message, "错误");
    }
    
    /**
     * 显示成功消息
     */
    private void showSuccessMessage(String message) {
        com.intellij.notification.Notifications.Bus.notify(
            new com.intellij.notification.Notification(
                "CodePins",
                "智能建议",
                message,
                com.intellij.notification.NotificationType.INFORMATION
            ),
            project
        );
    }
}
