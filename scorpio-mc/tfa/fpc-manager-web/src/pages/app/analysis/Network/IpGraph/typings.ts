import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type { IFieldProperty } from '../../components/fieldsManager';
import { EModelAlias } from '../../components/fieldsManager';
import { EFieldEnumValueSource, EFormatterType } from '../../components/fieldsManager';

export const SORT_PROPERTY_LOCALITY_LIST = [
  {
    text: '会话数',
    value: 'establishedSessions',
  },
  {
    text: '总字节数',
    value: 'totalBytes',
  },
];

export const fieldsMapping: Record<string, IFieldProperty> = {
  ip_address: {
    name: 'IP/IP网段',
    formatterType: EFormatterType.TEXT,
    filterFieldType: EFieldType.IPV4 || EFieldType.IPV6,
    filterOperandType: EFieldOperandType.IP,
  },
  minEstablishedSessions: {
    name: '最小新建会话数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  minTotalBytes: {
    name: '最小字节数',
    formatterType: EFormatterType.BYTE,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  sortProperty: {
    name: '线粗维度',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.LOCAL,
    enumValue: SORT_PROPERTY_LOCALITY_LIST,
  },
  count: {
    name: '会话对数量',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  networkId: {
    name: '网络',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.MODEL,
    enumValue: EModelAlias.network,
  },
};
