package cn.ilikexff.codepins.extensions;

import cn.ilikexff.codepins.settings.CodePinsSettings;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * 文件保存监听器
 * 在文件保存时自动检测注释标记
 */
public class PinCommentSaveListener implements FileDocumentManagerListener {

    @Override
    public void beforeDocumentSaving(@NotNull Document document) {
        // 获取设置
        CodePinsSettings settings = CodePinsSettings.getInstance();
        
        // 如果启用了完成指令符号，则不在保存时触发图钉添加
        // 而是等待用户手动触发或使用完成符号
        if (settings.useCompletionSymbol) {
            return;
        }
        
        // 获取文件
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file == null) {
            return;
        }

        // 获取所有打开的项目
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length == 0) {
            return;
        }

        // 使用第一个打开的项目
        Project project = projects[0];

        // 获取 PSI 文件
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (psiFile == null) {
            return;
        }

        // 使用注释检测器处理文件
        // 创建一个 PinCommentAction 实例并调用其扫描方法
        PinCommentAction commentAction = new PinCommentAction();
        commentAction.scanFile(psiFile, document, project);
    }
}
