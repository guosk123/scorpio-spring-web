import ReactECharts from '@/components/ReactECharts';
import EChartsMessage from '@/components/Message';
import * as dateMath from '@/utils/frame/datemath';
import { bytesToSize, timeFormatter } from '@/utils/utils';
import { Card, Radio } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import React, { PureComponent } from 'react';
import {
  cardProps,
  chartHeight,
  chartLoading,
  computedPercent,
  dateStringParse,
  defaultSelectedTime,
  timeRange,
} from '../../utils';

const SIZE_LIMIT = 20;
const chartTitle = `应用流量Top${SIZE_LIMIT}`;

@connect(
  ({ homeModel: { applicationFlowCount }, SAKnowledgeModel: { allApplicationMap }, loading }) => ({
    applicationFlowCount,
    allApplicationMap,
    queryAllApplicationLoading: loading.effects['SAKnowledgeModel/queryAllApplications'],
  }),
)
class ApplicationFlowTop extends PureComponent {
  constructor(props) {
    super(props);

    this.state = {
      selectedTime: defaultSelectedTime,
      from: dateStringParse(defaultSelectedTime),
      to: moment().format(),
      loading: false,
    };
  }

  async componentDidMount() {
    // 获取所有的应用
    await this.queryAllApplications();
    this.queryData();
  }

  queryData = () => {
    this.setState(() => ({
      loading: true,
    }));
    const { dispatch } = this.props;
    const { from, to } = this.state;
    const { startTime, endTime, interval } = timeFormatter(from, to);
    dispatch({
      type: 'homeModel/queryApplicationFlowCount',
      payload: {
        startTime,
        endTime,
        interval,
        sortProperty: 'total_bytes',
        sortDirection: 'desc',
      },
    }).then(() => {
      this.setState(() => ({
        loading: false,
      }));
    });
  };

  handleTimeChange = (newSelectedTime) => {
    this.setState(
      {
        selectedTime: newSelectedTime,
        from: moment(dateMath.parse(newSelectedTime)).format(),
        to: moment().format(),
      },
      () => {
        this.queryData();
      },
    );
  };

  queryAllApplications = async () => {
    const { dispatch } = this.props;
    await dispatch({
      type: 'SAKnowledgeModel/queryAllApplications',
    });
  };

  handleResult = () => {
    const { applicationFlowCount = [], allApplicationMap } = this.props;
    const seriesData = [];
    const categories = [];

    const resultCopy = applicationFlowCount.slice();
    // // 根据总流量排序
    resultCopy.sort((a, b) => b.totalBytes - a.totalBytes);
    // 所有应用的流量
    let allBytes = 0;
    // 取前20个
    resultCopy.forEach((row, index) => {
      const { applicationId, totalBytes } = row;
      if (index < SIZE_LIMIT) {
        const applicationName = allApplicationMap.hasOwnProperty(applicationId)
          ? allApplicationMap[applicationId].nameText
          : applicationId;
        categories.push(applicationName);
        seriesData.push(totalBytes);
      }
      allBytes += totalBytes;
    });

    return { categories, seriesData, allBytes };
  };

  render() {
    const { selectedTime, loading } = this.state;
    const { queryAllApplicationLoading } = this.props;
    const { categories, seriesData, allBytes } = this.handleResult();

    const option = {
      xAxis: {
        type: 'category',
        data: categories,
        axisTick: {
          show: false,
          interval: 1,
        },
        axisLabel: {
          interval: 0,
          rotate: categories.length > 10 ? 45 : 0,
          formatter(value) {
            if (value.length > 10) {
              return `${value.slice(0, 10)}...`;
            }
            return value;
          },
        },
      },
      yAxis: [
        {
          type: 'value',
          axisLabel: {
            formatter: (value) => {
              return bytesToSize(value);
            },
          },
        },
      ],
      dataZoom: [
        {
          type: 'inside',
          minValueSpan: 5,
        },
      ],
      grid: {
        bottom: 10,
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
        },
        formatter: (params) => {
          const item = params[0];
          let label = `<div>${item.name}</div>`;
          label += item.marker;
          label += '流量: ';
          label += `${bytesToSize(item.value)}`;
          // 计算占比
          label += `（${computedPercent(item.value, allBytes)}）`;
          label += '</b><br/>';
          return label;
        },
      },
      toolbox: {},
      legend: {
        show: false,
      },
      series: [
        {
          name: '流量',
          type: 'bar',
          barWidth: '20px',
          data: seriesData,
        },
      ],
    };

    const renderContent = () => {
      if (queryAllApplicationLoading || loading) {
        return chartLoading;
      }
      if (seriesData.length === 0) {
        return <EChartsMessage height={chartHeight} />;
      }

      return <ReactECharts option={option} opts={{ height: chartHeight }} />;
    };

    return (
      <Card
        {...cardProps}
        title={chartTitle}
        extra={
          <Radio.Group
            value={selectedTime}
            buttonStyle="solid"
            size="small"
            disabled={loading}
            onChange={(e) => this.handleTimeChange(e.target.value)}
          >
            {timeRange.map((time) => (
              <Radio.Button key={time.key} value={time.key}>
                {time.label}
              </Radio.Button>
            ))}
          </Radio.Group>
        }
      >
        {renderContent()}
      </Card>
    );
  }
}

export default ApplicationFlowTop;
