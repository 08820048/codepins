package cn.ilikexff.codepins;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * 插件侧边栏面板的工厂类，用于展示所有图钉信息，并支持点击跳转
 */
public class PinsToolWindow implements ToolWindowFactory {

    /**
     * 创建侧边栏 ToolWindow 的 UI 内容
     */
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        PinStorage.initFromSaved(); // 恢复历史图钉
        // 从 PinStorage 获取图钉数据
        List<PinEntry> pins = PinStorage.getPins();

        // 创建一个 Swing 列表模型，用于存储并展示图钉项
        DefaultListModel<PinEntry> model = new DefaultListModel<>();
        for (PinEntry pin : pins) {
            model.addElement(pin);
        }

        // 将模型注册到 PinStorage，便于后续添加图钉时能刷新 UI
        PinStorage.setModel(model);

        // 使用 JList 显示模型数据
        JList<PinEntry> pinList = new JList<>(model);
        pinList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 只允许单选

        // 添加双击事件监听器：双击某项时跳转对应代码位置
        pinList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = pinList.locationToIndex(e.getPoint()); // 获取点击项索引
                    if (index >= 0) {
                        PinEntry entry = model.get(index); // 获取对应的图钉对象
                        openFileAtLine(project, entry.filePath, entry.line); // 执行跳转
                    }
                }
            }
        });

        // 将 JList 放入滚动面板
        JScrollPane scrollPane = new JScrollPane(pinList);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        // 创建插件工具窗口内容并添加
        Content content = ContentFactory.getInstance().createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    /**
     * 执行跳转操作：打开指定文件并跳转到指定行号
     */
    private void openFileAtLine(Project project, String filePath, int line) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByIoFile(new File(filePath));
        if (file != null) {
            new OpenFileDescriptor(project, file, line, 0).navigate(true);
        } else {
            System.out.println("[CodePins] Failed to open file: " + filePath);
        }
    }
}