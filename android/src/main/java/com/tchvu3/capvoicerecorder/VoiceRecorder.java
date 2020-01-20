package com.tchvu3.capvoicerecorder;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.util.Base64;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.tchvu3.capacitorvoicerecorder.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.tchvu3.capvoicerecorder.Messages.ALREADY_RECORDING;
import static com.tchvu3.capvoicerecorder.Messages.CANNOT_RECORD_ON_THIS_PHONE;
import static com.tchvu3.capvoicerecorder.Messages.FAILED_TO_FETCH_RECORDING;
import static com.tchvu3.capvoicerecorder.Messages.FAILED_TO_RECORD;
import static com.tchvu3.capvoicerecorder.Messages.MISSING_PERMISSION;
import static com.tchvu3.capvoicerecorder.Messages.RECORDING_HAS_NOT_STARTED;
import static com.tchvu3.capvoicerecorder.Messages.RECORD_AUDIO_REQUEST_CODE;

@NativePlugin(
        permissions = {Manifest.permission.RECORD_AUDIO},
        requestCodes = {RECORD_AUDIO_REQUEST_CODE}
)
public class VoiceRecorder extends Plugin {

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
            saveCall(call);
            pluginRequestPermission(Manifest.permission.RECORD_AUDIO, RECORD_AUDIO_REQUEST_CODE);
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
            RecordData recordData = new RecordData(readRecordedFileAsBase64(recordedFile), getMsDurationOfAudioFile(recordedFile.getAbsolutePath()));
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

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);
        PluginCall savedCall = getSavedCall();
        if (savedCall == null || requestCode != RECORD_AUDIO_REQUEST_CODE)
            return;

        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                savedCall.resolve(ResponseGenerator.failResponse());
                return;
            }
        }
        savedCall.resolve(ResponseGenerator.successResponse());
    }

    private boolean doesUserGaveAudioRecordingPermission() {
        return hasPermission(Manifest.permission.RECORD_AUDIO);
    }

    private String readRecordedFileAsBase64(File recordedFile) {
        BufferedInputStream bns;
        byte[] bArray = new byte[(int) recordedFile.length()];
        try {
            bns = new BufferedInputStream(new FileInputStream(recordedFile));
            bns.read(bArray);
            bns.close();
        } catch (IOException exp) {
            return null;
        }
        return Base64.encodeToString(bArray, Base64.DEFAULT);
    }

    private int getMsDurationOfAudioFile(String recordedFilePath) {
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(recordedFilePath);
            mp.prepare();
            return mp.getDuration();
        } catch (Exception ignore) {
            return -1;
        }
    }

}
