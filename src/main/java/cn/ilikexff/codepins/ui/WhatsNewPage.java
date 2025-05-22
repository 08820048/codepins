package cn.ilikexff.codepins.ui;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

import java.time.Year;

/**
 * "What's New"页面
 * 用于在编辑器中显示插件更新内容
 */
public class WhatsNewPage {

    /**
     * 在编辑器中打开"What's New"页面
     *
     * @param project 当前项目
     * @param version 插件版本
     */
    public static void openWhatsNewPage(@NotNull Project project, @NotNull String version) {
        // 创建Markdown内容
        String markdownContent = generateWhatsNewMarkdown(version);

        // 创建虚拟文件 - 使用.md扩展名以便IDE识别为Markdown
        LightVirtualFile file = new LightVirtualFile(
                "CodePins " + version + " 更新内容.md",
                FileTypeManager.getInstance().getFileTypeByExtension("md"),
                markdownContent
        );

        // 打开编辑器
        FileEditorManager.getInstance(project).openFile(file, true);
    }

    /**
     * 生成"What's New"的Markdown内容
     *
     * @param version 插件版本
     * @return Markdown内容
     */
    private static String generateWhatsNewMarkdown(@NotNull String version) {
        StringBuilder markdown = new StringBuilder();

        // 标题
        markdown.append("# CodePins ").append(version).append(" 更新内容\n\n");
        markdown.append("感谢您使用CodePins插件！以下是此版本的更新内容。\n\n");

        // 根据版本添加不同的更新内容
        if (version.equals("1.1.3")) {
            markdown.append("## 新功能与改进\n\n");

            // 标签管理增强
            markdown.append("### 标签管理增强\n\n");
            markdown.append("* 免费版现在可以使用最多10种不同标签\n");
            markdown.append("* 免费版每个图钉最多可添加3个标签\n");
            markdown.append("* 专业版用户可以使用无限标签\n\n");

            // 用户界面优化
            markdown.append("### 用户界面优化\n\n");
            markdown.append("* 标签筛选面板显示当前标签使用情况\n");
            markdown.append("* 标签编辑对话框增加标签限制提示\n");
            markdown.append("* 改进了标签颜色生成算法\n\n");

            // 性能改进
            markdown.append("### 性能改进\n\n");
            markdown.append("* 优化了标签筛选性能\n");
            markdown.append("* 减少了内存占用\n\n");

            // FREEMIUM商业模式
            markdown.append("### FREEMIUM商业模式\n\n");
            markdown.append("* 实现FREEMIUM商业模式，区分专业版和免费版功能\n");
            markdown.append("* 添加统一的专业版功能升级对话框\n");
            markdown.append("* 简化为仅保留文本水印选项\n\n");
        } else {
            // 默认更新内容
            markdown.append("## 新版本更新\n\n");
            markdown.append("感谢您使用CodePins插件！\n\n");
        }

        // 链接
        markdown.append("## 相关链接\n\n");
        markdown.append("[访问插件主页](https://plugins.jetbrains.com/plugin/27300-codepins--code-bookmarks)\n\n");

        // 页脚
        markdown.append("---\n\n");
        markdown.append("CodePins插件 © ").append(Year.now().getValue()).append(" ilikexff. 保留所有权利。\n");

        return markdown.toString();
    }
}
