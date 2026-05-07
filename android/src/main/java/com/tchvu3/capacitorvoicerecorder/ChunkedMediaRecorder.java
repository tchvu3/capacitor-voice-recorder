package com.tchvu3.capacitorvoicerecorder;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Records audio in fixed-duration chunks. Each chunk is captured by a fresh
 * MediaRecorder into its own file, so chunks are self-contained standalone
 * AAC files (not continuations of a stream) and can be transcribed independently.
 */
public class ChunkedMediaRecorder implements AudioManager.OnAudioFocusChangeListener {

    private final Context context;
    private final RecordOptions options;
    private final int chunkIntervalMs;
    private final VoiceRecorder voiceRecorder;
    private final Handler chunkHandler;
    private final AudioManager audioManager;

    private MediaRecorder mediaRecorder;
    private File currentChunkFile;
    private Runnable chunkRunnable;
    private AudioFocusRequest audioFocusRequest;
    private Runnable onInterruptionBegan;
    private Runnable onInterruptionEnded;
    private CurrentRecordingStatus status = CurrentRecordingStatus.NONE;
    private int chunkIndex = 0;

    public ChunkedMediaRecorder(Context context, RecordOptions options, int chunkIntervalMs, VoiceRecorder voiceRecorder) {
        this.context = context;
        this.options = options;
        this.chunkIntervalMs = chunkIntervalMs;
        this.voiceRecorder = voiceRecorder;
        this.chunkHandler = new Handler(Looper.getMainLooper());
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void setOnInterruptionBegan(Runnable callback) {
        this.onInterruptionBegan = callback;
    }

    public void setOnInterruptionEnded(Runnable callback) {
        this.onInterruptionEnded = callback;
    }

    public void startRecording() throws IOException {
        if (status == CurrentRecordingStatus.RECORDING) {
            return;
        }

        requestAudioFocus();
        chunkIndex = 0;
        createAndStartNewRecorder();
        status = CurrentRecordingStatus.RECORDING;
        scheduleNextChunk();
    }

    public void stopRecording() {
        if (status == CurrentRecordingStatus.NONE) {
            return;
        }

        cancelScheduledChunk();

        // Stop the current recorder and emit what it captured as the final chunk.
        File finishedFile = currentChunkFile;
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
            }
        } catch (Exception ignore) {
            // MediaRecorder.stop() can throw if no frames were written; we still want to try to emit.
        } finally {
            mediaRecorder = null;
        }

        emitChunk(finishedFile, true);

