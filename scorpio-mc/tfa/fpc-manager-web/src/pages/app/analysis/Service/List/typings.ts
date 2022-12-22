export enum ThresholdConfigType {
  'NotExist' = '0',
  'MoreThan' = '1',
  'LessThan' = '2',
}
export const ThresholdConfig_Enum = {
  [ThresholdConfigType.NotExist]: '不存在',
  [ThresholdConfigType.MoreThan]: '大于',
  [ThresholdConfigType.LessThan]: '小于',
};
export const ThresholdConfigOptions = [
  {
    label: ThresholdConfig_Enum[ThresholdConfigType.NotExist],
    value: ThresholdConfigType.NotExist,
  },
  {
    label: ThresholdConfig_Enum[ThresholdConfigType.MoreThan],
    value: ThresholdConfigType.MoreThan,
  },
  {
    label: ThresholdConfig_Enum[ThresholdConfigType.LessThan],
    value: ThresholdConfigType.LessThan,
  },
];
export enum PercentageRateType {
  'SupportedLinkSuccessRate' = '0',
  'ClientReTransmitRate' = '1',
  'SeverReTransmitRate' = '2',
}
export const Percentage_Enum = {
  [PercentageRateType.SupportedLinkSuccessRate]: '连接成功率',
  [PercentageRateType.ClientReTransmitRate]: '客户端重传率',
  [PercentageRateType.SeverReTransmitRate]: '服务端重传率',
};
export const percentageRateTypeOptions = [
  {
    label: Percentage_Enum[PercentageRateType.SupportedLinkSuccessRate],
    value: PercentageRateType.SupportedLinkSuccessRate,
  },
  {
    label: Percentage_Enum[PercentageRateType.ClientReTransmitRate],
    value: PercentageRateType.ClientReTransmitRate,
  },
  {
    label: Percentage_Enum[PercentageRateType.SeverReTransmitRate],
    value: PercentageRateType.SeverReTransmitRate,
  },
];

export enum ChartTrendType {
  'FlowRateTrend' = '0',
  'AlarmDistributionTrend' = '1',
}
export const Trend_Enum = {
  [ChartTrendType.FlowRateTrend]: '流量分布',
  [ChartTrendType.AlarmDistributionTrend]: '告警分布图',
};
export const chartTrendTypeOptions = [
  {
    label: Trend_Enum[ChartTrendType.FlowRateTrend],
    value: ChartTrendType.FlowRateTrend,
  },
  {
    label: Trend_Enum[ChartTrendType.AlarmDistributionTrend],
    value: ChartTrendType.AlarmDistributionTrend,
  },
];

export enum FieldValue {
  /** 数值 */
  'COUNT' = 'count',
  /** Bytes每秒 */
  'BYTE_PS' = 'bytePs',
  /** 字节 */
  'BYTE' = 'byte',
  /** 时延 */
  'TIME_DELAY' = 'timeDelay',
  /** 比率 */
  'RATE' = 'rate',
}

export const FieldValueUnitMap = {
  [FieldValue.COUNT]: '',
  [FieldValue.BYTE_PS]: 'bps',
  [FieldValue.BYTE]: 'bytes',
  [FieldValue.TIME_DELAY]: 'ms',
  [FieldValue.RATE]: '%',
};

export interface IField {
  /** 字段 */
  key: string;
  /** 字段名称 */
  label: string;
  /** 字段值类型 */
  valueType: FieldValue;
  /** 是否默认显示 */
  isDefault?: boolean;
}
/** 所有指标字段的集合 */

