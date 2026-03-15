#!/bin/bash

# 双色球彩票管理 Android App - GitHub 部署脚本

cd /home/node/.openclaw/workspace/lottery-app

# 初始化 Git 仓库（如果尚未初始化）
if [ ! -d .git ]; then
    git init
    git checkout -b main
fi

# 添加 .gitignore（如果存在）
if [ -f .gitignore ]; then
    git add .gitignore
fi

# 添加所有项目文件
git add .

# 创建提交
git commit -m "Initial commit: 双色球彩票管理 Android App v1.0"

# 检查是否已配置 GitHub CLI
if ! command -v gh &> /dev/null; then
    echo "Error: GitHub CLI (gh) is not installed"
    exit 1
fi

# 检查是否已登录 GitHub
if ! gh auth status &> /dev/null; then
    echo "Please login to GitHub first:"
    echo "gh auth login"
    exit 1
fi

# 创建 GitHub 仓库（如果不存在）
REPO_NAME="lottery-app"
if ! gh repo view $REPO_NAME &> /dev/null; then
    gh repo create $REPO_NAME --private --source=. --push
else
    # 添加 remote（如果需要）
    git remote add origin https://github.com/$(gh api /user --jq '.login')/$REPO_NAME.git 2>/dev/null || true
    git push -u origin main
fi

echo "Successfully pushed to GitHub!"