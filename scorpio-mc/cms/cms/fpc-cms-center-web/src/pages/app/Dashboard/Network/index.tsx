import type { ConnectState } from '@/models/connect';
import type { AppModelState } from '@/models/app/index';
import { Row, Col } from 'antd';
import { useEffect, useState } from 'react';
import { queryFpcDevices } from '../services';
import { connect } from 'umi';
import { convertBandwidth, formatNumber } from '@/utils/utils';
import ProTimeAxisChart from '../components/ProTimeAxisChart';

const Index: React.FC<AppModelState> = ({ globalSelectedTime }) => {
  /** 所有fpc设备 */
  const [fpcDevices, setFpcDevices] = useState<any[]>([]);

  /** 获取fpc设备 */
  const fetchFpcDevices = async () => {
    const { success, result } = await queryFpcDevices();
    if (!success) {
      return;
    }
    setFpcDevices(result);
  };

  /** 初始化数据 */
  useEffect(() => {
    fetchFpcDevices();
  }, []);

  return (
    <>
      <Row gutter={[10, 10]}>
        <Col span={12}>
          <ProTimeAxisChart
            title="探针吞吐量TOP趋势"
            startTime={globalSelectedTime.startTime}
            endTime={globalSelectedTime.endTime}
            interval={globalSelectedTime.interval}
            fpcDevices={fpcDevices}
            metric="total_bytes"
            topNumber={10}
            type="network"
            unitConverter={(value: number) => {
              if (value === undefined) {
                return '采集缺失';
              }
              return convertBandwidth(value);
            }}
          />
        </Col>
        <Col span={12}>
          <ProTimeAxisChart
            title="探针并发会话TOP趋势"
            startTime={globalSelectedTime.startTime}
            endTime={globalSelectedTime.endTime}
            interval={globalSelectedTime.interval}
            fpcDevices={fpcDevices}
            metric="concurrent_sessions"
            topNumber={10}
            type="network"
            unitConverter={(value: number) => {
              if (value === undefined) {
                return '采集缺失';
              }
              return formatNumber(value);
            }}
          />
        </Col>
        <Col span={12}>
          <ProTimeAxisChart
            title="探针新建会话TOP趋势"
            startTime={globalSelectedTime.startTime}
            endTime={globalSelectedTime.endTime}
            interval={globalSelectedTime.interval}
            fpcDevices={fpcDevices}
            metric="established_sessions"
            topNumber={10}
            type="network"
            unitConverter={(value: number) => {
              if (value === undefined) {
                return '采集缺失';
              }
              return `${formatNumber(value)}`;
            }}
          />
        </Col>
        <Col span={12}>
          <ProTimeAxisChart
            title="探针TCP建连成功率最差TOP趋势"
            startTime={globalSelectedTime.startTime}
            endTime={globalSelectedTime.endTime}
            interval={globalSelectedTime.interval}
            fpcDevices={fpcDevices}
            metric="tcp_establish_success_rate"
            topNumber={10}
            type="network"
            valueName="ratio"
            unitConverter={(value: number) => {
              if (value === undefined) {
                return '采集缺失';
              }
              return `${(value * 100).toFixed(2)}%`;
            }}
          />
        </Col>
      </Row>
    </>
  );
};

export default connect(({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
}))(Index);
