import { WebPlugin } from '@capacitor/core';
import { GenericResponse, RecordingData, VoiceRecorderPlugin } from './definitions';
export declare class VoiceRecorderWeb extends WebPlugin implements VoiceRecorderPlugin {
    constructor();
    askForPermission(): Promise<GenericResponse>;
    havePermission(): Promise<GenericResponse>;
    startRecording(): Promise<GenericResponse>;
    stopRecording(): Promise<RecordingData>;
}
declare const VoiceRecorder: VoiceRecorderWeb;
export { VoiceRecorder };
