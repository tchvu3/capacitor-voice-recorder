export type Base64String = string

export interface RecordingData {
  value: {
    recordDataBase64: Base64String
    msDuration: number
    mimeType: string
  };
}

export interface GenericResponse {
  value: boolean;
}

export interface CurrentRecordingStatus {
  status: 'RECORDING' | 'PAUSED' | 'NONE';
}

export interface VoiceRecorderPlugin {
  canDeviceVoiceRecord (): Promise<GenericResponse>;

  requestAudioRecordingPermission (): Promise<GenericResponse>;

  hasAudioRecordingPermission (): Promise<GenericResponse>;

  startRecording (): Promise<GenericResponse>;

  stopRecording (): Promise<RecordingData>;

  pauseRecording (): Promise<GenericResponse>;

  resumeRecording (): Promise<GenericResponse>;

  getCurrentStatus (): Promise<CurrentRecordingStatus>;

}
