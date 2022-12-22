import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import Bar from '@/pages/app/analysis/components/Bar';
import { queryL3Top } from '@/pages/app/analysis/service';
import type { IUriParams } from '@/pages/app/analysis/typings';
import { ESortDirection, ESourceType } from '@/pages/app/analysis/typings';
import { Card } from 'antd';
import { useContext, useEffect, useMemo, useState } from 'react';
import { connect, useParams } from 'umi';
import { ENumericalValue } from '../..';
import { v1 as uuidv1 } from 'uuid';
import { snakeCase } from '@/utils/frame/utils';
import numeral from 'numeral';
import { EFormatterType } from '@/pages/app/analysis/components/fieldsManager';
import type { INetworkTreeItem } from '@/pages/app/Network/typing';
import { ENetowrkType } from '@/pages/app/Network/typing';
import { ServiceContext } from '@/pages/app/analysis/Service/index';
import { NetworkTypeContext } from '@/pages/app/Network/Analysis';

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
  TCP_SERVER_ESTABLISHED_FAIL_COUNTS = 'tcpServerEstablishedFailCounts',
  TCP_CLIENT_ESTABLISHED_FAIL_COUNTS = 'tcpClientEstablishedFailCounts',
  TCP_SERVER_RECV_RETRANSMISSION_PACKETS = 'tcpServerRecvRetransmissionPackets',
  TCP_SERVER_SEND_RETRANSMISSION_PACKETS = 'tcpServerSendRetransmissionPackets',
  TCP_CLIENT_RECV_RETRANSMISSION_PACKETS = 'tcpClientRecvRetransmissionPackets',
  TCP_CLIENT_SEND_RETRANSMISSION_PACKETS = 'tcpClientSendRetransmissionPackets',
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
  const { networkId, serviceId }: IUriParams = useParams();
  const getUrlParams = () => {
    const tmpNetworkId = networkId || '';
    if (tmpNetworkId.includes('^')) {
      return [serviceId, tmpNetworkId.split('^')[1]];
    }
    return [serviceId, tmpNetworkId];
  };

  const [networkType] = useContext<[ENetowrkType, INetworkTreeItem[]] | any>(
    serviceId ? ServiceContext : NetworkTypeContext,
  );

  const [queryLoading, setQueryLoading] = useState(false);
  const [data, setData] = useState([]);

  const payload = useMemo(() => {
    const [tmpServiceId, tmpNetworkId] = getUrlParams();
    return {
      sourceType: tmpServiceId ? ESourceType.SERVICE : ESourceType.NETWORK,
      networkGroupId: networkType === ENetowrkType.NETWORK_GROUP ? tmpNetworkId : undefined,
      networkId: networkType === ENetowrkType.NETWORK ? tmpNetworkId : undefined,
      serviceId: tmpServiceId,
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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
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
  }, [payload]);

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
        onClick={(e: any) => {
          onClick(e.target.innerText);
        }}
        valueTextFormatterFn={
          compareProperty === ENumericalValue.COUNT
            ? (value: any) => value
            : (value: any) => `${value}%`
        }
        fixValueWidth={5}
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
