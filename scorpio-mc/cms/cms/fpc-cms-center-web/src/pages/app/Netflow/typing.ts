import type { IFieldProperty } from '@/pages/app/analysis/components/fieldsManager';
import { EFormatterType } from '@/pages/app/analysis/components/fieldsManager';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import { EFieldEnumValueSource } from '@/pages/app/analysis/components/fieldsManager';
import { IP_PROTOCOL_ENUM_LIST } from '@/common/app';

// 排序枚举值
export enum ESortDirection {
  'DESC' = 'desc',
  'ASC' = 'asc',
}

// 协议枚举值
export enum EProtocalVersion {
  'Netflow-V5' = 1,
  'Netflow-V7' = 2,
  'Netflow-V9' = 3,
  'IPFIX' = 4,
  'SFlow' = 5,
}

// 源类型枚举值，0设备 1接口
export enum EDeviceType {
  'device' = 0,
  'interface' = 1,
}

// 下钻枚举值
export enum EDrilldown {
  'undrilldown' = '0', // 不下钻
  'drilldown' = '1', // 下钻
}

// 表格数据类型，决定表格展示格式和折线图纵坐标
export enum ETableDataType {
  'BYTE' = 0,
  'PACKET' = 1,
}

// 动态路由参数
export interface IUrlParams {
  deviceName: string;
  netifNo: string;
}

// 流量分析折线图title
export const tableRowInfos = {
  totalBytes: {
    title: '总字节数',
    type: ETableDataType.BYTE,
  },
  totalPackets: {
    title: '总包数',
    type: ETableDataType.PACKET,
  },
  transmitBytes: {
    title: '总字节数',
    type: ETableDataType.BYTE,
  },
  transmitPackets: {
    title: '总包数',
    type: ETableDataType.PACKET,
  },
  ingestBytes: {
    title: '总字节数',
    type: ETableDataType.BYTE,
  },
  ingestPackets: {
    title: '总包数',
    type: ETableDataType.PACKET,
  },
};

// 设备列表数据-设备
export interface INetflowDevice {
  id: string;
  deviceName: string;
  deviceType: string;
  alias: string;
  protocolVersion: EProtocalVersion;
  totalBandwidth: number;
  description: string;
  netif?: INetflowDeviceNetif[];
  children?: INetflowDeviceNetif[];
}

// 设备列表数据-接口
export interface INetflowDeviceNetif {
  id: string;
  deviceName: string;
  netifNo: number;
  netifSpeed: number;
  deviceType: string;
  alias: string;
  totalBandwidth: number;
  ingestBandwidth: number;
  transmitBandwidth: number;
  description: string;
}

// 折线图接口数据
export interface ILineApiData {
  totalBytes?: number;
  ipAddress?: string;
  ingest_bytes?: number;
  transmit_bytes?: number;
  timeStamp: string;
  ipv4Address?: string;
  ipv6Address?: string;
  protocol?: string;
  port?: string;
}

// dashboard数据
export interface IDashbroad {
  totalBytes: string;
  totalPackets: string;
  totalBandwidth: string;
  totalPacketSpeed: string;
  transmitBandwidthRatio: string; // 发送带宽速率
  ingestBandwidthRatio: string; // 接收带宽速率
  // 发送带宽
  transmitBandwidthHistogram: ILineApiData[];
  // 接收带宽
  ingestBandwidthHistogram: ILineApiData[];
  // 总带宽
  totalBandwidthHistogram: ILineApiData[];
  // 协议端口带宽
  protocolPortBandwidthHistogram: ILineApiData[];
  // 会话带宽
  sessionBandwidthHistogram: ILineApiData[];
  // 出入方向带宽
  transmitIngestBandwidthHistogram: ILineApiData[];
}

// 维度折线图数据
export interface IDimensionHistogram {
  ipAddress: string;
  timeStamp: string;
  totalBytes: number;
}

/** 带覆盖的折线图数据 */
export interface IStackedHistogram extends IDimensionHistogram {
  areaStyle: Record<string, unknown>;
}

// 流量分析相关
/** ip维度 */
export interface IFlowIp {
  ipAddress: string;
  totalPackets: string;
  totalBytes: string;
}

/** ip发送维度 */
export interface IFlowTransmitIp {
  ipAddress: string;
  transmitPackets: string;
  transmitBytes: string;
}

/** ip接收维度 */
export interface IFlowIngestIp {
  ipAddress: string;
  ingestPackets: string;
  ingestBytes: string;
}

/** 会话维度 */
export interface IFlowSession {
  srcIp: string;
  destIp: string;
  srcPort: string;
  destPort: string;
  protocol: string;
  totalBytes: string;
  totalPackets: string;
}

/** 协议端口维度 */
export interface IFlowPort {
  port: string;
  protocol: string;
  totalBytes: string;
  totalPackets: string;
}

// 会话详单数据定义
export interface IFlowRecord {
  report_time: string;
  src_ip: string;
  dest_ip: string;
  src_port: number;
  dest_port: number;
  protocol: string;
  total_bytes: number;
  total_packets: number;
  transmit_bytes: number;
  transmit_packets: number;
  ingest_bytes: number;
  ingest_packets: number;
  tcp_flag: number;
  dscp_flag: string;
  duration: string;
  device_name: string;
  in_netif: string;
  out_netif: string;
  start_time: string;
  end_time: string;
}

// 流量分析FilterField
export const fieldsMapping: Record<string, IFieldProperty> = {
  ip_address: {
    name: 'IP地址',
    formatterType: EFormatterType.TEXT,
    filterFieldType: EFieldType.IPV4 || EFieldType.IPV6,
    filterOperandType: EFieldOperandType.IP,
  },
  src_ip: {
    name: '源IP地址',
    formatterType: EFormatterType.TEXT,
    filterFieldType: EFieldType.IPV4 || EFieldType.IPV6,
    filterOperandType: EFieldOperandType.IP,
  },
  dest_ip: {
    name: '目的IP地址',
    formatterType: EFormatterType.TEXT,
    filterFieldType: EFieldType.IPV4 || EFieldType.IPV6,
    filterOperandType: EFieldOperandType.IP,
  },
  port: {
    name: '端口号',
    formatterType: EFormatterType.TEXT,
    filterOperandType: EFieldOperandType.PORT,
  },
  src_port: {
    name: '源端口号',
    formatterType: EFormatterType.TEXT,
    filterOperandType: EFieldOperandType.PORT,
  },
  dest_port: {
    name: '目的端口号',
    formatterType: EFormatterType.TEXT,
    filterOperandType: EFieldOperandType.PORT,
  },
  protocol: {
    name: '协议',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.LOCAL,
    enumValue: IP_PROTOCOL_ENUM_LIST,
  },
  duration: {
    name: '持续时间',
    formatterType: EFormatterType.TIME,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  ingest_packets: {
    name: '接收数据包',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  transmit_packets: {
    name: '发送数据包',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  total_packets: {
    name: '总数据包',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  total_bytes: {
    name: '总字节数',
    formatterType: EFormatterType.BYTE,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  transmit_bytes: {
    name: '发送字节数',
    formatterType: EFormatterType.BYTE,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  ingest_bytes: {
    name: '接收字节数',
    formatterType: EFormatterType.BYTE,
    filterOperandType: EFieldOperandType.NUMBER,
  },
};
