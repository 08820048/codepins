package cn.ilikexff.codepins;

import cn.ilikexff.codepins.settings.CodePinsSettings;
import cn.ilikexff.codepins.ui.AnimationUtil;
import cn.ilikexff.codepins.ui.EmptyStatePanel;
import cn.ilikexff.codepins.ui.ExportDialog;
import cn.ilikexff.codepins.ui.ImportDialog;
import cn.ilikexff.codepins.ui.PinListCellRenderer;
import cn.ilikexff.codepins.ui.SearchTextField;
import cn.ilikexff.codepins.ui.SimpleTagEditorDialog;
import cn.ilikexff.codepins.ui.TagFilterPanel;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PinsToolWindow implements ToolWindowFactory {

    private Project project;
    private DefaultListModel<PinEntry> model;
    private List<PinEntry> allPins;
    private JList<PinEntry> list;
    private final TagFilterPanel[] tagFilterPanelRef = new TagFilterPanel[1]; // 使用数组引用来解决前向引用问题

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.project = project;
        model = new DefaultListModel<>();
        list = new JList<>(model);
        PinStorage.setModel(model);

        PinStorage.initFromSaved();
        allPins = PinStorage.getPins();

        // 使用自定义的现代卡片式渲染器
        PinListCellRenderer cellRenderer = new PinListCellRenderer();
        list.setCellRenderer(cellRenderer);

        // 设置拖放功能
        setupDragAndDrop();

        // 添加鼠标移动监听器，实现悬停效果
        list.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Rectangle cellBounds = list.getCellBounds(index, index);
                    if (cellBounds != null && cellBounds.contains(e.getPoint())) {
                        cellRenderer.setHoverIndex(index);
                        list.repaint();
                    }
                }
            }
        });

        // 鼠标离开列表时清除悬停效果
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                cellRenderer.setHoverIndex(-1);
                list.repaint();
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
                                // 获取渲染器
                                PinListCellRenderer renderer = (PinListCellRenderer) list.getCellRenderer();

                                // 更新悬停索引
                                int oldHoverIndex = renderer.getHoverIndex();
                                if (oldHoverIndex != index) {
                                    // 如果悬停索引发生变化，添加动画效果
                                    renderer.setHoverIndex(index);

                                    // 获取单元格组件
                                    @SuppressWarnings("unchecked") // 添加注解来抑制警告
                                    Component cellComponent = list.getCellRenderer().getListCellRendererComponent(
                                            list, entry, index, false, false); // 这里的调用是安全的

                                    // 添加悬停动画效果
                                    AnimationUtil.hoverEffect(cellComponent);

                                    // 重绘列表
                                    list.repaint(cellBounds);
                                }

                                // 显示自定义悬浮预览
                                PinHoverPreview.showPreview(entry, project, list, e.getXOnScreen(), e.getYOnScreen() + 20);
                            }
                        }
                    } else {
                        // 鼠标不在任何项上，隐藏预览
                        PinHoverPreview.hidePreview();

                        // 重置悬停索引
                        PinListCellRenderer renderer = (PinListCellRenderer) list.getCellRenderer();
                        renderer.setHoverIndex(-1);
                        list.repaint();
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
                    Icon tagIcon = IconLoader.getIcon("/icons/tag.svg", getClass());
                    Icon deleteIcon = IconLoader.getIcon("/icons/trash.svg", getClass());
                    Icon refreshIcon = IconLoader.getIcon("/icons/refresh.svg", getClass());

                    // 根据图钉类型添加不同的菜单项
                    if (selected.isBlock) {
                        // 如果是代码块图钉，添加代码预览项
                        JMenuItem codeItem = new JMenuItem("查看代码块", codeIcon);
                        codeItem.addActionListener(event -> {
                            // 添加按钮动画效果
                            AnimationUtil.buttonClickEffect(codeItem);
                            CodePreviewUtil.showPreviewPopup(project, selected);
                        });
                        menu.add(codeItem);
                    }

                    // 添加编辑备注项
                    JMenuItem editItem = new JMenuItem("修改备注", editIcon);
                    editItem.addActionListener(event -> {
                        // 添加按钮动画效果
                        AnimationUtil.buttonClickEffect(editItem);
                        String newNote = JOptionPane.showInputDialog(null, "请输入新的备注：", selected.note);
                        if (newNote != null) {
                            PinStorage.updateNote(selected, newNote.trim());
                        }
                    });
                    menu.add(editItem);

                    // 添加编辑标签项
                    JMenuItem tagItem = new JMenuItem("编辑标签", tagIcon);
                    tagItem.addActionListener(event -> {
                        // 添加按钮动画效果
                        AnimationUtil.buttonClickEffect(tagItem);
                        SimpleTagEditorDialog dialog = new SimpleTagEditorDialog(project, selected);
                        if (dialog.showAndGet()) {
                            // 如果用户点击了确定，更新标签
                            PinStorage.updateTags(selected, dialog.getTags());
                        }
                    });
                    menu.add(tagItem);

                    // 添加删除项
                    JMenuItem deleteItem = new JMenuItem("删除本钉", deleteIcon);
                    deleteItem.addActionListener(event -> {
                        // 添加按钮动画效果
                        AnimationUtil.buttonClickEffect(deleteItem);

                        // 检查是否需要确认
                        boolean confirmDelete = CodePinsSettings.getInstance().confirmDelete;
                        boolean shouldDelete = true;

                        if (confirmDelete) {
                            int result = JOptionPane.showConfirmDialog(
                                    null,
                                    "确定要删除这个图钉吗？",
                                    "删除确认",
                                    JOptionPane.YES_NO_OPTION
                            );
                            shouldDelete = (result == JOptionPane.YES_OPTION);
                        }

                        if (shouldDelete) {
                            PinStorage.removePin(selected);
                            allPins = PinStorage.getPins();
                        }
                    });
                    menu.add(deleteItem);

                    // 添加刷新项
                    JMenuItem refreshItem = new JMenuItem("刷新", refreshIcon);
                    refreshItem.addActionListener(event -> {
                        // 添加按钮动画效果
                        AnimationUtil.buttonClickEffect(refreshItem);

                        // 重新加载所有图钉
                        allPins = PinStorage.getPins();
                        model.clear();
                        for (PinEntry pin : allPins) {
                            model.addElement(pin);
                        }
                        list.repaint();

                        // 刷新标签筛选面板
                        // 注意：标签面板在下面初始化，这里使用延迟执行
                        SwingUtilities.invokeLater(() -> {
                            if (tagFilterPanelRef[0] != null) {
                                tagFilterPanelRef[0].refreshTagsView();
                            }
                        });
                    });
                    menu.add(refreshItem);

                    // 显示菜单
                    menu.show(list, e.getX(), e.getY());
                }
            }
        });

        // 创建标签筛选面板
        tagFilterPanelRef[0] = new TagFilterPanel(selectedTags -> {
            // 当标签选择变化时，更新图钉列表
            updatePinsList(selectedTags);
        });

        // 创建空状态面板
        EmptyStatePanel emptyStatePanel = new EmptyStatePanel();

        // 创建卡片布局，用于切换显示列表或空状态
        CardLayout cardLayout = new CardLayout();
        JPanel contentPanel = new JPanel(cardLayout);

        // 创建列表面板（包含标签筛选和列表）
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(tagFilterPanelRef[0], BorderLayout.NORTH);

        JBScrollPane scrollPane = new JBScrollPane(list);
        listPanel.add(scrollPane, BorderLayout.CENTER);

        // 添加列表面板和空状态面板
        contentPanel.add(listPanel, "LIST");
        contentPanel.add(emptyStatePanel, "EMPTY");

        // 根据图钉数量显示适当的面板
        updateContentView(cardLayout, contentPanel);

        // 添加模型监听器，当图钉数量变化时更新视图
        model.addListDataListener(new javax.swing.event.ListDataListener() {
            @Override
            public void intervalAdded(javax.swing.event.ListDataEvent e) {
                updateContentView(cardLayout, contentPanel);
            }

            @Override
            public void intervalRemoved(javax.swing.event.ListDataEvent e) {
                updateContentView(cardLayout, contentPanel);
            }

            @Override
            public void contentsChanged(javax.swing.event.ListDataEvent e) {
                updateContentView(cardLayout, contentPanel);
            }
        });

        // 创建顶部面板（搜索和工具栏）
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(createSearchField(), BorderLayout.CENTER);
        topPanel.add(createToolbar().getComponent(), BorderLayout.EAST);

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 添加到工具窗口
        Content content = ContentFactory.getInstance().createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    /**
     * 创建现代化搜索框
     */
    private JComponent createSearchField() {
        // 创建现代化搜索框
        SearchTextField searchField = new SearchTextField("搜索图钉（支持备注与路径）");

        // 创建容器面板，添加边距
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(JBUI.Borders.empty(5, 8, 5, 8)); // 增加边距，使搜索框与其他元素保持适当距离
        container.setOpaque(false); // 透明背景，增加现代感
        container.add(searchField, BorderLayout.CENTER);

        searchField.addDocumentListener(new DocumentListener() {
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

        return container;
    }

    /**
     * 更新内容视图，根据图钉数量显示列表或空状态
     */
    private void updateContentView(CardLayout cardLayout, JPanel contentPanel) {
        if (model.isEmpty()) {
            cardLayout.show(contentPanel, "EMPTY");
        } else {
            cardLayout.show(contentPanel, "LIST");
        }
    }

    /**
     * 根据选中的标签更新图钉列表
     */
    private void updatePinsList(List<String> selectedTags) {
        model.clear();

        if (selectedTags == null || selectedTags.isEmpty()) {
            // 如果没有选中标签，显示所有图钉
            for (PinEntry pin : allPins) {
                model.addElement(pin);
            }
        } else {
            // 如果有选中标签，显示匹配的图钉
            List<PinEntry> filteredPins = PinStorage.filterByTags(selectedTags);
            for (PinEntry pin : filteredPins) {
                model.addElement(pin);
            }
        }
    }

    /**
     * 设置拖放功能
     */
    private void setupDragAndDrop() {
        // 创建自定义的拖放处理器
        list.setDragEnabled(true);
        list.setDropMode(DropMode.INSERT);

        // 获取渲染器实例
        @SuppressWarnings("unchecked") // 添加注解来抑制警告
        PinListCellRenderer cellRenderer = (PinListCellRenderer) list.getCellRenderer(); // 这里的转换是安全的

        // 创建自定义的传输处理器
        list.setTransferHandler(new TransferHandler() {
            private int dragIndex = -1;

            @Override
            @SuppressWarnings("unchecked") // 添加注解来抑制警告
            protected Transferable createTransferable(JComponent c) {
                JList<PinEntry> list = (JList<PinEntry>) c; // 这里的转换是安全的，因为我们知道c是一个 JList<PinEntry>
                dragIndex = list.getSelectedIndex();

                // 创建一个简单的 Transferable 对象，包含拖动的索引
                return new Transferable() {
                    @Override
                    public DataFlavor[] getTransferDataFlavors() {
                        return new DataFlavor[] { DataFlavor.stringFlavor };
                    }

                    @Override
                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return flavor.equals(DataFlavor.stringFlavor);
                    }

                    @Override
                    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                        if (flavor.equals(DataFlavor.stringFlavor)) {
                            return String.valueOf(dragIndex);
                        } else {
                            throw new UnsupportedFlavorException(flavor);
                        }
                    }
                };
            }

            @Override
            public int getSourceActions(JComponent c) {
                return MOVE;
            }

            @Override
            public boolean canImport(TransferSupport support) {
                if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    return false;
                }

                // 获取放置的位置，更新视觉反馈
                @SuppressWarnings("unchecked") // 添加注解来抑制警告
                JList.DropLocation dl = (JList.DropLocation) support.getDropLocation(); // 这里的转换是安全的
                int dropIndex = dl.getIndex();

                // 更新拖放目标的视觉效果
                cellRenderer.setDragOverIndex(dropIndex);
                list.repaint();

                return true;
            }

            @Override
            public boolean importData(TransferSupport support) {
                try {
                    // 获取拖动的索引
                    String dragIndexStr = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    int fromIndex = Integer.parseInt(dragIndexStr);

                    // 获取放置的位置
                    @SuppressWarnings("unchecked") // 添加注解来抑制警告
                    JList.DropLocation dl = (JList.DropLocation) support.getDropLocation(); // 这里的转换是安全的
                    int toIndex = dl.getIndex();

                    // 如果放置位置在拖动索引之后，需要调整
                    if (fromIndex < toIndex) {
                        toIndex--;
                    }

                    // 移动图钉位置
                    PinStorage.movePinPosition(fromIndex, toIndex);

                    // 选中移动后的项
                    list.setSelectedIndex(toIndex);

                    // 添加动画效果
                    AnimationUtil.scale(list, 1.0f, 0.98f, 100, () -> {
                        AnimationUtil.scale(list, 0.98f, 1.0f, 150);
                    });

                    return true;
                } catch (Exception e) {
                    System.out.println("[CodePins] 拖放失败: " + e.getMessage());
                    return false;
                }
            }

            @Override
            protected void exportDone(JComponent source, Transferable data, int action) {
                // 重置拖放目标的视觉效果
                cellRenderer.setDragOverIndex(-1);
                list.repaint();

                // 重置拖动索引
                dragIndex = -1;
            }
        });
    }

    /**
     * 创建工具栏
     */
    private ActionToolbar createToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        // 导出按钮
        Icon exportIcon = IconLoader.getIcon("/icons/folder-output.svg", getClass());
        group.add(new AnAction("导出图钉", "将图钉导出到文件", exportIcon) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                ExportDialog dialog = new ExportDialog(project);
                dialog.show();

                // 刷新标签筛选面板
                if (tagFilterPanelRef[0] != null) {
                    tagFilterPanelRef[0].refreshTagsView();
                }
            }
        });

        // 导入按钮
        Icon importIcon = IconLoader.getIcon("/icons/folder-input.svg", getClass());
        group.add(new AnAction("导入图钉", "从文件导入图钉", importIcon) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                ImportDialog dialog = new ImportDialog(project);
                if (dialog.showAndGet()) {
                    // 刷新图钉列表
                    allPins = PinStorage.getPins();
                    model.clear();
                    for (PinEntry pin : allPins) {
                        model.addElement(pin);
                    }

                    // 刷新标签筛选面板
                    if (tagFilterPanelRef[0] != null) {
                        tagFilterPanelRef[0].refreshTagsView();
                    }
                }
            }
        });

        // 清空按钮
        Icon clearIcon = IconLoader.getIcon("/icons/x-octagon.svg", getClass());
        group.add(new AnAction("清空图钉", "清除所有图钉记录", clearIcon) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(null,
                        "确定要清空所有图钉吗？", "确认清空", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    PinStorage.clearAll();
                    allPins = PinStorage.getPins();
                    model.clear();

                    // 刷新标签筛选面板
                    if (tagFilterPanelRef[0] != null) {
                        tagFilterPanelRef[0].refreshTagsView();
                    }
                }
            }
        });

        return ActionManager.getInstance().createActionToolbar("CodePinsToolbar", group, true);
    }

    // 已移除 createListPopupMenu 方法，改为使用 MouseAdapter 动态创建菜单
}