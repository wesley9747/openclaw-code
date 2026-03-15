# OCR 服务
import requests
import base64
import json
from typing import Dict, Optional
from app.core.config import settings

class OCRService:
    """百度智能云 OCR 服务"""
    
    def __init__(self):
        self.api_key = settings.BAIDU_OCR_API_KEY
        self.secret_key = settings.BAIDU_OCR_SECRET_KEY
        self.access_token = None
        self.token_expires_at = 0
    
    def _get_access_token(self) -> str:
        """获取或刷新 access_token"""
        import time
        if self.access_token and time.time() < self.token_expires_at - 300:
            return self.access_token
        
        url = "https://aip.baidubce.com/oauth/2.0/token"
        data = {
            "grant_type": "client_credentials",
            "client_id": self.api_key,
            "client_secret": self.secret_key
        }
        resp = requests.post(url, data=data, timeout=10)
        resp.raise_for_status()
        result = resp.json()
        self.access_token = result["access_token"]
        self.token_expires_at = time.time() + result.get("expires_in", 2592000)
        return self.access_token
    
    def recognize_image(self, image_base64: str) -> Dict:
        """识别图片中的文字（通用高精版）"""
        token = self._get_access_token()
        url = f"https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic?access_token={token}"
        headers = {"Content-Type": "application/x-www-form-urlencoded"}
        data = {"image": image_base64}
        resp = requests.post(url, data=data, headers=headers, timeout=30)
        resp.raise_for_status()
        return resp.json()
    
    def parse_lottery_tickets(self, ocr_result: Dict) -> list:
        """解析 OCR 结果为彩票投注单数据"""
        # 这里调用我们之前开发的解析算法
        from app.services.lottery_parser import LotteryParser
        words = [item["words"] for item in ocr_result.get("words_result", [])]
        parser = LotteryParser()
        return parser.parse(words)

# 单例
ocr_service = OCRService()
