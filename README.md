# OpenClaw 项目集合

这是一个包含多个独立项目的多项目管理仓库。

---

## 📂 项目结构

```
openclaw-code/
├── projects/
│   ├── lottery-h5/          # 🎱 彩票智查 - 飞书应用 + 后端（当前活跃项目）
│   └── archived/            # 📁 旧项目归档（仅供历史参考）
└── README.md                # 本文件
```

---

## 🎯 当前活跃项目

### [彩票智查 - 飞书应用 + 后端](projects/lottery-h5/)

**技术栈**：Vue 3 + Vant 4 + FastAPI + PostgreSQL + 飞书 OAuth

**功能**：
- ✅ 飞书账号一键登录
- ✅ 拍照识别（百度 OCR）
- ✅ 开奖查询与中奖判断
- ✅ 智能预测（用户自配大模型）
- ✅ 年度统计分析
- ✅ 飞书机器人推送

**快速开始**：
1. 查看 [项目文档](projects/lottery-h5/docs/)
2. 预览 [交互原型](projects/lottery-h5/index.html)
3. 阅读 [开发指南](projects/lottery-h5/README.md)

**在线预览**：
- GitHub Pages：https://wesley9747.github.io/openclaw-code/projects/lottery-h5/
- 原型地址：https://wesley9747.github.io/openclaw-code/projects/lottery-h5/index.html

---

## 📁 归档项目

位于 `projects/archived/` 目录，包含历史项目（已废弃）：

- `lottery-app/` - 安卓单机版（早期设计）
- `feishu-lottery-app/` - 早期飞书应用尝试

这些项目仅作历史参考，不参与当前开发。

---

## 🚀 开发规范

### 新增项目

1. 在 `projects/` 下创建新目录
2. 编写完善的 README.md
3. 提交时使用清晰的分支和 commit message
4. 更新本文件，添加项目链接

### Commit 约定

```
feat(project-name): 描述新功能
fix(project-name): 修复问题
docs(project-name): 文档更新
chore(project-name): 构建/工具更新
```

### 目录命名

- 使用小写 + 连字符：`my-project`
- 避免空格和下划线
- 英文名称，必要时加中文说明在 README 中

---

## 🔧 工具与环境

- **主工作区**：`/home/node/.openclaw/workspace/`
- **Git 远程**：https://github.com/wesley9747/openclaw-code
- **分支策略**：main（稳定）+ feature/*（功能分支）
- **代码审查**：通过 GitHub PR

---

## 📞 联系

作者：大龙虾 🦞

---

**最后更新**：2025-03-15
