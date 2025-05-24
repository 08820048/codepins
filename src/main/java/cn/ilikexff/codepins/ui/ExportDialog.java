package cn.ilikexff.codepins.ui;

import cn.ilikexff.codepins.PinEntry;
import cn.ilikexff.codepins.PinStorage;
import cn.ilikexff.codepins.utils.ImportExportUtil;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 导出对话框
 * 用于导出图钉数据
 */
public class ExportDialog extends DialogWrapper {

    private final Project project;
    private final List<PinEntry> allPins;
    private final DefaultListModel<PinEntry> pinsModel;
    private final JBList<PinEntry> pinsList;
    private JRadioButton exportAllRadio;
    private JRadioButton exportSelectedRadio;

    /**
     * 构造函数
     *
     * @param project 当前项目
     */
    public ExportDialog(Project project) {
        super(project);
        this.project = project;
        this.allPins = PinStorage.getPins();

        // 创建图钉列表模型
        pinsModel = new DefaultListModel<>();
        for (PinEntry pin : allPins) {
            pinsModel.addElement(pin);
        }

        // 创建图钉列表
        pinsList = new JBList<>(pinsModel);
        pinsList.setCellRenderer(new PinListCellRenderer());
        pinsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        setTitle("导出图钉");
        setSize(650, 600);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setPreferredSize(new Dimension(650, 600));
        mainPanel.setBorder(JBUI.Borders.empty(15));

        // 创建顶部说明面板
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()),
                JBUI.Borders.empty(0, 0, 15, 0)
        ));

        JLabel titleLabel = new JLabel("导出图钉数据");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel("<html>您可以选择导出全部图钉或仅导出选中的图钉。<br>" +
                "如需选择特定图钉，请选择'仅导出选中的图钉'选项，然后在下方列表中选择要导出的图钉。</html>");
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(descLabel);

        // 创建中间部分（选项和列表）
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));

        // 创建选项面板
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(JBUI.Borders.empty(0, 0, 5, 0));
        optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 导出选项
        JLabel exportOptionsLabel = new JLabel("导出选项：");
        exportOptionsLabel.setFont(exportOptionsLabel.getFont().deriveFont(Font.BOLD));
        exportOptionsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        exportAllRadio = new JRadioButton("导出所有图钉");
        exportAllRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        exportAllRadio.setSelected(true);

        exportSelectedRadio = new JRadioButton("仅导出选中的图钉");
        exportSelectedRadio.setAlignmentX(Component.LEFT_ALIGNMENT);

        ButtonGroup exportGroup = new ButtonGroup();
        exportGroup.add(exportAllRadio);
        exportGroup.add(exportSelectedRadio);

        optionsPanel.add(exportOptionsLabel);
        optionsPanel.add(Box.createVerticalStrut(5));
        optionsPanel.add(exportAllRadio);
        optionsPanel.add(exportSelectedRadio);

        centerPanel.add(optionsPanel, BorderLayout.NORTH);

        // 创建图钉列表面板
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(JBUI.Borders.empty(5, 0, 0, 0));

        JLabel pinsLabel = new JLabel("可用图钉：");
        pinsLabel.setFont(pinsLabel.getFont().deriveFont(Font.BOLD));

        JBScrollPane scrollPane = new JBScrollPane(pinsList);
        scrollPane.setPreferredSize(new Dimension(620, 350));
        scrollPane.setMinimumSize(new Dimension(620, 350));

        listPanel.add(pinsLabel, BorderLayout.NORTH);
        listPanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(listPanel, BorderLayout.CENTER);

        // 组装主面板
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 添加选择变化监听器
        exportAllRadio.addActionListener(e -> {
            pinsList.clearSelection();
            pinsList.setEnabled(false);
        });

        exportSelectedRadio.addActionListener(e -> {
            pinsList.setEnabled(true);
            if (pinsList.getSelectedIndices().length == 0) {
                pinsList.setSelectionInterval(0, 0);
            }
        });

        // 初始状态
        pinsList.setEnabled(false);

        return mainPanel;
    }

    @Override
    protected void doOKAction() {
        // 获取要导出的图钉
        List<PinEntry> pinsToExport;
        if (exportAllRadio.isSelected()) {
            pinsToExport = allPins;
        } else {
            pinsToExport = new ArrayList<>();
            for (int index : pinsList.getSelectedIndices()) {
                pinsToExport.add(pinsModel.getElementAt(index));
            }
        }

        // 如果没有图钉可导出，显示错误消息
        if (pinsToExport.isEmpty()) {
            Messages.showErrorDialog(
                    project,
                    "没有图钉可导出。请选择至少一个图钉进行导出。",
                    "导出错误"
            );
            return;
        }

        // 显示文件保存对话框
        FileSaverDescriptor descriptor = new FileSaverDescriptor("导出图钉", "选择保存位置", "json");

        FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project);
        VirtualFileWrapper wrapper = dialog.save((com.intellij.openapi.vfs.VirtualFile)null, "codepins_export.json");

        if (wrapper != null) {
            File file = wrapper.getFile();
            boolean success = ImportExportUtil.exportPins(project, file, pinsToExport);

            if (success) {
                Messages.showInfoMessage(
                        project,
                        "成功导出 " + pinsToExport.size() + " 个图钉到文件：\n" + file.getAbsolutePath(),
                        "导出成功"
                );
                super.doOKAction();
            }
        } else {
            // 用户取消了文件保存对话框
            return;
        }
    }
}
