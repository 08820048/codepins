package cn.ilikexff.codepins.extensions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * 注释标记检测器初始化器
 * 在项目启动时初始化注释标记检测器
 */
public class PinCommentDetectorInitializer implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        // 安装注释标记检测器
        PinCommentDetector.installOn(project);
    }
}
