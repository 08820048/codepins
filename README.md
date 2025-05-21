# 📌 CodePins – IntelliJ 插件

轻量级图钉系统插件，支持将任意代码行或代码块 "📌" 固定为标记，支持备注、跳转、搜索、删除、清空等功能，适用于临时笔记、错误定位与 TODO 跟踪。

## ✨ 插件亮点功能

- 📌 右键代码行 → 添加图钉（或使用快捷键 `Alt+Shift+P`）
- ✏️ 编辑图钉备注（支持添加标签，如 #important, #bug, #todo）
- 🔍 搜索图钉（按文件路径、备注内容或标签）
- 🗑 删除图钉 / 🧹 清空全部
- 🧭 双击跳转对应代码行（或使用 `Alt+Shift+LEFT/RIGHT` 在图钉间导航）
- 💾 自动持久化保存（项目级）
- 🔄 支持拖放重新排序图钉
- 📋 支持导入/导出图钉，方便团队共享

---

## 📷 截图预览

> 面板 UI | 添加图钉 | 搜索图钉

（建议上传至 GitHub issues 或图床后插入链接）

---

## 🚀 快速开始

1. 安装插件（通过 JetBrains Marketplace 或手动安装 `.zip`）
2. 在任意代码中右键点击 → 📌 Pin This Line（或使用快捷键 `Alt+Shift+P`）
3. 打开左侧工具栏 `CodePins` 查看图钉
4. 双击跳转、右键操作、顶部支持搜索与清空
5. 使用标签（如 #bug）组织和筛选图钉

---

## 🔧 开发要求

- IntelliJ IDEA 2024.1+
- Gradle 8+
- Java 17+
- 插件 SDK：org.jetbrains.intellij 1.17.3

---

## 🛠 构建与运行

```bash
# 运行插件
./gradlew runIde

# 构建插件
./gradlew buildPlugin
```
输出路径：`build/distributions/CodePins-1.1.3.zip`

---

## 📂 项目结构

```
├── plugin.xml               # 插件元信息声明
├── PinsToolWindow.java      # 插件主 UI 窗口
├── PinAction.java           # 编辑器右键添加图钉
├── PinStorage.java          # 本地存储与 XML 序列化
├── PinEntry.java            # 图钉数据结构
├── SharingUtil.java         # 代码分享功能
├── IconPreloader.java       # 图标预加载
```

---

## 🧑‍💻 作者

- GitHub: [@08820048](https://github.com/08820048)
- 插件页面：[JetBrains Plugin Marketplace](https://plugins.jetbrains.com/plugin/22761-codepins--code-bookmarks)
