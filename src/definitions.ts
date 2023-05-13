export interface VoiceRecorderPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
