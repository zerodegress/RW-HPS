# RW-HPS - Http API

> 注:
> - 本章节是介绍 `RW-HPS` 中的一个内置插件
> - 本章节**不是**关于 `UPLIST-API` 的章节

## 启用

我懒得写, 来个人PR

## WS

### info
```
{
    "cookie" : "5420dedf8f1829bc43d03843d26216523fe19e06ee253abcacd3a4ee5b9af12b",
    "type" : "register"
}

{
    "type" : "ping"
}

{
    "type" : "getConsole"
}

{
    "type" : "runCommand",
    "runCommand" : "version"
}
```
就这四个json, 十秒内必须有个包发过去, 没包就发ping  
只需要获取一次(发送一次) getConsole 过去, 就会自动注册事件, 把控制台的数据发送到ws  
格式自己试吧, 懒得写了  