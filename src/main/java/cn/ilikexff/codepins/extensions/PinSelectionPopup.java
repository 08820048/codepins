package cn.ilikexff.codepins.extensions;

import cn.ilikexff.codepins.core.PinEntry;
import cn.ilikexff.codepins.core.PinStorage;
import cn.ilikexff.codepins.settings.CodePinsSettings;
import cn.ilikexff.codepins.ui.SimpleTagEditorDialog;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 选择文本后的浮动操作按钮
 * 当用户选择文本后，在选择区域附近显示一个浮动操作按钮，点击后将选中的代码块添加为图钉
 */
public class PinSelectionPopup implements SelectionListener {
    private static final Map<Editor, JBPopup> activePopups = new HashMap<>();
    private final Editor editor;
    private final Project project;

    /**
     * 构造函数
     *
     * @param editor  编辑器
     * @param project 项目
     */
    public PinSelectionPopup(Editor editor, Project project) {
        this.editor = editor;
        this.project = project;
    }

    /**
     * 为编辑器添加选择监听器
     *
     * @param editor  编辑器
     * @param project 项目
     */
    public static void installOn(Editor editor, Project project) {
        PinSelectionPopup popup = new PinSelectionPopup(editor, project);
        editor.getSelectionModel().addSelectionListener(popup);
        
        // 添加鼠标点击监听器，在点击编辑器时关闭弹出窗口
        editor.addEditorMouseListener(new EditorMouseListener() {
            @Override
            public void mouseClicked(@NotNull EditorMouseEvent event) {
                closePopupFor(editor);
            }
        });
    }

    @Override
    public void selectionChanged(@NotNull SelectionEvent e) {
        // 如果已有弹出窗口，先关闭
        closePopupFor(editor);

        // 如果没有选择文本，不显示弹出窗口
        if (!hasSelection()) {
            return;
        }

        // 创建弹出窗口
        createPopup();
    }

    /**
     * 检查是否有文本选择
     *
     * @return 是否有文本选择
     */
    private boolean hasSelection() {
        SelectionModel selectionModel = editor.getSelectionModel();
        return selectionModel.hasSelection() && 
               selectionModel.getSelectedText() != null && 
               !selectionModel.getSelectedText().isEmpty();
    }

    /**
     * 创建弹出窗口
     */
    private void createPopup() {
        // 创建操作组
        DefaultActionGroup group = new DefaultActionGroup();
        
        // 添加图钉操作
        group.add(new AnAction("添加为图钉", "将选中的代码块添加为图钉", 
                IconLoader.getIcon("/icons/pin-here.svg", PinSelectionPopup.class)) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                addPinForSelection();
                closePopupFor(editor);
            }
        });

        // 创建操作工具栏
        JComponent toolbar = ActionManager.getInstance()
                .createActionToolbar("PinSelectionPopup", group, true)
                .getComponent();

        // 创建弹出窗口
        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(toolbar, toolbar)
                .setRequestFocus(false)
                .setFocusable(false)
                .setResizable(false)
                .setCancelOnClickOutside(true)
                .setCancelOnWindowDeactivation(true)
                .setCancelKeyEnabled(true)
                .createPopup();

        // 计算弹出窗口位置
        Point popupPoint = calculatePopupPoint();
        if (popupPoint != null) {
            popup.show(new RelativePoint(editor.getContentComponent(), popupPoint));
            activePopups.put(editor, popup);
        }
    }

    /**
     * 计算弹出窗口位置
     *
     * @return 弹出窗口位置
     */
    private Point calculatePopupPoint() {
        SelectionModel selectionModel = editor.getSelectionModel();
        if (!selectionModel.hasSelection()) {
            return null;
        }

        // 获取选择的起始和结束偏移量
        int startOffset = selectionModel.getSelectionStart();
        int endOffset = selectionModel.getSelectionEnd();

        // 获取选择的起始和结束位置
        Point startPoint = editor.offsetToXY(startOffset);
        Point endPoint = editor.offsetToXY(endOffset);

        // 计算弹出窗口位置（选择区域的右上角）
        return new Point(Math.max(startPoint.x, endPoint.x) + 10, Math.min(startPoint.y, endPoint.y) - 10);
    }

    /**
     * 关闭编辑器的弹出窗口
     *
     * @param editor 编辑器
     */
    private static void closePopupFor(Editor editor) {
        JBPopup popup = activePopups.get(editor);
        if (popup != null && !popup.isDisposed()) {
            popup.cancel();
            activePopups.remove(editor);
        }
    }

    /**
     * 为选中的代码块添加图钉
     */
    private void addPinForSelection() {
        SelectionModel selectionModel = editor.getSelectionModel();
        if (!selectionModel.hasSelection()) {
            return;
        }

        Document document = editor.getDocument();
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file == null) {
            return;
        }

        int startOffset = selectionModel.getSelectionStart();
        int endOffset = selectionModel.getSelectionEnd();
        
        // 检查用户设置，决定是否显示备注框和标签框
        boolean showNoteDialog = CodePinsSettings.getInstance().showNoteDialogOnQuickAdd;
        
        if (showNoteDialog) {
            // 显示备注框和标签框
            // 请求用户输入备注
            String note = Messages.showInputDialog(
                    project,
                    "请输入图钉备注（可选）：",
                    "添加图钉",
                    null
            );

            // 如果用户取消了输入，不添加图钉
            if (note == null) {
                return;
            }

            // 创建标签对话框，请求用户输入标签
            List<String> tags = new ArrayList<>();
            SimpleTagEditorDialog tagDialog = new SimpleTagEditorDialog(project, new PinEntry(
                    file.getPath(),
                    document.createRangeMarker(0, 0), // 临时标记，仅用于对话框
                    note,
                    System.currentTimeMillis(),
                    System.getProperty("user.name"),
                    true,
                    tags
            ));

            if (tagDialog.showAndGet()) {
                // 如果用户点击了确定，获取标签
                tags = tagDialog.getTags();
            }

            // 添加图钉
            PinEntry pinEntry = new PinEntry(
                    file.getPath(),
                    document.createRangeMarker(startOffset, endOffset),
                    note,
                    System.currentTimeMillis(),
                    System.getProperty("user.name"),
                    true,
                    tags
            );
            PinStorage.addPin(pinEntry);
        } else {
            // 直接创建图钉，不显示备注框和标签框
            PinEntry.createPin(project, file.getPath(), document, startOffset, endOffset, "", true);
        }
    }
}
