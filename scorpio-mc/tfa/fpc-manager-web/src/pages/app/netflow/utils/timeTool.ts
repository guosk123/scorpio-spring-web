import moment from 'moment';

/** 获取时间间隔 */
export function getTimeInterval(startTime: string, endTime: string) {
  const diffTime = moment(endTime).diff(startTime) / 1000;
  if (diffTime <= 86400) {
    return 300;
  }
  if (diffTime > 86400) {
    return 3600;
  }
  return 0;
}

/**
 * 格式化分钟时间
 * 分钟数取整5分钟
 * 分钟数：00, 05, 10, 15 ....
 * @param {String} time
 * @param {number} interval
 */
export function processingMinutes(time: string | number, interval = 5) {
  const timeMinutes = new Date(time).getMinutes();
  const timeText = moment(time).format('YYYY-MM-DD HH:00:00');
  let corrected = 0;
  if (timeMinutes % interval === 0) {
    corrected = Math.floor(timeMinutes / interval - 1) * interval;
  } else {
    corrected = Math.floor(timeMinutes / interval) * interval;
  }
  return moment(moment(timeText).add(corrected, 'minutes')).format();
}
