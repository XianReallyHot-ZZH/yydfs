# yydfs
[yydfs](https://github.com/XianReallyHot-ZZH/yydfs)是一个简单的分布式文件系统demo，模拟实现了文件的上传，下载，同步等基本功能。


# 功能简介
1. 文件上传
2. 文件下载
3. 文件同步备份服务器
    * 基于http的同步
    * 基于mq的异步
4. 文件元数据
5. 文件统一命名处理
6. 文件系统目录拆分与管理


# 快速开始
1. 启动rocketmq
2. 启动两个本实例，其中配置参数server.port,yydfs.uploadPath,downloadUrl,backupUrl四个参数要根据实例的不同角色定位做不同的设置
3. 打开浏览器访问http://localhost:8080/index.html
4. 选择文件（图片）上传


