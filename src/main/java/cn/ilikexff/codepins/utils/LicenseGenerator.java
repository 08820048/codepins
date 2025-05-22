package cn.ilikexff.codepins.utils;

import com.intellij.openapi.diagnostic.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;

/**
 * 许可证生成工具类
 * 用于生成离线许可证
 * 注意：此类仅用于开发和测试，不会包含在最终的插件中
 */
public class LicenseGenerator {
    private static final Logger LOG = Logger.getInstance(LicenseGenerator.class);
    
    // 产品代码
    private static final String PRODUCT_CODE = "***REMOVED_PRODUCT_CODE***";
    
    /**
     * 生成离线许可证
     *
     * @param machineId 机器ID
     * @param expirationDays 过期天数
     * @return 许可证内容
     */
    public static String generateLicense(String machineId, int expirationDays) {
        try {
            // 计算过期时间
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, expirationDays);
            Date expirationDate = calendar.getTime();
            long expirationTime = expirationDate.getTime();
            
            // 生成许可证内容
            String licenseContent = PRODUCT_CODE + "|" + machineId + "|" + expirationTime;
            
            // 加密许可证内容
            return StringEncryptor.encrypt(licenseContent);
        } catch (Exception e) {
            LOG.error("Error generating license: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 保存许可证到文件
     *
     * @param license 许可证内容
     * @param filePath 文件路径
     * @return 是否保存成功
     */
    public static boolean saveLicense(String license, String filePath) {
        try {
            // 创建目录
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            
            // 写入文件
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(license.getBytes(StandardCharsets.UTF_8));
            }
            
            return true;
        } catch (Exception e) {
            LOG.error("Error saving license: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 生成并保存许可证
     *
     * @param machineId 机器ID
     * @param expirationDays 过期天数
     * @return 是否成功
     */
    public static boolean generateAndSaveLicense(String machineId, int expirationDays) {
        try {
            // 生成许可证
            String license = generateLicense(machineId, expirationDays);
            if (license == null) {
                return false;
            }
            
            // 保存许可证
            String homePath = System.getProperty("user.home");
            String licensePath = homePath + File.separator + ".codepins" + File.separator + "license.dat";
            return saveLicense(license, licensePath);
        } catch (Exception e) {
            LOG.error("Error generating and saving license: " + e.getMessage(), e);
            return false;
        }
    }
}
