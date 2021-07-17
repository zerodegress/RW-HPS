## Language(语言)  

[跳转中文介绍](https://github.com/RW-HPS/RW-HPS/blob/master/README-CN.md)  

# RW-HPS
![Github stars](https://img.shields.io/github/stars/RW-HPS/RW-HPS.svg)
![](https://github.com/RW-HPS/RW-HPS/actions/workflows/gradle.yml/badge.svg?branch=master)
[![](https://jitpack.io/v/RW-HPS/RW-HPS.svg)](https://jitpack.io/#RW-HPS/RW-HPS)  


## Statement
### All development is for learning, please do not use it for illegal purposes
- RW-HPS Is completely free and open source software, only for learning and entertainment purposes
- RW-HPS Will not be forced to charge fees in any way, or impose material conditions on users

## Present
Third party rust war server  
This is a netty based server  
It aims to provide better game experience for players as a high performance and high availability server

### Licenses
Used in this project  
GNU General Public License v3.0

#### Game protocols that will not be supported
- Server List -> ADD List, Update List, Remove List  
- Money related, such as value-added services  

**All development is for learning, do not use for illegal purposes** 

`RW-HPS` is open sourced under the `GPLv3` agreement. For the healthy development of the entire community，We **highly recommended**you do the following：

- **Indirect contact (including but not limited to using `Http API` or cross-process technology and bytecode modification) to `RW-HPS` software using `GPLv3` open source**
- **For personal use, please join the server to use from RW-HPS**
- **Do not encourage, do not support all commercial use*

### Derivative software needs to declare and quote

- If the software package released by RW-HPS is quoted without modifying RW-HPS, the derivative project shall mention the use of RW-HPS in any part of the description。
- If the RW-HPS source code is modified and then released，**Or refer to the internal implementation of RW-HPS to release another project**，Then the derivative project must be**article head**or 'RW-HPS' related information**first Appearance**s position**clearly stated**from This Warehouse (`https://github.com/RW-HPS/RW-HPS`)。Don’t distort or hide the fact that it’s free and open source。


## Start
- Development documentation: [docs](https://github.com/RW-HPS/RW-HPS/wiki)  
- Update log: [release](https://github.com/RW-HPS/RW-HPS/releases)  
- Development plan: [milestones](https://github.com/RW-HPS/RW-HPS/milestones)  
- Discuss:
  > Questions raised in GitHub discussions will be answered, and you are welcome to share your new ideas based on the project.  
  > Email contact: dr@der.kim  
  > Tencent QQ Group: [901913920](https://qm.qq.com/cgi-bin/qm/qr?k=qhJ6ekYF9pD9jO6j8H2rZw8ePAVypoU0&jump_from=webapi) (GPLv3)  
  > Telegram Group: [RW-HPS](https://t.me/RW_HPS)  
  >> Tencent QQ: A modern messaging software used by all Chinese netizens.  
- Mirroring:
  > [Github](https://github.com/RW-HPS/RW-HPS)  
  > [Gitee](https://gitee.com/derdct/RW-HPS)

## Run

| Configure 		| CPU             | RAM 	| SYSTEM 			| Disk 	| Java      |
|:--- 		|:---             |:---     |:---           |:---       |:---       |
| Currently Allocated 	| BCM2711         | 4G      | Ubuntu 19.10  | 500 HDD  | Java 11   |
| Minimum Configuration 	| ARMv7 Processor rev 5  | 64M      | Linux~  | 64M HDD  | Java 11   |

## Mark Setup

| Configure 		| CPU             | RAM 	| SYSTEM 			| Disk 	| Java      | Gradle    |
|:--- 		|:---             |:--- 	|:--- 			|:---      	|:---       |:---       |
| Currently Allocated 	| BCM2711         | 4G 		| Ubuntu 19.10 	| 500G HDD 	| Java 11    | 6.2.2     |

## Server Command List

| Command 					 | Parameter 																						 | Information 									 |
|:--- 					 |:--- 																						 |:--- 									 |
| help 		              |                                                  										 | Get help 		 |
| start                  |                                                  										 | Turn on the server 						 |
| say 		      | &lt;TEXT&gt;                                                  										 | Send messages in the name of Server 				 |
| giveadmin                | &lt;PlayerSerialNumber&gt; 																 | Transfer Admin       		         |
| restart 			 | 																							 | Restart server 				 |
| gameover 				 |  	 | Restart The Game               				 |
| clearbanip          		 |                                                  										 | Clean up the banned IP               	 |
| admin          		 |&lt;add/remove&gt; &lt;PlayerSite&gt;                                                  										 | Set up admin               			 |
| clearbanuuid          		 |                               	   											 | Clear banned uuid               			 |
| clearbanall          		 |                               	   											 | Empty ban               			 |
| ban          		 | &lt;PlayerSerialNumber&gt;                                 	   											 | Ban someone               			 |
| mute          		 |  &lt;PlayerSerialNumber&gt;  &lt;Time/s&gt;                             	   											 | Clear banned uuid               			 |
| kick          		 |  &lt;PlayerSerialNumber&gt;  &lt;Time/s&gt;                             	   											 | Kick               			 |
| isafk          		 |  &lt;off/on&gt;                             	   											 | Whether to enable AFK               			 |
| plugins          		 |                               	   											 | View the list of plugins               			 |
| players          		 |                               	   											 | View player list               			 |
| kill          		 | &lt;PlayerSerialNumber&gt;                             	   											 | Kill the player               			 |
| clearmuteall          		 |                               	   											 | Unmute all               			 |
| maps          		 |                               	   											 | View Custom Map               			 |
| reloadmaps          		 |                               	   											 | Reload map               			 |
| stop          		 |                               	   											 | Stop the server               			 |


## Game Command List

| Command 			| Parameter 												 | Information 										 |
|:---           |:--- 												 |:--- 										 |
| help      |   | Get help 									 |

## Sponsor
RW-HPS is an open source project authorized by GPL V3, which is free to use. However, the amount of work required to maintain and develop new features for the project is not sustainable without appropriate funding.  
Please note that sponsorship is entirely voluntary. Sponsors are not privileged and can use all features without sponsorship.

We receive donations through the following channels：
+ [爱发电](https://afdian.net/@derdct)

### Thanks  
@Miku Inspiration from Rukkit project  
@Tiexiu.xyz Provide computing support  
@Aunken ARC/Mindustry The project provides the underlying vision  
@Apache org.apache.tools.zip  
