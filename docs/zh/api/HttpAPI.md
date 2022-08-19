# RW-HPS - Http API

> 注:
> - 本章节是介绍 `RW-HPS` 中的一个内置插件
> - 本章节**不是**关于 `UPLIST-API` 的章节

## 启用
`HttpApi`插件默认启用,如需关闭请前往`data/plugins/HttpApi/HttpApi.json`  
它看起来是这样的:
```json
{"enabled":true,"listen":"127.0.0.1","port":8080,"token":"defaultToken"}
```
其中,`enabled`代表是否启用,你只需将`true`改为`false`即可关闭

## 使用
`HttpApi`会在`listen`监听`port`端口,例如`127.0.0.1:8080`  
有以下api可用:
- `about`
- `info`
- `gameinfo`
- `plugins`
- `mods`

调用需要加上`token`参数,否则服务器将返回403
```json
{
  "code": 403,
  "reason": "invalid token"
}
```

### about
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