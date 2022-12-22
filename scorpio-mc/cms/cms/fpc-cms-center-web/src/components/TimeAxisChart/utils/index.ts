import moment from 'moment';
import type { TimeAxisChartData } from '../typing';

export const TIME_AXIS_LABEL_MAX_LENGTH = 12 * 6;

/**
 * @param timeData 已有的时间点数据，带unix时间戳数组
 * @param start 图表时间轴的开始时间，unix时间戳
 * @param end 图表时间轴的结束时间，unix时间戳
 * @param containerWidth 图表容器的宽度
 * @param interval 时间点之间的间隔,单位为秒，默认值1
 */
export const fillTimeAxisPoint = (
  data: TimeAxisChartData[],
  start: number,
  end: number,
  interval: number = 60,
) => {
  if (data.length < 1) {
    return [];
  }

  const totalPoints = Math.floor((end - start) / (interval * 1000));
  if (totalPoints + 1 === data.length || totalPoints === data.length) {
    return data;
  }

  const timePoints: TimeAxisChartData[] = [];
  // const now = moment().valueOf();

  const empty = Object.keys(data[0]).reduce((prev, curr) => {
    return {
      ...prev,
      [curr]: 0,
    };
  }, {});

  for (let i = 0; i < data.length; i += 1) {
    const currentTime = data[i].timestamp;
    const idx = Math.floor((moment(currentTime).valueOf() - start) / 1000 / interval);
    if (idx >= 0 && idx <= totalPoints) {
      timePoints[idx] = data[i];
    }
  }

  for (let i = 0; i < totalPoints; i += 1) {
    if (!timePoints[i]) {
      timePoints[i] = { ...empty, timestamp: start + interval * i * 1000 };
    }
  }

  // 判断是不是最近xxx， 如果是，则
  // if (now - end < interval * 1000) {
  //   return timePoints;
  // }

  // if (timePoints[totalPoints]) {
  //   timePoints[totalPoints] = { ...empty, timestamp: start + interval * totalPoints * 1000 };
  // }

  return timePoints;
};
