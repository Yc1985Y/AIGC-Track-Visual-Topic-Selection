# 快速入门指南

## 项目概览

Visual Semantic Action Agent (VSAA) 是一个基于 Android 的多模态 AI 智能体系统，集成了视觉识别、语言理解、语音交互等功能。

---

## 📋 系统要求

### 开发环境

- **Android Studio**: 2023.1.0 或更高版本
- **JDK**: 17 或更高版本
- **Gradle**: 8.0 或更高版本
- **Android SDK**: 34+
- **最低 API 级别**: Android 10 (API 29)

### 硬件要求

- **RAM**: 8GB+
- **磁盘**: 10GB+ 可用空间
- **CPU**: 4核或更多

---

## 🚀 快速启动

### 步骤 1: 克隆或下载项目

```bash
# 如果使用 Git
git clone <project-url>
cd VisualSemanticAgent

# 或下载 ZIP 文件并解压
unzip VisualSemanticAgent.zip
cd VisualSemanticAgent
```

### 步骤 2: 在 Android Studio 中打开项目

```bash
# 使用命令行
open -a "Android Studio" .

# 或通过 Android Studio GUI
# File > Open > 选择项目目录
```

### 步骤 3: 等待 Gradle 同步

首次打开时，Android Studio 会自动同步 Gradle 文件：

- ✓ 下载依赖库
- ✓ 编译项目
- ✓ 索引源代码

这可能需要 3-5 分钟，具体取决于网络速度。

### 步骤 4: 配置 API 密钥

编辑 `local.properties` 文件（如果不存在则创建）：

```properties
# local.properties
sdk.dir=/Users/xxx/Library/Android/sdk
vsa.api_key=your-api-key-here
vsa.model_name=gpt-4-vision
```

或在 `build.gradle` 中配置：

```gradle
android {
    defaultConfig {
        buildConfigField("String", "API_KEY", "\"${project.property("vsa.api_key")}\"")
    }
}
```

### 步骤 5: 连接设备或启动模拟器

**使用真实设备**:

```bash
# 通过 USB 连接 Android 设备
# 在设备上启用开发者模式和 USB 调试
adb devices  # 检查连接
```

**使用模拟器**:

```bash
# 在 Android Studio 中打开 AVD Manager
# Device Manager > Create Device > 选择配置 > Finish

# 或使用命令行
emulator -avd Pixel_7_API_34
```

### 步骤 6: 运行应用

**方式 1: 使用 Android Studio**

1. 选择 Run > Run 'app'
2. 或按 `Shift + F10` (Windows/Linux) 或 `Control + R` (Mac)

**方式 2: 使用命令行**

```bash
# 构建并安装
./gradlew installDebug

# 或直接运行
./gradlew run
```

### 步骤 7: 授予必要的权限

应用首次运行时会请求以下权限：

- 📷 摄像头权限
- 🎤 麦克风权限
- 💾 存储权限

点击"允许"即可。

---

## 🎯 基本使用

### 主界面导航

```
MainActivity
├── 摄像头预览区域
│   └── 显示实时视频流
├── 控制按钮
│   ├── 拍照按钮
│   ├── 闪光灯按钮
│   └── 切换摄像头
├── 语音输入按钮
│   └── 按住说话
└── 结果显示区域
    ├── 识别的对象列表
    ├── 图像描述
    └── 执行结果
```

### 常用操作

#### 1. 拍照并分析

```
1. 启动应用
2. 对准要分析的内容
3. 点击拍照按钮
4. 等待 AI 分析（通常 1-2 秒）
5. 查看结果
```

#### 2. 语音输入

```
1. 点击麦克风按钮
2. 说出命令，例如：
   - "这是什么?"
   - "识别所有物体"
   - "打开闪光灯"
3. 松开按钮
4. 等待系统处理和回复
```

#### 3. 查看详细结果

```
结果包括：
- 检测到的对象及其置信度
- 图像的自然语言描述
- 建议的操作
```

---

## 📚 项目文档

### 核心文档

| 文档                                           | 内容               | 适用人群           |
| ---------------------------------------------- | ------------------ | ------------------ |
| [ARCHITECTURE.md](./ARCHITECTURE.md)           | 系统架构设计       | 架构师、高级开发者 |
| [API_GUIDE.md](./API_GUIDE.md)                 | API/设计参考       | 开发者、集成人员   |
| [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md) | 开发规范和最佳实践 | 开发者、测试人员   |
| [PROJECT_SUMMARY.md](./PROJECT_SUMMARY.md)     | 历史阶段总结       | 项目经理、决策者   |

### 从哪里开始

**我是新开发者**

1. 先读 [README.md](./README.md)
2. 然后读 [ARCHITECTURE.md](./ARCHITECTURE.md) 第 1-3 章
3. 查阅 [API_GUIDE.md](./API_GUIDE.md) 学习如何使用

**我要集成某个模块**

1. 查找 [API_GUIDE.md](./API_GUIDE.md) 中的相应模块
2. 参考代码示例
3. 在 [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md) 中查找最佳实践

**我要修复问题或优化性能**

1. 查阅 [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md) 中的调试部分
2. 参考性能优化建议
3. 运行测试验证修复

---

## 🔧 常见配置

### 修改 API 端点

编辑 `VLMModels.kt`:

```kotlin
object VLMConfig {
    const val API_ENDPOINT = "https://api.example.com/v1"
    const val DEFAULT_MODEL = "gpt-4-vision"
    const val TIMEOUT_MS = 30000L
}
```

### 调整相机设置

编辑 `CameraManager.kt`:

