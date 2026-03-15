from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel
from typing import List
import base64
import json

from app.services.ocr_service import ocr_service
from app.schemas.ticket import TicketParseResponse

router = APIRouter(prefix="/api/ocr", tags=["ocr"])

class ImageRequest(BaseModel):
    image_base64: str  # data:image/jpeg;base64,...

class ParseRequest(BaseModel):
    ocr_result: dict  # 百度 OCR 返回的原始结果

@router.post("/recognize")
async def recognize(req: ImageRequest):
    """识别图片文字（调用百度 OCR）"""
    try:
        # 移除 data:image 前缀
        img_data = req.image_base64.split(",")[-1] if "," in req.image_base64 else req.image_base64
        result = ocr_service.recognize_image(img_data)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/parse-tickets", response_model=List[TicketParseResponse])
async def parse_tickets(req: ParseRequest):
    """从 OCR 结果解析出彩票投注单"""
    try:
        tickets = ocr_service.parse_lottery_tickets(req.ocr_result)
        return tickets
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
