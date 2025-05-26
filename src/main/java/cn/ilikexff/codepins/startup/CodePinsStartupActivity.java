package cn.ilikexff.codepins.startup;

import cn.ilikexff.codepins.settings.LanguageSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * CodePins 启动活动
 * 在插件启动时初始化语言设置
 */
public class CodePinsStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        // 插件启动时的初始化逻辑
        // 语言设置会在需要时自动加载
    }
}
