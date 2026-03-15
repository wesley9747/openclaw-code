# 彩票智查 - 飞书应用 + 后端完整设计文档

> 版本: v1.0-feishu-app | 更新: 2025-06-20 | 作者: 大龙虾 🦞

---

## 📦 文档导航

本仓库包含 **飞书应用集成版** 的完整设计文档。如果不需要飞书集成，请参考 `H5+后端` 版本。

### 核心文档（飞书应用版）

| 文档 | 描述 | 大小 |
|------|------|------|
| [需求规格文档-飞书应用+后端.md](需求规格文档-飞书应用+后端.md) | 用户故事、功能需求、API设计、成本估算 | 5.3KB |
| [原型设计-飞书应用+后端.md](原型设计-飞书应用+后端.md) | 用户流程、8页面线框图、交互细节 | 7.1KB |
| [UI设计规范-飞书应用+后端.md](UI设计规范-飞书应用+后端.md) | Vant组件、主题、页面设计、代码示例 | 10.9KB |
| [架构设计-飞书应用+后端.md](架构设计-飞书应用+后端.md) | 后端FastAPI、前端Vue、数据库、部署方案 | 11.9KB |

### 参考文档（H5独立版）

- [需求规格文档-H5+后端.md](需求规格文档-H5+后端.md)
- [原型设计-H5+后端.md](原型设计-H5+后端.md)
- [UI设计规范-H5+后端.md](UI设计规范-H5+后端.md)
- [架构设计-H5+后端.md](架构设计-H5+后端.md)

---

## 🎯 项目简介

**彩票智查** 是一个集成到飞书工作台的 H5 应用 + 后端服务，提供：

✅ **飞书账号一键登录**（OAuth 2.0）
✅ **拍照识别**（OCR + 智能解析）
✅ **开奖查询与中奖判断**（内置历史数据）
✅ **智能预测**（支持用户自配大模型API）
✅ **统计分析**（年度ROI、趋势图表）
✅ **飞书机器人推送**（开奖/中奖提醒）
✅ **数据云端同步**（多设备访问）

---

## 📁 目录结构

```
lottery-h5/
├── docs/
│   ├── 需求规格文档-飞书应用+后端.md    # 主需求文档
│   ├── 原型设计-飞书应用+后端.md         # 低保真线框图
│   ├── UI设计规范-飞书应用+后端.md       # 组件库和页面设计
│   ├── 架构设计-飞书应用+后端.md         # 技术架构和API设计
│   └── ... (H5独立版文档)
├── frontend/          # (待创建) Vue 3 + Vant 前端
├── backend/           # (待创建) FastAPI 后端
├── assets/            # 静态资源
└── README.md          # 本文件
```

---

## 🚀 快速开始

### 1. 飞书应用配置

1. 登录 [飞书开发者平台](https://open.feishu.cn/)
2. 创建「自建应用」→「网页应用」
3. 配置：
   - 首页地址: `https://your-frontend.vercel.app`
   - OAuth 回调: `https://your-frontend.vercel.app/auth/feishu/callback`
   - 权限: `email`, `user_id`, `avatar`
4. 获取 `APP_ID` 和 `APP_SECRET`

### 2. 后端开发

```bash
cd backend
cp .env.example .env
# 编辑 .env，填入数据库、飞书、百度OCR等配置
docker-compose up -d  # 启动PostgreSQL + Redis
pip install -r requirements.txt
uvicorn app.main:app --reload
```

### 3. 前端开发

```bash
cd frontend
cp .env.local.example .env.local
# 编辑 .env.local，填入后端API地址
npm install
npm run dev
```

### 4. 部署

- **后端**: Railway / Vercel Serverless / 自建
- **前端**: Vercel / Netlify (自动HTTPS)
- **数据库**: Supabase (免费额度)

详见 [架构设计-飞书应用+后端.md](架构设计-飞书应用+后端.md) 第4节。

---

## 📋 开发里程碑

| 阶段 | 任务 | 预计工时 |
|------|------|----------|
| 1. 飞书配置 | 创建应用，获取权限 | 0.5天 |
| 2. 后端核心 | 用户认证 + CRUD | 3天 |
| 3. OCR集成 | 百度API + 异步任务 | 1天 |
| 4. 预测功能 | 大模型转发 | 2天 |
| 5. 推送功能 | 飞书机器人 | 1天 |
| 6. 前端开发 | 8个页面实现 | 5-7天 |
| 7. 集成测试 | 端到端测试 | 2天 |
| 8. 部署上线 | 配置域名、监控 | 1天 |
| **总计** | | **14-17天** |

---

## 🔑 关键技术点

### 飞书登录流程

```
用户点击"使用飞书登录"
   ↓
跳转飞书授权页 (OAuth 2.0)
   ↓
用户确认授权
   ↓
返回应用 (callback?code=xxx)
   ↓
前端提交 code 到后端
   ↓
后端换取 access_token → 获取用户信息
   ↓
创建/更新用户 → 返回 JWT
   ↓
前端存储 JWT，跳转首页
```

### 机器人推送

- 定时任务：每天开奖后查询中奖用户
- 调用飞书机器人 Webhook 发送卡片消息
- 需用户开启"开奖提醒"权限

### OCR 识别

- 前端拍照 → Base64 → 后端
- 调用 **百度智能云通用文字识别**（高精度）
- 解析期号和号码 → 查询开奖 → 计算中奖

---

## 💰 成本估算

| 服务 | 月费用 | 说明 |
|------|--------|------|
| Supabase (免费额度) | ¥0 | < 500MB, 1GB 流量 |
| 百度 OCR | ¥50-100 | 5000次调用预估 |
| Railway (后端) | ¥0-50 | 免费额度 |
| Vercel (前端) | ¥0 | 免费 |
| 飞书应用 | ¥0 | 免费 |
| **总计** | **¥70-200/月** | |

---

## 📚 附录

### 环境变量清单

**后端 (.env)**:
```bash
DATABASE_URL=postgresql+asyncpg://...
REDIS_URL=redis://...
JWT_SECRET=your-secret
FEISHU_APP_ID=cli_xxxxx
FEISHU_APP_SECRET=xxxxxxxx
FEISHU_BOT_WEBHOOK=https://...
BAIDU_OCR_API_KEY=...
```

**前端 (.env.local)**:
```env
VITE_API_BASE_URL=https://your-backend.up.railway.app
VITE_FEISHU_OAUTH_URL=https://open.feishu.cn/open-apis/authen/v1/authorize
VITE_FEISHU_REDIRECT_URI=https://your-app.vercel.app/auth/feishu/callback
```

### 常用链接

- [飞书开放平台](https://open.feishu.cn/)
- [Supabase](https://supabase.com)
- [FastAPI 文档](https://fastapi.tiangolo.com/)
- [Vant 组件库](https://vant-ui.github.io/vant/)
- [百度OCR文档](https://cloud.baidu.com/doc/OCR/OCR-API.html)

---

## 📝 许可证

MIT License - 仅供学习和研究彩票分析使用。

---

**大龙虾 🦞** - 2025-06-20
