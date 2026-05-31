# 校园速达 Relay

一款"柜到寝"的校园跑腿小程序，解决外卖柜到宿舍楼"最后 500 米"的痛点 —— 让取外卖的同学顺路帮同楼同学带货，赚一瓶饮料钱。

需求方发单（填写柜号、楼栋、跑腿费），跑腿方在大厅顺路接单，经"接单 → 拍照取件 → 拍照送达 → 确认收货"完成闭环。取件与送达均强制现场拍照留痕，建立信任、便于纠纷追溯。完成订单后跑腿费全额入账跑腿方钱包（零抽佣），支持提现申请与运营线下打款。

## 技术栈

- **后端**：Spring Boot 3 + MyBatis-Plus + MySQL，JWT 鉴权
- **前端**：微信原生小程序
- **AI**：接入大模型实现自然语言发单解析

## 核心特性

- 完整订单状态机：待接单 → 已接单 → 已取件 → 已送达 → 已完成
- 拍照留痕：取件/送达强制现场拍照，照片经平台存储校验，防伪造
- 钱包流水化记账：余额变动全程数据库原子操作，确认收货防重复入账
- 资金安全：提现遵循"真钱出 ≤ 真钱进"，提现仅生成待打款申请，由运营线下打款，杜绝资金漏洞
- 运营后台接口：提现处理（已打款 / 失败退款），独立口令鉴权

## 本地运行

后端依赖以下环境变量（敏感信息不入库，请在运行环境中配置）：

```bash
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=你的数据库密码
export ADMIN_TOKEN=你的运营口令
export DEEPSEEK_API_KEY=你的DeepSeek密钥   # 如需自然语言发单
```

1. 建库并导入流水表 DDL：`src/main/resources/db/wallet_transaction.sql`
2. 启动后端：`./mvnw spring-boot:run`（默认端口 8080，context-path `/api`）
3. 用微信开发者工具打开前端项目，开发阶段勾选"不校验合法域名"

## 项目结构

```
后端 com.spike.relay
├── controller   # 接口层（订单/钱包/文件/登录/运营后台）
├── service      # 业务逻辑
├── entity       # 实体
├── mapper       # MyBatis-Plus 数据访问
├── interceptor  # JWT 鉴权拦截器
└── config       # Web 配置、静态资源映射

前端 miniprogram/pages
├── hall         # 订单大厅
├── postOrder    # 发布订单
├── orderDetail  # 订单详情（状态感知 + 拍照留痕）
├── orders       # 我的订单
├── wallet       # 钱包（余额 / 流水 / 提现）
└── profile      # 个人中心
```
