# 🎱 飞书双色球彩票管理 Web 应用

飞书内嵌 Web 应用，支持在飞书工作台中直接使用。

## 功能特性

- 📝 **记录管理** - 添加、删除彩票购买记录
- 🔄 **飞书同步** - 与飞书多维表格双向同步
- 🤖 **智能预测** - 本地算法 + AI 智能推荐
- 📊 **数据统计** - 中奖次数、总奖金统计

## 快速开始

### 1. 安装依赖

```bash
cd feishu-lottery-app
npm install
```

### 2. 配置飞书应用

设置环境变量或直接修改 `server.js`：

```bash
export FEISHU_APP_ID=cli_xxxxxxxxxxxxx
export FEISHU_APP_SECRET=xxxxxxxxxxxxxxxxxxxxxx
```

### 3. 启动服务

```bash
npm start
```

服务启动后访问：`http://localhost:3000`

### 4. 配置飞书应用首页

1. 打开 [飞书开放平台](https://open.feishu.cn/)
2. 创建企业自建应用
3. 配置应用首页地址：`https://your-domain.com/`
4. 发布应用

## 目录结构

```
feishu-lottery-app/
├── server.js          # Node.js 后端服务
├── package.json       # 项目配置
├── public/
│   └── index.html     # 前端页面
└── README.md
```

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 应用首页 |
| GET | `/api/records` | 获取购彩记录 |
| POST | `/api/records` | 添加记录 |
| DELETE | `/api/records/:id` | 删除记录 |
| POST | `/api/predict` | 预测推荐 |
| GET | `/api/draws` | 历史开奖数据 |
| POST | `/api/sync` | 从飞书同步 |

## 部署

### 方式一：本地运行
```bash
npm start
```

### 方式二：Docker 部署
```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 3000
CMD ["npm", "start"]
```

### 方式三：Serverless
可部署到 Vercel、Railway、Render 等平台。

## 需要您提供的

⚠️ 以下信息需要您提供才能启用完整功能：

1. **飞书应用凭证**
   - App ID (`cli_xxxxxxxx`)
   - App Secret

2. **飞书多维表格**
   - 应用必须有访问多维表格的权限

3. **域名**
   - 飞书应用需要 HTTPS 域名

## 许可证

MIT