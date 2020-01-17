declare module '@capacitor/core' {
  interface PluginRegistry {
    VoiceRecorder: VoiceRecorderPlugin
  }
}

export interface RecordingData {

}

export interface GenericResponse {
  value: boolean
}

export interface VoiceRecorderPlugin {
  havePermission (): Promise<GenericResponse>

  askForPermission (): Promise<GenericResponse>

  startRecording (): Promise<GenericResponse>

  stopRecording (): Promise<RecordingData>
}
