package cn.ilikexff.codepins.extensions;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * 编辑器工厂监听器
 * 用于监听编辑器的创建和销毁，为新创建的编辑器添加文档监听器
 */
public class PinEditorFactoryListener implements EditorFactoryListener {
    private final Project project;

    /**
     * 构造函数
     *
     * @param project 项目
     */
    public PinEditorFactoryListener(Project project) {
        this.project = project;
    }

    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        // 获取编辑器的文档
        Document document = event.getEditor().getDocument();
        
        // 添加文档监听器
        document.addDocumentListener(new PinDocumentListener(project, document));
    }

    @Override
    public void editorReleased(@NotNull EditorFactoryEvent event) {
        // 编辑器释放时不需要特殊处理
    }
}
