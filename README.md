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

![详细说明解释](https://qwzhang01.github.io/2022/12/09/SpringBoot%E4%B8%8EMybatis%E4%B8%8B%E5%AD%97%E6%AE%B5%E8%84%B1%E6%95%8F%E3%80%81%E5%8A%A0%E5%AF%86%E8%A7%A3%E5%AF%86%E9%9D%9E%E4%BE%B5%E5%85%A5%E6%80%A7%E5%AE%9E%E7%8E%B0%E6%96%B9%E5%BC%8F/)
