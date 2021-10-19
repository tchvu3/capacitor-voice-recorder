#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(VoiceRecorder, "VoiceRecorder",
           CAP_PLUGIN_METHOD(canDeviceVoiceRecord, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(requestAudioRecordingPermission, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(hasAudioRecordingPermission, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(startRecording, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(stopRecording, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(pauseRecording, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(resumeRecording, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getCurrentStatus, CAPPluginReturnPromise);
)
