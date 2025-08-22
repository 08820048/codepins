package cn.ilikexff.codepins;

import cn.ilikexff.codepins.i18n.CodePinsBundle;
import cn.ilikexff.codepins.core.PinEntry;
import cn.ilikexff.codepins.core.PinStorage;
import cn.ilikexff.codepins.core.PinState;
import cn.ilikexff.codepins.core.PinStateService;
import cn.ilikexff.codepins.settings.CodePinsSettings;
import cn.ilikexff.codepins.ui.AnimationUtil;
import cn.ilikexff.codepins.ui.EmptyStatePanel;
import cn.ilikexff.codepins.ui.ExportDialog;
import cn.ilikexff.codepins.ui.ImportDialog;
import cn.ilikexff.codepins.ui.PinListCellRenderer;
import cn.ilikexff.codepins.ui.SearchTextField;
import cn.ilikexff.codepins.ui.ShareDialog;
import cn.ilikexff.codepins.ui.SimpleTagEditorDialog;
import cn.ilikexff.codepins.ui.StatisticsPanel;
import cn.ilikexff.codepins.ui.TagFilterPanel;
import cn.ilikexff.codepins.ai.ui.SmartSuggestionPanel;
import cn.ilikexff.codepins.utils.IconUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ActionPlaces;
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
import java.util.stream.Collectors;

public class PinsToolWindow implements ToolWindowFactory {

    private Project project;
    private DefaultListModel<PinEntry> model;
    private List<PinEntry> allPins;
    private JList<PinEntry> list;
    private final TagFilterPanel[] tagFilterPanelRef = new TagFilterPanel[1]; // 使用数组引用来解决前向引用问题
    private SearchTextField searchField;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JComponent pinCountLabel; // 添加图钉数量标签引用

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
                    Icon shareIconMini = IconUtil.loadIcon("/icons/share-mini.svg", getClass());
                    Icon deleteIcon = IconUtil.loadIcon("/icons/trash.svg", getClass());
                    Icon refreshIcon = IconUtil.loadIcon("/icons/refresh.svg", getClass());

