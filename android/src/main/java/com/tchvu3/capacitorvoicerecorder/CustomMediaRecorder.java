package com.tchvu3.capacitorvoicerecorder;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomMediaRecorder {

    private final Context context;
    private final RecordOptions options;
    private MediaRecorder mediaRecorder;
    private File outputFile;
    private CurrentRecordingStatus currentRecordingStatus = CurrentRecordingStatus.NONE;

    public CustomMediaRecorder(Context context, RecordOptions options) throws IOException {
        this.context = context;
        this.options = options;
        generateMediaRecorder();
    }

    private void generateMediaRecorder() throws IOException {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(96000);
        mediaRecorder.setAudioSamplingRate(44100);
        setRecorderOutputFile();
        mediaRecorder.prepare();
    }

    private void setRecorderOutputFile() throws IOException {
        File outputDir = context.getCacheDir();
        String directory = options.getDirectory();
        String subDirectory = options.getSubDirectory();

        if (directory != null) {
            outputDir = this.getDirectory(directory);
            if (subDirectory != null) {
                Pattern pattern = Pattern.compile("^/?(.+[^/])/?$");
                Matcher matcher = pattern.matcher(subDirectory);
                if (matcher.matches()) {
                    outputDir = new File(outputDir, matcher.group(1));
                    if (!outputDir.exists()) {
                        outputDir.mkdirs();
                    }
                }
            }
        }

        outputFile = File.createTempFile(String.format("recording-%d", System.currentTimeMillis()), ".aac", outputDir);

        if (directory == null) {
            outputFile.deleteOnExit();
        }

        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
    }

    private File getDirectory(String directory) {
        return switch (directory) {
            case "DOCUMENTS" -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            case "DATA", "LIBRARY" -> context.getFilesDir();
            case "CACHE" -> context.getCacheDir();
            case "EXTERNAL" -> context.getExternalFilesDir(null);
            case "EXTERNAL_STORAGE" -> Environment.getExternalStorageDirectory();
            default -> null;
        };
    }

    public void startRecording() {
        mediaRecorder.start();
        currentRecordingStatus = CurrentRecordingStatus.RECORDING;
    }

    public void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        currentRecordingStatus = CurrentRecordingStatus.NONE;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public RecordOptions getRecordOptions() {
        return options;
    }

    public boolean pauseRecording() throws NotSupportedOsVersion {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            throw new NotSupportedOsVersion();
        }

        if (currentRecordingStatus == CurrentRecordingStatus.RECORDING) {
            mediaRecorder.pause();
            currentRecordingStatus = CurrentRecordingStatus.PAUSED;
            return true;
        } else {
            return false;
        }
    }

    public boolean resumeRecording() throws NotSupportedOsVersion {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            throw new NotSupportedOsVersion();
        }

        if (currentRecordingStatus == CurrentRecordingStatus.PAUSED) {
            mediaRecorder.resume();
            currentRecordingStatus = CurrentRecordingStatus.RECORDING;
            return true;
        } else {
            return false;
        }
    }

    public CurrentRecordingStatus getCurrentStatus() {
        return currentRecordingStatus;
    }

    public boolean deleteOutputFile() {
        return outputFile.delete();
    }

    public static boolean canPhoneCreateMediaRecorder(Context context) {
        return true;
    }

    private static boolean canPhoneCreateMediaRecorderWhileHavingPermission(Context context) {
        CustomMediaRecorder tempMediaRecorder = null;
        try {
            tempMediaRecorder = new CustomMediaRecorder(context, new RecordOptions(null, null));
            tempMediaRecorder.startRecording();
            tempMediaRecorder.stopRecording();
            return true;
        } catch (Exception exp) {
            return exp.getMessage().startsWith("stop failed");
        } finally {
            if (tempMediaRecorder != null) tempMediaRecorder.deleteOutputFile();
        }
    }
}
