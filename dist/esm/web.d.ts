import { WebPlugin } from '@capacitor/core';
import { GenericResponse, RecordingData, VoiceRecorderPlugin } from './definitions';
export declare class VoiceRecorderWeb extends WebPlugin implements VoiceRecorderPlugin {
    constructor();
    canDeviceVoiceRecord(): Promise<GenericResponse>;
    hasAudioRecordingPermission(): Promise<GenericResponse>;
    requestAudioRecordingPermission(): Promise<GenericResponse>;
    startRecording(): Promise<GenericResponse>;
    stopRecording(): Promise<RecordingData>;
}
declare const VoiceRecorder: VoiceRecorderWeb;
export { VoiceRecorder };
