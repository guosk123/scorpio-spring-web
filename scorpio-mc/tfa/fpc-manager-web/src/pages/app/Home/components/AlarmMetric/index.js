import { ChartCard } from '@/components/Charts';
import { connect } from 'dva';
import React, { PureComponent } from 'react';
import alarmIcon from '../../assets/alarm.svg';
import { customSpin } from '../../utils';

/**
 * 告警数量统计
 */
@connect(({ alarmModel: { countAlarmByLevel }, loading }) => ({
  countAlarmByLevel,
  queryLoading: loading.effects['alarmModel/countGroupbyLevel'],
}))
class AlarmMetric extends PureComponent {
  componentDidMount() {
    this.queryAlarmLevel();
  }

  // 告警等级分布
  queryAlarmLevel = () => {
    const { dispatch } = this.props;
    dispatch({
      type: 'alarmModel/countGroupbyLevel',
    });
  };

  render() {
    // 统计
    const { countAlarmByLevel, chartProps, queryLoading } = this.props;

    let total = 0;
    if (Array.isArray(countAlarmByLevel)) {
      countAlarmByLevel.forEach((item) => {
        total += item.count;
      });
    }

    return (
      <ChartCard
        {...chartProps}
        title="系统告警数量"
        avatar={<img alt="indicator" style={{ width: 56, height: 56 }} src={alarmIcon} />}
        total={() => (queryLoading ? customSpin : total)}
      />
    );
  }
}

export default AlarmMetric;
