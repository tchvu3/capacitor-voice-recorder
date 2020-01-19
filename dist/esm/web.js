import { WebPlugin } from '@capacitor/core';
export class VoiceRecorderWeb extends WebPlugin {
    constructor() {
        super({
            name: 'VoiceRecorder',
            platforms: ['web'],
        });
    }
    canDeviceVoiceRecord() {
        return Promise.reject(new Error('VoiceRecorder does not have web implementation'));
    }
    hasAudioRecordingPermission() {
        return Promise.reject(new Error('VoiceRecorder does not have web implementation'));
    }
    requestAudioRecordingPermission() {
        return Promise.reject(new Error('VoiceRecorder does not have web implementation'));
    }
    startRecording() {
        return Promise.reject(new Error('VoiceRecorder does not have web implementation'));
    }
    stopRecording() {
        return Promise.reject(new Error('VoiceRecorder does not have web implementation'));
    }
}
const VoiceRecorder = new VoiceRecorderWeb();
export { VoiceRecorder };
import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(VoiceRecorder);
//# sourceMappingURL=web.js.map