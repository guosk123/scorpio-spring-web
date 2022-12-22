import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type { IFieldProperty } from '../../components/fieldsManager';
import { EModelAlias } from '../../components/fieldsManager';
import { EFieldEnumValueSource, EFormatterType } from '../../components/fieldsManager';
import { SORT_PROPERTY_LOCALITY_LIST } from '@/pages/app/Network/IpGraph/typings';

export const videoFields: Record<string, IFieldProperty> = {
  ipAddress: {
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
  l7ProtocolId: {
    name: '应用层协议',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.MODEL,
    enumValue: EModelAlias.l7protocol,
  },
};
