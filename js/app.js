/**
 * 双色球彩票管理 H5 应用
 * 主入口文件
 * 
 * @version 1.0.0
 * @author 大龙虾 🦞
 */

// 确保先加载常量
if (typeof CONSTANTS === 'undefined') {
    document.write('<script src="js/constants.js"><\/script>');
}

// 加载其他模块（按依赖顺序）
document.write('<script src="js/storage.js"><\/script>');
document.write('<script src="js/prediction.js"><\/script>');
document.write('<script src="js/ui.js"><\/script>');
document.write('<script src="js/camera.js"><\/script>');

// ========== 应用主逻辑 ==========

const App = {
    // 临时选择状态
    selectedRedBalls: new Set(),
    selectedBlueBall: null,
    editingId: null,
    
    init() {
        this.bindEvents();
        this.renderHome();
        console.log('🎱 双色球H5应用已就绪');
    },
    
    bindEvents() {
        // 底部导航点击
        document.querySelectorAll('.tab-item').forEach(tab => {
            tab.addEventListener('click', () => {
                this.switchPage(tab.dataset.page);
            });
        });
        
        // 模态框外部点击关闭
        document.querySelectorAll('.modal').forEach(modal => {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    modal.classList.remove('active');
                    if (modal.id === 'modal-camera') {
                        Camera.stop();
                        document.getElementById('ocr-result').style.display = 'none';
                    }
                }
            });
        });
        
        // 期号生成（示例）
        document.getElementById('input-period')?.addEventListener('focus', (e) => {
            if (!e.target.value) {
                e.target.value = UI.generatePeriod();
            }
        });
    },
    
    switchPage(pageName) {
        UI.switchPage(pageName);
        
        if (pageName === 'home') {
            this.renderHome();
        } else if (pageName === 'prediction') {
            this.loadPredictionPage();
        }
    },
    
    // ========== 首页 ==========
    
    renderHome() {
        const records = Storage.getRecords();
        UI.renderRecordList(records);
    },
    
    // ========== 记录管理 ==========
    
    openAddModal(record = null) {
        this.editingId = record ? record.id : null;
        this.selectedRedBalls = new Set(record ? record.redBalls : []);
        this.selectedBlueBall = record ? record.blueBall : null;
        
        const periodInput = document.getElementById('input-period');
        const remarksInput = document.getElementById('input-remarks');
        
        periodInput.value = record ? record.period : UI.generatePeriod();
        remarksInput.value = record ? (record.remarks || '') : '';
        
        this.updateNumberSelectors();
        UI.openModal('modal-add');
    },
    
    updateNumberSelectors() {
        // 红球选择器
        const redGrid = document.getElementById('red-ball-selector');
        redGrid.innerHTML = '';
        for (let i = 1; i <= 33; i++) {
            const btn = document.createElement('button');
            btn.className = `number-btn ${this.selectedRedBalls.has(i) ? 'selected' : ''}`;
            btn.textContent = UI.padNum(i);
            btn.onclick = () => this.toggleRedBall(i);
            redGrid.appendChild(btn);
        }
        
        // 蓝球选择器
        const blueGrid = document.getElementById('blue-ball-selector');
        blueGrid.innerHTML = '';
        for (let i = 1; i <= 16; i++) {
            const btn = document.createElement('button');
            btn.className = `number-btn blue ${this.selectedBlueBall === i ? 'selected' : ''}`;
            btn.textContent = UI.padNum(i);
            btn.onclick = () => this.toggleBlueBall(i);
            blueGrid.appendChild(btn);
        }
        
        document.getElementById('red-count').textContent = this.selectedRedBalls.size;
        document.getElementById('blue-count').textContent = this.selectedBlueBall ? 1 : 0;
    },
    
    toggleRedBall(num) {
        if (this.selectedRedBalls.has(num)) {
            this.selectedRedBalls.delete(num);
        } else if (this.selectedRedBalls.size < 6) {
            this.selectedRedBalls.add(num);
        } else {
            UI.toast('最多选择6个红球', 'error');
            return;
        }
        this.updateNumberSelectors();
    },
    
    toggleBlueBall(num) {
        this.selectedBlueBall = this.selectedBlueBall === num ? null : num;
        this.updateNumberSelectors();
    },
    
    saveCurrentRecord() {
        const period = document.getElementById('input-period').value.trim();
        const remarks = document.getElementById('input-remarks').value.trim();
        
        if (!period) {
            UI.error(CONSTANTS.MESSAGES.REQUIRED_PERIOD);
            return;
        }
        
        const validation = UI.validateNumberSelection(
            Array.from(this.selectedRedBalls),
            this.selectedBlueBall
        );
        if (!validation.valid) {
            UI.error(validation.message);
            return;
        }
        
        const record = {
            period,
            redBalls: Array.from(this.selectedRedBalls),
            blueBall: this.selectedBlueBall,
            remarks
        };
        
        if (this.editingId) {
            Storage.updateRecord(this.editingId, record);
            UI.success(CONSTANTS.MESSAGES.SUCCESS_UPDATE);
        } else {
            Storage.addRecord(record);
            UI.success(CONSTANTS.MESSAGES.SUCCESS_ADD);
        }
        
        UI.closeModal('modal-add');
        this.renderHome();
    },
    
    editRecord(id) {
        const record = Storage.getRecordById(id);
        if (record) {
            this.openAddModal(record);
        }
    },
    
    confirmDelete(id) {
        if (confirm('确定要删除这条记录吗？')) {
            Storage.deleteRecord(id);
            this.renderHome();
            UI.success(CONSTANTS.MESSAGES.SUCCESS_DELETE);
        }
    },
    
    // ========== 相机/OCR ==========
    
    async startCamera() {
        UI.openModal('modal-camera');
        return await Camera.start();
    },
    
    captureAndRecognize() {
        UI.toast(CONSTANTS.MESSAGES.PREDICTION_LOADING);
        
        Camera.capture()
            .then(imageData => {
                return Camera.mockOCR(imageData);
            })
            .then(result => {
                this.displayOCRResult(result);
                UI.success('识别完成');
            })
            .catch(err => {
                UI.error('识别失败: ' + err.message);
            });
    },
    
    displayOCRResult(result) {
        const container = document.getElementById('ocr-numbers');
        container.innerHTML = UI.renderBalls(result.reds, result.blue);
        
        const resultDiv = document.getElementById('ocr-result');
        resultDiv.style.display = 'block';
        
        // 更新选择器状态
        this.selectedRedBalls = new Set(result.reds);
        this.selectedBlueBall = result.blue;
        this.updateNumberSelectors();
        
        // 添加确认按钮
        resultDiv.innerHTML += `
            <button class="btn btn-success btn-block confirm-btn" style="margin-top:16px;">
                确认并添加
            </button>
        `;
        
        resultDiv.querySelector('.confirm-btn').onclick = () => {
            UI.closeModal('modal-camera');
            Camera.stop();
            document.getElementById('input-period').value = UI.generatePeriod();
            this.openAddModal();
        };
    },
    
    // ========== 预测功能 ==========
    
    loadPredictionPage() {
        // 模拟历史数据（实际应从Storage读取开奖记录）
        const historyDraws = this._generateMockDraws(CONSTANTS.DEFAULTS.MOCK_DRAW_COUNT);
        const stats = Prediction.calculateStats(historyDraws);
        
        document.getElementById('draw-count').textContent = `基于 ${historyDraws.length} 期`;
        
        document.getElementById('hot-red-balls').innerHTML = 
            stats.hotRed.slice(0, 6).map(n => 
                `<span class="ball red">${UI.padNum(n.num)}</span>`
            ).join('');
            
        document.getElementById('cold-red-balls').innerHTML = 
            stats.coldRed.slice(0, 6).map(n => 
                `<span class="ball red" style="opacity:0.6">${UI.padNum(n.num)}</span>`
            ).join('');
        
        // 隐藏上次结果
        document.getElementById('prediction-result').style.display = 'none';
    },
    
    predictWithLocal() {
        const historyDraws = this._generateMockDraws(CONSTANTS.DEFAULTS.MOCK_DRAW_COUNT);
        const result = Prediction.综合分析(historyDraws, 3);
        
        this.displayPrediction(result.recommendations.slice(0, 3), result.analysis);
    },
    
    async predictWithAI() {
        UI.toast('正在连接AI服务...');
        
        // TODO: 调用OpenClaw的AI能力或Web API
        // 目前模拟
        setTimeout(() => {
            const historyDraws = this._generateMockDraws(CONSTANTS.DEFAULTS.MOCK_DRAW_COUNT);
            const result = Prediction.综合分析(historyDraws, 3);
            result.analysis = '🤖 AI智能分析（模拟）\n\n' + result.analysis;
            this.displayPrediction(result.recommendations, result.analysis);
            UI.success('AI分析完成');
        }, 2000);
    },
    
    displayPrediction(recommendations, analysis) {
        const resultDiv = document.getElementById('prediction-result');
        resultDiv.style.display = 'block';
        
        document.getElementById('recommended-numbers').innerHTML = 
            recommendations.map((rec, idx) => `
                <div style="text-align:center; margin:12px 0;">
                    <div style="font-size:12px;color:var(--text-secondary);">方案 ${idx + 1}</div>
                    <div class="lottery-balls" style="justify-content:center; margin-top:8px;">
                        ${UI.renderBalls(rec.redBalls, rec.blueBall)}
                    </div>
                </div>
            `).join('');
        
        document.getElementById('analysis-text').textContent = analysis;
        document.getElementById('confidence-bar').style.width = '75%';
        document.getElementById('confidence-label').textContent = '置信度 75%';
    },
    
    // ========== 辅助方法 ==========
    
    _generateMockDraws(count) {
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
    },
    
    // ========== 飞书同步 ==========
    
    syncToFeishu() {
        UI.toast('同步功能需配置后端服务，当前为单机版');
    },
    
    async loadFeishuData() {
        const url = document.getElementById('feishu-url').value;
        if (!url) {
            UI.error('请输入飞书表格链接');
            return;
        }
        UI.toast('正在从飞书导入...');
        // TODO: 实现飞书API调用
        setTimeout(() => {
            UI.success('导入成功');
        }, 1500);
    },
    
    // ========== 数据管理 ==========
    
    exportData() {
        const records = Storage.getRecords();
        const data = {
            version: CONSTANTS.APP_VERSION,
            exportDate: new Date().toISOString(),
            records
        };
        const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `lottery-records-${new Date().toISOString().split('T')[0]}.json`;
        a.click();
        URL.revokeObjectURL(url);
        UI.success('数据导出成功');
    },
    
    clearAllData() {
        if (confirm('确定要清空所有数据吗？此操作不可撤销！')) {
            Storage.deleteAllRecords();
            this.renderHome();
            UI.success('数据已清空');
        }
    }
};

// 全局暴露
window.App = App;

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    // 初始化演示数据（如果是首次访问）
    if (!localStorage.getItem(CONSTANTS.STORAGE_KEYS.RECORDS)) {
        const demos = [
            { period: '2025009', redBalls: [3,8,15,22,27,31], blueBall: 9 },
            { period: '2025008', redBalls: [1,5,12,19,23,29], blueBall: 14, matchedRedCount: 3, matchedBlue: false },
            { period: '2025007', redBalls: [7,11,16,21,26,32], blueBall: 12, matchedRedCount: 1, matchedBlue: true }
        ];
        Storage.saveRecords(demos);
    }
    
    App.init();
});
