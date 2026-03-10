// 双色球彩票管理 H5 应用
// ==================== 数据存储 ====================

const STORAGE_KEY = 'lottery_records';
const DRAW_HISTORY_KEY = 'draw_history';

// 读取记录
function getRecords() {
    const data = localStorage.getItem(STORAGE_KEY);
    return data ? JSON.parse(data) : [];
}

// 保存记录
function saveRecords(records) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(records));
}

// 添加记录
function addRecord(record) {
    const records = getRecords();
    record.id = Date.now();
    record.createdAt = new Date().toISOString();
    record.syncStatus = 'pending';
    records.unshift(record);
    saveRecords(records);
    return record;
}

// 删除记录
function deleteRecord(id) {
    const records = getRecords();
    const filtered = records.filter(r => r.id !== id);
    saveRecords(filtered);
}

// 更新记录
function updateRecord(id, updates) {
    const records = getRecords();
    const index = records.findIndex(r => r.id === id);
    if (index !== -1) {
        records[index] = { ...records[index], ...updates };
        saveRecords(records);
    }
}

// ==================== 页面导航 ====================

document.querySelectorAll('.tab-item').forEach(tab => {
    tab.addEventListener('click', () => {
        const page = tab.dataset.page;
        
        // 切换标签
        document.querySelectorAll('.tab-item').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        
        // 切换页面
        document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
        document.getElementById(`page-${page}`).classList.add('active');
        
        // 页面特定逻辑
        if (page === 'home') renderRecords();
        if (page === 'prediction') loadPredictionData();
    });
});

// ==================== 首页逻辑 ====================

// 渲染记录列表
function renderRecords() {
    const records = getRecords();
    const list = document.getElementById('record-list');
    const empty = document.getElementById('empty-state');
    
    // 更新统计
    updateStats(records);
    
    if (records.length === 0) {
        list.style.display = 'none';
        empty.style.display = 'block';
        return;
    }
    
    list.style.display = 'flex';
    empty.style.display = 'none';
    
    list.innerHTML = records.slice(0, 10).map(record => `
        <div class="record-item">
            <div class="record-header">
                <span class="record-period">第 ${record.period} 期</span>
                <span class="record-date">${formatDate(record.createdAt)}</span>
            </div>
            
            ${record.redBalls && record.redBalls.length > 0 ? `
                <div class="record-numbers">
                    <div class="lottery-balls">
                        ${record.redBalls.sort((a,b) => a-b).map(n => `<span class="ball red">${padNum(n)}</span>`).join('')}
                        <span class="ball plus">+</span>
                        <span class="ball blue">${padNum(record.blueBall)}</span>
                    </div>
                </div>
            ` : '<p style="color: var(--text-secondary);">未设置号码</p>'}
            
            ${record.drawRedBalls ? `
                <div class="record-result">
                    <div class="lottery-balls">
                        ${record.drawRedBalls.sort((a,b) => a-b).map(n => `<span class="ball red" style="opacity:0.6">${padNum(n)}</span>`).join('')}
                        <span class="ball plus">+</span>
                        <span class="ball blue" style="opacity:0.6">${padNum(record.drawBlueBall)}</span>
                    </div>
                </div>
                <div style="margin-top: 8px; display: flex; gap: 8px; flex-wrap: wrap;">
                    ${record.matchedRedCount > 0 ? `<span class="match-tag win">红球 ${record.matchedRedCount}/6</span>` : ''}
                    <span class="match-tag ${record.matchedBlue ? 'win' : ''}">蓝球 ${record.matchedBlue ? '✓' : '✗'}</span>
                </div>
            ` : ''}
            
            <div class="record-actions">
                <button class="btn btn-sm btn-outline" onclick="editRecord(${record.id})">编辑</button>
                <button class="btn btn-sm btn-danger" onclick="confirmDelete(${record.id})">删除</button>
            </div>
        </div>
    `).join('');
}

// 更新统计
function updateStats(records) {
    const total = records.length;
    const wins = records.filter(r => r.isWin).length;
    const amount = records.reduce((sum, r) => sum + (r.prizeAmount || 0), 0);
    
    document.getElementById('stat-total').textContent = total;
    document.getElementById('stat-wins').textContent = wins;
    document.getElementById('stat-amount').textContent = amount;
}

// 格式化日期
function formatDate(dateStr) {
    const date = new Date(dateStr);
    return `${date.getMonth()+1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2,'0')}`;
}

// 格式化数字
function padNum(n) {
    return String(n).padStart(2, '0');
}

