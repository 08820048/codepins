package cn.ilikexff.codepins;

import cn.ilikexff.codepins.settings.CodePinsSettings;
import cn.ilikexff.codepins.ui.AnimationUtil;
import cn.ilikexff.codepins.ui.EmptyStatePanel;
import cn.ilikexff.codepins.ui.ExportDialog;
import cn.ilikexff.codepins.ui.ImportDialog;
import cn.ilikexff.codepins.ui.PinListCellRenderer;
import cn.ilikexff.codepins.ui.SearchTextField;
import cn.ilikexff.codepins.ui.ShareDialog;
import cn.ilikexff.codepins.ui.SimpleTagEditorDialog;
import cn.ilikexff.codepins.ui.TagFilterPanel;
import cn.ilikexff.codepins.utils.IconUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

        // 设置多选模式，允许批量操作
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

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

                    // 设置菜单样式
                    menu.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new JBColor(new Color(200, 200, 200, 100), new Color(60, 60, 60, 150)), 1),
                            BorderFactory.createEmptyBorder(2, 2, 2, 2)
                    ));

                    // 加载图标
                    Icon codeIcon = IconUtil.loadIcon("/icons/view.svg", getClass());
                    Icon editIcon = IconUtil.loadIcon("/icons/edit.svg", getClass());
                    Icon tagIcon = IconUtil.loadIcon("/icons/tag.svg", getClass());
                    Icon shareIcon = IconUtil.loadIcon("/icons/share.svg", getClass());
                    Icon deleteIcon = IconUtil.loadIcon("/icons/trash.svg", getClass());
                    Icon refreshIcon = IconUtil.loadIcon("/icons/refresh.svg", getClass());

                    // 根据图钉类型添加不同的菜单项
                    if (selected.isBlock) {
                        // 如果是代码块图钉，添加代码预览项
                        JMenuItem codeItem = new JMenuItem("查看代码块", codeIcon);
                        // 应用自定义UI
                        cn.ilikexff.codepins.ui.CustomMenuItemUI.apply(codeItem);
                        codeItem.addActionListener(event -> {
                            // 添加按钮动画效果
                            AnimationUtil.buttonClickEffect(codeItem);
                            CodePreviewUtil.showPreviewPopup(project, selected);
                        });
                        menu.add(codeItem);
                    }

                    // 添加编辑备注项
                    JMenuItem editItem = new JMenuItem("修改备注", editIcon);
                    // 应用自定义UI
                    cn.ilikexff.codepins.ui.CustomMenuItemUI.apply(editItem);
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
                    // 应用自定义UI
                    cn.ilikexff.codepins.ui.CustomMenuItemUI.apply(tagItem);
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

                    // 添加复制图钉项
                    Icon copyIcon = IconUtil.loadIcon("/icons/copy.svg", getClass());
                    JMenuItem copyItem = new JMenuItem("复制图钉", copyIcon);
                    // 应用自定义UI
                    cn.ilikexff.codepins.ui.CustomMenuItemUI.apply(copyItem);
                    copyItem.addActionListener(event -> {
                        // 添加按钮动画效果
                        AnimationUtil.buttonClickEffect(copyItem);

                        // 复制图钉
                        copyPin(selected);
                    });
                    menu.add(copyItem);

                    // 添加分享项
                    JMenuItem shareItem = new JMenuItem("分享图钉", shareIcon);
                    // 应用自定义UI
                    cn.ilikexff.codepins.ui.CustomMenuItemUI.apply(shareItem);
                    shareItem.addActionListener(event -> {
                        // 添加按钮动画效果
                        AnimationUtil.buttonClickEffect(shareItem);

                        // 创建分享对话框
                        List<PinEntry> pinsToShare = new ArrayList<>();
                        pinsToShare.add(selected);
                        ShareDialog dialog = new ShareDialog(project, pinsToShare);
                        dialog.show();
                    });
                    menu.add(shareItem);

                    // 添加删除项
                    JMenuItem deleteItem = new JMenuItem("删除本钉", deleteIcon);
                    // 应用自定义UI
                    cn.ilikexff.codepins.ui.CustomMenuItemUI.apply(deleteItem);
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
                    // 应用自定义UI
                    cn.ilikexff.codepins.ui.CustomMenuItemUI.apply(refreshItem);
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
        JPanel topPanel = new JPanel(new BorderLayout(8, 0));
        topPanel.setBorder(JBUI.Borders.empty(4, 4, 4, 4));

        // 创建搜索面板，包含搜索框
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        searchPanel.add(createSearchField(), BorderLayout.CENTER);

        // 创建右侧面板，包含图钉计数和工具栏
        JPanel rightPanel = new JPanel(new BorderLayout(8, 0));
        rightPanel.setOpaque(false);

        // 添加图钉计数标签
        rightPanel.add(createPinCountLabel(), BorderLayout.WEST);

        // 添加工具栏
        rightPanel.add(createToolbar().getComponent(), BorderLayout.EAST);

        // 添加到顶部面板
        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(rightPanel, BorderLayout.EAST);

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
        Icon exportIcon = IconUtil.loadIcon("/icons/folder-output.svg", getClass());
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
        Icon importIcon = IconUtil.loadIcon("/icons/folder-input.svg", getClass());
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

        // 分享按钮
        Icon shareIcon = IconUtil.loadIcon("/icons/share.svg", getClass());
        group.add(new AnAction("分享图钉", "分享选中的图钉", shareIcon) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                List<PinEntry> selectedPins = list.getSelectedValuesList();
                if (selectedPins.isEmpty()) {
                    // 如果没有选中的图钉，提示用户
                    Messages.showInfoMessage(
                            project,
                            "请先选择要分享的图钉",
                            "分享图钉"
                    );
                    return;
                }

                // 创建分享对话框
                ShareDialog dialog = new ShareDialog(project, selectedPins);
                dialog.show();
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                // 只有在有图钉时才启用此操作
                e.getPresentation().setEnabled(!model.isEmpty());
            }
        });

        // 批量删除按钮
        Icon deleteMultipleIcon = IconUtil.loadIcon("/icons/delete-multiple.svg", getClass());
        group.add(new AnAction("批量删除", "删除选中的图钉", deleteMultipleIcon) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                List<PinEntry> selectedPins = list.getSelectedValuesList();
                if (selectedPins.isEmpty()) {
                    Messages.showInfoMessage(
                            project,
                            "请先选择要删除的图钉",
                            "批量删除"
                    );
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(null,
                        "确定要删除选中的 " + selectedPins.size() + " 个图钉吗？",
                        "确认删除", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    deleteSelectedPins(selectedPins);
                }
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                // 只有在有图钉时才启用此操作
                e.getPresentation().setEnabled(!model.isEmpty());
            }
        });

        // 排序按钮
        Icon sortIcon = IconUtil.loadIcon("/icons/sort.svg", getClass());
        DefaultActionGroup sortGroup = new DefaultActionGroup("排序", true);
        sortGroup.getTemplatePresentation().setIcon(sortIcon);
        sortGroup.getTemplatePresentation().setText("排序图钉");
        sortGroup.getTemplatePresentation().setDescription("按不同条件排序图钉");

        // 添加排序选项
        sortGroup.add(new AnAction("按创建时间（新→旧）") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                sortPins(SortType.TIME_DESC);
            }
        });

        sortGroup.add(new AnAction("按创建时间（旧→新）") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                sortPins(SortType.TIME_ASC);
            }
        });

        sortGroup.add(new AnAction("按文件名") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                sortPins(SortType.FILENAME);
            }
        });

        sortGroup.add(new AnAction("按备注") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                sortPins(SortType.NOTE);
            }
        });

        group.add(sortGroup);

        // 清空按钮
        Icon clearIcon = IconUtil.loadIcon("/icons/x-octagon.svg", getClass());
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

    /**
     * 创建图钉计数标签
     */
    private JComponent createPinCountLabel() {
        // 获取图钉数量信息
        Map<String, Integer> countInfo = PinStorage.getPinsCountInfo();
        int currentCount = countInfo.get("current");
        int maxCount = countInfo.get("max");

        // 创建标签
        JLabel countLabel = new JLabel();

        // 获取当前UI主题颜色
        Color textColor = UIUtil.getLabelForeground();
        Color hoverColor = JBColor.namedColor("Link.activeForeground", new JBColor(new Color(0x4083C9), new Color(0x589DF6)));
        Color warningColor = JBColor.namedColor("Component.warningForeground", new JBColor(new Color(0xA0522D), new Color(0xBC6D4C)));
        Color errorColor = JBColor.namedColor("Component.errorForeground", new JBColor(new Color(0xC75450), new Color(0xFF5261)));
        Color successColor = JBColor.namedColor("Plugins.tagForeground", new JBColor(new Color(0x008000), new Color(0x369E6A)));

        // 设置标签文本和样式
        if (maxCount == -1) {
            // 专业版用户，无限制
            countLabel.setText(currentCount + " 图钉");
            countLabel.setForeground(successColor);
            countLabel.setIcon(IconUtil.loadIcon("/icons/premium-small.svg", getClass()));
        } else {
            // 免费版用户，有限制
            countLabel.setText(currentCount + "/" + maxCount);
            countLabel.setIcon(IconUtil.loadIcon("/icons/pin-small.svg", getClass()));

            // 根据使用比例设置颜色
            float usageRatio = (float) currentCount / maxCount;
            if (usageRatio >= 0.9) {
                countLabel.setForeground(errorColor);
            } else if (usageRatio >= 0.7) {
                countLabel.setForeground(warningColor);
            } else {
                countLabel.setForeground(textColor);
            }
        }

        // 设置字体和边距
        countLabel.setFont(countLabel.getFont().deriveFont(Font.PLAIN, 12f));
        countLabel.setBorder(JBUI.Borders.empty(0, 8, 0, 4));
        countLabel.setIconTextGap(4);

        // 添加鼠标点击事件，点击时显示升级对话框
        if (maxCount != -1) {
            countLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            countLabel.setToolTipText("免费版限制100个图钉，点击升级到专业版获取无限图钉");

            countLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // 显示升级对话框
                    cn.ilikexff.codepins.services.LicenseService.getInstance().showUpgradeDialogIfNeeded(project, "无限图钉");
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    countLabel.setForeground(hoverColor);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    // 恢复原来的颜色
                    float usageRatio = (float) currentCount / maxCount;
                    if (usageRatio >= 0.9) {
                        countLabel.setForeground(errorColor);
                    } else if (usageRatio >= 0.7) {
                        countLabel.setForeground(warningColor);
                    } else {
                        countLabel.setForeground(textColor);
                    }
                }
            });
        }

        // 创建容器面板，使用半透明背景
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(countLabel, BorderLayout.CENTER);

        return container;
    }

    // 已移除 createListPopupMenu 方法，改为使用 MouseAdapter 动态创建菜单

    /**
     * 排序类型枚举
     */
    private enum SortType {
        TIME_DESC,  // 按创建时间降序（新→旧）
        TIME_ASC,   // 按创建时间升序（旧→新）
        FILENAME,   // 按文件名
        NOTE        // 按备注
    }

    /**
     * 对图钉列表进行排序
     *
     * @param type 排序类型
     */
    private void sortPins(SortType type) {
        if (allPins.isEmpty()) {
            return;
        }

        List<PinEntry> sortedPins = new ArrayList<>(allPins);

        switch (type) {
            case TIME_DESC:
                // 按创建时间降序（新→旧）
                sortedPins.sort((p1, p2) -> Long.compare(p2.timestamp, p1.timestamp));
                break;
            case TIME_ASC:
                // 按创建时间升序（旧→新）
                sortedPins.sort(Comparator.comparingLong(p -> p.timestamp));
                break;
            case FILENAME:
                // 按文件名
                sortedPins.sort((p1, p2) -> {
                    String fileName1 = new File(p1.filePath).getName();
                    String fileName2 = new File(p2.filePath).getName();
                    return fileName1.compareToIgnoreCase(fileName2);
                });
                break;
            case NOTE:
                // 按备注
                sortedPins.sort(Comparator.comparing(p -> p.note, String.CASE_INSENSITIVE_ORDER));
                break;
        }

        // 更新存储和模型
        PinStorage.updatePinsOrder(sortedPins);
        updateListModel();

        // 显示排序成功消息
        String sortTypeText = "";
        switch (type) {
            case TIME_DESC:
                sortTypeText = "创建时间（新→旧）";
                break;
            case TIME_ASC:
                sortTypeText = "创建时间（旧→新）";
                break;
            case FILENAME:
                sortTypeText = "文件名";
                break;
            case NOTE:
                sortTypeText = "备注";
                break;
        }
        Messages.showInfoMessage(
                project,
                "已按" + sortTypeText + "排序图钉",
                "排序完成"
        );
    }

    /**
     * 批量删除选中的图钉
     *
     * @param selectedPins 选中的图钉列表
     */
    private void deleteSelectedPins(List<PinEntry> selectedPins) {
        for (PinEntry pin : selectedPins) {
            PinStorage.removePin(pin);
        }

        // 更新列表
        allPins = PinStorage.getPins();
        updateListModel();

        // 刷新标签筛选面板
        if (tagFilterPanelRef[0] != null) {
            tagFilterPanelRef[0].refreshTagsView();
        }

        // 显示删除成功消息
        Messages.showInfoMessage(
                project,
                "已删除 " + selectedPins.size() + " 个图钉",
                "批量删除"
        );
    }

    /**
     * 复制图钉
     * 创建一个新的图钉，复制原图钉的所有属性，并添加到图钉列表中
     *
     * @param original 原始图钉
     */
    private void copyPin(PinEntry original) {
        try {
            // 获取文件
            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(original.filePath);
            if (file == null || !file.exists()) {
                Messages.showErrorDialog(
                        project,
                        "无法复制图钉，文件不存在或已被删除。",
                        "复制失败"
                );
                return;
            }

            // 获取文档
            Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document == null) {
                Messages.showErrorDialog(
                        project,
                        "无法复制图钉，无法获取文件内容。",
                        "复制失败"
                );
                return;
            }

            // 创建新的标记
            RangeMarker newMarker;
            if (original.isBlock) {
                // 如果是代码块图钉，复制整个范围
                newMarker = document.createRangeMarker(
                        original.marker.getStartOffset(),
                        original.marker.getEndOffset()
                );
            } else {
                // 如果是单行图钉，复制当前行
                int line = document.getLineNumber(original.marker.getStartOffset());
                int lineStartOffset = document.getLineStartOffset(line);
                int lineEndOffset = document.getLineEndOffset(line);
                newMarker = document.createRangeMarker(lineStartOffset, lineEndOffset);
            }

            // 复制标签
            List<String> tags = new ArrayList<>(original.getTags());

            // 创建新的图钉
            PinEntry newPin = new PinEntry(
                    original.filePath,
                    newMarker,
                    original.note + " (复制)",  // 添加"(复制)"后缀以区分
                    System.currentTimeMillis(), // 使用当前时间戳
                    System.getProperty("user.name"),
                    original.isBlock,
                    tags
            );

            // 添加到存储
            boolean success = PinStorage.addPin(newPin);
            if (success) {
                Messages.showInfoMessage(
                        project,
                        "图钉复制成功！",
                        "复制成功"
                );
            } else {
                Messages.showErrorDialog(
                        project,
                        "图钉复制失败，可能已达到免费版图钉数量限制。",
                        "复制失败"
                );
            }
        } catch (Exception e) {
            Messages.showErrorDialog(
                    project,
                    "复制图钉时发生错误: " + e.getMessage(),
                    "复制失败"
            );
        }
    }
}