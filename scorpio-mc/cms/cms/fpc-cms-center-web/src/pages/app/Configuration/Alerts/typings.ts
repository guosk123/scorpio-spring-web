/** 告警级别枚举值 */
export const ALERT_LEVEL_ENUM = {
  '0': '提示',
  '1': '一般',
  '2': '重要',
  '3': '紧急',
};

/**
 * 告警级别
 * @description 存在4种级别，和系统告警保持一致
 */
export type AlertLevelTypes = keyof typeof ALERT_LEVEL_ENUM;

/**
 * 告警分类
 */
export enum EAlertCategory {
  /** 阈值告警 */
  THRESHOLD = 'threshold',
  /** 基线告警 */
  TREND = 'trend',
  /** 组合告警 */
  ADVANCED = 'advanced',
}

/** 告警类型枚举值 */
export const ALERT_CATEGORY_ENUM = {
  [EAlertCategory.THRESHOLD]: '阈值告警',
  [EAlertCategory.TREND]: '基线告警',
  [EAlertCategory.ADVANCED]: '组合告警',
};

/**
 * 告警规则是否启用
 */
export enum EAlertRuleStatus {
  'DISENABLE' = '0',
  'ENABLE' = '1',
}

/**
 * 告警指标数据源
 *
 * - ipAddress: IP地址
 * - hostGroup: IP地址组
 * - application: 应用
 * - geolocation: 地区
 */
export enum ESource {
  IPADDRESS = 'ipAddress',
  HOSTGROUP = 'hostGroup',
  APPLICATION = 'application',
  GEOLOCATION = 'location',
}

/** 告警指标数据源枚举值 */
export const ALTER_SOURCE_ENUM = {
  [ESource.IPADDRESS]: 'IP',
  [ESource.HOSTGROUP]: 'IP地址组',
  [ESource.APPLICATION]: '应用',
  [ESource.GEOLOCATION]: '地区',
};

/** 告警指标枚举值 */
export const ALERT_METRIC_ENUM = {
  total_bytes: '流量大小（字节）',
  total_packets: '数据包数量',
  established_sessions: '总新建会话数量',
  established_tcp_sessions: 'TCP新建会话数量',
  concurrent_tcp_sessions: 'TCP并发连接数量',
  tcp_syn_packets: 'TCP同步包数量',
  tcp_syn_ack_packets: 'TCP同步确认包数量',
  tcp_syn_rst_packets: 'TCP同步重置包数量',
  tcp_zero_window_packets: 'TCP零窗口包数量',
  tcp_established_success_counts: 'TCP建连成功次数',
  tcp_established_fail_counts: 'TCP建连失败次数',
  tcp_client_network_latency_avg: '客户端平均网络时延(ms)',
  tcp_server_network_latency_avg: '服务器平均网络时延(ms)',
  server_response_latency_avg: '服务器平均响应时延(ms)',
  long_connections: '长连接数量',
  /**
   * 只能用在网络维度下
   * @description 目前好像没有做校验，以后可能需要做校验
   */
  broadcast_packets: '广播包数',
};

/** 告警指标类型 */
export type AlertMetricTypes = keyof typeof ALERT_METRIC_ENUM;

/**
 * 告警的计算模型枚举值
 */
export const ALERT_CALCULATION_ENUM = {
  TOTAL: '计数',
  MEAN: '均值',
  MIN: '最小',
  '25PCT': '25分位数',
  MEDIAN: '中位数',
  '75PCT': '75分位数',
  '95PCT': '95分位数',
  MAX: '最大',
  STDEV: '标准差',
};

/**
 * 阈值告警的计算模型
 *
 * - 支持：计数(TOTAL)、均值(MEAN)
 */
export type AlertCalculationTypes = keyof typeof ALERT_CALCULATION_ENUM;

/**
 * 基线告警权重模型枚举值
 */
export const TREND_WEIGHTING_MODEL_ENUM = {
  MIN: '最小',
  MEAN: '均值',
  MEDIAN: '中位数',
  MAX: '最大',
};

/**
 * 基线告警的权重模型
 *
 * - 支持：均值(MEAN)、最小值(MIN)、中位数(MEDIAN)、最大值(MAX)
 */
export type TrendWeightingModelTypes = keyof typeof TREND_WEIGHTING_MODEL_ENUM;

/** 基线告警回顾窗口 */
export enum ETrendWindowingModel {
  /**
   * 相同小时天同比
   */
  HOUR_OF_DAY = 'hour_of_day',
  /**
   * 相同小时周同比
   */
  HOUR_OF_WEEK = 'hour_of_week',
  /**
   * 分钟环比
   */
  LAST_N_MINUTES = 'last_n_minutes',
  /**
   * 小时环比
   */
  LAST_N_HOURS = 'last_n_hours',
}

/**
 * 基线告警回顾窗口枚举值
 *
 * - hour_of_day 相同小时天同比
 * - hour_of_week 相同小时周同比
 * - last_n_minutes 分钟环比
 * - last_n_hours 小时环比
 */
