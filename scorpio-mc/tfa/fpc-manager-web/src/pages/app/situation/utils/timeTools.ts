import { timeFormatter } from '@/utils/utils';
import moment from 'moment';
import type { ITimeInfo } from '../Network';

function currentTime() {
  return new Date(moment().valueOf());
}

// 获得整日
function getIntegTime(time: Date) {
  time.setHours(0);
  time.setMinutes(0);
  time.setSeconds(0);
  time.setMilliseconds(0);
  return time;
}
// 将字符串类型的时间转换为UTC格式
export function mapTimeToUTC(time: string | Date | number): string {
  return moment(time).format();
}

// 获得当天时间
export function getTodayTime(): [string, string] {
  return [mapTimeToUTC(getIntegTime(currentTime())), mapTimeToUTC(currentTime())];
}

// 获得昨天时间
export function getYesterDayTime(): [string, string] {
  const curTime = getIntegTime(currentTime());
  return [mapTimeToUTC(curTime.getTime() - 24 * 60 * 60 * 1000), mapTimeToUTC(curTime)];
}

// 获得前7天前时间
export function get7DaysBefore(): [string, string] {
  const curTime = currentTime();
  const curIntegTime = getIntegTime(currentTime());
  return [mapTimeToUTC(curIntegTime.getTime() - 7 * 24 * 60 * 60 * 1000), mapTimeToUTC(curTime)];
}

const currentHours = new Date(window.systemTime!).getHours();
const currentMinutes = new Date(window.systemTime!).getMinutes();

export const lastOneDay = timeFormatter(
  // 如果处于每天的凌晨0点5分钟以内，时间强制+5m
  moment().format('YYYY-MM-DD 00:00:00'),
  currentHours === 0 && currentMinutes < 5
    ? moment().format('YYYY-MM-DD HH:05:00')
    : moment().format('YYYY-MM-DD HH:mm:00'),
  /** 按照5分钟的统计粒度处理 */
);

export const lastOneHour = timeFormatter(
  moment().add(-1, 'h').format('YYYY-MM-DD HH:mm:ss'),
  moment().format('YYYY-MM-DD HH:mm:ss'),
  5 * 60,
);

export const oneHourTimer = {
  startTime: new Date(lastOneHour.startTime).getTime(),
  endTime: new Date(lastOneHour.endTime).getTime(),
  interval: lastOneHour.interval,
};

export const timeToNumber = (selectTimeInfo: ITimeInfo) => {
  return {
    startTime: new Date(selectTimeInfo.startTime).getTime(),
    endTime: new Date(selectTimeInfo.endTime).getTime(),
    interval: selectTimeInfo.interval,
  };
};