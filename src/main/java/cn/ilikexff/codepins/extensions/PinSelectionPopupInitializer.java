package cn.ilikexff.codepins.extensions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * 选择文本后的浮动操作按钮初始化器
 * 在项目启动时初始化选择文本后的浮动操作按钮功能
 */
public class PinSelectionPopupInitializer implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        // 为所有已打开的编辑器安装选择监听器
        for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
            PinSelectionPopup.installOn(editor, project);
        }

        // 为新创建的编辑器安装选择监听器
        EditorFactory.getInstance().addEditorFactoryListener(new EditorFactoryListener() {
            @Override
            public void editorCreated(@NotNull EditorFactoryEvent event) {
                Editor editor = event.getEditor();
                if (editor.getProject() == project) {
                    PinSelectionPopup.installOn(editor, project);
                }
            }
        }, project);
    }
}
