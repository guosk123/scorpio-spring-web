import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { NetworkTypeContext } from '@/pages/app/Network/Analysis';
import { RetransmissionContext } from '@/pages/app/Network/components/Retransmission';
import {
  ERetransmissionTabs,
  jumpToRetransmissionTab,
} from '@/pages/app/Network/components/Retransmission/constant';
import type { INetworkTreeItem } from '@/pages/app/Network/typing';
import { ENetowrkType } from '@/pages/app/Network/typing';
import { Col, Row } from 'antd';
import { useContext, useMemo, useState } from 'react';
import { connect, useParams } from 'umi';
import type { IOfflinePcapData } from '../../OfflinePcapAnalysis/typing';
import { ServiceContext } from '../../Service/index';
import { ENumericalValue } from '../../TCP_Connection/ErrorAnalysis';
import CardForService from '../../TCP_Connection/ErrorAnalysis/components/CardForService';
import type { IUriParams } from '../../typings';
import { EServiceType } from '../../typings';
import ClientRecvRetransmission from '../components/ClientRecvRetransmission';
import ClientSendRetransmission from '../components/ClientSendRetransmission';
import ServerRecvRetransmission from '../components/ServerRecvRetransmission';
import ServerSendRetransmission from '../components/ServerSendRetransmission';

interface Props {
  currentPcpInfo: IOfflinePcapData;
  globalSelectedTime: Required<IGlobalTime>;
}

function TCPRetransmissionAnalysis(props: Props) {
  const { currentPcpInfo, globalSelectedTime } = props;
  const { networkId, serviceId, pcapFileId }: IUriParams = useParams();
  const [numericalValue, setNumericalValue] = useState(ENumericalValue.COUNT);
  const [serviceType, setServiceType] = useState(EServiceType.TOTALSERVICE);
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
  const [state, dispatch] = useContext(RetransmissionContext);
  const jumpToFn = (tmpFilter: any) => {
    jumpToRetransmissionTab(state, dispatch, ERetransmissionTabs.RETRANSMISSION_DETAIL, tmpFilter);
  };

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
  }, [networkType, selectedTimeInfo.originEndTime, selectedTimeInfo.originStartTime]);
  return (
    <CardForService changeService={setServiceType} changeRate={setNumericalValue}>
      <Row gutter={10}>
        <Col span={12}>
          <ClientSendRetransmission
            selectedTimeInfo={selectedTimeInfo}
            serviceType={serviceType}
            compareProperty={numericalValue}
            dsl={dsl}
            jumpToFn={jumpToFn}
          />
        </Col>
        <Col span={12}>
          <ServerRecvRetransmission
            selectedTimeInfo={selectedTimeInfo}
            serviceType={serviceType}
            compareProperty={numericalValue}
            dsl={dsl}
            jumpToFn={jumpToFn}
          />
        </Col>
        <Col span={12}>
          <ServerSendRetransmission
            selectedTimeInfo={selectedTimeInfo}
            serviceType={serviceType}
            compareProperty={numericalValue}
            dsl={dsl}
            jumpToFn={jumpToFn}
          />
        </Col>
        <Col span={12}>
          <ClientRecvRetransmission
            selectedTimeInfo={selectedTimeInfo}
            serviceType={serviceType}
            compareProperty={numericalValue}
            dsl={dsl}
            jumpToFn={jumpToFn}
          />
        </Col>
      </Row>
    </CardForService>
  );
}
export default connect(
  ({ appModel: { globalSelectedTime }, npmdModel: { currentPcpInfo } }: any) => {
    return { globalSelectedTime, currentPcpInfo };
  },
)(TCPRetransmissionAnalysis);
