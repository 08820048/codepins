# 📌 CodePins – 智能代码书签插件

<div align="center">

**现代化的代码书签解决方案，让您的开发工作流更高效**

[![JetBrains Plugin](https://img.shields.io/jetbrains/plugin/v/27300-codepins--code-bookmarks.svg)](https://plugins.jetbrains.com/plugin/27300-codepins--code-bookmarks)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/27300-codepins--code-bookmarks.svg)](https://plugins.jetbrains.com/plugin/27300-codepins--code-bookmarks)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

</div>

## 🎉 完全免费开源！

**CodePins 现在完全免费开源！** 所有功能对所有用户开放，无任何限制。如果这个插件对您有帮助，请考虑：

- ⭐ **给项目点 Star**：[GitHub 仓库](https://github.com/08820048/codepins)
- ☕ **请我们喝咖啡**：[捐赠支持](https://docs.codepins.cn/donate)
- 🤝 **参与贡献**：查看 [贡献指南](CONTRIBUTING.md)

## ✨ 核心功能

### 🔖 无限制的智能书签
- **无限图钉数量**：想添加多少就添加多少
- **无限标签系统**：使用标签组织和分类您的图钉
- **智能备注**：为每个图钉添加详细说明

### 🎯 高效导航
- **快捷键导航**：`Alt+Shift+Left/Right` 在图钉间快速跳转
- **即时预览**：悬停查看代码片段，无需切换文件
- **智能搜索**：按文件名、路径、注释或标签快速查找图钉

### 🎨 现代化界面
- **美观设计**：支持亮色和暗色主题，界面简洁现代
- **拖拽排序**：手动重新排列图钉顺序
- **卡片式布局**：清晰展示代码信息和上下文

### 🔄 数据管理
- **自动同步**：图钉位置随代码编辑自动更新
- **导入导出**：在项目间或团队成员间分享图钉
- **持久化存储**：IDE 重启后图钉信息完整保留

### 📤 分享功能
- **多格式导出**：支持 Markdown、HTML、JSON 等格式
- **社交分享**：一键分享到多个开发者平台
- **代码卡片**：生成精美的代码分享图片

## 🚀 主要操作

### 📌 多种添加图钉方式
- **右键菜单**：右键代码行 → `Add CodePin Here`
- **快捷键**：使用快捷键 `Alt+Shift+P` 添加图钉
- **选择文本**：选中代码后，点击出现的浮动操作按钮添加为图钉
- **注释标记**：在代码中添加特定格式的注释自动创建图钉
  - 单行标记：`@cp` 或 `@cp: 备注内容`（也兼容旧格式 `@pin`）
  - 代码块标记：`@cpb` 或 `@cpb: 备注内容`（也兼容旧格式 `@pin-block`）
  - 指定行号范围：`@cpb1-20: 备注内容`（创建从第1行到第20行的代码块图钉）
  - **快速添加标签**：在注释中使用 `#标签名` 语法快速添加标签
    - 示例：`@cp 重要函数 #重要 #待办`
    - 示例：`@cpb 需要重构的代码 #重构 #技术债`
    - 示例：`@cpb1-20 性能优化点 #性能 #优化`

### 🔄 图钉管理
- ✏️ **编辑图钉**：双击图钉编辑备注和标签（如 #important, #bug, #todo）
- 🔍 **搜索图钉**：按文件路径、备注内容或标签搜索
- 🗑 **删除图钉**：右键图钉 → `Delete` 或批量清空
- 🧭 **快速跳转**：双击图钉跳转到对应代码行
- 🔄 **重新排序**：拖拽图钉调整顺序
- 📋 **团队分享**：导入/导出图钉，方便团队协作

## 📷 功能预览

> 面板 UI | 添加图钉 | 搜索图钉

（图片将在插件发布时更新）

## 🚀 快速开始

1. 安装插件（通过 JetBrains Marketplace 或手动安装 `.zip`）
2. 在任意代码中右键点击 → 📌 Pin This Line（或使用快捷键 `Alt+Shift+P`）
3. 打开左侧工具栏 `CodePins` 查看图钉
4. 双击跳转、右键操作、顶部支持搜索与清空
5. 使用标签（如 #bug）组织和筛选图钉

## 💡 使用技巧

1. **标签管理**
   - 使用有意义的标签（如 #bug、#todo、#important）来组织图钉
   - 标签支持搜索，可以快速找到相关图钉
   - 现在支持无限标签，更好地组织代码

2. **快捷键使用**
   - `Alt+Shift+P`：添加图钉
   - `Alt+Shift+Left/Right`：在图钉间导航
   - `Alt+Shift+T`：切换 CodePins 工具窗口
   - 熟练使用快捷键可以显著提高效率

3. **代码块标记**
   - 选中多行代码后添加图钉，可以标记整个代码块
   - 代码块图钉会显示行号范围，方便定位

4. **搜索技巧**
   - 支持按文件路径、备注内容、标签进行搜索
   - 使用标签前缀（如 #）可以快速筛选特定类型的图钉

5. **团队协作**
   - 使用导入导出功能在团队成员间共享图钉
   - 支持多种导出格式，满足不同需求

## ✨ 贡献者

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-2-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

感谢这些优秀的人 ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/08820048"><img src="https://avatars.githubusercontent.com/u/08820048" width="100px;" alt="08820048"/><br /><sub><b>08820048</b></sub></a><br /><a href="#code-08820048" title="Code">💻</a> <a href="#doc-08820048" title="Documentation">📖</a> <a href="#maintenance-08820048" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/Auroral0810"><img src="https://avatars.githubusercontent.com/u/140379943?s=400&u=ea4e758ddc17a3df7a6b29bc4dc435ba1a35e999&v=4" width="100px;" alt="Auroral0810"/><br /><sub><b>Auroral0810</b></sub></a><br /><a href="#code-Auroral0810" title="Code">💻</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

该项目遵循 [all-contributors](https://github.com/all-contributors/all-contributors) 规范。欢迎任何形式的贡献！

## 🤝 贡献指南

我们欢迎各种形式的贡献！请查看 [贡献指南](CONTRIBUTING.md) 了解详细信息。

### 贡献方式
- 🐛 **报告 Bug**：[GitHub Issues](https://github.com/08820048/codepins/issues)
- 💡 **提出功能建议**：创建 Feature Request
- 📝 **改进文档**：完善使用指南和 API 文档
- 💻 **贡献代码**：修复 Bug 或实现新功能
- 🌍 **帮助翻译**：翻译到更多语言

## 📞 联系我们

如果您有任何问题或建议：

- 📧 **邮箱**：ilikexff@gmail.com
- 🐛 **问题反馈**：[GitHub Issues](https://github.com/08820048/codepins/issues)
- 💬 **讨论交流**：[GitHub Discussions](https://github.com/08820048/codepins/discussions)
- 🏪 **插件页面**：[JetBrains Marketplace](https://plugins.jetbrains.com/plugin/27300-codepins--code-bookmarks)

## 📝 更新日志

### [1.2.1] - 注释标记标签功能 (2025-05-26)

#### 🎉 新功能
* **🏷️ 注释标记标签支持**：现在可以通过注释指令直接添加标签
  * 单行图钉：`@cp 备注内容 #标签名`
  * 代码块图钉：`@cpb 备注内容 #标签名`
  * 带行号范围：`@cpb1-20 备注内容 #标签名`
* **🇸🇳 中文标签支持**：现在支持使用中文标签，如 `#重要` `#待办`

#### 🔧 改进
* **开发效率**：通过注释标记直接添加标签，减少操作步骤
* **用户体验**：简化图钉标签管理流程
* **团队协作**：通过代码注释直接共享带标签的图钉

### [1.2.0] - 便捷图钉功能增强 (2025-05-25)

#### 🎉 新功能
* **👆 选择文本浮动按钮**：选中代码后自动显示添加图钉的浮动按钮
* **💬 注释标记自动识别**：支持通过特定格式的注释自动创建图钉，包括单行标记（`@pin`）和代码块标记（`@pin-block`）

#### 🔧 改进
* **用户体验**：提供更多直观、便捷的图钉添加方式
* **团队协作**：通过注释标记功能增强团队协作体验
* **界面优化**：新增浮动按钮，提升可见性

### [1.1.3] - 完全免费开源 (2024-12-19)

#### 🎉 重大更新
* **🆓 完全免费开源**：CodePins 现在完全免费！所有功能对所有用户开放
* **🚫 移除所有限制**：无图钉数量限制、无标签限制
* **💝 捐赠支持**：添加捐赠链接支持项目持续发展
* **🤝 开源贡献**：欢迎社区参与项目维护和发展

#### 🧹 代码优化
* **简化架构**：移除复杂的许可证验证系统
* **性能提升**：简化代码结构，提高运行效率
* **UI 改进**：更新设置界面，添加开源贡献邀请

#### 📖 文档完善
* **贡献指南**：添加详细的开源贡献指南
* **README 更新**：更新项目描述，反映开源特性

### [1.1.2] - 功能增强与UI优化 (2023-10-20)

#### 🚀 新功能
* **拖放功能**: 支持手动重新排序图钉
* **键盘快捷键**: 添加Alt+Shift+P添加图钉，Alt+Shift+Left/Right导航
* **动画效果**: 悬停项目时的平滑过渡

#### 🔧 改进
* **导出/导入**: 修复功能并改进UI和用户体验
* **图钉列表**: 优化样式，提高亮色主题支持
* **搜索框**: 采用现代设计并支持暗色主题
* **代码预览**: 增强卡片样式，适应不同主题
* **图标更新**: 更新导入/导出功能的图标

### [1.1.1] - 用户体验改进 (2023-09-10)

#### 🚀 新功能
* **标签系统**: 添加标签功能，更好地组织和筛选图钉
* **空状态视图**: 提供友好的引导信息

#### 🔧 改进
* **悬停预览**: 改进功能，解决线程安全问题
* **代码预览UI**: 根据代码长度动态调整高度
* **图钉列表项**: 完全重新设计，采用现代卡片式设计
* **搜索字段**: 增强视觉反馈和用户体验

### [1.0.0] - 初始版本发布 (2023-08-01)

#### 🚀 核心功能
* **图钉管理**: 支持右键添加、搜索、删除与清空图钉
* **代码标记**: 支持代码块和单行代码标记
* **备注功能**: 支持图钉备注与持久化保存
* **快速导航**: 支持工具窗口双击跳转代码位置
