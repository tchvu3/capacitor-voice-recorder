package com.tchvu3.capacitorvoicerecorder;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Base64;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@CapacitorPlugin(
    name = "VoiceRecorder",
    permissions = { @Permission(alias = VoiceRecorder.RECORD_AUDIO_ALIAS, strings = { Manifest.permission.RECORD_AUDIO }) }
)
public class VoiceRecorder extends Plugin {

    static final String RECORD_AUDIO_ALIAS = "voice recording";
    private CustomMediaRecorder mediaRecorder;
    private ChunkedMediaRecorder chunkedMediaRecorder;

    @PluginMethod
    public void canDeviceVoiceRecord(PluginCall call) {
        if (CustomMediaRecorder.canPhoneCreateMediaRecorder(getContext())) {
            call.resolve(ResponseGenerator.successResponse());
        } else {
            call.resolve(ResponseGenerator.failResponse());
        }
    }

    @PluginMethod
    public void requestAudioRecordingPermission(PluginCall call) {
        if (doesUserGaveAudioRecordingPermission()) {
            call.resolve(ResponseGenerator.successResponse());
        } else {
            requestPermissionForAlias(RECORD_AUDIO_ALIAS, call, "recordAudioPermissionCallback");
        }
    }

    @PermissionCallback
    private void recordAudioPermissionCallback(PluginCall call) {
        this.hasAudioRecordingPermission(call);
    }

    @PluginMethod
    public void hasAudioRecordingPermission(PluginCall call) {
        call.resolve(ResponseGenerator.fromBoolean(doesUserGaveAudioRecordingPermission()));
    }

    @PluginMethod
    public void startRecording(PluginCall call) {
        if (!CustomMediaRecorder.canPhoneCreateMediaRecorder(getContext())) {
            call.reject(Messages.CANNOT_RECORD_ON_THIS_PHONE);
            return;
        }

        if (!doesUserGaveAudioRecordingPermission()) {
            call.reject(Messages.MISSING_PERMISSION);
            return;
        }

        if (this.isMicrophoneOccupied()) {
            call.reject(Messages.MICROPHONE_BEING_USED);
            return;
        }

        if (mediaRecorder != null) {
            call.reject(Messages.ALREADY_RECORDING);
            return;
        }

        try {
            String directory = call.getString("directory");
            String subDirectory = call.getString("subDirectory");
            RecordOptions options = new RecordOptions(directory, subDirectory);
            mediaRecorder = new CustomMediaRecorder(getContext(), options);

            // Set up interruption callbacks
            mediaRecorder.setOnInterruptionBegan(() -> {
                notifyListeners("voiceRecordingInterrupted", null);
            });

            mediaRecorder.setOnInterruptionEnded(() -> {
                notifyListeners("voiceRecordingInterruptionEnded", null);
            });

            mediaRecorder.startRecording();
            call.resolve(ResponseGenerator.successResponse());
        } catch (Exception exp) {
            mediaRecorder = null;
            call.reject(Messages.FAILED_TO_RECORD, exp);
        }
    }

    @PluginMethod
    public void stopRecording(PluginCall call) {
        if (mediaRecorder == null) {
            call.reject(Messages.RECORDING_HAS_NOT_STARTED);
            return;
        }

        try {
            mediaRecorder.stopRecording();
            File recordedFile = mediaRecorder.getOutputFile();
            RecordOptions options = mediaRecorder.getRecordOptions();

            String path = null;
            String recordDataBase64 = null;
            if (options.getDirectory() != null) {
                path = recordedFile.getName();
                if (options.getSubDirectory() != null) {
                    path = options.getSubDirectory() + "/" + path;
                }
            } else {
                recordDataBase64 = readRecordedFileAsBase64(recordedFile);
            }

            RecordData recordData = new RecordData(
                recordDataBase64,
                getMsDurationOfAudioFile(recordedFile.getAbsolutePath()),
                "audio/aac",
                path
            );
            if ((recordDataBase64 == null && path == null) || recordData.getMsDuration() < 0) {
                call.reject(Messages.EMPTY_RECORDING);
            } else {
                call.resolve(ResponseGenerator.dataResponse(recordData.toJSObject()));
            }
        } catch (Exception exp) {
            call.reject(Messages.FAILED_TO_FETCH_RECORDING, exp);
        } finally {
            RecordOptions options = mediaRecorder.getRecordOptions();
            if (options.getDirectory() == null) {
                mediaRecorder.deleteOutputFile();
            }

            mediaRecorder = null;
        }
    }

    @PluginMethod
    public void pauseRecording(PluginCall call) {
        if (mediaRecorder == null) {
            call.reject(Messages.RECORDING_HAS_NOT_STARTED);
            return;
        }
        try {
            call.resolve(ResponseGenerator.fromBoolean(mediaRecorder.pauseRecording()));
        } catch (NotSupportedOsVersion exception) {
            call.reject(Messages.NOT_SUPPORTED_OS_VERSION);
        }
    }

