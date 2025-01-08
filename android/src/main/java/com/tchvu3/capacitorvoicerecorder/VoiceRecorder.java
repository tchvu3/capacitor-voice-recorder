package com.tchvu3.capacitorvoicerecorder;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Base64;
import android.util.Base64OutputStream;

import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@CapacitorPlugin(
    name = "VoiceRecorder",
    permissions = { @Permission(alias = VoiceRecorder.RECORD_AUDIO_ALIAS, strings = { Manifest.permission.RECORD_AUDIO }) }
)
public class VoiceRecorder extends Plugin {

    static final String RECORD_AUDIO_ALIAS = "voice recording";
    private CustomMediaRecorder mediaRecorder;

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
            mediaRecorder = new CustomMediaRecorder(getContext());
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
        String filePath = call.getString("filePath");

        if (filePath != null) {
            File dataFolder = new File(getContext().getFilesDir(), "");
            if (!dataFolder.exists()) {
              dataFolder.mkdirs();
            }

            File destinationFile = new File(dataFolder, filePath);
            File parentFolder = destinationFile.getParentFile();
            if (parentFolder != null && !parentFolder.exists()) {
              parentFolder.mkdirs();
            }

            try (FileInputStream inStream = new FileInputStream(recordedFile);
               FileOutputStream outStream = new FileOutputStream(destinationFile)) {
              byte[] buffer = new byte[1024];
              int length;
              while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
              }
              RecordData recordData = new RecordData(
                null,
                getMsDurationOfAudioFile(destinationFile.getAbsolutePath()),
                "audio/aac",
                destinationFile.getAbsolutePath()
              );
              System.out.println("Recorded file path: " + destinationFile.getAbsolutePath());
              System.out.println("Recorded file: " + recordData.getPath());
              call.resolve(ResponseGenerator.dataResponse(recordData.toJSObject()));
            } catch (IOException e) {
              call.reject(Messages.FAILED_TO_SAVE_RECORDING, e);
            }
        } else {
          RecordData recordData = new RecordData(
            readRecordedFileAsBase64(recordedFile),
            getMsDurationOfAudioFile(recordedFile.getAbsolutePath()),
            "audio/aac",
            null
          );
          if (recordData.getRecordDataBase64() == null || recordData.getMsDuration() < 0) {
            call.reject(Messages.EMPTY_RECORDING);
          } else {
            call.resolve(ResponseGenerator.dataResponse(recordData.toJSObject()));
          }
        }
      } catch (Exception exp) {
        call.reject(Messages.FAILED_TO_FETCH_RECORDING, exp);
      } finally {
        mediaRecorder.deleteOutputFile();
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
        if (mediaRecorder == null) {
            call.resolve(ResponseGenerator.statusResponse(CurrentRecordingStatus.NONE));
        } else {
            call.resolve(ResponseGenerator.statusResponse(mediaRecorder.getCurrentStatus()));
        }
    }

    private boolean doesUserGaveAudioRecordingPermission() {
        return getPermissionState(VoiceRecorder.RECORD_AUDIO_ALIAS).equals(PermissionState.GRANTED);
    }

    private String readRecordedFileAsBase64(File recordedFile) {
      try (FileInputStream fileInputStream = new FileInputStream(recordedFile);
         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         Base64OutputStream base64OutputStream = new Base64OutputStream(byteArrayOutputStream, Base64.NO_WRAP)) {

        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
          base64OutputStream.write(buffer, 0, bytesRead);
        }

        base64OutputStream.close();
        return byteArrayOutputStream.toString("UTF-8");
      } catch (IOException exp) {
        return null;
      }
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
