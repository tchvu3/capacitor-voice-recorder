import type { GenericResponse } from './definitions';

export const successResponse = (): GenericResponse => ({ value: true });
export const failureResponse = (): GenericResponse => ({ value: false });

export const missingPermissionError = (): Error => new Error('MISSING_PERMISSION');
export const alreadyRecordingError = (): Error => new Error('ALREADY_RECORDING');
export const microphoneBeingUsedError = (): Error => new Error('MICROPHONE_BEING_USED');
export const deviceCannotVoiceRecordError = (): Error => new Error('DEVICE_CANNOT_VOICE_RECORD');
export const failedToRecordError = (): Error => new Error('FAILED_TO_RECORD');
export const emptyRecordingError = (): Error => new Error('EMPTY_RECORDING');

export const recordingHasNotStartedError = (): Error => new Error('RECORDING_HAS_NOT_STARTED');
export const failedToFetchRecordingError = (): Error => new Error('FAILED_TO_FETCH_RECORDING');

export const couldNotQueryPermissionStatusError = (): Error => new Error('COULD_NOT_QUERY_PERMISSION_STATUS');
