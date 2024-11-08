# WildNote
我的个人笔记管理软件经历了从Mybase、CyberArticle到后来的TheBrain、OneNote、Evernote、WizNote，其中WizNote一直使用至今，也是使用时间最长的笔记管理软件。Notion发布后试用过一段时间，有些不错的功能，但考虑到更换软件带来的大量资料迁移工作，实在没有动力进行更换。  

我对个人知识管理的理解，其实是包含工具和整理两个维度，没有一套整理信息的方法论，工具再强大也没法体现优势。关于信息整理，有很多理论可以参考，如GTD、MECE、PARA等等，在这些理论的基础上，也形成了一套符合个人习惯的信息整理体系，有了体系的支撑，工具反而没有那么重要了，无非是方便记录、跟进、回顾和检索。所以我重新考虑了一下关于笔记管理的思路：  

1. 笔记使用Markdown格式，纯文本功能太弱，Html太臃肿，Markdown易上手，也足够普及  
2. 笔记使用文件方式整理，一个文件就是一个笔记，通过文件夹组织笔记，简洁明了，迁移备份都方便  
3. 使用Visual Studio Code编辑笔记，打开笔记文件夹后资源管理器面板就相当于笔记目录，对Markdown格式友好，检索也方便  
4. 多台常用设备使用Syncthing同步笔记文件夹，最好有一台设备一直在线，我正好具备这样的条件  

完成以上步骤后，只剩下一个问题，当常用设备不在身边时，无法查阅笔记，为解决这个问题而开发了WildNote，用于远程访问设备上的笔记。  

## 项目说明
`wildnote\wildnote-svc`为后端程序，使用Java开发  
`wildnote\wildnote-web`为前端应用，主要使用Vue3和Cherry Markdown开发  

## 如何运行
1. 使用`npm run build`打包`wildnote\wildnote-web`，默认生成在`wildnote\wildnote-web\dist`文件夹  
2. 复制`wildnote\wildnote-svc\src\main\resources\application.properties`到`wildnote`同一级文件夹，改名为`wildnote.properties`，并修改以下配置项
```
#访问wildnote的账户
wildnote.username=admin
#访问wildnote的密码
wildnote.password=admin
#笔记文件夹路径
wildnote.path=C:\\note
```
3. 使用`mvn package`打包`wildnote\wildnote-svc`默认生成在`wildnote\wildnote-svc\target`文件夹  
4. 运行`wildnote\run-wildnote.bat`启动程序  
5. 打开浏览器访问`http://localhost:8080`  
6. 通过内网映射工具将应用映射到公网使用  
