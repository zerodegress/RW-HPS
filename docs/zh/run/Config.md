> 服务器默认启动命令  
>> Server模式为**start**  
>>> Relay模式为**startrelay**  
>>> 直接转发消耗更多带宽，效果与使用 VPN 转发相同  
>>
>>> Relay模式为**startrelaytest**  
>>> 使用多播, 减少宽带消耗  
> 
> "DefStartCommand": "start"  

> Log记录等级  
>> OFF FATAL ERROR WARN INFO DEBUG TRACE ALL
> "Log": "ALL"  

> 服务器的名字 在上列表的PlayerName显示  
"ServerName": "RW-HPS"  
> 服务器的地图名 自定义在列表的MapsName显示 **留空为显示MapsName**  
"Subtitle": ""  

> 服务器运行的端口  
> "Port": 5123  

> 服务器的密码  
> "Passwd": ""

> 玩家进入时发送的消息  
>> "EnterAd": ""
> 
> 游戏开始时发送的消息  
>> "StartAd": ""
> 
> 房间已满时KICK的消息  
>> "MaxPlayerAd": ""
> 
> 房间已开始游戏时KICK的消息  
>> "StartPlayerAd": ""  

> 最大玩家  
>> "MaxPlayer": 100

> 游戏最长时间 (避免玩家挂机) (关闭则为 -1)
>> "MaxGameIngTime": 7200

> 游戏最小开始人数 (关闭则为 -1)
>> "StartMinPlayerSize": -1

> 游戏最小自动开始人数 (关闭则为 -1)
>> "AutoStartMinPlayerSize": 4

> 游戏最长时间 (避免玩家挂机)
>> "MaxGameIngTime": 7200

> 游戏最长时间 (避免玩家挂机)
>> "MaxGameIngTime": 7200

> 最大发言长度  
>> "MaxMessageLen": 40  

> 最大单位数量  
>> "MaxUnit": 2000  

> 游戏多少毫秒每X刻
>> "Tick": 6
> 
> 游戏X刻
>> "TickTime": 150

> 默认倍率  
>> "DefIncome": 1.0  

> 是否启用第一个进入为Admin  
>> "OneAdmin": true  

> 是否启用重连-BETA  
>> "ReConnect": false

> RELAY模式下 是否启用单房间模式  
>> "SingleUserRelay": false  
> 
> 单房间模式下是否启用Mod  
>> "SingleUserRelayMod": false

> 是否自动重读取maps下地图  
>> "AutoReLoadMap": false

> 游戏端口共用 (Game+HTTP)  
>> "WebGameBypassPort": false

> Web服务  
>> "WebService": false
> 
> 单独Web端口  
>> "SeparateWebPort": 5124
> 
> 启用SSL  
>> "SSL": false
>
> SSL 密码  
>> "SLPasswd": "RW-HPS"

> 点石成金模式  
> "Turnstoneintogold": false

> 服务器进程的PID  
"RunPid": 2757





