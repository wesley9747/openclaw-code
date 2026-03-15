#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
彩票 OCR 识别测试脚本（最终优化版）
"""

import requests
import json
import re
import sys
from typing import List, Dict

API_KEY = "fUvloJHZWITEkWUjT1whximb"
SECRET_KEY = "5xzedObnVENwoJd5uQ4xlnSaPId4TeHI"

def get_access_token():
    print("🔑 正在获取 access_token...")
    resp = requests.post("https://aip.baidubce.com/oauth/2.0/token", params={
        "grant_type": "client_credentials", "client_id": API_KEY, "client_secret": SECRET_KEY
    }, timeout=10)
    data = resp.json()
    if "access_token" in data:
        print(f"✅ 获取成功！有效期 {data['expires_in']}秒")
        return data["access_token"]
    print(f"❌ 获取失败: {data}")
    return None

def recognize_image(token, image_path=None):
    url = f"https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic?access_token={token}"
    with open(image_path, "rb") as f:
        import base64
        data = {"image": base64.b64encode(f.read())}
    resp = requests.post(url, data=data, headers={"Content-Type": "application/x-www-form-urlencoded"}, timeout=30)
    return resp.json()

def split_red_balls_candidates(s: str) -> List[List[int]]:
    """回溯生成所有满足6红球(1-33)的分割"""
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
                backtrack(idx+1, cur)
                cur.pop()
        # 2位
        if idx+1 < n:
            d2 = int(s[idx:idx+2])
            if 10 <= d2 <= 33:
                cur.append(d2)
                backtrack(idx+2, cur)
                cur.pop()
    backtrack(0, [])
    return results

def parse_lottery_numbers(words: List[str]) -> Dict:
    all_text = " ".join(words)
    print(f"📝 文本: {all_text[:200]}...")
    
    # 期号
    period = None
    for p in [r"开奖期[：:]\s*(20\d{4,5})", r"销售期[：:]\s*(20\d{4,5})", r"(20\d{4,5})-\d{1,2}"]:
        m = re.search(p, all_text)
        if m:
            period = re.sub(r"[^\d]", "", m.group(1))
            print(f"✅ 期号: {period}")
            break
    
    # 类型
    ttype = "双色球" if "双色球" in all_text else ("大乐透" if "大乐透" in all_text else None)
    if ttype:
        print(f"✅ 类型: {ttype}")
    
    tickets = []
    if ttype == "双色球":
        for letter, red_str, blue in re.findall(r"([A-F])\.\s*([\d\s]+)-(\d+)", all_text):
            red_raw = red_str.strip()
            blue_fmt = f"{int(blue):02d}"
            final = None
            
            # 预处理：移除空格，准备回溯
            red_clean = red_raw.replace(" ", "")
            
            # 尝试回溯算法（适用于任何连续串，包括原含空格但移除后）
            candidates = split_red_balls_candidates(red_clean)
            if candidates:
                # 优先选含33的解
                has33 = [arr for arr in candidates if 33 in arr]
                if has33:
                    best = has33[0] if len(has33)==1 else min(has33, key=lambda a: sum(a))
                elif len(candidates) == 1:
                    best = candidates[0]
                else:
                    best = min(candidates, key=lambda a: sum(a))
                final = [f"{x:02d}" for x in sorted(best)]
                print(f"  注{letter}: '{red_raw}' → {final}")
            else:
                print(f"  注{letter}: ⚠️ 无法解析 '{red_raw}'")
                continue
            
            tickets.append({"ticket_id": letter, "red_balls": final, "blue_ball": blue_fmt})
    
    return {"period": period, "lottery_type": ttype, "tickets": tickets, "raw_words": words}

def main():
    print("="*60 + "\n🧪 彩票 OCR 测试（百度智能云）\n" + "="*60)
    token = get_access_token()
    if not token: sys.exit(1)
    
    path = "/home/node/.openclaw/media/inbound/633a1230-5d42-4e32-b5af-dc9edad7b282.webp"
    res = recognize_image(token, image_path=path)
    words = [w["words"] for w in res.get("words_result", [])]
    
    parsed = parse_lottery_numbers(words)
    
    print("\n" + "="*60 + "\n🎯 结果汇总\n" + "="*60)
    if parsed["period"]: print(f"期号: {parsed['period']}")
    if parsed["lottery_type"]:
        print(f"类型: {parsed['lottery_type']}")
        for t in parsed["tickets"]:
            print(f"  注{t['ticket_id']}: 红球 {' '.join(t['red_balls'])} | 蓝球 {t['blue_ball']}")
    else:
        print("⚠️ 未识别彩票类型")

if __name__ == "__main__":
    main()
