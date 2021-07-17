import { registerPlugin } from '@capacitor/core';

import type { VoiceRecorderPlugin } from './definitions';

const VoiceRecorder = registerPlugin<VoiceRecorderPlugin>('VoiceRecorder', {
  web: () => import('./web').then(m => new m.VoiceRecorderWeb()),
});

export * from './definitions';
export { VoiceRecorder };
