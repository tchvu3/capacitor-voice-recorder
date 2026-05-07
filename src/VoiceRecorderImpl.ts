import write_blob from 'capacitor-blob-writer';
import getBlobDuration from 'get-blob-duration';

import type {
  AudioChunk,
  Base64String,
  ChunkedRecordingOptions,
  CurrentRecordingStatus,
  GenericResponse,
  RecordingData,
  RecordingOptions,
} from './definitions';
import { RecordingStatus } from './definitions';
import {
  alreadyRecordingError,
  couldNotQueryPermissionStatusError,
  deviceCannotVoiceRecordError,
  emptyRecordingError,
  failedToFetchRecordingError,
  failedToRecordError,
  failureResponse,
  missingPermissionError,
  recordingHasNotStartedError,
  successResponse,
} from './predefined-web-responses';

// these mime types will be checked one by one in order until one of them is found to be supported by the current browser
const POSSIBLE_MIME_TYPES = {
  'audio/aac': '.aac',
  'audio/mp4': '.mp3',
  'audio/webm;codecs=opus': '.ogg',
  'audio/webm': '.ogg',
  'audio/ogg;codecs=opus': '.ogg',
};
const DEFAULT_CHUNK_INTERVAL_MS = 30000;
const neverResolvingPromise = (): Promise<any> => new Promise(() => undefined);

export type AudioChunkEmitter = (chunk: AudioChunk) => void;

export class VoiceRecorderImpl {
  private mediaRecorder: MediaRecorder | null = null;
  private chunks: any[] = [];
  private pendingResult: Promise<RecordingData> = neverResolvingPromise();

  // Chunked recording state
  private chunkedStream: MediaStream | null = null;
  private chunkedRecorder: MediaRecorder | null = null;
  private chunkedBuffer: Blob[] = [];
  private chunkedInterval: number = DEFAULT_CHUNK_INTERVAL_MS;
  private chunkedTimer: ReturnType<typeof setTimeout> | null = null;
  private chunkIndex: number = 0;
  private chunkedEmitter: AudioChunkEmitter | null = null;
  private chunkedMimeType: string | null = null;

  public static async canDeviceVoiceRecord(): Promise<GenericResponse> {
    if (navigator?.mediaDevices?.getUserMedia == null || VoiceRecorderImpl.getSupportedMimeType() == null) {
      return failureResponse();
    } else {
      return successResponse();
    }
  }

  public async startRecording(options?: RecordingOptions): Promise<GenericResponse> {
    if (this.mediaRecorder != null || this.chunkedRecorder != null) {
      throw alreadyRecordingError();
    }
    const deviceCanRecord = await VoiceRecorderImpl.canDeviceVoiceRecord();
    if (!deviceCanRecord.value) {
      throw deviceCannotVoiceRecordError();
    }
    const havingPermission = await VoiceRecorderImpl.hasAudioRecordingPermission().catch(() => successResponse());
    if (!havingPermission.value) {
      throw missingPermissionError();
    }

    return navigator.mediaDevices
      .getUserMedia({ audio: true })
      .then((stream) => this.onSuccessfullyStartedRecording(stream, options))
      .catch(this.onFailedToStartRecording.bind(this));
  }

  public async stopRecording(): Promise<RecordingData> {
    if (this.mediaRecorder == null) {
      throw recordingHasNotStartedError();
    }
    try {
      this.mediaRecorder.stop();
      this.mediaRecorder.stream.getTracks().forEach((track) => track.stop());
      return this.pendingResult;
    } catch (ignore) {
      throw failedToFetchRecordingError();
    } finally {
      this.prepareInstanceForNextOperation();
    }
  }

  public static async hasAudioRecordingPermission(): Promise<GenericResponse> {
    if (navigator.permissions.query == null) {
      if (navigator.mediaDevices == null) {
        return Promise.reject(couldNotQueryPermissionStatusError());
      }
      return navigator.mediaDevices
        .getUserMedia({ audio: true })
        .then(() => successResponse())
        .catch(() => {
          throw couldNotQueryPermissionStatusError();
        });
    }

    return navigator.permissions
      .query({ name: 'microphone' as any })
      .then((result) => ({ value: result.state === 'granted' }))
      .catch(() => {
        throw couldNotQueryPermissionStatusError();
      });
  }

  public static async requestAudioRecordingPermission(): Promise<GenericResponse> {
    const havingPermission = await VoiceRecorderImpl.hasAudioRecordingPermission().catch(() => failureResponse());
    if (havingPermission.value) {
      return successResponse();
    }

    return navigator.mediaDevices
      .getUserMedia({ audio: true })
      .then(() => successResponse())
      .catch(() => failureResponse());
  }

  public pauseRecording(): Promise<GenericResponse> {
    if (this.mediaRecorder == null) {
      throw recordingHasNotStartedError();
    } else if (this.mediaRecorder.state === 'recording') {
      this.mediaRecorder.pause();
      return Promise.resolve(successResponse());
    } else {
      return Promise.resolve(failureResponse());
    }
  }

