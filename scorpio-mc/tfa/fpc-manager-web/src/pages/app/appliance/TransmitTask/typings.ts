export enum EIpTunnelMode {
  /** 没有隧道封装 */
  'NONE' = 'none',
  /** GRE封装 */
  'GRE' = 'gre',
  /** VXLAN封装 */
  'VXLAN' = 'vxlan',
}

export enum EVlanProcesslMode {
  /** 不处理 */
  'IGNORE' = 'ignore',
  /** 新增VLANN */
  'NEWVLAN' = 'newVlan',
  /** 更改VLANNID */
  'CHGCLANID' = 'chgVlanId',
}

export enum EVlanProcessType {
  /** 新增VLANN */
  'NEWVLAN' = 'newVlan',
  /** 替换VLANNID */
  'CHGCLANID' = 'replayValnId',
}

/** 是否计算校验和 */
export enum EIpTunnelCheckSum {
  /** 校验 */
  'YES' = '1',
  /** 不校验 */
  'NO' = '0',
}

export enum EMacChgMode {
  'CHANGE' = '1',
  'NOT_CHANGE' = '0',
}

export enum EIpChgMode {
  'CHANGE' = '1',
  'NOT_CHANGE' = '0',
}

/**
 * 隧道封装
 */
export interface IIpTunnel {
  /** 封装模式 */
  mode: EIpTunnelMode;

  params: {
    /** 源IP */
    sourceIp: string;
    /** 目的IP */
    destIp: string;
    /** 源MAC地址 */
    sourceMac?: string;
    /** 目的MAC地址 */
    destMac: string;

    /** 是否计算校验和 */
    checksum: EIpTunnelCheckSum;

    /**
     * KEY
     * - min: 0
     * - max: 4294967295
     */
    key?: number;

    /** 源端口 */
    sourcePort: number;
    /** 目的端口 */
    destPort: number;

    /**
     * vnid
     *
     * - min: 0
     * - max: 16777215
     */
    vnid: number;
  };
}

export interface IVlanProcess {
  type: 'vlanProcess';
  /** 处理模式 */
  mode: EVlanProcesslMode;
  rule: {
    /** vlan id */
    vlanId?: string;
    /** 已有VLAN头的包处理 */
    processType?: EVlanProcessType;
    /** VLAN修改 */
    vlanIdAlteration?: {
      source: string;
      target: string;
    }[];
    /** 而外vlan规则 */
    extraVlanIdRule?: '0' | '1';
  };
}

export interface IMacChg {
  type: 'macChg';
  /** 处理模式 */
  mode: EMacChgMode;
  /** 规则 */
  rule?: {
    sourceMac: string;
    destMac: string;
  };
}

export interface IIpChg {
  type: 'ipChg';
  /** 处理模式 */
  mode: EIpChgMode;
  /** 规则 */
  rule?: {
    sourceIp: string;
    destIp: string;
  };
}

/** 过滤规则条件 */
export type IFilterTuple = Record<string, any[] | any>;

/** 全流量查询任务状态 */
export enum ETransmitTaskState {
  /** 进行中 */
  'START' = '0',
  /** 已停止 */
  'STOPPED' = '1',
  /** 已完成 */
  'FINISHED' = '2',
}

/** 转发策略 */
export enum EReplayForwardAction {
  /** 先存储，再转发  */
  'STORE_REPLAY' = '0',
  /** 不存储，直接转发  */
  'NO_STORE_REPLAY' = '1',
}

/** 查询条件类型 */
export enum EFilterConditionType {
  /** 规则条件 */
  'TUPLE' = '0',
  /** BPF语法 */
  'BPF' = '1',
  /** 混合条件，上面两种条件都有 */
  'ALL' = '2',
}
/** 查询条件类型：ID和显示标签的映射表 */
export const FILTER_CONDITION_TYPE_MAP: Record<EFilterConditionType, string> = {
  [EFilterConditionType.TUPLE]: '规则条件',
  [EFilterConditionType.BPF]: 'BPF表达式',
  [EFilterConditionType.ALL]: '混合条件',
};

/** 流量导出模式 */
export enum ETransmitMode {
  /** PCAP文件 */
  'PCAP' = '0',
  /** 重放 */
  'REPLAY' = '1',
  /** PCAPNG文件 */
  'PCAPNG' = '2',
  /** 外部存储服务器 */
  'EXTERNAL_STORAGE' = '3',
  /** 内部IDS引擎检测 */
  // 'INTERNAL_IDS_ENGINE_DETECTION' = '4'
}

/** 导出模式：ID和显示标签的映射表 */
export const TASK_MODE_MAP: Record<ETransmitMode, string> = {
  [ETransmitMode.PCAP]: 'PCAP文件',
  [ETransmitMode.PCAPNG]: 'PCAPNG文件',
  [ETransmitMode.REPLAY]: '重放',
  [ETransmitMode.EXTERNAL_STORAGE]: '存储服务器',
  // [ETransmitMode.INTERNAL_IDS_ENGINE_DETECTION]:'内部IDS引擎检测'
};

/** 重放速率的单位 */
export enum EReplayRateUnit {
  'KBPS' = '0',
  'PPS' = '1',
}

/** 重放速率的单位：ID和显示标签的映射表 */
export const REPLAY_RATE_UNIT_MAP: Record<EReplayRateUnit, string> = {
  [EReplayRateUnit.KBPS]: 'Kbps',
  [EReplayRateUnit.PPS]: 'pps',
};

