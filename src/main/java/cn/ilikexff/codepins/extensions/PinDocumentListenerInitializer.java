package cn.ilikexff.codepins.extensions;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * 文档监听器初始化器
 * 在项目启动时初始化文档监听器
 */
public class PinDocumentListenerInitializer implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        // 获取所有打开的编辑器
        com.intellij.openapi.editor.Editor[] editors = EditorFactory.getInstance().getAllEditors();
        
        // 为每个编辑器的文档添加监听器
        for (com.intellij.openapi.editor.Editor editor : editors) {
            Document document = editor.getDocument();
            document.addDocumentListener(new PinDocumentListener(project, document));
        }
        

        
        // 添加编辑器工厂监听器，为新打开的文档添加监听器
        EditorFactory.getInstance().addEditorFactoryListener(new PinEditorFactoryListener(project), project);
    }
}
