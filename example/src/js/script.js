import {VoiceRecorder} from 'capacitor-voice-recorder';

updateRecordCapabilityLabel()
updatePermissionStatusLabel()
updateOngoingRecordingStatus()

document.querySelector('#request-for-permission').addEventListener('click', () => {
    VoiceRecorder.requestAudioRecordingPermission().then(() => updatePermissionStatusLabel())
})

document.querySelector('#start-recording').addEventListener('click', () => {
    VoiceRecorder.startRecording()
        .then(result => onPromiseResolved(result, "start"))
        .catch(error => onPromiseThrown(error))
})

document.querySelector('#end-recording').addEventListener('click', () => {
    VoiceRecorder.stopRecording()
        .then(result => console.log(result.value))
        .catch(error => onPromiseThrown(error))
})

document.querySelector('#pause-recording').addEventListener('click', () => {
    VoiceRecorder.pauseRecording()
        .then(result => onPromiseResolved(result, "pause"))
        .catch(error => onPromiseThrown(error))
})

document.querySelector('#resume-recording').addEventListener('click', () => {
    VoiceRecorder.resumeRecording()
        .then(result => onPromiseResolved(result, "resume"))
        .catch(error => onPromiseThrown(error))
})

function onPromiseResolved(result, operation) {
    if (!result.value) {
        console.error(`Failed to ${operation} recording`)
    }
    updateOngoingRecordingStatus()
}

function onPromiseThrown(error) {
    console.error(error)
    updateOngoingRecordingStatus()
}

function updatePermissionStatusLabel() {
    VoiceRecorder.hasAudioRecordingPermission().then(result => {
        const permissionStatusLabel = document.querySelector('#permission-status');
        if (result.value) {
            permissionStatusLabel.textContent = 'Granted'
        } else {
            permissionStatusLabel.textContent = 'Not Granted'
        }
    });
}

function updateRecordCapabilityLabel() {
    VoiceRecorder.canDeviceVoiceRecord().then(result => {
        const capabilityStatusLabel = document.querySelector('#record-capability-status');
        if (result.value) {
            capabilityStatusLabel.textContent = 'Can'
        } else {
            capabilityStatusLabel.textContent = 'Cannot'
        }
    });
}

function updateOngoingRecordingStatus() {
    VoiceRecorder.getCurrentStatus().then(result => {
        const capabilityStatusLabel = document.querySelector('#ongoing-recording-status');
        if (result.status === 'NONE') {
            capabilityStatusLabel.textContent = 'None'
        } else if (result.status === 'RECORDING') {
            capabilityStatusLabel.textContent = 'Recording...'
        } else if (result.status === 'PAUSED') {
            capabilityStatusLabel.textContent = 'Paused'
        } else {
            capabilityStatusLabel.textContent = 'Unknown'
        }
    });
}
