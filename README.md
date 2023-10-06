# MealMate 😋

MealMate是一个基于Spring Cloud微服务架构设计的应用程序，旨在让用户能够分享美食、与他人互动并探索附近的美食场所。该项目只提供了功能接口，你应该根据下面的接口测试指南来测试本项目。

## 功能特点
![image](https://github.com/xiaoxinxing66/MealMate/assets/93857716/be493b16-6311-4c29-97e0-4fc9c68548c1)

1. **用户模块：**

   - 实现用户注册，结合Oauth2 + Spring Security实现用户单点登录，SSO功能。

   - 使用BitMap记录用户签到信息，使用Zset管理用户积分。

2. **社交模块：**

   - 使用Redis的Set数据类型存储关注集合、粉丝集合，共同关注列表。

   - 使用Zset来存储Feed集合，当关注和取关好友时，Feed会相应更新。

3. **餐厅模块：**

   - 使用Redis的Hash数据类型存储餐厅热点数据缓存，提高查询性能。

   - 使用Redis的List数据类型存储餐厅评论信息。

4. **订单模块：**
   - 使用Redis的Lua脚本和自定义的分布式锁来确保订单不会超卖，同时限制每人只下一单。

5. **地理位置模块：**
   - 使用Redis的GEO数据类型存储用户的地理位置信息，实现附近的人功能。

## 技术栈

- Spring Cloud微服务架构
- Redis缓存
- Spring Security和Oauth2认证
- Java开发语言

## 部署指南

1. 先要克隆项目到本地。
2. 配置各个微服务的配置文件，包括数据库连接、Redis连接和安全配置。
3. 使用Docker容器化部署各个微服务或使用Spring Cloud的部署工具。
4. 启动各个微服务，确保它们正确运行。
5. 关于接口测试，这里推荐一个比较好用的工具：[Postman API Platform](https://www.postman.com/)

## API测试指南

1. **环境准备**：
   - 确保您已经部署了项目的后端微服务，并且它们正在运行。
   - 确保您具有适当的访问权限，包括身份验证令牌或API密钥。
2. **使用API工具**：
   - 您可以使用常见的API测试工具，如Postman。
   - 在测试之前，确保您已经配置了API请求的基本URL，例如：`http://localhost/`。
     - 本项目使用Spring Cloud gateway网关，直接写`localhost`即可。
3. **身份验证**：
   - 有些API可能需要身份验证令牌，您需要在请求中包含适当的令牌或授权标头。请先调用登录的接口来获取token。
4. **执行API请求**：
   - 根据您的需求，选择要测试的API接口，并使用合适的HTTP请求方法（GET、POST、PUT、DELETE等）发送请求。
   - 在请求中包含必要的参数和数据，如请求体、查询参数或标头。
5. **查看响应**：
   - 检查API的响应，确保它们符合您的预期。通常，响应将以JSON格式返回。
   - 检查状态代码、响应消息和任何返回的数据。

## 联系我们

email：[2507932331@qq.com](mailto:2507932331@qq.com)

项目链接：[xiaoxinxing66/MealMate (github.com)](https://github.com/xiaoxinxing66/MealMate)

