package cn.ilikexff.codepins.listeners;

import cn.ilikexff.codepins.ui.WhatsNewDialog;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

/**
 * 插件激活监听器
 * 用于在IDE启动时显示"What's New"对话框
 */
public class PluginActivationListener implements StartupActivity.DumbAware {

    private static final String PLUGIN_ID = "cn.ilikexff.codepins";
    private static final String PROPERTY_LAST_VERSION = "codepins.last.version";

    @Override
    public void runActivity(@NotNull Project project) {
        // 注册应用程序激活监听器
        MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect(project);
        connection.subscribe(ApplicationActivationListener.TOPIC, new ApplicationActivationListener() {
            @Override
            public void applicationActivated(@NotNull IdeFrame ideFrame) {
                // 获取当前插件版本
                IdeaPluginDescriptor pluginDescriptor = PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID));
                if (pluginDescriptor == null) {
                    return;
                }

                String currentVersion = pluginDescriptor.getVersion();
                String lastVersion = PropertiesComponent.getInstance().getValue(PROPERTY_LAST_VERSION, "");

                // 如果版本不同，显示"What's New"对话框
                if (!currentVersion.equals(lastVersion)) {
                    // 保存当前版本
                    PropertiesComponent.getInstance().setValue(PROPERTY_LAST_VERSION, currentVersion);

                    // 显示"What's New"对话框
                    ApplicationManager.getApplication().invokeLater(() -> {
                        WhatsNewDialog dialog = new WhatsNewDialog(project, currentVersion);
                        dialog.show();
                    });
                }
            }
        });
    }
}
