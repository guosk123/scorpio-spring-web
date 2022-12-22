/* eslint-disable no-underscore-dangle */
import type { IEnumValue } from '@/components/FieldFilter/typings';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import {
  DHCP_MESSAGE_TYPE_LIST,
  ETHERNET_TYPE_LIST,
  IP_PROTOCOL_ENUM_LIST,
  IP_ADDRESS_LOCALITY_LIST,
} from '@/common/app';
import type { ILogicalSubnet } from '@/pages/app/Configuration/LogicalSubnet/typings';
import { bytesToSize, convertBandwidth } from '@/utils/utils';
import { MALICIOUS_SUB_CATEGORY_IDS } from '@/models/app/saKnowledge';
import moment from 'moment';
import numeral from 'numeral';
import type {
  GeolocationModelState,
  IIpAddressGroupModelState,
  MetadataModelState,
  NetworkModelState,
  SAKnowledgeModelState,
  ServiceModelState,
} from 'umi';
import { getDvaApp } from 'umi';

export const TIME_FORMAT = 'YYYY-MM-DD HH:mm:ss';

/** 字段枚举值的来源 */
export enum EFieldEnumValueSource {
  /** 本地常量 */
  'LOCAL' = 'local',
  /** Model数据 */
  'MODEL' = 'model',
  /** 外部变量数据 */
  'VARIABlES' = 'variables'
}

/** 字段格式化类型 */
export enum EFormatterType {
  /** 时间 */
  TIME = 'time',
  /** 字节数 */
  BYTE = 'byte',
  /** bps */
  BPS = 'bps',
  /** bps */
  BYTE_PS = 'byte_ps',
  /** 时延 */
  LATENCY = 'latency',
  /** 枚举值 */
  ENUM = 'enum',
  /** COUNT值 */
  COUNT = 'count',
  /** pps */
  PPS = 'pps',
  /** per second */
  PS = 'ps',
  /** 文本字符串 */
  TEXT = 'text',
  /**
   * 存储空间
   * @description 换算进制按照 1024
   */
  CAPACITY = 'capacity',
}

/** 统一个格式化类型，在不同的页面可能会有不同的格式化方法，但总体来说不会太多 */
/** 格式化方法集合 */
export const fieldFormatterFuncMap: Record<string, (value: any) => string> = {
  [EFormatterType.BYTE]: (value: number) => {
    if (value) {
      return bytesToSize(value);
    }
    return '0';
  },
  [EFormatterType.BYTE_PS]: (value: number) => {
    // 没有提供则将value视为bps
    return convertBandwidth(value * 8);
  },
  [EFormatterType.BPS]: (value: number) => {
    return convertBandwidth(value);
  },
  [EFormatterType.TIME]: (value: string) => moment(value).format('YYYY-MM-DD HH:mm:ss'),
  [EFormatterType.LATENCY]: (value: number) => `${numeral(value).format('0,0')}ms`,
  [EFormatterType.COUNT]: (value: number) => {
    if (value) {
      return numeral(value).format('0,0');
    }
    return '0';
  },
  [EFormatterType.PPS]: (value: number) => {
    return `${value}pps`;
  },
  [EFormatterType.PS]: (value: number) => {
    return `${value}ps`;
  },
  [EFormatterType.TEXT]: (value: string) => value,
};

export enum EModelAlias {
  network = 'network',
  service = 'service',
  country = 'country',
  city = 'city',
  province = 'province',
  application = 'application',
  category = 'category',
  subcategory = 'subcategory',
  l7protocol = 'l7protocol',
  hostGroup = 'hostGroup',
}

interface modelEnumData {
  list: IEnumValue[];
  map: Record<string, any>;
}

/**
 *
 * @param modelLocation umi中，已注册的model名称
 * @returns 返回值list为filter组件中需要的数组。map为表格组件中根据id获取名称的map对象。
 */
