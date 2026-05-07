import type { Directory } from '@capacitor/filesystem';
import type { PluginListenerHandle } from '@capacitor/core';

export type Base64String = string;

export interface RecordingData {
  value: {
    recordDataBase64?: Base64String;
    msDuration: number;
    mimeType: string;
    path?: string;
  };
}

/**
 * Payload emitted for each audio chunk during chunked recording.
 * Each chunk is a self-contained audio file that can be decoded and
 * transcribed independently (e.g. uploaded to a transcription service).
 *
 * Shape follows the Capacitor listener convention: `notifyListeners(name, data)`
 * delivers `data` directly to JS callbacks (no `{ value: ... }` envelope).
 */
export interface AudioChunk {
  recordDataBase64: Base64String;
  msDuration: number;
  mimeType: string;
  /** 0-based sequential index of this chunk within the recording session. */
  chunkIndex: number;
  /** True only for the chunk emitted when stopChunkedRecording() is called. */
  isFinalChunk: boolean;
}

export type RecordingOptions =
  | never
  | {
      directory: Directory;
      subDirectory?: string;
    };

/**
 * Options accepted by startChunkedRecording.
 * All fields are optional; omitting the options starts an in-memory base64 chunked recording
 * with the default chunk interval.
 */
export interface ChunkedRecordingOptions {
  directory?: Directory;
  subDirectory?: string;
  /**
   * Interval (in milliseconds) at which the plugin emits an `audioChunk` event.
   * Defaults to 30000 (30 seconds). Minimum recommended: 1000 ms.
   * Shorter intervals mean smaller, more numerous chunks; each chunk has a small audio gap
   * at its boundary (~50ms native) because a new recorder is opened per chunk.
   */
  chunkIntervalMs?: number;
}

export interface GenericResponse {
  value: boolean;
}

export const RecordingStatus = {
  RECORDING: 'RECORDING',
  PAUSED: 'PAUSED',
  INTERRUPTED: 'INTERRUPTED',
  NONE: 'NONE',
} as const;

export interface CurrentRecordingStatus {
  status: (typeof RecordingStatus)[keyof typeof RecordingStatus];
}

/**
 * Event payload for voiceRecordingInterrupted event (empty - no data)
 */
export interface VoiceRecordingInterruptedEvent {}

/**
 * Event payload for voiceRecordingInterruptionEnded event (empty - no data)
 */
export interface VoiceRecordingInterruptionEndedEvent {}

export interface VoiceRecorderPlugin {
  canDeviceVoiceRecord(): Promise<GenericResponse>;

  requestAudioRecordingPermission(): Promise<GenericResponse>;

  hasAudioRecordingPermission(): Promise<GenericResponse>;

  startRecording(options?: RecordingOptions): Promise<GenericResponse>;

  stopRecording(): Promise<RecordingData>;

  pauseRecording(): Promise<GenericResponse>;

  resumeRecording(): Promise<GenericResponse>;

  getCurrentStatus(): Promise<CurrentRecordingStatus>;

  /**
   * Start a chunked recording. The plugin records continuously and emits an `audioChunk`
   * event every `chunkIntervalMs` milliseconds with a standalone, decodable audio file
   * containing the audio captured since the previous chunk. A final chunk (with
   * `isFinalChunk: true`) is emitted when `stopChunkedRecording()` is called.
   *
   * Use this for long-form recordings (e.g. meetings) where you want to stream chunks
   * to a server rather than holding a multi-hour recording in memory.
   *
   * Cannot be used concurrently with `startRecording`.
   */
  startChunkedRecording(options?: ChunkedRecordingOptions): Promise<GenericResponse>;

  /**
   * Stop the chunked recording. Emits a final `audioChunk` event with `isFinalChunk: true`
   * before resolving.
   */
  stopChunkedRecording(): Promise<GenericResponse>;

  /**
   * Pause chunked recording. On iOS/Android this calls pause on the underlying native
   * recorder (API 24+). The chunk timer is paused — no chunks are emitted while paused.
   */
  pauseChunkedRecording(): Promise<GenericResponse>;

  /**
   * Resume chunked recording. If the recorder is in the INTERRUPTED state (e.g. after a
   * phone call), a fresh native recorder is created and chunking resumes with the next
   * sequential `chunkIndex`.
   */
  resumeChunkedRecording(): Promise<GenericResponse>;

  /**
   * Listen for audio recording interruptions (e.g., phone calls, other apps using microphone).
   * Available on iOS and Android only.
   *
   * @param eventName - The name of the event to listen for
   * @param listenerFunc - The callback function to invoke when the event occurs
   * @returns A promise that resolves to a PluginListenerHandle
   */
  addListener(
    eventName: 'voiceRecordingInterrupted',
    listenerFunc: (event: VoiceRecordingInterruptedEvent) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listen for audio recording interruption end events.
   * Available on iOS and Android only.
   *
   * @param eventName - The name of the event to listen for
   * @param listenerFunc - The callback function to invoke when the event occurs
   * @returns A promise that resolves to a PluginListenerHandle
   */
  addListener(
    eventName: 'voiceRecordingInterruptionEnded',
    listenerFunc: (event: VoiceRecordingInterruptionEndedEvent) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listen for audio chunk events emitted during a chunked recording session.
   * Call this before `startChunkedRecording()` to avoid missing the first chunk.
   *
   * @param eventName - `'audioChunk'`
   * @param listenerFunc - The callback invoked for every chunk, including the final one
   * @returns A promise that resolves to a PluginListenerHandle
   */
  addListener(
    eventName: 'audioChunk',
    listenerFunc: (chunk: AudioChunk) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Remove all listeners for this plugin.
   */
  removeAllListeners(): Promise<void>;
}
