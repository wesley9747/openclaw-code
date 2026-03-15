"""
彩票号码解析器（基于百度 OCR 结果）
"""

import re
from typing import List, Dict

class LotteryParser:
    """彩票 OCR 结果解析器"""
    
    def parse(self, words: List[str]) -> list:
        """主解析入口"""
        all_text = " ".join(words)
        
        # 1. 提取期号
        period = self._extract_period(all_text)
        
        # 2. 检测类型
        lottery_type = "双色球" if "双色球" in all_text else None
        if not lottery_type:
            return []
        
        # 3. 解析每注号码
        tickets = []
        ticket_pattern = r"([A-F])\.\s*([\d\s]+)-(\d+)"
        matches = re.findall(ticket_pattern, all_text)
        
        for letter, red_str, blue in matches:
            red_clean = red_str.strip().replace(" ", "")
            blue_fmt = f"{int(blue):02d}"
            
            # 使用回溯算法解析红球
            red_balls = self._split_red_balls(red_clean)
            if red_balls:
                tickets.append({
                    "ticket_id": letter,
                    "red_balls": [f"{x:02d}" for x in sorted(red_balls)],
                    "blue_ball": blue_fmt,
                    "period": period,
                    "lottery_type": lottery_type
                })
        
        return tickets
    
    def _extract_period(self, text: str) -> str:
        """提取期号"""
        patterns = [
            r"开奖期[：:]\s*(20\d{4,5})",
            r"销售期[：:]\s*(20\d{4,5})",
            r"(20\d{4,5})-\d{1,2}"
        ]
        for p in patterns:
            m = re.search(p, text)
            if m:
                return re.sub(r"[^\d]", "", m.group(1))
        return ""
    
    def _split_red_balls(self, s: str) -> List[int]:
        """回溯算法：将连续数字串分割为6个红球 (1-33)"""
        results = []
        n = len(s)
        
        def backtrack(idx, cur):
            if idx == n:
                if len(cur) == 6 and all(1 <= x <= 33 for x in cur):
                    results.append(cur.copy())
                return
            need = 6 - len(cur)
            rem = n - idx
            if rem < need or rem > need * 2:
                return
            # 1位
            if idx < n:
                d1 = int(s[idx])
                if 1 <= d1 <= 9:
                    cur.append(d1)
                    backtrack(idx + 1, cur)
                    cur.pop()
            # 2位
            if idx + 1 < n:
                d2 = int(s[idx:idx+2])
                if 10 <= d2 <= 33:
                    cur.append(d2)
                    backtrack(idx + 2, cur)
                    cur.pop()
        
        backtrack(0, [])
        
        if not results:
            return []
        # 多解时优先选含33的，其次总和小的
        has33 = [arr for arr in results if 33 in arr]
        if has33:
            return has33[0] if len(has33) == 1 else min(has33, key=lambda a: sum(a))
        return min(results, key=lambda a: sum(a))
