import React, { PureComponent } from 'react';
import { Card, Radio } from 'antd';
import moment from 'moment';
import { timeRange, cardProps, defaultSelectedTime, dateStringParse } from '../../utils';
import * as dateMath from '@/utils/frame/datemath';
import SystemStateMetricsChart from '../SystemStateMetricsChart';
import { connect } from 'dva';

@connect(({ loading }) => ({
  queryLoading: loading.effects['metricModel/queryMonitorMetrics'],
}))
class MonitorMetricsChart extends PureComponent {
  constructor(props) {
    super(props);

    this.state = {
      selectedTime: defaultSelectedTime,
      from: dateStringParse(defaultSelectedTime),
      to: moment().format(),
    };
  }

  handleTimeChange = (newSelectedTime) => {
    this.setState({
      selectedTime: newSelectedTime,
      from: moment(dateMath.parse(newSelectedTime)).format(),
      to: moment().format(),
    });
  };

  handleTimeSelection = ({ from, to }) => {
    this.setState({
      selectedTime: undefined,
      from,
      to,
    });
  };

  render() {
    const { selectedTime, from, to } = this.state;
    const { queryLoading } = this.props;

    return (
      <Card
        {...cardProps}
        title="系统状态"
        extra={
          <Radio.Group
            value={selectedTime}
            buttonStyle="solid"
            size="small"
            disabled={queryLoading}
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
        <SystemStateMetricsChart from={from} to={to} onTimeSelection={this.handleTimeSelection} />
      </Card>
    );
  }
}

export default MonitorMetricsChart;
