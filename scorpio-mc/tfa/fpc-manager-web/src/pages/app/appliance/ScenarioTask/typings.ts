/**
 * 分析场景类型
 */
export type TScenarioTaskType =
  | 'beacon-detection'
  | 'nonstandard-protocol'
  | 'dynamic-domain'
  | 'suspicious-https'
  | 'intelligence-ip'
  | 'brute-force-ssh'
  | 'brute-force-rdp';

/**
 * 场景分析任务
 */
export interface IScenarioTask {
  name: string;
  id: string;
  state: '0' | '1' | '2' | '3';
  type: TScenarioTaskType;
  typeText: string;
  description?: string;
  analysisStartTime: string;
  analysisEndTime: string;
  executionStartTime?: string;
  executionEndTime?: string;
  executionProgress: number;
  executionTrace?: string;
  createTime?: string;
  updateTime?: string;
}

/**
 * Beacon分析结果
 */
export interface IScenarioTaskBeaconResult {
  task_id: string;
  id: string;
  src_ip: string;
  dest_ip: string;
  dest_port: number;
  protocol: string;
  period: string;
  record_total_hit: number;
}

/**
 * Brute分析结果
 */
export interface IScenarioTaskBruteResult {
  id: string;
  inner_host: string;
  start_time: string;
  end_time: number;
  record_total_hit: string;
  record_max_hit_every_1minutes: string;
  record_max_hit_every_3minutes: number;
}

/**
 * 非标协议分析结果
 */
export interface IScenarioTaskNonStandardProtocolResult {
  l7_protocol: string;
  ip_protocol: string;
  port: string;
  record_total_hit: number;
}

/**
 * 动态域名分析结果
 */
export interface IScenarioTaskDynamicDomainResult {
  inner_host: string;
  dynamic_domain: string;
  record_total_hit: number;
}

/**
 * IP情报分析结果
 */
export interface IScenarioTaskIpIntelligenceResult {
  ip_address: string;
  record_total_hit: number;
}

/**
 * 可疑https分析结果
 */
export interface IScenarioTaskSuspiciousHttpsResult {
  ja3: string;
  record_total_hit: number;
}

/**
 * 查询场景分析结果搜索条件
 */
export interface IQueryScenarioTaskResultParams {
  id: string;
  type: TScenarioTaskType;
  query?: Record<string, string>;
  sortProperty?: string; // 非必填,默认start_time,排序字段
  sortDirection?: 'desc' | 'asc'; // 非必填,默认desc,排序方向
}

/**
 * 动态域名聚合结果搜索条件
 */
export interface IQueryDynamicDomainTermsParams {
  id: string;
  type: TScenarioTaskType;
  termField: 'inner_host' | 'dynamic_domain';
}

/**
 * 自定义分析模板
 */
export interface IScenarioCustomTemplate {
  id?: string;
  name: string;
  dataSource: string;
  filterDsl: string;
  filterSpl: string;
  function: string;
  groupBy?: string;
  description?: string;
  sliceTimeInterval: number;
  avgTimeInterval: number;
}

export interface IScenarioCustomTemplateFunction {
  name: string;
  params: Record<string, any>;
}

export type ScenarioTaskResult =
  | IScenarioTaskBeaconResult[]
  | IScenarioTaskDynamicDomainResult[]
  | IScenarioTaskIpIntelligenceResult[]
  | IScenarioTaskNonStandardProtocolResult[]
  | IScenarioTaskSuspiciousHttpsResult[];
