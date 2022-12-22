export enum EPktAnalysisDataSource {
  /** 全流量查询任务 */
  'transmit-task',
  /** 数据包 */
  'packet',
}

/**
 * 祯列表查询参数
 */
export interface IQueryPacketListParams {
  req: 'frames';
  capture: string;
  limit: number;
  filter?: string;
  skip?: number;
}

/**
 * 协议树查询参数
 */
export interface IQueryProtocolTreeParams {
  req: 'frame';
  bytes: 'yes';
  proto: 'yes';
  capture: string;
  frame: number;
  prev_frame: number;
}

/**
 * 协议树
 */
export interface IProtocolTree {
  err: '0' | '1';
  tree: IProtocolTreeNode[];
  bytes: string;
  /** 追踪流时需要携带的参数 */
  fol: [TFollowType, string][];
  ds: {
    bytes: string;
    name: string;
  }[];
}

/**
 * 协议树的节点
 */
export interface IProtocolTreeNode {
  l: string;
  h: number[];
  t: string;
  f: string;
  e: number;
  ds?: number;
  i?: number[];
  p?: number[];
  p_ds?: number;
  n: IProtocolTreeNode[];
}

/**
 * 过滤条件自动补全的条件
 */
export interface IFilterCompleteParams {
  req: 'complete';
  field: string;
}

/**
 * 自动提示返回值
 */
export interface IFilterCompleteResult {
  f: string;
  n: string;
  t: number;
}

/**
 * 查询参数：检查过滤条件是否正确
 */
export interface IFilterCheckParams {
  req: 'check';
  filter: string;
}

/**
 * 查询参数：pcap文件统计
 */
export interface IQueryIntervalParams {
  req: 'intervals';
  capture: string;
  interval?: number;
  filter?: string;
}

/** Pcap文件信息 */
export interface IPcapInfo {
  frames: number;
  duration: number;
  filename: string;
  filesize: number;
}

/**
 * 数据：pcap文件统计数据
 */
export interface IIntervalData {
  intervals: number[];
  last: number;
  frames: number;
  bytes: number;
}

/**
 * 数据：单条祯的内容
 */
export interface IFrameData {
  id?: string;
  /** 帧内容[No,Time,Source,Destination,Protocol,Length,Info] */
  c: string[];
  /** 帧序号 */
  num: number;
  /** 背景颜色 */
  bg: string;
  /** 字体颜色 */
  fg: string;
}

/**
 * 数据：shark常量信息
 */
export interface SharkColumnsData {
  name: string;
  format: string;
}
export interface ISharkStatData {
  name: string;
  tap?: string;
}

export type TFollowType = 'TCP' | 'UDP' | 'HTTP';

export interface ICustomStatTapData extends ISharkStatData {
  name_zh: string;
  follow?: TFollowType;
  filter?: string;
}

/**
 * Tap统计返回值
 */
export declare type TapType =
  | 'stats'
  | 'conv'
  | 'host'
  | 'flow'
  | 'nstat'
  | 'rtd'
  | 'srt'
  | 'eo'
  | 'voip-calls'
  | 'expert'
  | 'wlan'
  | 'fake-wlan-details'
  | 'rtp-streams'
  | 'rtp-analyse';

/** Tap统计公共的内容 */
export interface ITapResponseData {
  tap: string;
  type: TapType;
  [propName: string]: any;
}

// ----------conversations tap Start--------------

/** conversations tap */
export interface IConvsTapResponseData extends ITapResponseData {
  convs: IConvsTapData[];
  geoip: boolean;
  proto: string;
}

export interface IConvsTapData {
  saddr: string;
  daddr: string;
  rxf: number;
  rxb: number;
  txf: number;
  txb: number;
  start: number;
  stop: number;
  filter?: string;
  sport: any;
  dport: any;
  _sname?: string;
  _dname?: string;
  _name?: string;
  _packets?: any;
  _bytes: any;
  _duration: any;
  _rate_tx: any;
  _rate_rx: any;
  _filter: any;
}
// ----------conversations tap End--------------

// ----------src tap(Service Response Time) Start--------------

