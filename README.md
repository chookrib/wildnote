# WildNote

## 项目说明

`wildnote\wildnote-server`为后端项目，使用 Java 开发  
`wildnote\wildnote-web`为前端项目，使用 Vite + Vue 3 + Javascript 开发  
`wildnote\wildnote-web-ts`为前端项目升级 ts 版本，使用 Vite + Vue 3 + TypeScript 开发  

## 使用说明

1. 打包（后端项目）
- 进入`wildnote\wildnote-server`目录
- 运行`mvn pakage`打包后端程序
- 默认生成在`wildnote\wildnote-server\target`目录

2. 打包（前端项目）
- 进入`wildnote\wildnote-web-ts`目录
- 运行`npm install`安装依赖
- 运行`npm run build`打包前端程序
- 默认生成在`wildnote\wildnote-web-ts\dist`目录

3. 启动
- 运行`wildnote\wildnote-run.bat`启动程序
- 脚本支持传入自定义配置文件路径参数
  - 如：`wildnote-run.bat "--spring.config.additional-location=D:\application-prod.yaml"`
- 脚本支持传入参数覆盖`application.yaml`中的配置项，参数需用双引号包裹，参数最多支持9个
  - 如：`wildnote-run.bat "--server.port=8888" "--wildnote.username=admin" "--wildnote.password=admin" "--wildnote.note-root-path=D:\note"`

4. 访问
- 使用浏览器访问`http://localhost:端口`进行使用，默认端口为`8080`
- 默认用户名为`admin`，默认密码为`admin`

5. 备份
- 提供`wildnote\bak-wildnote.bat`备份脚本，可进行笔记目录备份
  - 运行：`wildnote\bak-wildnote.bat 笔记目录路径 备份目录路径`
- 可设定计划任务进行自动备份
