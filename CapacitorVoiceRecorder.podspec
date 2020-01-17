
  Pod::Spec.new do |s|
    s.name = 'CapacitorVoiceRecorder'
    s.version = '0.0.1'
    s.summary = 'Capacitor plugin for voice recording'
    s.license = 'MIT'
    s.homepage = 'https://tchvu3@bitbucket.org/tchvu3/capacitor-voice-recorder.git'
    s.author = 'Avihu Harush'
    s.source = { :git => 'https://tchvu3@bitbucket.org/tchvu3/capacitor-voice-recorder.git', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end