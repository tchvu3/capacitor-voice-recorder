import { WebPlugin } from '@capacitor/core'
import { GenericResponse, RecordingData, VoiceRecorderPlugin } from './definitions'

export class VoiceRecorderWeb extends WebPlugin implements VoiceRecorderPlugin {
  constructor () {
    super({
      name: 'VoiceRecorder',
      platforms: ['web'],
    })
  }

  askForPermission (): Promise<GenericResponse> {
    return undefined
  }

  havePermission (): Promise<GenericResponse> {
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
