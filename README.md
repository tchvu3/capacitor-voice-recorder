<p align="center">
  <img src="https://user-images.githubusercontent.com/236501/85893648-1c92e880-b7a8-11ea-926d-95355b8175c7.png" width="128" height="128" />
</p>
<h3 align="center">Capacitor Voice Recorder</h3>
<p align="center"><strong><code>tchvu3/capacitor-voice-recorder</code></strong></p>
<p align="center">Capacitor plugin for simple voice recording</p>

<p align="center">
  <img src="https://img.shields.io/maintenance/yes/2024" />
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

Optional options can be used with this method to save the file in the device's filesystem and return a uri to that file instead of a base64 string.
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

When a `directory` option has been passed to the `VoiceRecorder.startRecording` method the data will include a `uri` instead of a `recordDataBase64`

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
| `uri`              | The URI to the audio file                      |

| Error Code                  | Description                                          |
|-----------------------------|------------------------------------------------------|
| `RECORDING_HAS_NOT_STARTED` | No recording in progress.                            |
| `EMPTY_RECORDING`           | Recording stopped immediately after starting.        |
| `FAILED_TO_FETCH_RECORDING` | Unknown error occurred while fetching the recording. |

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

```typescript
const base64Sound = '...' // from plugin
const mimeType = '...'  // from plugin
const audioRef = new Audio(`data:${mimeType};base64,${base64Sound}`)
audioRef.oncanplaythrough = () => audioRef.play()
audioRef.load()
```

## Compatibility

Versioning follows Capacitor versioning.
Major versions of the plugin are compatible with major versions of Capacitor.
You can find each version in its own dedicated branch.

| Plugin Version | Capacitor Version | Branch |
|----------------|-------------------|--------|
| 5.*            | 5                 | v5     |
| 6.*            | 6                 | master |

## Donation

If you enjoy my work and find it useful, feel free to invite me to a cup of coffee :)

[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://www.buymeacoffee.com/tchvu3)

## License

This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.

### Credit

Thanks to independo-gmbh for the readme update.
