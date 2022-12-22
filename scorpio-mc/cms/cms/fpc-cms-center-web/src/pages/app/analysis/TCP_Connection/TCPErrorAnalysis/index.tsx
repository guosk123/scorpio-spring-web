import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { NetworkTypeContext } from '@/pages/app/Network/Analysis';
import type { INetworkTreeItem } from '@/pages/app/Network/typing';
import { ENetowrkType } from '@/pages/app/Network/typing';
import { Col, Row } from 'antd';
import { useContext, useMemo, useRef, useState } from 'react';
import { connect, useParams } from 'umi';
import type { IOfflinePcapData } from '../../OfflinePcapAnalysis/typing';
import { ServiceContext } from '../../Service/index';
import type { IUriParams } from '../../typings';
import { EServiceType } from '../../typings';
import ActiveConnectRate from './components/ActiveConnectRate';
import CardForService from './components/CardForService';
import ErrorConnectTable from './components/ErrorConnectTable';
import UnactiveConnectRate from './components/UnactiveConnectRate';

interface Props {
  currentPcpInfo: IOfflinePcapData;
  globalSelectedTime: Required<IGlobalTime>;
}

export enum ENumericalValue {
  COUNT = 'count',
  RATE = 'rate',
}

function TCPErrorAnalysis(props: Props) {
  const { currentPcpInfo, globalSelectedTime } = props;
  const { networkId, serviceId, pcapFileId }: IUriParams = useParams();
  const [serviceType, setServiceType] = useState(EServiceType.TOTALSERVICE);
  const [numericalValue, setNumericalValue] = useState(ENumericalValue.COUNT);
  const getUrlParams = () => {
    const tmpNetworkId = networkId || '';
    if (tmpNetworkId.includes('^')) {
      return [serviceId, tmpNetworkId.split('^')[1]];
    }
    return [serviceId, tmpNetworkId];
  };

  const selectedTimeInfo = useMemo(() => {
    // 离线文件分析
    if (pcapFileId && currentPcpInfo) {
      return {
        startTime: currentPcpInfo?.filterStartTime,
        endTime: currentPcpInfo?.filterEndTime,
        originStartTime: currentPcpInfo?.filterStartTime,
        originEndTime: currentPcpInfo?.filterEndTime,
      };
    }

    return globalSelectedTime;
  }, [currentPcpInfo, globalSelectedTime, pcapFileId]);

  const [networkType] = useContext<[ENetowrkType, INetworkTreeItem[]] | any>(
    serviceId ? ServiceContext : NetworkTypeContext,
  );

  const dsl = useMemo(() => {
    const [tmpServiceId, tmpNetworkId] = getUrlParams();
    let res = '';
    if (tmpServiceId) {
      res = `(network_id = ${tmpNetworkId}) AND (service_id = ${tmpServiceId})`;
    } else if (tmpNetworkId && networkType === ENetowrkType.NETWORK) {
      res = `(network_id = ${tmpNetworkId || pcapFileId})`;
    }

    const last = `| gentimes timestamp start="${selectedTimeInfo.originStartTime}" end="${selectedTimeInfo.originEndTime}"`;
    return res + last;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedTimeInfo.originEndTime, selectedTimeInfo.originStartTime]);

  const sortFnRef = useRef((key: EServiceType) => key);

  return (
    <CardForService
      changeService={(key: EServiceType) => {
        setServiceType(key);
        sortFnRef.current(key);
      }}
      changeRate={setNumericalValue}
    >
      <Row gutter={10}>
        <Col span={12}>
          <ActiveConnectRate
            selectedTimeInfo={selectedTimeInfo}
            serviceType={serviceType}
            numericalValue={numericalValue}
            dsl={dsl}
          />
        </Col>
        <Col span={12}>
          <UnactiveConnectRate
            selectedTimeInfo={selectedTimeInfo}
            serviceType={serviceType}
            numericalValue={numericalValue}
            dsl={dsl}
          />
        </Col>
      </Row>
      <ErrorConnectTable
        selectedTimeInfo={selectedTimeInfo}
        serviceType={serviceType}
        compareProperty={numericalValue}
        changeSortKey={(sortFn: any) => {
          sortFnRef.current = sortFn;
        }}
        dsl={dsl}
      />
    </CardForService>
  );
}
export default connect(
  ({ appModel: { globalSelectedTime }, npmdModel: { currentPcpInfo } }: any) => {
    return { globalSelectedTime, currentPcpInfo };
  },
)(TCPErrorAnalysis);
