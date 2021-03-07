# RW-HPS

欢迎来到 RW-HPS Plugin 文档。

## 协议的覆盖
**请先设置Plugin在协议后加载**

```
/*
* Copyright 2020-2021 Dr.
*
*  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
*  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
*
*  https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
*/
package dr.rwhps.plugin.test;

/**
 * @author Dr
 */
public class OveMain extends [NetCoreName] {
    static {
        Data.core.admin.setNetConnectProtocol(new Administration.NetConnectProtocolData(new OveMain(null,null),151));
    }
~
}
```