        abandonAudioFocus();
        currentChunkFile = null;
        status = CurrentRecordingStatus.NONE;
    }

    public boolean pauseRecording() throws NotSupportedOsVersion {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            throw new NotSupportedOsVersion();
        }
        if (status == CurrentRecordingStatus.RECORDING && mediaRecorder != null) {
            cancelScheduledChunk();
            mediaRecorder.pause();
            status = CurrentRecordingStatus.PAUSED;
            return true;
        }
        return false;
    }

    public boolean resumeRecording() throws NotSupportedOsVersion {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            throw new NotSupportedOsVersion();
        }
        if (status == CurrentRecordingStatus.PAUSED && mediaRecorder != null) {
            requestAudioFocus();
            mediaRecorder.resume();
            status = CurrentRecordingStatus.RECORDING;
            scheduleNextChunk();
            return true;
        }
        if (status == CurrentRecordingStatus.INTERRUPTED) {
            // After an interruption we already stopped the recorder; start fresh.
            requestAudioFocus();
            try {
                createAndStartNewRecorder();
                status = CurrentRecordingStatus.RECORDING;
                scheduleNextChunk();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    public CurrentRecordingStatus getCurrentStatus() {
        return status;
    }

    // --- internal ---

    private void createAndStartNewRecorder() throws IOException {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(96000);
        mediaRecorder.setAudioSamplingRate(44100);

        File outputDir = resolveOutputDir();
        currentChunkFile = File.createTempFile(
            String.format("chunked-recording-%d-%d", chunkIndex, System.currentTimeMillis()),
            ".aac",
            outputDir
        );
        if (options.getDirectory() == null) {
            currentChunkFile.deleteOnExit();
        }
        mediaRecorder.setOutputFile(currentChunkFile.getAbsolutePath());
        mediaRecorder.prepare();
        mediaRecorder.start();
    }

    private File resolveOutputDir() {
        File outputDir = context.getCacheDir();
        String directory = options.getDirectory();
        String subDirectory = options.getSubDirectory();

        if (directory != null) {
            File resolved = getDirectory(directory);
            if (resolved != null) {
                outputDir = resolved;
            }
            if (subDirectory != null) {
                Pattern pattern = Pattern.compile("^/?(.+[^/])/?$");
                Matcher matcher = pattern.matcher(subDirectory);
                if (matcher.matches()) {
                    options.setSubDirectory(matcher.group(1));
                    outputDir = new File(outputDir, matcher.group(1));
                    if (!outputDir.exists()) {
                        outputDir.mkdirs();
                    }
                }
            }
        }
        return outputDir;
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

    private void scheduleNextChunk() {
        cancelScheduledChunk();
        chunkRunnable = new Runnable() {
            @Override
            public void run() {
                if (status != CurrentRecordingStatus.RECORDING) {
                    return;
                }
                rotateChunk();
                scheduleNextChunk();
            }
        };
        chunkHandler.postDelayed(chunkRunnable, chunkIntervalMs);
    }

    private void cancelScheduledChunk() {
        if (chunkRunnable != null) {
            chunkHandler.removeCallbacks(chunkRunnable);
            chunkRunnable = null;
        }
    }

    /**
     * Stops the current recorder, emits the captured file as a non-final chunk,
     * then immediately starts a new recorder writing to a new file.
     */
    private void rotateChunk() {
        File finishedFile = currentChunkFile;
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }
        } catch (Exception ignore) {
            // If stop() throws (e.g. too-short chunk), skip emitting this chunk.
            mediaRecorder = null;
            finishedFile = null;
        }

        if (finishedFile != null) {
            emitChunk(finishedFile, false);
        }

        try {
            createAndStartNewRecorder();
        } catch (IOException e) {
            status = CurrentRecordingStatus.NONE;
        }
    }

    private void emitChunk(File file, boolean isFinalChunk) {
        if (file == null || !file.exists()) {
            return;
        }
        int duration = getMsDurationOfAudioFile(file.getAbsolutePath());
        if (duration <= 0 && !isFinalChunk) {
            file.delete();
            return;
        }
        String base64 = readFileAsBase64(file);
        if (base64 == null) {
            file.delete();
            return;
        }

        AudioChunk chunk = new AudioChunk(
            base64,
            Math.max(duration, 0),
            "audio/aac",
            chunkIndex,
            isFinalChunk
        );
        voiceRecorder.emitAudioChunk(chunk);
        chunkIndex++;

        // Reclaim disk — the JS side now holds the base64.
        file.delete();
    }

    private String readFileAsBase64(File file) {
        byte[] data = new byte[(int) file.length()];
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            int read = 0;
            while (read < data.length) {
                int n = bis.read(data, read, data.length - read);
                if (n < 0) break;
                read += n;
            }
        } catch (IOException e) {
            return null;
        }
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    private int getMsDurationOfAudioFile(String path) {
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(path);
            player.prepare();
            return player.getDuration();
        } catch (Exception e) {
            return -1;
        } finally {
            try {
                player.release();
            } catch (Exception ignore) {}
        }
    }

    // --- audio focus / interruption ---

    private void requestAudioFocus() {
        if (audioManager == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attrs)
                .setOnAudioFocusChangeListener(this)
                .build();
            audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    private void abandonAudioFocus() {
        if (audioManager == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
            audioFocusRequest = null;
        } else {
            audioManager.abandonAudioFocus(this);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (status == CurrentRecordingStatus.RECORDING) {
                    cancelScheduledChunk();
                    File finishedFile = currentChunkFile;
                    try {
                        if (mediaRecorder != null) {
                            mediaRecorder.stop();
                            mediaRecorder.release();
                        }
                    } catch (Exception ignore) {
                    } finally {
                        mediaRecorder = null;
                    }
                    // Emit what was captured so audio captured before the interruption isn't lost.
                    emitChunk(finishedFile, false);
                    currentChunkFile = null;
                    status = CurrentRecordingStatus.INTERRUPTED;
                    if (onInterruptionBegan != null) {
                        onInterruptionBegan.run();
                    }
                }
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if (status == CurrentRecordingStatus.INTERRUPTED && onInterruptionEnded != null) {
                    onInterruptionEnded.run();
                }
                break;
        }
    }
}
