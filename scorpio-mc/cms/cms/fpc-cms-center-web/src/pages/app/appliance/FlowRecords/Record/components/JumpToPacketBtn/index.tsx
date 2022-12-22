import { jumpToAnalysisTabNew } from '@/pages/app/Network/Analysis/constant';
import { FolderOutlined } from '@ant-design/icons';
import { Button } from 'antd';
import { useContext, useMemo } from 'react';
import { AnalysisContext } from '@/pages/app/Network/Analysis';
import { ENetworkTabs } from '@/pages/app/Network/typing';
import { ServiceAnalysisContext } from '@/pages/app/analysis/Service/index';
import { useParams, useSelector } from 'umi';
import type { IUriParams } from '@/pages/app/analysis/typings';
import type { ConnectState } from '@/models/connect';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import network from '@/models/app/network';

const packetSupportFilterType = [
  'ipv4_initiator',
  'ipv4_responder',
  'ipv6_initiator',
  'ipv6_responder',
  'port_initiator',
  'port_responder',
  // 'ip_protocol',单独处理
  'l7_protocol_id',
  'vlan_id',
  'country_id_initiator',
  'country_id_responder',
  'province_id_initiator',
  'province_id_responder',
  'city_id_initiator',
  'city_id_responder',
  'ethernet_initiator',
  'ethernet_responder',
  'application_id',
];

export const flowRecordFilterToUrl = (urlInfo: {
  filterCondition: any;
  serviceId: string;
  networkId: string;
  startTime: any;
  endTime: any;
}) => {
  const { filterCondition, networkId, serviceId } = urlInfo;
  console.log(filterCondition, 'filterCondition');
  // 数据包页面支持的过滤类型

  const filterNetworkId = filterCondition.find((item: any) => item.field === 'network_id');
  const filterServiceId = filterCondition.find((item: any) => item.field === 'service_id');

  const networkFilter = [];
  if (networkId) {
    networkFilter.push({
      field: 'network_id',
      operator: EFilterOperatorTypes.EQ,
      operand: networkId || '',
    });
  }
  if (filterNetworkId && !networkFilter.length) {
    networkFilter.push(filterNetworkId);
  }

  const serviceFilter = [];
  if (serviceId) {
    serviceFilter.push({
      field: 'service_id',
      operator: EFilterOperatorTypes.EQ,
      operand: serviceId || '',
    });
  }

  if (filterServiceId && !serviceFilter.length) {
    serviceFilter.push(filterServiceId);
  }

  // 对过滤条件进行过滤
  const packetSupportFilter = filterCondition
    .map((sub: any) => {
      if (!sub.hasOwnProperty('operand') && sub.group.length < 2) {
        return sub.group[0];
      }
      return sub;
    })
    .filter(
      (item: any) =>
        item.hasOwnProperty('operand') &&
        ((item.field === 'ip_protocol' && ['tcp', 'udp', 'icmp', 'sctp'].includes(item.operand)) ||
          (packetSupportFilterType.includes(item.field) && item.operator === '=')),
    )
    // 整理过滤条件结构
    .map((ele: any) => ({
      field:
        (['ipv4_initiator', 'ipv4_responder', 'ipv6_initiator', 'ipv6_responder'].includes(
          ele.field,
        ) &&
          'ipAddress') ||
        (['port_initiator', 'port_responder'].includes(ele.field) && 'port') ||
        (['country_id_initiator', 'country_id_responder'].includes(ele.field) && 'countryId') ||
        (['province_id_initiator', 'province_id_responder'].includes(ele.field) && 'provinceId') ||
        (['city_id_initiator', 'city_id_responder'].includes(ele.field) && 'cityId') ||
        (['ethernet_initiator', 'ethernet_responder'].includes(ele.field) && 'macAddress') ||
        (ele.field === 'ip_protocol' && 'ipProtocol') ||
        (ele.field === 'l7_protocol_id' && 'l7ProtocolId') ||
        (ele.field === 'vlan_id' && 'vlanId') ||
        (ele.field === 'application_id' && 'applicationId'),
      operator: ele.operator,
      operand: ele.field === 'ip_protocol' ? ele.operand.toLocaleUpperCase() : ele.operand,
    }));
  return [...packetSupportFilter, ...networkFilter, ...serviceFilter];
  // return JSON.stringify(packetSupportFilter);
};

export default function JumpToPacketBtn(props: any) {
  const { info } = props;
  console.log(info, 'info');
  const { networkId, serviceId }: IUriParams = useParams();
  const [state, dispatch] = useContext(serviceId ? ServiceAnalysisContext : AnalysisContext);
  const filterArr = flowRecordFilterToUrl(info) as any[];
  console.log(filterArr, 'filterArr');
  // if (networkId) {
  //   filterArr.push({
  //     field: 'network_id',
  //     operator: EFilterOperatorTypes.EQ,
  //     operand: networkId || '',
  //   });
  // }
  // if (serviceId) {
  //   filterArr.push({
  //     field: 'service_id',
  //     operator: EFilterOperatorTypes.EQ,
  //     operand: serviceId || '',
  //   });
  // }

  return (
    <Button
      icon={<FolderOutlined />}
      type="primary"
      onClick={() => {
        // 数据包页面支持的过滤类型
        jumpToAnalysisTabNew(state, dispatch, ENetworkTabs.PACKET, {
          filter: flowRecordFilterToUrl(info),
          globalSelectedTime: {
            startTime: info.startTime,
            endTime: info.endTime,
          },
        });
      }}
    >
      数据包
    </Button>
  );
}
