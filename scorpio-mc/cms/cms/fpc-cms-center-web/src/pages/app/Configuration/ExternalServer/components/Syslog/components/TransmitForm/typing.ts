import type { EReceiverType } from '../../../../typings';

export interface IReceiverContent {
  syslogServerIpAddress: string;
  syslogServerPort: string;
  protocol: string;
  severity: string;
  facility: string;
  encodeType: string;
  separator: string;
}
export interface ISyslogTransmitForm {
  id?: string;
  name: string;
  receiverContent: string;
  receiverType: EReceiverType;
}
