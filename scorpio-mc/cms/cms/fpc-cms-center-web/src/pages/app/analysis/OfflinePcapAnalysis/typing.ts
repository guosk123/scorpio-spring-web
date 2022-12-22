import type { Effect } from 'umi';
import type { Reducer } from 'redux';
import type {
  IByteMetric,
  IBytesCategoryMetric,
  IDscpMetric,
  IExtraMetric,
  IFrameCategoryMetric,
  IFramePacketLengthMetric,
  IIpProtocolPacketMetric,
  IPacketMetric,
  IPerformanceMetric,
  ITcpMetric,
} from '../typings';
import type { ConnectState } from '@/models/connect';

export interface IOfflinePcapData {
  id: string;
  name: string;
  /** 数据开始时间 */
  packetStartTime: string;
  /** 数据结束时间 */
  packetEndTime: string;
  /**
   * 过滤开始时间
   * @description 对 packetStartTime 处理后的结果
   */
  filterStartTime: string;
  /**
   * 过滤结束时间
   * @description 对 packetEndTime 处理后的结果
   */
  filterEndTime: string;
  size: string;
  status: EPcapState;
  executionProgress: number;
  executionResult: IExecutionResult;
}

export interface IExecutionResult
  extends IExtraMetric,
    IByteMetric,
    IPacketMetric,
    IPerformanceMetric,
    ITcpMetric,
    IFramePacketLengthMetric,
    IIpProtocolPacketMetric,
    IFrameCategoryMetric,
    IBytesCategoryMetric,
    IDscpMetric {
  /** 分析失败时时的原因 */
  msg?: string;
}

export enum EPcapState {
  /** 等待分析 */
  'WAITANALYSIS' = '0',
  /** 正在分析 */
  'ANALYZING' = '1',
  /** 分析异常 */
  'EXCEPTION' = '2',
  /** 分析完成 */
  'COMPLETE' = '3',
  /** 已删除 */
  'DELETED' = '4',
}

interface IReducerType {
  state: IPcapModelState;
  action: Record<string, any>;
}

export interface IPcapModelState {
  pcapState: IPcapState;
  uploadUri: string;
}

export interface IPcapModelType {
  namespace: string;
  state: IPcapModelState;
  effects: {
    queryPcapList: Effect;
    deletePcapFile: Effect;
    queryUploadUri: Effect;
  };
  reducers: {
    savePcapList: Reducer<IReducerType>;
    saveUploadUri: Reducer<IReducerType>;
  };
}

export interface IPcapState {
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  pcapList: IOfflinePcapData[];
}

export interface IPcapConnectState extends ConnectState {
  offlinePcapModel: IPcapModelState;
}
