##### 启动方式
- 项目导入idea
- 新建数据库 desensitize
- 新建表user，表结构如下
```
CREATE TABLE `user` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '姓名',
  `phoneNo` varchar(1000) NOT NULL DEFAULT '' COMMENT '手机号码',
  `gender` varchar(100) NOT NULL DEFAULT '' COMMENT '性别',
  `idNo` varchar(100) NOT NULL DEFAULT '' COMMENT '身份证',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典配置表';
```
- 运行com.qw.desensitize.DesensitizeApplication

##### 加密解密脱敏核心代码位置
- common.sensitive 接口字段脱敏
- common.encrypt1 拦截器加密解密
- common.encrypt2 类型转换器加密解密