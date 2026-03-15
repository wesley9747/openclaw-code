/**
 * 数据存储模块
 * 负责 LocalStorage 的读写操作
 */

const Storage = {
    // 版本迁移
    init() {
        const currentVersion = localStorage.getItem(CONSTANTS.STORAGE_KEYS.VERSION);
        if (!currentVersion || currentVersion !== CONSTANTS.APP_VERSION) {
            this.migrateData(currentVersion);
            localStorage.setItem(CONSTANTS.STORAGE_KEYS.VERSION, CONSTANTS.APP_VERSION);
        }
    },
    
    migrateData(oldVersion) {
        console.log(`数据迁移: ${oldVersion} -> ${CONSTANTS.APP_VERSION}`);
        // 未来版本升级时在这里添加迁移逻辑
    },
    
    // 读取记录
    getRecords() {
        try {
            const data = localStorage.getItem(CONSTANTS.STORAGE_KEYS.RECORDS);
            return data ? JSON.parse(data) : [];
        } catch (e) {
            console.error('读取记录失败:', e);
            return [];
        }
    },
    
    // 保存记录（带容量检查）
    saveRecords(records) {
        try {
            const size = this.getStorageSize();
            const maxSize = 4.5 * 1024 * 1024; // 4.5MB 安全阈值
            if (size > maxSize) {
                console.warn('LocalStorage 接近容量限制，自动清理旧数据');
                this.cleanupOldRecords(records);
            }
            localStorage.setItem(CONSTANTS.STORAGE_KEYS.RECORDS, JSON.stringify(records));
        } catch (e) {
            console.error('保存记录失败:', e);
            throw new Error(CONSTANTS.MESSAGES.ERROR_STORAGE);
        }
    },
    
    getStorageSize() {
        let total = 0;
        for (let key in localStorage) {
            if (localStorage.hasOwnProperty(key)) {
                total += localStorage[key].length * 2; // UTF-16 编码，每个字符2字节
            }
        }
        return total;
    },
    
    cleanupOldRecords(records) {
        const maxRecords = CONSTANTS.DEFAULTS.MAX_RECORDS;
        if (records.length > maxRecords) {
            records = records.slice(0, maxRecords);
        }
    },
    
    // CRUD 操作
    addRecord(record) {
        const records = this.getRecords();
        record.id = Date.now();
        record.createdAt = new Date().toISOString();
        record.syncStatus = 'pending';
        records.unshift(record);
        this.saveRecords(records);
        return record;
    },
    
    updateRecord(id, updates) {
        const records = this.getRecords();
        const index = records.findIndex(r => r.id === id);
        if (index !== -1) {
            records[index] = { ...records[index], ...updates };
            this.saveRecords(records);
            return true;
        }
        return false;
    },
    
    deleteRecord(id) {
        const records = this.getRecords();
        const filtered = records.filter(r => r.id !== id);
        this.saveRecords(filtered);
        return true;
    },
    
    deleteAllRecords() {
        if (confirm('确定要删除所有记录吗？此操作不可撤销。')) {
            localStorage.removeItem(CONSTANTS.STORAGE_KEYS.RECORDS);
            return true;
        }
        return false;
    },
    
    getRecordById(id) {
        const records = this.getRecords();
        return records.find(r => r.id === id);
    },
    
    // 统计数据
    getStats() {
        const records = this.getRecords();
        const total = records.length;
        const wins = records.filter(r => r.isWin).length;
        const amount = records.reduce((sum, r) => sum + (r.prizeAmount || 0), 0);
        
        return { total, wins, amount };
    }
};

// 自动初始化
Storage.init();

if (typeof module !== 'undefined' && module.exports) {
    module.exports = Storage;
}
