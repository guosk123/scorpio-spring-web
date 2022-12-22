export interface PositionDetail {
  x: number;
  y: number;
  w: number;
  h: number;
}
export enum ENetowrkType {
  NETWORK_GROUP = 'networkGroup',
  NETWORK = 'network',
}
export interface INetworkTreeItem {
  title: string;
  value: string;
  key: string;
  type: ENetowrkType;
  recordId: string;
  networkInSensorIds?: string;
  children?: INetworkTreeItem[];
  status: '0' | '1';
  statusDetail?: string;
}

export type IpTableTitle = {
  ip_address: string;
  tcpEstablishedCounts: number;
  tcpEstablishedFailCounts: number;
  totalBytes: number;
  ip_locality_responder?: number;
  ip_locality_initiator?: number;
};

export type PortTabelTitle = {
  port_initiator: string;
  port_responder: string;
  tcpEstablishedCounts: number;
  tcpEstablishedFailCounts: number;
  totalBytes: number;
};

export type DomainNameTableTitle = {
  domain: string;
  AverageLatency: number;
  totalCounts: number;
  failCounts: number;
};

export type AlarmTableTitle = {
  msg: string;
  timestamp: string;
  counts: string;
  signatureSeverity: string;
};

export enum IShowCategory {
  VISITINGIP = 'ip_initiator',
  VISITEDIP = 'ip_responder',
  SHARINGDOMAINNAME = 'ip_dest',
  VISITINGDOMAINNAME = 'ip_src',
  SHARINGPORT = 'port_initiator',
  VISITINGPORT = 'port_responder',
  // FlOWRATIO = 'application_id_ratio',
  // FLOWCONNECTIONS = 'application_id_rank',
  CONNECTIONSOURCE = 'country_initiator',
  CONNECTIONTARGET = 'country_responder',
  SECURITYALERTS = 'security_alert',
  // TOP应用宽带趋势
  APPLICATIONTREND = 'applications',
  // TOP地区宽带趋势
  LOCATIONTREND = 'locations',
}

export const categoryMap = {
  [IShowCategory.VISITINGIP]: '来访的IP',
  [IShowCategory.VISITEDIP]: '访问的IP',
  [IShowCategory.SHARINGPORT]: '开放的端口',
  [IShowCategory.VISITINGPORT]: '访问的端口',
  [IShowCategory.SHARINGDOMAINNAME]: '开放的域名',
  [IShowCategory.VISITINGDOMAINNAME]: '访问的域名',
  // [IShowCategory.FlOWRATIO]: '应用流量占比',
  // [IShowCategory.FLOWCONNECTIONS]: '应用连接排名',
  [IShowCategory.CONNECTIONSOURCE]: '连接源',
  [IShowCategory.CONNECTIONTARGET]: '连接目的',
  [IShowCategory.SECURITYALERTS]: '安全告警',
  [IShowCategory.APPLICATIONTREND]: 'TOP应用宽带趋势',
  [IShowCategory.LOCATIONTREND]: 'TOP地区宽带趋势',
};

export enum SeverityLevelType {
  urgent = '0',
  serious = '1',
  common = '2',
  tip = '3',
  audit = '4',
}

export const SeverityLevelMap = {
  [SeverityLevelType.urgent]: '紧急',
  [SeverityLevelType.serious]: '严重',
  [SeverityLevelType.common]:  '一般',
  [SeverityLevelType.tip]: '提示',
  [SeverityLevelType.audit]: '审计'
}

export enum MapType {
  WORLD = 'worldMap',
  CHINA = 'china',
}
export const Map_Enum = {
  [MapType.CHINA]: '中国地图',
  [MapType.WORLD]: '世界地图',
};
export const MapTypeOptions = [
  { label: Map_Enum[MapType.CHINA], value: MapType.CHINA },
  { label: Map_Enum[MapType.WORLD], value: MapType.WORLD },
];

export enum MapDataType {
  TRFFICCOUNT = 'traffic',
  FLOWCOUNT = 'flow',
}
export const MapData_Enum = {
  [MapDataType.TRFFICCOUNT]: '会话数',
  [MapDataType.FLOWCOUNT]: '流量数',
};
export const MapDataTypeOptions = [
  {
    label: MapData_Enum[MapDataType.TRFFICCOUNT],
    value: MapDataType.TRFFICCOUNT,
  },
  {
    label: MapData_Enum[MapDataType.FLOWCOUNT],
    value: MapDataType.FLOWCOUNT,
  },
];

//来访IP，访问IP，开放端口，访问端口切换三种展示方式
export enum DataShowedType {
  TABLE = 'table',
  PIECHART = 'piechart',
  BARLINECHART = 'barlinechart',
}
export const DataShowedType_Enum = {
  [DataShowedType.TABLE]: '表格',
  [DataShowedType.PIECHART]: '饼图',
  [DataShowedType.BARLINECHART]: '柱折线图',
};
export const DataShowedTypeOptions = [
  {
    label: DataShowedType_Enum[DataShowedType.TABLE],
    value: DataShowedType.TABLE,
  },
  {
    label: DataShowedType_Enum[DataShowedType.PIECHART],
    value: DataShowedType.PIECHART,
  },
  {
    label: DataShowedType_Enum[DataShowedType.BARLINECHART],
    value: DataShowedType.BARLINECHART,
  },
];

export enum NetworkLocationType {
  INTRANET = '0',
  EXTRANET = '1',
  ALL = '2',
}
export const NetworkLocationType_Enum = {
  [NetworkLocationType.INTRANET]: '内网',
  [NetworkLocationType.EXTRANET]: '外网',
  [NetworkLocationType.ALL]: '全部',
};
export const NetworkLocationTypeOptions = [
  {
    label: NetworkLocationType_Enum[NetworkLocationType.INTRANET],
    value: NetworkLocationType.INTRANET,
  },
  {
    label: NetworkLocationType_Enum[NetworkLocationType.EXTRANET],
    value: NetworkLocationType.EXTRANET,
  },
  {
    label: NetworkLocationType_Enum[NetworkLocationType.ALL],
    value: NetworkLocationType.ALL,
  },
];

export const NetworklocationTypeFilters = [
  {
    text: NetworkLocationType_Enum[NetworkLocationType.EXTRANET],
    value: NetworkLocationType.EXTRANET,
  },
  {
    text: NetworkLocationType_Enum[NetworkLocationType.INTRANET],
    value: NetworkLocationType.INTRANET,
  },
];

export const tableTop = [10, 50, 100, 150, 300];
