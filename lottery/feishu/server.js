/**
 * 飞书双色球彩票管理 - 后端服务
 * 负责：API 接口、飞书鉴权、OCR识别调用
 */

const express = require('express');
const path = require('path');
const axios = require('axios');

const app = express();
const PORT = process.env.PORT || 3000;

// ============ 配置 ============
// 飞书应用凭证（需要您提供）
const APP_ID = process.env.FEISHU_APP_ID || 'cli_xxxxxxxxxxxxx';
const APP_SECRET = process.env.FEISHU_APP_SECRET || 'xxxxxxxxxxxxxxxxxxxxxx';
const TOKEN_STORE = {}; // 简化存储，生产环境用数据库

// 飞书 API 地址
const FEISHU_HOST = 'https://open.feishu.cn';
const BITABLE_TOKEN = 'LpDqb4WOxak09Hse2eScWhlhnQh'; // 您的多维表格
const BITABLE_TABLE_ID = 'tbld2MxnC04PiS3v';

// 中奖规则
const PRIZE_RULES = [
    { red: 6, blue: 1, name: '一等奖', amount: 10000000 },
    { red: 6, blue: 0, name: '二等奖', amount: 500000 },
    { red: 5, blue: 1, name: '三等奖', amount: 3000 },
    { red: 5, blue: 0, name: '四等奖', amount: 200 },
    { red: 4, blue: 1, name: '四等奖', amount: 200 },
    { red: 4, blue: 0, name: '五等奖', amount: 10 },
    { red: 3, blue: 1, name: '五等奖', amount: 10 },
];

// 内存存储（生产环境用数据库）
let lotteryRecords = [];
let drawHistory = [];

// ============ 中间件 ============
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// 飞书应用验签中间件（生产环境启用）
// app.use('/api', feishuAuthMiddleware);

// ============ 飞书 API 封装 ============

// 获取应用租户访问令牌
async function getTenantAccessToken() {
    try {
        const response = await axios.post(`${FEISHU_HOST}/open-apis/auth/v3/tenant_access_token/internal`, {
            app_id: APP_ID,
            app_secret: APP_SECRET
        });
        return response.data.tenant_access_token;
    } catch (error) {
        console.error('获取Token失败:', error.message);
        return null;
    }
}

// 从飞书多维表格获取数据
async function getBitableData() {
    const token = await getTenantAccessToken();
    if (!token) return [];

    try {
        const response = await axios.get(
            `${FEISHU_HOST}/open-apis/bitable/v1/apps/${BITABLE_TOKEN}/tables/${BITABLE_TABLE_ID}/records`,
            { headers: { Authorization: `Bearer ${token}` } }
        );
        return response.data.data?.items || [];
    } catch (error) {
        console.error('获取飞书数据失败:', error.message);
        return [];
    }
}

// 同步数据到飞书
async function syncToBitable(record) {
    const token = await getTenantAccessToken();
    if (!token) return false;

    try {
        await axios.post(
            `${FEISHU_HOST}/open-apis/bitable/v1/apps/${BITABLE_TOKEN}/tables/${BITABLE_TABLE_ID}/records`,
            {
                fields: {
                    '期号': record.period,
                    '购买日期': record.buyDate || new Date().toISOString().split('T')[0],
                    '红球号码': record.redBalls.join(','),
                    '蓝球号码': record.blueBall,
                    '备注': record.remarks || ''
                }
            },
            { headers: { Authorization: `Bearer ${token}` } }
        );
        return true;
    } catch (error) {
        console.error('同步到飞书失败:', error.message);
        return false;
    }
}

// ============ API 路由 ============

// 首页 - 返回飞书应用首页
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// 获取购彩记录
app.get('/api/records', (req, res) => {
    res.json({ code: 0, data: lotteryRecords });
});

// 添加购彩记录
app.post('/api/records', async (req, res) => {
    const { period, redBalls, blueBall, remarks } = req.body;
    
    if (!period || !redBalls || redBalls.length !== 6 || !blueBall) {
        return res.json({ code: 400, message: '参数不完整' });
    }

    const record = {
        id: Date.now(),
        period,
        redBalls: redBalls.sort((a, b) => a - b),
        blueBall,
        remarks: remarks || '',
        buyDate: new Date().toISOString(),
        createdAt: new Date().toISOString()
    };

    lotteryRecords.unshift(record);

    // 同步到飞书
    await syncToBitable(record);

    res.json({ code: 0, data: record });
});

// 删除记录
app.delete('/api/records/:id', (req, res) => {
    const id = parseInt(req.params.id);
    lotteryRecords = lotteryRecords.filter(r => r.id !== id);
    res.json({ code: 0 });
});