```kotlin
val cameraManager = CameraManager()
cameraManager.setResolution(1920, 1080)  // 设置分辨率
cameraManager.setFocusMode(FocusMode.AUTO)  // 自动对焦
cameraManager.setZoom(1.5f)  // 1.5 倍缩放
```

### 配置语言

编辑 `VoiceRecognitionManager.kt` 和 `TextToSpeechManager.kt`:

```kotlin
voiceManager.setLanguage("zh-CN")  // 中文
voiceManager.setLanguage("en-US")  // 英文

ttsManager.setLanguage("zh-CN")
ttsManager.setSpeechRate(1.2f)  // 1.2 倍速度
```

### 启用调试模式

在 `build.gradle` 中:

```gradle
buildTypes {
    debug {
        debuggable true
        minifyEnabled false
    }
}
```

---

## 🧪 运行测试

### 单元测试

```bash
# 运行所有单元测试
./gradlew test

# 运行特定测试类
./gradlew test --tests VLMNetworkClientTest

# 生成覆盖率报告
./gradlew test jacocoTestReport
```

### 集成测试

```bash
# 运行所有 Android 测试
./gradlew connectedAndroidTest

# 运行特定测试
./gradlew connectedAndroidTest -k "CameraIntegration"
```

### 性能测试

```bash
# 运行性能基准测试
./gradlew benchmark
```

---

## 🐛 调试技巧

### 查看日志

```bash
# 实时查看日志
adb logcat | grep VSA

# 清除日志并从头开始
adb logcat -c
adb logcat | grep VSA

# 保存日志到文件
adb logcat > debug.log
```

### 使用 Android Studio Debugger

```
Run > Debug 'app'
```

在代码中设置断点：

- 点击代码行号左侧
- 应用会在该行暂停执行
- 查看变量值、调用栈等

### 使用 Android Profiler

```
View > Tool Windows > Profiler
```

监控：

- CPU 使用率
- 内存占用
- 网络流量
- 电池消耗

---

## 📱 在不同设备上测试

### 测试清单

- [ ] Pixel 手机 (标准)
- [ ] Samsung 手机 (自定义 UI)
- [ ] 平板设备 (大屏幕)
- [ ] 低端设备 (性能)
- [ ] 不同 Android 版本 (兼容性)

### 创建虚拟设备配置

```bash
# 通过命令行创建 AVD
android create avd -n Pixel7 -t android-34 -c 512M

# 启动特定 AVD
emulator -avd Pixel7

# 查看所有可用 AVD
emulator -list-avds
```

---

## 🔐 安全设置

### API 密钥管理

**不要**将 API 密钥提交到版本控制：

```bash
# 添加到 .gitignore
echo "local.properties" >> .gitignore
echo "*.key" >> .gitignore
```

### 权限最小化

在 `AndroidManifest.xml` 中只声明必要的权限：

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 📊 构建和发布

### 构建 Debug 版本

```bash
./gradlew assembleDebug
# 输出: app/build/outputs/apk/debug/app-debug.apk
```

### 构建 Release 版本

```bash
# 首先创建签名密钥
keytool -genkey -v -keystore my-release-key.keystore \
  -alias my-key -keyalg RSA -keysize 2048 -validity 10000

# 配置签名
# 在 build.gradle 中添加 signingConfigs

./gradlew assembleRelease
# 输出: app/build/outputs/apk/release/app-release.apk
```

### 创建 App Bundle

```bash
./gradlew bundleRelease
# 输出: app/build/outputs/bundle/release/app-release.aab
```

---

## ⚠️ 常见问题排查

### 问题: Gradle 同步失败

**解决方案**:

1. 检查网络连接
2. 清除 Gradle 缓存: `./gradlew --stop && rm -rf ~/.gradle`
3. 更新 Gradle: `./gradlew wrapper --gradle-version 8.1.1`
4. 在 Android Studio 中: File > Invalidate Caches > Invalidate and Restart

### 问题: 摄像头无法打开

**解决方案**:

1. 检查权限已授予
2. 确保没有其他应用占用摄像头
3. 查看日志了解详细错误信息
4. 在 AndroidManifest.xml 中检查权限声明

### 问题: API 请求超时

**解决方案**:

1. 检查网络连接
2. 增加超时时间: `VLMNetworkClient.setConfig(timeout = 60000L)`
3. 检查 API 服务器状态
4. 检查 API 密钥是否有效

### 问题: 应用崩溃

**解决方案**:

1. 查看 Logcat 中的崩溃日志
2. 检查堆栈跟踪
3. 设置断点并使用 Debugger
4. 提交 Issue 并附加崩溃日志

---

## 🆘 获取帮助

### 资源

1. 📖 项目文档 - 开始使用前必读
2. 🔍 API_GUIDE - 具体功能使用方法
3. 🛠️ DEVELOPMENT_GUIDE - 开发规范和最佳实践
4. 💻 源代码示例 - 主要类中的注释

### 联系方式

- 📧 Email: support@example.com
- 🐙 GitHub Issues: 报告问题和建议
- 💬 讨论区: 技术讨论和经验分享

---

## 📈 下一步

完成入门后，建议：

1. **深入理解架构** - 阅读 [ARCHITECTURE.md](./ARCHITECTURE.md)
2. **学习 API** - 研究 [API_GUIDE.md](./API_GUIDE.md) 的示例
3. **遵循规范** - 参考 [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md)
4. **编写测试** - 为新功能添加单元测试
5. **性能优化** - 使用 Profiler 优化应用

---

## 📝 变更日志

### v1.0.0 (2026-05-01)

- ✨ 初始发布
- 📦 核心功能实现
- 📚 完整文档编写

---

**祝您开发愉快！** 🎉

有问题？查看 [常见问题](#-常见问题排查) 部分或提交 Issue。
