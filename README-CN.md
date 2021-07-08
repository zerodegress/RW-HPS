# RW-HPS
![Github stars](https://img.shields.io/github/stars/RW-HPS/RW-HPS.svg)
![](https://github.com/RW-HPS/RW-HPS/actions/workflows/gradle.yml/badge.svg?branch=master)
[![](https://jitpack.io/v/RW-HPS/RW-HPS.svg)](https://jitpack.io/#RW-HPS/RW-HPS)

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
- 若修改 RW-HPS 源代码再发布，**或参考 RW-HPS 内部实现发布另一个项目**，则衍生项目必须在**文章首部**或 'RW-HPS' 相关内容**首次出现**的位置**明确声明**来源于本仓库 (`https://github.com/RW-HPS/RW-HPS`)。不得扭曲或隐藏免费且开源的事实。

## 开始
- 开发文档: [docs](https://github.com/RW-HPS/RW-HPS/wiki)
- 更新日志: [release](https://github.com/RW-HPS/RW-HPS/releases)
- 开发计划: [milestones](https://github.com/RW-HPS/RW-HPS/milestones)
- 讨论:
  > 在 GitHub Discussions 提出的问题会收到回复, 也欢迎分享你基于项目的新想法  
  > 邮件联系: dr@der.kim  
  > Tencent QQ Group: [901913920](https://qm.qq.com/cgi-bin/qm/qr?k=qhJ6ekYF9pD9jO6j8H2rZw8ePAVypoU0&jump_from=webapi) (GPLv3)  
  > Telegram: [RW-HPS](https://t.me/RW_HPS  )
  >> *RW-HPS 没有任何的捐赠项目*
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
| 当前配置 	| BCM2711         | 4G      | Ubuntu 19.10  | 500 HDD  | Java 11   |
| 最低配置 	| ARMv7 Processor rev 5  | 64M      | Linux~  | 64M HDD  | Java 11   |

## 构建配置

| 配置 		| CPU             | 内存 	| 系统 			| 硬盘大小 	| Java      | Gradle    |
|:--- 		|:---             |:--- 	|:--- 			|:---      	|:---       |:---       |
| 当前配置 	| BCM2711         | 4G 		| Ubuntu 19.10 	| 500G HDD 	| Java 11    | 6.2.2     |

## 服务器命令列表
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
| mute          		 |  &lt;PlayerSerialNumber&gt;  &lt;Time/s&gt;                             	   											 | 清除被ban的uuid               			 |
| kick          		 |  &lt;PlayerSerialNumber&gt;  &lt;Time/s&gt;                             	   											 | 踢出               			 |
| isafk          		 |  &lt;off/on&gt;                             	   											 | 是否启用AFK               			 |
| plugins          		 |                               	   											 | 查看插件列表               			 |
| players          		 |                               	   											 | 查看玩家列表               			 |
| kill          		 | &lt;PlayerSerialNumber&gt;                              	   											 | 杀死玩家               			 |
| clearmuteall          		 |                               	   											 | 取消全部禁言               			 |
| maps          		 |                               	   											 | 查看Custom Map               			 |
| reloadmaps          		 |                               	   											 | 重新加载地图               			 |
| stop          		 |                               	   											 | 停止服务器               			 |


## 游戏命令列表

| 命令 			| 参数 												 | 信息 										 |
|:---           |:--- 												 |:--- 										 |
| help      |   | 获取帮助 									 |

### 鸣谢  
@Miku 的RUKKIT项目带来的启发  
@Tiexiu.xyz 提供计算支持  
@Aunken 的ARC/Mindustry项目提供底层设想  
@Apache 的org.apache.tools.zip
