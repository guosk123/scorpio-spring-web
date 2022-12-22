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

export default function UnactiveConnectRate(props: Props) {
  const { serviceType, numericalValue, dsl } = props;
  const [state, dispatch] = useContext(ConnectionContext);
  return (
    <ConnectRateBar
      title="服务端建连失败TOP10"
      onClick={(value: string) => {
        const tmpFilter = [
          {
            field: ipV4Regex.test(value) ? 'ipv4_responder' : 'ipv6_responder',
            operator: EFilterOperatorTypes.EQ,
            operand: value,
          },
        ];
        jumpToConnectTab(state, dispatch, EConnectTabs.CONNECTION_ERROR, tmpFilter);
      }}
      serviceType={serviceType}
      compareProperty={numericalValue}
      sortProperty={ESort.TCP_SERVER_ESTABLISHED_FAIL_COUNTS}
      dsl={dsl}
    />
  );
}
