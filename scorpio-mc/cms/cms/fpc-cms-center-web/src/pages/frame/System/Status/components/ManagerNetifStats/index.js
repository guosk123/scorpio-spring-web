import { DEVICE_NETIF_STATE_UP, STATS_TIME_RANGE } from '@/common/dict';
import ReactECharts from '@/components/ReactECharts';
import * as dateMath from '@/utils/frame/datemath';
import { convertBandwidth, processingSeconds } from '@/utils/frame/utils';
import { LoadingOutlined } from '@ant-design/icons';
import { Card, Radio, Spin, Tooltip } from 'antd';
import { connect } from 'dva';
import _ from 'lodash';
import moment from 'moment';
import numeral from 'numeral';
import { stringify } from 'qs';
import React, { PureComponent } from 'react';
import { withRouter } from 'umi';
import { timeFormatter } from '@/utils/utils';
import styles from './index.less';

// 管理接口图表历史状态展示
@withRouter
@connect()
class ManagerNetifStats extends PureComponent {
  state = {
    result: {
      isShowBps: true, // 是否展示的是bps
      rxBps: [], // 流入bps
      rxPps: [], // 流入数据包
      txBps: [], // 流出bps
      txPps: [], // 流出数据包
      maxBps: 1024,
      maxPps: 100,
    },
    loading: false,
  };

  componentDidMount() {
    const {
      location: { query },
    } = this.props;
    this.getData({ ...query });
  }

  componentWillReceiveProps(nextProps) {
    const { location } = this.props;
    const nextLocation = nextProps.location;
    const urlParams = `${stringify(location.query)}${location.hash}`;
    const nextUrlParams = `${stringify(nextLocation.query)}${nextLocation.hash}`;
    if (urlParams !== nextUrlParams) {
      this.getData(nextLocation.query);
    }
  }

  handleTypeChange = (e) => {
    const { result } = this.state;
    const checked = e.target.value === '带宽';
    this.setState({
      result: {
        ...result,
        isShowBps: checked,
      },
    });
  };

  getData = (query) => {
    this.setState({
      loading: true,
    });

    const { dispatch, data } = this.props;
    const { netifName, category } = data;

    const { from, to } = query;

    const timeToFormat = (time) => moment(time).format();

    // 默认的起止时间
    let startTime = timeToFormat(dateMath.parse(STATS_TIME_RANGE[0].key));
    let endTime = timeToFormat(moment().valueOf());

    if (from && to) {
      startTime = timeToFormat(dateMath.parse(from));
      endTime = timeToFormat(dateMath.parse(to));
    }

    // 对时间进行处理，取整秒
    // 秒数介于 0 - 30 之间， 取 00，
    // 秒数介于 30 - 59 之间， 取 30，
    startTime = processingSeconds(startTime);
    endTime = processingSeconds(endTime);

    // 计算时间间隔
    const { interval } = timeFormatter(startTime, endTime);

    // 获取统计
    dispatch({
      type: 'deviceNetifModel/queryDeviceNetifUsages',
      payload: {
        type: 'management-port',
        name: netifName,
        category,
        startTime,
        endTime,
        interval,
      },
    }).then((result) => {
      this.handleResult({
        result: _.cloneDeep(result),
        startTime,
        endTime,
        interval,
      });
    });
  };

  handleResult = ({ result }) => {
    if(result?.sort){
      result?.sort((a, b) => {
        return moment(a.metricTime).valueOf() - moment(b.metricTime).valueOf();
      });
    }
    // 接收
    const rxBps = [];
    const rxPps = [];
    // 流出
    const txBps = [];
    const txPps = [];
    // 存储所有的点，用于计算最大的点
    const allBpsPoint = [];
    const allPpsPoint = [];

    // 取出所有的点
    const timeData = [];

    for (let i = 0; i < result.length; i += 1) {
      const {
        metricTime,
        rxBps: itemRxbps,
        rxPps: itemRxPps,
        txBps: itemtxBps,
        txPps: itemtxPps,
      } = result[i];
      timeData.push(metricTime);
      // 接收bps
      rxBps.push(-itemRxbps);
      allBpsPoint.push(+itemRxbps);

      // 接收pps
      rxPps.push(-itemRxPps);
      allPpsPoint.push(+itemRxPps);

      // 流出bps
      txBps.push(+itemtxBps);
      allBpsPoint.push(+itemtxBps);

      // 流出pps
      txPps.push(+itemtxPps);
      allPpsPoint.push(+itemtxPps);
    }

    // 计算最大的峰值
    const maxBps = Math.max.apply(null, allBpsPoint) || 1024;
    const maxPps = Math.max.apply(null, allPpsPoint) || 100;

    const { result: prevResult } = this.state;

    this.setState({
      result: {
        ...prevResult,
        rxBps,
        rxPps,
        txBps,
        txPps,
        maxBps,
        maxPps,
        timeData,
      },
      loading: false,
    });
  };

  render() {
    const { data } = this.props;
    const { loading } = this.state;

    const option = () => {
      const {
        result: { isShowBps, maxBps, maxPps, rxBps, rxPps, txBps, txPps, timeData },
      } = this.state;

      // ---- 显示bps时 ----
      // y轴
      let yAxis = {
        name: '带宽',
        type: 'value',
        max: maxBps,
        min: -maxBps,
        axisLabel: {
          formatter(value) {
            return convertBandwidth(Math.abs(value));
          },
        },
      };
      let seriesData = [
        {
          name: '流出带宽',
          type: 'bar',
          stack: 'one',
          data: txBps,
          color: '#4FA9FF',
        },
        {
          name: '流入带宽',
          type: 'bar',
          stack: 'one',
          data: rxBps,
          color: '#7CD7C4',
        },
      ];
      // ---- 显示数据包时 ----
      if (!isShowBps) {
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
            name: '流出数据包',
            type: 'bar',
            stack: 'two',
            data: txPps,
            color: '#CBAAFF',
          },
          {
            name: '流入数据包',
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
              formatter(value) {
                return `${moment(value).format('MM-DD')}\n${moment(value).format('HH:mm:ss')}`;
              },
            },
          },
        ],
        yAxis,
        tooltip: {
          formatter(params) {
            const time = params[0].axisValue;
            let s = `<b>${moment(time).format('YYYY-MM-DD HH:mm:ss')}</b></br>`;
            params.forEach((item) => {
              s += item.marker;
              s += `${item.seriesName}: `;
              s += isShowBps
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
    };

    return (
      <Card
        className={styles.chartCard}
        size="small"
        title={
          <div>
            <Tooltip title={data.state === DEVICE_NETIF_STATE_UP ? 'UP' : 'DOWN'}>
              <div
                className={[
                  styles.statusIcon,
                  data.state === DEVICE_NETIF_STATE_UP ? styles.up : styles.down,
                ].join(' ')}
              />
            </Tooltip>
            流量管理接口：{data.netifName}
          </div>
        }
        bodyStyle={{ padding: 10 }}
        extra={
          <div className={styles.extraWrapper}>
            <Radio.Group
              defaultValue="带宽"
              buttonStyle="solid"
              size="small"
              onChange={this.handleTypeChange}
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
              <ReactECharts option={option()} opts={{ height: 300 }} />
            )}
          </div>
        </div>
      </Card>
    );
  }
}

export default ManagerNetifStats;
