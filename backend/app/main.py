#!/usr/bin/env python3
"""
彩票智查 - 后端 API (FastAPI)
"""

from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import jwt
import time
from datetime import datetime, timedelta
from app.services.ocr_service import ocr_service
from app.services.lottery_parser import LotteryParser

# ============ 配置 ============
class Settings:
    JWT_SECRET = "dev-secret-change-in-production"
    JWT_ALGORITHM = "HS256"
    JWT_EXPIRE_DAYS = 7
    FRONTEND_URL = "http://localhost:5173"

settings = Settings()

# ============ FastAPI 应用 ============
app = FastAPI(title="彩票智查 API", version="1.0.0")

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=[settings.FRONTEND_URL],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ============ 数据模型 ============
class User(BaseModel):
    id: int
    feishu_user_id: str
    email: str
    name: str
    avatar: Optional[str] = None

class LoginRequest(BaseModel):
    code: str

class LoginResponse(BaseModel):
    access_token: str
    user: User

class RecordCreate(BaseModel):
    period: str
    lottery_type: str
    red_balls: List[str]
    blue_ball: str
    bet_amount: float = 2.0
    note: Optional[str] = None

class RecordResponse(BaseModel):
    id: int
    period: str
    lottery_type: str
    red_balls: List[str]
    blue_ball: str
    draw_red_balls: Optional[List[str]] = None
    draw_blue_ball: Optional[str] = None
    is_win: bool = False
    prize_level: Optional[str] = None
    prize_amount: float = 0.0
    bet_amount: float
    note: Optional[str] = None
    created_at: str

# ============ 工具函数 ============
def create_jwt(user_id: int, feishu_user_id: str, name: str, email: str) -> str:
    """生成 JWT token"""
    payload = {
        "user_id": user_id,
        "feishu_user_id": feishu_user_id,
        "name": name,
        "email": email,
        "exp": time.time() + settings.JWT_EXPIRE_DAYS * 86400
    }
    return jwt.encode(payload, settings.JWT_SECRET, algorithm=settings.JWT_ALGORITHM)

def verify_jwt(token: str) -> dict:
    """验证 JWT token"""
    try:
        payload = jwt.decode(token, settings.JWT_SECRET, algorithms=[settings.JWT_ALGORITHM])
        return payload
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Token expired")
    except jwt.InvalidTokenError:
        raise HTTPException(status_code=401, detail="Invalid token")

# ============ 内存数据库（开发用）============
# 生产环境请替换为 PostgreSQL
fake_db = {
    "users": {},  # feishu_user_id -> User dict
    "records": [],  # List[Record]
    "next_id": 1
}

# ============ 路由 ============

@app.get("/health")
async def health():
    return {"status": "ok", "timestamp": datetime.now().isoformat()}

# 认证
@app.post("/api/auth/feishu-login", response_model=LoginResponse)
async def feishu_login(req: LoginRequest):
    """
    飞书登录（Mock版本）
    真实场景：用 code 换取飞书 access_token，再获取用户信息
    """
    # TODO: 实现真实飞书 API 调用
    # 暂时返回测试用户
    mock_user = {
        "feishu_user_id": f"ou_mock_{int(time.time())}",
        "email": "test@example.com",
        "name": "测试用户",
        "avatar": None
    }
    
    # 保存/更新用户
    user_id = fake_db["users"].get(mock_user["feishu_user_id"], {"id": fake_db["next_id"]})["id"]
    if mock_user["feishu_user_id"] not in fake_db["users"]:
        fake_db["users"][mock_user["feishu_user_id"]] = {
            "id": user_id,
            **mock_user
        }
        fake_db["next_id"] += 1
    
    token = create_jwt(user_id, mock_user["feishu_user_id"], mock_user["name"], mock_user["email"])
    
    return LoginResponse(
        access_token=token,
        user=User(id=user_id, **mock_user)
    )

# OCR 识别
@app.post("/api/ocr/recognize")
async def recognize_image(image_base64: str):
    """调用百度 OCR 识别图片文字"""
    try:
        result = ocr_service.recognize_image(image_base64)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 解析彩票