    @PluginMethod
    public void resumeRecording(PluginCall call) {
        if (mediaRecorder == null) {
            call.reject(Messages.RECORDING_HAS_NOT_STARTED);
            return;
        }
        try {
            call.resolve(ResponseGenerator.fromBoolean(mediaRecorder.resumeRecording()));
        } catch (NotSupportedOsVersion exception) {
            call.reject(Messages.NOT_SUPPORTED_OS_VERSION);
        }
    }

    @PluginMethod
    public void getCurrentStatus(PluginCall call) {
        if (mediaRecorder == null && chunkedMediaRecorder == null) {
            call.resolve(ResponseGenerator.statusResponse(CurrentRecordingStatus.NONE));
        } else if (mediaRecorder != null) {
            call.resolve(ResponseGenerator.statusResponse(mediaRecorder.getCurrentStatus()));
        } else {
            call.resolve(ResponseGenerator.statusResponse(chunkedMediaRecorder.getCurrentStatus()));
        }
    }

    @PluginMethod
    public void startChunkedRecording(PluginCall call) {
        if (!CustomMediaRecorder.canPhoneCreateMediaRecorder(getContext())) {
            call.reject(Messages.CANNOT_RECORD_ON_THIS_PHONE);
            return;
        }
        if (!doesUserGaveAudioRecordingPermission()) {
            call.reject(Messages.MISSING_PERMISSION);
            return;
        }
        if (this.isMicrophoneOccupied()) {
            call.reject(Messages.MICROPHONE_BEING_USED);
            return;
        }
        if (mediaRecorder != null || chunkedMediaRecorder != null) {
            call.reject(Messages.ALREADY_RECORDING);
            return;
        }

        try {
            String directory = call.getString("directory");
            String subDirectory = call.getString("subDirectory");
            int chunkIntervalMs = call.getInt("chunkIntervalMs", 30000);
            RecordOptions options = new RecordOptions(directory, subDirectory);
            chunkedMediaRecorder = new ChunkedMediaRecorder(getContext(), options, chunkIntervalMs, this);
            chunkedMediaRecorder.setOnInterruptionBegan(() -> notifyListeners("voiceRecordingInterrupted", null));
            chunkedMediaRecorder.setOnInterruptionEnded(() -> notifyListeners("voiceRecordingInterruptionEnded", null));
            chunkedMediaRecorder.startRecording();
            call.resolve(ResponseGenerator.successResponse());
        } catch (Exception exp) {
            chunkedMediaRecorder = null;
            call.reject(Messages.FAILED_TO_RECORD, exp);
        }
    }

    @PluginMethod
    public void stopChunkedRecording(PluginCall call) {
        if (chunkedMediaRecorder == null) {
            call.reject(Messages.RECORDING_HAS_NOT_STARTED);
            return;
        }
        try {
            chunkedMediaRecorder.stopRecording();
            call.resolve(ResponseGenerator.successResponse());
        } catch (Exception exp) {
            call.reject(Messages.FAILED_TO_FETCH_RECORDING, exp);
        } finally {
            chunkedMediaRecorder = null;
        }
    }

    @PluginMethod
    public void pauseChunkedRecording(PluginCall call) {
        if (chunkedMediaRecorder == null) {
            call.reject(Messages.RECORDING_HAS_NOT_STARTED);
            return;
        }
        try {
            call.resolve(ResponseGenerator.fromBoolean(chunkedMediaRecorder.pauseRecording()));
        } catch (NotSupportedOsVersion exception) {
            call.reject(Messages.NOT_SUPPORTED_OS_VERSION);
        }
    }

    @PluginMethod
    public void resumeChunkedRecording(PluginCall call) {
        if (chunkedMediaRecorder == null) {
            call.reject(Messages.RECORDING_HAS_NOT_STARTED);
            return;
        }
        try {
            call.resolve(ResponseGenerator.fromBoolean(chunkedMediaRecorder.resumeRecording()));
        } catch (NotSupportedOsVersion exception) {
            call.reject(Messages.NOT_SUPPORTED_OS_VERSION);
        }
    }

    public void emitAudioChunk(AudioChunk chunk) {
        notifyListeners("audioChunk", chunk.toJSObject());
    }

    private boolean doesUserGaveAudioRecordingPermission() {
        return getPermissionState(VoiceRecorder.RECORD_AUDIO_ALIAS).equals(PermissionState.GRANTED);
    }

    private String readRecordedFileAsBase64(File recordedFile) {
        BufferedInputStream bufferedInputStream;
        byte[] bArray = new byte[(int) recordedFile.length()];
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(recordedFile));
            bufferedInputStream.read(bArray);
            bufferedInputStream.close();
        } catch (IOException exp) {
            return null;
        }
        return Base64.encodeToString(bArray, Base64.DEFAULT);
    }

    private int getMsDurationOfAudioFile(String recordedFilePath) {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(recordedFilePath);
            mediaPlayer.prepare();
            return mediaPlayer.getDuration();
        } catch (Exception ignore) {
            return -1;
        }
    }

    private boolean isMicrophoneOccupied() {
        AudioManager audioManager = (AudioManager) this.getContext().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) return true;
        return audioManager.getMode() != AudioManager.MODE_NORMAL;
    }
}
