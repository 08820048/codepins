package cn.ilikexff.codepins;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
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

        // 初始化数据
        PinStorage.initFromSaved();
        allPins = PinStorage.getPins();

        // ✅ 设置图钉列表的美化渲染器（基于 RangeMarker 获取最新行号）
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PinEntry entry) {
                    int line = entry.getCurrentLine(entry.marker.getDocument());
                    String display = "<html><body style='width:1000px; white-space:nowrap;'>"
                            + "<b>" + getFileName(entry.filePath) + "</b> "
                            + "<font color='gray'>@ Line " + (line + 1) + "</font>";

                    if (entry.note != null && !entry.note.isEmpty()) {
                        display += " - <i><font color='#1ad320'>" + escapeHtml(entry.note) + "</font></i>";
                    }

                    display += "</body></html>";
                    Icon icon = IconLoader.getIcon("/icons/logo.svg", getClass());
                    label.setIcon(icon);
                    label.setText(display);
                    label.setToolTipText(entry.filePath + (entry.note != null ? " · " + entry.note : ""));
                }
                return label;
            }

            private String getFileName(String path) {
                int slash = path.lastIndexOf('/');
                return slash >= 0 ? path.substring(slash + 1) : path;
            }

            private String escapeHtml(String s) {
                return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            }
        });

        // ✅ 双击跳转
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

        // ✅ 右键菜单（含“修改备注”和“删除”）
        list.setComponentPopupMenu(createListPopupMenu(list));

        // ✅ 滚动面板
        JBScrollPane scrollPane = new JBScrollPane(list);

        // ✅ 顶部搜索 + 清空按钮
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(createSearchField(), BorderLayout.CENTER);
        topPanel.add(createToolbar().getComponent(), BorderLayout.EAST);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        Content content = ContentFactory.getInstance().createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

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

    private ActionToolbar createToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();
        Icon clearIcon = IconLoader.getIcon("/icons/x-octagon.svg", getClass());
        group.add(new AnAction("清空图钉", "清除所有图钉记录", clearIcon) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(null,
                        "确定要清空所有图钉吗？", "确认清空", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    PinStorage.clearAll();
                    allPins = PinStorage.getPins();
                }
            }
        });

        return ActionManager.getInstance().createActionToolbar("CodePinsToolbar", group, true);
    }

    private JPopupMenu createListPopupMenu(JList<PinEntry> list) {
        JPopupMenu menu = new JPopupMenu();

        Icon editIcon = IconLoader.getIcon("/icons/edit.svg", getClass());
        JMenuItem editItem = new JMenuItem("修改备注", editIcon);
        editItem.addActionListener(e -> {
            PinEntry selected = list.getSelectedValue();
            if (selected != null) {
                String newNote = JOptionPane.showInputDialog(null, "请输入新的备注：", selected.note);
                if (newNote != null) {
                    PinStorage.updateNote(selected, newNote.trim());
                }
            }
        });

        Icon delIcon = IconLoader.getIcon("/icons/trash.svg", getClass());
        JMenuItem deleteItem = new JMenuItem("删除本钉", delIcon);
        deleteItem.addActionListener(e -> {
            PinEntry selected = list.getSelectedValue();
            if (selected != null) {
                PinStorage.removePin(selected);
                allPins = PinStorage.getPins();
            }
        });

        menu.add(editItem);
        menu.add(deleteItem);
        return menu;
    }
}
