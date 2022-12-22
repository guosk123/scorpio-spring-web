import { DEFAULT_PAGE_SIZE_KEY, PAGE_DEFAULT_SIZE } from '@/common/app';
import AutoHeightContainer from '@/components/AutoHeightContainer';
import { TableEmpty } from '@/components/EnhancedTable';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import type { IEnumValue, IFilter } from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type { IFieldProperty } from '@/pages/app/analysis/components/fieldsManager';
import {
  EFieldEnumValueSource,
  EFormatterType,
  EModelAlias,
  fieldFormatterFuncMap,
  getEnumValueFromModelNext,
} from '@/pages/app/analysis/components/fieldsManager';
import storage from '@/utils/frame/storage';
import { Pagination, Table } from 'antd';
import type { PaginationProps } from 'antd/es/pagination';
import type { ColumnProps } from 'antd/lib/table';
import { connect } from 'dva';
import { useMemo, useState } from 'react';
import { v1 } from 'uuid';
import type { IPacket, IPacketConnectState } from '../../typings';
import { EConditionType, EIpProtocol } from '../../typings';
import PacketLimit from '../PacketLimit';
import styles from './index.less';

interface IField extends IFieldProperty {
  dataIndex: string;
  operators?: EFilterOperatorTypes[];
  width?: number;
  /** 是否可搜索 */
  searchable?: boolean;
  /** 在表格中是否需要隐藏 */
  hide?: boolean;
}
export const fieldList: IField[] = [
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
    enumSource: EFieldEnumValueSource.MODEL,
    formatterType: EFormatterType.ENUM,
    enumValue: EModelAlias.network,
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

interface IPacketListProps {
  listData: IPacket[];
  // conditionType: EConditionType;
  onAppendFilter: (filter: IFilter) => void;
  queryListloading: boolean | undefined;
  loading?: boolean;
}
function PacketList(props: IPacketListProps) {
  const { listData, queryListloading, onAppendFilter, loading } = props;

  const [currentPage, setCurrentPage] = useState<number>(1);
  const [pageSize, setPageSzie] = useState<number>(() => {
    return parseInt(storage.get(DEFAULT_PAGE_SIZE_KEY) || '20', 10) || PAGE_DEFAULT_SIZE;
  });
  const [tableWrapHeight, setTableWrapperHeight] = useState(200);

  // 获取表格展示的字段内容
  const getFieldValueText = (field: IField, value: any) => {
    // 区分枚举的字段
    if (field.formatterType === EFormatterType.ENUM) {
      // 取枚举值展示
      const enumValueList: IEnumValue[] = [];
      if (field.enumSource === EFieldEnumValueSource.LOCAL) {
        enumValueList.push(...(field.enumValue as IEnumValue[]));
      } else {
        const modelData = getEnumValueFromModelNext(field.enumValue as EModelAlias);
        if (modelData) {
          enumValueList.push(...modelData.list);
        }
      }

      // serviceId可能返回多个
      if (field.dataIndex === 'serviceId') {
        const serviceIdList: string[] = value?.split(',') || [];
        const serviceNameList: string[] = [];
        serviceIdList.forEach((id) => {
          const serviceInfo = enumValueList.find((item) => String(item.value) === String(id));
          serviceNameList.push(serviceInfo?.text || `[已删除: ${id}]`);
        });
        return serviceNameList.join();
      }
      // 其他的字段直接从枚举值中查找结果
      const target = enumValueList.find((item) => String(item.value) === String(value));
      // 应用查不到名称的直接返回空字符
      if (field.dataIndex === 'applicationId' && !target) {
        return '';
      }
      return target?.text || value;
    }
    if (field.formatterType === 'time') {
      return value;
      // const tmp = value.split('.');
      // return `${moment(tmp[0]).format('YYYY-MM-DD HH:mm:ss')}.${tmp[1]}`;
    }
    const renderFunc = fieldFormatterFuncMap[field.formatterType];
    if (renderFunc) {
      return renderFunc(value);
    }

    return value;
  };

  const tableColumns: ColumnProps<IPacket>[] = useMemo(
    () =>
      fieldList
        .filter((field) => !field.hide)
        .map((field) => {
          const res = {
            title: field.name,
            dataIndex: field.dataIndex,
            ...(field.width ? { width: field.width } : {}),
            ellipsis: true,
            align: 'center' as any,
            render: (value: any) => {
              const text = getFieldValueText(field, value);
              // if (conditionType === EConditionType.BPF) {
              //   return text;
              // }

              const { dataIndex, searchable } = field;

              let fieldId = '';
              // 先根据标志快速过滤
              if (searchable) {
                fieldId = dataIndex;
              } else {
                // 再根据字段特殊过滤
                switch (dataIndex) {
                  case 'ipInitiator':
                  case 'ipResponder':
                    fieldId = 'ipAddress';
                    break;
                  case 'portInitiator':
                  case 'portResponder':
                    fieldId = 'port';
                    break;
                  case 'countryIdInitiator':
                  case 'countryIdResponder':
                    fieldId = 'countryId';
                    break;
                  case 'provinceIdResponder':
                  case 'provinceIdInitiator':
                    fieldId = 'provinceId';
                    break;
                  case 'cityIdInitiator':
                  case 'cityIdResponder':
                    fieldId = 'cityId';
                    break;
                  case 'ethernetInitiator':
                  case 'ethernetResponder':
                    fieldId = 'macAddress';
                    break;
                  default:
                    break;
                }
              }

              if (!fieldId) {
                return text;
              }

              return (
                <FilterBubble
                  dataIndex={fieldId}
                  label={text}
                  operand={value}
                  fieldType={field.filterFieldType}
                  operandType={field.filterOperandType!}
                  filterSimple
                  onClick={(newFilter) => {
                    if (onAppendFilter) {
                      onAppendFilter(newFilter);
                    }
                  }}
                />
              );
            },
          };

          return res;
        }),
    [onAppendFilter],
  );

  const handlePageChange = (current: number, newPageSize: number | undefined) => {
    setCurrentPage(current);
    setPageSzie(newPageSize as number);
    storage.put(DEFAULT_PAGE_SIZE_KEY, newPageSize);
  };

  const pageProps: PaginationProps = {
    total: listData.length,
    pageSize,
    current: currentPage,
  };

  return (
    <AutoHeightContainer onHeightChange={(h) => setTableWrapperHeight(h)}>
      {/* 先不用虚拟列表了，因为虚拟列表中无法使用自定义 render 函数， 无法进行 ID->字符串的转换 */}
      <Table<IPacket>
        rowKey={() => v1()}
        className={styles['list-table']}
        columns={tableColumns}
        dataSource={listData}
        loading={loading}
        size="small"
        bordered
        pagination={pageProps}
        //  - 分页高度 - 表头高度
        scroll={{ x: 'max-content', y: tableWrapHeight - 40 - 40, scrollToFirstRowOnChange: true }}
        locale={{
          emptyText: <TableEmpty height={tableWrapHeight - 40 - 40} />,
        }}
      />
      <div className={styles['list-footer']}>
        <PacketLimit />
        <Pagination size="small" {...pageProps} onChange={handlePageChange} />
      </div>
    </AutoHeightContainer>
  );
}

export default connect(({ loading: { effects } }: IPacketConnectState) => {
  return {
    queryListloading: effects['packetModel/queryPacketList'],
  };
})(PacketList);
