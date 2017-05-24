# play-common

游戏服务端基础组件

* play-api      基础组件共享接口

* play-config   游戏配置相关

* play-data     数据库、缓存相关

* play-route    请求路由相关

注意：
    所有标记 [module args] 类的实现，需要覆写 toString 方法。

|文件夹|作用|备注
|---|---|---
|api|放置公共接口|用于模块调用则参数不可多于 1 个
|args|放置公共接口参数 pojo 类或接口
|impl|接口实现类
|factory|工厂方法
