# RW-HPS
![Github stars](https://img.shields.io/github/stars/RW-HPS/RW-HPS.svg)
![](https://github.com/RW-HPS/RW-HPS/actions/workflows/gradle.yml/badge.svg?branch=master)
[![](https://jitpack.io/v/RW-HPS/RW-HPS.svg)](https://jitpack.io/#RW-HPS/RW-HPS)

### **目前处于Kt重写状态 欢迎进行重写并提交Pr**

## 这是什么
这个一个基于铁锈战争的服务器,你可以在任何可以运行Java11的设备上运行它
### 它可以做什么
你可以在这台服务器上进行许许多多有趣的事情,你在原版的游戏上能干的事情它大部分都能干  
你在原版游戏上干不了的东西,它也可以干  
你还可以写出Plugin来扩展服务器的功能  
你也可以自定义你的协议实现

## 声明
### 一切开发旨在学习，请勿用于非法用途
- RW-HPS 是完全免费且开放源代码的软件，仅供学习和娱乐用途使用
- RW-HPS 不会通过任何方式强制收取费用，或对使用者提出物质条件

## 介绍
第三方铁锈战争服务器  
这是一个基于Netty的服务器  
旨在作为一个高性能 高可用的服务器 为玩家提供更好的游戏体验

### 许可证
本服务端遵守  
GNU General Public License v3.0

#### 不会支持的游戏协议
- 列表相关，如ADD List, Update List, Remove List  
- 金钱相关，如增值服务    

**一切开发旨在学习，请勿用于非法用途**  

`RW-HPS` 采用 `GPLv3` 协议开源。为了整个社区的良性发展，我们**强烈建议**您做到以下几点：

- **间接接触（包括但不限于使用 `Http API` 或 跨进程技术 以及字节码修改）到 `RW-HPS` 的软件使用 `GPLv3` 开源**
- **个人使用请加入服务端使用自RW-HPS**
- **不鼓励，不支持一切商业使用**

### 衍生软件需声明引用

- 若引用 RW-HPS 发布的软件包而不修改 RW-HPS，则衍生项目需在描述的任意部位提及使用 RW-HPS。
- 若修改 RW-HPS 源代码再发布，**或参考 RW-HPS 内部实现发布另一个项目**，则衍生项目必须在**文章首部**或 'RW-HPS' 相关内容**首次出现**的位置**明确声明**来源于本仓库 (`https://github.com/RW-HPS/RW-HPS`) 不得扭曲或隐藏免费且开源的事实。


## 协议支持

<details>
  <summary>支持的协议列表</summary>  

**消息相关**
- 团队消息
- 群发消息
- 地图位置
- 禁言

**游戏相关**
- 单位移动
- 游戏重连
- 自定义地图

**扩展功能**
- 加载插件
- 玩家跳转服务器(TODO)
- 地图生成单位

**其他**
- BanUUID
- BanIP

</details>

## 开始
- 开发文档: [docs](https://github.com/RW-HPS/RW-HPS/wiki)
- 更新日志: [release](https://github.com/RW-HPS/RW-HPS/releases)
- 开发计划: [milestones](https://github.com/RW-HPS/RW-HPS/milestones)
- 讨论:
  > 在 GitHub Discussions 提出的问题会收到回复, 也欢迎分享你基于项目的新想法  
  > 邮件联系: dr@der.kim  
  > Tencent QQ Group: [901913920](https://qm.qq.com/cgi-bin/qm/qr?k=qhJ6ekYF9pD9jO6j8H2rZw8ePAVypoU0&jump_from=webapi) (AGPLv3)    
  > <del>Telegram Group: [RW-HPS](https://t.me/RW_HPS) </del>  
  > Discord: [RW-HPS](https://discord.gg/VwwxJhVG64)
- 镜像:
  > [Github](https://github.com/RW-HPS/RW-HPS)  
  > [Gitee](https://gitee.com/derdct/RW-HPS)  

### 我们被谁使用
- Tiexiu.xyz
    - (new) [rw.tiexiu.xyz](https://rw.tiexiu.xyz)  
    - (old) [sfe.tiexiu.xyz](https://sfe.tiexiu.xyz)  
- 非官方RELAYCN
    - [RelayCN-Unofficial] relay.der.kim

## 运行配置

| 配置 		| CPU             | 内存 	| 系统 			| 硬盘大小 	| Java      |
|:--- 		|:---             |:---     |:---           |:---       |:---       |
| 建议配置 	| ARMv7 Processor rev 5 +   | 128MB      | Linux~  | 64MB HDD  | Java 11   |
| 最低配置 	| ARMv7 Processor rev 5  | 64M      | Linux~  | 64M HDD  | Java 11   |

## 服务器命令列表
<details>
  <summary>服务器命令列表</summary>  

| 命令 					 | 参数 																						 | 信息 									 |
|:--- 					 |:--- 																						 |:--- 									 |
| help 		              |                                                  										 | 获取帮助 		 |
| start                  |                                                  										 | 开启服务器 						 |
| say 		            | &lt;文字&gt;                                                  								| 用Server的名义发消息 				 |
| giveadmin              | &lt;玩家位置&gt; 																            | 转移Admin       		         |
| restart 			      | 																						| 重启服务器 				  |
| gameover 				 |  	                                                                                    | 重新开始游戏               				 |
| clearbanip          		 |                                                  										 | 清理被ban的ip               	 |
| admin          		 |&lt;add/remove&gt; &lt;PlayerSite&gt;                                                  										 | 设置admin               			 |
| clearbanuuid          		 |                               	   											 | 清除被ban的uuid               			 |
| clearbanall          		 |                               	   											 | 清空ban               			 |
| ban          		 | &lt;PlayerSerialNumber&gt;                                 	   											 | 禁止某人               			 |
| mute          		 |  &lt;PlayerSerialNumber&gt;  &lt;Time/s&gt;                             	   											 | 禁言某人               			 |
| kick          		 |  &lt;PlayerSerialNumber&gt;  &lt;Time/s&gt;                             	   											 | 踢出               			 |
| isafk          		 |  &lt;off/on&gt;                             	   											 | 是否启用AFK               			 |
| plugins          		 |                               	   											 | 查看插件列表               			 |
| players          		 |                               	   											 | 查看玩家列表               			 |
| kill          		 | &lt;PlayerSerialNumber&gt;                              	   											 | 杀死玩家               			 |
| clearmuteall          		 |                               	   											 | 取消全部禁言               			 |
| maps          		 |                               	   											 | 查看Custom Map               			 |
| reloadmaps          		 |                               	   											 | 重新加载地图               			 |
| stop          		 |                               	   											 | 停止服务器               			 |
抱歉 或许有更多的命令没有被加入 因为文档没有时间更新  
</details>

## 游戏命令列表
<details>
  <summary>客户端命令列表</summary>  

| 命令 			| 参数 												 | 信息 										 |
|:---           |:--- 												 |:--- 										 |
| help      |   | 获取帮助 									 |
这里的命令我建议自己在服务端测试 不多写  
抱歉 或许有更多的命令没有被加入 因为文档没有时间更新  
</details>

## 赞助
RW-HPS是AGPL v3授权的开放源码项目，完全免费使用。然而，如果没有适当的资金支持，为项目维护和开发新功能所需的工作量是不可持续的。  
请注意，赞助是全自愿的。赞助者不会获得特权，不赞助也可以使用全部的功能。

我们通过以下渠道接受捐赠：  
+ [爱发电](https://afdian.net/@derdct)

## 感谢
[Thanks](https://github.com/RW-HPS/RW-HPS/blob/master/Thanks-CN.md)

> [IntelliJ IDEA](https://zh.wikipedia.org/zh-hans/IntelliJ_IDEA) 是一个在各个方面都最大程度地提高开发人员的生产力的 IDE，适用于 JVM 平台语言。

特别感谢 [JetBrains](https://www.jetbrains.com/?from=rw-hps) 为开源项目提供免费的 [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=rw-hps) 等 IDE 的授权  
[<img src=".github/jetbrains-variant-3.png" width="200"/>](https://www.jetbrains.com/?from=rw-hps)
