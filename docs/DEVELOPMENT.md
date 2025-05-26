# 🛠️ CodePins 开发指南

本文档为 CodePins 插件的开发提供详细指导，包括项目结构、开发规范、国际化、测试等内容。

## 📋 目录

- [项目概述](#项目概述)
- [开发环境](#开发环境)
- [项目结构](#项目结构)
- [开发规范](#开发规范)
- [国际化开发](#国际化开发)
- [UI 开发指南](#ui-开发指南)
- [测试指南](#测试指南)
- [构建和发布](#构建和发布)

## 🎯 项目概述

CodePins 是一个现代化的 IntelliJ IDEA 代码书签插件，使用以下技术栈：

- **语言**: Java 17+
- **UI 框架**: Swing (传统组件)
- **构建工具**: Gradle + IntelliJ Platform Plugin
- **IDE 平台**: IntelliJ Platform SDK
- **国际化**: Java ResourceBundle

## 🔧 开发环境

### 必需工具
- **JDK**: 17 或更高版本
- **IDE**: IntelliJ IDEA (推荐 Ultimate 版本)
- **Gradle**: 8.0+ (通过 Gradle Wrapper)

### 环境配置
1. 克隆项目：
   ```bash
   git clone https://github.com/08820048/codepins.git
   cd codepins
   ```

2. 导入到 IntelliJ IDEA：
   - File → Open → 选择项目根目录
   - 等待 Gradle 同步完成

3. 配置 Plugin SDK：
   - File → Project Structure → SDKs
   - 添加 IntelliJ Platform Plugin SDK

## 📁 项目结构

```
CodePins/
├── src/main/
│   ├── java/cn/ilikexff/codepins/
│   │   ├── core/                    # 核心功能
│   │   │   ├── PinStateService.java # 图钉状态管理
│   │   │   └── Pin.java             # 图钉数据模型
│   │   ├── ui/                      # 用户界面
│   │   │   ├── PinsToolWindow.java  # 主工具窗口
│   │   │   ├── dialogs/             # 对话框组件
│   │   │   └── panels/              # 面板组件
│   │   ├── actions/                 # IDE 动作
│   │   │   ├── AddPinAction.java    # 添加图钉动作
│   │   │   └── NavigateAction.java  # 导航动作
│   │   ├── settings/                # 设置管理
│   │   │   ├── CodePinsSettings.java
│   │   │   └── LanguageSettings.java
│   │   ├── i18n/                    # 国际化
│   │   │   └── CodePinsBundle.java  # 国际化工具类
│   │   ├── services/                # 服务类
│   │   └── utils/                   # 工具类
│   └── resources/
│       ├── META-INF/
│       │   └── plugin.xml           # 插件配置
│       ├── messages/                # 国际化资源
│       │   ├── CodePinsBundle.properties
│       │   ├── CodePinsBundle_en.properties
│       │   └── CodePinsBundle_zh_CN.properties
│       └── icons/                   # 图标资源
├── src/test/                        # 测试代码
├── docs/                            # 文档
├── build.gradle.kts                 # 构建配置
└── README.md
```

## 📝 开发规范

### 代码风格
- 使用 4 空格缩进
- 类名使用 PascalCase
- 方法和变量使用 camelCase
- 常量使用 UPPER_SNAKE_CASE
- 包名使用小写字母

### 注释规范
```java
/**
 * 图钉数据模型类
 *
 * @author CodePins Team
 * @since 1.0.0
 */
public class Pin {
    /**
     * 图钉的唯一标识符
     */
    private String id;

    /**
     * 创建新的图钉实例
     *
     * @param filePath 文件路径
     * @param lineNumber 行号
     * @param note 备注信息
     * @return 新创建的图钉实例
     */
    public static Pin create(String filePath, int lineNumber, String note) {
        // 实现代码
    }
}
```

### Git 提交规范
使用 Conventional Commits 格式：

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

类型说明：
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

示例：
```
feat(i18n): 添加完整的中英文国际化支持

- 实现动态语言切换功能
- 添加语言设置页面
- 修复资源束缓存问题

Closes #123
```

## 🌐 国际化开发

### 概述
CodePins 使用 Java ResourceBundle 实现国际化，目前支持中文和英文。

### 资源文件结构
```
src/main/resources/messages/
├── CodePinsBundle.properties           # 默认资源（英文）
├── CodePinsBundle_en.properties       # 英文资源
└── CodePinsBundle_zh_CN.properties    # 中文资源
```

### 添加新的国际化文本

#### 1. 定义资源键
使用层次化的键名结构：

```properties
# 按功能分组
button.add=Add
button.edit=Edit
button.delete=Delete

# 对话框
dialog.confirm.title=Confirmation
dialog.confirm.delete=Are you sure you want to delete this pin?

# 通知
notification.success.title=Success
notification.error.title=Error

# 设置
settings.general.title=General Settings
settings.language.title=Language
```

#### 2. 添加到所有语言文件
确保在所有支持的语言文件中添加相同的键：

**CodePinsBundle.properties (英文)**:
```properties
pin.add.success=Pin added successfully
pin.delete.confirm=Are you sure you want to delete this pin?
```

**CodePinsBundle_zh_CN.properties (中文)**:
```properties
pin.add.success=图钉添加成功
pin.delete.confirm=您确定要删除这个图钉吗？
```

#### 3. 在代码中使用
```java
// 简单文本
String message = CodePinsBundle.message("pin.add.success");

// 带参数的文本
String message = CodePinsBundle.message("pins.count", count);
```

### 参数化文本
对于包含动态内容的文本，使用 MessageFormat 参数：

```properties
# 资源文件
notification.pins.exported=Successfully exported {0} pins to {1}
file.info=File: {0}, Size: {1} KB

# 中文
notification.pins.exported=成功导出 {0} 个图钉到 {1}
file.info=文件：{0}，大小：{1} KB
```

```java
// Java 代码
String message = CodePinsBundle.message("notification.pins.exported", count, filePath);
```

### 国际化最佳实践

1. **避免硬编码文本**：
   ```java
   // ❌ 错误
   JButton button = new JButton("Add Pin");

   // ✅ 正确
   JButton button = new JButton(CodePinsBundle.message("button.add.pin"));
   ```

2. **键名命名规范**：
   - 使用描述性的键名
   - 按功能模块分组
   - 保持一致的命名风格

3. **文本编写原则**：
   - 简洁明了
   - 用户友好
   - 保持一致性

## 🎨 UI 开发指南

### UI 框架选择
CodePins 使用传统的 Swing 组件而非 IntelliJ Platform UI DSL，原因：
- 更好的兼容性
- 更灵活的布局控制
- 团队熟悉度高

### 主题适配
确保 UI 组件在亮色和暗色主题下都能正常显示：

```java
public class ThemedPanel extends JPanel {
    public ThemedPanel() {
        // 使用 IDE 主题颜色
        setBackground(UIUtil.getPanelBackground());
        setForeground(UIUtil.getLabelForeground());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 自定义绘制逻辑
    }
}
```

### 响应式设计
```java
public class ResponsiveDialog extends JDialog {
    private void setupLayout() {
        setLayout(new BorderLayout());

        // 使用合适的布局管理器
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 设置组件约束
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        add(contentPanel, BorderLayout.CENTER);
    }
}
```

### 图标使用
```java
// 加载 SVG 图标
Icon icon = IconUtil.loadIcon("/icons/pin-add.svg");
JButton button = new JButton(icon);

// 支持主题切换的图标
Icon themedIcon = IconUtil.getThemedIcon("/icons/pin.svg");
```

## 🧪 测试指南

### 单元测试
```java
public class PinTest {
    @Test
    public void testPinCreation() {
        Pin pin = Pin.create("/path/to/file.java", 10, "Test note");

        assertNotNull(pin.getId());
        assertEquals("/path/to/file.java", pin.getFilePath());
        assertEquals(10, pin.getLineNumber());
        assertEquals("Test note", pin.getNote());
    }
}
```

### 国际化测试
```java
public class I18nTest {
    @Test
    public void testMessageRetrieval() {
        String message = CodePinsBundle.message("button.add");
        assertNotNull(message);
        assertFalse(message.isEmpty());
    }

    @Test
    public void testParameterizedMessage() {
        String message = CodePinsBundle.message("pins.count", 5);
        assertTrue(message.contains("5"));
    }
}
```

### UI 测试
```java
public class UITest {
    @Test
    public void testDialogCreation() {
        SwingUtilities.invokeAndWait(() -> {
            AddPinDialog dialog = new AddPinDialog();
            assertNotNull(dialog);
            assertTrue(dialog.isDisplayable());
        });
    }
}
```

## 🚀 构建和发布

### 本地构建
```bash
# 编译项目
./gradlew build

# 运行测试
./gradlew test

# 构建插件
./gradlew buildPlugin
```

### 调试运行
```bash
# 启动带插件的 IDE 实例
./gradlew runIde
```

### 发布准备
1. 更新版本号（`build.gradle.kts`）
2. 更新 `CHANGELOG.md`
3. 确保所有测试通过
4. 构建最终插件包

### 版本管理
- 使用语义化版本号：`MAJOR.MINOR.PATCH`
- 在 `plugin.xml` 中更新版本信息
- 创建 Git 标签：`git tag v1.2.0`

## 📞 获取帮助

如果在开发过程中遇到问题：

1. **查看文档**：首先查阅本开发指南和 IntelliJ Platform SDK 文档
2. **代码参考**：查看项目中现有的实现
3. **提交 Issue**：在 GitHub 上提交详细的问题描述
4. **参与讨论**：在项目讨论区与其他开发者交流

## 🤝 贡献流程

1. Fork 项目
2. 创建功能分支：`git checkout -b feature/new-feature`
3. 提交更改：`git commit -m 'feat: add new feature'`
4. 推送分支：`git push origin feature/new-feature`
5. 创建 Pull Request

## 🔧 常用开发命令

### Gradle 命令
```bash
# 清理构建
./gradlew clean

# 编译代码
./gradlew compileJava

# 运行所有测试
./gradlew test

# 检查代码质量
./gradlew check

# 构建插件包
./gradlew buildPlugin

# 发布到本地仓库
./gradlew publishToMavenLocal
```

### Git 工作流
```bash
# 创建新功能分支
git checkout -b feature/i18n-support

# 查看状态
git status

# 添加文件
git add .

# 提交更改
git commit -m "feat(i18n): add internationalization support"

# 推送到远程
git push origin feature/i18n-support
```

## 📚 参考资源

### 官方文档
- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/)
- [Swing Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/)
- [Java ResourceBundle](https://docs.oracle.com/javase/8/docs/api/java/util/ResourceBundle.html)

### 项目相关
- [项目 GitHub](https://github.com/08820048/codepins)
- [问题追踪](https://github.com/08820048/codepins/issues)
- [讨论区](https://github.com/08820048/codepins/discussions)

## 🐛 常见问题解决

### 编译问题
**问题**: `Cannot resolve symbol 'ApplicationManager'`
**解决**: 确保正确导入 IntelliJ Platform 依赖

**问题**: 国际化文本显示为键名
**解决**: 检查资源文件路径和键名是否正确

### 运行时问题
**问题**: 插件加载失败
**解决**: 检查 `plugin.xml` 配置和类路径

**问题**: UI 组件在暗色主题下显示异常
**解决**: 使用 `UIUtil` 类获取主题相关的颜色

---

**记住**：良好的代码不仅要功能正确，还要易于理解和维护！💻✨

**Happy Coding!** 🎉
