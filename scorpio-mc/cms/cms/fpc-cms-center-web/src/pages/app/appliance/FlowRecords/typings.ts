import type { Moment } from 'moment';
import type {
  ETHERNET_TYPE_ENUM,
  IP_ADDRESS_LOCALITY_ENUM,
  TCP_SESSION_STATE_ENUM,
} from '@/common/app';
import type { ESourceType } from '@/pages/app/analysis/typings';

/** 通过tabkey取得枚举值 */
export enum ETablekeyEntry {
  'analysis-tcp-retransmission-table' = '重传',
  'tcp-connection-error-table' = '建连失败',
  'long-connection-table' = '长连接',
  'npmd-flow-record-table' = '会话详单',
}

export interface IBasicParams {
  id?: string;
  sourceType?: ESourceType;
  packetFileId?: string;
  startTime?: string | Moment;
  endTime?: string | Moment;
  /** DSL 过滤条件 */
  dsl?: string;
}

/**
 * 流日志查询搜索条件
 */
export interface IQueryRecordParams extends IBasicParams {
  flowId?: string;
  sid?: number;
  sortProperty?: string;
  sortDirection?: 'desc' | 'asc';
  page?: number;
  pageSize?: number;
  tableKey?: string;
  serviceId?: string;
  networkGroupId?: string;
  columns?: string;
  queryId?: string;
}

export enum EFlag {
  'False' = 0,
  'True',
}

/** 流日志聚合条件 */
export interface IFlowStatisticsParams extends IBasicParams {
  histogramInterval: number; // 直方图统计间隔
  termFieldName?: string;
}

/** 下载流日志PCAP文件的参数 */
export interface IDownloadRecordFileParams {
  flowPacketId: string;
  startTime: string; // 毫秒时间戳
  endTime: string; // 毫秒时间戳
}

/** 每条流日志数据 */
export interface IFlowRecordData {
  /** 生成的唯一ID，防止重复ID导致表格数据展示问题 */
  id: string;
  /** 接口ID */
  interface: string;
  /** 流ID */
  flow_id: number | string;

  /** 网络ID */
  network_id: string[];
  /** 业务ID */
  service_id: string[];

  /** VLANID */
  vlan_id: string;
  /** 流日志开始时间 */
  start_time: string;
  /** 流日志上报时间 */
  report_time: string;
  /** 持续时间 */
  duration: number;
  /**
   * 流日志类型
   * @value 0 正常流
   * @value 1 超长流
   */
  flow_continued: '0' | '1';

  /** 上行字节数 */
  upstream_bytes: number;
  /** 下行字节数 */
  downstream_bytes: number;
  /** 总字节数 */
  total_bytes: number;

  /** 上行包数 */
  upstream_packets: number;
  /** 下行包数 */
  downstream_packets: number;
  /** 总包数 */
  total_packets: number;

  /** 上行payload字节数 */
  upstream_payload_bytes: number;
  /** 下行payload字节数 */
  downstream_payload_bytes: number;
  /** payload总字节数 */
  total_payload_bytes: number;
  /** 上行payload数据包 */
  upstream_payload_packets: number;
  /** 下行payload数据包 */
  downstream_payload_packets: number;
  /** payload总数据包 */
  total_payload_packets: number;

  /** 客户端网络时延 */
  tcp_client_network_latency: number;
  /** 服务器网络时延 */
  tcp_server_network_latency: number;
  /** 服务器响应时延 */
  server_response_latency: number;
  /**
   * 客户端网络时延标记
   * @deprecated 大概率是没用的字段
   */
  tcp_client_network_latency_flag: EFlag;

  /**
   * 服务端网络时延标记
   * @deprecated 大概率是没用的字段
   */
  tcp_server_network_latency_flag: EFlag;

  /**
   * 服务端响应时延标记
   * @deprecated 大概率是没用的字段
   */
  server_response_latency_flag: EFlag;

  /** 客户端TCP零窗口包数 */
  tcp_client_zero_window_packets: number;
  /** 服务器TCP零窗口包数 */
  tcp_server_zero_window_packets: number;

  /** TCP会话状态 */
  tcp_session_state: TCP_SESSION_STATE_ENUM;
  /** TCP建立连接成功标记 */
  tcp_established_success_flag: EFlag;
  /** TCP建立连接失败标记 */
  tcp_established_fail_flag: EFlag;

  /** eth类型 */
  ethernet_type: ETHERNET_TYPE_ENUM;
  /** 源MAC */
  ethernet_initiator: string;
  /** 目的MAC */
  ethernet_responder: string;
  /** 网络层协议 */
  ethernet_protocol: string;

