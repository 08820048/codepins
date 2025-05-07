# 📌 CodePins – IntelliJ 插件

轻量级图钉系统插件，支持将任意代码行 “📌” 固定为标记，支持备注、跳转、搜索、删除、清空等功能，适用于临时笔记、错误定位与 TODO 跟踪。

## ✨ 插件亮点功能

- 📌 右键代码行 → 添加图钉
- ✏️ 编辑图钉备注
- 🔍 搜索图钉（路径或备注）
- 🗑 删除图钉 / 🧹 清空全部
- 🧭 双击跳转对应代码行
- 💾 自动持久化保存（项目级）

---

## 📷 截图预览

> 面板 UI | 添加图钉 | 搜索图钉

（建议上传至 GitHub issues 或图床后插入链接）

---

## 🚀 快速开始

1. 安装插件（通过 JetBrains Marketplace 或手动安装 `.zip`）
2. 在任意代码中右键点击 → 📌 Pin This Line
3. 打开左侧工具栏 `CodePins` 查看图钉
4. 双击跳转、右键操作、顶部支持搜索与清空

---

## 📂 项目结构

```
├── plugin.xml               # 插件元信息声明
├── PinsToolWindow.java      # 插件主 UI 窗口
├── PinAction.java           # 编辑器右键添加图钉
├── PinStorage.java          # 本地存储与 XML 序列化
├── PinEntry.java            # 图钉数据结构
```

---

## 📦 插件打包

```bash
./gradlew buildPlugin
```
输出路径：`build/distributions/CodePins-1.0.0.zip`

---

## 🧑‍💻 作者

- GitHub: [@08820048](https://github.com/08820048)
- 插件页面：[JetBrains Plugin Marketplace]()（上传后填写）