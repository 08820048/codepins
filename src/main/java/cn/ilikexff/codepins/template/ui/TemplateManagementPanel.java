package cn.ilikexff.codepins.template.ui;

import cn.ilikexff.codepins.i18n.CodePinsBundle;
import cn.ilikexff.codepins.template.PinTemplate;
import cn.ilikexff.codepins.template.TemplateStorage;
import cn.ilikexff.codepins.template.TemplateVariableProcessor;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.*;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 模板管理面板
 * 用于在设置页面中管理图钉模板
 */
public class TemplateManagementPanel extends JBPanel<TemplateManagementPanel> {
    
    private final TemplateStorage templateStorage;
    private final DefaultListModel<PinTemplate> listModel;
    private final JBList<PinTemplate> templateList;
    private final JBTextField nameField;
    private final JBTextField descriptionField;
    private final JBTextArea contentArea;
    private final JBTextField tagsField;
    private final JComboBox<PinTemplate.TemplateType> typeComboBox;
    private final JBLabel previewLabel;
    
    private PinTemplate currentTemplate;
    
    public TemplateManagementPanel() {
        this.templateStorage = TemplateStorage.getInstance();
        this.listModel = new DefaultListModel<>();
        this.templateList = new JBList<>(listModel);
        this.nameField = new JBTextField();
        this.descriptionField = new JBTextField();
        this.contentArea = new JBTextArea(5, 30);
        this.tagsField = new JBTextField();
        this.typeComboBox = new JComboBox<>(PinTemplate.TemplateType.values());
        this.previewLabel = new JBLabel();
        
        initializeUI();
        loadTemplates();
        setupListeners();
    }
    
    /**
     * 初始化UI
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));
        
        // 左侧：模板列表
        JPanel leftPanel = createTemplateListPanel();
        
        // 右侧：模板编辑
        JPanel rightPanel = createTemplateEditPanel();
        
        // 分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.4);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    /**
     * 创建模板列表面板
     */
    private JPanel createTemplateListPanel() {
        JPanel panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("模板列表"));
        
        // 模板列表
        templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateList.setCellRenderer(new TemplateListCellRenderer());
        JBScrollPane scrollPane = new JBScrollPane(templateList);
        scrollPane.setPreferredSize(new Dimension(280, 400));
        