export const TREND_WINDOWING_MODEL_ENUM = {
  [ETrendWindowingModel.HOUR_OF_DAY]: '相同小时天同比',
  [ETrendWindowingModel.HOUR_OF_WEEK]: '相同小时周同比',
  [ETrendWindowingModel.LAST_N_MINUTES]: '分钟环比',
  [ETrendWindowingModel.LAST_N_HOURS]: '小时环比',
};

/**
 * 告警周期类型
 *
 * - none 不告警
 * - once 告警一次
 * - repeatedly 周期性告警
 */
export enum EAlertRefireType {
  /** 不告警 */
  'NONE' = 'none',
  /** 告警一次 */
  'ONCE' = 'once',
  /** 周期性告警 */
  'REPEATEDLY' = 'repeatedly',
}

/**
 * 告警周期类型枚举值
 */
export const ALERT_REFIRE_TYPE_ENUM = {
  [EAlertRefireType.NONE]: '不告警',
  [EAlertRefireType.ONCE]: '只告警一次',
  [EAlertRefireType.REPEATEDLY]: '周期性告警',
};

/** 计算操作符枚举值 */
export const OPERATOR_ENUM = {
  '>': '>',
  '>=': '>=',
  '=': '=',
  '<=': '<=',
  '<': '<',
};
/**
 * 计算操作符
 */
export type OperatorTypes = keyof typeof OPERATOR_ENUM;

/**
 * 窗口时间大小枚举值
 */

export const WINDOW_SECONDS_NUMBER = {
  TIME60S: '60',
  TIME300S: '300',
  TIME600S: '600',
  TIME1800S: '1800',
  TIME3600S: '3600',
};

export const WINDOW_SECONDS_ENUM = {
  // '30': '30s',
  [WINDOW_SECONDS_NUMBER.TIME60S]: '1分钟',
  [WINDOW_SECONDS_NUMBER.TIME300S]: '5分钟',
  [WINDOW_SECONDS_NUMBER.TIME600S]: '10分钟',
  [WINDOW_SECONDS_NUMBER.TIME1800S]: '30分钟',
  [WINDOW_SECONDS_NUMBER.TIME3600S]: '1小时',
};

/**
 * 告警间隔时间枚举值
 */
export const ALERT_REFIRE_TIME_ENUM = {
  '300': '5分钟',
  '900': '15分钟',
  '1800': '30分钟',
  '3600': '1小时',
};

/**
 * 数据源
 */
export interface IAlertSource {
  // 数据源类型
  sourceType: ESource;
  // 数据源的值
  sourceValue: string;
}

/**
 * 过滤条件操作
 * - equal 等于
 * - not_equal 不等于
 */
export type FilterOperatorTypes = 'equal' | 'not_equal';

/**
 * 过滤组合的关联关系
 */
export type FilterGroupOperatorTypes = 'and' | 'any';

/**
 * 过滤组
 */
export type IFilterGroup = {
  operator: FilterGroupOperatorTypes;
  group: IFilterGroup[];
};

/**
 * 过滤的基本的等式
 */
export type IFilter = {
  operator: FilterOperatorTypes;
  alertRef: string;
};

/**
 * 组合告警的组合条件
 */
export interface IFilterCondition {
  operator: FilterGroupOperatorTypes;
  group: (IFilterGroup | IFilter)[];
}

/**
 * 告警条件
 */
export interface IFireCriteria {
  /**
   * 操作符
   */
  operator: OperatorTypes;
  /**
   * 操作数
   */
  operand: number;
  /**
   * 时间窗口大小，单位为 s
   */
  windowSeconds: number;
  /**
   * 计算方法
   */
  calculation: AlertCalculationTypes;
}

export interface IAlertMetrics {
  // 是否是比率
  isRatio: boolean;
  // 分子
  numerator: {
    // 指标
    metric: AlertMetricTypes;
    // 数据源类型
    sourceType: ESource;
    // 数据源的值
    sourceValue: string;
  };
  // 分母
  denominator: {
    // 指标
    metric: AlertMetricTypes;
    // 数据源类型
    sourceType: ESource;
    // 数据源的值
    sourceValue: string;
  };
}

/**
 * 阈值告警的设置
 */
export interface IThresholdAlertRules {
  /**
   * 指标
   */
  metrics: IAlertMetrics;

  /**
   * 数据源
   */
  source: IAlertSource;

  /**
   * 触发条件
   */
  fireCriteria: IFireCriteria;
}

export interface ITrendDefine {
  /**
   * 权重模型
   */
  weightingModel: TrendWeightingModelTypes;
  /**
   * 基线窗口
   */
  windowingModel: ETrendWindowingModel;
  /**
   * 回顾周期
   */
  windowingCount: number;
}

/**
 * 基线告警的设置
 */
export interface ITrendAlertRules {
  /**
   * 指标
   */
  metrics: IAlertMetrics;

  /**
   * 数据源
   */
  source: IAlertSource;

  /**
   * 基线定义
   */
  trend: ITrendDefine;