                    // 根据图钉类型添加不同的菜单项
                    if (selected.isBlock) {
                        // 如果是代码块图钉，添加代码预览项
                        JMenuItem codeItem = new JMenuItem(CodePinsBundle.message("context.view.code"), codeIcon);
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
                    JMenuItem editItem = new JMenuItem(CodePinsBundle.message("context.edit.note"), editIcon);
                    // 应用自定义UI
                    cn.ilikexff.codepins.ui.CustomMenuItemUI.apply(editItem);
                    editItem.addActionListener(event -> {
                        // 添加按钮动画效果
                        AnimationUtil.buttonClickEffect(editItem);
                        String newNote = JOptionPane.showInputDialog(null, CodePinsBundle.message("note.placeholder"), selected.note);
                        if (newNote != null) {
                            PinStorage.updateNote(selected, newNote.trim());
                        }
                    });
                    menu.add(editItem);

                    // 添加编辑标签项
                    JMenuItem tagItem = new JMenuItem(CodePinsBundle.message("context.edit.tags"), tagIcon);
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
                    JMenuItem copyItem = new JMenuItem(CodePinsBundle.message("context.copy.pin"), copyIcon);
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
                    JMenuItem shareItem = new JMenuItem(CodePinsBundle.message("context.share.pin"), shareIconMini);
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
                    JMenuItem deleteItem = new JMenuItem(CodePinsBundle.message("context.delete.pin"), deleteIcon);
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
                            // 更新图钉数量标签
                            updatePinCountLabel();
                        }
                    });
                    menu.add(deleteItem);

                    // 添加刷新项
                    JMenuItem refreshItem = new JMenuItem(CodePinsBundle.message("tag.refresh"), refreshIcon);
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
        this.cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);

        // 创建列表面板（只包含列表）
        JPanel listPanel = new JPanel(new BorderLayout());
        JBScrollPane scrollPane = new JBScrollPane(list);
        listPanel.add(scrollPane, BorderLayout.CENTER);

        // 添加列表面板和空状态面板到卡片布局
        contentPanel.add(listPanel, "LIST");
        contentPanel.add(emptyStatePanel, "EMPTY");

        // 创建包含标签面板和内容面板的完整内容区域
        JPanel fullContentPanel = new JPanel(new BorderLayout());
        fullContentPanel.add(tagFilterPanelRef[0], BorderLayout.NORTH);
        fullContentPanel.add(contentPanel, BorderLayout.CENTER);

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

        // 创建顶部面板（搜索和工具栏），使用更紧凑的布局
        JPanel topPanel = new JPanel(new BorderLayout(2, 0));
        topPanel.setBorder(JBUI.Borders.empty(2, 4, 2, 4)); // 减少上下边距，保持左右边距

        // 创建搜索面板，包含搜索框
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        searchPanel.add(createSearchField(), BorderLayout.CENTER);

        // 创建右侧面板，包含图钉计数和工具栏，使用垂直居中的布局
        // 使用BoxLayout而不是FlowLayout，以便更好地控制垂直对齐
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
        rightPanel.setOpaque(false);
        // 添加垂直对齐的设置
        rightPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        // 添加图钉计数标签
        pinCountLabel = createPinCountLabel();
        // 设置垂直对齐
        pinCountLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        rightPanel.add(pinCountLabel);
        
        // 添加工具栏
        JComponent toolbarComponent = createToolbar().getComponent();
        // 设置垂直对齐
        toolbarComponent.setAlignmentY(Component.CENTER_ALIGNMENT);
        rightPanel.add(toolbarComponent);

        // 添加到顶部面板
        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(rightPanel, BorderLayout.EAST);

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(fullContentPanel, BorderLayout.CENTER);

        // 创建选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();

        // 添加图钉列表选项卡
        tabbedPane.addTab(CodePinsBundle.message("ui.pins.list"), IconUtil.loadIcon("/icons/pin.svg", getClass()), mainPanel);

        // 创建统计面板
        StatisticsPanel statisticsPanel = new StatisticsPanel();
        tabbedPane.addTab(CodePinsBundle.message("statistics.tab.title"), IconUtil.loadIcon("/icons/chart.svg", getClass()), statisticsPanel);

        // 创建智能建议面板
        SmartSuggestionPanel suggestionPanel = new SmartSuggestionPanel(project);
        tabbedPane.addTab("智能建议", IconUtil.loadIcon("/icons/ai-suggestion.svg", getClass()), suggestionPanel);

        // 监听选项卡切换，刷新统计数据
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) { // 统计面板
                statisticsPanel.refreshStatistics();
            } else if (tabbedPane.getSelectedIndex() == 2) { // 智能建议面板
                // 当切换到智能建议面板时，激活面板并分析当前文件
                suggestionPanel.onPanelActivated();
            }
        });

        // 添加到工具窗口
        Content content = toolWindow.getContentManager().getFactory().createContent(tabbedPane, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    /**
     * 创建现代化搜索框
     */
    private JComponent createSearchField() {
        // 创建现代化搜索框
        this.searchField = new SearchTextField(CodePinsBundle.message("tooltip.searchPlaceholder"));

        // 创建容器面板，添加边距，减少上下边距以便更好地垂直居中
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(JBUI.Borders.empty(2, 8, 2, 8)); // 减少上下边距，保持左右边距
        container.setOpaque(false); // 透明背景，增加现代感
        container.add(searchField, BorderLayout.CENTER);
        // 设置垂直对齐
        container.setAlignmentY(Component.CENTER_ALIGNMENT);

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

        // 更新图钉数量标签
        updatePinCountLabel();
    }

    /**
     * 更新图钉数量标签
     */
    private void updatePinCountLabel() {
        if (pinCountLabel != null) {
            // 创建新的图钉计数标签
            JComponent newCountLabel = createPinCountLabel();

            // 替换旧的标签
            if (pinCountLabel.getParent() != null) {
                Container parent = pinCountLabel.getParent();
                int index = -1;
                for (int i = 0; i < parent.getComponentCount(); i++) {
                    if (parent.getComponent(i) == pinCountLabel) {
                        index = i;
                        break;
                    }
                }

                if (index >= 0) {
                    parent.remove(pinCountLabel);
                    parent.add(newCountLabel, index);
                    parent.revalidate();
                    parent.repaint();
                    pinCountLabel = newCountLabel;
                }
            }
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
            protected Transferable createTransferable(JComponent c) {
                @SuppressWarnings("unchecked") // 这里的注解是必要的，因为我们知道c是一个 JList<PinEntry>
                JList<PinEntry> list = (JList<PinEntry>) c;
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
                    JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
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

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
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

                    // 更新图钉数量标签
                    updatePinCountLabel();
                }
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        });

        // 分享按钮
        Icon shareIcon = IconUtil.loadIcon("/icons/share.svg", getClass());
        group.add(new AnAction(CodePinsBundle.message("toolbar.share"), CodePinsBundle.message("toolbar.share.desc"), shareIcon) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                List<PinEntry> selectedPins = list.getSelectedValuesList();
                if (selectedPins.isEmpty()) {
                    // 如果没有选中的图钉，提示用户
                    Messages.showInfoMessage(
                            project,
                            CodePinsBundle.message("toolbar.select.prompt"),
                            CodePinsBundle.message("toolbar.share")
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

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        });

        // 批量删除按钮
        Icon deleteMultipleIcon = IconUtil.loadIcon("/icons/delete-multiple.svg", getClass());
        group.add(new AnAction(CodePinsBundle.message("toolbar.delete.multiple"), CodePinsBundle.message("toolbar.delete.multiple.desc"), deleteMultipleIcon) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                List<PinEntry> selectedPins = list.getSelectedValuesList();
                if (selectedPins.isEmpty()) {
                    Messages.showInfoMessage(
                            project,
                            CodePinsBundle.message("toolbar.select.prompt"),
                            CodePinsBundle.message("toolbar.delete.multiple")
                    );
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(null,
                        CodePinsBundle.message("toolbar.confirm.delete", selectedPins.size()),
                        CodePinsBundle.message("toolbar.confirm.delete.title"), JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    deleteSelectedPins(selectedPins);
                }
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                // 只有在有图钉时才启用此操作
                e.getPresentation().setEnabled(!model.isEmpty());
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        });

        // 排序按钮
        Icon sortIcon = IconUtil.loadIcon("/icons/sort.svg", getClass());
        DefaultActionGroup sortGroup = new DefaultActionGroup(CodePinsBundle.message("toolbar.sort"), true);
        sortGroup.getTemplatePresentation().setIcon(sortIcon);
        sortGroup.getTemplatePresentation().setText(CodePinsBundle.message("toolbar.sort"));
        sortGroup.getTemplatePresentation().setDescription(CodePinsBundle.message("toolbar.sort"));

        // 添加排序选项
        sortGroup.add(new AnAction(CodePinsBundle.message("toolbar.sort.time.desc")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                sortPins(SortType.TIME_DESC);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        });

        sortGroup.add(new AnAction(CodePinsBundle.message("toolbar.sort.time.asc")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                sortPins(SortType.TIME_ASC);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        });

        sortGroup.add(new AnAction(CodePinsBundle.message("toolbar.sort.name.asc")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                sortPins(SortType.FILENAME);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        });

        sortGroup.add(new AnAction(CodePinsBundle.message("toolbar.sort.path.asc")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                sortPins(SortType.NOTE);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        });

        group.add(sortGroup);

        // 清空按钮
        Icon clearIcon = IconUtil.loadIcon("/icons/x-octagon.svg", getClass());
        group.add(new AnAction(CodePinsBundle.message("button.clear"), CodePinsBundle.message("button.clear"), clearIcon) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(null,
                        CodePinsBundle.message("dialog.confirm.clear"), CodePinsBundle.message("dialog.confirm.title"), JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    PinStorage.clearAll();
                    allPins = PinStorage.getPins();
                    model.clear();

                    // 刷新标签筛选面板
                    if (tagFilterPanelRef[0] != null) {
                        tagFilterPanelRef[0].refreshTagsView();
                    }

                    // 更新图钉数量标签
                    updatePinCountLabel();
                }
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        });

        return ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, true);
    }

    /**
     * 创建图钉计数标签
     */
    private JComponent createPinCountLabel() {
        // 获取图钉数量信息
        int currentCount = PinStorage.getPins().size();
        
        // 创建标签
        JLabel countLabel = new JLabel();
        
        // 获取当前UI主题颜色
        Color successColor = JBColor.namedColor("Plugins.tagForeground", new JBColor(new Color(0x008000), new Color(0x369E6A)));
        
        // 设置标签文本和样式 - 插件现在完全免费开源
        countLabel.setText(CodePinsBundle.message("toolbar.pin.count", currentCount));
        countLabel.setForeground(successColor);
        countLabel.setIcon(IconUtil.loadIcon("/icons/pin-small.svg", getClass()));
        // 设置字体和边距，使用更紧凑的边距
        countLabel.setFont(countLabel.getFont().deriveFont(Font.PLAIN, 12f));
        countLabel.setBorder(JBUI.Borders.empty(0, 4, 0, 2)); // 减少左右边距，使布局更紧凑
        countLabel.setIconTextGap(2); // 减少图标和文本间距
        // 设置垂直对齐
        countLabel.setVerticalAlignment(SwingConstants.CENTER);

        // 设置提示信息
        countLabel.setToolTipText("图钉数量无限制，插件已完全免费开源");

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

        // 更新图钉数量标签
        updatePinCountLabel();

        // 显示删除成功消息
        Messages.showInfoMessage(
                project,
                "已删除 " + selectedPins.size() + " 个图钉",
                "批量删除"
        );
    }

    /**
     * 更新列表模型
     * 根据当前的筛选条件更新列表显示
     */
    private void updateListModel() {
        // 获取当前的筛选标签
        List<String> filterTags = new ArrayList<>();
        if (tagFilterPanelRef[0] != null) {
            filterTags = tagFilterPanelRef[0].getSelectedTags();
        }

        // 获取搜索文本
        String searchText = searchField.getText().trim().toLowerCase();

        // 根据标签和搜索文本筛选图钉
        List<PinEntry> filteredPins;
        if (filterTags.isEmpty()) {
            filteredPins = new ArrayList<>(allPins);
        } else {
            filteredPins = PinStorage.filterByTags(filterTags);
        }

        // 应用搜索筛选
        if (!searchText.isEmpty()) {
            filteredPins = filteredPins.stream()
                    .filter(pin -> {
                        String fileName = new File(pin.filePath).getName().toLowerCase();
                        String note = pin.note.toLowerCase();
                        return fileName.contains(searchText) || note.contains(searchText);
                    })
                    .collect(Collectors.toList());
        }

        // 更新模型
        model.clear();
        for (PinEntry pin : filteredPins) {
            model.addElement(pin);
        }

        // 更新空状态面板和图钉数量标签
        if (contentPanel != null && cardLayout != null) {
            updateContentView(cardLayout, contentPanel);
        }
    }

    /**
     * 更新空状态面板
     * 根据当前列表是否为空决定显示空状态还是列表
     */
    private void updateEmptyState() {
        if (contentPanel != null && cardLayout != null) {
            updateContentView(cardLayout, contentPanel);
        }
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
                // 更新图钉数量标签
                updatePinCountLabel();

                Messages.showInfoMessage(
                        project,
                        CodePinsBundle.message("notification.copy.success"),
                        CodePinsBundle.message("notification.copy.success.title")
                );
            } else {
                Messages.showErrorDialog(
                        project,
                        "图钉复制失败，请稍后重试。",
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