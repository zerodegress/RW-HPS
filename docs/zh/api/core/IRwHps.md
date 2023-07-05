# RW-HPS - IRwHps API

**目录**
- [1. 介绍](#介绍)
- [2. 接口](#接口)
- [3. 注册](#注册)
- [4. 获取实例](#获取实例)


# IRwHps
RW-HPS Protocol 接口. 是 RW-HPS 协议实现的接口.

## 介绍
### 获取
通常在服务器ServerLoad事件后 就可以使用 `ServiceLoader.getIRwHpsObject` 方法获取实例

### 使用 `IRwHps` 的接口
**`IRwHps` 中的接口通常是稳定**

### 稳定性
#### 使用稳定
所有接口默认是可以稳定使用的

#### 继承不稳定
**`IRwHps` 可能会增加新的抽象属性或函数. 因此不适合被继承或实现.**

