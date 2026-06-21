# GlassAI

Liquid Glass 风格的全能 AI 聊天 App（Kotlin + Jetpack Compose）。
通用 OpenAI 兼容协议（/v1/chat/completions），API Key 本地存储，无后端中转。

## 手机用户如何产出 APK（GitHub Actions 云编译）

1. 在 GitHub 建一个仓库（建议 Private），名字比如 GlassAI。
2. 把本项目整个目录上传到该仓库的 main 分支。
3. 仓库里已有 .github/workflows/build.yml，推送后会自动触发构建；也可在 Actions 页面手动点 Run workflow。
4. 构建完成后进入该次运行，在 Artifacts 里下载 app-debug.apk 安装。

工作流会自动安装 JDK、Android SDK，生成 Gradle Wrapper，再用 ./gradlew assembleDebug 构建，仓库里无需放任何二进制文件。

## 配置 Providers

设置页可切换/编辑以下预设，填入 API Key 与模型名即可：

- OpenAI: https://api.openai.com/v1  模型示例：gpt-4o-mini
- DeepSeek: https://api.deepseek.com/v1  模型：deepseek-chat
- Moonshot: https://api.moonshot.cn/v1  模型：moonshot-v1-8k
- Qwen: https://dashscope.aliyuncs.com/compatible-mode/v1  模型：qwen-plus
- Ollama（模拟器连宿主机）: http://10.0.2.2:11434/v1  模型：llama3.1

## 安全提示

- API Key 存在本机 DataStore，仅用于直连你配置的 Provider。
- Demo 级用法，请勿把含 Key 的 APK 公开发布。
