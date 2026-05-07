import { WebPlugin } from '@capacitor/core';

import { VoiceRecorderImpl } from './VoiceRecorderImpl';
import type {
  ChunkedRecordingOptions,
  CurrentRecordingStatus,
  GenericResponse,
  RecordingData,
  RecordingOptions,
  VoiceRecorderPlugin,
} from './definitions';

export class VoiceRecorderWeb extends WebPlugin implements VoiceRecorderPlugin {
  private voiceRecorderInstance = new VoiceRecorderImpl();

  constructor() {
    super();
    // Forward chunk events emitted by the impl to Capacitor listeners.
    this.voiceRecorderInstance.setChunkEmitter((chunk) => {
      this.notifyListeners('audioChunk', chunk);
    });
  }

  public canDeviceVoiceRecord(): Promise<GenericResponse> {
    return VoiceRecorderImpl.canDeviceVoiceRecord();
  }

  public hasAudioRecordingPermission(): Promise<GenericResponse> {
    return VoiceRecorderImpl.hasAudioRecordingPermission();
  }

  public requestAudioRecordingPermission(): Promise<GenericResponse> {
    return VoiceRecorderImpl.requestAudioRecordingPermission();
  }

  public startRecording(options?: RecordingOptions): Promise<GenericResponse> {
    return this.voiceRecorderInstance.startRecording(options);
  }

  public stopRecording(): Promise<RecordingData> {
    return this.voiceRecorderInstance.stopRecording();
  }

  public pauseRecording(): Promise<GenericResponse> {
    return this.voiceRecorderInstance.pauseRecording();
  }

  public resumeRecording(): Promise<GenericResponse> {
    return this.voiceRecorderInstance.resumeRecording();
  }

  public getCurrentStatus(): Promise<CurrentRecordingStatus> {
    return this.voiceRecorderInstance.getCurrentStatus();
  }

  public startChunkedRecording(options?: ChunkedRecordingOptions): Promise<GenericResponse> {
    return this.voiceRecorderInstance.startChunkedRecording(options);
  }

  public stopChunkedRecording(): Promise<GenericResponse> {
    return this.voiceRecorderInstance.stopChunkedRecording();
  }

  public pauseChunkedRecording(): Promise<GenericResponse> {
    return this.voiceRecorderInstance.pauseChunkedRecording();
  }

  public resumeChunkedRecording(): Promise<GenericResponse> {
    return this.voiceRecorderInstance.resumeChunkedRecording();
  }
}
