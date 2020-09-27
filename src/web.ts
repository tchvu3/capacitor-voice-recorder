import { registerWebPlugin, WebPlugin } from '@capacitor/core'
import { GenericResponse, RecordingData, VoiceRecorderPlugin } from './definitions'

export class VoiceRecorderWeb extends WebPlugin implements VoiceRecorderPlugin {
  constructor () {
    super({
      name: 'VoiceRecorder',
      platforms: ['web'],
    })
  }

  canDeviceVoiceRecord (): Promise<GenericResponse> {
    return Promise.reject(new Error('VoiceRecorder does not have web implementation'))
  }

  hasAudioRecordingPermission (): Promise<GenericResponse> {
    return Promise.reject(new Error('VoiceRecorder does not have web implementation'))
  }

  requestAudioRecordingPermission (): Promise<GenericResponse> {
    return Promise.reject(new Error('VoiceRecorder does not have web implementation'))
  }

  startRecording (): Promise<GenericResponse> {
    return Promise.reject(new Error('VoiceRecorder does not have web implementation'))
  }

  stopRecording (): Promise<RecordingData> {
    return Promise.reject(new Error('VoiceRecorder does not have web implementation'))
  }

}

const VoiceRecorder = new VoiceRecorderWeb()

export { VoiceRecorder }

registerWebPlugin(VoiceRecorder)