  /** 源IPv4 */
  ipv4_initiator: string;
  /** 源IPv6 */
  ipv6_initiator: string;
  /** 源IP位置 */
  ip_locality_initiator: IP_ADDRESS_LOCALITY_ENUM;
  /** 源端口 */
  port_initiator: number;
  /** 源IP归属地址组ID */
  hostgroup_id_initiator: string;

  /** 目的IPv4 */
  ipv4_responder: string;
  /** 目的IPv6 */
  ipv6_responder: string;
  /** 目的IP位置 */
  ip_locality_responder: IP_ADDRESS_LOCALITY_ENUM;
  /** 目的端口 */
  port_responder: number;
  /** 目的IP归属地址组ID */
  hostgroup_id_responder: string;
  /**
   * 传输层协议
   * @description 常量在 IP_PROTOCOL_LIST 数组中记录
   */
  ip_protocol: string;
  /**
   * 应用层协议
   * @description 这个是从 SA 规则库中解析出来的
   */
  l7_protocol_id: string;

  /** 应用大类ID */
  application_category_id: number;
  /** 应用大类中文名字 */
  application_category_name?: string;
  /** 应用大类描述信息 */
  application_category_description?: string;
  /** 应用子分类ID */
  application_subcategory_id: number;
  /** 应用子分类中文名字 */
  application_subcategory_name?: string;
  /** 应用子分类描述信息 */
  application_subcategory_description?: string;
  /** 应用ID */
  application_id: number;
  /** 应用中文名字 */
  application_name?: string;
  /** 应用描述信息 */
  application_description?: string;

  /** 僵尸木蠕应用ID */
  malicious_application_id: number;
  /** 僵尸木蠕应用名字 */
  malicious_application_name: string;
  /** 僵尸木蠕应用描述信息 */
  malicious_application_description: string;

  /** 源国家ID */
  country_id_initiator: string;
  /** 源省份ID */
  province_id_initiator: string;
  /** 源城市ID */
  city_id_initiator: string;
  /** 源区县ID */
  district_id_initiator: string;
  /** 流发起方的AOI类型 */
  aoi_type_initiator: string;
  /** 流发起方的AOI名称 */
  aoi_name_initiator: string;

  /** 目的国家ID */
  country_id_responder: string;
  /** 目的省份ID */
  province_id_responder: string;
  /** 目的城市ID */
  city_id_responder: string;
  /** 目的区县ID */
  district_id_responder: string;
  /** 流应答方的AOI类型 */
  aoi_type_responder: string;
  /** 流应答方的AOI名称 */
  aoi_name_responder: string;

  /** 新建会话数 */
  established_sessions: number;
  /** TCP同步数据包 */
  tcp_syn_packets: number;
  /** TCP同步确认数据包 */
  tcp_syn_ack_packets: number;
  /** TCP同步重置数据包 */
  tcp_syn_rst_packets: number;
  /** TCP客户端总包数 */
  tcp_client_packets: number;
  /** TCP服务端总包数 */
  tcp_server_packets: number;
  /** TCP客户端重传包数 */
  tcp_client_retransmission_packets: number;
  /** TCP客户端重传率 */
  tcp_client_retransmission_rate: number;
  /** TCP服务端重传包数 */
  tcp_server_retransmission_packets: number;
  /** TCP服务端重传率 */
  tcp_server_retransmission_rate: number;
  /** TCP客户端丢包字节数 */
  tcp_client_loss_bytes: number;
  /** TCP服务端丢包字节数 */
  tcp_server_loss_bytes: number;
}

/**
 * 流日志查询结果ajax返回数据
 */
export interface IFlowRecordResponse {
  content: IFlowRecordData[];
  sort: { ascending: boolean; direction: 'DESC' | 'ASC'; property: string }[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// 流日志统计相关的数据
// ===============

export interface IFlowRecordStatisticsDatehistogram {
  timestamp: string;
  timestamp_as_number: string;
  count: number;
}
export interface IFlowRecordStatisticsIpProtocol {
  ip_protocol: string;
  count: number;
}
export interface IFlowRecordStatisticsL7Protocol {
  l7_protocol_id: string;
  count: number;
}

/**
 * 统计聚合结果
 */
export interface IFlowRecordStatisticsResponse {
  /**
   * 记录总数量
   *
   * fix: 表格记录总数查询需要扫描全量的结果集，磁盘IO使用极高
   * 所以把总页数放在这里
   */
  total: number;
}

/** 心跳参数 */
export interface IHeartbeatParams {
  queryId: string;
}
