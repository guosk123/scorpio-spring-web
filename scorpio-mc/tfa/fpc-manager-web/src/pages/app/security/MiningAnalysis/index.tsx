import TimeAxisChart from '@/components/TimeAxisChart';
import type { TimeAxisChartData } from '@/components/TimeAxisChart/typing';
import type { ConnectState } from '@/models/connect';
import { AlertFilled } from '@ant-design/icons';
import { Card, Col, Row, Statistic } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import type { IGlobalSelectedTime } from 'umi';
import { useSelector } from 'umi';
import BarChart from '../components/BarChart';
import { queryAlertMessageHistogram, queryAlertMessageStatistics } from '../service';
import type {
  SuricataHistogramType,
  SuricataStatisticsResult,
  SuricataStatisticsType,
} from '../typings';

export const CHART_HEIGHT = 300;

type StatType<T> = Partial<Record<SuricataStatisticsType | SuricataHistogramType, T>>;

const Dashboard = () => {
  const [data, setData] = useState<StatType<SuricataStatisticsResult[]>>({} as any);

  const [loadings, setLoadings] = useState<StatType<boolean>>({});

  const [alarmTrendData, setAlarmTrendData] = useState<TimeAxisChartData[]>([]);

  const globalTime = useSelector<ConnectState, Required<IGlobalSelectedTime>>(
    (state) => state.appModel.globalSelectedTime,
  );

  useEffect(() => {
    const { startTime, endTime, interval } = globalTime;
    const statTypes: SuricataStatisticsType[] = [
      'top_mining_host',
      'top_mining_domain',
      'top_mining_pool_address',
    ];

    setLoadings({
      top_mining_host: true,
      top_mining_domain: true,
      top_mining_pool_address: true,
      mining_alarm_trend: true,
    });

    statTypes.map((type) => {
      queryAlertMessageStatistics({
        dsl: `type = ${
          type as SuricataStatisticsType
        } | gentimes timestamp start="${startTime}" end="${endTime}"`,
      }).then((res) => {
        if(res.success){
          setData((prev) => {
            return {
              ...prev,
              [type]: res.result,
            };
          });
          setLoadings((prev) => ({
            ...prev,
            [type]: false,
          }));
        }
      });
    });

    queryAlertMessageHistogram({
      dsl: `type = ${'mining_alarm_trend'} | gentimes timestamp start="${startTime}" end="${endTime}"`,
      interval,
    }).then((res) => {
      if(res.success){
        setAlarmTrendData(res?.result || []);
        setLoadings((prev) => ({
          ...prev,
          mining_alarm_trend: false,
        }));
      }
    });
  }, [globalTime, globalTime.endTime, globalTime.interval, globalTime.startTime]);

  // 计算data.alarm_trend之和
  const totalAlarmCount: number = useMemo(() => {
    return (
      alarmTrendData?.reduce((total: number, curr: any) => {
        return total + curr.count;
      }, 0) || 0
    );
  }, [alarmTrendData]);

  return (
    <Card
      title={
        <Statistic
          value={totalAlarmCount}
          prefix={
            <>
              <AlertFilled />
              <span style={{ fontSize: 14 }}>告警总数:</span>
            </>
          }
        />
      }
      bordered={false}
    >
      <Row gutter={[10, 10]} align="middle">
        <Col span={12}>
          <Card title="告警趋势(挖矿行为和挖矿域名)" size="small">
            <TimeAxisChart
              startTime={globalTime.startTimestamp}
              endTime={globalTime.endTimestamp}
              interval={globalTime.interval}
              data={alarmTrendData}
              brush={false}
              chartHeight={CHART_HEIGHT}
              nameMap={{ count: '告警总数' }}
              loading={loadings.mining_alarm_trend}
            />
          </Card>
        </Col>
        <Col span={12}>
          <BarChart
            chartType="top_mining_host"
            title="TOP10挖矿主机"
            data={data.top_mining_host || []}
            menus={['flow', 'DNS', 'alarm']}
            loading={loadings.top_mining_host}
          />
        </Col>
        <Col span={12}>
          <BarChart
            chartType="top_mining_domain"
            title="TOP10挖矿域名"
            data={data.top_mining_domain || []}
            menus={['flow', 'DNS', 'alarm']}
            loading={loadings.top_mining_domain}
          />
        </Col>

        <Col span={12}>
          <BarChart
            chartType="top_mining_pool_address"
            title="TOP10矿池地址"
            data={data.top_mining_pool_address || []}
            menus={['flow', 'DNS', 'alarm']}
            loading={loadings.top_mining_pool_address}
          />
        </Col>
      </Row>
    </Card>
  );
};

export default Dashboard;
