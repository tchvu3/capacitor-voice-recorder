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
  if the previous 2 conditions are met then the recording will start
---
* stopRecording - will stop the recording that previously started. if the function startRecording()
  has not been called before this function then the promise will reject with the message "RECORDING_HAS_NOT_STARTED".
  in a case of success the recording will be returned to the user in base64 string


## Usage


```

import { Plugins } from "@capacitor/core"
const { VoiceRecorder } = Plugins

// will print true / false based on the device ability to record
VoiceRecorder.canDeviceVoiceRecord().then(result => console.log(result.value))

/** 
* will prompt the user to give the required permission, after that
* the function will print true / false based on the user response
*/
VoiceRecorder.requestAudioRecordingPermission().then(result => console.log(result.value))

// will print true / false based on the status of the recording permission
VoiceRecorder.hasAudioRecordingPermission.then(result => console.log(result.value))

/**
* In case of success the promise will resolve with {"value": true}
* (in this example "true" will be printed out)
* in case of an error the promise will reject with one of the following messages:
* "MISSING_PERMISSION", "CANNOT_RECORD_ON_THIS_PHONE" or "FAILED_TO_RECORD"
*/
VoiceRecorder.startRecording()
.then(result => console.log(result.value))
.catch(error => console.log(error))

/**
* In case of success the promise will resolve with {"data": base64FileString}, the file will be in 3gp format.
* in case of an error the promise will reject with one of the following messages:
* "RECORDING_HAS_NOT_STARTED" or "FAILED_TO_FETCH_RECORDING"
*/
VoiceRecorder.stopRecording()
.then(result => console.log(result.data))
.catch(error => console.log(error))

```