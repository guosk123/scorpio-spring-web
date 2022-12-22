import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import Bar from '@/pages/app/analysis/components/Bar';
import { EFormatterType } from '@/pages/app/analysis/components/fieldsManager';
import { queryL3Top } from '@/pages/app/analysis/service';
import type { IUriParams } from '@/pages/app/analysis/typings';
import { ESortDirection, ESourceType } from '@/pages/app/analysis/typings';
import { snakeCase } from '@/utils/frame/utils';
import { useSafeState } from 'ahooks';
import { Card } from 'antd';
import numeral from 'numeral';
import { useEffect, useMemo } from 'react';
import { connect, useParams } from 'umi';
import { v1 as uuidv1 } from 'uuid';
import { ENumericalValue } from '../..';

const cardProps = {
  bordered: true,
  size: 'small',
  style: { marginBottom: 10 },
  bodyStyle: { height: 300, padding: 6 },
};

enum EDataSource {
  RATE = 'rate',
  COUNT = 'count',
}

//EFormatterType.BYTE : EFormatterType.COUNT
export const dataSourceList = [
  {
    label: '比率',
    value: EDataSource.RATE,
  },
  {
    label: '次数',
    value: EDataSource.COUNT,
  },
];

export enum ESort {
  TCPSERVERESTABLISHEDFAILCOUNTS = 'tcpServerEstablishedFailCounts',
  TCPCLIENTESTABLISHEDFAILCOUNTS = 'tcpClientEstablishedFailCounts',
  TCPSERVERRECVRETRANSMISSIONPACKETS = 'tcpServerRecvRetransmissionPackets',
  TCPSERVERSENDRETRANSMISSIONPACKETS = 'tcpServerSendRetransmissionPackets',
  TCPCLIENTRECVRETRANSMISSIONPACKETS = 'tcpClientRecvRetransmissionPackets',
  TCPCLIENTSENDRETRANSMISSIONPACKETS = 'tcpClientSendRetransmissionPackets',
}

//IGlobalTime
interface Props {
  allNetworkFlowHistogram?: any;
  globalSelectedTime?: IGlobalTime;
  bordered?: boolean;
  title?: string;
  onClick?: any;
  serviceType?: string;
  itemValue?: string;
  compareProperty?: ENumericalValue;
  sortProperty?: string;
  dsl?: string;
}

function ConnectRateBar(props: Props) {
  const {
    globalSelectedTime,
    bordered,
    title,
    onClick,
    serviceType,
    compareProperty,
    sortProperty,
    dsl,
  } = props;
  const { networkId, serviceId, pcapFileId }: IUriParams = useParams();
  const [queryLoading, setQueryLoading] = useSafeState(false);
  const [data, setData] = useSafeState([]);

  const payload = useMemo(() => {
    let sourceType = ESourceType.NETWORK;
    if (serviceId) {
      sourceType = ESourceType.SERVICE;
    } else if (pcapFileId) {
      sourceType = ESourceType.OFFLINE;
    }
    return {
      sourceType,
      [networkId ? 'networkId' : 'packetFileId']: networkId || pcapFileId,
      serviceId,
      startTime: globalSelectedTime?.startTime || '',
      endTime: globalSelectedTime?.endTime || '',
      interval: globalSelectedTime?.interval || 60,
      sortProperty: snakeCase(sortProperty || ''),
      sortDirection: ESortDirection.DESC,
      count: 10,
      dsl,
      drilldown: 0,
      queryId: uuidv1(),
      serviceType,
      compareProperty,
    };
  }, [
    serviceId,
    networkId,
    pcapFileId,
    globalSelectedTime?.startTime,
    globalSelectedTime?.endTime,
    globalSelectedTime?.interval,
    sortProperty,
    dsl,
    serviceType,
    compareProperty,
  ]);

  useEffect(() => {
    setQueryLoading(true);
    queryL3Top(payload).then((res) => {
      const { success, result } = res;
      setQueryLoading(false);
      if (success) {
        setData(result);
      }
    });
  }, [payload, setData, setQueryLoading]);

  const dataSource = useMemo(() => {
    return data.map((item: any) => {
      const values = (Object.values(item) || ['-', 0]) as any;
      return {
        label: values[0] || '-',
        value:
          compareProperty === ENumericalValue.COUNT
            ? values[1]
            : numeral((values[1] * 100).toFixed(0)).value(),
      } as {
        label: string;
        value: number;
      };
    });
  }, [compareProperty, data]);

  return (
    <Card {...(bordered ? { ...cardProps, bordered } : cardProps)} size="small" title={title}>
      <Bar
        loading={queryLoading}
        height={300}
        data={dataSource}
        formatterType={EFormatterType.TEXT}
        onClick={(label) => {
          onClick(label);
        }}
        valueTextFormatterFn={
          compareProperty === ENumericalValue.COUNT
            ? (value: any) => value
            : (value: any) => `${value}%`
        }
        fixValueWidth={5}
        style={{ paddingRight: 10 }}
      />
    </Card>
  );
}

export default connect(
  ({ homeModel: { allNetworkFlowHistogram }, appModel: { globalSelectedTime } }: ConnectState) => {
    return {
      allNetworkFlowHistogram,
      globalSelectedTime,
    };
  },
)(ConnectRateBar);