export const fieldList: IField[] = [
  // 带宽统计
  {
    key: 'totalBytes',
    label: '流量大小',
    valueType: FieldValue.BYTE,
    // isDefault: true
  },
  { key: 'bytepsAvg', label: '平均带宽', valueType: FieldValue.BYTE_PS },
  { key: 'bytepsPeak', label: '峰值带宽', valueType: FieldValue.BYTE_PS },
  { key: 'downstreamBytes', label: '下行流量', valueType: FieldValue.BYTE },
  { key: 'upstreamBytes', label: '上行流量', valueType: FieldValue.BYTE },
  // 包数统计
  { key: 'totalPackets', label: '总数据包', valueType: FieldValue.COUNT },
  { key: 'downstreamPackets', label: '下行数据包', valueType: FieldValue.COUNT },
  { key: 'upstreamPackets', label: '上行数据包', valueType: FieldValue.COUNT },
  // 会话统计
  { key: 'activeSessions', label: '活动会话数', valueType: FieldValue.COUNT },
  {
    key: 'concurrentSessions',
    label: '最大并发会话数',
    valueType: FieldValue.COUNT,
    // isDefault: true,
  },
  {
    key: 'establishedSessions',
    label: '新建会话数',
    valueType: FieldValue.COUNT,
    // isDefault: true
  },
  { key: 'destroyedSessions', label: '销毁会话数', valueType: FieldValue.COUNT },
  { key: 'establishedTcpSessions', label: 'TCP新建会话数', valueType: FieldValue.COUNT },
  { key: 'establishedUdpSessions', label: 'UDP新建会话数', valueType: FieldValue.COUNT },
  { key: 'establishedIcmpSessions', label: 'ICMP新建会话数', valueType: FieldValue.COUNT },
  { key: 'establishedOtherSessions', label: '其他协议新建会话数', valueType: FieldValue.COUNT },
  { key: 'establishedUpstreamSessions', label: '上行新建会话数', valueType: FieldValue.COUNT },
  { key: 'establishedDownstreamSessions', label: '下行新建会话数', valueType: FieldValue.COUNT },
  // 分片数据包统计
  { key: 'fragmentTotalBytes', label: '分片流量大小', valueType: FieldValue.BYTE },
  { key: 'fragmentTotalPackets', label: '分片数据包数', valueType: FieldValue.COUNT },
  // TCP相关统计
  { key: 'tcpSynPackets', label: 'TCP同步包数', valueType: FieldValue.COUNT },
  { key: 'tcpClientSynPackets', label: 'TCP客户端syn包数', valueType: FieldValue.COUNT },
  { key: 'tcpServerSynPackets', label: 'TCP服务端syn包数', valueType: FieldValue.COUNT },
  { key: 'tcpSynAckPackets', label: 'TCP同步确认包', valueType: FieldValue.COUNT },
  { key: 'tcpSynRstPackets', label: 'TCP同步重置包', valueType: FieldValue.COUNT },
  { key: 'tcpEstablishedFailCounts', label: 'TCP建连失败数', valueType: FieldValue.COUNT },
  { key: 'tcpEstablishedSuccessCounts', label: 'TCP建连成功数', valueType: FieldValue.COUNT },
  { key: 'tcpEstablishedSuccessRate', label: 'TCP连接成功率', valueType: FieldValue.RATE },
  { key: 'tcpEstablishedTimeAvg', label: 'TCP连接平均响应时间', valueType: FieldValue.TIME_DELAY },
  { key: 'tcpZeroWindowPackets', label: 'TCP零窗口包数', valueType: FieldValue.COUNT },
  {
    key: 'tcpClientNetworkLatencyAvg',
    label: '客户端网络平均时延',
    valueType: FieldValue.TIME_DELAY,
    // isDefault: true,
  },
  {
    key: 'tcpServerNetworkLatencyAvg',
    label: '服务器网络平均时延',
    valueType: FieldValue.TIME_DELAY,
  },
  {
    key: 'serverResponseLatencyAvg',
    label: '服务器响应平均时延',
    valueType: FieldValue.TIME_DELAY,
  },
  { key: 'serverResponseFastCounts', label: '服务器迅速响应个数', valueType: FieldValue.COUNT },
  { key: 'serverResponseNormalCounts', label: '服务器正常响应个数', valueType: FieldValue.COUNT },
  { key: 'serverResponseTimeoutCounts', label: '服务器超时响应个数', valueType: FieldValue.COUNT },
  {
    key: 'serverResponseLatencyPeak',
    label: '服务器响应时间峰值',
    valueType: FieldValue.TIME_DELAY,
  },
  {
    key: 'tcpClientRetransmissionPackets',
    label: 'TCP客户端重传包数',
    valueType: FieldValue.COUNT,
  },
  { key: 'tcpClientPackets', label: 'TCP客户端总包数', valueType: FieldValue.COUNT },
  {
    key: 'tcpServerRetransmissionPackets',
    label: 'TCP服务端重传包数',
    valueType: FieldValue.COUNT,
  },
  { key: 'tcpServerPackets', label: 'TCP服务端总包数', valueType: FieldValue.COUNT },
  { key: 'tcpClientZeroWindowPackets', label: '客户端零窗口包数', valueType: FieldValue.COUNT },
  { key: 'tcpServerZeroWindowPackets', label: '服务端零窗口包数', valueType: FieldValue.COUNT },

  { key: 'uniqueIpCounts', label: '独立用户数', valueType: FieldValue.COUNT },
];

export interface BusinessPanelSettings {
  parameters: Record<string, number>;
  percent_parameter: string | undefined;
  time_window_parameter: string | undefined;
}
