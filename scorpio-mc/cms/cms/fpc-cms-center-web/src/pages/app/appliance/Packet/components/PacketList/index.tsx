import { DEFAULT_PAGE_SIZE_KEY, PAGE_DEFAULT_SIZE } from '@/common/app';
import AutoHeightContainer from '@/components/AutoHeightContainer';
import { TableEmpty } from '@/components/EnhancedTable';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import type { IEnumValue, IFilter } from '@/components/FieldFilter/typings';
import type { IFieldProperty, EModelAlias } from '@/pages/app/analysis/components/fieldsManager';
import {
  EFieldEnumValueSource,
  EFormatterType,
  fieldFormatterFuncMap,
  getEnumValueFromModelNext,
} from '@/pages/app/analysis/components/fieldsManager';
import storage from '@/utils/frame/storage';
import { Pagination, Table } from 'antd';
import type { PaginationProps } from 'antd/es/pagination';
import type { ColumnProps } from 'antd/lib/table';
import { connect } from 'dva';
import { useMemo, useState } from 'react';
import type { IField } from '../../hooks/useFieldList/useFieldList';
import useFieldList from '../../hooks/useFieldList/useFieldList';
import type { IPacket } from '../../typings';
import { EConditionType, EIpProtocol } from '../../typings';
import PacketLimit from '../PacketLimit';
import styles from './index.less';

interface IPacketListProps {
  listData: IPacket[];
  // conditionType: EConditionType;
  onAppendFilter: (filter: IFilter) => void;
  loading?: boolean;
}
function PacketList(props: IPacketListProps) {
  const { listData, onAppendFilter, loading } = props;

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

  // 这里不关心在在表格中隐藏的数据
  const fieldList = useFieldList({});

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
    [fieldList, onAppendFilter],
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
        rowKey="id"
        className={styles['list-table']}
        columns={tableColumns}
        dataSource={listData}
        size="small"
        bordered
        loading={loading}
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

export default connect()(PacketList);
