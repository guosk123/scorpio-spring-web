export interface ISearchBoxInfo {
  networkIds: string;
  IpAddress: string;
}

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
  signatureSeverity: string;
  counts: string;
};

export enum IShowCategory {
  VISITINGIP = 'ip_initiator',
  VISITEDIP = 'ip_responder',
  SHARINGDOMAINNAME = 'ip_dest',
  VISITINGDOMAINNAME = 'ip_src',
  SHARINGPORT = 'port_initiator',
  VISITINGPORT = 'port_responder',
  SECURITYALERTS = 'security_alert',
  // FlOWRATIO = 'application_id_ratio',
  // FLOWCONNECTIONS = 'application_id_rank',
  // 连接源
  CONNECTIONSOURCE = 'country_initiator',
  // 连接目的
  CONNECTIONTARGET = 'country_responder',
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
  [IShowCategory.SECURITYALERTS]: '安全告警',
  // [IShowCategory.FlOWRATIO]: '应用流量占比',
  // [IShowCategory.FLOWCONNECTIONS]: '应用连接排名',
  [IShowCategory.CONNECTIONSOURCE]: '连接源',
  [IShowCategory.CONNECTIONTARGET]: '连接目的',
  [IShowCategory.APPLICATIONTREND]: '应用宽带趋势',
  [IShowCategory.LOCATIONTREND]: '地区宽带趋势',
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
  BARCHART = 'barchart',
}
export const DataShowedType_Enum = {
  [DataShowedType.TABLE]: '表格',
  [DataShowedType.PIECHART]: '饼图',
  [DataShowedType.BARLINECHART]: '柱折线图',
  [DataShowedType.BARCHART]: '柱状图',
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

export const tableTop = [10, 20, 50, 100, 150];
// export enum ChinaProvinceType {
//   // 北京
//   //  Beijing BJ
//   BJ = '110000',
//   // 天津
//   //  Tianjing TJ
//   TJ = '120000',
//   // 河北
//   //  Hebei HE
//   HE = '130000',
//   // 山西
//   //  Shanxi SX
//   SX = '140000',
//   // 内蒙古自治区
//   //  Inner Mongoria IM
//   // （NM）
//   NM = '150000',
//   // 辽宁
//   //  Liaoning LN
//   LN = '210000',
//   // 吉林
//   //  Jilin JL
//   JL = '220000',
//   // 黑龙江
//   //  Heilongjiang HL
//   HL = '230000',
//   SH = '310000',
//   JS = '320000',
//   ZJ = '330000',
//   // 安徽
//   //  Anhui AH
//   AH = '340000',
//   FJ = '350000',
//   JX = '360000',
//   SD = '370000',
//   HA = '410000',
//   HB = '420000',
//   HN = '430000',
//   GD = '440000',
//   GX = '450000',
//   HI = '460000',
//   CQ = '500000',
//   SC = '510000',
//   GZ = '520000',
//   YN = '530000',
//   XZ = '540000',
//   SN = '610000',
//   GS = '620000',
//   QH = '630000',
//   NX = '640000',
//   XJ = '650000',
//   // 台湾
//   //  Taiwan TW
//   TW = '710000',
//   // 香港
//   //  Hong Kong HK
//   HK = '810000',
//   // 澳门
//   //  Macao MO
//   MO = '820000',
// }

// export const ChinaProvince_Enum = {
//   [ChinaProvinceType.BJ]: '北京',
//   [ChinaProvinceType.TJ]: '天津',
//   [ChinaProvinceType.HE]: '河北',
//   [ChinaProvinceType.SX]: '山西',
//   [ChinaProvinceType.NM]: '内蒙古',
//   [ChinaProvinceType.LN]: '辽宁',
//   [ChinaProvinceType.JL]: '吉林',
//   [ChinaProvinceType.HL]: '黑龙江',
//   [ChinaProvinceType.SH]: '上海',
//   [ChinaProvinceType.JS]: '江苏',
//   [ChinaProvinceType.ZJ]: '浙江',
//   [ChinaProvinceType.AH]: '安徽',
//   [ChinaProvinceType.FJ]: '福建',
//   [ChinaProvinceType.JX]: '江西',
//   [ChinaProvinceType.SD]: '山东',
//   [ChinaProvinceType.HA]: '河南',
//   [ChinaProvinceType.HB]: '湖北',
//   [ChinaProvinceType.HN]: '湖南',
//   [ChinaProvinceType.GD]: '广东',
//   [ChinaProvinceType.GX]: '广西',
//   [ChinaProvinceType.HI]: '海南',
//   [ChinaProvinceType.CQ]: '重庆',
//   [ChinaProvinceType.SC]: '四川',
//   [ChinaProvinceType.GZ]: '贵州',
//   [ChinaProvinceType.YN]: '云南',
//   [ChinaProvinceType.XZ]: '西藏',
//   [ChinaProvinceType.SN]: '陕西',
//   [ChinaProvinceType.GS]: '甘肃',
//   [ChinaProvinceType.QH]: '青海',
//   [ChinaProvinceType.NX]: '宁夏',
//   [ChinaProvinceType.XJ]: '新疆',
//   [ChinaProvinceType.TW]: '台湾',
//   [ChinaProvinceType.HK]: '香港',
//   [ChinaProvinceType.MO]: '澳门',
// };

export const chinaProvinceOptions = [
  // { name: '全国', code: '0' },
  { name: '北京市', code: '110000' },
  { name: '天津市', code: '120000' },
  { name: '河北省', code: '130000' },
  { name: '山西省', code: '140000' },
  { name: '内蒙古自治区', code: '150000' },
  { name: '辽宁省', code: '210000' },
  { name: '吉林省', code: '220000' },
  { name: '黑龙江省', code: '230000' },
  { name: '上海市', code: '310000' },
  { name: '江苏省', code: '320000' },
  { name: '浙江省', code: '330000' },
  { name: '安徽省', code: '340000' },
  { name: '福建省', code: '350000' },
  { name: '江西省', code: '360000' },
  { name: '山东省', code: '370000' },
  { name: '河南省', code: '410000' },
  { name: '湖北省', code: '420000' },
  { name: '湖南省', code: '430000' },
  { name: '广东省', code: '440000' },
  { name: '广西壮族自治区', code: '450000' },
  { name: '海南省', code: '460000' },
  { name: '重庆市', code: '500000' },
  { name: '四川省', code: '510000' },
  { name: '贵州省', code: '520000' },
  { name: '云南省', code: '530000' },
  { name: '西藏自治区', code: '540000' },
  { name: '陕西省', code: '610000' },
  { name: '甘肃省', code: '620000' },
  { name: '青海省', code: '630000' },
  { name: '宁夏回族自治区', code: '640000' },
  { name: '新疆维吾尔自治区', code: '650000' },
  { name: '台湾省', code: '710000' },
  { name: '香港特别行政区', code: '810000' },
  { name: '澳门', code: '820000' },
  { name: '南海诸岛' },
];
