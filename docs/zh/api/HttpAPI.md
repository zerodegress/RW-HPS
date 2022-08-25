# RW-HPS - Http API

> 注:
> - 本章节是介绍 `RW-HPS` 中的一个内置插件
> - 本章节**不是**关于 `UPLIST-API` 的章节

## 启用
`HttpApi`插件默认启用,但是它的所需功能并未默认开启,如需启用请前往`data/Config.json`  
将`WebGameBypassPort`后面的`false`改为`true`  
插件的配置文件在`data/plugins/HttpApi/HttpApi.json`
```json
{
  "enabled": true,
  "path": "/plugin/httpApi",
  "token": "defaultToken"
}
```

## 使用
`HttpApi`会在游戏端口创建HTTP服务器,例如`127.0.0.1:5123`  
默认路径为`/plugin/httpApi/<METHOD>`
有以下api可用:
- **POST**
  - `command`
- **GET**
  - `about`
  - `info`
  - `gameinfo`
  - `plugins`
  - `mods`
- **WS**
  - `info`

注意: ws的路径**是**`/WebSocket/httpApi`,而**不是**`/plugin/httpApi/ws`

调用需要`token`为参数,否则服务器将返回403
```json
{
    "code": 403,
    "reason": "invalid token"
}
```

## POST

### command
**执行命令**  
需要`exec`为参数,返回执行结果  
下列结果是`exec`为`plugins`的返回
```json
{
  "code": 200,
  "data": "name: UpList description: [Core Plugin] UpList author: Dr version: 1.0\nname: ConnectionLimit description: [Core Plugin Extend] ConnectionLimit author: Dr version: 1.0\nname: HttpApi description: [Core Plugin Extend] HttpApi author: zhou2008 version: 1.0\n"
}
```

## GET

### about
**返回系统信息**  
```json
{
    "code": 200,
    "data": {
        "system": "Linux",
        "arch": "amd64",
        "jvmName": "OpenJDK 64-Bit Server VM",
        "jvmVersion": "1.8.0_345"
    }
}
```

### info
**服务器信息**  
```json
{
    "code": 200,
    "data": {
        "isRunning": true,
        "serverPort": 5123,
        "online": 0,
        "maxOnline": 10,
        "serverMap": "Crossing Large (10p)",
        "serverSubtitle": "",
        "serverName": "RW-HPS",
        "needPassword": false,
        "gameStarted": false
    }
}
```

### gameinfo
**游戏信息**  
```json
{
    "code": 200,
    "data": {
        "income": 1,
        "noNukes": false,
        "credits": 0,
        "sharedControl": false,
        "players": []
    }
}
```

### plugins
**插件列表**  
```json
{
    "code": 200,
    "data": [
        {
            "name": "UpList",
            "desc": "[Core Plugin] UpList",
            "author": "Dr",
            "version": "1.0"
        },
        {
            "name": "ConnectionLimit",
            "desc": "[Core Plugin Extend] ConnectionLimit",
            "author": "Dr",
            "version": "1.0"
        },
        {
            "name": "HttpApi",
            "desc": "[Core Plugin Extend] HttpApi",
            "author": "zhou2008",
            "version": "1.0"
        }
    ]
}
```

### mods
**mod列表**  
```json
{
    "code": 200,
    "data": [
        {
            "name": "core_RW-HPS_units_159.zip"
        }
    ]
}
```

## WS

### info
连接成功后先发送`token`,否则服务器将返回`invalid token`  
之后客户端随便发点什么,服务器就会返回和GET里的info差不多的内容
```json
{
    "isRunning": true,
    "serverPort": 5123,
    "online": 0,
    "maxOnline": 10,
    "serverMap": "Crossing Large (10p)",
    "serverSubtitle": "",
    "serverName": "RW-HPS",
    "needPassword": false,
    "gameStarted": false
}
```