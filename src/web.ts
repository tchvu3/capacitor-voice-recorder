import { WebPlugin } from '@capacitor/core'
import { GenericResponse, RecordingData, VoiceRecorderPlugin } from './definitions'

export class VoiceRecorderWeb extends WebPlugin implements VoiceRecorderPlugin {
  constructor () {
    super({
      name: 'VoiceRecorder',
      platforms: ['web'],
    })
  }

  canDeviceVoiceRecord (): Promise<GenericResponse> {
    return undefined
  }

  hasAudioRecordingPermission (): Promise<GenericResponse> {
    return undefined
  }

  requestAudioRecordingPermission (): Promise<GenericResponse> {
    return undefined
  }

  startRecording (): Promise<GenericResponse> {
    return undefined
  }

  stopRecording (): Promise<RecordingData> {
    return undefined
  }

}

const VoiceRecorder = new VoiceRecorderWeb()

export { VoiceRecorder }

import { registerWebPlugin } from '@capacitor/core'

registerWebPlugin(VoiceRecorder)
