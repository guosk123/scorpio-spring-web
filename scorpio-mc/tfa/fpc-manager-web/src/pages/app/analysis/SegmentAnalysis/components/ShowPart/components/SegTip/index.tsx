import { Tooltip } from 'antd';
import { scaleLinear } from 'd3';
import { useMemo } from 'react';
import { snakeCase } from '@/utils/utils';
import type { INetworkSegmentDetails } from '../../../../typings';

interface Props {
  networkDetailItemId: string;
  networkDetail: INetworkSegmentDetails;
  networkIndex: number;
  networkBaseSet: INetworkSegmentDetails;
}

const getColor = (value: number, baseValue: number) => {
  const deviationRate: number = Number(((value - baseValue) / baseValue).toFixed(2));
  const deviation = deviationRate * 100;
  if (deviation > 0 && deviation <= 5) {
    return 'black';
  }
  if (deviation > 5 && deviation <= 30) {
    return 'green';
  }
  if (deviation > 30 && deviation <= 50) {
    return '#FFCC00';
  }
  if (deviation > 50) {
    return 'red';
  }
  return 'black';
};

const labelColorValueRangeMap = {
  tcpClientRetransmissionPackets: [0, 100],
  tcpClientRetransmissionRate: [0, 100],
  tcpServerRetransmissionPackets: [0, 100],
  tcpServerRetransmissionRate: [0, 100],
  tcpClientNetworkLatency: [0, 1000],
  tcpServerNetworkLatency: [0, 1000],
};

export default function SegTip(props: Props) {
  const { networkDetailItemId, networkBaseSet, networkDetail, networkIndex } = props;

  const getSuffix = (titleId: string) => {
    const titles = snakeCase(titleId).split('_');
    const suffixId = titles.pop();
    if (suffixId === 'latency') {
      return 'ms';
    }
    if (suffixId === 'rate') {
      return '%';
    }
    return '';
  };

  const showedBackgroundColor = useMemo(() => {
    if (networkDetail[networkDetailItemId] === networkBaseSet[networkDetailItemId]) {
      return 'black';
    }
    return getColor(networkDetail[networkDetailItemId], networkBaseSet[networkDetailItemId]);
  }, [networkBaseSet, networkDetail, networkDetailItemId]);

  return (
    <Tooltip title={`${networkDetail[networkDetailItemId]}${getSuffix(networkDetailItemId)}`}>
      <div
        style={{
          // display: 'inline',
          display: 'flex',
          flexDirection: 'row',
          overflow: 'hidden',
          // padding: '3px 7px',
          width: 'max-content',
          fontSize: '13px',
          fontWeight: 'bold',
          lineHeight: 1,
          color: showedBackgroundColor,
          textAlign: 'center',
          whiteSpace: 'nowrap',
          textOverflow: 'ellipsis',
          verticalAlign: 'baseline',
          // backgroundColor: showedBackgroundColor,
          // borderRadius: '10px',
        }}
      >
        <div
          style={{
            minWidth: 10,
            maxWidth: 60,
            textOverflow: 'ellipsis',
            overflow: 'hidden',
            whiteSpace: 'nowrap',
            lineHeight: 1,
          }}
        >
          {networkDetail[networkDetailItemId]}
        </div>
        {getSuffix(networkDetailItemId) !== '' && <div>{getSuffix(networkDetailItemId)}</div>}
      </div>
    </Tooltip>
  );
}
