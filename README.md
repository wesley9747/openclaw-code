# 🎱 彩票智查 - 完整开发指南

完整的前后端项目，包含 FastAPI 后端 + Vue 3 前端，已实现 OCR 识别、飞书登录、投注管理等功能。

---

## 📁 项目结构

```
lottery-h5/
├── backend/          # FastAPI 后端
│   ├── app/
│   │   ├── main.py          # 主应用
│   │   ├── core/            # 配置、安全
│   │   ├── api/v1/          # API 路由
│   │   ├── models/          # 数据模型
│   │   ├── schemas/         # Pydantic
│   │   ├── crud/            # 数据库操作
│   │   └── services/        # 业务逻辑（OCR、解析）
│   ├── .env.example         # 环境变量模板
│   ├── requirements.txt     # Python 依赖
│   └── README.md
├── frontend/         # Vue 3 前端
│   ├── src/
│   │   ├── views/           # 8个页面
│   │   ├── stores/          # Pinia状态
│   │   ├── api/             # API模块
│   │   ├── utils/
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html
│   ├── vite.config.js
│   ├── package.json
│   └── .env.local
└── README.md
```

---

## 🚀 快速开始

### 1. 后端启动

```bash
cd backend
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt

# 复制环境变量
cp .env.example .env
# 编辑 .env，填入百度OCR Key（已配置）和其他配置

# 运行开发服务器
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

访问：http://localhost:8000/docs (Swagger UI)

### 2. 前端启动

```bash
cd frontend
npm install
npm run dev
```

访问：http://localhost:5173

---

## 🔑 已配置的服务

### ✅ 百度 OCR
- **AppID**: 122347980
- **API Key**: fUvloJHZWITEkWUjT1whximb
- **Secret Key**: 5xzedObnVENwoJd5uQ4xlnSaPId4TeHI
- **免费额度**: 1000次/月
- **测试状态**: ✅ 100% 准确率（test_ocr_final.py）

### ✅ 飞书应用
- **App ID**: cli_a93a84c29b38dcef
- **App Secret**: diHdnRfOyUbzHqs4mDfjNdhvSuiHtuaQ
- **状态**: 已创建，待配置 OAuth 回调

---

## 📋 核心功能

| 功能 | 状态 | 说明 |
|------|------|------|
| 飞书登录 | 🟡 Mock | 开发模式跳过真实 OAuth |
| OCR 识别 | ✅ 完成 | 百度高精版，支持双色球解析 |
| 记录 CRUD | ✅ 完成 | 增删改查 + 保存 |
| 开奖查询 | 🟡 模拟 | 返回测试数据，需对接真实源 |
| 中奖判断 | ✅ 完成 | 6+1 奖级逻辑 |
| 智能预测 | 🟡 框架完成 | 待接入大模型 API |
| 统计分析 | ✅ 完成 | 年度 ROI、月度图表 |
| 飞书推送 | ⬜ 待开发 | 机器人消息 |

---

## 🧪 OCR 测试

运行测试脚本验证百度 OCR 识别效果：

```bash
cd backend
python ../../test_ocr_final.py
```

使用示例图片：
```
/home/node/.openclaw/media/inbound/633a1230-5d42-4e32-b5af-dc9edad7b282.webp
```

期望输出：5注双色球号码 + 期号 2026024

---

## 🔧 开发说明

### Mock 登录
后端 `/api/auth/feishu-login` 接受任意 code，返回测试用户。真实上线需替换为飞书 API 调用。

### 数据库
当前使用内存字典存储。生产环境请配置 PostgreSQL 并实现 SQLAlchemy 模型。

### 环境变量
后端 `.env` 文件中配置：
- `DATABASE_URL`
- `BAIDU_OCR_API_KEY/SECRET`
- `FEISHU_APP_ID/SECRET`
- `JWT_SECRET`

---

## 📤 部署建议

### 后端
- **平台**: Railway / Vercel Serverless
- **命令**: `uvicorn app.main:app --host 0.0.0.0 --port $PORT`
- **环境**: 设置 `DATABASE_URL` (Supabase PostgreSQL)

### 前端
- **平台**: Vercel
- **自动部署**: 连接 GitHub 仓库即可

### 域名配置
1. 部署后获取域名（如 `lottery-backend.up.railway.app`）
2. 更新前端 `.env.local` 的 `VITE_API_BASE_URL`
3. 飞书后台修改 OAuth 回调地址为 `https://your-frontend.vercel.app/auth/feishu/callback`

---

## 📞 联系

作者：大龙虾 🦞  
项目：https://github.com/wesley9747/openclaw-code/tree/main/projects/lottery-h5
