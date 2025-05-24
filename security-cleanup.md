# 🛡️ 安全清理指南

## 🚨 发现的敏感信息

在转换为开源项目的过程中，我们发现了以下潜在的敏感信息：

### 1. 加密密钥 (已处理)
- **文件**: `src/main/java/cn/ilikexff/codepins/utils/StringEncryptor.java`
- **问题**: 硬编码的AES加密密钥 `"***REMOVED_SECRET_KEY***"`
- **状态**: ✅ 已替换为占位符并标记为废弃

### 2. 许可证验证逻辑 (已清理)
- **文件**: `src/main/java/cn/ilikexff/codepins/services/LicenseService.java`
- **问题**: 包含付费验证逻辑
- **状态**: ✅ 已简化并移除敏感逻辑

## 🔧 立即行动方案

### 方案一：Git 历史清理 (推荐)

如果您担心Git历史中的敏感信息，可以采用以下方法：

#### 1. 使用 git-filter-repo 清理特定文件
```bash
# 安装 git-filter-repo
pip install git-filter-repo

# 清理特定文件的历史记录
git filter-repo --path src/main/java/cn/ilikexff/codepins/utils/StringEncryptor.java --invert-paths

# 或者清理特定内容
git filter-repo --replace-text <(echo '***REMOVED_SECRET_KEY***==>REDACTED_KEY')
```

#### 2. 使用 BFG Repo-Cleaner
```bash
# 下载 BFG
wget https://repo1.maven.org/maven2/com/madgag/bfg/1.14.0/bfg-1.14.0.jar

# 清理敏感字符串
java -jar bfg-1.14.0.jar --replace-text replacements.txt

# replacements.txt 内容:
# ***REMOVED_SECRET_KEY***==>***REMOVED***
```

#### 3. 强制推送清理后的历史
```bash
git reflog expire --expire=now --all
git gc --prune=now --aggressive
git push --force-with-lease origin main
```

### 方案二：创建新仓库 (最安全)

如果敏感信息较多，建议创建全新的仓库：

```bash
# 1. 创建新的空仓库
git init codepins-clean
cd codepins-clean

# 2. 复制当前清理后的代码
cp -r ../codepins/* .

# 3. 初始化新的Git历史
git add .
git commit -m "Initial commit - CodePins open source version"

# 4. 推送到新仓库
git remote add origin https://github.com/08820048/codepins.git
git push -u origin main
```

## 🔍 敏感信息检查清单

### ✅ 已检查和清理的项目
- [x] 加密密钥和算法
- [x] 许可证验证逻辑
- [x] 产品密钥和标识符
- [x] 付费功能验证代码
- [x] 升级对话框和付费提示

### 🔍 需要继续检查的项目
- [ ] 配置文件中的敏感信息
- [ ] 测试文件中的示例数据
- [ ] 构建脚本中的密钥
- [ ] 文档中的敏感信息
- [ ] 注释中的内部信息

## 🛠️ 自动化检查工具

### 使用 git-secrets
```bash
# 安装 git-secrets
git clone https://github.com/awslabs/git-secrets.git
cd git-secrets && make install

# 配置检查规则
git secrets --register-aws
git secrets --install

# 扫描历史记录
git secrets --scan-history
```

### 使用 truffleHog
```bash
# 安装 truffleHog
pip install truffleHog

# 扫描仓库
truffleHog --regex --entropy=False .
```

## 📋 最佳实践

### 1. 环境变量使用
```java
// 好的做法
String apiKey = System.getenv("API_KEY");
String secret = System.getProperty("app.secret");

// 避免的做法
String apiKey = "hardcoded-key-123";
```

### 2. 配置文件分离
```
# 公开配置
config/
├── application.properties      # 公开配置
├── application-dev.properties  # 开发环境
└── .gitignore                 # 忽略敏感配置

# 敏感配置 (不提交到Git)
config/
├── secrets.properties         # 添加到 .gitignore
└── local.properties          # 添加到 .gitignore
```

### 3. .gitignore 更新
```gitignore
# 敏感配置文件
*.key
*.pem
secrets.properties
local.properties
.env

# IDE 敏感文件
.idea/workspace.xml
.idea/tasks.xml
```

## 🚀 后续建议

### 1. 立即行动
1. ✅ 替换所有硬编码的敏感信息
2. 🔄 考虑清理Git历史记录
3. 📝 更新 .gitignore 文件
4. 🔍 运行自动化扫描工具

### 2. 长期维护
1. 🛡️ 设置 pre-commit hooks 防止敏感信息提交
2. 🔍 定期运行安全扫描
3. 📚 团队安全培训
4. 📋 建立代码审查流程

## 📞 如需帮助

如果您需要更详细的帮助或有特殊情况：

1. 📧 联系安全专家
2. 🔍 使用专业的代码扫描服务
3. 📚 参考 OWASP 安全指南
4. 🛡️ 考虑使用密钥管理服务

---

**重要提醒**: 一旦代码公开，就应该假设所有历史记录都可能被访问。最安全的做法是创建全新的仓库。
