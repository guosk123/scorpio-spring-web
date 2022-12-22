import type { EReceiverType } from '../../../../typings';
import { ECertification } from '../../typing';

export interface IkafkaContent {
  /** 认证方式 */
  kerberosCertification: ECertification;
  /** IP */
  kafkaServerAddress: string;
  /** topic */
  kafkaServerTopic: string;
  /** keyTab文件路径 */
  keytabFilePath: string;
  /** key尝试恢复时间 */
  keyRestoreTime: string;
  /** sasl.kerberos.service.name */
  saslKerberosServiceName: string;
  /** sasl.kerberos.principal */
  saslKerberosPrincipal: string;
  /** 安全协议 */
  securityProtocol: string;
  /** 鉴权机制 */
  authenticationMechanism: string;
}
export interface IkafkaTransmitForm {
  id?: string;
  name: string;
  receiverContent: string;
  receiverType: EReceiverType;
}
