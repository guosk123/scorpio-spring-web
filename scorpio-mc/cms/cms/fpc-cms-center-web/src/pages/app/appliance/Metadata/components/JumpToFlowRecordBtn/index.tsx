import { EFilterGroupOperatorTypes, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { ServiceAnalysisContext } from '@/pages/app/analysis/Service/index';
import type { IUriParams } from '@/pages/app/analysis/typings';
import { AnalysisContext } from '@/pages/app/Network/Analysis';
import type { URLFilter } from '@/pages/app/Network/Analysis/constant';
import { jumpToAnalysisTabNew } from '@/pages/app/Network/Analysis/constant';
import { ENetworkTabs } from '@/pages/app/Network/typing';
import { isIpv4 } from '@/utils/utils';
import { Tooltip } from 'antd';
import moment from 'moment';
import { useContext } from 'react';
import { useParams, useSelector } from 'umi';
import type { IMetadataDhcp } from '../../typings';
import { EMetadataProtocol } from '../../typings';

interface Props {
  record: any;
  protocol: string;
  networkId?: string;
  serviceId?: string;
}

export default function JumpToFlowRecordBtn(props: Props) {
  const { record, protocol, networkId, serviceId } = props;
  const urlParam: IUriParams = useParams();
  const [state, dispatch] = useContext(
    urlParam.serviceId ? ServiceAnalysisContext : AnalysisContext,
  );
  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (globalState) => globalState.appModel.globalSelectedTime,
  );
  const { startTime, timestamp, flowId, srcIp, srcPort, destIp, destPort } = record;
  // 没有flow_packet_id时，无法下载和在线分析
  if (!flowId || flowId === '0') {
    return null;
  }

  // 判断元数据开始时间到现在的时间间隔是否在1小时内，
  // 在1小时内的话提示下可能还未生成
  const isOverOneHours = Math.abs(moment(startTime || timestamp).diff(moment(), 'hours', true)) > 1;
  // 根据五元组进行过滤流日志
  const srcIpIsV4 = isIpv4(srcIp);
  const destIpIsV4 = isIpv4(destIp);
  // ICMP没有端口的概念
  const filterParam: URLFilter[] =
    protocol !== EMetadataProtocol.ICMPV4 && protocol !== EMetadataProtocol.ICMPV6
      ? [
          { field: 'port_initiator', operator: EFilterOperatorTypes.EQ, operand: srcPort },
          { field: 'port_responder', operator: EFilterOperatorTypes.EQ, operand: destPort },
        ]
      : [];

  if (typeof record.networkId === 'string') {
    filterParam.push({
      field: 'network_id',
      operand: record.networkId,
      operator: EFilterOperatorTypes.EQ,
    });
  } else {
    if (record?.networkId?.length > 0) {
      filterParam.push({
        field: 'network_id',
        operator: EFilterOperatorTypes.EQ,
        operand: record.networkId[0],
      });
    }
  }

  if (protocol === EMetadataProtocol.DHCP) {
    filterParam.push({
      group: [
        {
          group: [
            {
              field: 'ipv4_initiator',
              operator: EFilterOperatorTypes.EQ,
              operand: (record as IMetadataDhcp).srcIpv4,
            },
            {
              field: 'ipv4_responder',
              operator: EFilterOperatorTypes.EQ,
              operand: (record as IMetadataDhcp).destIpv4,
            },
          ],
          operator: EFilterGroupOperatorTypes.AND,
        },
        {
          group: [
            {
              field: 'ipv4_initiator',
              operator: EFilterOperatorTypes.EQ,
              operand: (record as IMetadataDhcp).destIpv4,
            },
            {
              field: 'ipv4_responder',
              operator: EFilterOperatorTypes.EQ,
              operand: (record as IMetadataDhcp).srcIpv4,
            },
          ],
          operator: EFilterGroupOperatorTypes.AND,
        },
      ],
      operator: EFilterGroupOperatorTypes.OR,
    } as any);
  } else if (protocol === EMetadataProtocol.DHCPV6) {
    filterParam.push({
      group: [
        {
          group: [
            {
              field: 'ipv6_initiator',
              operator: EFilterOperatorTypes.EQ,
              operand: (record as IMetadataDhcp).srcIpv6,
            },
            {
              field: 'ipv6_responder',
              operator: EFilterOperatorTypes.EQ,
              operand: (record as IMetadataDhcp).destIpv6,
            },
          ],
          operator: EFilterGroupOperatorTypes.AND,
        },
        {
          group: [
            {
              field: 'ipv6_initiator',
              operator: EFilterOperatorTypes.EQ,
              operand: (record as IMetadataDhcp).destIpv6,
            },
            {
              field: 'ipv6_responder',
              operator: EFilterOperatorTypes.EQ,
              operand: (record as IMetadataDhcp).srcIpv6,
            },
          ],
          operator: EFilterGroupOperatorTypes.AND,
        },
      ],
      operator: EFilterGroupOperatorTypes.OR,
    } as any);
  } else if (protocol === EMetadataProtocol.ARP) {
    filterParam.push({
      group: [
        {
          group: [
            {
              field: 'ipv4_initiator',
              operator: EFilterOperatorTypes.EQ,
              operand: (record as IMetadataDhcp).srcIp,
            },
            {
              field: 'ipv4_responder',
              operator: EFilterOperatorTypes.EQ,
              operand: (record as IMetadataDhcp).destIp,
            },
          ],
          operator: EFilterGroupOperatorTypes.AND,
        },
        {
          group: [
            {
              field: 'ipv4_initiator',
              operator: EFilterOperatorTypes.EQ,
              operand: (record as IMetadataDhcp).destIp,
            },
            {
              field: 'ipv4_responder',
              operator: EFilterOperatorTypes.EQ,
              operand: (record as IMetadataDhcp).srcIp,
            },
          ],
          operator: EFilterGroupOperatorTypes.AND,
        },
      ],
      operator: EFilterGroupOperatorTypes.OR,
    } as any);
  } else {
    filterParam.push(
      {
        field: srcIpIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
        operator: EFilterOperatorTypes.EQ,
        operand: srcIp,
      },
      {
        field: destIpIsV4 ? 'ipv4_responder' : 'ipv6_responder',
        operator: EFilterOperatorTypes.EQ,
        operand: destIp,
      },
    );
  }

  const linkDom = (
    <span
      className="link"
      onClick={() => {
        jumpToAnalysisTabNew(state, dispatch, ENetworkTabs.FLOWRECORD, {
          filter: filterParam,
          flowId,
          networkId,
          serviceId,
          globalSelectedTime: {
            startTime: globalSelectedTime.startTime,
            endTime: globalSelectedTime.endTime,
          },
        });
      }}
    >
      会话详单
    </span>
  );
  return (
    <>
      {/* 判断开始时间 */}
      {isOverOneHours ? linkDom : <Tooltip title="会话详单可能还未生成">{linkDom}</Tooltip>}
    </>
  );
}
