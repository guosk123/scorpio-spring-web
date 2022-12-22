import { EMetricApiType } from '@/common/api/analysis';
import { DEVICE_NETIF_STATE_UP } from '@/common/dict';
import ReactECharts from '@/components/ReactECharts';
import type { INetif, INetifAnalysis } from '@/pages/app/configuration/DeviceNetif/typings';
import { queryMetricAnalysysHistogram } from '@/services/app/analysis';
import { convertBandwidth, timeFormatter } from '@/utils/utils';
import { LoadingOutlined } from '@ant-design/icons';
import { Card, Radio, Spin, Tooltip } from 'antd';
import moment from 'moment';
import numeral from 'numeral';
import { useCallback, useEffect, useMemo, useState } from 'react';
import styles from './index.less';

interface IResult {
  rxBps: any[]; // 流入bps
  rxPps: any[]; // 流入数据包
  txBps: any[]; // 流出bps
  txPps: any[]; // 流出数据包
  maxBps: number;
  maxPps: number;
  timeData: string[];
}

interface ITimeInfo {
  startTime: string;
  endTime: string;
  interval: number;
}

interface IProps {
  from: string;
  to: string;
  netif: INetif;
}
const ManagerNetifStats = ({ from, to, netif }: IProps) => {
  // 是否展示的是bps
  const [showBps, setShowBps] = useState(true);
  const [result, setResult] = useState<IResult>({
    rxBps: [], // 流入bps
    rxPps: [], // 流入数据包
    txBps: [], // 流出bps
    txPps: [], // 流出数据包
    maxBps: 1024,
    maxPps: 100,
    timeData: [],
  });

  const [loading, setLoading] = useState<boolean>(false);

  const timeInfo: ITimeInfo = useMemo(() => {
    return timeFormatter(from, to);
  }, [from, to]);

  const handleResult = useCallback(
    (netifHistogram: INetifAnalysis[]) => {
      // 接收
      const rxBps: any[] = [];
      const rxPps: any[] = [];
      // 流出
      const txBps: any[] = [];
      const txPps: any[] = [];
      // 存储所有的点，用于计算最大的点
      const allBpsPoint = [];
      const allPpsPoint = [];

      const { interval } = timeInfo;
      const timeData: any[] = [];
      for (let i = 0; i < netifHistogram.length; i += 1) {
        const {
          timestamp,
          transmitBytes = 0,
          totalBytes = 0,
          transmitPackets = 0,
          totalPackets = 0,
        } = netifHistogram[i];
        timeData.push(timestamp);
        // 接收bps
        const rxBpsValue = (totalBytes * 8) / interval;
        rxBps.push(-rxBpsValue);
        allBpsPoint.push(+rxBpsValue);

        // 接收pps
        const rxPpsValue = Math.floor(totalPackets / interval);
        rxPps.push(-rxPpsValue);
        allPpsPoint.push(+rxPpsValue);

        // 流出bps
        const txBpsValue = (transmitBytes * 8) / interval;
        txBps.push(+txBpsValue);
        allBpsPoint.push(+txBpsValue);

        // 流出pps
        const txPpsValue = Math.floor(transmitPackets / interval);
        txPps.push(+txPpsValue);
        allPpsPoint.push(+txPpsValue);
      }

      // 计算最大的峰值
      const maxBps = Math.max.apply(null, allBpsPoint) || 1024;
      const maxPps = Math.max.apply(null, allPpsPoint) || 100;

      setResult((prevResult) => ({
        ...prevResult,
        rxBps,
        rxPps,
        txBps,
        txPps,
        maxBps,
        maxPps,
        timeData,
      }));

      setLoading(false);
    },
    [timeInfo],
  );

  const { name } = useMemo(() => netif, [netif]);

  const getData = useCallback(async () => {
    setLoading(true);
    // 获取统计
    const response = await queryMetricAnalysysHistogram({
      metricApi: EMetricApiType.netif,
      netifName: name,
      ...timeInfo,
    } as any);
    handleResult(response.success ? response.result : []);
  }, [handleResult, name, timeInfo]);

  useEffect(() => {
    getData();
  }, [getData]);

  const handleTypeChange = (e: any) => {
    const checked = e.target.value === '带宽';
    setShowBps(checked);
  };

  const option: any = useMemo(() => {
    const { maxBps, maxPps, rxBps, rxPps, txBps, txPps, timeData } = result;

    // ---- 显示bps时 ----
    // y轴
    let yAxis = {
      name: '带宽',
      type: 'value',
      max: maxBps,
      min: -maxBps,
      axisLabel: {
        formatter(value: any) {
          return convertBandwidth(Math.abs(value));
        },
      },
    };
    let seriesData = [
      {
        name: '发送带宽',
        type: 'bar',
        stack: 'one',
        data: txBps,
        color: '#4FA9FF',
      },
      {
        name: '接收带宽',
        type: 'bar',
        stack: 'one',
        data: rxBps,
        color: '#7CD7C4',
      },
    ];
    // ---- 显示数据包时 ----
    if (!showBps) {
      yAxis = {
        name: '数据包',
        type: 'value',
        max: maxPps,
        min: -maxPps,
        axisLabel: {
          formatter(value) {
            return `${Math.abs(value)}pps`;
          },
        },
      };
      seriesData = [
        {
          name: '发送数据包',
          type: 'bar',
          stack: 'two',
          data: txPps,
          color: '#CBAAFF',
        },
        {
          name: '接收数据包',
          type: 'bar',
          data: rxPps,
          stack: 'two',
          color: '#f38387',
        },
      ];
    }
    return {
      xAxis: [
        {
          type: 'category',
          data: timeData,
          axisLabel: {
            formatter(value: any) {
              return `${moment(value).format('MM-DD')}\n${moment(value).format('HH:mm:ss')}`;
            },
          },
        },
      ],
      yAxis,
      tooltip: {
        formatter(params: any[]) {
          const time = params[0].axisValue;
          let s = `<b>${moment(time).format('YYYY-MM-DD HH:mm:ss')}</b></br>`;
          params.forEach((item) => {
            s += item.marker;
            s += `${item.seriesName}: `;
            s += showBps
              ? convertBandwidth(Math.abs(item.value))
              : `${numeral(Math.abs(item.value)).format('0,0')}pps`;
            s += '</br>';
          });
          return s;
        },
      },
      legend: {
        enabled: false,
      },
      series: seriesData,
    };
  }, [result, showBps]);

  return (
    <Card
      className={styles.chartCard}
      size="small"
      title={
        <div>
          <Tooltip title={netif.state === DEVICE_NETIF_STATE_UP ? 'UP' : 'DOWN'}>
            <div
              className={[
                styles.statusIcon,
                netif.state === DEVICE_NETIF_STATE_UP ? styles.up : styles.down,
              ].join(' ')}
            />
          </Tooltip>
          流量管理接口：{netif.name}
        </div>
      }
      bodyStyle={{ padding: 10 }}
      extra={
        <div className={styles.extraWrapper}>
          <Radio.Group
            defaultValue="带宽"
            buttonStyle="solid"
            size="small"
            onChange={handleTypeChange}
          >
            <Radio.Button value="带宽">带宽</Radio.Button>
            <Radio.Button value="数据包">数据包</Radio.Button>
          </Radio.Group>
        </div>
      }
    >
      <div className={styles.content}>
        <div>
          {loading ? (
            <div className={styles.loading}>
              <Spin indicator={<LoadingOutlined style={{ fontSize: 24 }} spin />} />
            </div>
          ) : (
            <ReactECharts option={option} opts={{ height: 300 }} />
          )}
        </div>
      </div>
    </Card>
  );
};

export default ManagerNetifStats;
