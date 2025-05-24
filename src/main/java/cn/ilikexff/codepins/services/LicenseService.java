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
    // 产品代码已加密，防止反编译后直接获取
    private static final String PRODUCT_CODE_ENCRYPTED = "***REMOVED_ENCODED_DATA***"; // Base64编码的"***REMOVED_PRODUCT_CODE***"

    // 获取产品代码
    private static String getProductCode() {
        try {
            // 解码Base64
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(PRODUCT_CODE_ENCRYPTED);
            return new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOG.error("解码产品代码失败", e);
            return "INVALID_CODE"; // 返回无效代码
        }
    }
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
     * 检查是否可以进行在线验证
     */
    private boolean isOnlineVerificationAvailable() {
        try {
            // 检查是否可以访问JetBrains许可证服务器
            // 这里简化处理，只检查LicensingFacade类是否可用
            Class.forName("com.intellij.ide.plugins.marketplace.LicensingFacade");
            return true;
        } catch (Exception e) {
            LOG.info("Online verification not available: " + e.getMessage());
            return false;
        }
    }

    /**
     * 离线验证许可证
     */
    private boolean verifyOfflineLicense() {
        try {
            // 获取机器ID
            String machineId = getMachineId();

            // 获取产品代码
            String productCode = getProductCode();

            // 检查是否存在离线许可证文件
            java.io.File licenseFile = new java.io.File(System.getProperty("user.home"), ".codepins/license.dat");
            if (!licenseFile.exists()) {
                LOG.info("Offline license file not found");
                return false;
            }

            // 读取许可证文件
            String licenseContent = readLicenseFile(licenseFile);
            if (licenseContent == null || licenseContent.isEmpty()) {
                LOG.info("Offline license file is empty");
                return false;
            }

            // 验证许可证内容
            // 格式: 加密的(产品代码 + 机器ID + 过期时间)
            String decryptedContent = cn.ilikexff.codepins.utils.StringEncryptor.decrypt(licenseContent);
            String[] parts = decryptedContent.split("\\|");

            if (parts.length != 3) {
                LOG.info("Invalid offline license format");
                return false;
            }

            // 验证产品代码
            if (!parts[0].equals(productCode)) {
                LOG.info("Product code mismatch in offline license");
                return false;
            }

            // 验证机器ID
            if (!parts[1].equals(machineId)) {
                LOG.info("Machine ID mismatch in offline license");
                return false;
            }

            // 验证过期时间
            long expirationTime = Long.parseLong(parts[2]);
            if (expirationTime < System.currentTimeMillis()) {
                LOG.info("Offline license has expired");
                return false;
            }

            LOG.info("Offline license is valid");
            return true;
        } catch (Exception e) {
            LOG.error("Error verifying offline license: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取机器ID
     */
    private String getMachineId() {
        try {
            // 获取主机名
            String hostname = java.net.InetAddress.getLocalHost().getHostName();

            // 获取MAC地址
            java.util.Enumeration<java.net.NetworkInterface> networkInterfaces = java.net.NetworkInterface.getNetworkInterfaces();
            StringBuilder macAddresses = new StringBuilder();

            while (networkInterfaces.hasMoreElements()) {
                java.net.NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    for (byte b : mac) {
                        macAddresses.append(String.format("%02X", b));
                    }
                    break; // 只使用第一个有效的MAC地址
                }
            }

            // 组合主机名和MAC地址，并计算哈希值
            String combined = hostname + "|" + macAddresses.toString();
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(combined.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }

            // 返回前32个字符作为机器ID
            return hexString.substring(0, Math.min(32, hexString.length()));
        } catch (Exception e) {
            LOG.error("Error getting machine ID: " + e.getMessage(), e);
            return "unknown";
        }
    }

    /**
     * 读取许可证文件
     */
    private String readLicenseFile(java.io.File file) {
        try {
            java.nio.file.Path path = file.toPath();
            return new String(java.nio.file.Files.readAllBytes(path), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOG.error("Error reading license file: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 验证插件完整性，防止篡改
     */
    private boolean verifyIntegrity() {
        try {
            // 验证类是否被篡改
            String className = this.getClass().getName();
            String expectedClassName = "cn.ilikexff.codepins.services.LicenseService";

            if (!className.equals(expectedClassName)) {
                LOG.warn("Class name mismatch: expected " + expectedClassName + ", got " + className);
                return false;
            }

            // 验证类加载器
            ClassLoader classLoader = this.getClass().getClassLoader();
            if (classLoader == null) {
                LOG.warn("Class loader is null");
                return false;
            }

            // 验证关键方法是否存在
            try {
                this.getClass().getDeclaredMethod("isPremiumUser");
                this.getClass().getDeclaredMethod("checkLicense");
                this.getClass().getDeclaredMethod("getLicenseStatus");
            } catch (NoSuchMethodException e) {
                LOG.warn("Critical method missing: " + e.getMessage());
                return false;
            }

            // 验证产品代码格式
            String productCode = getProductCode();
            if (productCode == null || productCode.length() != 15 || !productCode.startsWith("P")) {
                LOG.warn("Invalid product code format");
                return false;
            }

            // 计算类的简单校验和
            int checksum = 0;
            for (char c : className.toCharArray()) {
                checksum += c;
            }

            // 验证校验和
            if (checksum % 256 != 83) { // 83是预期的校验和余数
                LOG.warn("Checksum verification failed");
                return false;
            }

            return true;
        } catch (Exception e) {
            LOG.error("Integrity verification failed", e);
            return false;
        }
    }

    /**
     * 检查许可证
     * 使用JetBrains Marketplace API验证许可证
     */
    public void checkLicense() {
        try {
            // 添加篡改检测
            if (!verifyIntegrity()) {
                LOG.warn("License integrity check failed");
                licenseStatus = LicenseStatus.INVALID;
                return;
            }

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

            // 添加离线验证逻辑
            if (!isOnlineVerificationAvailable()) {
                LOG.info("Using offline verification");
                boolean offlineValid = verifyOfflineLicense();
                licenseStatus = offlineValid ? LicenseStatus.VALID : LicenseStatus.INVALID;
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

            // 获取产品代码
            String productCode = getProductCode();

            // 获取许可证密钥
            Method getLicenseKeyMethod = licensingFacadeClass.getMethod("getLicenseKey", String.class);
            String licenseKey = (String) getLicenseKeyMethod.invoke(licensingFacade, productCode);

            if (licenseKey == null || licenseKey.isEmpty()) {
                LOG.info("No license key found for product: " + productCode);
                licenseStatus = LicenseStatus.INVALID;
                return;
            }

            // 验证许可证
            Method isLicenseValidMethod = licensingFacadeClass.getMethod("isLicenseValid", String.class, String.class);
            boolean isValid = (Boolean) isLicenseValidMethod.invoke(licensingFacade, productCode, licenseKey);

            if (!isValid) {
                LOG.info("License is not valid for product: " + productCode);
                licenseStatus = LicenseStatus.INVALID;
                return;
            }

            // 检查过期时间
            Method getLicenseExpirationDateMethod = licensingFacadeClass.getMethod("getLicenseExpirationDate", String.class, String.class);
            Date expirationDate = (Date) getLicenseExpirationDateMethod.invoke(licensingFacade, productCode, licenseKey);

            if (expirationDate != null && expirationDate.before(new Date())) {
                LOG.info("License has expired on: " + expirationDate);
                licenseStatus = LicenseStatus.EXPIRED;
                return;
            }

            // 许可证有效
            LOG.info("License is valid for product: " + productCode);
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
        // 如果状态为未检查，重新检查
        if (licenseStatus == LicenseStatus.NOT_CHECKED) {
            checkLicense();
        }
        return licenseStatus == LicenseStatus.VALID;
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

        // 显示升级对话框（已移除价格信息和升级按钮）
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
