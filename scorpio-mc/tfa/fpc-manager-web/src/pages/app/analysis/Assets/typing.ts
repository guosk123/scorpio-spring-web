export enum EIsbaselineType {
  'isBaseline' = '1',
  'notBaseline' = '0',
}

export const EIsbaselineTypeMap = {
  [EIsbaselineType.isBaseline]: '存在',
  [EIsbaselineType.notBaseline]: '不存在',
};

export enum EbaselineStatusType {
  'deviceType' = 'deviceType',
  'os' = 'os',
  'label' = 'label',
  'port' = 'port',
  'assetOnline' = 'assetOnline'
}

export const EbaselineStatusTypeNameMap = {
  [EbaselineStatusType.deviceType]: '设备类型',
  [EbaselineStatusType.os]: '操作系统',
  [EbaselineStatusType.label]: '服务标签',
  [EbaselineStatusType.port]: '监听端口',
  [EbaselineStatusType.assetOnline]: '资产是否在线'
};

export enum EbaselineType {
  'deviceTypeChange' = '1',
  'openPortChange' = '2',
  'bussinessLabelChange' = '3',
  'operatingSystemChange' = '4',
  'assetsOfflineSenor' = '5',
}

export const EbaselineTypeNameMap = {
  [EbaselineType.openPortChange]: '开放端口变化',
  [EbaselineType.deviceTypeChange]: '设备类型变化',
  [EbaselineType.operatingSystemChange]: '操作系统变化',
  [EbaselineType.bussinessLabelChange]: '业务标签变化',
  [EbaselineType.assetsOfflineSenor]: '资产下线感知',
};
export const baselineTypeOptions = [
  {
    label: EbaselineTypeNameMap[EbaselineType.openPortChange],
    value: EbaselineType.openPortChange,
  },
  {
    label: EbaselineTypeNameMap[EbaselineType.deviceTypeChange],
    value: EbaselineType.deviceTypeChange,
  },
  {
    label: EbaselineTypeNameMap[EbaselineType.operatingSystemChange],
    value: EbaselineType.operatingSystemChange,
  },
  {
    label: EbaselineTypeNameMap[EbaselineType.bussinessLabelChange],
    value: EbaselineType.bussinessLabelChange,
  },
  {
    label: EbaselineTypeNameMap[EbaselineType.assetsOfflineSenor],
    value: EbaselineType.assetsOfflineSenor,
  },
];
export enum ESortDirection {
  'DESC' = 'desc',
  'ASC' = 'asc',
}
export interface ISortParams {
  sortProperty: string;
  sortDirection: ESortDirection | '';
}

export interface IassetsSettingParams {
  ipAddress: string;
  type: string;
  description: string;
}


export interface optionsType {
  label: string;
  key: string;
}