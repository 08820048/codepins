package cn.ilikexff.codepins.ui;

import cn.ilikexff.codepins.i18n.CodePinsBundle;
import cn.ilikexff.codepins.utils.ImportExportUtil;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * 导入对话框
 * 用于导入图钉数据
 */
public class ImportDialog extends DialogWrapper {
    
    private final Project project;
    private JRadioButton mergeRadio;
    private JRadioButton replaceRadio;
    private JLabel filePathLabel;
    private File selectedFile;
    
    /**
     * 构造函数
     * 
     * @param project 当前项目
     */
    public ImportDialog(Project project) {
        super(project);
        this.project = project;
        
        setTitle(CodePinsBundle.message("import.dialog.title"));
        setSize(500, 300);
        init();
    }
    
    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(500, 300));
        mainPanel.setBorder(JBUI.Borders.empty(10));
        
        // 创建顶部说明面板
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()),
                JBUI.Borders.empty(0, 0, 10, 0)
        ));
        
        JLabel titleLabel = new JLabel(CodePinsBundle.message("import.dialog.header"));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel(CodePinsBundle.message("import.dialog.instruction"));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(descLabel);
        
        // 创建文件选择面板
        JPanel filePanel = new JPanel(new BorderLayout(10, 0));
        filePanel.setBorder(JBUI.Borders.empty(10, 0));
        filePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel fileLabel = new JLabel(CodePinsBundle.message("import.file.label") + ":");
        fileLabel.setFont(fileLabel.getFont().deriveFont(Font.BOLD));
        
        filePathLabel = new JLabel(CodePinsBundle.message("import.file.none"));
        filePathLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border()),
                JBUI.Borders.empty(5)
        ));
        
        JButton browseButton = new JButton(CodePinsBundle.message("import.file.select"));
        browseButton.addActionListener(e -> selectImportFile());
        
        filePanel.add(fileLabel, BorderLayout.NORTH);
        filePanel.add(filePathLabel, BorderLayout.CENTER);
        filePanel.add(browseButton, BorderLayout.EAST);
        
        // 创建选项面板
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(JBUI.Borders.empty(10, 0));
        optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // 导入选项
        JLabel importOptionsLabel = new JLabel(CodePinsBundle.message("import.options.label"));
        importOptionsLabel.setFont(importOptionsLabel.getFont().deriveFont(Font.BOLD));
        importOptionsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        mergeRadio = new JRadioButton(CodePinsBundle.message("import.mode.merge"));
        mergeRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        mergeRadio.setSelected(true);
        
        replaceRadio = new JRadioButton(CodePinsBundle.message("import.mode.replace"));
        replaceRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        ButtonGroup importGroup = new ButtonGroup();
        importGroup.add(mergeRadio);
        importGroup.add(replaceRadio);
        
        optionsPanel.add(importOptionsLabel);
        optionsPanel.add(Box.createVerticalStrut(5));
        optionsPanel.add(mergeRadio);
        optionsPanel.add(replaceRadio);
        
        // 创建警告面板
        JPanel warningPanel = new JPanel(new BorderLayout());
        warningPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border()),
                JBUI.Borders.empty(10, 0, 0, 0)
        ));
        
        JLabel warningLabel = new JLabel("<html><b>" + CodePinsBundle.message("import.warning") + "</b> " + CodePinsBundle.message("import.warning.desc") + "</html>");
        // 确保警告文本使用当前语言设置
        warningLabel.setForeground(JBColor.RED);
        warningPanel.add(warningLabel, BorderLayout.CENTER);
        
        // 组装主面板
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(filePanel, BorderLayout.CENTER);
        mainPanel.add(optionsPanel, BorderLayout.SOUTH);
        mainPanel.add(warningPanel, BorderLayout.PAGE_END);
        
        return mainPanel;
    }
    
    /**
     * 选择导入文件
     */
    private void selectImportFile() {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false)
                .withTitle(CodePinsBundle.message("import.file.chooser.title"))
                .withDescription(CodePinsBundle.message("import.file.chooser.desc"))
                .withFileFilter(file -> file.getExtension() != null && file.getExtension().equalsIgnoreCase("json"));
        
        VirtualFile[] files = FileChooser.chooseFiles(descriptor, project, null);
        if (files.length > 0) {
            selectedFile = new File(files[0].getPath());
            filePathLabel.setText(CodePinsBundle.message("import.file.selected", selectedFile.getAbsolutePath()));
        }
    }
    
    @Override
    protected void doOKAction() {
        if (selectedFile == null || !selectedFile.exists()) {
            Messages.showErrorDialog(
                    project,
                    "请选择有效的导入文件。",
                    "导入错误"
            );
            return;
        }
        
        ImportExportUtil.ImportMode mode = mergeRadio.isSelected() ?
                ImportExportUtil.ImportMode.MERGE : ImportExportUtil.ImportMode.REPLACE;
        
        int importCount = ImportExportUtil.importPins(project, selectedFile, mode);
        
        if (importCount >= 0) {
            Messages.showInfoMessage(
                    project,
                    CodePinsBundle.message("import.success", importCount),
                    CodePinsBundle.message("import.success.title")
            );
            super.doOKAction();
        }
    }
}
