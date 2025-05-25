package cn.ilikexff.codepins.extensions;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * 编辑器工厂监听器
 * 监听编辑器的创建和销毁，为新创建的编辑器添加完成符号监听器
 */
public class PinCompletionSymbolEditorFactoryListener implements EditorFactoryListener {
    private final Project project;

    /**
     * 构造函数
     *
     * @param project 项目
     */
    public PinCompletionSymbolEditorFactoryListener(Project project) {
        this.project = project;
    }

    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        Document document = event.getEditor().getDocument();
        document.addDocumentListener(new PinCompletionSymbolListener(project, document));
    }
}
