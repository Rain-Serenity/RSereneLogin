# RSereneLogin

基于 `LiLogin-Velocity-1.1.1` 重构的 Velocity 登录插件。

- **Group**: `com.rserene.chosen.server`
- **Version**: `1.0-SNAPSHOT`
- **Author**: `Chosen_1st`

## 模块说明

- `common`：公共 API、加载器、流程工具。
- `core`：登录核心逻辑，构建产物 `RSereneLogin-Core.JarFile`。
- `velocity-injector`：Velocity 登录流程注入逻辑，构建产物 `RSereneLogin-Velocity-Injector.JarFile`。
- `velocity`：Velocity 插件入口与平台适配层，最终插件 JAR 由此模块产出。

## 构建

```powershell
./gradlew build
```

构建产物：`velocity/build/libs/RSereneLogin-Velocity-1.0-SNAPSHOT.jar`

## 注意事项

- 插件引用了 Velocity 内部实现类，升级 Velocity 版本时需同步适配。
- 运行时依赖由加载器根据 `libraries`、`.digests`、`repositories` 自动下载到插件数据目录。
- 源码结构与原始源码不同。
