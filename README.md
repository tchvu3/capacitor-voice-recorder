<p align="center">
  <img src="https://user-images.githubusercontent.com/236501/85893648-1c92e880-b7a8-11ea-926d-95355b8175c7.png" width="128" height="128" />
</p>
<h3 align="center">Capacitor Voice Recorder</h3>
<p align="center"><strong><code>tchvu3/capacitor-voice-recorder</code></strong></p>
<p align="center">Capacitor plugin for simple voice recording (For Capacitor 4)</p>

<p align="center">
  <img src="https://img.shields.io/maintenance/yes/2022" />
  <a href="https://www.npmjs.com/package/capacitor-voice-recorder"><img src="https://img.shields.io/npm/l/capacitor-voice-recorder" /></a>
<br>
  <a href="https://www.npmjs.com/package/capacitor-voice-recorder"><img src="https://img.shields.io/npm/dw/capacitor-voice-recorder" /></a>
  <a href="https://www.npmjs.com/package/capacitor-voice-recorder"><img src="https://img.shields.io/npm/v/capacitor-voice-recorder" /></a>
</p>

## Maintainers

| Maintainer | GitHub |
| -----------| -------|
| Avihu Harush | [tchvu3](https://github.com/tchvu3) |

## Installation

```
npm install --save capacitor-voice-recorder
npx cap sync
```

#### ios note

Make sure to include the ```NSMicrophoneUsageDescription```
key, and a corresponding purpose string in your app's Info.plist

## Configuration

No configuration required for this plugin.

## Supported methods

| Name | Android | iOS | Web |
| :------------------------------ | :------ | :-- | :-- |
| canDeviceVoiceRecord | ✅ | ✅ | ✅ |
requestAudioRecordingPermission | ✅ | ✅ | ✅ |
| hasAudioRecordingPermission | ✅ | ✅ | ✅ |
| startRecording | ✅ | ✅ | ✅ |
| stopRecording | ✅ | ✅ | ✅ |
| pauseRecording | ✅ | ✅ | ✅ |
| resumeRecording | ✅ | ✅ | ✅ |
| getCurrentStatus | ✅ | ✅ | ✅ |

## Explanation

* canDeviceVoiceRecord - on mobile this function will always return a promise that resolves to `{ value: true }`,
  while in a browser it will be resolved to `{ value: true }` / `{ value: false }` based on the browser's ability to record.
  note that this method does not take into account the permission status,
  only if the browser itself is capable of recording at all.

---

* requestAudioRecordingPermission - if the permission has already been provided then the promise will resolve with `{ value: true }`,
  otherwise the promise will resolve to `{ value: true }` / `{ value: false }` based on the answer of the user to the request.

---

* hasAudioRecordingPermission - will resolve to `{ value: true }` / `{ value: false }` based on the status of the permission.
  please note that the web implementation of this plugin uses the Permissions API under the hood which is not widespread as of now.
  as a result, if the status of the permission cannot be checked the promise will reject with `COULD_NOT_QUERY_PERMISSION_STATUS`.
  in that case you have no choice but to use the `requestAudioRecordingPermission` function straight away or `startRecording` and capture any exception that is thrown.

---

* startRecording - if the app lacks the required permission then the promise will reject with the message `MISSING_PERMISSION`.
  if the current device cannot voice record at all (for example, due to old browser) then the promise will reject with `DEVICE_CANNOT_VOICE_RECORD`.
  if there's a recording already running then the promise will reject with `ALREADY_RECORDING`,
  and if other apps are using the microphone then the promise will reject
  with `MICROPHONE_BEING_USED`. in a case of unknown error the promise will reject with `FAILED_TO_RECORD`.

---

* stopRecording - will stop the recording that has been previously started. if the function `startRecording` has not been called beforehand
  the promise will reject with `RECORDING_HAS_NOT_STARTED`.
  in a case of unknown error the promise will reject with `FAILED_TO_FETCH_RECORDING`.
  in case of success, you will get the recording in base-64, the duration of the
  recording in milliseconds, and the mime type.

---

* pauseRecording - will pause an ongoing recording. note that if the recording has not started yet the promise
  will reject with `RECORDING_HAS_NOT_STARTED`. in case of success the promise will resolve to `{ value: true }` if the pause
  was successful or `{ value: false }` if the recording is already paused.
  note that on certain mobile os versions this function is not supported.
  in these cases the function will reject with `NOT_SUPPORTED_OS_VERSION` and your only viable options is to stop the recording instead.

---

* resumeRecording - will resume a paused recording. note that if the recording has not started yet the promise
  will reject with `RECORDING_HAS_NOT_STARTED`. in case of success the promise will resolve to `{ value: true }` if the resume
  was successful or `{ value: false }` if the recording is already running.
  note that on certain mobile os versions this function is not supported.
  in these cases the function will reject with `NOT_SUPPORTED_OS_VERSION` and your only viable options is to stop the recording instead

---

* getCurrentStatus - will let you know the current status of the current recording (if there is any at all).
  will resolve with one of the following values: `{ status: "NONE" }` if the plugin is idle and waiting to start a new recording.
  `{ status: "RECORDING" }` if the plugin is in the middle of recording and `{ status: "PAUSED" }` if the recording is paused right now.

## Usage

```

// only 'VoiceRecorder' is mandatory, the rest is for typing
import { VoiceRecorder, VoiceRecorderPlugin, RecordingData, GenericResponse, CurrentRecordingStatus } from 'capacitor-voice-recorder';

// will print true / false based on the ability of the current device (or web browser) to record audio
VoiceRecorder.canDeviceVoiceRecord().then((result: GenericResponse) => console.log(result.value))

/**
* will prompt the user to give the required permission, after that
* the function will print true / false based on the user response
*/
VoiceRecorder.requestAudioRecordingPermission().then((result: GenericResponse) => console.log(result.value))

/**
* will print true / false based on the status of the recording permission.
* the promise will reject with "COULD_NOT_QUERY_PERMISSION_STATUS"
* if the current device cannot query the current status of the recording permission
*/
VoiceRecorder.hasAudioRecordingPermission.then((result: GenericResponse) => console.log(result.value))

/**
* In case of success the promise will resolve to { value: true }
* in case of an error the promise will reject with one of the following messages:
* "MISSING_PERMISSION", "ALREADY_RECORDING", "MICROPHONE_BEING_USED", "DEVICE_CANNOT_VOICE_RECORD", or "FAILED_TO_RECORD"
*/
VoiceRecorder.startRecording()
.then((result: GenericResponse) => console.log(result.value))
.catch(error => console.log(error))

/**
* In case of success the promise will resolve to:
* {"value": { recordDataBase64: string, msDuration: number, mimeType: string }},
* the file will be in one of several possible formats (more on that later).
* in case of an error the promise will reject with one of the following messages:
* "RECORDING_HAS_NOT_STARTED" or "FAILED_TO_FETCH_RECORDING"
*/
VoiceRecorder.stopRecording()
.then((result: RecordingData) => console.log(result.value))
.catch(error => console.log(error))

/**
* will pause an ongoing recording. note that if the recording has not started yet the promise
* will reject with `RECORDING_HAS_NOT_STARTED`. in case of success the promise will resolve to `{ value: true }` if the pause
* was successful or `{ value: false }` if the recording is already paused.
* if the current mobile os does not support this method the promise will reject with `NOT_SUPPORTED_OS_VERSION`
*/
VoiceRecorder.pauseRecording()
.then((result: GenericResponse) => console.log(result.value))
.catch(error => console.log(error))

/**
* will resume a paused recording. note that if the recording has not started yet the promise
* will reject with `RECORDING_HAS_NOT_STARTED`. in case of success the promise will resolve to `{ value: true }` if the resume
* was successful or `{ value: false }` if the recording is already running.
* if the current mobile os does not support this method the promise will reject with `NOT_SUPPORTED_OS_VERSION`
*/
VoiceRecorder.resumeRecording()
.then((result: GenericResponse) => console.log(result.value))
.catch(error => console.log(error))

/**
* Will return the current status of the plugin.
* in this example one of these possible values will be printed: "NONE" / "RECORDING" / "PAUSED"
*/
VoiceRecorder.getCurrentStatus()
.then((result: CurrentRecordingStatus) => console.log(result.status))
.catch(error => console.log(error))

```

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

```
const base64Sound = '...' // from plugin
const mimeType = '...'  // from plugin
const audioRef = new Audio(`data:${mimeType};base64,${base64Sound}`)
audioRef.oncanplaythrough = () => audioRef.play()
audioRef.load()
```