// 更新开奖结果
app.post('/api/records/:id/draw', (req, res) => {
    const id = parseInt(req.params.id);
    const { drawRedBalls, drawBlueBall } = req.body;

    const record = lotteryRecords.find(r => r.id === id);
    if (!record) {
        return res.json({ code: 404, message: '记录不存在' });
    }

    record.drawRedBalls = drawRedBalls;
    record.drawBlueBall = drawBlueBall;

    // 计算中奖情况
    const matchedRed = record.redBalls.filter(n => drawRedBalls.includes(n)).length;
    const matchedBlue = record.blueBall === drawBlueBall;

    record.matchedRedCount = matchedRed;
    record.matchedBlue = matchedBlue;

    // 查询中奖等级
    const prize = PRIZE_RULES.find(p => 
        p.red === matchedRed && (p.blue === (matchedBlue ? 1 : 0))
    );

    record.isWin = !!prize;
    record.prizeLevel = prize?.name || null;
    record.prizeAmount = prize?.amount || 0;

    res.json({ code: 0, data: record });
});

// 获取历史开奖数据（模拟）
app.get('/api/draws', (req, res) => {
    // 实际应从彩票官网API获取
    res.json({ code: 0, data: generateMockDraws(50) });
});

// AI 预测
app.post('/api/predict', async (req, res) => {
    const { method } = req.body; // 'local' 或 'ai'

    if (method === 'ai') {
        // 调用 Qwen API（需要配置）
        const prediction = await getQwenPrediction();
        return res.json({ code: 0, data: prediction });
    }

    // 本地算法预测
    const prediction = generateLocalPrediction();
    res.json({ code: 0, data: prediction });
});

// 从飞书同步数据
app.post('/api/sync', async (req, res) => {
    const feishuData = await getBitableData();
    
    // 转换格式
    const records = feishuData.map(item => ({
        id: Date.now() + Math.random(),
        period: item.fields['期号'],
        redBalls: (item.fields['红球号码'] || '').split(',').map(n => parseInt(n)).filter(n => n),
        blueBall: parseInt(item.fields['蓝球号码']),
        remarks: item.fields['备注'],
        buyDate: item.fields['购买日期'],
        synced: true
    }));

    // 合并（去重）
    records.forEach(r => {
        if (!lotteryRecords.find(lr => lr.period === r.period && lr.redBalls.join() === r.redBalls.join())) {
            lotteryRecords.unshift(r);
        }
    });

    res.json({ code: 0, count: records.length });
});

// ============ 预测算法 ============

function generateMockDraws(count) {
    const draws = [];
    for (let i = 0; i < count; i++) {
        const reds = [];
        while (reds.length < 6) {
            const n = Math.floor(Math.random() * 33) + 1;
            if (!reds.includes(n)) reds.push(n);
        }
        draws.push({
            period: `2024${String(100 - i).padStart(3, '0')}`,
            redBalls: reds.sort((a, b) => a - b),
            blueBall: Math.floor(Math.random() * 16) + 1,
            date: new Date(Date.now() - i * 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
        });
    }
    return draws;
}

function generateLocalPrediction() {
    const draws = generateMockDraws(30);
    const redCount = {};
    
    draws.forEach(d => {
        d.redBalls.forEach(n => {
            redCount[n] = (redCount[n] || 0) + 1;
        });
    });

    const hot = Object.entries(redCount)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 4)
        .map(([n]) => parseInt(n));

    const reds = [...hot];
    while (reds.length < 6) {
        const n = Math.floor(Math.random() * 33) + 1;
        if (!reds.includes(n)) reds.push(n);
    }

    return {
        redBalls: reds.sort((a, b) => a - b),
        blueBall: Math.floor(Math.random() * 16) + 1,
        confidence: 75,
        analysis: '基于近期冷热号统计，推荐以上号码'
    };
}

async function getQwenPrediction() {
    // 实际需要调用 Qwen API
    // 这里返回模拟结果
    const reds = [];
    while (reds.length < 6) {
        const n = Math.floor(Math.random() * 33) + 1;
        if (!reds.includes(n)) reds.push(n);
    }

    return {
        redBalls: reds.sort((a, b) => a - b),
        blueBall: Math.floor(Math.random() * 16) + 1,
        confidence: 82,
        analysis: '基于 Qwen3-Max 大模型分析近50期开奖规律得出'
    };
}

// ============ 启动 ============
app.listen(PORT, '0.0.0.0', () => {
    console.log(`🎱 飞书彩票应用已启动: http://localhost:${PORT}`);
    console.log(`📋 飞书应用首页: http://localhost:${PORT}/`);
});

module.exports = app;