/** src tap(Service Response Time) */
export interface ISrcTapResponseData extends ITapResponseData {
  tables: ISrcTapTableData[];
  geoip: boolean;
}
export interface SrcTapTableRowData {
  n: string;
  idx: number;
  num: number;
  min: number;
  max: number;
  tot: number;

  [propName: string]: any;
}
export interface ISrcTapTableData {
  n: string;
  f: string;
  c: string;
  r: SrcTapTableRowData[];
}
// ----------src tap(Service Response Time) End--------------

// ----------rtd tap(Response Time Delay) Start--------------

/** rtd tap(Response Time Delay) */
export interface IRtdTapResponseData extends ITapResponseData {
  stats: IRtdStatData[];
  open_req: any; // TODO: 暂时不知道类型
}

export interface IRtdStatData {
  type: string;
  num: number;
  min: number;
  max: number;
  tot: number;
  min_frame: number;
  max_frame: number;
  open_req: number;
  disc_rsp: number;
  req_dup: number;
  rsp_dup: number;
  [propName: string]: any;
}
// ----------rtd tap(Response Time Delay) End--------------

// ----------stats tap End--------------
export interface IStatsTapResponseData extends ITapResponseData {
  stats: StatsTapRowData[];
  name: string;
}

export interface StatsTapRowData {
  name: 'string';
  count: number;
  avg: number;
  min: number;
  max: number;
  rate: number;
  perc: number;
  vals?: any; // TODO: 暂时不知道类型
  burstrate: number;
  bursttime: number;
  sub?: StatsTapRowData[];
  [propName: string]: any;
}
// ----------stats tap End--------------

// ----------nstat tap Start--------------
export interface INstatTapResponseData extends ITapResponseData {
  fields: INstatFieldData[];
  tables: INstatTableData[];
  name: string;
}

export interface INstatFieldData {
  c: string;
}
export interface INstatTableData {
  t: 'string';
  i: any[];
}

// ----------nstat tap End--------------

// ----------RTP stream tap Start--------------
export interface IRtpStreamTapResponseData extends ITapResponseData {
  streams: IRtpStreamData[];
}

export interface IRtpStreamData {
  ssrc: number;
  payload: string;
  saddr: string;
  sport: number;
  daddr: string;
  dport: number;
  pkts: number;
  max_delta: number;
  max_jitter: number;
  mean_jitter: number;
  expectednr: number;
  totalnr: number;
  problem: boolean;
  ipver: number;

  _ssrc?: string;
  _pb?: string;
  _lost?: string;
  _analyse?: string;
  _download?: string;
  _play?: string;
  _play_descr?: string;
  _filter?: string;
  [propName: string]: any;
}
// ----------RTP stream tap End--------------

// ----------flow tap Start--------------
export interface IFlowSeqTapResponseData extends ITapResponseData {
  nodes: string[];
  flows: IFlowSeqData[];
}

export interface IFlowSeqData {
  t: string;
  n: number[];
  pn: number[];
  c: string;
}
// ----------flow tap End--------------

// ----------expert tap Start--------------
export interface IExpertTapResponseData extends ITapResponseData {
  details: IExpertData[];
}

export interface IExpertData {
  f: number;
  s: 'Comment' | 'Chat' | 'Note' | 'Warning' | 'Error';
  g: string;
  m: string;
  p: string;

  [propName: string]: any;
}
// ----------expert tap End--------------

// ----------导出对象 Start--------------
export interface IExportObjectTapResponseData extends ITapResponseData {
  proto: 'HTTP' | 'SMB' | 'IMF' | 'TFTP';
  objects: IExportObjectData[];
}

export interface IExportObjectData {
  pkt: number;
  hostname: string;
  type: string;
  filename: string;
  _download: string;
  len: number;
}
// ----------导出对象 End--------------

export interface IFollowData {
  shost: string;
  sport: string;
  sbytes: number;
  chost: string;
  cport: string;
  cbytes: number;
  payloads: IFollowPayloadData[];
}

export interface IFollowPayloadData {
  n: number;
  d: string;
  s?: string;
}

export interface IHexdumpHighlight {
  tab: number;
  start: number;
  end: number;
  style: string;
}

/**
 * 帧列表时间列的类型：相对时间、绝对时间
 */
export type TFrameColTimeType = 'absolute' | 'relative';
