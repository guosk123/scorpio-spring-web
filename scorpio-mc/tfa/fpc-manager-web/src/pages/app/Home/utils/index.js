/* eslint-disable @typescript-eslint/no-invalid-this */
import React from 'react';
import { LoadingOutlined } from '@ant-design/icons';
import { message, Spin } from 'antd';
import moment from 'moment';
import * as dateMath from '@/utils/frame/datemath';
import { timeFormatter } from '@/utils/utils';

export const cardProps = {
  bordered: false,
  size: 'small',
  style: { marginBottom: 10 },
  bodyStyle: { paddingBottom: 10 },
};

export const customSpin = <Spin indicator={<LoadingOutlined spin />} />;

export const timeRange = [
  {
    key: 'now-30m',
    label: '最近30分钟',
    interval: 30,
  },
  {
    key: 'now-24h',
    label: '最近24小时',
    interval: 5 * 60,
  },
  {
    key: 'now-7d',
    label: '最近7天',
    interval: 30 * 60,
  },
];

export const chartColors = [
  '#1890FF',
  '#2FC25B',
  '#FACC14',
  '#223273',
  '#8543E0',
  '#13C2C2',
  '#3436C7',
  '#F04864',
];

export const chartHeight = 260;

export const spinIndicator = <LoadingOutlined style={{ fontSize: 24 }} spin />;
export const chartLoading = (
  <div
    style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: chartHeight }}
  >
    <Spin indicator={spinIndicator} />
  </div>
);

/**
 * 默认选择的时间
 */
export const defaultSelectedTime = 'now-30m';

/**
 * 查询统计
 * @param {String} dispatchType 查询的 api
 */
export function queryHistogram(dispatchType, params = {}) {
  const { dispatch } = this.props;
  const { from, to } = this.state;
  const { startTime, endTime, interval } = timeFormatter(from, to);
  // 获取统计
  dispatch({
    type: dispatchType,
    payload: {
      startTime,
      endTime,
      interval,
      ...params,
    },
  });
}

/**
 * 切换时间，查询数据
 * @param {String} newSelectedTime 时间变化
 */
export function handleTimeChange(newSelectedTime) {
  this.setState(
    {
      selectedTime: newSelectedTime,
      from: moment(dateMath.parse(newSelectedTime)).format(),
      to: moment().format(),
    },
    () => {
      this.queryHistogram(this.state.dispatchType);
    },
  );
}

/**
 * 相对时间格式化成ISO标准时间
 * @param {String} dateString
 * @eg now-24h => 2019-09-24T18:50:00+08:00
 */
export const dateStringParse = (dateString) => moment(dateMath.parse(dateString)).format();

/**
 * 图表选择时间下钻
 * @param {String} from
 * @param {String} to
 */
export function handleChartSelection(event) {
  event.preventDefault();
  const { min, max } = event.xAxis[0];
  if (!min || !max) {
    return;
  }
  if (max - min < 30 * 1000) {
    message.warning('时间间隔至少为30s');
    return;
  }

  const from = moment(min).format();
  const to = moment(max).format();
  this.setState(
    {
      selectedTime: undefined,
      from,
      to,
    },
    () => {
      this.queryHistogram(this.state.dispatchType);
    },
  );
}

/**
 * 图表导出时的配置项
 * @param {String} chartTitle 图表标题
 */
export function exportingChartOptions({ chartTitle, from, to }) {
  const { startTime, endTime, interval } = timeFormatter(from, to);

  return {
    title: {
      text: chartTitle,
    },
    subtitle: {
      text: `${moment(startTime).format('YYYY-MM-DD HH:mm:ss')} ~ ${moment(
        moment(endTime).add(interval, 'seconds'),
      ).format('YYYY-MM-DD HH:mm:ss')}`,
    },
  };
}

/**
 * 计算百分比
 * @param {Number} count
 * @param {Number} totalCount
 */
export const computedPercent = (count, totalCount) => {
  if (count === 0 || totalCount === 0) {
    return '0%';
  }
  const percent = ((count / totalCount) * 100).toFixed(2);
  if (percent < 0.01) {
    return '<0.01%';
  }
  return `${percent}%`;
};
