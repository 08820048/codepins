# 🤝 贡献指南 | Contributing Guide

感谢您对 CodePins 项目的关注！我们非常欢迎各种形式的贡献，无论是代码、文档、测试还是反馈建议。

## 📋 目录

- [如何贡献](#如何贡献)
- [开发环境设置](#开发环境设置)
- [代码贡献流程](#代码贡献流程)
- [代码规范](#代码规范)
- [问题报告](#问题报告)
- [功能建议](#功能建议)
- [文档贡献](#文档贡献)
- [社区准则](#社区准则)

## 🚀 如何贡献

### 1. 🐛 报告 Bug
- 在 [Issues](https://github.com/08820048/codepins/issues) 页面创建新的 Bug 报告
- 使用 Bug 报告模板，提供详细的复现步骤
- 包含您的环境信息（IDE 版本、操作系统等）

### 2. 💡 提出功能建议
- 在 [Issues](https://github.com/08820048/codepins/issues) 页面创建功能请求
- 详细描述您希望的功能和使用场景
- 解释为什么这个功能对用户有价值

### 3. 📝 改进文档
- 修正文档中的错误或不清楚的地方
- 添加使用示例和最佳实践
- 翻译文档到其他语言

### 4. 💻 贡献代码
- 修复 Bug
- 实现新功能
- 优化性能
- 重构代码

## 🛠️ 开发环境设置

### 前置要求
- **JDK 17** 或更高版本
- **IntelliJ IDEA 2024.1** 或更高版本
- **Git**

### 设置步骤

1. **Fork 项目**
   ```bash
   # 在 GitHub 上 Fork 项目到您的账户
   ```

2. **克隆代码**
   ```bash
   git clone https://github.com/YOUR_USERNAME/codepins.git
   cd codepins
   ```

3. **导入项目**
   - 使用 IntelliJ IDEA 打开项目
   - 等待 Gradle 同步完成

4. **运行插件**
   ```bash
   ./gradlew runIde
   ```

5. **构建插件**
   ```bash
   ./gradlew build
   ```

## 🔄 代码贡献流程

### 1. 创建分支
```bash
git checkout -b feature/your-feature-name
# 或
git checkout -b fix/your-bug-fix
```

### 2. 开发和测试
- 编写代码
- 添加或更新测试
- 确保所有测试通过
- 测试插件功能

### 3. 提交代码
```bash
git add .
git commit -m "feat: 添加新功能描述"
# 或
git commit -m "fix: 修复某个问题"
```

### 4. 推送分支
```bash
git push origin feature/your-feature-name
```

### 5. 创建 Pull Request
- 在 GitHub 上创建 Pull Request
- 填写详细的描述
- 关联相关的 Issue

## 📏 代码规范

### Java 代码规范
- 使用 4 个空格缩进
- 类名使用 PascalCase
- 方法名和变量名使用 camelCase
- 常量使用 UPPER_SNAKE_CASE
- 添加适当的注释和 JavaDoc

### 提交信息规范
使用 [Conventional Commits](https://www.conventionalcommits.org/) 格式：

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

**类型 (type):**
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

**示例:**
```
feat(ui): 添加图钉拖拽排序功能
fix(storage): 修复图钉数据丢失问题
docs(readme): 更新安装说明
```

## 🐛 问题报告

报告 Bug 时，请包含以下信息：

### 环境信息
- IntelliJ IDEA 版本
- CodePins 插件版本
- 操作系统和版本
- JDK 版本

### 问题描述
- 简洁明确的问题标题
- 详细的问题描述
- 预期行为 vs 实际行为
- 复现步骤
- 相关截图或日志

### Bug 报告模板
```markdown
## 环境信息
- IDE: IntelliJ IDEA 2024.1
- 插件版本: 1.1.3
- 操作系统: macOS 14.0
- JDK: 17.0.8

## 问题描述
简洁描述遇到的问题...

## 复现步骤
1. 打开 CodePins 工具窗口
2. 添加一个图钉
3. 执行某个操作...

## 预期行为
应该发生什么...

## 实际行为
实际发生了什么...

## 截图
如果适用，添加截图来帮助解释问题
```

## 💡 功能建议

提出功能建议时，请考虑：

- **用户价值**: 这个功能对用户有什么帮助？
- **使用场景**: 在什么情况下会用到这个功能？
- **实现复杂度**: 是否容易实现？
- **兼容性**: 是否与现有功能冲突？

### 功能建议模板
```markdown
## 功能描述
简洁描述建议的功能...

## 使用场景
描述什么时候会用到这个功能...

## 预期行为
详细描述功能应该如何工作...

## 替代方案
是否有其他解决方案？

## 附加信息
其他相关信息...
```

## 📖 文档贡献

我们欢迎以下文档贡献：

- 修正拼写和语法错误
- 改进文档结构和可读性
- 添加使用示例和教程
- 翻译文档到其他语言
- 更新过时的信息

## 🌟 社区准则

为了维护一个友好、包容的社区环境，请遵守以下准则：

### ✅ 应该做的
- 保持友善和尊重
- 欢迎新贡献者
- 提供建设性的反馈
- 专注于对项目最有利的事情
- 承认他人的贡献

### ❌ 不应该做的
- 使用不当语言或图像
- 人身攻击或政治攻击
- 公开或私下骚扰
- 发布他人的私人信息
- 其他不专业的行为

## 🎯 开始贡献

准备好开始贡献了吗？

1. 🔍 浏览 [Issues](https://github.com/08820048/codepins/issues) 找到感兴趣的任务
2. 💬 在 Issue 中留言表示您想要处理
3. 🍴 Fork 项目并创建分支
4. 💻 开始编码
5. 📤 提交 Pull Request

## 📞 联系我们

如果您有任何问题或需要帮助：

- 📧 邮箱: ilikexff@gmail.com
- 🐛 Issues: [GitHub Issues](https://github.com/08820048/codepins/issues)
- 💬 讨论: [GitHub Discussions](https://github.com/08820048/codepins/discussions)

## 🙏 致谢

感谢所有为 CodePins 项目做出贡献的开发者！您的贡献让这个项目变得更好。

---

**再次感谢您对 CodePins 的关注和支持！** 🎉
