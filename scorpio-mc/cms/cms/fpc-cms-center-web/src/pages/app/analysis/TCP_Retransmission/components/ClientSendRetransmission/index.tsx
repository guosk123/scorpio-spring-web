import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { ETimeType } from '@/components/GlobalTimeSelector';
import { ipV4Regex, jumpNewPage } from '@/utils/utils';
import { history } from 'umi';
import type { ENumericalValue } from '../../../TCP_Connection/ErrorAnalysis';
import ConnectRateBar, {
  ESort,
} from '../../../TCP_Connection/ErrorAnalysis/components/ConnectRateBar';

interface Props {
  selectedTimeInfo: any;
  serviceType: string;
  compareProperty?: ENumericalValue;
  dsl: string;
  jumpToFn?: any;
}

export default function ClientSendRetransmission(props: Props) {
  const { selectedTimeInfo, serviceType, compareProperty, dsl, jumpToFn } = props;
  return (
    <ConnectRateBar
      title="客户端发送重传TOP10"
      onClick={(value: string) => {
        const tmpFilter = [
          {
            field: ipV4Regex.test(value) ? 'ipv4_initiator' : 'ipv6_initiator',
            operator: EFilterOperatorTypes.EQ,
            operand: value,
          },
        ];
        jumpToFn(tmpFilter);
      }}
      serviceType={serviceType}
      compareProperty={compareProperty}
      sortProperty={ESort.TCP_CLIENT_SEND_RETRANSMISSION_PACKETS}
      dsl={dsl}
    />
  );
}
