import type { ETHERNET_TYPE_ENUM } from '@/common/app';
import type { ConnectState } from '@/models/connect';
import type { IPacketModelState } from './model';

export enum EResponseCode {
  'Success' = 0,
  'Failed' = 1,
}

/** 过滤条件类型 */
export enum EConditionType {
  /** BPF表达式 */
  'BPF' = '0',
  /** 规则条件 */
  'TUPLE' = '1',
}

/** 查询参数 */
export interface IQueryParams {
  /** 本次查询任务的ID */
  queryId: string;
  startTime: string;
  endTime: string;
  networkId?: string | undefined;
  serviceId?: string;
  /** 过滤条件类型 */
  // conditionType: EConditionType;
  /** 规则条件 */
  tuple: string;
  /** BPF表达式 */
  bpf: string;
  /**
   * 查询数据量
   * @description 最大上限1W
   */
  limit: number;
}

export enum EIpProtocol {
  TCP = 'TCP',
  UDP = 'UDP',
  ICMP = 'ICMP',
  SCTP = 'SCTP',
  OTHER = 'OTHER',
}

/** 可进行搜索的字段 */
export type TSearchField =
  | 'ipAddress'
  | 'port'
  | 'vlanId'
  | 'applicationId'
  | 'l7ProtocolId'
  | 'macAddress'
  | 'countryId'
  | 'provinceId'
  | 'cityId';

/** 数据包列表 */
export interface IPacket {
  /** 自定义添加的 ID */
  id: string;
  /** 数据包的时间 */
  timestamp: string;
  /** 所属网络ID */
  networkId: string;
  /** 所属业务ID */
  serviceId: string;
  /** 源IP */
  ipInitiator: string;
  /** 源端口 */
  portInitiator: string | number;
  /** 目的IP */
  ipResponder: string;
  /** 目的端口 */
  portResponder: string | number;
  /** 传输层协议 */
  ipProtocol: EIpProtocol;
  /** flags当前不做为查询条件，返回是字符串，类似于SYN,ACK */
  tcpFlags: string;
  /** VLANID */
  vlanId: string;
  /** 总字节数 */
  totalBytes: string;
  /** 应用ID */
  applicationId: string;
  /** 应用层协议ID */
  l7ProtocolId: string;
  /** ETH类型 */
  ethernetType: ETHERNET_TYPE_ENUM;
  /** 目的MAC地址 */
  ethernetInitiator: string;
  /** 源MAC地址 */
  ethernetResponder: string;
  /** 源IP国家 */
  countryIdInitiator: string;
  /** 源IP省份 */
  provinceIdInitiator: string;
  /** 源IP城市 */
  cityIdInitiator: string;
  /** 目的IP国家 */
  countryIdResponder: string;
  /** 目的IP省份 */
  provinceIdResponder: string;
  /** 目的IP城市 */
  cityIdResponder: string;
}

/** 数据包列表服务器返回值 */
export interface IPacketResponse {
  code: EResponseCode;
  result: IPacket[];
  /** 失败原因 */
  msg?: string;
}

/** 数据包统计执行状态 */
export enum EPacketRefineStatus {
  /** 正在搜索 */
  'RUNNING' = 'RUNNING',
  /** 正常结束 */
  'DONE' = 'DONE',
  /** 异常结束 */
  'EXCEPTION' = 'EXCEPTION',
  /** 查询结果超出上限 */
  'PKT_COUNT_OVERFLOW' = 'PKT_COUNT_OVERFLOW',
}
/** 数据包列表执行状态 */
export enum EPacketListStatus {
  /** 正在搜索 */
  'RUNNING' = 'RUNNING',
  /** 正常结束 */
  'DONE' = 'DONE',
  /** 异常结束 */
  'EXCEPTION' = 'EXCEPTION',
  /** 查询结果超出上限 */
  'PKT_COUNT_OVERFLOW' = 'PKT_COUNT_OVERFLOW',
}

/** 数据包统计中的汇总数据 */
export interface IPacketRefineExecution {
  /** 已搜索的字节数 */
  searchBytes: number;
  /** 已搜索完成的数据包数量 */
  searchPacketCount: number;
  /** 已搜索完成的流数量 */
  searchFlowCount: number;
  /** 命中的最早数据包的时间 */
  matchMinTimestamp: number;
  /** 命中的最晚数据包的时间 */
  matchMaxTimestamp: number;
}

export type IPacketRefineAggregationKey = Record<TSearchField, string | number>;

/**  数据包统计聚合项 */
export interface IPacketRefineAggregation {
  label: string;
  type: TSearchField | 'ipProtocol';
  items: [
    {
      // 如果字段是枚举值，这里应该是个 ID
      label: string;
      value: number;
      /** 拆分数据，用于点击追加过滤条件 */
      keys: IPacketRefineAggregationKey;
    },
  ];
  /** 本项统计项的总数量 */
  total: number;
}

/** 数据包统计数据 */
export interface IPacketRefine {
  /** 查询状态 */
  status: EPacketRefineStatus;
  /** 执行时的备注信息，也可以说发生异常时的原因，这个字段前端先不用展示 */
  message: string;
  /** 数据包统计中的汇总数据 */
  execution: IPacketRefineExecution;
  /** 数据包统计聚合结果 */
  aggregations: IPacketRefineAggregation[];
}

/** 数据包聚合 服务器返回值 */
export interface IPacketRefineResponse {
  code: EResponseCode;
  result: IPacketRefine;
  /** 失败原因 */
  msg?: string;
}

export interface IPacketConnectState extends ConnectState {
  packetModel: IPacketModelState;
}
