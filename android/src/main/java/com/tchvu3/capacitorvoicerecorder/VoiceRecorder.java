package com.tchvu3.capacitorvoicerecorder;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
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

import static com.tchvu3.capacitorvoicerecorder.Messages.ALREADY_RECORDING;
import static com.tchvu3.capacitorvoicerecorder.Messages.CANNOT_RECORD_ON_THIS_PHONE;
import static com.tchvu3.capacitorvoicerecorder.Messages.FAILED_TO_FETCH_RECORDING;
import static com.tchvu3.capacitorvoicerecorder.Messages.FAILED_TO_RECORD;
import static com.tchvu3.capacitorvoicerecorder.Messages.MICROPHONE_BEING_USED;
import static com.tchvu3.capacitorvoicerecorder.Messages.MISSING_PERMISSION;
import static com.tchvu3.capacitorvoicerecorder.Messages.RECORDING_HAS_NOT_STARTED;

@CapacitorPlugin(
        name = "VoiceRecorder",
        permissions = {@Permission(alias = VoiceRecorder.RECORD_AUDIO_ALIAS, strings = {Manifest.permission.RECORD_AUDIO})}
)
public class VoiceRecorder extends Plugin {

    static final String RECORD_AUDIO_ALIAS = "voice recording";
    private CustomMediaRecorder mediaRecorder;

    @PluginMethod()
    public void canDeviceVoiceRecord(PluginCall call) {
        if (CustomMediaRecorder.canPhoneCreateMediaRecorder(getContext()))
            call.resolve(ResponseGenerator.successResponse());
        else
            call.resolve(ResponseGenerator.failResponse());
    }

    @PluginMethod()
    public void requestAudioRecordingPermission(PluginCall call) {
        if (doesUserGaveAudioRecordingPermission()) {
            call.resolve(ResponseGenerator.successResponse());
        } else {
            requestPermissions(call);
        }
    }

    @PermissionCallback
    private void recordAudioPermissionCallback(PluginCall call) {
        if (doesUserGaveAudioRecordingPermission()) {
            call.resolve(ResponseGenerator.successResponse());
        } else {
            call.resolve(ResponseGenerator.failResponse());
        }
    }

    @PluginMethod()
    public void hasAudioRecordingPermission(PluginCall call) {
        call.resolve(ResponseGenerator.fromBoolean(doesUserGaveAudioRecordingPermission()));
    }

    @PluginMethod()
    public void startRecording(PluginCall call) {
        if (!doesUserGaveAudioRecordingPermission()) {
            call.reject(MISSING_PERMISSION);
            return;
        }

        if (!CustomMediaRecorder.canPhoneCreateMediaRecorder(getContext())) {
            call.reject(CANNOT_RECORD_ON_THIS_PHONE);
            return;
        }

        if (this.isMicrophoneOccupied()) {
            call.reject(MICROPHONE_BEING_USED);
            return;
        }

        if (mediaRecorder != null) {
            call.reject(ALREADY_RECORDING);
            return;
        }

        try {
            mediaRecorder = new CustomMediaRecorder(getContext());
            mediaRecorder.startRecording();
            call.resolve(ResponseGenerator.successResponse());
        } catch (Exception exp) {
            call.reject(FAILED_TO_RECORD, exp);
        }
    }

    @PluginMethod()
    public void stopRecording(PluginCall call) {
        if (mediaRecorder == null) {
            call.reject(RECORDING_HAS_NOT_STARTED);
            return;
        }

        try {
            mediaRecorder.stopRecording();
            File recordedFile = mediaRecorder.getOutputFile();
            RecordData recordData = new RecordData(
                    readRecordedFileAsBase64(recordedFile),
                    getMsDurationOfAudioFile(recordedFile.getAbsolutePath()),
                    "audio/aac"
            );
            if (recordData.getRecordDataBase64() == null || recordData.getMsDuration() < 0)
                call.reject(FAILED_TO_FETCH_RECORDING);
            else
                call.resolve(ResponseGenerator.dataResponse(recordData.toJSObject()));
        } catch (Exception exp) {
            call.reject(FAILED_TO_FETCH_RECORDING, exp);
        } finally {
            mediaRecorder.deleteOutputFile();
            mediaRecorder = null;
        }
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
        if (audioManager == null)
            return true;
        return audioManager.getMode() != AudioManager.MODE_NORMAL;
    }

}