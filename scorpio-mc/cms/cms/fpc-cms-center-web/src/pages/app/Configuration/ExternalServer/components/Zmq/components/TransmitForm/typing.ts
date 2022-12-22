import type { EReceiverType } from '../../../../typings';

export interface IReceiverContent {
  zmqServerIpAddress: string;
  zmqServerPort: string;
  protocol: string;
}

export interface IZmqTransmitForm {
  id?: string;
  name: string;
  receiverContent: string;
  receiverType: EReceiverType;
}
