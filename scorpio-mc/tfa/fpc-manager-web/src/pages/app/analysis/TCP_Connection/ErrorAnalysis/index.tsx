import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { Col, Row } from 'antd';
import { useMemo, useRef, useState } from 'react';
import { connect, useParams } from 'umi';
import type { IOfflinePcapData } from '../../OfflinePcapAnalysis/typing';
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

function ErrorAnalysis(props: Props) {
  const { currentPcpInfo, globalSelectedTime } = props;
  const { networkId, serviceId, pcapFileId }: IUriParams = useParams();
  const [serviceType, setServiceType] = useState(EServiceType.TOTALSERVICE);
  const [numericalValue, setNumericalValue] = useState(ENumericalValue.COUNT);

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
        {/* {tableDom} */}
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
)(ErrorAnalysis);
