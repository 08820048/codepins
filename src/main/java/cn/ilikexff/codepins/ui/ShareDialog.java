package cn.ilikexff.codepins.ui;

import cn.ilikexff.codepins.PinEntry;
import cn.ilikexff.codepins.utils.ImageGenerator;
import cn.ilikexff.codepins.utils.SharingUtil;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 分享对话框
 * 用于选择分享格式和方式
 */
public class ShareDialog extends DialogWrapper {

    private final Project project;
    private final List<PinEntry> pins;

    private JRadioButton markdownRadio;
    private JRadioButton htmlRadio;
    private JRadioButton jsonRadio;
    private JRadioButton codeOnlyRadio;
    private JRadioButton imageRadio;

    private JRadioButton clipboardRadio;
    private JRadioButton fileRadio;

    private JCheckBox codeOnlyCheckBox;

    private JComboBox<ImageGenerator.Theme> themeComboBox;
    private JPanel themePanel;

    /**
     * 构造函数
     *
     * @param project 当前项目
     * @param pins 要分享的图钉列表
     */
    public ShareDialog(Project project, List<PinEntry> pins) {
        super(project);
        this.project = project;
        this.pins = new ArrayList<>(pins);

        setTitle("分享图钉");
        setSize(500, 300);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));

        // 创建格式选择面板
        JPanel formatPanel = new JPanel(new GridLayout(6, 1));
        formatPanel.setBorder(BorderFactory.createTitledBorder("分享格式"));

        markdownRadio = new JBRadioButton("Markdown");
        markdownRadio.setSelected(true);
        htmlRadio = new JBRadioButton("HTML");
        jsonRadio = new JBRadioButton("JSON");
        codeOnlyRadio = new JBRadioButton("仅代码");
        imageRadio = new JBRadioButton("图片");

        ButtonGroup formatGroup = new ButtonGroup();
        formatGroup.add(markdownRadio);
        formatGroup.add(htmlRadio);
        formatGroup.add(jsonRadio);
        formatGroup.add(codeOnlyRadio);
        formatGroup.add(imageRadio);

        formatPanel.add(new JBLabel("选择分享格式:"));
        formatPanel.add(markdownRadio);
        formatPanel.add(htmlRadio);
        formatPanel.add(jsonRadio);
        formatPanel.add(codeOnlyRadio);
        formatPanel.add(imageRadio);

        // 添加图片主题选择面板
        themePanel = new JPanel(new BorderLayout());
        themePanel.setBorder(BorderFactory.createTitledBorder("图片主题"));

        themeComboBox = new JComboBox<>(ImageGenerator.Theme.values());
        themeComboBox.setSelectedItem(ImageGenerator.Theme.DARK); // 默认选择暗色主题
        themePanel.add(new JBLabel("选择主题:"), BorderLayout.WEST);
        themePanel.add(themeComboBox, BorderLayout.CENTER);
        themePanel.setVisible(false); // 初始不可见

        // 添加监听器，当选择图片格式时显示主题选择面板
        imageRadio.addActionListener(e -> themePanel.setVisible(imageRadio.isSelected()));
        markdownRadio.addActionListener(e -> themePanel.setVisible(false));
        htmlRadio.addActionListener(e -> themePanel.setVisible(false));
        jsonRadio.addActionListener(e -> themePanel.setVisible(false));
        codeOnlyRadio.addActionListener(e -> themePanel.setVisible(false));

        // 创建分享方式面板
        JPanel methodPanel = new JPanel(new GridLayout(4, 1));
        methodPanel.setBorder(BorderFactory.createTitledBorder("分享方式"));

        clipboardRadio = new JBRadioButton("复制到剪贴板");
        clipboardRadio.setSelected(true);
        fileRadio = new JBRadioButton("导出到文件");

        ButtonGroup methodGroup = new ButtonGroup();
        methodGroup.add(clipboardRadio);
        methodGroup.add(fileRadio);

        methodPanel.add(new JBLabel("选择分享方式:"));
        methodPanel.add(clipboardRadio);
        methodPanel.add(fileRadio);

        // 添加"只分享代码"选项
        codeOnlyCheckBox = new JCheckBox("只分享代码内容（不包含元数据）");
        // 检查是否有代码块图钉
        boolean hasCodeBlock = false;
        for (PinEntry pin : pins) {
            if (pin.isBlock) {
                hasCodeBlock = true;
                break;
            }
        }
        codeOnlyCheckBox.setEnabled(hasCodeBlock);
        if (!hasCodeBlock) {
            codeOnlyCheckBox.setToolTipText("没有代码块图钉可供分享");
        }
        methodPanel.add(codeOnlyCheckBox);

        // 创建信息面板
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("分享信息"));

        JLabel infoLabel = new JBLabel("将分享 " + pins.size() + " 个图钉");
        infoPanel.add(infoLabel, BorderLayout.CENTER);

        // 添加到主面板
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(formatPanel, BorderLayout.CENTER);
        leftPanel.add(themePanel, BorderLayout.SOUTH);

        JPanel optionsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        optionsPanel.add(leftPanel);
        optionsPanel.add(methodPanel);

        panel.add(optionsPanel, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);

        return panel;
    }

    @Override
    protected void doOKAction() {
        // 获取选择的格式
        SharingUtil.SharingFormat format;
        if (markdownRadio.isSelected()) {
            format = SharingUtil.SharingFormat.MARKDOWN;
        } else if (htmlRadio.isSelected()) {
            format = SharingUtil.SharingFormat.HTML;
        } else if (jsonRadio.isSelected()) {
            format = SharingUtil.SharingFormat.JSON;
        } else if (imageRadio.isSelected()) {
            format = SharingUtil.SharingFormat.IMAGE;
        } else {
            format = SharingUtil.SharingFormat.CODE_ONLY;
        }

        // 获取是否只分享代码
        boolean codeOnly = codeOnlyCheckBox.isSelected();

        // 根据选择的方式执行分享操作
        if (clipboardRadio.isSelected()) {
            // 复制到剪贴板
            boolean success;
            if (format == SharingUtil.SharingFormat.IMAGE) {
                // 图片格式不支持复制到剪贴板，提示用户
                Messages.showInfoMessage(
                        project,
                        "图片格式不支持复制到剪贴板，请选择导出到文件",
                        "分享提示"
                );
                return;
            } else {
                success = SharingUtil.copyPinsToClipboard(project, pins, format, codeOnly);
            }
            if (success) {
                Messages.showInfoMessage(
                        project,
                        "已成功将 " + pins.size() + " 个图钉以 " + format.getDisplayName() + " 格式复制到剪贴板",
                        "分享成功"
                );
                super.doOKAction();
            }
        } else {
            // 导出到文件
            String extension;
            switch (format) {
                case MARKDOWN:
                    extension = "md";
                    break;
                case HTML:
                    extension = "html";
                    break;
                case JSON:
                    extension = "json";
                    break;
                case IMAGE:
                    extension = "png";
                    break;
                default:
                    extension = "txt";
            }

            // 显示文件保存对话框
            FileSaverDescriptor descriptor = new FileSaverDescriptor(
                    "分享图钉",
                    "选择保存位置",
                    extension
            );

            FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project);
            VirtualFileWrapper wrapper = dialog.save((com.intellij.openapi.vfs.VirtualFile)null, "codepins_share." + extension);

            if (wrapper != null) {
                File file = wrapper.getFile();
                boolean success;
                if (format == SharingUtil.SharingFormat.IMAGE) {
                    try {
                        // 获取选择的主题
                        ImageGenerator.Theme theme = (ImageGenerator.Theme) themeComboBox.getSelectedItem();

                        // 如果只有一个图钉，直接生成图片
                        if (pins.size() == 1) {
                            PinEntry pin = pins.get(0);
                            String code = SharingUtil.getCodeSnippet(project, pin);
                            String language = SharingUtil.getFileLanguage(pin.filePath);

                            // 生成图片
                            File imageFile = ImageGenerator.generateCodeCard(code, language, theme, 800);

                            // 复制生成的图片到目标文件
                            java.nio.file.Files.copy(
                                    imageFile.toPath(),
                                    file.toPath(),
                                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                            );

                            // 删除临时文件
                            imageFile.delete();

                            success = true;
                        } else {
                            // 如果有多个图钉，生成一个合并的图片
                            // 获取所有代码块
                            StringBuilder codeBuilder = new StringBuilder();
                            String commonLanguage = null;

                            for (PinEntry pin : pins) {
                                if (pin.isBlock) {
                                    String code = SharingUtil.getCodeSnippet(project, pin);
                                    if (code != null && !code.trim().isEmpty()) {
                                        codeBuilder.append(code).append("\n\n");

                                        // 记录第一个有效的语言
                                        if (commonLanguage == null) {
                                            commonLanguage = SharingUtil.getFileLanguage(pin.filePath);
                                        }
                                    }
                                }
                            }

                            // 如果没有有效的代码块，显示错误
                            if (codeBuilder.length() == 0) {
                                Messages.showErrorDialog(
                                        project,
                                        "没有可用的代码块可供分享",
                                        "分享错误"
                                );
                                return;
                            }

                            // 使用第一个有效的语言，如果没有，则使用通用语言
                            String language = commonLanguage != null ? commonLanguage : "text";

                            // 生成图片
                            File imageFile = ImageGenerator.generateCodeCard(codeBuilder.toString(), language, theme, 800);

                            // 复制生成的图片到目标文件
                            java.nio.file.Files.copy(
                                    imageFile.toPath(),
                                    file.toPath(),
                                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                            );

                            // 删除临时文件
                            imageFile.delete();

                            success = true;
                        }
                    } catch (Exception e) {
                        Messages.showErrorDialog(
                                project,
                                "生成图片失败: " + e.getMessage(),
                                "分享错误"
                        );
                        return;
                    }
                } else {
                    success = SharingUtil.exportPinsToFile(project, pins, file, format, codeOnly);
                }

                if (success) {
                    Messages.showInfoMessage(
                            project,
                            "成功将 " + pins.size() + " 个图钉以 " + format.getDisplayName() + " 格式导出到文件：\n" + file.getAbsolutePath(),
                            "分享成功"
                    );
                    super.doOKAction();
                }
            } else {
                // 用户取消了文件保存对话框
                return;
            }
        }
    }
}