  public resumeRecording(): Promise<GenericResponse> {
    if (this.mediaRecorder == null) {
      throw recordingHasNotStartedError();
    } else if (this.mediaRecorder.state === 'paused') {
      this.mediaRecorder.resume();
      return Promise.resolve(successResponse());
    } else {
      return Promise.resolve(failureResponse());
    }
  }

  public getCurrentStatus(): Promise<CurrentRecordingStatus> {
    if (this.mediaRecorder != null) {
      if (this.mediaRecorder.state === 'recording') {
        return Promise.resolve({ status: RecordingStatus.RECORDING });
      }
      if (this.mediaRecorder.state === 'paused') {
        return Promise.resolve({ status: RecordingStatus.PAUSED });
      }
      return Promise.resolve({ status: RecordingStatus.NONE });
    }
    if (this.chunkedRecorder != null) {
      if (this.chunkedRecorder.state === 'recording') {
        return Promise.resolve({ status: RecordingStatus.RECORDING });
      }
      if (this.chunkedRecorder.state === 'paused') {
        return Promise.resolve({ status: RecordingStatus.PAUSED });
      }
    }
    return Promise.resolve({ status: RecordingStatus.NONE });
  }

  // --- chunked recording ---

  public setChunkEmitter(emitter: AudioChunkEmitter | null): void {
    this.chunkedEmitter = emitter;
  }

  public async startChunkedRecording(options?: ChunkedRecordingOptions): Promise<GenericResponse> {
    if (this.chunkedRecorder != null || this.mediaRecorder != null) {
      throw alreadyRecordingError();
    }
    const deviceCanRecord = await VoiceRecorderImpl.canDeviceVoiceRecord();
    if (!deviceCanRecord.value) {
      throw deviceCannotVoiceRecordError();
    }
    const havingPermission = await VoiceRecorderImpl.hasAudioRecordingPermission().catch(() => successResponse());
    if (!havingPermission.value) {
      throw missingPermissionError();
    }

    const mimeType = VoiceRecorderImpl.getSupportedMimeType();
    if (mimeType == null) {
      throw deviceCannotVoiceRecordError();
    }

    this.chunkedInterval = options?.chunkIntervalMs ?? DEFAULT_CHUNK_INTERVAL_MS;
    this.chunkedMimeType = mimeType;
    this.chunkIndex = 0;
    this.chunkedBuffer = [];

    try {
      this.chunkedStream = await navigator.mediaDevices.getUserMedia({ audio: true });
    } catch {
      this.prepareChunkedInstanceForNextOperation();
      throw failedToRecordError();
    }

    this.startNewChunkedRecorder();
    this.scheduleNextChunk();
    return successResponse();
  }

  public async stopChunkedRecording(): Promise<GenericResponse> {
    if (this.chunkedRecorder == null) {
      throw recordingHasNotStartedError();
    }
    if (this.chunkedTimer != null) {
      clearTimeout(this.chunkedTimer);
      this.chunkedTimer = null;
    }

    // Stop current recorder and emit final chunk.
    await this.rotateChunkAndEmit(true);

    if (this.chunkedStream != null) {
      this.chunkedStream.getTracks().forEach((track) => track.stop());
    }
    this.prepareChunkedInstanceForNextOperation();
    return successResponse();
  }

  public pauseChunkedRecording(): Promise<GenericResponse> {
    if (this.chunkedRecorder == null) {
      throw recordingHasNotStartedError();
    }
    if (this.chunkedRecorder.state === 'recording') {
      if (this.chunkedTimer != null) {
        clearTimeout(this.chunkedTimer);
        this.chunkedTimer = null;
      }
      this.chunkedRecorder.pause();
      return Promise.resolve(successResponse());
    }
    return Promise.resolve(failureResponse());
  }

  public resumeChunkedRecording(): Promise<GenericResponse> {
    if (this.chunkedRecorder == null) {
      throw recordingHasNotStartedError();
    }
    if (this.chunkedRecorder.state === 'paused') {
      this.chunkedRecorder.resume();
      this.scheduleNextChunk();
      return Promise.resolve(successResponse());
    }
    return Promise.resolve(failureResponse());
  }

  // --- internals ---

  public static getSupportedMimeType<T extends keyof typeof POSSIBLE_MIME_TYPES>(): T | null {
    if (MediaRecorder?.isTypeSupported == null) return null;

    const foundSupportedType = Object.keys(POSSIBLE_MIME_TYPES).find((type) => MediaRecorder.isTypeSupported(type)) as
      | T
      | undefined;

    return foundSupportedType ?? null;
  }

