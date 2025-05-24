# CodePins插件ProGuard配置文件

# 保留JetBrains平台相关类
-keep class com.intellij.** { *; }
-keep class org.jetbrains.** { *; }

# 保留插件入口点和服务类
-keep class cn.ilikexff.codepins.PinsToolWindow { *; }
-keep class cn.ilikexff.codepins.PinAction { *; }
-keep class cn.ilikexff.codepins.actions.** { *; }
-keep class cn.ilikexff.codepins.services.** { *; }
-keep class cn.ilikexff.codepins.settings.CodePinsSettings { *; }
-keep class cn.ilikexff.codepins.settings.CodePinsSettingsConfigurable { *; }
-keep class cn.ilikexff.codepins.settings.CodePinsSettingsComponent { *; }
-keep class cn.ilikexff.codepins.PinStateService { *; }

# 保留所有在plugin.xml中注册的类
-keep @com.intellij.openapi.components.State class *
-keep @com.intellij.openapi.components.Service class *
-keep @org.jetbrains.annotations.ApiStatus.Internal class *

# 保留所有扩展点实现
-keep class * implements com.intellij.openapi.project.ProjectComponent
-keep class * implements com.intellij.openapi.application.ApplicationComponent
-keep class * implements com.intellij.openapi.components.PersistentStateComponent
-keep class * implements com.intellij.openapi.startup.StartupActivity
-keep class * implements com.intellij.openapi.actionSystem.AnAction
-keep class * implements com.intellij.openapi.wm.ToolWindowFactory

# 保留所有注解
-keepattributes *Annotation*

# 保留泛型信息
-keepattributes Signature

# 保留源文件和行号信息，用于异常堆栈跟踪
-keepattributes SourceFile,LineNumberTable

# 保留资源文件
-keep class *.xml
-keep class *.html
-keep class *.properties

# 保留清单文件
-keep class META-INF.plugin.xml

# 不混淆枚举类
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 不混淆序列化类
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 不混淆本地方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 不混淆Bean类的getter和setter方法
-keepclassmembers class * {
    void set*(***);
    *** get*();
}

# 加密字符串
-encryptstrings class cn.ilikexff.codepins.services.LicenseService {
    private static final String PRODUCT_CODE;
}

# 混淆控制流
-obfuscateflow aggressive

# 混淆方法参数
-methodparameterchanges flowObfuscate

# 混淆引用
-obfuscatereferences normal

# 不混淆UI相关类的名称，以避免反射问题
-keepnames class * extends javax.swing.JComponent
-keepnames class * extends javax.swing.JPanel
-keepnames class * extends javax.swing.JDialog

# 不混淆资源文件
-keepdirectories resources

# 保留简化的许可证服务（用于兼容性）
-keep class cn.ilikexff.codepins.services.LicenseService {
    public boolean isPremiumUser();
    public static cn.ilikexff.codepins.services.LicenseService getInstance();
    public cn.ilikexff.codepins.services.LicenseService$LicenseStatus getLicenseStatus();
    public java.lang.String getLicenseStatusDescription();
}

# 保留许可证状态枚举
-keep class cn.ilikexff.codepins.services.LicenseService$LicenseStatus {
    public static final cn.ilikexff.codepins.services.LicenseService$LicenseStatus *;
    public java.lang.String getDisplayName();
}

# 不混淆设置类
-keep class cn.ilikexff.codepins.settings.CodePinsSettings {
    public static cn.ilikexff.codepins.settings.CodePinsSettings getInstance();
}

# 保留必要的第三方库
-keep class com.google.gson.** { *; }
-keep class org.apache.xmlgraphics.** { *; }
-keep class org.apache.pdfbox.** { *; }
-keep class com.google.zxing.** { *; }
-keep class org.kohsuke.** { *; }

# 插件现在完全免费，移除特定的优化和混淆规则

# 保留所有的接口
-keep interface * extends *

# 保留所有的异常类
-keep class * extends java.lang.Exception
