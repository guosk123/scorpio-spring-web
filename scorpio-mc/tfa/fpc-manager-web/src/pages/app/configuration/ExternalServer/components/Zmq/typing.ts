export interface IMailType {
  /** id */
  id: string;
  /** 名称 */
  name: string;
  /** zmq服务器id */
  zmqServerIpAddress: string;
  /** zmq服务器端口 */
  zmqServerPort: string;
  /** 协议 */
  protocol: string;
}
