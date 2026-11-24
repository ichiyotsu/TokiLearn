# TokiLearn

上海专升本《机械设计基础》学习软件（Android）。0.1.0 纵切版：考点、刷题（含画图题与刷题工作台）、错题本、我的/设置。全部本地，无服务器；Gemini Key 只存本机 DataStore，请求直连 Google Gemini API。

## 导航（两级）

TokiLearn 是多学科学习软件，不止机械设计基础一门：

- 主页底栏：首页 / 我的。首页是学科列表（0.1.0 先只有机械设计基础一门）+ 继续学习入口 + 跨学科错题汇总（有错题时才显示）。
- 进入学科后的底栏：考点 / 刷题 / 错题 / 我的。

所有数据层（考点、题目、做题进度、错题本、Gemini 上下文）一律按 `subjectId` 分键存储；后续加学科只需在 `Subjects` 里加一份数据，不用改导航和页面。

## 工程

- `android/`：Android 工程（Gradle 9.6 / AGP 9.4.1 / Kotlin 2.4.21 / Compose 1.12.1 / Material3 Expressive 1.5.0-beta02）
- 包名 `com.tokilearn.app`，minSdk 36 / targetSdk 36 / compileSdk 37，versionName 0.1.0 / versionCode 1
- 设计：TokiUI 0.2.0-b1 语义规范（快照在 `android/tokiui/`，与 TokiMusic 同款）+ MD3E；默认深色，可切浅色

## 构建

```sh
cd android
export JAVA_HOME=~/workspace/tools/jdk17
export ANDROID_HOME=~/workspace/tools/android-sdk
./gradlew assembleDebug --max-workers=2
```

一次只能一个 Gradle 构建；构建前按 `~/TOOLS.md` 检查 Hatch Egress CA 指纹与 truststore、代理密码（当次 exec 的 env 现取，绝不打印）。

## 签名

Release 签名走环境变量 `TOKILEARN_KEYSTORE` / `TOKILEARN_STORE_PASSWORD` / `TOKILEARN_KEY_ALIAS` / `TOKILEARN_KEY_PASSWORD`；无环境变量时回退 debug key。Keystore 绝不进仓库。

## 题目来源

0.1.0 内置 13 道真实题目，全部出自杨可桢《机械设计基础》（第 7 版）笔记和课后习题详解（名校考研真题详解，第 1、2 章）与 TaskPro 笔记中的公式（F=3n−2PL−PH、d=mz、db=d·cosα），其余章节与《专升本必刷 2000 题》均为“待导入”（2000 题 OCR 进行中）。题目绝不编造。
