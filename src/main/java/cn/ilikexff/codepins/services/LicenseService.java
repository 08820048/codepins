package cn.ilikexff.codepins.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * 许可证服务
 * 插件现在完全免费开源，此服务仅用于提供捐赠功能
 */
@Service
public final class LicenseService {
    private static final Logger LOG = Logger.getInstance(LicenseService.class);

    private LicenseStatus licenseStatus = LicenseStatus.FREE;

    /**
     * 许可证状态枚举（保留用于兼容性）
     */
    public enum LicenseStatus {
        FREE("免费开源版");

        private final String displayName;

        LicenseStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 获取实例
     *
     * @return LicenseService实例
     */
    public static LicenseService getInstance() {
        return ApplicationManager.getApplication().getService(LicenseService.class);
    }

    /**
     * 构造函数
     * 插件现在完全免费，无需检查许可证
     */
    public LicenseService() {
        // 插件现在完全免费开源
    }

    /**
     * 检查许可证（简化版）
     * 插件现在完全免费，无需复杂的许可证验证
     */
    public void checkLicense() {
        // 插件现在完全免费开源，无需许可证验证
        licenseStatus = LicenseStatus.FREE;
        LOG.info("CodePins is now completely free and open source");
    }

    /**
     * 检查用户是否为付费用户
     * 插件现在完全免费，始终返回false
     *
     * @return 始终返回false（免费版）
     */
    public boolean isPremiumUser() {
        return false; // 插件现在完全免费
    }

    /**
     * 显示捐赠对话框
     *
     * @param project 当前项目
     * @param featureName 功能名称（保留用于兼容性）
     * @return 始终返回true
     */
    public boolean showUpgradeDialogIfNeeded(Project project, String featureName) {
        // 插件现在完全免费，可以考虑显示捐赠对话框
        return true;
    }

    /**
     * 获取许可证状态
     *
     * @return 许可证状态
     */
    public LicenseStatus getLicenseStatus() {
        return licenseStatus;
    }

    /**
     * 获取许可证状态描述
     *
     * @return 许可证状态描述
     */
    public String getLicenseStatusDescription() {
        return "您正在使用CodePins免费开源版";
    }
}
