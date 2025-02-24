# WildNote

相信很多人都会使用笔记软件进行个人知识管理（PKM），我也陆续使用过 Mybase、CyberArticle、TheBrain、OneNote、Evernote、WizNote 作为笔记软件，
其中 WizNote 使用时间最长，Notion 出来后也尝试了一下，并没有当作主要工具。

笔记工具每隔一段时间就会出现新面孔，是跟风用新的，还是继续用旧的，每次都会让人纠结，如何不受工具制约又能有效进行个人知识管理，是我一直在思考的问题。
PKM底层其实是要建立一套知识管理的体系，而不是盲目使用更热门的软件。这方面也有很多方法论，如 GTD、MECE、PARA 等等，
经过这些年的实践，到是形成了一套符合个人习惯的知识管理体系，反观工具反而没有那么重要了，无非是方便记录、跟进、回顾和检索。

所以我重新思考了一下关于笔记管理工具的需求，并采用了一套新的笔记管理方式：

1. 笔记使用文件方式组织，一个文件就是一个笔记，通过文件夹组织笔记，简洁明了，迁移备份都方便；
2. 笔记使用 Markdown 格式编辑，Markdown 易上手，目前也足够普及；
3. 使用 Visual Studio Code 管理笔记文件，打开笔记文件夹后资源管理器面板就相当于笔记目录，对 Markdown 格式友好，检索也方便；
4. 多台常用设备使用 Syncthing 同步笔记文件夹，如果有一台设备一直在线，就相当于有一个笔记服务器；
5. 当常用设备不在身边时，最好能通过互联网访问笔记；

其中前4点好实现，只剩下第5点没有解决，所以开发了这个项目，用于远程访问设备上的笔记。

## 项目说明

`wildnote\wildnote-svc`为后端程序，使用 Java 开发。  
`wildnote\wildnote-web`为前端程序，主要使用 Vue3、Ant Design Vue 和 Cherry Markdown 开发。  

## 如何运行

1. 进入`wildnote\wildnote-web`目录，运行`npm run build`打包前端程序，默认生成在`wildnote\wildnote-web\dist`目录；
2. 进入`wildnote\wildnote-srv`目录，运行`mvn pakage`打包后端程序，默认生成在`wildnote\wildnote-srv\target`目录；
3. 运行`wildnote\run-wildnote.bat 笔记文件目录路径 用户名 密码 [端口:默认8080]`启动程序；
4. 打开浏览器访问`http://localhost:端口`进入系统，如需在公网访问需自行映射到公网；

## 笔记备份

运行`wildnote\bak-wildnote.bat 笔记文件目录路径 备份目录路径`备份笔记目录，可添加计划任务自动执行备份。
