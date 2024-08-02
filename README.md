<p align="center">
  <img src="https://user-images.githubusercontent.com/236501/85893648-1c92e880-b7a8-11ea-926d-95355b8175c7.png" width="128" height="128" />
</p>
<h3 align="center">Capacitor Voice Recorder</h3>
<p align="center"><strong><code>@independo/capacitor-voice-recorder</code></strong></p>
<p align="center">Capacitor plugin for audio recording</p>

<p align="center">
  <img src="https://img.shields.io/maintenance/yes/2024" />
  <a href="https://www.npmjs.com/package/@independo/capacitor-voice-recorder"><img src="https://img.shields.io/npm/l/@independo/capacitor-voice-recorder" /></a>
<br>
  <a href="https://www.npmjs.com/package/@independo/capacitor-voice-recorder"><img src="https://img.shields.io/npm/dw/@independo/capacitor-voice-recorder" /></a>
  <a href="https://www.npmjs.com/package/@independo/capacitor-voice-recorder"><img src="https://img.shields.io/npm/v/@independo/capacitor-voice-recorder" /></a>
</p>

## Installation

```
npm install --save @independo/capacitor-voice-recorder
npx cap sync
```

#### Using with iOS

Add the following to your `Info.plist`:

```xml
<key>NSMicrophoneUsageDescription</key>
<string>This app uses the microphone to record audio.</string>
```

## Configuration

No configuration required for this plugin.

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

## Overview

The `@independo/capacitor-voice-recorder` plugin allows you to record audio on Android, iOS, and Web platforms. Below is a summary
of the key methods and how to use them.

### Checking Device Capabilities and Permissions

#### canDeviceVoiceRecord

Checks if the device/browser can record audio.

```typescript
VoiceRecorder.canDeviceVoiceRecord().then((result: GenericResponse) => console.log(result.value));
```

| Return Value       | Description                                                                            |
|--------------------|----------------------------------------------------------------------------------------|
| `{ value: true }`  | The device/browser can record audio.                                                   |
| `{ value: false }` | The browser cannot record audio. Note: On mobile, it always returns `{ value: true }`. |

#### requestAudioRecordingPermission

Requests audio recording permission from the user.

```typescript
VoiceRecorder.requestAudioRecordingPermission().then((result: GenericResponse) => console.log(result.value));
```

| Return Value       | Description         |
|--------------------|---------------------|
| `{ value: true }`  | Permission granted. |
| `{ value: false }` | Permission denied.  |

#### hasAudioRecordingPermission

Checks if the audio recording permission has been granted.

```typescript
VoiceRecorder.hasAudioRecordingPermission().then((result: GenericResponse) => console.log(result.value));
```

| Return Value                        | Description                        |
|-------------------------------------|------------------------------------|
| `{ value: true }`                   | Permission granted.                |
| `{ value: false }`                  | Permission denied.                 |
| Error Code                          | Description                        |
| `COULD_NOT_QUERY_PERMISSION_STATUS` | Failed to query permission status. |

### Managing Recording

#### startRecording

Starts the audio recording.

```typescript
VoiceRecorder.startRecording()
    .then((result: GenericResponse) => console.log(result.value))
    .catch(error => console.log(error));
```

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

| Error Code                  | Description                                          |
|-----------------------------|------------------------------------------------------|
| `RECORDING_HAS_NOT_STARTED` | No recording in progress.                            |
| `EMPTY_RECORDING`           | Recording stopped immediately after starting.        |
| `FAILED_TO_FETCH_RECORDING` | Unknown error occurred while fetching the recording. |

#### pauseRecording

Pauses the ongoing audio recording.

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

Resumes a paused audio recording.

```typescript
VoiceRecorder.resumeRecording()
    .then((result: GenericResponse) => console.log(result.value))
    .catch(error => console.log(error));
```

| Return Value       | Description                     |
|--------------------|---------------------------------|
| `{ value: true }`  | Recording resumed successfully. |
| `{ value: false }` | Recording is already running.   |

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

| Status Code | Description                                          |
|-------------|------------------------------------------------------|
| `NONE`      | Plugin is idle and waiting to start a new recording. |
| `RECORDING` | Plugin is currently recording.                       |
| `PAUSED`    | Recording is paused.                                 |

## Format and Mime type

The plugin will return the recording in one of several possible formats.
the format is dependent on the os / web browser that the user uses.
on android and ios the mime type will be `audio/aac`, while on chrome and firefox it
will be `audio/webm;codecs=opus` and on safari it will be `audio/mp4`.
note that these 3 browsers has been tested on. the plugin should still work on
other browsers, as there is a list of mime types that the plugin checks against the
user's browser.

Note that this fact might cause unexpected behavior in case you'll try to play recordings
between several devices or browsers - as they not all support the same set of audio formats.
it is recommended to convert the recordings to a format that all your target devices supports.
as this plugin focuses on the recording aspect, it does not provide any conversion between formats.

## Playback

To play the recorded file you can use plain javascript:

```typescript
const base64Sound = '...' // from plugin
const mimeType = '...'  // from plugin
const audioRef = new Audio(`data:${mimeType};base64,${base64Sound}`)
audioRef.oncanplaythrough = () => audioRef.play()
audioRef.load()
```

## Collaborators

| Collaborators      |                                                             | GitHub                                    | Donation                                                                                                                          |
|--------------------|-------------------------------------------------------------|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| Avihu Harush       | Original Author                                             | [tchvu3](https://github.com/tchvu3)       | [!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://www.buymeacoffee.com/tchvu3) |
| Konstantin Strümpf | Contributor for [Independo GmbH](https://www.independo.app) | [kstruempf](https://github.com/kstruempf) |                                                                                                                                   |

