/**
 * 相机/OCR 模块
 * 处理拍照和号码识别（目前是模拟实现）
 */

const Camera = {
    stream: null,
    
    async start() {
        try {
            this.stream = await navigator.mediaDevices.getUserMedia({
                video: { facingMode: 'environment' },
                audio: false
            });
            const video = document.getElementById('camera-preview');
            video.srcObject = this.stream;
            return true;
        } catch (err) {
            console.error('相机访问失败:', err);
            UI.error(CONSTANTS.MESSAGES.ERROR_CAMERA + ': ' + err.message);
            return false;
        }
    },
    
    stop() {
        if (this.stream) {
            this.stream.getTracks().forEach(track => track.stop());
            this.stream = null;
        }
    },
    
    capture() {
        const video = document.getElementById('camera-preview');
        const canvas = document.getElementById('camera-canvas');
        
        if (!video.videoWidth) {
            throw new Error('相机未就绪');
        }
        
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        
        const ctx = canvas.getContext('2d');
        ctx.drawImage(video, 0, 0);
        
        return canvas.toDataURL('image/jpeg', 0.8);
    },
    
    /**
     * 模拟OCR识别
     * 真实场景需要调用后端API或使用Tesseract.js
     */
    async mockOCR(imageData) {
        // 模拟网络延迟
        await new Promise(resolve => setTimeout(resolve, 1500));
        
        // 返回随机生成的号码（模拟识别结果）
        return this._generateRandomNumbers();
    },
    
    /**
     * 真实OCR实现（需要加载Tesseract.js）
     */
    async realOCR(imageData) {
        // TODO: 集成 Tesseract.js 或其他OCR库
        // const result = await Tesseract.recognize(imageData, 'chi_sim+eng');
        // return this._parseOCRResult(result);
        return this._generateRandomNumbers();
    },
    
    _generateRandomNumbers() {
        const reds = [];
        while (reds.length < 6) {
            const n = Math.floor(Math.random() * 33) + 1;
            if (!reds.includes(n)) reds.push(n);
        }
        const blue = Math.floor(Math.random() * 16) + 1;
        return { reds, blue };
    },
    
    _parseOCRResult(ocrResult) {
        // 解析OCR返回的文本，提取号码
        // 这是一个示例，实际需要根据彩票样式定制
        const text = ocrResult.data.text;
        const numbers = text.match(/\d{1,2}/g) || [];
        const reds = numbers.slice(0, 6).map(Number).filter(n => n >= 1 && n <= 33);
        const blue = numbers[6] ? parseInt(numbers[6]) : null;
        
        if (reds.length === 6 && blue && blue >= 1 && blue <= 16) {
            return { reds, blue };
        }
        
        // 解析失败，返回随机
        return this._generateRandomNumbers();
    }
};

if (typeof module !== 'undefined' && module.exports) {
    module.exports = Camera;
}
