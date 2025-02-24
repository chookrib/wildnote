# WildNote

我的个人笔记管理软件经历了从 Mybase、CyberArticle 到后来的 TheBrain、OneNote、Evernote、WizNote，其中 WizNote 一直使用至今，也是使用时间最长的笔记管理软件。Notion 发布后试用过一段时间，有些不错的功能，但考虑到更换软件带来的大量资料迁移工作，实在没有动力进行更换。

我对个人知识管理的理解，其实是包含工具和整理两个维度，没有一套整理信息的方法论，工具再强大也没法体现优势。关于信息整理，有很多理论可以参考，如 GTD、MECE、PARA 等等，在这些理论的基础上，也形成了一套符合个人习惯的信息整理体系，有了体系的支撑，工具反而没有那么重要了，无非是方便记录、跟进、回顾和检索。所以我重新考虑了一下关于笔记管理的思路：

1. 笔记使用 Markdown 格式，纯文本功能太弱，Html 太臃肿，Markdown 易上手，也足够普及；
2. 笔记使用文件方式整理，一个文件就是一个笔记，通过文件夹组织笔记，简洁明了，迁移备份都方便；
3. 使用 Visual Studio Code 编辑笔记，打开笔记文件夹后资源管理器面板就相当于笔记目录，对 Markdown 格式友好，检索也方便；
4. 多台常用设备使用 Syncthing 同步笔记文件夹，最好有一台设备一直在线，我正好具备这样的条件。

完成以上步骤后，只剩下一个问题，当常用设备不在身边时，无法查阅笔记，为解决这个问题而开发了 WildNote，用于远程访问设备上的笔记。

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

