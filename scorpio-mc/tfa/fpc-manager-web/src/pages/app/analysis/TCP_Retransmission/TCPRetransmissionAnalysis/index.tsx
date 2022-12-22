import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { Col, Row } from 'antd';
import { useMemo, useState } from 'react';
import { connect, useParams } from 'umi';
import type { IOfflinePcapData } from '../../OfflinePcapAnalysis/typing';
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

  const dsl = useMemo(() => {
    let res = '';
    if (serviceId) {
      res = `(network_id = ${networkId}) AND (service_id = ${serviceId})`;
    } else {
      res = `(network_id = ${networkId || pcapFileId})`;
    }
    const last = `| gentimes timestamp start="${selectedTimeInfo.originStartTime}" end="${selectedTimeInfo.originEndTime}"`;
    return res + last;
  }, [
    networkId,
    pcapFileId,
    selectedTimeInfo.originEndTime,
    selectedTimeInfo.originStartTime,
    serviceId,
  ]);
  return (
    <CardForService changeService={setServiceType} changeRate={setNumericalValue}>
      <Row gutter={10}>
        <Col span={12}>
          <ClientSendRetransmission
            selectedTimeInfo={selectedTimeInfo}
            serviceType={serviceType}
            compareProperty={numericalValue}
            dsl={dsl}
          />
        </Col>
        <Col span={12}>
          <ServerRecvRetransmission
            selectedTimeInfo={selectedTimeInfo}
            serviceType={serviceType}
            compareProperty={numericalValue}
            dsl={dsl}
          />
        </Col>
        <Col span={12}>
          <ServerSendRetransmission
            selectedTimeInfo={selectedTimeInfo}
            serviceType={serviceType}
            compareProperty={numericalValue}
            dsl={dsl}
          />
        </Col>
        <Col span={12}>
          <ClientRecvRetransmission
            selectedTimeInfo={selectedTimeInfo}
            serviceType={serviceType}
            compareProperty={numericalValue}
            dsl={dsl}
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
