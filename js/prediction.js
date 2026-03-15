/**
 * 预测算法模块
 * 提供多种本地预测策略
 */

const Prediction = {
    /**
     * 生成推荐号码
     * @param {Array} historyDraws - 历史开奖数据
     * @param {String} strategy - 策略名称 (hot-cold, parity, neighbors, remainder, random)
     * @returns {Object} 预测结果
     */
    generate(historyDraws, strategy = 'hot-cold') {
        const strategies = {
            'hot-cold': this.hotColdStrategy,
            'parity': this.parityStrategy,
            'neighbors': this.neighborsStrategy,
            'remainder': this.remainderStrategy,
            'random': this.randomStrategy
        };
        
        const fn = strategies[strategy] || this.randomStrategy;
        return fn.call(this, historyDraws);
    },
    
    /**
     * 热门+冷门策略
     */
    hotColdStrategy(historyDraws) {
        const stats = this._calculateStats(historyDraws);
        const hotReds = stats.hotRed.slice(0, 15).map(n => n.num);
        const coldBlues = stats.coldBlue.slice(0, 3).map(n => n.num);
        
        const selectedReds = this._selectUnique(hotReds, 6);
        const blueBall = coldBlues.length > 0 ? coldBlues[0] : this._randomInt(1, 16);
        
        return {
            redBalls: selectedReds.sort((a, b) => a - b),
            blueBall,
            reason: '热门红球 + 冷门蓝球'
        };
    },
    
    /**
     * 奇偶平衡策略
     */
    parityStrategy(historyDraws) {
        const recent = historyDraws.slice(0, 10);
        let oddCount = 0;
        recent.forEach(draw => {
            oddCount += draw.redBalls.filter(n => n % 2 === 1).length;
        });
        const avgOdd = oddCount / (recent.length * 6);
        const targetOdd = avgOdd > 0.5 ? 3 : 4;
        
        const reds = [];
        while (reds.length < 6) {
            const n = this._randomInt(1, 33);
            if (!reds.includes(n)) {
                if (n % 2 === 1 && reds.filter(x => x % 2 === 1).length < targetOdd) {
                    reds.push(n);
                } else if (n % 2 === 0 && reds.filter(x => x % 2 === 0).length < 6 - targetOdd) {
                    reds.push(n);
                }
            }
        }
        
        return {
            redBalls: reds.sort((a, b) => a - b),
            blueBall: this._randomInt(1, 16),
            reason: '奇偶平衡（目标奇数: ' + targetOdd + '）'
        };
    },
    
    /**
     * 邻号分析策略
     */
    neighborsStrategy(historyDraws) {
        if (historyDraws.length < 3) {
            return this.randomStrategy(historyDraws);
        }
        
        const recent = historyDraws.slice(0, 3).flatMap(d => d.redBalls);
        const neighbors = recent.flatMap(n => [n - 1, n + 1])
            .filter(n => n >= 1 && n <= 33)
            .sort((a, b) => a - b);
        
        const reds = this._selectUnique(neighbors, 6);
        return {
            redBalls: reds,
            blueBall: this._randomInt(1, 16),
            reason: '邻号追踪（基于最近3期）'
        };
    },
    
    /**
     * 余数分布策略
     */
    remainderStrategy(historyDraws) {
        const reds = new Set();
        for (let mod = 0; mod < 6; mod++) {
            const candidates = Array.from({length: 33}, (_, i) => i + 1)
                .filter(n => n % 6 === mod);
            if (candidates.length > 0) {
                const pick = candidates[Math.floor(Math.random() * candidates.length)];
                reds.add(pick);
            }
        }
        
        return {
            redBalls: this._selectUnique(Array.from(reds), 6).sort((a, b) => a - b),
            blueBall: this._randomInt(1, 16),
            reason: '余数均匀分布（模6）'
        };
    },
    
    /**
     * 随机策略
     */
    randomStrategy(historyDraws) {
        const reds = [];
        while (reds.length < 6) {
            const n = this._randomInt(1, 33);
            if (!reds.includes(n)) reds.push(n);
        }
        return {
            redBalls: reds.sort((a, b) => a - b),
            blueBall: this._randomInt(1, 16),
            reason: '纯随机组合'
        };
    },
    
    /**
     * 综合预测（调用多个策略）
     */
   综合分析(historyDraws, count = 5) {
        const strategies = ['hot-cold', 'parity', 'neighbors', 'remainder', 'random'];
        const results = [];
        
        for (let i = 0; i < Math.min(count, strategies.length); i++) {
            results.push(this.generate(historyDraws, strategies[i]));
        }
        
        return {
            recommendations: results,
            analysis: this._generateAnalysis(historyDraws)
        };
    },
    
    /**
     * 计算冷热号统计
     */
    calculateStats(historyDraws) {
        return this._calculateStats(historyDraws);
    },
    
    // ========== 私有方法 ==========
    
    _calculateStats(historyDraws) {
        const redCount = {};
        const blueCount = {};
        
        historyDraws.forEach((draw, index) => {
            const weight = historyDraws.length - index; // 近期权重更高
            draw.redBalls.forEach(n => {
                redCount[n] = (redCount[n] || 0) + weight;
            });
            blueCount[draw.blueBall] = (blueCount[draw.blueBall] || 0) + weight;
        });
        
        const toArray = (map) => Object.entries(map)
            .map(([num, count]) => ({ num: parseInt(num), count }))
            .sort((a, b) => b.count - a.count);
        
        return {
            hotRed: toArray(redCount).slice(0, 10),
            coldRed: toArray(redCount).slice().reverse().slice(0, 10),
            hotBlue: toArray(blueCount).slice(0, 5),
            coldBlue: toArray(blueCount).slice().reverse().slice(0, 5)
        };
    },
    
    _selectUnique(candidates, needed) {
        const result = [];
        const available = [...candidates].sort(() => Math.random() - 0.5);
        
        for (let n of available) {
            if (!result.includes(n)) {
                result.push(n);
                if (result.length >= needed) break;
            }
        }
        
        // 如果不够，补充随机数
        while (result.length < needed) {
            const n = this._randomInt(1, 33);
            if (!result.includes(n)) result.push(n);
        }
        
        return result;
    },
    
    _randomInt(min, max) {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    },
    
    _generateAnalysis(historyDraws) {
        if (historyDraws.length === 0) return '暂无历史数据';
        
        const stats = this._calculateStats(historyDraws);
        const hotRed = stats.hotRed.slice(0, 5).map(n => `${n.num}(${n.count})`).join(', ');
        const coldRed = stats.coldRed.slice(0, 5).map(n => `${n.num}(${n.count})`).join(', ');
        
        return `
📊 数据分析（基于最近 ${historyDraws.length} 期）

🔥 热门红球：${hotRed}
❄️ 冷门红球：${coldRed}

预测策略说明：
1️⃣ 热门+冷门：选择高频红球，搭配低频蓝球
2️⃣ 奇偶平衡：参考历史分布，避免极端
3️⃣ 邻号追踪：基于近期号码的相邻号
4️⃣ 余数分布：确保模6均匀覆盖
5️⃣ 随机组合：作为备选方案

⚠️ 温馨提示：彩票预测仅供参考娱乐
        `.trim();
    }
};

if (typeof module !== 'undefined' && module.exports) {
    module.exports = Prediction;
}