  /**
   * 触发条件
   */
  fireCriteria: IFireCriteria;
}

/**
 * 组合告警的设置
 */
export interface IAdvancedAlertRules {
  /**
     * 告警触发条件json
     * 
     * @eg (告警A AND !告警B) OR (告警C AND 告警D)
     * @eg
     ```json
     {
       "operator": "or",
       "group": [
         {
           "operator": "and",
           "group": [
             {
               "operator": "equal"
               "alertRef": "A",
             },
             {
               "operator": "not_equal"
               "alertRef": "B",
             },
           ]
         },
         {
           "operator": "and",
           "group": [
             {
               "operator": "equal"
               "alertRef": "C",
             },
             {
               "operator": "equal"
               "alertRef": "D",
             },
           ]
         }
       ]
     }
     ```
     */
  fireCriteria: IFilterCondition;
  // 触发周期
  windowSeconds: number;
}

/**
 * 告警触发行为
 */
export interface IAlertRefire {
  /**
   * 告警触发类型
   */
  type: EAlertRefireType;
  /**
   * 周期触发时的时间间隔
   */
  seconds: number;
}

/**
 * 告警的定义
 */
export interface IAlertRule {
  id: string;
  /** 告警规则名称 */
  name: string;
  /**
   * 告警级别
   */
  level: AlertLevelTypes;
  /**
   * 告警类型
   */
  category: EAlertCategory;
  /**
   * 数据源：告警指标数据源
   */
  alterSourceType: ESource;
  /**
   * 阈值告警
   */
  thresholdSettings: IThresholdAlertRules;
  /**
   * 基线告警
   */
  trendSettings: ITrendAlertRules;
  /**
   * 组合告警
   */
  advancedSettings: IAdvancedAlertRules;
  /**
   * 告警触发行为
   */
  refire: IAlertRefire;
  /**
   * 描述信息
   */
  description?: string;

  /**
   * 当前告警是否启用
   */
  status: EAlertRuleStatus;
  /**
   * tcp新建会话数
   */
  establishedTcpSessions: string | number;
  /**
   * 并发会话数
   */
  concurrentSessions: string | number;
  /**
   * 作用的网络
   * @description 由于引擎端不好处理，所以这里的 ID 最多只能单选一个值
   */
  networkIds: string;
  /**
   * 作用的业务
   * @description 由于引擎端不好处理，所以这里的 ID 最多只能单选一个值
   */
  serviceIds: string;

  metric: AlertMetricTypes;
  // 解析出来的JSON信息
  source: IAlertSource;
  fireCriteria: IFireCriteria;
  trendDefine: ITrendDefine;
}

export interface IAlertMessageComponent {
  // 子告警ID
  alertId: string;
  // 完整的告警定义
  alertDefine: IAlertRule;

  // 告警触发的内容
  alertFireContext: {
    // 统计窗口的开始时间
    windowStartTime: string;
    // 统计窗口的结束时间
    windowEndTime: string;

    // 告警指标具体的值
    // 阈值告警的话，应该就是一个数值
    thresholdResult: number;
    // 基线值
    trendBaseline: number;
    // 基线实际的值
    trendResult: number;
    // 趋势百分比
    trendPercent: number;
  };

  // ============
  // 拼接告警信息
  // ① 阈值告警，有比率
  // 在{windowStartTime} ~ {windowEndTime} 内，{metrics.numerator}指标与{metrics.denominator}指标比率的{calculation}为{calculationResult}
  // ② 阈值告警，无比率
  // 在{windowStartTime} ~ {windowEndTime} 内，{metrics.numerator}指标的{calculation}为{calculationResult}
}

/**
 * 告警消息
 */
export interface IAlertMessage {
  id: string;
  alertId: string;
  // 可搜索的字段拿出来一份
  name: string;
  // 告警分类
  category: EAlertCategory;
  // 告警级别
  level: AlertLevelTypes;
  // 网络ID
  networkId: string;
  // 业务ID
  serviceId?: string;
  // 完整的告警定义
  alertDefine: IAlertRule;
  // 如果是组合告警，components应该是个数组，里面是每个子告警
  // 如果不是组合告警，components数组里面就一个元素，且子告警ID = 外层的告警ID
  components: IAlertMessageComponent[];
  // 告警产生时间
  ariseTime: string;
  // 是否处理
  status: string;
  // 处理结果
  reason: string;
}

/**
 * 告警外发
 */
export interface IAlertSyslog {
  id?: string;
  /**
   * 日志主机IP地址
   */
  ipAddress: string;
  /**
   * 端口
   */
  port: string;
  /**
   * 协议
   *
   * - 目前系统只支持UDP
   */
  protocol: string;
  /**
   * 状态
   *
   * - 0：开启
   * - 1：关闭
   */
  state: '0' | '1';
}

/**
 * 告警周期类型
 *
 * - none 不告警
 * - once 告警一次
 * - repeatedly 周期性告警
 */
export type AlertRefireTypeTypes = 'none' | 'once' | 'repeatedly';
