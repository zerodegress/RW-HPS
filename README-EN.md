## 语言  

[跳转中文](https://github.com/deng-rui/RW-HPS/blob/master/README.md)  
[TO EN](https://github.com/deng-rui/RW-HPS/blob/master/README-EN.md)  

# RW-HPS  
Third party rust war server  
This is a netty based server  
It aims to provide better game experience for players as a high performance and high availability server  

### Licenses
Used in this project  
GNU General Public License v3.0

#### Game protocols that will not be supported
- Server List -> ADD List, Update List, Remove List  

**All development is for learning, do not use for illegal purposes**  

## Start
- Development documentation: [docs](docs/README.md)  
- Update log: [release](https://github.com/deng-rui/RWHPS/releases)  
- Development plan: [milestones](https://github.com/deng-rui/RWHPS/milestones)  
- Discuss:
  > Questions raised in GitHub discussions will be answered, and you are welcome to share your new ideas based on the project.  
  > Email contact : Der-DCT@pm.me or  

## Construction
1. Install JDK 11 +. If you don't know how to do it, check it out.  
2. Run "gradlew jar"  
3. Your jar will be in the 'build/libs' directory  
4. Run your jar to experience high performance server. Please note that this server does not support running in win  

## Run

| Configure 		| CPU             | RAM 	| SYSTEM 			| Disk 	| Java      |
|:--- 		|:---             |:---     |:---           |:---       |:---       |
| Currently Allocated 	| BCM2711         | 4G      | Ubuntu 19.10  | 500 HHD  | Java 11   |
| Minimum Configuration 	| ARMv7 Processor rev 5  | 512M      | ubuntu 16.04+ | 1G HHD  | Java 11   |

## Mark Setup

| Configure 		| CPU             | RAM 	| SYSTEM 			| Disk 	| Java      | Gradle    |
|:--- 		|:---             |:--- 	|:--- 			|:---      	|:---       |:---       |
| Currently Allocated 	| BCM2711         | 4G 		| Ubuntu 19.10 	| 500G HHD 	| Java 11    | 6.2.2     |

## Server Command List
[2021-02-20 09:42:18 UTC]    clearbanip - 清理被ban的ip  
[2021-02-20 09:42:18 UTC]    admin <add/remove> <PlayerSite> - 设置admin  
[2021-02-20 09:42:18 UTC]    clearbanuuid - 清除被ban的uuid   
[2021-02-20 09:42:18 UTC]    clearbanall - 清空ban  
[2021-02-20 09:42:18 UTC]    ban <PlayerSerialNumber> - 禁止某人  
[2021-02-20 09:42:18 UTC]    mute <PlayerSerialNumber> <Time(s)> - 禁言  
[2021-02-20 09:42:18 UTC]    kick <PlayerSerialNumber> [time] - 踢出  
[2021-02-20 09:42:18 UTC]    isafk <off/on> - 是否禁止AFK  
[2021-02-20 09:42:18 UTC]    plugins - 查看插件列表  
[2021-02-20 09:42:19 UTC]    players - 查看玩家列表   
[2021-02-20 09:42:19 UTC]    kill <PlayerSerialNumber> - 杀死玩家  
[2021-02-20 09:42:19 UTC]    clearmuteall - 取消全部禁言  
[2021-02-20 09:42:19 UTC]    upserverlist - 上传Server到List   
[2021-02-20 09:42:19 UTC]    cleanunit - 上传Server到List  
[2021-02-20 09:42:19 UTC]    maps - 取消全部禁言  
[2021-02-20 09:42:19 UTC]    stop - 停止服务器

| Command 					 | Parameter 																						 | Information 									 |
|:--- 					 |:--- 																						 |:--- 									 |
| help 		              |                                                  										 | 获取帮助 		 |
| start                  |                                                  										 | 开启服务器 						 |
| say 		      | &lt;文字&gt                                                  										 | 用Server的名义发消息 				 |
| giveadmin                | &lt;玩家位置&gt; 																 | 转移Admin       		         |
| restart 			 | 																							 | 重启服务器 				 |
| gameover 				 |  	 | 重新开始游戏               				 |
| keys          		 |                                                  										 | 查看服务器已建立的Key              	 |
| rmkeys          		 |                                                  										 | 删除全部Key               			 |
| rmkey          		 | &lt;Key&gt;                               	   											 | 删除指定Key               			 |


## Game Command List

| Command 			| Parameter 												 | Information 										 |
|:---           |:--- 												 |:--- 										 |
| help      |   | 获取帮助 									 |

### Thanks  
@Miku Inspiration from Rukkit project  
@Tiexiu.xyz Provide computing support  
@Aunken ARC/Mindustry The project provides the underlying vision  
@Apache org.apache.tools.zip  