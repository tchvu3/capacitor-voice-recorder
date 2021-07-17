import { WebPlugin } from '@capacitor/core';

import type { VoiceRecorderPlugin } from './definitions';

export class VoiceRecorderWeb extends WebPlugin implements VoiceRecorderPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
