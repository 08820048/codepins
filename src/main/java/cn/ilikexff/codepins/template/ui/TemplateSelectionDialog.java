package cn.ilikexff.codepins.template.ui;

import cn.ilikexff.codepins.i18n.CodePinsBundle;
import cn.ilikexff.codepins.template.PinTemplate;
import cn.ilikexff.codepins.template.TemplateStorage;
import cn.ilikexff.codepins.template.TemplateVariableProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.*;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 模板选择对话框
 * 用于快速选择和应用模板
 */
public class TemplateSelectionDialog extends DialogWrapper {
    
    private final TemplateStorage templateStorage;
    private final DefaultListModel<PinTemplate> listModel;
    private final JBList<PinTemplate> templateList;
    private final JBLabel previewLabel;
    private final JBTextField descriptionField;
    
    private PinTemplate selectedTemplate;
    
    public TemplateSelectionDialog(Project project) {
        super(project);
        this.templateStorage = TemplateStorage.getInstance();
        this.listModel = new DefaultListModel<>();
        this.templateList = new JBList<>(listModel);
        this.previewLabel = new JBLabel();
        this.descriptionField = new JBTextField();
        
        setTitle("选择模板");
        setSize(600, 500);
        
        initializeUI();
        loadTemplates();
        setupListeners();
        
        init();
    }
    
    /**
     * 初始化UI
     */
    private void initializeUI() {
        // 设置列表
        templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateList.setCellRenderer(new TemplateListCellRenderer());
        
        // 设置预览标签
        previewLabel.setVerticalAlignment(SwingConstants.TOP);
        previewLabel.setText("选择模板查看预览");
        
        // 设置描述输入框
        descriptionField.putClientProperty("JTextField.placeholderText", "输入描述信息（可选）");
    }
    
    /**
     * 创建中心面板
     */
    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JBPanel<>(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(580, 450));
        
        // 左侧：模板列表
        JPanel leftPanel = createTemplateListPanel();
        
        // 右侧：预览和描述
        JPanel rightPanel = createPreviewPanel();
        
        // 分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(280);
        splitPane.setResizeWeight(0.5);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    /**
     * 创建模板列表面板
     */
    private JPanel createTemplateListPanel() {
        JPanel panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("可用模板"));
        
        // 模板列表
        JBScrollPane scrollPane = new JBScrollPane(templateList);
        scrollPane.setPreferredSize(new Dimension(260, 350));
        
        // 分类标签
        JPanel categoryPanel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT));
        JBLabel categoryLabel = new JBLabel("双击模板快速应用");
        categoryLabel.setForeground(JBColor.GRAY);
        categoryPanel.add(categoryLabel);
        
        panel.add(categoryPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建预览面板
     */
    private JPanel createPreviewPanel() {
        JPanel panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("模板预览"));
        
        // 描述输入区域
        JPanel descPanel = new JBPanel<>(new BorderLayout());
        descPanel.setBorder(BorderFactory.createTitledBorder("描述信息"));
        descPanel.add(descriptionField, BorderLayout.CENTER);
        
        JBLabel descHint = new JBLabel("<html><small>如果模板包含 {description} 变量，请在此输入描述信息</small></html>");
        descHint.setForeground(JBColor.GRAY);
        descPanel.add(descHint, BorderLayout.SOUTH);
        
        // 预览区域
        JPanel previewPanel = new JBPanel<>(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("预览效果"));
        
        JBScrollPane previewScrollPane = new JBScrollPane(previewLabel);
        previewScrollPane.setPreferredSize(new Dimension(280, 200));
        previewPanel.add(previewScrollPane, BorderLayout.CENTER);
        
        panel.add(descPanel, BorderLayout.NORTH);
        panel.add(previewPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 设置监听器
     */
    private void setupListeners() {
        // 列表选择监听器
        templateList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updatePreview();
            }
        });
        
        // 双击应用模板
        templateList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    doOKAction();
                }
            }
        });
        
        // 描述输入监听器
        descriptionField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
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
        
        // 默认选择第一个模板
        if (!templates.isEmpty()) {
            templateList.setSelectedIndex(0);
        }
    }
    
    /**
     * 更新预览
     */
    private void updatePreview() {
        PinTemplate selected = templateList.getSelectedValue();
        if (selected == null) {
            previewLabel.setText("选择模板查看预览");
            return;
        }
        
        selectedTemplate = selected;
        
        // 处理模板内容
        String content = selected.getContent();
        if (content != null && !content.isEmpty()) {
            // 如果有描述输入，替换 {description} 变量
            String description = descriptionField.getText().trim();
            java.util.Map<String, String> customVars = new java.util.HashMap<>();
            if (!description.isEmpty()) {
                customVars.put("description", description);
            }
            
            String processed = TemplateVariableProcessor.processTemplate(content, customVars);
            
            // 显示预览
            StringBuilder preview = new StringBuilder();
            preview.append("<html>");
            preview.append("<div style='font-family: monospace; padding: 10px;'>");
            preview.append("<b>模板：</b>").append(selected.getName()).append("<br>");
            preview.append("<b>类型：</b>").append(selected.getType().getDisplayName()).append("<br>");
            if (!selected.getTags().isEmpty()) {
                preview.append("<b>标签：</b>").append(String.join(", ", selected.getTags())).append("<br>");
            }
            preview.append("<br>");
            preview.append("<b>生成内容：</b><br>");
            preview.append("<div style='background-color: #f5f5f5; padding: 5px; border: 1px solid #ccc;'>");
            preview.append(processed.replace("\n", "<br>"));
            preview.append("</div>");
            preview.append("</div>");
            preview.append("</html>");
            
            previewLabel.setText(preview.toString());
        } else {
            previewLabel.setText("模板内容为空");
        }
    }
    
    /**
     * 获取选中的模板
     */
    public PinTemplate getSelectedTemplate() {
        return selectedTemplate;
    }
    
    /**
     * 获取描述信息
     */
    public String getDescription() {
        return descriptionField.getText().trim();
    }
    
    /**
     * 检查是否有有效选择
     */
    @Override
    protected void doOKAction() {
        if (templateList.getSelectedValue() == null) {
            return;
        }
        selectedTemplate = templateList.getSelectedValue();
        super.doOKAction();
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
                
                // 设置显示文本
                setText(template.getDisplayName());
                
                // 内置模板使用不同颜色
                if (template.isBuiltIn()) {
                    setForeground(isSelected ? Color.WHITE : JBColor.BLUE);
                }
                
                // 设置工具提示
                setToolTipText("<html><b>" + template.getName() + "</b><br>" + 
                              template.getDescription() + "</html>");
            }
            
            return this;
        }
    }
}
