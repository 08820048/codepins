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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class PinsToolWindow implements ToolWindowFactory {

    private DefaultListModel<PinEntry> model;
    private List<PinEntry> allPins; // 原始数据用于过滤
    private JList<PinEntry> list;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        model = new DefaultListModel<>();
        list = new JList<>(model);
        PinStorage.setModel(model);

        // 初始化加载数据
        PinStorage.initFromSaved();
        allPins = PinStorage.getPins();

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

        list.setComponentPopupMenu(createListPopupMenu(list));

        JBScrollPane scrollPane = new JBScrollPane(list);

        // ✅ 顶部组件：搜索输入框 + 清空按钮工具栏
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(createSearchField(), BorderLayout.CENTER);
        topPanel.add(createToolbar().getComponent(), BorderLayout.EAST);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        Content content = ContentFactory.getInstance().createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    /**
     * 创建搜索输入框，支持备注和路径模糊匹配
     */
    private JTextField createSearchField() {
        JTextField searchField = new JTextField();
        searchField.setToolTipText("搜索图钉（支持备注与路径）");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            void filter() {
                String keyword = searchField.getText().trim().toLowerCase();
                model.clear();

                List<PinEntry> filtered = allPins.stream()
                        .filter(p -> p.filePath.toLowerCase().contains(keyword) ||
                                (p.note != null && p.note.toLowerCase().contains(keyword)))
                        .collect(Collectors.toList());

                for (PinEntry pin : filtered) {
                    model.addElement(pin);
                }
            }

            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });

        return searchField;
    }

    /**
     * 创建清空按钮工具栏
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
                    allPins = PinStorage.getPins(); // 同步原始数据
                }
            }
        });

        return ActionManager.getInstance().createActionToolbar("CodePinsToolbar", group, true);
    }

    /**
     * 创建图钉右键菜单：编辑备注、删除
     */
    private JPopupMenu createListPopupMenu(JList<PinEntry> list) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("✏️ 修改备注");
        editItem.addActionListener(e -> {
            PinEntry selected = list.getSelectedValue();
            if (selected != null) {
                String newNote = JOptionPane.showInputDialog(null, "请输入新的备注：", selected.note);
                if (newNote != null) {
                    PinStorage.updateNote(selected, newNote.trim());
                }
            }
        });

        JMenuItem deleteItem = new JMenuItem("🗑 删除该图钉");
        deleteItem.addActionListener(e -> {
            PinEntry selected = list.getSelectedValue();
            if (selected != null) {
                PinStorage.removePin(selected);
                allPins = PinStorage.getPins(); // 同步源数据
            }
        });

        menu.add(editItem);
        menu.add(deleteItem);
        return menu;
    }
}