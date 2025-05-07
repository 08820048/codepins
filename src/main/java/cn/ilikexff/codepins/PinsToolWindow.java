package cn.ilikexff.codepins;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;

/**
 * 插件侧边栏窗口，用于展示图钉列表和清空操作
 */
public class PinsToolWindow implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        DefaultListModel<PinEntry> model = new DefaultListModel<>();
        JList<PinEntry> list = new JList<>(model);
        PinStorage.setModel(model);

        // 启动时加载持久化图钉
        PinStorage.initFromSaved();

        // 双击跳转到文件行
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

        // 右键菜单：删除单个图钉
        list.setComponentPopupMenu(createListPopupMenu(list));

        // 图钉列表 + 滚动容器
        JBScrollPane scrollPane = new JBScrollPane(list);

        // ✅ 工具栏按钮（目前仅添加：清空图钉）
        ActionToolbar toolbar = createToolbar();

        // 布局组件
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(toolbar.getComponent(), BorderLayout.NORTH); // 工具栏置顶
        panel.add(scrollPane, BorderLayout.CENTER);            // 列表居中显示

        // 注册到 ToolWindow
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    /**
     * 创建右键菜单：删除当前图钉
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

    /**
     * 创建顶部工具栏，添加“清空全部图钉”按钮
     */
    private ActionToolbar createToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(new AnAction("🧹 清空图钉", "清除所有图钉记录", null) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(null,
                        "确定要清空所有图钉吗？", "确认清空", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    PinStorage.clearAll();
                }
            }
        });

        return ActionManager.getInstance().createActionToolbar("CodePinsToolbar", group, true);
    }
}