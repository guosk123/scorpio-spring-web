import type { AppModelState } from '@/models/app/index';
import type { INetflowModel } from '../model';
import type { IDashbroad, IUrlParams } from '../typing';
import { bytesToSize, convertBandwidth, timeFormatter } from '@/utils/utils';
import { ETimeType, getGlobalTime, globalTimeFormatText } from '@/components/GlobalTimeSelector';
import { processingMinutes } from '../utils/timeTool';
import { Col, Row, Spin } from 'antd';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { connect, useParams } from 'umi';
import { lineChartConverter, barChartConverter, sessionFomatter } from '../utils/converter';
import { EDeviceType } from '../typing';
import type { Dispatch } from 'umi';
import { ERealTimeStatisticsFlag } from '@/models/app';
import { getTimeInterval } from '../utils/timeTool';
import { completeTimePoint } from '@/utils/utils';
import numeral from 'numeral';
import moment from 'moment';
import styles from './index.less';
import Loading from '@/components/Loading';
import MultipleSourceTrend from '../../analysis/components/MultipleSourceTrend';

interface IDashboardParams extends AppModelState {
  dispatch: Dispatch;
  location: { query: { netifSpeed: string } };
  selectedNetifSpeed: number;
  realTimeStatisticsFlag: ERealTimeStatisticsFlag;
  dashboardData: IDashbroad;
}