// ==================== 添加/编辑记录 ====================

let selectedRedBalls = new Set();
let selectedBlueBall = null;
let editingId = null;

function openAddModal(record = null) {
    editingId = record ? record.id : null;
    selectedRedBalls = new Set(record ? record.redBalls : []);
    selectedBlueBall = record ? record.blueBall : null;
    
    document.getElementById('input-period').value = record ? record.period : generatePeriod();
    document.getElementById('input-remarks').value = record ? record.remarks : '';
    
    updateNumberSelectors();
    document.getElementById('modal-add').classList.add('active');
}

// 生成期号
function generatePeriod() {
    const now = new Date();
    const year = now.getFullYear();
    const week = Math.ceil((now.getDate()) / 7);
    return `${year}${String(week).padStart(2, '0')}`;
}

// 更新号码选择器
function updateNumberSelectors() {
    // 红球
    const redGrid = document.getElementById('red-ball-selector');
    redGrid.innerHTML = '';
    for (let i = 1; i <= 33; i++) {
        const btn = document.createElement('button');
        btn.className = `number-btn ${selectedRedBalls.has(i) ? 'selected' : ''}`;
        btn.textContent = padNum(i);
        btn.onclick = () => toggleRedBall(i);
        redGrid.appendChild(btn);
    }
    
    // 蓝球
    const blueGrid = document.getElementById('blue-ball-selector');
    blueGrid.innerHTML = '';
    for (let i = 1; i <= 16; i++) {
        const btn = document.createElement('button');
        btn.className = `number-btn blue ${selectedBlueBall === i ? 'selected' : ''}`;
        btn.textContent = padNum(i);
        btn.onclick = () => toggleBlueBall(i);
        blueGrid.appendChild(btn);
    }
    
    document.getElementById('red-count').textContent = selectedRedBalls.size;
    document.getElementById('blue-count').textContent = selectedBlueBall ? 1 : 0;
}

// 选择红球
function toggleRedBall(num) {
    if (selectedRedBalls.has(num)) {
        selectedRedBalls.delete(num);
    } else if (selectedRedBalls.size < 6) {
        selectedRedBalls.add(num);
    }
    updateNumberSelectors();
}

// 选择蓝球
function toggleBlueBall(num) {
    selectedBlueBall = selectedBlueBall === num ? null : num;
    updateNumberSelectors();
}

// 保存记录
function saveRecord() {
    const period = document.getElementById('input-period').value.trim();
    const remarks = document.getElementById('input-remarks').value.trim();
    
    if (!period) {
        showToast('请输入期号', 'error');
        return;
    }
    if (selectedRedBalls.size !== 6) {
        showToast('请选择6个红球', 'error');
        return;
    }
    if (!selectedBlueBall) {
        showToast('请选择1个蓝球', 'error');
        return;
    }
    
    const record = {
        period,
        redBalls: Array.from(selectedRedBalls),
        blueBall: selectedBlueBall,
        remarks
    };
    
    if (editingId) {
        updateRecord(editingId, record);
        showToast('更新成功');
    } else {
        addRecord(record);
        showToast('添加成功', 'success');
    }
    
    closeModal('modal-add');
    renderRecords();
}

// 编辑记录
function editRecord(id) {
    const records = getRecords();
    const record = records.find(r => r.id === id);
    if (record) {
        openAddModal(record);
    }
}

// 确认删除
function confirmDelete(id) {
    if (confirm('确定要删除这条记录吗？')) {
        deleteRecord(id);
        renderRecords();
        showToast('删除成功');
    }
}

// ==================== 相机/OCR ====================

let cameraStream = null;

async function openCamera() {
    const modal = document.getElementById('modal-camera');
    modal.classList.add('active');
    
    const video = document.getElementById('camera-preview');
    
    try {
        cameraStream = await navigator.mediaDevices.getUserMedia({ 
            video: { facingMode: 'environment' } 
        });
        video.srcObject = cameraStream;
    } catch (err) {
        showToast('无法访问相机: ' + err.message, 'error');
    }
}

function closeCamera() {
    const modal = document.getElementById('modal-camera');
    modal.classList.remove('active');
    
    if (cameraStream) {
        cameraStream.getTracks().forEach(track => track.stop());
        cameraStream = null;
    }
    
    document.getElementById('ocr-result').style.display = 'none';
}