export function getEnumValueFromModelNext(alias: EModelAlias): modelEnumData | null {
  const modelNameList = alias.split('|');
  if (modelNameList.length === 0) {
    return null;
  }
  let listData: IEnumValue[] = [];
  let mapData: Record<string, any> = {};
  switch (alias) {
    case EModelAlias.network: {
      const originData = getDvaApp()._store.getState().networkModel;
      const networkGroup = (originData as NetworkModelState).allNetworkGroup.map((item) => ({
        text: item.name,
        value: item.id,
      }));
      const networkList = (originData as NetworkModelState).allNetworkSensor.map((item) => ({
        text: item.networkInSensorName,
        value: item.networkInSensorId,
      }));
      const networkMap = (originData as NetworkModelState).allNetworkSensorMap;
      const networkGroupMap = (originData as NetworkModelState).allNetworkGroupMap;
      const allLogicalSubnets: IEnumValue[] = getDvaApp()
        ._store.getState()
        .logicSubnetModel.allLogicalSubnets.map((item: ILogicalSubnet) => ({
          text: item.name,
          value: item.id,
        }));
      const { allLogicalSubnetMap } = getDvaApp()._store.getState().logicSubnetModel;
      listData = [...networkGroup, ...networkList, ...allLogicalSubnets];
      mapData = {
        ...networkGroupMap,
        ...networkMap,
        ...allLogicalSubnetMap,
      };

      break;
    }
    case EModelAlias.service: {
      const originData = getDvaApp()._store.getState().serviceModel;
      listData = (originData as ServiceModelState).allServices.map((item) => ({
        text: item.name,
        value: item.id,
      }));
      mapData = (originData as ServiceModelState).allServiceMap;
      break;
    }
    case EModelAlias.country: {
      const originData = getDvaApp()._store.getState().geolocationModel;
      listData = (originData as GeolocationModelState).allCountryList.map((item) => ({
        text: item.nameText,
        value: item.countryId,
      }));
      mapData = (originData as GeolocationModelState).allCountryMap;
      break;
    }
    case EModelAlias.city: {
      const originData = getDvaApp()._store.getState().geolocationModel;
      listData = (originData as GeolocationModelState).allCityList.map((item) => ({
        text: item.nameText,
        value: item.cityId,
      }));
      mapData = (originData as GeolocationModelState).allCityMap;
      break;
    }
    case EModelAlias.province: {
      const originData = getDvaApp()._store.getState().geolocationModel;
      listData = (originData as GeolocationModelState).allProvinceList.map((item) => ({
        text: item.nameText,
        value: item.provinceId,
      }));
      mapData = (originData as GeolocationModelState).allProvinceMap;
      break;
    }
    case EModelAlias.application: {
      const originData = getDvaApp()._store.getState().SAKnowledgeModel;
      listData = (originData as SAKnowledgeModelState).allApplicationList
        .filter((ele) => !MALICIOUS_SUB_CATEGORY_IDS.includes(ele.subCategoryId))
        .map((item) => ({
          text: item.nameText,
          value: item.applicationId,
        }));
      mapData = (originData as SAKnowledgeModelState).allApplicationMap;
      break;
    }
    case EModelAlias.category: {
      const originData = getDvaApp()._store.getState().SAKnowledgeModel;
      listData = (originData as SAKnowledgeModelState).allCategoryList.map((item) => ({
        text: item.nameText,
        value: item.categoryId,
      }));
      mapData = (originData as SAKnowledgeModelState).allCategoryMap;
      break;
    }
    case EModelAlias.subcategory: {
      const originData = getDvaApp()._store.getState().SAKnowledgeModel;
      listData = (originData as SAKnowledgeModelState).allSubCategoryList.map((item) => ({
        text: item.nameText,
        value: item.subCategoryId,
      }));
      mapData = (originData as SAKnowledgeModelState).allSubCategoryMap;
      break;
    }
    case EModelAlias.l7protocol: {
      const originData = getDvaApp()._store.getState().metadataModel;
      listData = (originData as MetadataModelState).allL7ProtocolsList.map((item) => ({
        text: item.nameText,
        value: item.protocolId,
      }));
      mapData = (originData as MetadataModelState).allL7ProtocolMap;
      break;
    }
    case EModelAlias.hostGroup: {
      const originData = getDvaApp()._store.getState().ipAddressGroupModel;
      listData = (originData as IIpAddressGroupModelState).allIpAddressGroupList.map((item) => ({
        text: item.name,
        value: item.id,
      }));
      mapData = (originData as IIpAddressGroupModelState).allIpAddressGroupMap;
      break;
    }
    default: {
      return null;
    }
  }
  // 用于id去重
  const idSet: string[] = Object.keys(mapData);
  return {
    list: listData.filter((item) => {
      const findIndex = idSet.findIndex((id) => id === item.value);
      if (findIndex !== -1) {
        idSet.splice(findIndex, 1);
        return true;
      }
      return false;
    }),
    map: mapData,
  };
}

