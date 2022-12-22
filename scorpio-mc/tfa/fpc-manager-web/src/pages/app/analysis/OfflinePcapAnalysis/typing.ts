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
  taskId: string;
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
  /** 持续分析 */
  'CONTINUE' = '5',
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

/**
 *  MULTIPLE_FILES_TO_SINGLE_TASK            多文件  ——>单任务     普通分析模式
    MULTIPLE_FILES_TO_MULTIPLE_TASK          多文件  ——>多任务     普通分析多任务模式
    SINGLE_DIRECTORY_TO_SINGLE_TASK          单目录  ——>单任务     持续分析模式
    SINGLE_DIRECTORY_TO_MULTIPLE_TASK        单目录  ——>多任务     持续分析多任务模式
 */
export enum ETaskMode {
  MULTIPLE_FILES_TO_SINGLE_TASK = '普通分析模式',
  // MULTIPLE_FILES_TO_MULTIPLE_TASK = '普通分析多任务模式', // 暂不支持
  SINGLE_DIRECTORY_TO_SINGLE_TASK = '持续分析模式',
  SINGLE_DIRECTORY_TO_MULTIPLE_TASK = '持续分析多任务模式',
}

export interface IFileItem {
  fileName?: string;
  // 每个文件都需要一个完全不同的key，防止不同文件夹下的文件同名
  key?: string;
  // 需要给出当前节点是文件还是目录
  fileType?: 'DIR' | 'FILE';
  child?: IFileItem[] | any;
  filePath: string;
  disabled: boolean;
}

export enum EOfflineTabType {
  OFFLINE_PCAP_FILE_LIST = 'offlinePcapFileList',
  OFFLINE_TASK_LIST = 'offlineTaskList',
  OFFLINE_TASK_CREATE = 'offlineTaskCreate',
  OFFLINE_TASK_DETAIL = 'offlineTaskDetail',
  OFFLINE_TASK_LOG = 'offlineTaskLog',
}

export enum ESource {
  UPLOAD = 'UPLOAD',
  EXTERNAL_STORAGE = 'EXTERNAL_STORAGE',
}