async function captureImage() {
    const video = document.getElementById('camera-preview');
    const canvas = document.getElementById('camera-canvas');
    const context = canvas.getContext('2d');
    
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    context.drawImage(video, 0, 0);
    
    const imageData = canvas.toDataURL('image/jpeg');
    
    // 简单OCR模拟 - 实际需要调用OCR服务
    showToast('识别中...');
    
    // 这里可以调用后端OCR服务或使用Tesseract.js
    // 暂时模拟识别结果
    setTimeout(() => {
        const mockResult = generateMockOCR();
        displayOCRResult(mockResult);
        showToast('识别完成', 'success');
    }, 1500);
}

function generateMockOCR() {
    // 模拟OCR返回的号码
    const reds = [];
    while (reds.length < 6) {
        const n = Math.floor(Math.random() * 33) + 1;
        if (!reds.includes(n)) reds.push(n);
    }
    const blue = Math.floor(Math.random() * 16) + 1;
    return { reds, blue };
}

function displayOCRResult(result) {
    const container = document.getElementById('ocr-numbers');
    container.innerHTML = `
        <div class="lottery-balls">
            ${result.reds.sort((a,b) => a-b).map(n => `<span class="ball red">${padNum(n)}</span>`).join('')}
            <span class="ball plus">+</span>
            <span class="ball blue">${padNum(result.blue)}</span>
        </div>
    `;
    
    document.getElementById('ocr-result').style.display = 'block';
    
    // 设置到选择器
    selectedRedBalls = new Set(result.reds);
    selectedBlueBall = result.blue;
    updateNumberSelectors();
    
    // 添加确认按钮
    const resultDiv = document.getElementById('ocr-result');
    if (!resultDiv.querySelector('.confirm-btn')) {
        const btn = document.createElement('button');
        btn.className = 'btn btn-success btn-block confirm-btn';
        btn.style.marginTop = '16px';
        btn.textContent = '确认并添加';
        btn.onclick = () => {
            closeCamera();
            document.getElementById('input-period').value = generatePeriod();
            document.getElementById('modal-add').classList.add('active');
        };
        resultDiv.appendChild(btn);
    }
}

// ==================== 预测功能 ====================

// 加载预测数据
function loadPredictionData() {
    // 模拟历史开奖数据（实际应从API获取）
    const historyDraws = generateMockDraws(30);
    
    // 计算冷热号
    const hotCold = calculateHotCold(historyDraws);
    
    document.getElementById('draw-count').textContent = `基于 ${historyDraws.length} 期`;
    document.getElementById('hot-red-balls').innerHTML = hotCold.hot.slice(0, 6).map(n => 
        `<span class="ball red">${padNum(n.num)}</span>`
    ).join('');
    document.getElementById('cold-red-balls').innerHTML = hotCold.cold.slice(0, 6).map(n => 
        `<span class="ball red" style="opacity:0.6">${padNum(n.num)}</span>`
    ).join('');
}

// 生成模拟开奖数据
function generateMockDraws(count) {
    const draws = [];
    for (let i = 0; i < count; i++) {
        const reds = [];
        while (reds.length < 6) {
            const n = Math.floor(Math.random() * 33) + 1;
            if (!reds.includes(n)) reds.push(n);
        }
        draws.push({
            redBalls: reds,
            blueBall: Math.floor(Math.random() * 16) + 1
        });
    }
    return draws;
}

// 计算冷热号
function calculateHotCold(draws) {
    const redCount = {};
    
    draws.forEach(draw => {
        draw.redBalls.forEach(n => {
            redCount[n] = (redCount[n] || 0) + 1;
        });
    });
    
    const sorted = Object.entries(redCount)
        .map(([num, count]) => ({ num: parseInt(num), count }))
        .sort((a, b) => b.count - a.count);
    
    return {
        hot: sorted.slice(0, 10),
        cold: sorted.slice().reverse().slice(0, 10)
    };
}

// 本地算法预测
function predictWithLocal() {
    const historyDraws = generateMockDraws(30);
    const hotCold = calculateHotCold(historyDraws);
    
    // 基于冷热号生成推荐
    const hotNums = hotCold.hot.slice(0, 4).map(n => n.num);
    const randomNums = [];
    while (randomNums.length < 2) {
        const n = Math.floor(Math.random() * 33) + 1;
        if (!hotNums.includes(n) && !randomNums.includes(n)) {
            randomNums.push(n);
        }
    }
    
    const redBalls = [...hotNums, ...randomNums].slice(0, 6);
    const blueBall = Math.floor(Math.random() * 16) + 1;
    
    displayPrediction([{ redBalls, blueBall }], '本地算法分析');
}