const Profiles = ({
  dispatch,
  globalSelectedTime,
  selectedNetifSpeed,
  realTimeStatisticsFlag,
  dashboardData,
}: IDashboardParams) => {
  // 获取路由参数
  const urlParams = useParams<IUrlParams>();
  // 加载标志
  const [loading, setLoading] = useState<boolean>(false);
  // 截断开始时间
  const cutStartTime = useMemo(() => {
    return processingMinutes(globalSelectedTime.originStartTime);
  }, [globalSelectedTime.originStartTime]);
  // 截断结束时间
  const cutEndTime = useMemo(() => {
    return processingMinutes(globalSelectedTime.originEndTime);
  }, [globalSelectedTime.originEndTime]);
  // 间隔时间interval
  const interval = useMemo(() => {
    return getTimeInterval(cutStartTime, cutEndTime);
  }, [cutEndTime, cutStartTime]);
  // 设备类型
  const deviceType = useMemo(
    () => (urlParams.netifNo === undefined ? EDeviceType.device : EDeviceType.interface),
    [urlParams],
  );
  // 接口速率信息
  const netifSpeed = useMemo(() => {
    return selectedNetifSpeed;
  }, [selectedNetifSpeed]);

  /** 格式化数据 */
  // 协议端口数据
  const protocolPortData = useMemo(() => {
    return dashboardData?.protocolPortBandwidthHistogram?.map((item) => {
      return { ...item, protocolPort: `${item.protocol}:${item.port}` };
    });
  }, [dashboardData?.protocolPortBandwidthHistogram]);

  // throughput数据
  const throughtPutData = useMemo(() => {
    return dashboardData?.transmitIngestBandwidthHistogram?.map((item) => ({
      ...item,
      ingest_name: '入口',
      transmit_name: '出口',
    }));
  }, [dashboardData?.transmitIngestBandwidthHistogram]);

  // 时间或url变动的时候更新数据
  useEffect(() => {
    if (deviceType === EDeviceType.interface && netifSpeed === undefined) {
      return;
    }
    if (deviceType === EDeviceType.device && netifSpeed !== undefined) {
      return;
    }
    (async () => {
      setLoading(true);
      dispatch({
        type: 'netflowModel/queryDashboard',
        payload: {
          startTime: globalSelectedTime.originStartTime ? cutStartTime : '',
          endTime: globalSelectedTime.originEndTime ? cutEndTime : '',
          deviceName: urlParams.deviceName,
          netifNo: urlParams.netifNo,
          count: 10,
          interval,
          netifSpeed,
        },
      });
      setLoading(false);
    })();
  }, [
    urlParams.deviceName,
    urlParams.netifNo,
    interval,
    netifSpeed,
    deviceType,
    globalSelectedTime.originStartTime,
    globalSelectedTime.originEndTime,
    cutStartTime,
    cutEndTime,
  ]);

  // 单位转换器
  const unitConverter = (value: number) => {
    if (value === undefined) {
      return convertBandwidth(0);
    }
    return convertBandwidth(value);
  };
  // 时间变动
  const updateGlobalTime = useCallback(
    (from: number, to: number) => {
      let timeObj;
      if ((to - from) / 1000 < 120) {
        const diffSeconds = 120 - (to - from) / 1000;
        const offset = diffSeconds / 2;
        timeObj = timeFormatter(from - offset * 1000, to + offset * 1000);
      } else {
        timeObj = timeFormatter(from, to);
      }

      dispatch({
        type: 'appModel/updateGlobalTime',
        payload: getGlobalTime({
          relative: false,
          type: ETimeType.CUSTOM,
          custom: [
            moment(timeObj.startTime, globalTimeFormatText),
            moment(timeObj.endTime, globalTimeFormatText),
          ],
        }),
      });
    },
    [dispatch],
  );
  // 时间补点
  const handleChartBrush = useMemo(() => {
    if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.OPEN) {
      return undefined;
    }
    return updateGlobalTime;
  }, [updateGlobalTime, realTimeStatisticsFlag]);

  return (
    <Row gutter={[10, 10]} className={styles.container}>
      <Col span={24}>
        <div className={styles.header_container}>
          <div className={styles.header_item}>
            <Spin spinning={loading}>
              <div>总流量</div>
              <h2>
                {dashboardData?.totalBytes !== undefined
                  ? bytesToSize(parseInt(dashboardData?.totalBytes as string, 10))
                  : 0}
              </h2>
            </Spin>
          </div>
          <div className={styles.header_item}>
            {loading ? (
              <Loading style={{ margin: 30 }} />
            ) : (
              <>
                <div>总包数</div>
                <h2>
                  {dashboardData?.totalPackets !== undefined
                    ? numeral(parseInt(dashboardData?.totalPackets as string, 10)).format('0,0')
                    : 0}
                </h2>
              </>
            )}
          </div>
          {deviceType === EDeviceType.interface ? (
            <>
              <div className={styles.header_item}>
                {loading ? (
                  <Loading style={{ margin: 30 }} />
                ) : (
                  <>
                    <div>入方向平均带宽利用率</div>
                    <h2>
                      {dashboardData?.ingestBandwidthRatio !== undefined
                        ? `${(parseFloat(dashboardData?.ingestBandwidthRatio) * 100).toFixed(2)}%`
                        : 0}
                    </h2>
                  </>
                )}
              </div>
              <div className={styles.header_item}>
                {loading ? (
                  <Loading style={{ margin: 30 }} />
                ) : (
                  <>
                    <div>出方向平均带宽利用率</div>
                    <h2>
                      {dashboardData?.transmitBandwidthRatio !== undefined
                        ? `${(parseFloat(dashboardData?.transmitBandwidthRatio) * 100).toFixed(2)}%`
                        : 0}
                    </h2>
                  </>
                )}
              </div>
            </>
          ) : null}
          <div className={styles.header_item}>
            {loading ? (
              <Loading style={{ margin: 30 }} />
            ) : (
              <>
                <div>平均总带宽</div>
                <h2>
                  {dashboardData?.totalBandwidth !== undefined
                    ? convertBandwidth(8 * parseInt(dashboardData?.totalBandwidth, 10))
                    : 0}
                </h2>
              </>
            )}
          </div>
          <div className={styles.header_item}>
            {loading ? (
              <Loading style={{ margin: 30 }} />
            ) : (
              <>
                <div>平均总包速率</div>
                <h2>
                  {dashboardData?.totalPacketSpeed !== undefined
                    ? `${numeral(dashboardData?.totalPacketSpeed as string).format('0,0')}pps`
                    : '0pps'}
                </h2>
              </>
            )}
          </div>
        </div>
      </Col>
      {deviceType === EDeviceType.interface && (
        <Col span={12}>
          <MultipleSourceTrend
            title="接口平均带宽"
            percentTableLabelTitle="协议包类型"
            percentTableValueTitle="包数"
            loading={loading}
            onBrush={handleChartBrush}
            data={(() => {
              const lineData = {
                ...lineChartConverter(throughtPutData, 'ingest_name', 'ingestBandwidth', undefined),
                ...lineChartConverter(
                  throughtPutData,
                  'transmit_name',
                  'transmitBandwidth',
                  undefined,
                ),
              };
              Object.keys(lineData).forEach((key) => {
                lineData[key] = completeTimePoint(
                  lineData[key],
                  moment(moment(cutStartTime).add(5, 'minutes')).format(),
                  cutEndTime,
                  interval,
                );
              });
              return {
                lineChartData: lineData,
                barChartData: {
                  ...barChartConverter(throughtPutData, 'ingest_name', 'ingestBandwidth'),
                  ...barChartConverter(throughtPutData, 'transmit_name', 'transmitBandwidth'),
                },
              };
            })()}
            unitConverter={unitConverter}
          />
        </Col>
      )}
      <Col span={12}>
        <MultipleSourceTrend
          title="协议端口 Top"
          percentTableLabelTitle="协议包类型"
          percentTableValueTitle="包数"
          loading={loading}
          onBrush={handleChartBrush}
          data={(() => {
            const lineData = lineChartConverter(
              protocolPortData,
              'protocolPort',
              'totalBandwidth',
              undefined,
            );
            Object.keys(lineData).forEach((key) => {
              lineData[key] = completeTimePoint(
                lineData[key],
                moment(moment(cutStartTime).add(5, 'minutes')).format(),
                cutEndTime,
                interval,
              );
            });
            return {
              lineChartData: lineData,
              barChartData: {
                ...barChartConverter(protocolPortData, 'protocolPort', 'totalBandwidth'),
              },
            };
          })()}
          unitConverter={unitConverter}
        />
      </Col>
      <Col span={12}>
        <MultipleSourceTrend
          title="平均总带宽 Top"
          percentTableLabelTitle="协议包类型"
          percentTableValueTitle="包数"
          loading={loading}
          onBrush={handleChartBrush}
          data={(() => {
            const lineData = lineChartConverter(
              dashboardData?.totalBandwidthHistogram,
              'ipAddress',
              'totalBandwidth',
              undefined,
            );
            Object.keys(lineData).forEach((key) => {
              lineData[key] = completeTimePoint(
                lineData[key],
                moment(moment(cutStartTime).add(5, 'minutes')).format(),
                cutEndTime,
                interval,
              );
            });
            return {
              lineChartData: lineData,
              barChartData: {
                ...barChartConverter(
                  dashboardData?.totalBandwidthHistogram,
                  'ipAddress',
                  'totalBandwidth',
                ),
              },
            };
          })()}
          unitConverter={unitConverter}
        />
      </Col>
      <Col span={12}>
        <MultipleSourceTrend
          title="平均发送带宽 Top"
          percentTableLabelTitle="协议包类型"
          percentTableValueTitle="包数"
          loading={loading}
          onBrush={handleChartBrush}
          data={(() => {
            const lineData = lineChartConverter(
              dashboardData?.transmitBandwidthHistogram,
              'ipAddress',
              'transmitBandwidth',
              undefined,
            );
            Object.keys(lineData).forEach((key) => {
              lineData[key] = completeTimePoint(
                lineData[key],
                moment(moment(cutStartTime).add(5, 'minutes')).format(),
                cutEndTime,
                interval,
              );
            });
            return {
              lineChartData: lineData,
              barChartData: {
                ...barChartConverter(
                  dashboardData?.transmitBandwidthHistogram,
                  'ipAddress',
                  'transmitBandwidth',
                ),
              },
            };
          })()}
          unitConverter={unitConverter}
        />
      </Col>
      <Col span={12}>
        <MultipleSourceTrend
          title="平均接收带宽 Top"
          percentTableLabelTitle="协议包类型"
          percentTableValueTitle="包数"
          loading={loading}
          onBrush={handleChartBrush}
          data={(() => {
            const lineData = lineChartConverter(
              dashboardData?.ingestBandwidthHistogram,
              'ipAddress',
              'ingestBandwidth',
              undefined,
            );
            Object.keys(lineData).forEach((key) => {
              lineData[key] = completeTimePoint(
                lineData[key],
                moment(moment(cutStartTime).add(5, 'minutes')).format(),
                cutEndTime,
                interval,
              );
            });
            return {
              lineChartData: lineData,
              barChartData: {
                ...barChartConverter(
                  dashboardData?.ingestBandwidthHistogram,
                  'ipAddress',
                  'ingestBandwidth',
                ),
              },
            };
          })()}
          unitConverter={unitConverter}
        />
      </Col>
      <Col span={12}>
        <MultipleSourceTrend
          title="会话 Top"
          percentTableLabelTitle="协议包类型"
          percentTableValueTitle="包数"
          loading={loading}
          onBrush={handleChartBrush}
          data={(() => {
            const lineData = lineChartConverter(
              dashboardData?.sessionBandwidthHistogram,
              'sessionId',
              'totalBandwidth',
              sessionFomatter,
            );
            Object.keys(lineData).forEach((key) => {
              lineData[key] = completeTimePoint(
                lineData[key],
                moment(moment(cutStartTime).add(5, 'minutes')).format(),
                cutEndTime,
                interval,
              );
            });
            return {
              lineChartData: lineData,
              barChartData: {
                ...barChartConverter(
                  dashboardData?.sessionBandwidthHistogram,
                  'sessionId',
                  'totalBandwidth',
                  sessionFomatter,
                ),
              },
            };
          })()}
          unitConverter={unitConverter}
        />
      </Col>
    </Row>
  );
};

export default connect(
  ({
    netflowModel: { selectedNetifSpeed, dashboardData },
    appModel: { globalSelectedTime, realTimeStatisticsFlag },
  }: INetflowModel) => {
    return {
      dashboardData,
      globalSelectedTime,
      selectedNetifSpeed,
      realTimeStatisticsFlag,
    };
  },
)(Profiles);
