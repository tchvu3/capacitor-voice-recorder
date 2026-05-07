<p align="center">
  <img src="https://user-images.githubusercontent.com/236501/85893648-1c92e880-b7a8-11ea-926d-95355b8175c7.png" width="128" height="128" />
</p>
<h3 align="center">Capacitor Voice Recorder</h3>
<p align="center"><strong><code>tchvu3/capacitor-voice-recorder</code></strong></p>
<p align="center">Capacitor plugin for simple voice recording</p>

<p align="center">
  <img src="https://img.shields.io/maintenance/yes/2025" />
  <a href="https://www.npmjs.com/package/capacitor-voice-recorder"><img src="https://img.shields.io/npm/l/capacitor-voice-recorder" /></a>
<br>
  <a href="https://www.npmjs.com/package/capacitor-voice-recorder"><img src="https://img.shields.io/npm/dw/capacitor-voice-recorder" /></a>
  <a href="https://www.npmjs.com/package/capacitor-voice-recorder"><img src="https://img.shields.io/npm/v/capacitor-voice-recorder" /></a>
</p>

## Maintainers

| Maintainer   | GitHub                              |
|--------------|-------------------------------------|
| Avihu Harush | [tchvu3](https://github.com/tchvu3) |

## Installation

```
npm install --save capacitor-voice-recorder
npx cap sync
```

## Configuration

### Using with Android

Add the following to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

### Using with iOS

Add the following to your `Info.plist`:

```xml

<key>NSMicrophoneUsageDescription</key>
<string>This app uses the microphone to record audio.</string>
```

## Supported methods

| Name                            | Android | iOS | Web |
|:--------------------------------|:--------|:----|:----|
| canDeviceVoiceRecord            | ✅       | ✅   | ✅   |
| requestAudioRecordingPermission | ✅       | ✅   | ✅   |
| hasAudioRecordingPermission     | ✅       | ✅   | ✅   |
| startRecording                  | ✅       | ✅   | ✅   |
| stopRecording                   | ✅       | ✅   | ✅   |
| pauseRecording                  | ✅       | ✅   | ✅   |
| resumeRecording                 | ✅       | ✅   | ✅   |
| getCurrentStatus                | ✅       | ✅   | ✅   |
| startChunkedRecording           | ✅       | ✅   | ✅   |
| stopChunkedRecording            | ✅       | ✅   | ✅   |
| pauseChunkedRecording           | ✅       | ✅   | ✅   |
| resumeChunkedRecording          | ✅       | ✅   | ✅   |

## Events

| Event Name                        | Android | iOS | Web |
|:----------------------------------|:--------|:----|:----|
| `voiceRecordingInterrupted`       | ✅       | ✅   | ❌   |
| `voiceRecordingInterruptionEnded` | ✅       | ✅   | ❌   |
| `audioChunk`                      | ✅       | ✅   | ✅   |

## Overview

The `capacitor-voice-recorder` plugin allows you to record audio on Android, iOS, and Web platforms.
Below is a summary
of the key methods and how to use them.

### Checking Device Capabilities and Permissions

#### canDeviceVoiceRecord

Check if the device/browser can record audio.

```typescript
VoiceRecorder.canDeviceVoiceRecord().then((result: GenericResponse) => console.log(result.value));
```

| Return Value       | Description                                                                            |
|--------------------|----------------------------------------------------------------------------------------|
| `{ value: true }`  | The device/browser can record audio.                                                   |
| `{ value: false }` | The browser cannot record audio. Note: On mobile, it always returns `{ value: true }`. |

#### requestAudioRecordingPermission

Request audio recording permission from the user.

```typescript
VoiceRecorder.requestAudioRecordingPermission().then((result: GenericResponse) => console.log(result.value));
```

| Return Value       | Description         |
|--------------------|---------------------|
| `{ value: true }`  | Permission granted. |
| `{ value: false }` | Permission denied.  |

#### hasAudioRecordingPermission

Check if the audio recording permission has been granted.

```typescript
VoiceRecorder.hasAudioRecordingPermission().then((result: GenericResponse) => console.log(result.value));
```

| Return Value       | Description         |
|--------------------|---------------------|
| `{ value: true }`  | Permission granted. |
| `{ value: false }` | Permission denied.  |

| Error Code                          | Description                        |
|-------------------------------------|------------------------------------|
| `COULD_NOT_QUERY_PERMISSION_STATUS` | Failed to query permission status. |

### Managing Recording

#### startRecording

Start the audio recording.

Optional options can be used with this method to save the file in the device's filesystem and return a path to that file instead of a base64 string.
This greatly increases performance for large files.

```typescript
VoiceRecorder.startRecording(options?: RecordingOptions)
    .then((result: GenericResponse) => console.log(result.value))
    .catch(error => console.log(error));
```

| Option            | Description                                                                                          |
|-------------------|------------------------------------------------------------------------------------------------------|
| directory         | Specifies a Capacitor Filesystem [Directory](https://capacitorjs.com/docs/apis/filesystem#directory) |
| subDirectory      | Specifies a custom sub-directory (optional)                                                          |

| Return Value      | Description                     |
|-------------------|---------------------------------|
| `{ value: true }` | Recording started successfully. |

| Error Code                   | Description                              |
|------------------------------|------------------------------------------|
| `MISSING_PERMISSION`         | Required permission is missing.          |
| `DEVICE_CANNOT_VOICE_RECORD` | Device/browser cannot record audio.      |
| `ALREADY_RECORDING`          | A recording is already in progress.      |
| `MICROPHONE_BEING_USED`      | Microphone is being used by another app. |
| `FAILED_TO_RECORD`           | Unknown error occurred during recording. |

#### stopRecording

Stops the audio recording and returns the recording data.

When a `directory` option has been passed to the `VoiceRecorder.startRecording` method the data will include a `path` instead of a `recordDataBase64`

```typescript
VoiceRecorder.stopRecording()
    .then((result: RecordingData) => console.log(result.value))
    .catch(error => console.log(error));
```

| Return Value       | Description                                    |
|--------------------|------------------------------------------------|
| `recordDataBase64` | The recorded audio data in Base64 format.      |
| `msDuration`       | The duration of the recording in milliseconds. |
| `mimeType`         | The MIME type of the recorded audio.           |
| `path`             | The path to the audio file                     |

| Error Code                  | Description                                          |
|-----------------------------|------------------------------------------------------|
| `RECORDING_HAS_NOT_STARTED` | No recording in progress.                            |
| `EMPTY_RECORDING`           | Recording stopped immediately after starting.        |
| `FAILED_TO_FETCH_RECORDING` | Unknown error occurred while fetching the recording. |
| `FAILED_TO_MERGE_RECORDING` | Failed to merge audio segments after interruption (iOS only). |

#### pauseRecording

Pause the ongoing audio recording.

```typescript
VoiceRecorder.pauseRecording()
    .then((result: GenericResponse) => console.log(result.value))
    .catch(error => console.log(error));
```

| Return Value       | Description                    |
|--------------------|--------------------------------|
| `{ value: true }`  | Recording paused successfully. |
| `{ value: false }` | Recording is already paused.   |

| Error Code                  | Description                                        |
|-----------------------------|----------------------------------------------------|
| `RECORDING_HAS_NOT_STARTED` | No recording in progress.                          |
| `NOT_SUPPORTED_OS_VERSION`  | Operation not supported on the current OS version. |

#### resumeRecording

Resumes a paused or interrupted audio recording.

```typescript
VoiceRecorder.resumeRecording()
    .then((result: GenericResponse) => console.log(result.value))
    .catch(error => console.log(error));
```

| Return Value       | Description                     |
|--------------------|---------------------------------|
| `{ value: true }`  | Recording resumed successfully. |
| `{ value: false }` | Recording is already running.   |

**Note**: This method works with both `PAUSED` (user-initiated) and `INTERRUPTED` (system-initiated) states.

| Error Code                  | Description                                        |
|-----------------------------|----------------------------------------------------|
| `RECORDING_HAS_NOT_STARTED` | No recording in progress.                          |
| `NOT_SUPPORTED_OS_VERSION`  | Operation not supported on the current OS version. |

#### getCurrentStatus

Retrieves the current status of the recorder.

```typescript
VoiceRecorder.getCurrentStatus()
    .then((result: CurrentRecordingStatus) => console.log(result.status))
    .catch(error => console.log(error));
```

| Status Code   | Description                                              |
|---------------|----------------------------------------------------------|
| `NONE`        | Plugin is idle and waiting to start a new recording.     |
| `RECORDING`   | Plugin is currently recording.                           |
| `PAUSED`      | Recording is paused by user.                             |
| `INTERRUPTED` | Recording was paused due to system interruption.         |

### Audio Interruption Handling

The plugin automatically handles audio interruptions on **iOS** and **Android** (such as phone calls, other apps using the microphone, or system notifications). When an interruption occurs, the recording is automatically paused and the state changes to `INTERRUPTED`.

#### How It Works

1. **Interruption Begins**: When a phone call comes in or another app takes audio focus, the plugin automatically pauses the recording and emits a `voiceRecordingInterrupted` event.

2. **Interruption Ends**: When the interruption ends (e.g., phone call finishes), the plugin emits a `voiceRecordingInterruptionEnded` event, but keeps the state as `INTERRUPTED`.

3. **User Decision**: The app can then decide whether to resume recording (using `resumeRecording()`) or stop it (using `stopRecording()`).

#### Listening to Interruption Events

```typescript
import { VoiceRecorder } from 'capacitor-voice-recorder';

// Listen for interruption events (iOS & Android only)
VoiceRecorder.addListener('voiceRecordingInterrupted', () => {
  console.log('Recording was interrupted (e.g., phone call)');
  // Update UI to show interrupted state
});

VoiceRecorder.addListener('voiceRecordingInterruptionEnded', () => {
  console.log('Interruption ended - recording is still paused');
  // Optionally prompt user to resume or stop recording
  // VoiceRecorder.resumeRecording() or VoiceRecorder.stopRecording()
});
```

#### Platform Support

| Platform | Interruption Handling |
|----------|----------------------|
| iOS      | ✅ Full support (AVAudioSession interruption notifications) |
| Android  | ✅ Full support (AudioManager audio focus) |
| Web      | ❌ Not supported (maintains existing behavior) |

**Note**: The `INTERRUPTED` state is distinct from `PAUSED`. `PAUSED` is user-initiated, while `INTERRUPTED` is system-initiated. Both states can be resumed using `resumeRecording()`.

#### Technical Implementation (iOS)

On iOS, the `AVAudioRecorder.record()` method internally calls `prepareToRecord()`, which overwrites the existing audio file. To preserve audio across interruptions, the plugin implements segmented recording:

1. **Initial Recording**: Creates the base recording file (e.g., `recording-1234567890.aac`)
2. **After Interruption**: When resuming from `INTERRUPTED` state, a new segment file is created (e.g., `recording-1234567891-segment-1.aac`)
3. **Multiple Interruptions**: Each subsequent interruption creates additional numbered segments
4. **Merging**: When `stopRecording()` is called, all segments are automatically merged into a single audio file using `AVMutableComposition` and `AVAssetExportSession`
5. **Cleanup**: Temporary segment files are deleted after successful merge

**Note**: Regular pause/resume (user-initiated) continues to use a single file without segmentation. Only system interruptions trigger segmented recording.

**Format Change**: When audio interruptions occur and segments are merged, the final output file will be in M4A format (MIME type: `audio/mp4`) instead of AAC (MIME type: `audio/aac`). This is because AVAssetExportSession exports to M4A container format. Recordings without interruptions remain in AAC format.

## Format and Mime type

The plugin will return the recording in one of several possible formats.
The format is dependent on the os / web browser that the user uses.
On android and ios the mime type will be `audio/aac`, while on chrome and firefox it
will be `audio/webm;codecs=opus` and on safari it will be `audio/mp4`.
Note that these three browsers have been tested on.
The plugin should still work on other browsers,
as there is a list of mime types that the plugin checks against the user's browser.

Note that this fact might cause unexpected behavior in case you'll try to play recordings
between several devices or browsers—as they do not all support the same set of audio formats.
It is recommended to convert the recordings to a format that all your target devices support.
As this plugin focuses on the recording aspect, it does not provide any conversion between formats.

## Playback

To play the recorded file, you can use plain JavaScript:

### With Base64 string

```typescript
const base64Sound = '...' // from plugin
const mimeType = '...'  // from plugin
const audioRef = new Audio(`data:${mimeType};base64,${base64Sound}`)
audioRef.oncanplaythrough = () => audioRef.play()
audioRef.load()
```

### With Blob

```typescript
import { Capacitor } from '@capacitor/core'
import { Directory, Filesystem } from '@capacitor/filesystem'

const PATH = '...' // from plugin

/** Generate a URL to the blob file with @capacitor/core and @capacitor/filesystem */
const getBlobURL = async (path: string) => {
  const directory = Directory.Data // Same Directory as the one you used with VoiceRecorder.startRecording

  if (config.public.platform === 'web') {
    const { data } = await Filesystem.readFile({ directory, path })
    return URL.createObjectURL(data)
  }

  const { uri } = await Filesystem.getUri({ directory, path })
  return Capacitor.convertFileSrc(uri)
}

/** Read the audio file */
const play = async () => {
  const url = await getBlobURL(PATH)
  const audioRef = new Audio(url)
  audioRef.onended = () => { URL.revokeObjectUrl(url) }
  audioRef.play()
}

/** Load the audio file (ie: to send to a Cloud Storage service) */
const load = async () => {
  const url = await getBlobURL(PATH)
  const response = await fetch(url)
  return response.blob()
}
```

## Compatibility

Versioning follows Capacitor versioning.
Major versions of the plugin are compatible with major versions of Capacitor.
You can find each version in its own dedicated branch.

| Plugin Version | Capacitor Version |
|----------------|-------------------|
| 5.*            | 5                 |
| 6.*            | 6                 |
| 7.*            | 7                 |

## Chunked Recording

For long-form recordings (e.g. meetings, lectures) where holding a multi-hour audio buffer in memory is undesirable, use the chunked recording API. The plugin opens a fresh native recorder every `chunkIntervalMs` milliseconds, emits the captured audio as an `audioChunk` event, and immediately starts a new recorder. Each chunk is a **standalone, decodable audio file** — you can upload or transcribe chunks individually as they arrive.

```typescript
import { VoiceRecorder, type AudioChunk, type PluginListenerHandle } from 'capacitor-voice-recorder';

let listener: PluginListenerHandle | undefined;

async function startMeeting() {
  // Register the listener BEFORE starting so the first chunk isn't missed.
  listener = await VoiceRecorder.addListener('audioChunk', (chunk: AudioChunk) => {
    const { recordDataBase64, msDuration, mimeType, chunkIndex, isFinalChunk } = chunk;
    // Upload the chunk, decode it, feed it to a transcription service, etc.
    console.log(`chunk ${chunkIndex} (${msDuration}ms, ${mimeType}), final=${isFinalChunk}`);
  });

  await VoiceRecorder.startChunkedRecording({ chunkIntervalMs: 5 * 60 * 1000 }); // 5 min chunks
}

async function endMeeting() {
  await VoiceRecorder.stopChunkedRecording(); // emits final chunk (isFinalChunk: true) before resolving
  await listener?.remove();
}
```

#### Chunk boundary gap

Because each chunk uses a fresh native recorder, there is a brief gap between chunks (~10–50 ms native vs. 200–500 ms if you did stop-restart from JS). This is practically inaudible for transcription use cases but not zero. If you need gapless audio, merge chunks server-side or use `startRecording` with a single in-memory buffer.

#### Interruptions (iOS/Android)

When a phone call interrupts a chunked recording:
1. The plugin stops the current recorder.
2. The audio captured so far is emitted as a non-final chunk (so it isn't lost).
3. The plugin transitions to `INTERRUPTED` state and fires `voiceRecordingInterrupted`.
4. When the interruption ends, `voiceRecordingInterruptionEnded` fires. Call `resumeChunkedRecording()` to continue (a new native recorder is created and the `chunkIndex` continues from where it left off).

#### Differences from `startRecording`

| Feature                   | `startRecording`                                       | `startChunkedRecording`                                |
|---------------------------|--------------------------------------------------------|--------------------------------------------------------|
| Output                    | Single file returned when `stopRecording()` is called  | Stream of `audioChunk` events                          |
| Memory use                | Grows linearly with recording duration                 | Bounded (only the current chunk is held in memory)     |
| Chunk standalone?         | N/A                                                    | Yes — each chunk decodes independently                 |
| Recommended for           | Short clips (voice notes, snippets)                    | Long recordings (meetings, lectures, podcasts)         |

## Donation

If you enjoy my work and find it useful, feel free to invite me to a cup of coffee :)

[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://www.buymeacoffee.com/tchvu3)

## License

This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.

### Credit

Thanks to independo-gmbh for the readme update.
