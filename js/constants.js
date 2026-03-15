// 常量定义
const CONSTANTS = {
    // 球号范围
    RED_BALL_MIN: 1,
    RED_BALL_MAX: 33,
    RED_BALL_COUNT: 6,
    BLUE_BALL_MIN: 1,
    BLUE_BALL_MAX: 16,
    BLUE_BALL_COUNT: 1,
    
    // 存储键
    STORAGE_KEYS: {
        RECORDS: 'lottery_records',
        DRAW_HISTORY: 'draw_history',
        SETTINGS: 'lottery_settings',
        VERSION: 'lottery_version'
    },
    
    // 版本
    APP_VERSION: '1.0.0',
    
    // 统计默认值
    DEFAULTS: {
        MAX_RECORDS: 1000,
        MOCK_DRAW_COUNT: 30
    },
    
    // 消息提示
    MESSAGES: {
        REQUIRED_PERIOD: '请输入期号',
        REQUIRED_RED: '请选择6个红球',
        REQUIRED_BLUE: '请选择1个蓝球',
        SUCCESS_ADD: '添加成功',
        SUCCESS_UPDATE: '更新成功',
        SUCCESS_DELETE: '删除成功',
        ERROR_CAMERA: '无法访问相机',
        ERROR_STORAGE: '存储空间不足',
        PREDICTION_LOADING: '正在分析...'
    }
};

// 导出供模块使用
if (typeof module !== 'undefined' && module.exports) {
    module.exports = CONSTANTS;
}
