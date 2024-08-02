/**
 * Represents a Base64 encoded string.
 */
export type Base64String = string;

/**
 * Interface representing the data of a recording.
 */
export interface RecordingData {
  /**
   * The value containing the recording details.
   */
  value: {
    /**
     * The recorded data as a Base64 encoded string.
     */
    recordDataBase64: Base64String;
    /**
     * The duration of the recording in milliseconds.
     */
    msDuration: number;
    /**
     * The MIME type of the recorded file.
     */
    mimeType: string;
  };
}

/**
 * Interface representing a generic response with a boolean value.
 */
export interface GenericResponse {
  /**
   * The result of the operation as a boolean value.
   */
  value: boolean;
}

/**
 * Interface representing the current status of the voice recorder.
 */
export interface CurrentRecordingStatus {
  /**
   * The current status of the recorder, which can be one of the following values: 'RECORDING', 'PAUSED', 'NONE'.
   */
  status: 'RECORDING' | 'PAUSED' | 'NONE';
}

/**
 * Interface for the VoiceRecorderPlugin which provides methods to record audio.
 */
export interface VoiceRecorderPlugin {
  /**
   * Checks if the current device can record audio.
   * On mobile, this function will always resolve to `{ value: true }`.
   * In a browser, it will resolve to `{ value: true }` or `{ value: false }` based on the browser's ability to record.
   * This method does not take into account the permission status, only if the browser itself is capable of recording at all.
   * @returns A promise that resolves to a GenericResponse.
   * @throws Error with code "COULD_NOT_QUERY_PERMISSION_STATUS" if the device cannot query the permission status.
   */
  canDeviceVoiceRecord(): Promise<GenericResponse>;

  /**
   * Requests audio recording permission from the user.
   * If the permission has already been provided, the promise will resolve with `{ value: true }`.
   * Otherwise, the promise will resolve to `{ value: true }` or `{ value: false }` based on the user's response.
   * @returns A promise that resolves to a GenericResponse.
   * @throws Error if the permission request fails.
   */
  requestAudioRecordingPermission(): Promise<GenericResponse>;

  /**
   * Checks if audio recording permission has been granted.
   * Will resolve to `{ value: true }` or `{ value: false }` based on the status of the permission.
   * The web implementation of this plugin uses the Permissions API, which is not widespread.
   * If the status of the permission cannot be checked, the promise will reject with `COULD_NOT_QUERY_PERMISSION_STATUS`.
   * In that case, use `requestAudioRecordingPermission` or `startRecording` and capture any exception that is thrown.
   * @returns A promise that resolves to a GenericResponse.
   * @throws Error with code "COULD_NOT_QUERY_PERMISSION_STATUS" if the device cannot query the permission status.
   */
  hasAudioRecordingPermission(): Promise<GenericResponse>;

  /**
   * Starts audio recording.
   * On success, the promise will resolve to { value: true }.
   * On error, the promise will reject with one of the following error codes:
   * "MISSING_PERMISSION", "ALREADY_RECORDING", "MICROPHONE_BEING_USED", "DEVICE_CANNOT_VOICE_RECORD", or "FAILED_TO_RECORD".
   * @returns A promise that resolves to a GenericResponse.
   * @throws Error with one of the specified error codes if the recording cannot be started.
   */
  startRecording(): Promise<GenericResponse>;

  /**
   * Stops audio recording.
   * Will stop the recording that has been previously started.
   * If the function `startRecording` has not been called beforehand, the promise will reject with `RECORDING_HAS_NOT_STARTED`.
   * If the recording has been stopped immediately after it has been started, the promise will reject with `EMPTY_RECORDING`.
   * In a case of unknown error, the promise will reject with `FAILED_TO_FETCH_RECORDING`.
   * In case of success, the promise resolves to RecordingData containing the recording in base-64, the duration of the recording in milliseconds, and the MIME type.
   * @returns A promise that resolves to RecordingData.
   * @throws Error with one of the specified error codes if the recording cannot be stopped.
   */
  stopRecording(): Promise<RecordingData>;

  /**
   * Pauses the ongoing audio recording.
   * If the recording has not started yet, the promise will reject with an error code `RECORDING_HAS_NOT_STARTED`.
   * On success, the promise will resolve to { value: true } if the pause was successful or { value: false } if the recording is already paused.
   * On certain mobile OS versions, this function is not supported and will reject with `NOT_SUPPORTED_OS_VERSION`.
   * @returns A promise that resolves to a GenericResponse.
   * @throws Error with one of the specified error codes if the recording cannot be paused.
   */
  pauseRecording(): Promise<GenericResponse>;

  /**
   * Resumes a paused audio recording.
   * If the recording has not started yet, the promise will reject with an error code `RECORDING_HAS_NOT_STARTED`.
   * On success, the promise will resolve to { value: true } if the resume was successful or { value: false } if the recording is already running.
   * On certain mobile OS versions, this function is not supported and will reject with `NOT_SUPPORTED_OS_VERSION`.
   * @returns A promise that resolves to a GenericResponse.
   * @throws Error with one of the specified error codes if the recording cannot be resumed.
   */
  resumeRecording(): Promise<GenericResponse>;

  /**
   * Gets the current status of the voice recorder.
   * Will resolve with one of the following values:
   * `{ status: "NONE" }` if the plugin is idle and waiting to start a new recording.
   * `{ status: "RECORDING" }` if the plugin is in the middle of recording.
   * `{ status: "PAUSED" }` if the recording is paused.
   * @returns A promise that resolves to a CurrentRecordingStatus.
   * @throws Error if the status cannot be fetched.
   */
  getCurrentStatus(): Promise<CurrentRecordingStatus>;
}
