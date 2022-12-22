import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { ETimeType } from '@/components/GlobalTimeSelector';
import { ipV4Regex, jumpNewPage } from '@/utils/utils';
import { history } from 'umi';
import type { ENumericalValue } from '../../../TCP_Connection/ErrorAnalysis';
import ConnectRateBar, { ESort } from '../../../TCP_Connection/ErrorAnalysis/components/ConnectRateBar';

interface Props {
  selectedTimeInfo: any;
  serviceType: string;
  compareProperty?: ENumericalValue;
  dsl: string;
}

export default function ClientRecvRetransmission(props: Props) {
  const { selectedTimeInfo, serviceType, compareProperty, dsl } = props;
  return (
    <ConnectRateBar
      title="客户端接收重传TOP10"
      onClick={(value: string) => {
        const tmpFilter = [
          {
            field: ipV4Regex.test(value) ? 'ipv4_initiator' : 'ipv6_initiator',
            operator: EFilterOperatorTypes.EQ,
            operand: value,
          },
        ];
        jumpNewPage(
          `${
            history.location.pathname.split('retransmission-analysis')[0]
          }retransmission-detail?filter=${encodeURIComponent(JSON.stringify(tmpFilter))}&from=${new Date(
            selectedTimeInfo.originStartTime,
          ).valueOf()}&to=${new Date(selectedTimeInfo.originEndTime).valueOf()}&timeType=${
            ETimeType.CUSTOM
          }`,
        );
      }}
      serviceType={serviceType}
      compareProperty={compareProperty}
      sortProperty={ESort.TCPCLIENTRECVRETRANSMISSIONPACKETS}
      dsl={dsl}
    />
  );
}
