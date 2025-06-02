# WildNote

## 项目说明

`wildnote\wildnote-server`为服务端项目，使用 Java 开发。
`wildnote\wildnote-web`为前端项目，使用 Vue3 开发。  

## 使用说明

1. 前端项目打包，默认生成在`wildnote\wildnote-web\dist`目录。
- 进入`wildnote\wildnote-web`目录；
- 运行`npm install`安装依赖；
- 运行`npm run build`打包前端程序；

2. 后端程序打包，默认生成在`wildnote\wildnote-server\target`目录。
- 进入`wildnote\wildnote-server`目录；
- 运行`mvn pakage`打包后端程序；

3. 启动程序，启动后使用浏览器访问`http://localhost:端口`进行使用。
- 运行`wildnote\wildnote-run.bat`启动程序；
- 脚本支持传入参数覆盖`application.properties`中的配置，参数需用双引号包裹。
  - 如：`wildnote-run.bat "--server.port=8888" "--wildnote.username=admin" "--wildnote.password=password" "--wildnote.note-root-path=D:\note"`

4. 备份脚本，可设定计划任务进行自动备份。
- 运行`wildnote\bak-wildnote.bat 笔记目录路径 备份目录路径`；
