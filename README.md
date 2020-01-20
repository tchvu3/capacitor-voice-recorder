# capacitor-voice-recorder

Capacitor plugin for simple voice recording

## API

| Method                            | Info                                               | Platform    |
| ----------------------------------|:--------------------------------------------------:|:-----------:|
| canDeviceVoiceRecord()            | test if the phone can voice record                 | ios/android |
| requestAudioRecordingPermission() | request the user for permission                    | ios/android |
| hasAudioRecordingPermission()     | test if the user already gave permission to record | ios/android |
| startRecording()                  | start the recording                                | ios/android |
| stopRecording()                   | end the recording                                  | ios/android |

## Deep dive
#### Every function returns Promise

* canDeviceVoiceRecord - return promise that resolves to {"value": true} / {"value": false} based
  on the phones ability to record voice (will never throw exception)
---
* requestAudioRecordingPermission -  if the permission has already been provided
  then the promise will resolve to {"value": true}, otherwise the promise will resolve
  to {"value": true} / {"value": false} based on the answer of the user to the request
---
* hasAudioRecordingPermission - will resolve to {"value": true} / {"value": false} based on the
  status of the permission to voice record
---
* startRecording - if the app lacks the required permission then
  the promise will reject with the message "MISSING_PERMISSION".
  if the phone is unable to record then the promise will reject
  with the message "CANNOT_VOICE_RECORD_ON_THIS_PHONE".
  if there's a recording already running then the promise will reject with "ALREADY_RECORDING"
  if the previous 3 conditions are met then the recording will start.
  NOTE: in case of unknown error the promise will reject with "FAILED_TO_RECORD"
---
* stopRecording - will stop the recording that previously started. if the function startRecording()
  has not been called before this function then the promise will reject with the message "RECORDING_HAS_NOT_STARTED".
  in a case of success, the recording will be returned to the user in base64 string, the duration in milliseconds
  and the mime type


## Usage


```

import { Plugins } from "@capacitor/core"

// not mandatory, only for code completion
import { RecordingData, GenericResponse } from 'capacitor-voice-recorder'

// without types
const { VoiceRecorder } = Plugins

// will print true / false based on the device ability to record
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
* (in this example "true" will be printed out)
* in case of an error the promise will reject with one of the following messages:
* "MISSING_PERMISSION", "ALREADY_RECORDING", "CANNOT_RECORD_ON_THIS_PHONE" or "FAILED_TO_RECORD"
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

## ios note
make sure to include the NSMicrophoneUsageDescription key
and a corresponding purpose string in your app’s Info.plist