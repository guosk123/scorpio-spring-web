import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { ETimeType } from '@/components/GlobalTimeSelector';
import { ipV4Regex, jumpNewPage } from '@/utils/utils';
import ConnectRateBar, { ESort } from '../ConnectRateBar';
import { history } from 'umi';
import type { ENumericalValue } from '../..';

interface Props {
  selectedTimeInfo: any;
  serviceType: string;
  numericalValue: ENumericalValue;
  dsl: string;
}

export default function ActiveConnectRate(props: Props) {
  const { selectedTimeInfo, serviceType, numericalValue, dsl } = props;
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
        jumpNewPage(
          `${history.location.pathname.split('error-analysis')[0]}error?filter=${encodeURIComponent(
            JSON.stringify(tmpFilter),
          )}&from=${new Date(selectedTimeInfo.originStartTime).valueOf()}&to=${new Date(
            selectedTimeInfo.originEndTime,
          ).valueOf()}&timeType=${ETimeType.CUSTOM}`,
        );
      }}
      serviceType={serviceType}
      compareProperty={numericalValue}
      sortProperty={ESort.TCPCLIENTESTABLISHEDFAILCOUNTS}
      dsl={dsl}
    />
  );
}
