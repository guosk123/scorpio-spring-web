import type { IEnumValue } from '@/components/FieldFilter/typings';
import { EFieldType } from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { EFieldOperandType } from '@/components/FieldFilter/typings';
import { useMemo } from 'react';
import type { IFieldProperty } from '../../../../analysis/components/fieldsManager';
import { EFormatterType, EModelAlias } from '../../../../analysis/components/fieldsManager';
import { EFieldEnumValueSource } from '../../../../analysis/components/fieldsManager';

import { EIpProtocol } from '../../typings';

export interface IField extends IFieldProperty {
  dataIndex: string;
  operators?: EFilterOperatorTypes[];
  width?: number;
  /** 是否可搜索 */
  searchable?: boolean;
  /** 在表格中是否需要隐藏 */
  hide?: boolean;
}

export interface IFieldList {
  networkSelections?: IEnumValue[];
}

export default function useFieldList(props: IFieldList) {
  const { networkSelections } = props;

  const fieldList: IField[] = useMemo(() => {
    return [
      {
        name: '时间',
        dataIndex: 'timestamp',
        width: 280,
        formatterType: EFormatterType.TIME,
        filterOperandType: EFieldOperandType.STRING,
      },
      {
        name: '源IP',
        dataIndex: 'ipInitiator',
        width: 120,
        formatterType: EFormatterType.TEXT,
        filterOperandType: EFieldOperandType.IP,
      },
      {
        name: '源端口',
        dataIndex: 'portInitiator',
        width: 120,
        formatterType: EFormatterType.TEXT,
        filterOperandType: EFieldOperandType.PORT,
      },
      {
        name: '目的IP',
        dataIndex: 'ipResponder',
        width: 120,
        formatterType: EFormatterType.TEXT,
        filterOperandType: EFieldOperandType.IP,
      },
      {
        name: '目的端口',
        dataIndex: 'portResponder',
        width: 120,
        formatterType: EFormatterType.TEXT,
        filterOperandType: EFieldOperandType.PORT,
      },
      {
        name: 'IP',
        dataIndex: 'ipAddress',
        hide: true,
        formatterType: EFormatterType.TEXT,

        searchable: true,
        filterFieldType: EFieldType.IPV4 || EFieldType.IPV6,
        filterOperandType: EFieldOperandType.IP,
      },
      {
        name: '端口',
        dataIndex: 'port',
        hide: true,
        formatterType: EFormatterType.TEXT,

        searchable: true,
        filterOperandType: EFieldOperandType.PORT,
      },
      {
        name: '传输层协议',
        dataIndex: 'ipProtocol',
        width: 120,
        formatterType: EFormatterType.TEXT,

        searchable: true,
        filterOperandType: EFieldOperandType.ENUM,
        enumSource: EFieldEnumValueSource.LOCAL,
        // OTHER 类型的不参与过滤
        enumValue: Object.keys(EIpProtocol).map((proto) => ({
          text: proto,
          value: proto,
        })),
      },
      { name: 'tcpFlags', dataIndex: 'tcpFlags', width: 120, formatterType: EFormatterType.TEXT },
      {
        name: 'VLANID',
        dataIndex: 'vlanId',
        width: 120,
        searchable: true,
        formatterType: EFormatterType.TEXT,
        filterOperandType: EFieldOperandType.NUMBER,
      },
      { name: '总字节数', dataIndex: 'totalBytes', width: 120, formatterType: EFormatterType.BYTE },
      {
        name: '应用',
        dataIndex: 'applicationId',
        width: 120,
        formatterType: EFormatterType.ENUM,

        searchable: true,
        filterOperandType: EFieldOperandType.ENUM,
        enumSource: EFieldEnumValueSource.MODEL,
        enumValue: EModelAlias.application,
      },
      {
        name: '应用层协议',
        dataIndex: 'l7ProtocolId',
        width: 120,
        formatterType: EFormatterType.ENUM,

        searchable: true,
        filterOperandType: EFieldOperandType.ENUM,
        enumSource: EFieldEnumValueSource.MODEL,
        enumValue: EModelAlias.l7protocol,
      },
      // {
      //   name: 'ETH类型',
      //   dataIndex: 'ethernetType',
      //   width: 120,
      //   formatterType: EFormatterType.ENUM,
      //   filterOperandType: EFieldOperandType.ENUM,
      //   enumSource: EFieldEnumValueSource.LOCAL,
      //   enumValue: ETHERNET_TYPE_LIST,
      // },
      {
        name: '源MAC地址',
        dataIndex: 'ethernetInitiator',
        width: 200,
        formatterType: EFormatterType.TEXT,
      },
      {
        name: '目的MAC地址',
        dataIndex: 'ethernetResponder',
        width: 200,
        formatterType: EFormatterType.TEXT,
      },
      {
        name: 'MAC地址',
        dataIndex: 'macAddress',
        formatterType: EFormatterType.TEXT,

        hide: true,
        searchable: true,
        filterOperandType: EFieldOperandType.STRING,
      },
      {
        name: '源IP国家',
        dataIndex: 'countryIdInitiator',
        width: 120,
        formatterType: EFormatterType.ENUM,
        filterOperandType: EFieldOperandType.ENUM,
        enumSource: EFieldEnumValueSource.MODEL,
        enumValue: EModelAlias.country,
      },
      {
        name: '源IP省份',
        dataIndex: 'provinceIdInitiator',
        width: 120,
        formatterType: EFormatterType.ENUM,
        filterOperandType: EFieldOperandType.ENUM,
        enumSource: EFieldEnumValueSource.MODEL,
        enumValue: EModelAlias.province,
      },
      {
        name: '源IP城市',
        dataIndex: 'cityIdInitiator',
        width: 120,
        formatterType: EFormatterType.ENUM,
        filterOperandType: EFieldOperandType.ENUM,
        enumSource: EFieldEnumValueSource.MODEL,
        enumValue: EModelAlias.city,
      },
      {
        name: '目的IP国家 ',
        dataIndex: 'countryIdResponder',
        width: 120,
        formatterType: EFormatterType.ENUM,
        filterOperandType: EFieldOperandType.ENUM,
        enumSource: EFieldEnumValueSource.MODEL,
        enumValue: EModelAlias.country,
      },
      {
        name: '目的IP省份',
        dataIndex: 'provinceIdResponder',
        width: 120,
        formatterType: EFormatterType.ENUM,
        filterOperandType: EFieldOperandType.ENUM,
        enumSource: EFieldEnumValueSource.MODEL,
        enumValue: EModelAlias.province,
      },
      {
        name: '目的IP城市',
        dataIndex: 'cityIdResponder',
        width: 120,
        formatterType: EFormatterType.ENUM,
        filterOperandType: EFieldOperandType.ENUM,
        enumSource: EFieldEnumValueSource.MODEL,
        enumValue: EModelAlias.city,
      },
      {
        name: '国家',
        dataIndex: 'countryId',
        formatterType: EFormatterType.ENUM,
        hide: true,
        searchable: true,
        filterOperandType: EFieldOperandType.ENUM,
        enumSource: EFieldEnumValueSource.MODEL,
        enumValue: EModelAlias.country,
      },
      {
        name: '省份',
        dataIndex: 'provinceId',
        formatterType: EFormatterType.ENUM,

        hide: true,
        searchable: true,
        filterOperandType: EFieldOperandType.ENUM,
        enumSource: EFieldEnumValueSource.MODEL,
        enumValue: EModelAlias.province,
      },
      {
        name: '城市',
        dataIndex: 'cityId',
        formatterType: EFormatterType.ENUM,

        hide: true,
        searchable: true,
        filterOperandType: EFieldOperandType.ENUM,
        enumSource: EFieldEnumValueSource.MODEL,
        enumValue: EModelAlias.city,
      },
      {
        name: '网络',
        dataIndex: 'network_id',
        hide: true,
        searchable: true,
        filterOperandType: EFieldOperandType.ENUM,
        enumSource: EFieldEnumValueSource.VARIABlES,
        formatterType: EFormatterType.ENUM,
        enumValue: networkSelections || [],
        operators: [EFilterOperatorTypes.EQ],
      },
      {
        name: '业务',
        dataIndex: 'service_id',
        hide: true,
        searchable: true,
        filterOperandType: EFieldOperandType.ENUM,
        enumSource: EFieldEnumValueSource.MODEL,
        formatterType: EFormatterType.ENUM,
        enumValue: EModelAlias.service,
        operators: [EFilterOperatorTypes.EQ],
      },
      {
        name: 'BPF语句',
        dataIndex: 'bpf',
        hide: true,
        searchable: true,
        filterOperandType: EFieldOperandType.STRING,
        formatterType: EFormatterType.TEXT,
        operators: [EFilterOperatorTypes.EQ],
      },
    ];
  }, [networkSelections]);
  return fieldList;
}