// 可以进行排序的字段值类型
export const SortedTypes = [
  EFormatterType.BYTE,
  EFormatterType.COUNT,
  EFormatterType.LATENCY,
  EFormatterType.TIME,
];

/** 字段属性 */
export interface IFieldProperty {
  /** 字段名字 */
  name: string;
  /** 格式化类型 */
  formatterType: EFormatterType;

  // ------字段过滤配置-------
  /** 是否可以被过滤 */
  // searchable?: boolean;
  /** 过滤器中字段的类型 */
  filterFieldType?: EFieldType;
  /** Filter中字段的类型 */
  filterOperandType?: EFieldOperandType;

  enumSource?: EFieldEnumValueSource;

  /**
   * @value 本地枚举值
   * @value model位置
   */
  enumValue?: IEnumValue[] | EModelAlias;
}

// Record<Enum, IFieldProperty>
export const fieldsMapping: Record<string, IFieldProperty> = {
  timestamp: {
    name: '时间',
    formatterType: EFormatterType.TIME,
  },
  duration: {
    name: '持续时间',
    formatterType: EFormatterType.TIME,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  totalBytes: {
    name: '总字节数',
    formatterType: EFormatterType.BYTE,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  bytepsAvg: {
    name: '平均带宽',
    formatterType: EFormatterType.BYTE_PS,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  downstreamBytes: {
    name: '下行字节数',
    formatterType: EFormatterType.BYTE,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  upstreamBytes: {
    name: '上行字节数',
    formatterType: EFormatterType.BYTE,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  totalPayloadBytes: {
    name: '总负载字节数',
    formatterType: EFormatterType.BYTE,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  downstreamPayloadBytes: {
    name: '下行负载字节数',
    formatterType: EFormatterType.BYTE,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  upstreamPayloadBytes: {
    name: '上行负载字节数',
    formatterType: EFormatterType.BYTE,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  sendBytes: {
    name: '发送字节数',
    formatterType: EFormatterType.BYTE,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  receiveBytes: {
    name: '接收字节数',
    formatterType: EFormatterType.BYTE,
    filterOperandType: EFieldOperandType.NUMBER,
  },

  totalPackets: {
    name: '总数据包',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  downstreamPackets: {
    name: '接收数据包',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  upstreamPackets: {
    name: '发送数据包',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  totalPayloadPackets: {
    name: '总负载包数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  downstreamPayloadPackets: {
    name: '接收负载包数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  upstreamPayloadPackets: {
    name: '发送负载包数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  tcpSynPackets: {
    name: 'TCP同步包数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  tcpSynAckPackets: {
    name: 'TCP同步确认数据包数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  tcpSynRstPackets: {
    name: 'TCP同步重置数据包数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  sendPackets: {
    name: '发送包数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  receivePackets: {
    name: '接收包数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  establishedSessions: {
    name: '新建会话数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  tcpClientNetworkLatencyCounts: {
    name: '客户端网络时延统计次数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  tcpServerNetworkLatency: {
    name: '服务器网络总时延',
    formatterType: EFormatterType.LATENCY,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  tcpServerNetworkLatencyCounts: {
    name: '服务端网络时延统计次数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  serverResponseLatencyCounts: {
    name: '服务器响应时延统计次数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  tcpClientZeroWindowPackets: {
    name: '客户端零窗口包数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  tcpServerZeroWindowPackets: {
    name: '服务端零窗口包数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  tcpEstablishedSuccessCounts: {
    name: 'TCP建连成功数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  tcpEstablishedFailCounts: {
    name: 'TCP建连失败数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  tcpEstablishedSuccessRate: {
    name: 'TCP连接成功率',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  tcpClientRetransmissionPackets: {
    name: 'TCP客户端重传包数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  tcpClientPackets: {
    name: 'TCP客户端总包数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  tcpServerRetransmissionPackets: {
    name: 'TCP服务端重传包数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  tcpServerPackets: {
    name: 'TCP服务端总包数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  activeEstablishedSessions: {
    name: '主动新建会话数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  passiveEstablishedSessions: {
    name: '被动新建会话数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },

  tcpClientNetworkLatency: {
    name: '客户端网络总时延',
    formatterType: EFormatterType.LATENCY,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  serverResponseLatency: {
    name: '服务器响应总时延',
    formatterType: EFormatterType.LATENCY,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  tcpServerNetworkLatencyAvg: {
    name: '服务器网络平均时延',
    formatterType: EFormatterType.LATENCY,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  tcpClientNetworkLatencyAvg: {
    name: '客户端网络平均时延',
    formatterType: EFormatterType.LATENCY,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  serverResponseLatencyAvg: {
    name: '服务器响应平均时延',
    formatterType: EFormatterType.LATENCY,
    filterOperandType: EFieldOperandType.NUMBER,
  },

  port: {
    name: '端口号',
    formatterType: EFormatterType.TEXT,
    filterOperandType: EFieldOperandType.PORT,
  },
  macAddress: {
    name: 'MAC地址',
    formatterType: EFormatterType.TEXT,
  },
  ipAddress: {
    name: 'IP地址',
    formatterType: EFormatterType.TEXT,
    filterFieldType: EFieldType.IPV4 || EFieldType.IPV6,
    filterOperandType: EFieldOperandType.IP,
  },
  ipAAddress: {
    name: '地址A',
    formatterType: EFormatterType.TEXT,
    filterFieldType: EFieldType.IPV4 || EFieldType.IPV6,
    filterOperandType: EFieldOperandType.IP,
  },
  ipBAddress: {
    name: '地址B',
    formatterType: EFormatterType.TEXT,
    filterFieldType: EFieldType.IPV4 || EFieldType.IPV6,
    filterOperandType: EFieldOperandType.IP,
  },
  serverIpAddress: {
    name: '服务端IP',
    formatterType: EFormatterType.TEXT,
    filterFieldType: EFieldType.IPV4 || EFieldType.IPV6,
    filterOperandType: EFieldOperandType.IP,
  },
  serverMacAddress: {
    name: '服务端MAC地址',
    formatterType: EFormatterType.TEXT,
  },
  clientIpAddress: {
    name: '客户端IP',
    formatterType: EFormatterType.TEXT,
    filterFieldType: EFieldType.IPV4 || EFieldType.IPV6,
    filterOperandType: EFieldOperandType.IP,
  },
  clientMacAddress: {
    name: '客户端MAC地址',
    formatterType: EFormatterType.TEXT,
  },

  ipLocality: {
    name: 'IP所在位置',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.LOCAL,
    enumValue: IP_ADDRESS_LOCALITY_LIST,
  },
  // TODO: 确认ethernetType的名称叫什么
  ethernetType: {
    name: '三层协议类型',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.LOCAL,
    enumValue: ETHERNET_TYPE_LIST,
  },
  messageType: {
    name: '消息类型',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.LOCAL,
    enumValue: DHCP_MESSAGE_TYPE_LIST,
  },
  ipProtocol: {
    name: '传输层协议',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.LOCAL,
    enumValue: IP_PROTOCOL_ENUM_LIST,
  },
  type: {
    name: '统计粒度',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.LOCAL,
    enumValue: [
      { text: '分类', value: '0' },
      { text: '子分类', value: '1' },
      { text: '应用', value: '2' },
    ],
  },
  l7ProtocolId: {
    name: '应用层协议',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.MODEL,
    enumValue: EModelAlias.l7protocol,
  },
  protocol: {
    name: '协议',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.LOCAL,
    enumValue: IP_PROTOCOL_ENUM_LIST,
  },
  subcategoryId: {
    name: '子分类',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.MODEL,
    enumValue: EModelAlias.subcategory,
  },
  applicationId: {
    name: '应用',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.MODEL,
    enumValue: EModelAlias.application,
  },
  categoryId: {
    name: '分类',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.MODEL,
    enumValue: EModelAlias.category,
  },
  countryId: {
    name: '国家',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.MODEL,
    enumValue: EModelAlias.country,
  },
  provinceId: {
    name: '省份',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.MODEL,
    enumValue: EModelAlias.province,
  },
  cityId: {
    name: '城市',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.MODEL,
    enumValue: EModelAlias.city,
  },
  serviceId: {
    name: '业务',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.MODEL,
    enumValue: EModelAlias.service,
  },
  hostgroupId: {
    name: '地址组',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.MODEL,
    enumValue: EModelAlias.hostGroup,
  },
  networkId: {
    name: '网络',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.MODEL,
    enumValue: EModelAlias.network,
  },
};

export type AllFields = keyof typeof fieldsMapping;

export type IFieldsMapping = Record<AllFields, IFieldProperty>;
