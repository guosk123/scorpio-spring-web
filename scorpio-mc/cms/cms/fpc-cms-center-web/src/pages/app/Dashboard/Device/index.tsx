import type { ConnectState } from '@/models/connect';
import type { AppModelState } from '@/models/app/index';
import { Row, Col, Card, Select, Switch, Spin } from 'antd';
import { Fragment, useCallback, useEffect, useMemo, useState } from 'react';
import TimeAxisChart from '@/components/TimeAxisChart';
import Bar from '../components/Bar';
import type { TimeAxisChartData } from '@/components/TimeAxisChart/typing';
import { queryFpcDevices, queryIndexData, queryDiskIO } from '../services';
import { connect } from 'umi';
import styles from './index.less';
import moment from 'moment';
import { convertBandwidth, getLinkUrl, jumpNewPage } from '@/utils/utils';
import { lineConverter } from '../utils/converter';
import { EPartitionType, EDiskIOType } from '../typings';
import { history } from 'umi';
import ProTimeAxisChart from '../components/ProTimeAxisChart';

const { Option } = Select;

const Index: React.FC<AppModelState> = ({ globalSelectedTime }) => {
  /** 加载标识 */
  const [indexDataLoading, setIndexDataLoading] = useState<boolean>(true);
  /** 所有fpc设备 */
  const [fpcDevices, setFpcDevices] = useState<any[]>([]);
  // 方框数据
  const [onlineSensorCount, setOnlineSensorCount] = useState<number>();
  const [sensorAlarmCount, setSensorAlarmCount] = useState<number>();
  const [sensorDiskInput, setSensorDiskInput] = useState<any[]>();
  const [sensorDiskOutput, setSensorDiskOutput] = useState<any[]>();
  const [showDiskInput, setShowDiskInput] = useState<TimeAxisChartData[]>();
  const [showDiskOutput, setShowDiskOutput] = useState<TimeAxisChartData[]>();
  /** loading */
  const [diskInputLoading, setDiskInputLoading] = useState<boolean>(false);
  const [diskOutputLoading, setDiskOutputLoading] = useState<boolean>(false);
  /** IO分区类型 */
  const [patitionInputType, setPatitionInputType] = useState<EPartitionType>(EPartitionType.SYSTEM);
  const [patitionOutputType, setPatitionOutputType] = useState<EPartitionType>(
    EPartitionType.SYSTEM,
  );
  /** IO类型 */
  const [diskInputType, setDiskInputType] = useState<EDiskIOType>(EDiskIOType.READBYTES);
  const [diskOutputType, setDiskOutputType] = useState<EDiskIOType>(EDiskIOType.WRITEBYTES);
  /** 全局时间时间戳 */
  const startTime = useMemo(() => {
    return moment(globalSelectedTime.originStartTime).valueOf();
  }, [globalSelectedTime.originStartTime]);

  const endTime = useMemo(() => {
    return moment(globalSelectedTime.originEndTime).valueOf();
  }, [globalSelectedTime.originEndTime]);

  const interval = useMemo(() => {
    const duration = moment.duration(moment(endTime).diff(moment(startTime)));
    return duration.asMinutes() <= 60 ? 60 : 300;
  }, [startTime, endTime]);

  /** 展示开始时间 */
  const displayStartTime = useMemo(() => {
    return moment(globalSelectedTime.startTime)
      .add(interval / 60, 'm')
      .valueOf();
  }, [startTime]);

  const displayEndTime = useMemo(() => {
    return moment(globalSelectedTime.endTime).valueOf();
  }, [endTime]);

  /** 获取数据 */
  /** 获取fpc设备 */
  const fetchFpcDevices = async () => {
    const { success, result } = await queryFpcDevices();
    if (!success) {
      return;
    }
    setFpcDevices(result);
  };
  /** 获取方框数据 */
  const fetchIndexData = async (startTime: string, endTime: string) => {
    setIndexDataLoading(true);
    const {
      success,
      result: { onlineSensorCount, sensorAlarmCount },
    } = await queryIndexData({
      metric: 'online_sensor_count,sensor_alarm_count',
      startTime,
      endTime,
    });
    if (!success) {
      return;
    }
    if (success) {
      setIndexDataLoading(false);
      setOnlineSensorCount(onlineSensorCount);
      setSensorAlarmCount(sensorAlarmCount);
    }
  };
  const detailsOfOnlineSensors = () => {
    history.push('/configuration/equipment/sensor');
  };
  const detailsOfensorAlarms = () => {
    console.log(globalSelectedTime, 'globalSelectedTime');
    const linkUrl = getLinkUrl(
      `/logAlarm/alarm?startTime=${moment(
        globalSelectedTime.originStartTime,
      ).valueOf()}&endTime=${moment(globalSelectedTime.originEndTime).valueOf()}&components=${[
        '000000',
        '001001',
        '001002',
        '001004'
      ].join(',')}`,
    );
    jumpNewPage(linkUrl);
    // history.push(
    //   `/logAlarm/alarm?startTime=${moment(
    //     globalSelectedTime.originStartTime,
    //   ).valueOf()}&endTime=${moment(globalSelectedTime.originEndTime).valueOf()}&components=${[
    //     '000000',
    //     '001001',
    //     '001002',
    //   ].join(',')}`,
    // );
  };

  const querySensorDiskInput = useCallback(async () => {
    setDiskInputLoading(true);
    const { originStartTime, originEndTime } = globalSelectedTime;
    const { success, result } = await queryDiskIO({
      metric: diskInputType,
      partitionName: patitionInputType,
      startTime: originStartTime,
      endTime: originEndTime,
      interval,
      topNumber: 10,
    });
    if (!success) {
      setDiskInputLoading(false);
      return;
    }
    setSensorDiskInput(result);
    setDiskInputLoading(false);
  }, [diskInputType, interval, startTime, endTime, patitionInputType]);

  const querySensorDiskOutput = useCallback(async () => {
    setDiskOutputLoading(true);
    const { originStartTime, originEndTime } = globalSelectedTime;
    const { success, result } = await queryDiskIO({
      metric: diskOutputType,
      partitionName: patitionOutputType,
      startTime: originStartTime,
      endTime: originEndTime,
      interval,
      topNumber: 10,
    });
    if (!success) {
      setDiskInputLoading(false);
      return;
    }
    setSensorDiskOutput(result);
    setDiskOutputLoading(false);
  }, [diskOutputType, interval, startTime, endTime, patitionOutputType]);

  useEffect(() => {
    querySensorDiskInput();
  }, [querySensorDiskInput, diskInputType]);

  useEffect(() => {
    querySensorDiskOutput();
  }, [querySensorDiskOutput, diskOutputType]);

  useEffect(() => {
    /** 过滤内容 */
    setShowDiskInput(
      lineConverter(
        sensorDiskInput,
        fpcDevices,
        diskInputType,
        undefined,
        undefined,
        undefined,
        (value) => 8 * value,
      ),
    );
  }, [sensorDiskInput, fpcDevices]);

  useEffect(() => {
    /** 过滤内容 */
    setShowDiskOutput(
      lineConverter(
        sensorDiskOutput,
        fpcDevices,
        diskOutputType,
        undefined,
        undefined,
        undefined,
        (value) => 8 * value,
      ),
    );
  }, [sensorDiskOutput, fpcDevices, diskOutputType]);

  /** 初始化数据 */
  useEffect(() => {
    fetchFpcDevices();
  }, []);

  useEffect(() => {
    if (fpcDevices.length <= 0) {
      setIndexDataLoading(false);
      return;
    }
    const { originStartTime, originEndTime } = globalSelectedTime;
    // 获取方框数据
    fetchIndexData(originStartTime, originEndTime);
  }, [startTime, endTime, fpcDevices, globalSelectedTime]);

  return (
    <>
      <Row gutter={[10, 10]}>
        <Col span={24}>
          <div className={styles['header-container']}>
            <Card className={styles['header-container__item']}>
              <div className={styles['header-container__item__title']}>在线探针数</div>
              <div>
                {indexDataLoading ? (
                  <Spin />
                ) : (
                  <Fragment>
                    <span
                      className={styles['header-container__item__content']}
                      onClick={detailsOfOnlineSensors}
                    >
                      {onlineSensorCount !== undefined && onlineSensorCount + '个'}
                    </span>
                    {/* <span>个</span> */}
                  </Fragment>
                )}
              </div>
            </Card>
            <Card className={styles['header-container__item']}>
              <div className={styles['header-container__item__title']}>探针告警数</div>
              <div>
                {indexDataLoading ? (
                  <Spin />
                ) : (
                  <Fragment>
                    <span
                      className={styles['header-container__item__content']}
                      onClick={detailsOfensorAlarms}
                    >
                      {sensorAlarmCount !== undefined && sensorAlarmCount + '个'}
                    </span>
                    {/* <span>个</span> */}
                  </Fragment>
                )}
              </div>
            </Card>
          </div>
        </Col>
        <Col span={12}>
          <ProTimeAxisChart
            title="探针CPU使用率TOP趋势"
            startTime={globalSelectedTime.startTime}
            endTime={globalSelectedTime.endTime}
            interval={globalSelectedTime.interval}
            fpcDevices={fpcDevices}
            metric="cpu_metric"
            topNumber={10}
            type="device"
          />
        </Col>
        <Col span={12}>
          <ProTimeAxisChart
            title="探针内存使用率TOP趋势"
            startTime={globalSelectedTime.startTime}
            endTime={globalSelectedTime.endTime}
            interval={globalSelectedTime.interval}
            fpcDevices={fpcDevices}
            metric="memory_metric"
            topNumber={10}
            type="device"
          />
        </Col>
        <Col span={12}>
          <Bar
            title="探针系统分区剩余空间排名"
            metric={'system_fs_free'}
            startTime={globalSelectedTime.originStartTime}
            endTime={globalSelectedTime.originEndTime}
            fpcDevices={fpcDevices}
          />
        </Col>
        <Col span={12}>
          <Bar
            title="探针数据包索引分区剩余空间排名"
            metric="index_fs_free"
            startTime={globalSelectedTime.originStartTime}
            endTime={globalSelectedTime.originEndTime}
            fpcDevices={fpcDevices}
          />
        </Col>
        <Col span={12}>
          <Bar
            title="探针详单冷区剩余空间排名"
            metric="metadata_fs_free"
            startTime={globalSelectedTime.originStartTime}
            endTime={globalSelectedTime.originEndTime}
            fpcDevices={fpcDevices}
          />
        </Col>
        <Col span={12}>
          <Bar
            title="探针热区剩余空间排名"
            metric="metadata_hot_fs_free"
            startTime={globalSelectedTime.originStartTime}
            endTime={globalSelectedTime.originEndTime}
            fpcDevices={fpcDevices}
          />
        </Col>
        <Col span={12}>
          <Card
            size="small"
            title="探针磁盘读 TOP趋势"
            style={{ height: 360, marginBottom: '10px' }}
            bodyStyle={{
              height: 300,
              padding: 0,
            }}
            extra={
              <div>
                <Switch
                  style={{ marginRight: '10px' }}
                  checkedChildren="峰值"
                  unCheckedChildren="均值"
                  onChange={(checked) => {
                    if (checked) {
                      setDiskInputType(EDiskIOType.READBYTESPEAK);
                    } else {
                      setDiskInputType(EDiskIOType.READBYTES);
                    }
                  }}
                />
                <Select
                  value={patitionInputType}
                  onChange={(type) => {
                    setPatitionInputType(type);
                  }}
                  style={{ width: '150px' }}
                >
                  <Option key={EPartitionType.SYSTEM} value={EPartitionType.SYSTEM}>
                    系统分区
                  </Option>
                  <Option key={EPartitionType.INDEX} value={EPartitionType.INDEX}>
                    索引分区
                  </Option>
                  <Option key={EPartitionType.PACKET} value={EPartitionType.PACKET}>
                    全包分区
                  </Option>
                  <Option key={EPartitionType.METADATA} value={EPartitionType.METADATA}>
                    详单冷分区
                  </Option>
                  <Option key={EPartitionType.METADATA_HOT} value={EPartitionType.METADATA_HOT}>
                    详单热分区
                  </Option>
                </Select>
              </div>
            }
          >
            <TimeAxisChart
              brush={true}
              data={showDiskInput || []}
              startTime={displayStartTime}
              endTime={displayEndTime}
              interval={globalSelectedTime.interval}
              loading={diskInputLoading}
              // markArea={getMissingTimeArea(sensorDiskIO || [], '采集缺失') as any}
              unitConverter={(value: number) => {
                if (value !== undefined) {
                  return convertBandwidth(value);
                }
                return '采集缺失';
              }}
            />
          </Card>
        </Col>
        <Col span={12}>
          <Card
            size="small"
            title="探针磁盘写 TOP趋势"
            style={{ height: 360, marginBottom: '10px' }}
            bodyStyle={{
              height: 300,
              padding: 0,
            }}
            extra={
              <div>
                <Switch
                  style={{ marginRight: '10px' }}
                  checkedChildren="峰值"
                  unCheckedChildren="均值"
                  onChange={(checked) => {
                    if (checked) {
                      setDiskOutputType(EDiskIOType.WRITEBYTESPEAK);
                    } else {
                      setDiskOutputType(EDiskIOType.WRITEBYTES);
                    }
                  }}
                />
                <Select
                  value={patitionOutputType}
                  onChange={(type) => {
                    setPatitionOutputType(type);
                  }}
                  style={{ width: '150px' }}
                >
                  <Option key={EPartitionType.SYSTEM} value={EPartitionType.SYSTEM}>
                    系统分区
                  </Option>
                  <Option key={EPartitionType.INDEX} value={EPartitionType.INDEX}>
                    索引分区
                  </Option>
                  <Option key={EPartitionType.PACKET} value={EPartitionType.PACKET}>
                    全包分区
                  </Option>
                  <Option key={EPartitionType.METADATA} value={EPartitionType.METADATA}>
                    详单冷分区
                  </Option>
                  <Option key={EPartitionType.METADATA_HOT} value={EPartitionType.METADATA_HOT}>
                    详单热分区
                  </Option>
                </Select>
              </div>
            }
          >
            <TimeAxisChart
              brush={true}
              data={showDiskOutput || []}
              startTime={displayStartTime}
              endTime={displayEndTime}
              interval={globalSelectedTime.interval}
              loading={diskOutputLoading}
              // markArea={getMissingTimeArea(sensorDiskIO || [], '采集缺失') as any}
              unitConverter={(value: number) => {
                if (value !== undefined) {
                  return convertBandwidth(value);
                }
                return '采集缺失';
              }}
            />
          </Card>
        </Col>
      </Row>
    </>
  );
};

export default connect(({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
}))(Index);
