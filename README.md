<p align="center">
  <img src="https://user-images.githubusercontent.com/236501/85893648-1c92e880-b7a8-11ea-926d-95355b8175c7.png" width="128" height="128" />
</p>
<h3 align="center">Capacitor Voice Recorder</h3>
<p align="center"><strong><code>tchvu3/capacitor-voice-recorder</code></strong></p>
<p align="center">Capacitor plugin for simple voice recording (For Capacitor 3)</p>

<p align="center">
  <img src="https://img.shields.io/maintenance/yes/2021" />
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
| canDeviceVoiceRecord | ✅ | ✅ | ❌ | |  
requestAudioRecordingPermission | ✅ | ✅ | ❌ |  
| hasAudioRecordingPermission | ✅ | ✅ | ❌ |  
| startRecording | ✅ | ✅ | ❌ |  
| stopRecording | ✅ | ✅ | ❌ |

## Explanation

* canDeviceVoiceRecord - this function has been updated for Capacitor version 3 and will now ALWAYS return a promise that resolves to {"value": true}.
  it has not been removed completely to not break old implementations.

---

* requestAudioRecordingPermission - if the permission has already been provided then the promise will resolve with {"value": true},
  otherwise the promise will resolve to {"value": true} / {"value": false} based on the answer of the user to the request.

---

* hasAudioRecordingPermission - will resolve to {"value": true} / {"value": false} based on the status of the permission.

---

* startRecording - if the app lacks the required permission then the promise will reject with the message "MISSING_PERMISSION".
  if there's a recording already running then the promise will reject with "ALREADY_RECORDING",
  and if other apps are using the microphone then the promise will reject
  with "MICROPHONE_BEING_USED". in a case of unknown error the promise will reject with "FAILED_TO_RECORD".

---

* stopRecording - will stop the recording that has been previously started. if the function startRecording() has not been called beforehand
  the promise will reject with: "RECORDING_HAS_NOT_STARTED". in case of success, you will get the recording in base-64, the duration of the
  recording in milliseconds, and the mime type.

## Usage

```

// only 'VoiceRecorder' is mandatory, the rest is for typing
import { VoiceRecorder, VoiceRecorderPlugin, RecordingData, GenericResponse } from 'capacitor-voice-recorder';

// will ALWAYS print true (do not use this method, it has not been removed to keep old implementations from breaking)
VoiceRecorder.canDeviceVoiceRecord().then((result: GenericResponse) => console.log(result.value))

/** 
* will prompt the user to give the required permission, after that
* the function will print true / false based on the user response
*/
VoiceRecorder.requestAudioRecordingPermission().then((result: GenericResponse) => console.log(result.value))

// will print true / false based on the status of the recording permission
VoiceRecorder.hasAudioRecordingPermission.then((result: GenericResponse) => console.log(result.value))

/**
* In case of success the promise will resolve with {"value": true}
* in case of an error the promise will reject with one of the following messages:
* "MISSING_PERMISSION", "ALREADY_RECORDING", "MICROPHONE_BEING_USED" or "FAILED_TO_RECORD"
*/
VoiceRecorder.startRecording()
.then((result: GenericResponse) => console.log(result.value))
.catch(error => console.log(error))

/**
* In case of success the promise will resolve with:
* {"value": { recordDataBase64: string, msDuration: number, mimeType: string }},
* the file will be in *.acc format.
* in case of an error the promise will reject with one of the following messages:
* "RECORDING_HAS_NOT_STARTED" or "FAILED_TO_FETCH_RECORDING"
*/
VoiceRecorder.stopRecording()
.then((result: RecordingData) => console.log(result.value))
.catch(error => console.log(error))

```

## Playback

To play the recorded file you can use plain javascript:

```
const base64Sound = '...' // from plugin
const audioRef = new Audio(`data:audio/aac;base64,${base64Sound}`)
audioRef.oncanplaythrough = () => audioRef.play()
audioRef.load()
```
