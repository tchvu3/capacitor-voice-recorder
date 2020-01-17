import { WebPlugin } from '@capacitor/core';
export class VoiceRecorderWeb extends WebPlugin {
    constructor() {
        super({
            name: 'VoiceRecorder',
            platforms: ['web'],
        });
    }
    askForPermission() {
        return undefined;
    }
    havePermission() {
        return undefined;
    }
    startRecording() {
        return undefined;
    }
    stopRecording() {
        return undefined;
    }
}
const VoiceRecorder = new VoiceRecorderWeb();
export { VoiceRecorder };
import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(VoiceRecorder);
//# sourceMappingURL=web.js.map