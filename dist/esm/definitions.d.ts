declare module '@capacitor/core' {
    interface PluginRegistry {
        VoiceRecorder: VoiceRecorderPlugin;
    }
}
export declare type Base64String = string;
export interface RecordingData {
    value: {
        recordDataBase64: Base64String;
        msDuration: number;
    };
}
export interface GenericResponse {
    value: boolean;
}
export interface VoiceRecorderPlugin {
    canDeviceVoiceRecord(): Promise<GenericResponse>;
    requestAudioRecordingPermission(): Promise<GenericResponse>;
    hasAudioRecordingPermission(): Promise<GenericResponse>;
    startRecording(): Promise<GenericResponse>;
    stopRecording(): Promise<RecordingData>;
}
