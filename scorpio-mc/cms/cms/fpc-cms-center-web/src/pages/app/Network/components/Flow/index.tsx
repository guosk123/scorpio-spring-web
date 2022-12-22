import { createContext, Fragment, useContext } from 'react';
import EditTabs, { clearShareInfo, getTabDetail } from '../EditTabs';
import { flowTabs } from './constant';
import { history } from 'umi';
import FieldFilter from '@/components/FieldFilter';
import type {
  EFieldOperandType,
  EFieldType,
  IEnumValue,
  IFilter,
  IFilterCondition,
} from '@/components/FieldFilter/typings';
import { EFilterGroupOperatorTypes, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { deduplicateCondition } from '@/components/FieldFilter/utils';
import { isIpv4, parseArrayJson, snakeCase } from '@/utils/utils';
import React, { useEffect, useState } from 'react';
import { useLocation } from 'umi';
import type { EModelAlias } from '@/pages/app/analysis/components/fieldsManager';
import {
  EFieldEnumValueSource,
  EFormatterType,
  fieldsMapping,
  getEnumValueFromModelNext,
} from '@/pages/app/analysis/components/fieldsManager';
import { connect, useParams } from 'umi';
import LinkToFlowTab from './components/LinkToFlowTab';
import type { IUriParams } from '@/pages/app/analysis/typings';
import { ServiceAnalysisContext } from '@/pages/app/analysis/Service/index';
import { AnalysisContext } from '../../Analysis';
import { DimensionsSearchContext } from '@/pages/app/GlobalSearch/DimensionsSearch/SeartchTabs';
import {
  DimensionsTypeToFlowFilterMap,
  EDimensionsSearchType,
} from '@/pages/app/GlobalSearch/DimensionsSearch/typing';
import { dimensionsUrl } from '@/pages/app/GlobalSearch/DimensionsSearch/SeartchTabs/constant';

interface IFilterContextType {
  filterCondition: IFilter[];
  addConditionToFilter?: (condition: IFilterCondition) => void;
}

export const FilterContext = React.createContext<IFilterContextType>({ filterCondition: [] });

// TODO: 确定过滤器支持的条件类别
// 流量分析下各子类别支持的过滤条件
export const filterSelectValue = [
  // 地区下的条件
  'countryId',
  'provinceId',
  // 'cityId',
  // 应用下的条件
  'applicationId',
  'subcategoryId',
  'categoryId',
  // 应用层协议下的条件
  'l7ProtocolId',
  // 端口下的条件
  'port',
  'ipProtocol',
  // IP地址组的条件
  'hostgroupId',
  // MAC地址下的条件
  'macAddress',
  'ethernetType',
  // IP地址下的条件
  'ipAddress',
  'ipLocality',
];

export const filterFields = filterSelectValue.map((field) => {
  const { formatterType, name, filterOperandType, filterFieldType, enumSource, enumValue } =
    fieldsMapping[field];
  const isEnum = formatterType === EFormatterType.ENUM;
  const enumValueList: IEnumValue[] = [];
  if (isEnum) {
    if (enumSource === EFieldEnumValueSource.LOCAL) {
      enumValueList.push(...(enumValue as IEnumValue[]));
    } else {
      const modelData = getEnumValueFromModelNext(enumValue as EModelAlias);
      if (modelData) {
        enumValueList.push(...modelData.list);
      }
    }
  }
  return {
    title: name,
    dataIndex: snakeCase(field),
    operandType: filterOperandType as EFieldOperandType,
    type: filterFieldType as EFieldType,
    ...(isEnum
      ? {
          enumValue: enumValueList,
        }
      : {}),
  };
});

export const flowAnalysisFilterToFlowRecordFilter = (filter: IFilter[]) => {
  const fullFilter: IFilterCondition = [];
  filter.forEach((condition) => {
    const { field, operator, operand } = condition;

    if (field === 'country_id') {
      fullFilter.push({
        operator: EFilterGroupOperatorTypes.OR,
        group: [
          { field: 'country_id_initiator', operator, operand: String(operand) },
          { field: 'country_id_responder', operator, operand: String(operand) },
        ],
      });
    } else if (field === 'province_id') {
      fullFilter.push({
        operator: EFilterGroupOperatorTypes.OR,
        group: [
          { field: 'province_id_initiator', operator, operand: String(operand) },
          { field: 'province_id_responder', operator, operand: String(operand) },
        ],
      });
    } else if (field === 'application_id') {
      fullFilter.push({
        field,
        operator,
        operand: String(operand),
      });
    } else if (field === 'l7_protocol_id') {
      fullFilter.push({
        field,
        operator,
        operand: String(operand),
      });
    } else if (field === 'port') {
      fullFilter.push({
        operator: EFilterGroupOperatorTypes.OR,
        group: [
          { field: 'port_initiator', operator, operand: String(operand) },
          { field: 'port_responder', operator, operand: String(operand) },
        ],
      });
    } else if (field === 'ip_protocol') {
      fullFilter.push({
        field,
        operator,
        operand: String(operand),
      });
    } else if (field === 'hostgroup_id') {
      fullFilter.push({
        operator: EFilterGroupOperatorTypes.OR,
        group: [
          {
            field: 'hostgroup_id_initiator',
            operator: EFilterOperatorTypes.EQ,
            operand: String(operand),
          },
          {
            field: 'hostgroup_id_responder',
            operator: EFilterOperatorTypes.EQ,
            operand: String(operand),
          },
        ],
      });
    } else if (field === 'mac_address') {
      fullFilter.push({
        operator: EFilterGroupOperatorTypes.OR,
        group: [
          {
            field: 'ethernet_initiator',
            operator,
            operand: String(operand),
          },
          {
            field: 'ethernet_responder',
            operator,
            operand: String(operand),
          },
        ],
      });
    } else if (field === 'ethernet_type') {
      fullFilter.push({
        field,
        operator,
        operand: String(operand),
      });
    } else if (field === 'ip_address') {
      const isV4 = isIpv4(operand as string);
      fullFilter.push({
        operator: EFilterGroupOperatorTypes.OR,
        group: [
          {
            field: isV4 ? 'ipv4_initiator' : 'ipv6_initiator',
            operator,
            operand: String(operand),
          },
          {
            field: isV4 ? 'ipv4_responder' : 'ipv6_responder',
            operator,
            operand: String(operand),
          },
        ],
      });
    } else if (field === 'ip_locality') {
      fullFilter.push({
        operator: EFilterGroupOperatorTypes.OR,
        group: [
          {
            field: 'ip_locality_initiator',
            operator,
            operand: String(operand),
          },
          {
            field: 'ip_locality_responder',
            operator,
            operand: String(operand),
          },
        ],
      });
    }
  });
  return fullFilter;
};

/**
 * 计算下钻的标志
 * 过滤条件中不全是当前统计类型主键字段，便需要下钻
 * @param flowAnalysisType 统计类型
 * @param filter 过滤条件数组
 */
export const computedDrilldownFlag = (filter: IFilterCondition) => {
  // 没有过滤条件时，不需要下钻
  if (filter.length === 0) {
    return false;
  }

  return true;
};

export const FlowContext = createContext([]);

interface Props {
  dispatch: any;
  dimensinosTabType?: string;
}

function Flow(props: Props) {
  const { dispatch, dimensinosTabType } = props;
  const isDimensionsTab = history.location.pathname.includes(dimensionsUrl);
  const { serviceId } = useParams() as IUriParams;
  const [loading, setLoading] = useState(true);
  const [flowState, flowDispatch] = useContext(
    (() => {
      if (isDimensionsTab) {
        return DimensionsSearchContext;
      }
      return serviceId ? ServiceAnalysisContext : AnalysisContext;
    })(),
  );

  useEffect(() => {
    clearShareInfo(flowDispatch);
  }, [flowDispatch]);
  const { shareInfo } = flowState;

  useEffect(() => {
    dispatch({
      type: 'ipAddressGroupModel/queryAllIpAddressGroup',
    });
  }, [dispatch]);

  const location = useLocation() as any as {
    query: { filter: string };
  };

  const flowAnalysisDetail = getTabDetail(flowState) || {};
  // filter过滤条件
  const [filterCondition, setFilterCondition] = useState<IFilter[]>(() => {
    if (isDimensionsTab) {
      let tmpFilter: any = [];
      // 地区需要特殊处理
      if (
        flowAnalysisDetail?.searchBoxInfo?.dimensionsSearchType === EDimensionsSearchType.LOCATION
      ) {
        const locationField = ['country_id', 'province_id', 'city_id'];
        const locationOperandArr = flowAnalysisDetail?.searchBoxInfo?.content.split('_');
        tmpFilter = [
          {
            field: locationField[locationOperandArr.length - 1],
            operator: EFilterOperatorTypes.EQ,
            operand: locationOperandArr.pop(),
          },
        ];
      } else if (
        flowAnalysisDetail?.searchBoxInfo?.dimensionsSearchType ===
        EDimensionsSearchType.IPCONVERSATION
      ) {
        const ips = flowAnalysisDetail?.searchBoxInfo?.content.split('-');
        tmpFilter = [
          {
            field:
              DimensionsTypeToFlowFilterMap[
                flowAnalysisDetail?.searchBoxInfo?.dimensionsSearchType
              ],
            operator: EFilterOperatorTypes.EQ,
            operand: ips[0],
          },
          {
            field:
              DimensionsTypeToFlowFilterMap[
                flowAnalysisDetail?.searchBoxInfo?.dimensionsSearchType
              ],
            operator: EFilterOperatorTypes.EQ,
            operand: ips[1],
          },
        ];
      } else {
        tmpFilter = [
          {
            field:
              DimensionsTypeToFlowFilterMap[
                flowAnalysisDetail?.searchBoxInfo?.dimensionsSearchType
              ],
            operator: EFilterOperatorTypes.EQ,
            operand: flowAnalysisDetail?.searchBoxInfo?.content,
          },
        ];
      }

      return tmpFilter.concat(
        ((shareInfo as any) || []).filter((item: any) => {
          const res: boolean[] = [];
          tmpFilter.forEach((element: any) => {
            res.push(JSON.stringify(item) !== JSON.stringify(element));
          });
          return res.reduce((l, r) => l && r);
        }),
      );
    }
    const { filter } = location.query;
    const uriFilterData: IFilter[] = parseArrayJson(filter);
    return deduplicateCondition(uriFilterData, new Set()) as IFilter[];
  });

  const handleFilterChange = (newFilter: IFilterCondition) => {
    setFilterCondition(newFilter as IFilter[]);
  };

  const addConditionToFilter = (condition: IFilterCondition) => {
    setFilterCondition((prevCondition) => {
      // console.log([...prevCondition, ...condition]);
      return [...prevCondition, ...(condition as IFilter[])];
    });
  };

  return (
    <Fragment>
      <div style={{ marginBottom: 10, marginTop: 5 }}>
        <FieldFilter
          fields={filterFields}
          onChange={handleFilterChange}
          condition={filterCondition}
          historyStorageKey="tfa-flow-record-filter-history"
          simple
        />
      </div>
      <FilterContext.Provider value={{ filterCondition, addConditionToFilter }}>
        {isDimensionsTab ? (
          flowTabs[dimensinosTabType || '']?.content
        ) : (
          <EditTabs
            tabs={flowTabs}
            loading={loading}
            // 相对时间的话，tab页每次切换都去获取最新的
            // destroyInactiveTabPane={history.location.query?.relative === 'true'}
            linkToTab={
              <LinkToFlowTab
                onJumpDone={() => {
                  setLoading(false);
                }}
              />
            }
            destroyInactiveTabPane={true}
            consumerContext={FlowContext}
          />
        )}
      </FilterContext.Provider>
    </Fragment>
  );
}
export default connect()(Flow);