        // 按钮面板
        JPanel buttonPanel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT));
        
        JButton newButton = new JButton("新建");
        newButton.addActionListener(e -> createNewTemplate());
        
        JButton deleteButton = new JButton("删除");
        deleteButton.addActionListener(e -> deleteSelectedTemplate());
        
        JButton duplicateButton = new JButton("复制");
        duplicateButton.addActionListener(e -> duplicateSelectedTemplate());
        
        buttonPanel.add(newButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(duplicateButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 创建模板编辑面板
     */
    private JPanel createTemplateEditPanel() {
        JPanel panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("模板编辑"));
        
        // 编辑表单
        JPanel formPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("名称:", nameField)
                .addLabeledComponent("描述:", descriptionField)
                .addLabeledComponent("类型:", typeComboBox)
                .addLabeledComponent("标签:", tagsField)
                .addLabeledComponent("内容:", new JBScrollPane(contentArea))
                .getPanel();
        
        // 预览面板
        JPanel previewPanel = new JBPanel<>(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("预览"));
        previewLabel.setText("选择模板查看预览");
        previewLabel.setVerticalAlignment(SwingConstants.TOP);
        previewPanel.add(new JBScrollPane(previewLabel), BorderLayout.CENTER);
        previewPanel.setPreferredSize(new Dimension(0, 100));
        
        // 按钮面板
        JPanel buttonPanel = new JBPanel<>(new FlowLayout(FlowLayout.RIGHT));
        
        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> saveCurrentTemplate());
        
        JButton resetButton = new JButton("重置");
        resetButton.addActionListener(e -> resetForm());
        
        JButton previewButton = new JButton("预览");
        previewButton.addActionListener(e -> updatePreview());
        
        buttonPanel.add(previewButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(saveButton);
        
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(previewPanel, BorderLayout.SOUTH);
        panel.add(buttonPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    /**
     * 设置监听器
     */
    private void setupListeners() {
        // 列表选择监听器
        templateList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                PinTemplate selected = templateList.getSelectedValue();
                if (selected != null) {
                    loadTemplateToForm(selected);
                }
            }
        });
        
        // 内容变化监听器
        contentArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updatePreview();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updatePreview();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updatePreview();
            }
        });
    }
    
    /**
     * 加载模板列表
     */
    private void loadTemplates() {
        listModel.clear();
        List<PinTemplate> templates = templateStorage.getAllTemplates();
        for (PinTemplate template : templates) {
            listModel.addElement(template);
        }
    }
    
    /**
     * 将模板加载到表单
     */
    private void loadTemplateToForm(PinTemplate template) {
        currentTemplate = template;
        
        nameField.setText(template.getName());
        descriptionField.setText(template.getDescription());
        contentArea.setText(template.getContent());
        tagsField.setText(String.join(", ", template.getTags()));
        typeComboBox.setSelectedItem(template.getType());
        
        // 内置模板不可编辑
        boolean editable = !template.isBuiltIn();
        nameField.setEditable(editable);
        descriptionField.setEditable(editable);
        contentArea.setEditable(editable);
        tagsField.setEditable(editable);
        typeComboBox.setEnabled(editable);
        
        updatePreview();
    }
    
    /**
     * 更新预览
     */
    private void updatePreview() {
        String content = contentArea.getText();
        if (content != null && !content.isEmpty()) {
            String processed = TemplateVariableProcessor.processTemplate(content);
            previewLabel.setText("<html><pre>" + processed.replace("\n", "<br>") + "</pre></html>");
        } else {
            previewLabel.setText("内容为空");
        }
    }
    
    /**
     * 创建新模板
     */
    private void createNewTemplate() {
        PinTemplate newTemplate = new PinTemplate();
        newTemplate.setName("新模板");
        newTemplate.setDescription("模板描述");
        newTemplate.setContent("模板内容");
        newTemplate.setType(PinTemplate.TemplateType.CUSTOM);
        
        if (templateStorage.addTemplate(newTemplate)) {
            loadTemplates();
            templateList.setSelectedValue(newTemplate, true);
        } else {
            Messages.showErrorDialog("创建模板失败", "错误");
        }
    }
    
    /**
     * 删除选中的模板
     */
    private void deleteSelectedTemplate() {
        PinTemplate selected = templateList.getSelectedValue();
        if (selected == null) {
            return;
        }
        
        if (selected.isBuiltIn()) {
            Messages.showWarningDialog("内置模板不能删除", "警告");
            return;
        }
        
        int result = Messages.showYesNoDialog(
                "确定要删除模板 \"" + selected.getName() + "\" 吗？",
                "确认删除",
                Messages.getQuestionIcon()
        );
        
        if (result == Messages.YES) {
            if (templateStorage.removeTemplate(selected.getId())) {
                loadTemplates();
                resetForm();
            } else {
                Messages.showErrorDialog("删除模板失败", "错误");
            }
        }
    }
    
    /**
     * 复制选中的模板
     */
    private void duplicateSelectedTemplate() {
        PinTemplate selected = templateList.getSelectedValue();
        if (selected == null) {
            return;
        }
        
        PinTemplate duplicate = new PinTemplate(selected);
        duplicate.setId(null); // 清空ID，让系统生成新的
        duplicate.setName(selected.getName() + " (副本)");
        duplicate.setBuiltIn(false); // 复制的模板不是内置的
        
        if (templateStorage.addTemplate(duplicate)) {
            loadTemplates();
            templateList.setSelectedValue(duplicate, true);
        } else {
            Messages.showErrorDialog("复制模板失败", "错误");
        }
    }
    
    /**
     * 保存当前模板
     */
    private void saveCurrentTemplate() {
        if (currentTemplate == null) {
            return;
        }
        
        if (currentTemplate.isBuiltIn()) {
            Messages.showWarningDialog("内置模板不能修改", "警告");
            return;
        }
        
        // 更新模板数据
        currentTemplate.setName(nameField.getText().trim());
        currentTemplate.setDescription(descriptionField.getText().trim());
        currentTemplate.setContent(contentArea.getText());
        currentTemplate.setType((PinTemplate.TemplateType) typeComboBox.getSelectedItem());
        
        // 解析标签
        String tagsText = tagsField.getText().trim();
        if (!tagsText.isEmpty()) {
            String[] tags = tagsText.split(",");
            currentTemplate.getTags().clear();
            for (String tag : tags) {
                String trimmedTag = tag.trim();
                if (!trimmedTag.isEmpty()) {
                    currentTemplate.addTag(trimmedTag);
                }
            }
        }
        
        if (templateStorage.updateTemplate(currentTemplate)) {
            loadTemplates();
            Messages.showInfoMessage("模板保存成功", "成功");
        } else {
            Messages.showErrorDialog("保存模板失败", "错误");
        }
    }
    
    /**
     * 重置表单
     */
    private void resetForm() {
        currentTemplate = null;
        nameField.setText("");
        descriptionField.setText("");
        contentArea.setText("");
        tagsField.setText("");
        typeComboBox.setSelectedItem(PinTemplate.TemplateType.CUSTOM);
        previewLabel.setText("选择模板查看预览");
        
        // 恢复可编辑状态
        nameField.setEditable(true);
        descriptionField.setEditable(true);
        contentArea.setEditable(true);
        tagsField.setEditable(true);
        typeComboBox.setEnabled(true);
    }
    
    /**
     * 模板列表单元格渲染器
     */
    private static class TemplateListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof PinTemplate) {
                PinTemplate template = (PinTemplate) value;
                setText(template.getDisplayName());
                
                if (template.isBuiltIn()) {
                    setForeground(isSelected ? Color.WHITE : JBColor.BLUE);
                }
            }
            
            return this;
        }
    }
}