  private onSuccessfullyStartedRecording(stream: MediaStream, options?: RecordingOptions): GenericResponse {
    this.pendingResult = new Promise((resolve, reject) => {
      this.mediaRecorder = new MediaRecorder(stream);
      this.mediaRecorder.onerror = () => {
        this.prepareInstanceForNextOperation();
        reject(failedToRecordError());
      };
      this.mediaRecorder.onstop = async () => {
        const mimeType = VoiceRecorderImpl.getSupportedMimeType();
        if (mimeType == null) {
          this.prepareInstanceForNextOperation();
          reject(failedToFetchRecordingError());
          return;
        }
        const blobVoiceRecording = new Blob(this.chunks, { type: mimeType });
        if (blobVoiceRecording.size <= 0) {
          this.prepareInstanceForNextOperation();
          reject(emptyRecordingError());
          return;
        }

        let path;
        let recordDataBase64;
        if (options != null) {
          const subDirectory = options.subDirectory?.match(/^\/?(.+[^/])\/?$/)?.[1] ?? '';
          path = `${subDirectory}/recording-${new Date().getTime()}${POSSIBLE_MIME_TYPES[mimeType]}`;

          await write_blob({
            blob: blobVoiceRecording,
            directory: options.directory,
            fast_mode: true,
            path,
            recursive: true,
          });
        } else {
          recordDataBase64 = await VoiceRecorderImpl.blobToBase64(blobVoiceRecording);
        }

        const recordingDuration = await getBlobDuration(blobVoiceRecording);
        this.prepareInstanceForNextOperation();
        resolve({ value: { recordDataBase64, mimeType, msDuration: recordingDuration * 1000, path } });
      };
      this.mediaRecorder.ondataavailable = (event: any) => this.chunks.push(event.data);
      this.mediaRecorder.start();
    });
    return successResponse();
  }

  private startNewChunkedRecorder(): void {
    if (this.chunkedStream == null || this.chunkedMimeType == null) return;
    this.chunkedBuffer = [];
    this.chunkedRecorder = new MediaRecorder(this.chunkedStream);
    this.chunkedRecorder.ondataavailable = (event: BlobEvent) => {
      if (event.data && event.data.size > 0) {
        this.chunkedBuffer.push(event.data);
      }
    };
    this.chunkedRecorder.start();
  }

  private scheduleNextChunk(): void {
    if (this.chunkedRecorder == null || this.chunkedRecorder.state !== 'recording') return;
    if (this.chunkedTimer != null) clearTimeout(this.chunkedTimer);
    this.chunkedTimer = setTimeout(async () => {
      await this.rotateChunkAndEmit(false);
      this.scheduleNextChunk();
    }, this.chunkedInterval);
  }

  private async rotateChunkAndEmit(isFinalChunk: boolean): Promise<void> {
    if (this.chunkedRecorder == null || this.chunkedMimeType == null) return;

    // Stop the current recorder and wait for its final ondataavailable to flush.
    const currentRecorder = this.chunkedRecorder;
    const bufferToFlush = this.chunkedBuffer;
    const mimeType = this.chunkedMimeType;

    const stopped = new Promise<void>((resolve) => {
      currentRecorder.onstop = () => resolve();
      try {
        currentRecorder.stop();
      } catch {
        resolve();
      }
    });
    // Detach so the new recorder created below doesn't collide.
    this.chunkedRecorder = null;
    this.chunkedBuffer = [];

    await stopped;

    const blob = new Blob(bufferToFlush, { type: mimeType });
    if (blob.size > 0) {
      const base64 = await VoiceRecorderImpl.blobToBase64(blob);
      let duration = 0;
      try {
        duration = (await getBlobDuration(blob)) * 1000;
      } catch {
        duration = 0;
      }
      const chunk: AudioChunk = {
        recordDataBase64: base64,
        msDuration: Math.round(duration),
        mimeType,
        chunkIndex: this.chunkIndex,
        isFinalChunk,
      };
      this.chunkedEmitter?.(chunk);
      this.chunkIndex++;
    }

    if (!isFinalChunk) {
      this.startNewChunkedRecorder();
    }
  }

  private onFailedToStartRecording(): GenericResponse {
    this.prepareInstanceForNextOperation();
    throw failedToRecordError();
  }

  private static blobToBase64(blob: Blob): Promise<Base64String> {
    return new Promise((resolve) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        const recordingResult = String(reader.result);
        const splitResult = recordingResult.split('base64,');
        const toResolve = splitResult.length > 1 ? splitResult[1] : recordingResult;
        resolve(toResolve.trim());
      };
      reader.readAsDataURL(blob);
    });
  }

  private prepareInstanceForNextOperation(): void {
    if (this.mediaRecorder != null && this.mediaRecorder.state === 'recording') {
      try {
        this.mediaRecorder.stop();
      } catch (error) {
        console.warn('While trying to stop a media recorder, an error was thrown', error);
      }
    }
    this.pendingResult = neverResolvingPromise();
    this.mediaRecorder = null;
    this.chunks = [];
  }

  private prepareChunkedInstanceForNextOperation(): void {
    if (this.chunkedTimer != null) {
      clearTimeout(this.chunkedTimer);
      this.chunkedTimer = null;
    }
    if (this.chunkedRecorder != null && this.chunkedRecorder.state !== 'inactive') {
      try {
        this.chunkedRecorder.stop();
      } catch (error) {
        console.warn('While trying to stop a chunked media recorder, an error was thrown', error);
      }
    }
    this.chunkedRecorder = null;
    this.chunkedStream = null;
    this.chunkedBuffer = [];
    this.chunkIndex = 0;
    this.chunkedMimeType = null;
  }
}
