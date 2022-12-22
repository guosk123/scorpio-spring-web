import { EMetricApiType } from '@/common/api/analysis';
import type { IFilterCondition, IFilter, IFilterGroup } from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes, EFilterGroupOperatorTypes } from '@/components/FieldFilter/typings';
import { isIpv4 } from '@/utils/utils';
import { flowAnalysisFilterToFlowRecordFilter } from '../PageLayoutWithFilter';
import type { IActionLinkParams } from './typing';

/** 删除重复的 */
export const removeRepeatConditon = (filter: IFilter[], target: IFilter) => {
  const repeatIndex = filter.map((condition, index) => {
    if (
      condition.field === target.field &&
      condition.operator === target.operator &&
      String(condition.operand) === String(target.operand)
    ) {
      return index;
    }
    return -1;
  });
  repeatIndex.forEach((index) => {
    if (index !== -1) {
      filter.splice(index, 1);
    }
  });
};

/**
 * 生成详单跳转按钮
 */
export const getFlowRecordLink = ({
  type,
  record,
  filter,
  // serviceId,
  // networkId,
}: IActionLinkParams) => {
  // 拼接全部的 filter
  const fullFilter: IFilterCondition = [];
  const copyFilter = [...filter];
  // 地区
  if (type === EMetricApiType.location) {
    const { countryId = '', provinceId = '' } = record;

    if (provinceId) {
      const targetFilter: IFilter = {
        field: 'province_id',
        operator: EFilterOperatorTypes.EQ,
        operand: provinceId,
      };
      removeRepeatConditon(copyFilter, targetFilter);
      fullFilter.push({
        operator: EFilterGroupOperatorTypes.OR,
        group: [
          {
            field: 'province_id_initiator',
            operator: EFilterOperatorTypes.EQ,
            operand: String(provinceId),
          },
          {
            field: 'province_id_responder',
            operator: EFilterOperatorTypes.EQ,
            operand: String(provinceId),
          },
        ],
      });
    } else {
      const targetFilter: IFilter = {
        field: 'country_id',
        operator: EFilterOperatorTypes.EQ,
        operand: countryId,
      };
      removeRepeatConditon(copyFilter, targetFilter);
      fullFilter.push({
        operator: EFilterGroupOperatorTypes.OR,
        group: [
          {
            field: 'country_id_initiator',
            operator: EFilterOperatorTypes.EQ,
            operand: String(countryId),
          },
          {
            field: 'country_id_responder',
            operator: EFilterOperatorTypes.EQ,
            operand: String(countryId),
          },
        ],
      });
    }

    // if (cityId) {
    // 暂时不显示城市，城市后端可能存在问题
    //   initiatorFilter.group.push({
    //     field: 'city_id_initiator',
    //     operator: EFilterOperatorTypes.EQ,
    //     operand: String(cityId),
    //   });

    //   responderFilter.group.push({
    //     field: 'city_id_responder',
    //     operator: EFilterOperatorTypes.EQ,
    //     operand: String(cityId),
    //   });
    // }
  } else if (type === EMetricApiType.application) {
    const { applicationId } = record;
    const targetFilter: IFilter = {
      field: 'application_id',
      operator: EFilterOperatorTypes.EQ,
      operand: String(applicationId),
    };
    removeRepeatConditon(copyFilter, targetFilter);
    fullFilter.push(targetFilter);
  } else if (type === EMetricApiType.protocol) {
    const { l7ProtocolId } = record;
    const targetFilter: IFilter = {
      field: 'l7_protocol_id',
      operator: EFilterOperatorTypes.EQ,
      operand: String(l7ProtocolId),
    };
    removeRepeatConditon(copyFilter, targetFilter);
    fullFilter.push(targetFilter);
  } else if (type === EMetricApiType.port) {
    const { port } = record;
    const targetFilter: IFilter = {
      field: 'port',
      operator: EFilterOperatorTypes.EQ,
      operand: port,
    };
    removeRepeatConditon(copyFilter, targetFilter);
    fullFilter.push({
      operator: EFilterGroupOperatorTypes.OR,
      group: [
        { field: 'port_initiator', operator: EFilterOperatorTypes.EQ, operand: port },
        { field: 'port_responder', operator: EFilterOperatorTypes.EQ, operand: port },
      ],
    });
  } else if (type === EMetricApiType.hostGroup) {
    const { hostgroupId } = record;
    const targetFilter: IFilter = {
      field: 'hostgroup_id',
      operator: EFilterOperatorTypes.EQ,
      operand: hostgroupId,
    };
    removeRepeatConditon(copyFilter, targetFilter);
    fullFilter.push({
      operator: EFilterGroupOperatorTypes.OR,
      group: [
        {
          field: 'hostgroup_id_initiator',
          operator: EFilterOperatorTypes.EQ,
          operand: hostgroupId,
        },
        {
          field: 'hostgroup_id_responder',
          operator: EFilterOperatorTypes.EQ,
          operand: hostgroupId,
        },
      ],
    });
  } else if (type === EMetricApiType.macAddress) {
    const { macAddress } = record;
    const targetFilter: IFilter = {
      field: 'mac_address',
      operator: EFilterOperatorTypes.EQ,
      operand: macAddress,
    };
    removeRepeatConditon(copyFilter, targetFilter);
    fullFilter.push({
      operator: EFilterGroupOperatorTypes.OR,
      group: [
        {
          field: 'ethernet_initiator',
          operator: EFilterOperatorTypes.EQ,
          operand: macAddress,
        },
        {
          field: 'ethernet_responder',
          operator: EFilterOperatorTypes.EQ,
          operand: macAddress,
        },
      ],
    });
  } else if (type === EMetricApiType.ipAddress) {
    const { ipAddress } = record;
    const targetFilter: IFilter = {
      field: 'ip_address',
      operator: EFilterOperatorTypes.EQ,
      operand: ipAddress,
    };
    removeRepeatConditon(copyFilter, targetFilter);
    const ipAddressIsV4 = isIpv4(ipAddress as string);
    fullFilter.push({
      operator: EFilterGroupOperatorTypes.OR,
      group: [
        {
          field: ipAddressIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
          operator: EFilterOperatorTypes.EQ,
          operand: ipAddress,
        },
        {
          field: ipAddressIsV4 ? 'ipv4_responder' : 'ipv6_responder',
          operator: EFilterOperatorTypes.EQ,
          operand: ipAddress,
        },
      ],
    });
  } else if (type === EMetricApiType.ipConversation) {
    const { ipAAddress, ipBAddress } = record;
    const aIsV4 = isIpv4(ipAAddress as string);
    const bIsV4 = isIpv4(ipBAddress as string);
    const condition1: IFilterGroup = {
      operator: EFilterGroupOperatorTypes.AND,
      group: [
        {
          field: aIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
          operator: EFilterOperatorTypes.EQ,
          operand: ipAAddress,
        },
        {
          field: bIsV4 ? 'ipv4_responder' : 'ipv6_responder',
          operator: EFilterOperatorTypes.EQ,
          operand: ipBAddress,
        },
      ],
    };
    const condition2: IFilterGroup = {
      operator: EFilterGroupOperatorTypes.AND,
      group: [
        {
          field: bIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
          operator: EFilterOperatorTypes.EQ,
          operand: ipBAddress,
        },
        {
          field: aIsV4 ? 'ipv4_responder' : 'ipv6_responder',
          operator: EFilterOperatorTypes.EQ,
          operand: ipAAddress,
        },
      ],
    };
    fullFilter.push({
      operator: EFilterGroupOperatorTypes.OR,
      group: [condition1, condition2],
    });
  }

  if (copyFilter.length > 0) {
    const result = flowAnalysisFilterToFlowRecordFilter(copyFilter);
    fullFilter.push(...result);
  }

  // let urlPrefix = `/analysis/network/${networkId}`;
  // if (serviceId) {
  //   urlPrefix = `/analysis/service/${serviceId}/${networkId}`;
  // }
  return fullFilter;
  // return JSON.stringify(fullFilter);

  // return getLinkUrl(
  //   `${urlPrefix}/flow-record?filter=${encodeURIComponent(JSON.stringify(fullFilter))}`,
  // );
};
