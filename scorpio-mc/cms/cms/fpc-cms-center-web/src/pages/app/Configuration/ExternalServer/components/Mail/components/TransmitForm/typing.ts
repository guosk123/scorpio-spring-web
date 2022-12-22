import type { EReceiverType } from '../../../../typings';

export interface IReceiverContent {
  mailTitle: string;
  receiver: string;
  cc: string;
  bcc: string;
}
export interface IMailTransmitForm {
  id?: string;
  name: string;
  receiverContent: string;
  receiverType: EReceiverType;
}