// -----内容过滤 S-----
/** 内容过滤 */
export enum EFilterRawType {
  'HEX' = 'hex',
  'ASCII' = 'ascii',
  'REGULAR' = 'regular',
  'CHINESE' = 'chinese',
}

/** 内容过滤类型：ID和标签映射关系 */
export const FILTER_RAW_TYPE_MAP: Record<EFilterRawType, string> = {
  [EFilterRawType.HEX]: '16进制',
  [EFilterRawType.ASCII]: 'ASCII码',
  [EFilterRawType.REGULAR]: '正则表达式',
  [EFilterRawType.CHINESE]: '中文',
};

export interface IFilterRaw {
  id?: string;
  type: EFilterRawType;
  value: string;
}
export interface IFilterRawRule {
  id: string;
  group: IFilterRaw[];
}
// -----内容过滤 E-----

/** 任务执行状态 */
export enum ETaskSeekStatus {
  /** 未启动 */
  'START' = 0,
  /** 执行中 */
  'RUNNING' = 1,
  /** 已完成 */
  'FINISHED' = 2,
}

/** 任务检索状态：ID和标签映射关系 */
export const TASK_SEEK_STATUS_MAP = {
  [ETaskSeekStatus.START]: '未启动',
  [ETaskSeekStatus.RUNNING]: '执行中',
  [ETaskSeekStatus.FINISHED]: '已完成',
};

/** 任务来源类型 */
export const TASK_SOUCE_TYPE_MAP = {
  '': '全部',
  rest: 'restApi',
  assignment: 'CMS下发',
  web: 'web端配置',
};

/** PCAP 中是否写入全量数据 */
export enum EWriteAllData {
  /** 不是全量数据 */
  'NO' = 0,
  /** 是全量数据 */
  'YES' = 1,
}

/** 任务执行统计 */
export interface IExecutionTrace {
  /** 任务检索状态 */
  seekStatus: ETaskSeekStatus;
  /** 检索耗时 */
  seekTime: number;
  /** 需要检索的总容量 */
  totalSeekBytes: number;
  /** 已检索的容量 */
  finishedSeekBytes: number;
  /** 落盘PCAP文件的数据量 */
  writeBytes: number;
  /** 落盘PCAP文件中的包数 */
  writePacketCount: number;
  /** 落盘PCAP文件中的流数 */
  writeFlowCount: number;
  /** 落盘PCAP文件数据的起始时间 */
  writeDataStartTime: number;
  /** 落盘PCAP文件数据的结束时间 */
  writeDataEndTime: number;
  /** 落盘PCAP文件中的数据是否是查询条件中的全量数据 */
  isWriteAllData: EWriteAllData;
}

/** 全流量查询任务 */
export interface ITransmitTask {
  id: string;
  name: string;
  /** 任务状态 */
  state: ETransmitTaskState;
  /** 过滤条件开始时间 */
  filterStartTime: string;
  /** 过滤条件截止时间 */
  filterEndTime: string;

  /** 网络ID */
  filterNetworkId?: string;
  /** pcapid */
  filterPacketFileId?: string;
  /** pcapName */
  filterPacketFileName?: string;

  /** 过滤条件类型 */
  filterConditionType: EFilterConditionType;
  filterConditionTypeText: string;
  filterBpf: string;

  /** 规则条件 */
  filterTuple: string;
  filterTupleJson: IFilterTuple[];

  /** 内容匹配条件 */
  filterRaw: string;
  filterRawJson: IFilterRaw[];

  /** 导出模式 */
  mode: ETransmitMode;
  modeText: string;

  /** 转发策略 */
  forwardAction: EReplayForwardAction;
  forwardActionText: string;

  /** 重放接口 */
  replayNetif: string;
  /** 重放速率 */
  replayRate: string;

  /** 重放速率的单位 */
  replayRateUnit: EReplayRateUnit;
  replayRateUnitText: string;

  /** 隧道封装 */
  ipTunnel: string;
  ipTunnelJson: IIpTunnel;

  /** Vlan Process */
  vlanProcess: string;
  vlanProcessJson: IVlanProcess;

  /** 更改mac */
  macChg: string;
  macChgJson: IMacChg;

  /** 更改ip */
  ipChg: string;
  ipChgJson: IMacChg;

  /** 重放信息 */
  replayRule: string;

  /** 任务执行开始时间 */
  executionStartTime: string;
  /** 任务执行结束时间 */
  executionEndTime: string;
  /** 执行进度百分比 */
  executionProgress: number;
  /** 任务执行统计信息 */
  executionTrace: string;
  executionTraceJson: IExecutionTrace;

  /** 任务的创建者来源 */
  source: string;
  /** 描述信息 */
  description: string;
}

/** 全流量查询来源 */
export enum ETransmitSource {
  'NETWORK' = 'network',
  'PCAPFILE' = 'pcapfile',
}

/** pcap文件类型 */
export interface IPcapFile {
  createTime: string;
  id: string;
  name: string;
  executionProgress: string;
  executionResult: string;
  filePath: string;
  operatorId: string;
  packetEndTime: string;
  packetStartTime: string;
  size: string;
  status: string;
  statusText: string;
  taskId: string;
}
