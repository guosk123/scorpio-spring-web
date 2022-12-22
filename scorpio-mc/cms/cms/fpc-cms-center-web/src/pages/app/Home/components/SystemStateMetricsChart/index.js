import ReactECharts, { timeAxis } from '@/components/ReactECharts';
import { timeFormatter } from '@/utils/utils';
import { message } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import PropTypes from 'prop-types';
import React, { PureComponent } from 'react';
import { chartColors } from '../../utils';

@connect(({ metricModel: { monitorMetricsHistogram }, loading }) => ({
  monitorMetricsHistogram,
  queryLoading: loading.effects['metricModel/queryMonitorMetrics'],
}))
class SystemStateMetricsChart extends PureComponent {
  static defaultProps = {
    canZoom: true,
  };

  static propTypes = {
    from: PropTypes.string.isRequired, // 开始时间
    to: PropTypes.string.isRequired, // 截止时间
    canZoom: PropTypes.bool, // 是否可以放缩
  };

  constructor(props) {
    super(props);
    this.state = {
      from: props.from,
      to: props.to,
    };
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    if (nextProps.from !== this.state.from || nextProps.to !== this.state.to) {
      this.setState(
        {
          from: nextProps.from,
          to: nextProps.to,
        },
        () => {
          this.queryMonitorMetrics();
        },
      );
    }
  }

  componentDidMount() {
    this.queryMonitorMetrics();
  }

  /**
   * 图表选择时间下钻
   * @param {String} from
   * @param {String} to
   */
  handleBrushEnd = (from, to) => {
    // 对齐时间
    const { startTime, endTime } = timeFormatter(from, to);
    if (to - from < 60 * 1000) {
      message.warning('框选时间间隔至少需要一分钟');
      return;
    }

    const { onTimeSelection } = this.props;
    if (onTimeSelection) {
      onTimeSelection({ from: startTime, to: endTime });
    }
  };

  queryMonitorMetrics = () => {
    const { from, to } = this.state;
    const { dispatch } = this.props;
    const { startTime, endTime, interval } = timeFormatter(from, to);
    // 获取统计
    dispatch({
      type: 'metricModel/queryMonitorMetrics',
      payload: {
        startTime,
        endTime,
        interval,
      },
    });
  };

  handleResult = () => {
    const { monitorMetricsHistogram } = this.props;
    const cpuUsageArr = [];
    const memoryUageArr = [];
    const systemFsUageArr = [];
    const metadataFsUsedArr = [];
    const indexFsUsedArr = [];

    monitorMetricsHistogram.forEach(
      ({
        timestamp,
        memoryUsedRatio = 0,
        cpuUsedRatio = 0,
        systemFsUsedRatio = 0,
        metadataFsUsedRatio = 0,
        indexFsUsedRatio = 0,
      }) => {
        const time = moment(timestamp).valueOf();
        memoryUageArr.push([time, memoryUsedRatio]);
        cpuUsageArr.push([time, cpuUsedRatio]);
        systemFsUageArr.push([time, systemFsUsedRatio]);
        metadataFsUsedArr.push([time, metadataFsUsedRatio]);
        indexFsUsedArr.push([time, indexFsUsedRatio]);
      },
    );

    return [
      {
        name: 'CPU使用率',
        type: 'line',
        symbol: 'none',
        data: cpuUsageArr,
      },
      {
        name: '内存使用率',
        type: 'line',
        symbol: 'none',
        data: memoryUageArr,
      },
      {
        name: '系统分区使用率',
        type: 'line',
        symbol: 'none',
        data: systemFsUageArr,
      },
      {
        name: '索引分区使用率',
        type: 'line',
        symbol: 'none',
        data: indexFsUsedArr,
      },
      {
        name: '应用层协议详单分区使用率',
        type: 'line',
        symbol: 'none',
        data: metadataFsUsedArr,
      },
    ];
  };

  render() {
    const seriesData = this.handleResult();

    const option = {
      xAxis: {
        ...timeAxis,
      },
      colors: chartColors,
      yAxis: {
        max: 100,
        min: 0,
        axisLabel: {
          formatter: '{value}%',
        },
      },
      tooltip: {
        formatter: (params) => {
          let label = `${params.lastItem.axisValueLabel}<br/>`;
          params.forEach((v) => {
            label += v.marker;
            label += `<span style="dispay: inline-block">${v.seriesName}：</span><span">${
              v.value[1] || 0
            }%</span>`;
            label += '<br/>';
          });
          return label;
        },
      },
      series: seriesData,
    };

    return (
      <ReactECharts
        option={option}
        opts={{ height: 300 }}
        onBrushEnd={this.props.onTimeSelection ? this.handleBrushEnd : undefined}
      />
    );
  }
}

export default SystemStateMetricsChart;
