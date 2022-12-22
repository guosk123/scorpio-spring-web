export enum ERTPStatus {
  /** 设备未注册 */
  UNREGISTER = 0,
  /** 设备注册 */
  REGISTER = 1,
  /** 通讯邀请 */
  INVITE = 2,
  /** 通讯中 */
  ACK = 3,
  /** 通信取消 */
  CANCEL = 4,
  /** 通信终止 */
  BYE = 5,
  /** 通信拒绝 */
  DENY = 6,
  /** 通信超时 */
  OVERTIME = 7,
  /** 其他 非法字段 */
}

export enum EPolicyLevel {
  'LOW' = 0,
  'MIDDLE' = 1,
  'HIGH' = 2,
}

export function getRTPStatusText(status: number) {
  switch (status) {
    case ERTPStatus.UNREGISTER:
      return '设备未注册';
    case ERTPStatus.REGISTER:
      return '设备注册';
    case ERTPStatus.INVITE:
      return '通讯邀请';
    case ERTPStatus.ACK:
      return '通讯中';
    case ERTPStatus.CANCEL:
      return '通信取消';
    case ERTPStatus.BYE:
      return '通信终止';
    case ERTPStatus.DENY:
      return '通信拒绝';
    case ERTPStatus.OVERTIME:
      return '通信超时';
  }
  return '非法字段';
}

export interface IRTPFlow {
  /** 发送方设备编码 */
  from: string;
  /** 发送方IP */
  srcIp: string;
  /** 发送方端口 */
  srcPort: number;
  /** 接收方设备编码 */
  to: string;
  /** 接收方IP */
  destIp: string;
  /** 接收方端口 */
  destPort: number;
  /** 传输层协议 */
  ipProtocol: string;
  /** SSRC */
  ssrc: number;
  /** 视频流状态 */
  status: ERTPStatus;
  /** 通讯邀请时间 */
  inviteTime: string;
  /** 通讯开始时间 */
  startTime: string;
  /** 通讯结束时间 */
  endTime: string;
  /** RTP总包数 */
  rtpTotalPackets: number;
  /** RTP丢包数 */
  rtpLossPackets: number;
  /** RTP丢包率 */
  rtpLossRate: number;
  /** 最大抖动 */
  jitterMax: number;
  /** 平均抖动 */
  jitterMean: number;
  /** 载荷 */
  payload: string;
  /** 邀请主叫IP */
  inviteSrcIp: string;
  /** 邀请主叫端口 */
  inviteSrcPort: number;
  /** 邀请被叫IP */
  inviteDestIp: string;
  /** 邀请被叫端口 */
  inviteDestPort: number;
  /** 邀请传输层协议 */
  inviteIpProtocol: string;
  /** 控制通道流ID */
  sipFlowId: string;
  /** RTP流id */
  flowId: string;
  /** 网络id */
  networkId: string;
  /** 业务id */
  serviceId: string;
  /** 应用id */
  applictaionId: string;
  /** 采集策略等级 */
  level: EPolicyLevel;
  /** 采集策略名称 */
  policyName: string;
}

export interface IIpDevices {
  id: string;
  /** 设备IP */
  deviceIp: string;
  /** 设备编码 */
  deviceCode: string;
  /** RTP总包数 */
  rtpTotalPackets: number;
  /** RTP丢包数 */
  rtpLossPackets: number;
  /** RTP丢包率 */
  rtpLossRate: number;
  /** 最大抖动 */
  jitterMax: number;
  /** 平均抖动 */
  jitterMean: number;
  /** 上线时间 */
  startTime: string;
  /** 更新时间 */
  reportTime: string;
}

/** IP设备分析Tab */
export enum EVideoTabType {
  VIDEO_DEVICES_LIST = 'videoDevicesList',
  RTP_FLOW_LIST = 'rtpFlowList',
  IP_GRAPH = 'ip_graph',
  SEGMENT = 'segment',
}

/** dsl字段转换 */
export const DSL_CONVERT_MAP = {
  ssrc: 'maxMerge(ssrc)',
  status: 'maxMerge(status)',
  rtp_total_packets: 'sumMerge(rtp_total_packets)',
  rtp_loss_packets: 'sumMerge(rtp_loss_packets)',
  jitter_max: 'maxMerge(jitter_max)',
  jitter_mean: 'avgMerge(jitter_mean)',
  payload: 'anyMerge(payload)',
};