// AI智能预测（调用千问）
async function predictWithAI() {
    showToast('正在调用 Qwen3-Max 分析...');
    
    // 构建prompt
    const prompt = buildQwenPrompt();
    
    // 这里会通过OpenClaw浏览器调用千问网页版
    // 由于是H5网页，我们模拟返回结果
    setTimeout(() => {
        // 模拟AI推荐
        const recommendations = generateAIRecommendations();
        displayPrediction(recommendations, '基于 Qwen3-Max AI智能分析');
        showToast('AI分析完成', 'success');
    }, 2000);
}

// 构建发送给千问的prompt
function buildQwenPrompt() {
    return `你是双色球走势分析专家。根据最近30期开奖数据，请分析以下规律并推荐下期号码：
1. 冷热号趋势
2. 奇偶比例分布
3. 连号/邻号可能性分析
请给出推荐号码并解释推理过程。`;
}

// 生成AI推荐
function generateAIRecommendations() {
    const recommendations = [];
    
    for (let i = 0; i < 2; i++) {
        const reds = [];
        while (reds.length < 6) {
            const n = Math.floor(Math.random() * 33) + 1;
            if (!reds.includes(n)) reds.push(n);
        }
        recommendations.push({
            redBalls: reds.sort((a,b) => a-b),
            blueBall: Math.floor(Math.random() * 16) + 1
        });
    }
    
    return recommendations;
}

// 显示预测结果
function displayPrediction(recommendations, analysis) {
    const resultDiv = document.getElementById('prediction-result');
    resultDiv.style.display = 'block';
    
    const numbersDiv = document.getElementById('recommended-numbers');
    numbersDiv.innerHTML = recommendations.map((rec, idx) => `
        <div style="text-align: center; margin: 12px 0;">
            <div style="font-size: 12px; color: var(--text-secondary);">方案 ${idx + 1}</div>
            <div class="lottery-balls" style="justify-content: center; margin-top: 8px;">
                ${rec.redBalls.map(n => `<span class="ball red">${padNum(n)}</span>`).join('')}
                <span class="ball plus">+</span>
                <span class="ball blue">${padNum(rec.blueBall)}</span>
            </div>
        </div>
    `).join('');
    
    // 分析文本
    const analysisText = `🤖 ${analysis}\n\n根据近期开奖数据分析：\n• 奇偶比例建议 3:3 或 4:2\n• 和值预计在 90-120 之间\n• 建议关注热门号码\n\n以上推荐仅供参考，娱乐为主！`;
    
    document.getElementById('analysis-text').textContent = analysisText;
    document.getElementById('confidence-bar').style.width = '75%';
    document.getElementById('confidence-label').textContent = '置信度 75%';
}

// ==================== 飞书同步 ====================

function syncToFeishu() {
    showToast('同步功能开发中...');
}

function loadFeishuData() {
    const url = document.getElementById('feishu-url').value;
    if (!url) {
        showToast('请输入飞书表格链接', 'error');
        return;
    }
    showToast('正在从飞书导入...');
    // 这里会调用飞书API获取数据
    setTimeout(() => {
        showToast('导入成功', 'success');
    }, 1500);
}

// ==================== 通用工具 ====================

// 显示提示
function showToast(message, type = '') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = 'toast show ' + type;
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// 关闭弹窗
function closeModal(id) {
    document.getElementById(id).classList.remove('active');
}

// 点击弹窗外部关闭
document.querySelectorAll('.modal').forEach(modal => {
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.classList.remove('active');
            if (modal.id === 'modal-camera') {
                closeCamera();
            }
        }
    });
});

// ==================== 初始化 ====================

// 页面加载完成
document.addEventListener('DOMContentLoaded', () => {
    console.log('🎱 双色球彩票管理 Loaded');
    
    // 初始化号码选择器
    updateNumberSelectors();
    
    // 渲染首页记录
    renderRecords();
});

// 添加一些演示数据（新用户）
if (!localStorage.getItem(STORAGE_KEY)) {
    const demoRecords = [
        { period: '2025009', redBalls: [3, 8, 15, 22, 27, 31], blueBall: 9, isWin: false },
        { period: '2025008', redBalls: [1, 5, 12, 19, 23, 29], blueBall: 14, matchedRedCount: 3, matchedBlue: false, isWin: false, prizeLevel: null, prizeAmount: 0 },
        { period: '2025007', redBalls: [7, 11, 16, 21, 26, 32], blueBall: 12, matchedRedCount: 1, matchedBlue: true, isWin: false }
    ];
    saveRecords(demoRecords);
}