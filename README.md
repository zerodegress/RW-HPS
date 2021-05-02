## Language(语言)  

[跳转中文介绍](https://github.com/deng-rui/RW-HPS/blob/master/README-CN.md)  

# RW-HPS  
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

## Start
- Development documentation: [docs](docs/README.md)  
- Update log: [release](https://github.com/deng-rui/RWHPS/releases)  
- Development plan: [milestones](https://github.com/deng-rui/RWHPS/milestones)  
- Discuss:
  > Questions raised in GitHub discussions will be answered, and you are welcome to share your new ideas based on the project.  
  > Email contact : RW-HPS@der.kim  

## Construction
1. Install JDK 11 +. If you don't know how to do it, check it out.  
2. Run "gradlew jar"  
3. Your jar will be in the 'build/libs' directory  
4. Run your jar to experience high performance server.  

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
| stop          		 |                               	   											 | Stop the server               			 |


## Game Command List

| Command 			| Parameter 												 | Information 										 |
|:---           |:--- 												 |:--- 										 |
| help      |   | Get help 									 |

### Thanks  
@Miku Inspiration from Rukkit project  
@Tiexiu.xyz Provide computing support  
@Aunken ARC/Mindustry The project provides the underlying vision  
@Apache org.apache.tools.zip  
