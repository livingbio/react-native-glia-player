import type React from 'react';
import {
  codegenNativeComponent,
  type ViewProps,
  type CodegenTypes,
} from 'react-native';
import { codegenNativeCommands } from 'react-native';

type GliaPlayerViewPageLoadedEvent = {
  result: 'success' | 'error';
};

export interface NativeProps extends ViewProps {
  slotKey?: string;
  onPageLoaded?: CodegenTypes.BubblingEventHandler<GliaPlayerViewPageLoadedEvent> | null;
}

type ComponentType = ReturnType<typeof codegenNativeComponent<NativeProps>>;

export interface NativeCommands {
  pause: (viewRef: React.ElementRef<ComponentType>) => void;
  resume: (viewRef: React.ElementRef<ComponentType>) => void;
}

export const Commands = codegenNativeCommands<NativeCommands>({
  supportedCommands: ['pause', 'resume'],
});

export default codegenNativeComponent<NativeProps>('GliaPlayerView');