@app.post("/api/ocr/parse-tickets", response_model=List[dict])
async def parse_tickets(ocr_result: dict):
    """从 OCR 结果解析彩票投注单"""
    try:
        tickets = ocr_service.parse_lottery_tickets(ocr_result)
        return tickets
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 记录 CRUD
@app.post("/api/records", response_model=RecordResponse)
async def create_record(record: RecordCreate, token_data: dict = Depends(verify_jwt)):
    """创建投注记录"""
    record_id = fake_db["next_id"]
    now = datetime.now().isoformat()
    
    # 保存
    new_record = {
        "id": record_id,
        "period": record.period,
        "lottery_type": record.lottery_type,
        "red_balls": record.red_balls,
        "blue_ball": record.blue_ball,
        "draw_red_balls": None,
        "draw_blue_ball": None,
        "is_win": False,
        "prize_level": None,
        "prize_amount": 0.0,
        "bet_amount": record.bet_amount,
        "note": record.note,
        "created_at": now,
        "user_id": token_data["user_id"]
    }
    fake_db["records"].append(new_record)
    fake_db["next_id"] += 1
    
    return RecordResponse(**new_record)

@app.get("/api/records", response_model=List[RecordResponse])
async def list_records(token_data: dict = Depends(verify_jwt)):
    """获取当前用户的所有记录"""
    user_records = [r for r in fake_db["records"] if r.get("user_id") == token_data["user_id"]]
    return [RecordResponse(**r) for r in user_records]

@app.get("/api/records/{record_id}", response_model=RecordResponse)
async def get_record(record_id: int, token_data: dict = Depends(verify_jwt)):
    """获取单条记录"""
    for r in fake_db["records"]:
        if r["id"] == record_id and r.get("user_id") == token_data["user_id"]:
            return RecordResponse(**r)
    raise HTTPException(status_code=404, detail="Record not found")

# 开奖查询（模拟数据）
@app.get("/api/draws/latest")
async def get_latest_draws(lottery_type: str = "双色球", count: int = 10):
    """获取最新开奖结果（模拟数据）"""
    # TODO: 对接真实开奖数据源
    return {
        "lottery_type": lottery_type,
        "draws": [
            {
                "period": "2026024",
                "date": "2026-03-05",
                "red_balls": ["03", "07", "13", "18", "30", "32"],
                "blue_ball": "03"
            }
        ]
    }

# 统计
@app.get("/api/stats/summary")
async def get_stats(token_data: dict = Depends(verify_jwt)):
    """用户年度统计"""
    user_records = [r for r in fake_db["records"] if r.get("user_id") == token_data["user_id"]]
    total_bet = sum(r["bet_amount"] for r in user_records)
    total_win = sum(r["prize_amount"] for r in user_records if r["is_win"])
    roi = ((total_win - total_bet) / total_bet * 100) if total_bet > 0 else 0
    
    return {
        "total_bet": total_bet,
        "total_win": total_win,
        "roi": round(roi, 2),
        "record_count": len(user_records),
        "win_count": sum(1 for r in user_records if r["is_win"])
    }

@app.post("/api/records/{record_id}/check")
async def check_win(record_id: int, token_data: dict = Depends(verify_jwt)):
    """检查是否中奖（与最新开奖对比）"""
    # 简化的中奖逻辑
    record = None
    for r in fake_db["records"]:
        if r["id"] == record_id and r.get("user_id") == token_data["user_id"]:
            record = r
            break
    if not record:
        raise HTTPException(status_code=404, detail="Record not found")
    
    # 获取最新开奖（模拟）
    latest_draw = {
        "red_balls": ["03", "07", "13", "18", "30", "32"],
        "blue_ball": "03"
    }
    
    # 比对
    red_match = sum(1 for r in record["red_balls"] if r in latest_draw["red_balls"])
    blue_match = 1 if record["blue_ball"] == latest_draw["blue_ball"] else 0
    
    # 判断奖级
    prize_map = {
        (6, 1): "一等奖",
        (6, 0): "二等奖",
        (5, 1): "三等奖",
        (5, 0): "四等奖",
        (4, 1): "四等奖",
        (4, 0): "五等奖",
        (3, 1): "五等奖",
        (2, 1): "六等奖",
        (1, 1): "六等奖",
        (0, 1): "六等奖"
    }
    
    prize_level = prize_map.get((red_match, blue_match))
    is_win = prize_level is not None
    prize_amount = 0
    if prize_level == "一等奖": prize_amount = 5000000
    elif prize_level == "二等奖": prize_amount = 250000
    elif prize_level == "三等奖": prize_amount = 3000
    elif prize_level == "四等奖": prize_amount = 200
    elif prize_level == "五等奖": prize_amount = 10
    elif prize_level == "六等奖": prize_amount = 5
    
    # 更新记录
    record["draw_red_balls"] = latest_draw["red_balls"]
    record["draw_blue_ball"] = latest_draw["blue_ball"]
    record["is_win"] = is_win
    record["prize_level"] = prize_level
    record["prize_amount"] = prize_amount
    
    return {
        "is_win": is_win,
        "prize_level": prize_level,
        "prize_amount": prize_amount,
        "red_match": red_match,
        "blue_match": blue_match
    }

# 启动
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
