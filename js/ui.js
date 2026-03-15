/**
 * UI 交互模块
 * 处理页面切换、弹窗、通知等
 */

const UI = {
    // 页面导航
    switchPage(pageName) {
        document.querySelectorAll('.tab-item').forEach(tab => {
            tab.classList.toggle('active', tab.dataset.page === pageName);
        });
        document.querySelectorAll('.page').forEach(page => {
            page.classList.toggle('active', page.id === `page-${pageName}`);
        });
    },
    
    // 显示提示消息
    toast(message, type = '', duration = 3000) {
        const el = document.getElementById('toast');
        el.textContent = message;
        el.className = 'toast show ' + type;
        
        setTimeout(() => {
            el.classList.remove('show');
        }, duration);
    },
    
    success(message) {
        this.toast(message, 'success');
    },
    
    error(message) {
        this.toast(message, 'error');
    },
    
    // 弹窗控制
    openModal(modalId) {
        document.getElementById(modalId).classList.add('active');
    },
    
    closeModal(modalId) {
        document.getElementById(modalId).classList.remove('active');
    },
    
    // 生成号码HTML
    renderBalls(redBalls, blueBall, options = {}) {
        const { showPlus = true, redClass = 'red', blueClass = 'blue' } = options;
        const sortedReds = [...redBalls].sort((a, b) => a - b);
        
        let html = sortedReds.map(n => 
            `<span class="ball ${redClass}">${this.padNum(n)}</span>`
        ).join('');
        
        if (showPlus) {
            html += '<span class="ball plus">+</span>';
        }
        html += `<span class="ball ${blueClass}">${this.padNum(blueBall)}</span>`;
        
        return html;
    },
    
    // 格式化日期
    formatDate(dateStr) {
        const date = new Date(dateStr);
        return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`;
    },
    
    // 格式化数字（补零）
    padNum(n) {
        return String(n).padStart(2, '0');
    },
    
    // 渲染记录列表
    renderRecordList(records, limit = 10) {
        const list = document.getElementById('record-list');
        const empty = document.getElementById('empty-state');
        
        if (records.length === 0) {
            list.style.display = 'none';
            empty.style.display = 'block';
            return;
        }
        
        list.style.display = 'flex';
        empty.style.display = 'none';
        
        list.innerHTML = records.slice(0, limit).map(record => {
            const hasResult = record.drawRedBalls && record.drawBlueBall;
            
            // 中奖标签
            let matchTags = '';
            if (hasResult) {
                const tags = [];
                if (record.matchedRedCount > 0) {
                    tags.push(`<span class="match-tag ${record.matchedRedCount >= 3 ? 'win' : ''}">红球 ${record.matchedRedCount}/6</span>`);
                }
                tags.push(`<span class="match-tag ${record.matchedBlue ? 'win' : ''}">蓝球 ${record.matchedBlue ? '✓' : '✗'}</span>`);
                matchTags = `<div style="display: flex; gap: 8px; margin-top: 8px;">${tags.join('')}</div>`;
            }
            
            // 开奖号码（如果有）
            const drawResult = hasResult ? `
                <div class="record-result">
                    <div class="lottery-balls">
                        ${record.drawRedBalls.sort((a,b)=>a-b).map(n => 
                            `<span class="ball red" style="opacity:0.6">${this.padNum(n)}</span>`
                        ).join('')}
                        <span class="ball plus">+</span>
                        <span class="ball blue" style="opacity:0.6">${this.padNum(record.drawBlueBall)}</span>
                    </div>
                </div>
            ` : '';
            
            return `
                <div class="record-item" data-id="${record.id}">
                    <div class="record-header">
                        <span class="record-period">第 ${record.period} 期</span>
                        <span class="record-date">${this.formatDate(record.createdAt)}</span>
                    </div>
                    
                    ${record.redBalls && record.redBalls.length > 0 ? `
                        <div class="record-numbers">
                            <div class="lottery-balls">
                                ${this.renderBalls(record.redBalls, record.blueBall)}
                            </div>
                        </div>
                    ` : ''}
                    
                    ${drawResult}
                    ${matchTags}
                    
                    <div class="record-actions">
                        <button class="btn btn-sm btn-outline" onclick="App.editRecord(${record.id})">编辑</button>
                        <button class="btn btn-sm btn-danger" onclick="App.confirmDelete(${record.id})">删除</button>
                    </div>
                </div>
            `;
        }).join('');
        
        this.updateStats(records);
    },
    
    updateStats(records) {
        const total = records.length;
        const wins = records.filter(r => r.isWin).length;
        const amount = records.reduce((sum, r) => sum + (r.prizeAmount || 0), 0);
        
        document.getElementById('stat-total').textContent = total;
        document.getElementById('stat-wins').textContent = wins;
        document.getElementById('stat-amount').textContent = amount.toFixed(0);
    },
    
    // 生成期号（示例: 2024023）
    generatePeriod() {
        const now = new Date();
        const year = now.getFullYear();
        // 简单实现：用年月日
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        return `${year}${month}${day}`; // 简化版
    },
    
    validateNumberSelection(redBalls, blueBall) {
        if (!redBalls || redBalls.length !== 6) {
            return { valid: false, message: CONSTANTS.MESSAGES.REQUIRED_RED };
        }
        if (!blueBall) {
            return { valid: false, message: CONSTANTS.MESSAGES.REQUIRED_BLUE };
        }
        // 检查重复
        if (new Set(redBalls).size !== 6) {
            return { valid: false, message: '红球号码不能重复' };
        }
        return { valid: true };
    }
};

if (typeof module !== 'undefined' && module.exports) {
    module.exports = UI;
}
