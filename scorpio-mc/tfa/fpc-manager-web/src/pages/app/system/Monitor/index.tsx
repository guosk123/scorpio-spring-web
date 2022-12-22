import { DEVICE_NETIF_CATEGORY_MANAGER, STATS_TIME_RANGE } from '@/common/dict';
import type { ConnectState } from '@/models/connect';
import DiskBox from '@/pages/app/Home/components/DiskList';
import * as dateMath from '@/utils/frame/datemath';
import { Button, Card, Col, DatePicker, Row } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import { useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch, IMonitorMetric, ISystemRuntime } from 'umi';
import SystemStateMetricsChart from '../../Home/components/SystemStateMetricsChart';
import FsIoStats from './components/FsIoStats';
import ManagerNetifStats from './components/ManagerNetifStats';
import ManagerRuntimeEnvironment from './components/ManagerRuntimeEnvironment';
import styles from './index.less';

const { RangePicker } = DatePicker;

interface IProps {
  dispatch: Dispatch;
  systemRuntime: ISystemRuntime;
  metrics: IMonitorMetric[];
  deviceList: any[];
  queryRuntimeLoading?: boolean;
}

const getTimeInfo = (from: string) => ({
  fromTime: moment(dateMath.parse(from)).format(),
  toTime: moment().format(),
});
interface ITimeInfo {
  fromTime: string;
  toTime: string;
}

const SystemMonitor = ({ dispatch, deviceList }: IProps) => {
  const [from, setFrom] = useState<string>(STATS_TIME_RANGE[0].key);
  const [selectedTimeInfo, setSelectedTimeInfo] = useState<ITimeInfo>(getTimeInfo(from));
  const [selectedTimeKey, setSelectedTimeKey] = useState<string>('now-30m');
  const pickerRef = useRef<any>();

  useEffect(() => {
    // 获取统计
    dispatch({ type: 'moitorModel/queryMetrics' });
    // 获取接口列表
    dispatch({ type: 'deviceNetifModel/queryDeviceNetifs' });
  }, [dispatch]);

  const handleTimeChange = (value: string) => {
    setFrom(value);
  };

  useEffect(() => {
    setSelectedTimeInfo(getTimeInfo(from));
  }, [from]);

  // 管理口网卡
  const managerNetif = useMemo(() => {
    return deviceList.find((item) => item.category === DEVICE_NETIF_CATEGORY_MANAGER);
  }, [deviceList]);

  return (
    <>
      <div className={styles.selectWrapper}>
        <ManagerRuntimeEnvironment />
        <div className={styles.selectBox}>
          <RangePicker
            ref={pickerRef}
            allowClear={false}
            value={[moment(selectedTimeInfo.fromTime), moment(selectedTimeInfo.toTime)]}
            showTime
            onChange={(timeRange) => {
              if (timeRange && timeRange.length === 2) {
                setSelectedTimeInfo({
                  fromTime: timeRange[0]!.format(),
                  toTime: timeRange[1]!.format(),
                });
                setSelectedTimeKey('');
              }
            }}
            renderExtraFooter={() => {
              return (
                <>
                  {STATS_TIME_RANGE.map((item) => {
                    return (
                      <Button
                        size={'small'}
                        type={item.key === selectedTimeKey ? 'primary' : 'link'}
                        key={item.key}
                        onClick={() => {
                          handleTimeChange(item.key);
                          setSelectedTimeKey(item.key);
                          pickerRef.current.blur();
                        }}
                      >
                        {item.name}
                      </Button>
                    );
                  })}
                </>
              );
            }}
          />
        </div>
      </div>

      <Card
        size="small"
        title="系统状态"
        bodyStyle={{ padding: '10px 10px 0' }}
        style={{ marginBottom: 10 }}
      >
        <SystemStateMetricsChart
          from={selectedTimeInfo.fromTime}
          to={selectedTimeInfo.toTime}
          canZoom={false}
        />
      </Card>
      <FsIoStats from={selectedTimeInfo.fromTime} to={selectedTimeInfo.toTime} />
      <Row>
        <Col span={24}>
          {managerNetif && (
            <ManagerNetifStats
              netif={managerNetif}
              from={selectedTimeInfo.fromTime}
              to={selectedTimeInfo.toTime}
            />
          )}
        </Col>
      </Row>
      <Row>
        <Col span={24}>
          <DiskBox />
        </Col>
      </Row>
    </>
  );
};

export default connect(
  ({
    appModel: { systemRuntime },
    moitorModel: { metrics },
    deviceNetifModel: { list },
    loading: { effects },
  }: ConnectState) => ({
    systemRuntime,
    metrics,
    deviceList: list,
    queryRuntimeLoading: effects['appModel/queryRuntimeEnvironments'],
  }),
)(SystemMonitor);
