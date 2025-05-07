package cn.ilikexff.codepins;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 图钉侧边栏窗口，负责展示图钉列表与交互
 */
public class PinsToolWindow implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        DefaultListModel<PinEntry> model = new DefaultListModel<>();
        JList<PinEntry> list = new JList<>(model);
        PinStorage.setModel(model);

        // 加载持久化数据（初始化时）
        PinStorage.initFromSaved();

        // 鼠标双击跳转
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    PinEntry selected = list.getSelectedValue();
                    if (selected != null) {
                        selected.navigate(project);
                    }
                }
            }
        });

        // ✅ 添加右键菜单用于删除图钉
        list.setComponentPopupMenu(createListPopupMenu(list));

        JScrollPane scrollPane = new JScrollPane(list);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(scrollPane, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    /**
     * 创建右键菜单，用于删除选中的图钉
     */
    private JPopupMenu createListPopupMenu(JList<PinEntry> list) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("🗑 删除该图钉");

        deleteItem.addActionListener(e -> {
            PinEntry selected = list.getSelectedValue();
            if (selected != null) {
                PinStorage.removePin(selected);
            }
        });

        menu.add(deleteItem);
        return menu;
    }
}