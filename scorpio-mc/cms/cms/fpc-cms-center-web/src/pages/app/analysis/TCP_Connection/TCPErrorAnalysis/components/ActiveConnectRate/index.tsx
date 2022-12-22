import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { ipV4Regex } from '@/utils/utils';
import ConnectRateBar, { ESort } from '../ConnectRateBar';
import type { ENumericalValue } from '../..';
import { useContext } from 'react';
import { ConnectionContext } from '@/pages/app/Network/components/Connection';
import { jumpToConnectTab } from '@/pages/app/Network/components/Connection/constant';
import { EConnectTabs } from '@/pages/app/Network/components/Connection/typing';

interface Props {
  selectedTimeInfo: any;
  serviceType: string;
  numericalValue: ENumericalValue;
  dsl: string;
}

export default function ActiveConnectRate(props: Props) {
  const { serviceType, numericalValue, dsl } = props;
  const [state, dispatch] = useContext(ConnectionContext);
  return (
    <ConnectRateBar
      title="客户端建连失败TOP10"
      onClick={(value: string) => {
        const tmpFilter = [
          {
            field: ipV4Regex.test(value) ? 'ipv4_initiator' : 'ipv6_initiator',
            operator: EFilterOperatorTypes.EQ,
            operand: value,
          },
        ];
        jumpToConnectTab(state, dispatch, EConnectTabs.CONNECTION_ERROR, tmpFilter);
      }}
      serviceType={serviceType}
      compareProperty={numericalValue}
      sortProperty={ESort.TCP_CLIENT_ESTABLISHED_FAIL_COUNTS}
      dsl={dsl}
    />
  );
}
