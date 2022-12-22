import type { IFilter, IFilterGroup } from '@/components/FieldFilter/typings';
import { EFilterGroupOperatorTypes, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { getLinkUrl, isIpv4, jumpNewPage } from '@/utils/utils';
import { Tooltip } from 'antd';
import moment from 'moment';
import { useSelector } from 'umi';
import { getOriginTime } from '../../../Packet';
import type { IL7ProtocolMap, IMetadataArp, IMetadataDhcp } from '../../typings';
import { EMetadataProtocol } from '../../typings';
import { timeScale } from '../utils';

interface Props {
  record: any;
  protocol: any;
  selectedTimeInfo: any;
  pcapFileId: any;
  analysisResultId: string;
}

export default function JumpToRecordFromMeta(props: Props) {
  const { record, protocol, selectedTimeInfo, pcapFileId, analysisResultId } = props;
  const allL7ProtocolMap = useSelector<ConnectState, IL7ProtocolMap>(
    (state) => state.metadataModel.allL7ProtocolMap,
  );
  const { startTime, timestamp, flowId, srcIp, srcPort, destIp, destPort } = record;
  // 没有flow_packet_id时，无法下载和在线分析
  if (!flowId || flowId === '0') {
    return null;
  }
  const newEndTime = moment(startTime || timestamp).add(1, 'h');
  const newStartTime = moment(startTime || timestamp).subtract(1, 'h');
  // 如果页面是在场景分析结果中，跳转到详单即可
  if (location.pathname.includes('/scenario-task/result/')) {
    return (
      <span
        className="link"
        onClick={() => {
          jumpNewPage(
            getLinkUrl(
              `/analysis/security/scenario-task/result/flow-record?analysisResultId=${analysisResultId}&analysisStartTime=${encodeURIComponent(
                newStartTime.format()!,
              )}&analysisEndTime=${encodeURIComponent(newEndTime.format()!)}&flowId=${flowId}`,
            ),
          );
        }}
      >
        会话详单
      </span>
    );
  }

  // 判断元数据开始时间到现在的时间间隔是否在1小时内，
  // 在1小时内的话提示下可能还未生成
  const isOverOneHours = Math.abs(moment(startTime).diff(moment(), 'hours', true)) > 1;
  // 根据五元组进行过滤流日志

  const srcIpIsV4 = srcIp && isIpv4(srcIp);
  const destIpIsV4 = destIp && isIpv4(destIp);
  // ICMP没有端口的概念
  const filterParam: (IFilter | IFilterGroup)[] = [];

  if (typeof record.networkId === 'string') {
    filterParam.push({
      field: 'network_id',
      operator: EFilterOperatorTypes.EQ,
      operand: record.networkId,
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
  if (protocol !== EMetadataProtocol.ICMPV4 && protocol !== EMetadataProtocol.ICMPV6) {
    if (srcPort) {
      filterParam.push({
        field: 'port_initiator',
        operator: EFilterOperatorTypes.EQ,
        operand: srcPort,
      });
    }
    if (destPort) {
      filterParam.push({
        field: 'port_responder',
        operator: EFilterOperatorTypes.EQ,
        operand: destPort,
      });
    }
  }

  const { applicationName } = record as any;
  const l7protocolFilter: IFilterGroup = {
    group: [],
    operator: EFilterGroupOperatorTypes.OR,
  };
  if (applicationName) {
    let l7_protocol_id = '';
    for (const key in allL7ProtocolMap) {
      if (allL7ProtocolMap[key].name === applicationName) {
        l7_protocol_id = key;
        break;
      }
    }
    if (l7_protocol_id) {
      l7protocolFilter.group.push({
        field: 'l7_protocol_id',
        operator: EFilterOperatorTypes.EQ,
        operand: l7_protocol_id,
      });
    }
  }

  const srcInitiator: IFilter[] = [];
  const srcResponder: IFilter[] = [];
  const destInitaitor: IFilter[] = [];
  const destResponder: IFilter[] = [];

  if (protocol === EMetadataProtocol.DHCP) {
    if ((record as IMetadataDhcp).srcIpv4) {
      srcInitiator.push({
        field: 'ipv4_initiator',
        operator: EFilterOperatorTypes.EQ,
        operand: (record as IMetadataDhcp).srcIpv4,
      });
      srcResponder.push({
        field: 'ipv4_responder',
        operator: EFilterOperatorTypes.EQ,
        operand: (record as IMetadataDhcp).srcIpv4,
      });
    }

    if ((record as IMetadataDhcp).destIpv4) {
      destInitaitor.push({
        field: 'ipv4_initiator',
        operator: EFilterOperatorTypes.EQ,
        operand: (record as IMetadataDhcp).destIpv4,
      });
      destResponder.push({
        field: 'ipv4_responder',
        operator: EFilterOperatorTypes.EQ,
        operand: (record as IMetadataDhcp).destIpv4,
      });
    }
    filterParam.push({
      group: [
        {
          group: [...srcInitiator, ...destResponder],
          operator: EFilterGroupOperatorTypes.AND,
        },
        {
          group: [...destInitaitor, ...srcResponder],
          operator: EFilterGroupOperatorTypes.AND,
        },
      ],
      operator: EFilterGroupOperatorTypes.OR,
    });
  } else if (protocol === EMetadataProtocol.DHCPV6) {
    if ((record as IMetadataDhcp).srcIpv6) {
      srcInitiator.push({
        field: 'ipv6_initiator',
        operator: EFilterOperatorTypes.EQ,
        operand: (record as IMetadataDhcp).srcIpv6,
      });
      srcResponder.push({
        field: 'ipv6_responder',
        operator: EFilterOperatorTypes.EQ,
        operand: (record as IMetadataDhcp).srcIpv6,
      });
    }

    if ((record as IMetadataDhcp).destIpv6) {
      destInitaitor.push({
        field: 'ipv6_initiator',
        operator: EFilterOperatorTypes.EQ,
        operand: (record as IMetadataDhcp).destIpv6,
      });
      destResponder.push({
        field: 'ipv6_responder',
        operator: EFilterOperatorTypes.EQ,
        operand: (record as IMetadataDhcp).destIpv6,
      });
    }

    filterParam.push({
      group: [
        {
          group: [...srcInitiator, ...destResponder],
          operator: EFilterGroupOperatorTypes.AND,
        },
        {
          group: [...destInitaitor, ...srcResponder],
          operator: EFilterGroupOperatorTypes.AND,
        },
      ],
      operator: EFilterGroupOperatorTypes.OR,
    });
  } else if (protocol === EMetadataProtocol.ARP) {
    if ((record as IMetadataArp).srcIp) {
      srcInitiator.push({
        field: 'ipv4_initiator',
        operator: EFilterOperatorTypes.EQ,
        operand: record.srcIp,
      });
      srcResponder.push({
        field: 'ipv4_responder',
        operator: EFilterOperatorTypes.EQ,
        operand: record.srcIp,
      });
    }

    if ((record as IMetadataArp).destIp) {
      destInitaitor.push({
        field: 'ipv4_initiator',
        operator: EFilterOperatorTypes.EQ,
        operand: record.destIp,
      });
      destResponder.push({
        field: 'ipv4_responder',
        operator: EFilterOperatorTypes.EQ,
        operand: record.destIp,
      });
    }

    filterParam.push({
      group: [
        {
          group: [...srcInitiator, ...destResponder],
          operator: EFilterGroupOperatorTypes.AND,
        },
        {
          group: [...destInitaitor, ...srcResponder],
          operator: EFilterGroupOperatorTypes.AND,
        },
      ],
      operator: EFilterGroupOperatorTypes.OR,
    });
  } else {
    if (srcIp) {
      filterParam.push({
        field: srcIpIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
        operator: EFilterOperatorTypes.EQ,
        operand: srcIp,
      });
    }
    if (destIp) {
      filterParam.push({
        field: destIpIsV4 ? 'ipv4_responder' : 'ipv6_responder',
        operator: EFilterOperatorTypes.EQ,
        operand: destIp,
      });
    }
  }
  if (l7protocolFilter?.group?.length > 0) {
    filterParam.push(l7protocolFilter);
  }

  const linkDom = (
    <span
      className="link"
      onClick={() => {
        const scaleTime = timeScale(
          record.startTime,
          new Date(getOriginTime(selectedTimeInfo, 'start') as string).getTime(),
          new Date(getOriginTime(selectedTimeInfo, 'end') as string).getTime(),
        );

        let url = '';
        if (pcapFileId) {
          url = getLinkUrl(`/analysis/offline/${pcapFileId}/flow-record`);
        } else {
          url = getLinkUrl(`/analysis/trace/flow-record`);
        }

        url = `${url}?filter=${encodeURIComponent(JSON.stringify(filterParam))}&from=${
          scaleTime.startTime
        }&to=${scaleTime.endTime}&timeType=${ETimeType.CUSTOM}&flowId=${record.flowId}`;
        jumpNewPage(url);
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
