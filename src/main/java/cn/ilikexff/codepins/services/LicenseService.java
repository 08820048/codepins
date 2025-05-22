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
 * 用于验证用户是否购买了付费版本
 */
@Service
public final class LicenseService {
    private static final String PRODUCT_CODE = "***REMOVED_PRODUCT_CODE***";
    private static final Logger LOG = Logger.getInstance(LicenseService.class);

    private LicenseStatus licenseStatus = LicenseStatus.NOT_CHECKED;

    /**
     * 许可证状态枚举
     */
    public enum LicenseStatus {
        VALID("有效"),
        INVALID("无效"),
        EXPIRED("已过期"),
        NOT_CHECKED("未检查");

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
     * 初始化时检查许可证
     */
    public LicenseService() {
        checkLicense();
    }

    /**
     * 检查许可证
     * 使用JetBrains Marketplace API验证许可证
     */
    public void checkLicense() {
        try {
            // 获取ApplicationInfo实例
            Object appInfo = ApplicationManager.getApplication().getService(Class.forName("com.intellij.openapi.application.ApplicationInfo"));

            // 在开发模式下，默认返回无效，以便测试免费用户功能
            Method isEAPMethod = appInfo.getClass().getMethod("isEAP");
            boolean isEAP = (Boolean) isEAPMethod.invoke(appInfo);

            // 尝试获取isInternal方法，如果不存在则忽略
            boolean isInternal = false;
            try {
                Method isInternalMethod = appInfo.getClass().getMethod("isInternal");
                isInternal = (Boolean) isInternalMethod.invoke(appInfo);
            } catch (NoSuchMethodException e) {
                LOG.info("isInternal method not found, skipping internal check");
            }

            // 注释掉自动设置为有效的代码，以便测试免费用户功能
            // if (isEAP || isInternal) {
            //     licenseStatus = LicenseStatus.VALID;
            //     LOG.info("Running in development mode, license is considered valid");
            //     return;
            // }

            // 在开发环境中，默认设置为无效，以便测试免费用户功能
            // 注意：在生产环境中，这段代码将被保留，以确保开发环境中的测试正常进行
            if (isEAP || isInternal) {
                licenseStatus = LicenseStatus.INVALID;
                LOG.info("Running in development mode, license is considered invalid for testing");
                return;
            }

            // 使用反射获取LicensingFacade
            Class<?> licensingFacadeClass = Class.forName("com.intellij.ide.plugins.marketplace.LicensingFacade");
            Method getInstanceMethod = licensingFacadeClass.getMethod("getInstance");
            Object licensingFacade = getInstanceMethod.invoke(null);

            if (licensingFacade == null) {
                // 在某些环境中可能无法获取LicensingFacade
                LOG.warn("LicensingFacade is not available");
                licenseStatus = LicenseStatus.INVALID;
                return;
            }

            // 获取许可证密钥
            Method getLicenseKeyMethod = licensingFacadeClass.getMethod("getLicenseKey", String.class);
            String licenseKey = (String) getLicenseKeyMethod.invoke(licensingFacade, PRODUCT_CODE);

            if (licenseKey == null || licenseKey.isEmpty()) {
                LOG.info("No license key found for product: " + PRODUCT_CODE);
                licenseStatus = LicenseStatus.INVALID;
                return;
            }

            // 验证许可证
            Method isLicenseValidMethod = licensingFacadeClass.getMethod("isLicenseValid", String.class, String.class);
            boolean isValid = (Boolean) isLicenseValidMethod.invoke(licensingFacade, PRODUCT_CODE, licenseKey);

            if (!isValid) {
                LOG.info("License is not valid for product: " + PRODUCT_CODE);
                licenseStatus = LicenseStatus.INVALID;
                return;
            }

            // 检查过期时间
            Method getLicenseExpirationDateMethod = licensingFacadeClass.getMethod("getLicenseExpirationDate", String.class, String.class);
            Date expirationDate = (Date) getLicenseExpirationDateMethod.invoke(licensingFacade, PRODUCT_CODE, licenseKey);

            if (expirationDate != null && expirationDate.before(new Date())) {
                LOG.info("License has expired on: " + expirationDate);
                licenseStatus = LicenseStatus.EXPIRED;
                return;
            }

            // 许可证有效
            LOG.info("License is valid for product: " + PRODUCT_CODE);
            licenseStatus = LicenseStatus.VALID;

        } catch (Exception e) {
            LOG.error("Error checking license: " + e.getMessage(), e);
            // 在开发环境中，如果找不到许可证API，默认为无效以便测试
            if (e instanceof ClassNotFoundException) {
                LOG.info("LicensingFacade not found, assuming development environment");
                licenseStatus = LicenseStatus.INVALID; // 设置为无效以便测试免费用户功能
            } else {
                licenseStatus = LicenseStatus.INVALID;
            }
        }
    }

    /**
     * 检查用户是否为付费用户
     *
     * @return 是否为付费用户
     */
    public boolean isPremiumUser() {
        // 检查测试模式设置
        if (cn.ilikexff.codepins.settings.CodePinsSettings.getInstance().testPremiumMode) {
            return true; // 如果测试模式开启，返回true
        }

        // 如果状态为未检查，重新检查
        if (licenseStatus == LicenseStatus.NOT_CHECKED) {
            checkLicense();
        }
        return licenseStatus == LicenseStatus.VALID;
    }

    /**
     * 设置测试模式状态
     *
     * @param enabled 是否启用测试模式
     */
    public void setTestPremiumMode(boolean enabled) {
        cn.ilikexff.codepins.settings.CodePinsSettings.getInstance().testPremiumMode = enabled;
        LOG.info("Test premium mode " + (enabled ? "enabled" : "disabled"));
    }

    /**
     * 显示升级对话框
     *
     * @param project 当前项目
     * @param featureName 功能名称
     * @return 是否点击了升级按钮
     */
    public boolean showUpgradeDialogIfNeeded(Project project, String featureName) {
        if (isPremiumUser()) {
            return true; // 已经是付费用户，不需要显示对话框
        }

        // 显示高级升级对话框
        return cn.ilikexff.codepins.ui.PremiumUpgradeDialog.showDialog(project, featureName);
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
        switch (licenseStatus) {
            case VALID:
                return "您正在使用CodePins专业版";
            case EXPIRED:
                return "您的CodePins专业版许可证已过期";
            case INVALID:
                return "您正在使用CodePins免费版";
            case NOT_CHECKED:
                return "许可证状态未检查";
            default:
                return "未知许可证状态";
        }
    }
}
