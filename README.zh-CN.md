# BingoCV

BingoCV 是一个开源个人简历管理系统，目标是提供从简历资料维护、模板选择、积分兑换，到公开/私密分享的一站式管理能力。

当前项目包含 Spring Boot 后端、Vue 3 前端，以及 MySQL 初始化脚本。

## 功能特性

### √ 已实现功能

#### 1. 用户系统
- √ 用户注册（支持邀请码）
- √ 用户登录（验证码校验）
- √ 记住我功能
- √ 密码加密存储（BCrypt）
- √ 用户信息管理

#### 2. 简历管理
- √ **个人信息**：姓名、性别、年龄、城市、联系方式（手机/邮箱/QQ/微信）、个人简介、头像
- √ **教育经历**：学校、专业、起止时间、描述，支持排序
- √ **工作经历**：公司、职位、起止时间、工作描述，支持排序
- √ **技能特长**：技能关键词（空格分隔）
- √ **特长亮点**：特长名称、描述，支持排序

#### 3. 模板系统
- √ 模板市场浏览（按行业分类）
- √ 免费模板使用
- √ 积分兑换模板
- √ 我的模板管理
- √ 模板启用/切换
- √ 7套预设模板（工程师、产品、财务、讲师、医护、设计、通用）

#### 4. 积分系统
- √ 积分账户管理（余额、累计获得、累计消费）
- √ 积分流水记录
- √ 每日签到（连续签到奖励递增）
- √ 任务系统（新手任务、每日任务、成就任务）
- √ 任务完成状态跟踪
- √ 积分奖励自动发放

#### 5. 简历预览
- √ 实时预览简历
- √ 响应式设计
- √ 数据动态渲染

#### 6. 系统功能
- √ Knife4j/OpenAPI 接口文档
- √ 全局异常处理
- √ 统一响应格式
- √ Redis 缓存支持
- √ 逻辑删除
- √ 数据权限控制

### 🚧 规划中功能

#### Phase 4: 分享功能
- ⏳ 公开分享（生成分享链接）
- ⏳ 私密分享（访问密码）
- ⏳ 短链接生成
- ⏳ 访问统计（访客IP、地域、设备）
- ⏳ 访问人数限制
- ⏳ 分享有效期设置

#### Phase 5: AI 增强
- ⏳ AI 简历润色
- ⏳ 简历评分
- ⏳ 面试题生成
- ⏳ 智能建议

#### Phase 6: 高级功能
- ⏳ RBAC 权限管理
- ⏳ 后台管理面板
- ⏳ 操作日志
- ⏳ 接口限流
- ⏳ 数据看板
- ⏳ 支付订单（积分充值、模板购买）

## 技术栈

后端：

- Java 21
- Spring Boot 3.4.5
- MyBatis-Plus 3.5.x
- MySQL 8.x
- Redis
- Druid
- Hutool
- Kaptcha

前端：

- Vue 3
- Vite
- Element Plus
- Pinia
- Vue Router
- Axios
- Iconify

## 项目结构

```text
bingoCV/
|-- db/                         # 数据库脚本
|   `-- bingocv_schema.sql
|-- bingocv-web/                 # Vue 3 前端
|-- bingocv-worker/              # 前端构建输出目录
|   `-- dist/
|-- src/main/java/               # Spring Boot 后端源码
|-- src/main/resources/          # 后端配置与资源
|-- pom.xml
|-- README.md                    # English README
`-- README.zh-CN.md              # 中文说明
```

## 快速开始

### 1. 准备环境

- JDK 21+
- Maven 3.9+ 或项目内置 Maven Wrapper
- Node.js 18+
- MySQL 8.x
- Redis 6+

### 2. 初始化数据库

```bash
mysql -uroot -proot < db/bingocv_schema.sql
```

脚本会创建 `bingoCV` 数据库，并初始化用户、简历、模板、积分、任务、签到、分享、短链、系统配置、订单等核心表。

**（可选）导入测试数据：**

```bash
mysql -uroot -proot < db/bingocv_test_data.sql
```

测试数据包含：
- 4个测试账号（zhangsan / lisi / wangwu / admin）
- 完整的简历信息（教育、工作、技能等）
- 积分账户和流水记录
- 任务完成进度和签到记录
- 统一密码：`123456`

### 3. 修改后端配置

编辑：

```text
src/main/resources/application.yml
```

确认 MySQL 和 Redis 配置符合本机环境：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bingoCV?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
  data:
    redis:
      host: localhost
      port: 6379
```

### 4. 启动后端

```bash
./mvnw spring-boot:run
```

Windows：

```bat
mvnw.cmd spring-boot:run
```

默认后端地址：

```text
http://localhost:8080
```

### 5. 启动前端

```bash
cd bingocv-web
npm install
npm run dev
```

Vite 默认访问地址通常是：

```text
http://localhost:5173
```

### 6. 构建前端

```bash
cd bingocv-web
npm run build
```

发布构建输出目录配置为：

```text
bingocv-worker/dist
```

## 数据库脚本

数据库脚本位于：

```text
db/bingocv_schema.sql
```

已包含：

- `bingo_user`
- `bingo_profiles`
- `bingo_edu`
- `bingo_work`
- `bingo_skill`
- `bingo_specialty`
- `bingo_template`
- `bingo_user_template`
- `bingo_points`
- `bingo_points_log`
- `bingo_task`
- `bingo_user_task`
- `bingo_sign_in`
- `bingo_share`
- `bingo_share_access`
- `bingo_short_url`
- `bingo_system_config`
- `bingo_pay_order`

## 开发路线

- Phase 1：简历管理、菜单重构
- Phase 2：模板市场、我的模板、实时预览、PDF 导出
- Phase 3：积分账户、任务、签到、积分兑换
- Phase 4：公开分享、私密分享、短链、访问统计
- Phase 5：AI 简历润色、简历评分、面试题生成
- Phase 6：RBAC、后台配置、操作日志、接口限流、数据看板

## License

本项目使用 MIT License 开源。
