import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-sim-cards-manager' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const SimCardsManagerModule = NativeModules.SimCardsManager
  ? NativeModules.SimCardsManager
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export type EsimConfig = {
  address: string;
  confirmationCode?: string;
  eid?: string;
  iccid?: string;
  matchingId?: string;
  oid?: string;
};

export enum EsimResultStatus {
  Unknown = 0,
  Fail = 1,
  Success = 2,
}


type SimManager = {
  getSimCards(): Promise<boolean | never>;
  setupEsim(config: EsimConfig): Promise<EsimResultStatus | never>;
  isEsimSupported(): Promise<boolean | never>;
};

export default SimCardsManagerModule as SimManager;
