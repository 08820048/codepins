package cn.ilikexff.codepins;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
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
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PinsToolWindow implements ToolWindowFactory {

    private DefaultListModel<PinEntry> model;
    private List<PinEntry> allPins;
    private JList<PinEntry> list;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        model = new DefaultListModel<>();
        list = new JList<>(model);
        PinStorage.setModel(model);

        PinStorage.initFromSaved();
        allPins = PinStorage.getPins();

        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PinEntry entry) {
                    // 使用 ReadAction 包装文档访问操作，确保线程安全
                    String display = com.intellij.openapi.application.ReadAction.compute(() -> {
                        try {
                            int line = entry.getCurrentLine(entry.marker.getDocument());
                            String fileName = getFileName(entry.filePath);
                            String typeTag = entry.isBlock ? "<font color='#f78c6c'>[Block]</font>" : "<font color='#c3e88d'>[Line]</font>";
                            String notePart = (entry.note != null && !entry.note.isEmpty())
                                    ? " - <i><font color='#1ad320'>" + escapeHtml(entry.note) + "</font></i>" : "";

                            return "<html><body style='white-space:nowrap;'>"
                                    + "<b>" + fileName + "</b> "
                                    + "<font color='gray'>@ Line " + (line + 1) + "</font> "
                                    + typeTag + notePart + "</body></html>";
                        } catch (Exception e) {
                            // 如果发生异常，返回一个简化的显示
                            String fileName = getFileName(entry.filePath);
                            String typeTag = entry.isBlock ? "<font color='#f78c6c'>[Block]</font>" : "<font color='#c3e88d'>[Line]</font>";
                            String notePart = (entry.note != null && !entry.note.isEmpty())
                                    ? " - <i><font color='#1ad320'>" + escapeHtml(entry.note) + "</font></i>" : "";

                            return "<html><body style='white-space:nowrap;'>"
                                    + "<b>" + fileName + "</b> "
                                    + typeTag + notePart + "</body></html>";
                        }
                    });

                    label.setIcon(IconLoader.getIcon(entry.isBlock ? "/icons/code.svg" : "/icons/bookmark.svg", getClass()));
                    label.setText(display);

                    // 不再使用标准工具提示，而是使用自定义悬浮预览
                    // 清除原有工具提示
                    label.setToolTipText(null);
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

        // 添加鼠标监听器，处理双击导航和悬停预览
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

            @Override
            public void mouseExited(MouseEvent e) {
                // 鼠标离开列表时隐藏预览
                PinHoverPreview.hidePreview();
            }
        });

        // 添加鼠标移动监听器，处理悬停预览
        list.addMouseMotionListener(new MouseAdapter() {
            // 使用节流控制，减少鼠标移动事件的处理频率
            private long lastProcessTime = 0;
            private static final long THROTTLE_MS = 200; // 200毫秒节流

            @Override
            public void mouseMoved(MouseEvent e) {
                // 节流控制，减少鼠标移动事件的处理频率
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastProcessTime < THROTTLE_MS) {
                    return; // 如果距离上次处理时间小于节流时间，则跳过
                }
                lastProcessTime = currentTime;

                try {
                    // 获取鼠标位置的项
                    int index = list.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Rectangle cellBounds = list.getCellBounds(index, index);
                        if (cellBounds != null && cellBounds.contains(e.getPoint())) {
                            PinEntry entry = list.getModel().getElementAt(index);
                            if (entry != null) {
                                // 显示自定义悬浮预览
                                PinHoverPreview.showPreview(entry, project, list, e.getXOnScreen(), e.getYOnScreen() + 20);
                            }
                        }
                    } else {
                        // 鼠标不在任何项上，隐藏预览
                        PinHoverPreview.hidePreview();
                    }
                } catch (Exception ex) {
                    // 捕获并记录任何异常
                    System.out.println("[CodePins] 鼠标移动处理异常: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        // 使用 JPopupMenu.Listener 来动态创建菜单
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                // 获取鼠标位置的项
                int index = list.locationToIndex(e.getPoint());
                if (index >= 0) {
                    list.setSelectedIndex(index);
                    PinEntry selected = list.getSelectedValue();

                    // 创建菜单
                    JPopupMenu menu = new JPopupMenu();

                    // 加载图标
                    Icon codeIcon = IconLoader.getIcon("/icons/view.svg", getClass());
                    Icon editIcon = IconLoader.getIcon("/icons/edit.svg", getClass());
                    Icon deleteIcon = IconLoader.getIcon("/icons/trash.svg", getClass());
                    Icon refreshIcon = IconLoader.getIcon("/icons/refresh.svg", getClass());

                    // 根据图钉类型添加不同的菜单项
                    if (selected.isBlock) {
                        // 如果是代码块图钉，添加代码预览项
                        JMenuItem codeItem = new JMenuItem("查看代码块", codeIcon);
                        codeItem.addActionListener(event -> {
                            CodePreviewUtil.showPreviewPopup(project, selected);
                        });
                        menu.add(codeItem);
                    }

                    // 添加编辑项
                    JMenuItem editItem = new JMenuItem("修改备注", editIcon);
                    editItem.addActionListener(event -> {
                        String newNote = JOptionPane.showInputDialog(null, "请输入新的备注：", selected.note);
                        if (newNote != null) {
                            PinStorage.updateNote(selected, newNote.trim());
                        }
                    });
                    menu.add(editItem);

                    // 添加删除项
                    JMenuItem deleteItem = new JMenuItem("删除本钉", deleteIcon);
                    deleteItem.addActionListener(event -> {
                        PinStorage.removePin(selected);
                        allPins = PinStorage.getPins();
                    });
                    menu.add(deleteItem);

                    // 添加刷新项
                    JMenuItem refreshItem = new JMenuItem("刷新", refreshIcon);
                    refreshItem.addActionListener(event -> {
                        // 重新加载所有图钉
                        allPins = PinStorage.getPins();
                        model.clear();
                        for (PinEntry pin : allPins) {
                            model.addElement(pin);
                        }
                        list.repaint();
                    });
                    menu.add(refreshItem);

                    // 显示菜单
                    menu.show(list, e.getX(), e.getY());
                }
            }
        });

        JBScrollPane scrollPane = new JBScrollPane(list);
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

    // 已移除 createListPopupMenu 方法，改为使用 MouseAdapter 动态创建菜单
}