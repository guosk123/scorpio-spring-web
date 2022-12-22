import { ipV4Regex, snakeCase } from "@/utils/utils";
import moment from "moment";

/**
 * 将时间缩放到输入时间的前1h <= timestamp => 后1h
 * @param startTime 时间选择器时间
 * @param endTime
 */
 export const timeScale = (
  timeStamp: string,
  startTime: number | string,
  endTime: number | string,
) => {
  const newEndTime = moment(timeStamp).add(1, 'h');
  const newStartTime = moment(timeStamp).subtract(1, 'h');
  // 当前时间大于新结束时间之后2分钟
  const isAfterClockTime = moment().isAfter(newEndTime.add(2, 'minute'));
  const res = { startTime: moment(startTime).valueOf(), endTime: moment(endTime).valueOf() };
  // 时间戳是否正常
  if (moment(timeStamp).isAfter(startTime) && moment(timeStamp).isBefore(endTime)) {
    if (newStartTime.isAfter(startTime)) {
      res.startTime = isAfterClockTime
        ? newStartTime.add(2, 'minute').valueOf()
        : newStartTime.valueOf();
    }
    // 新结束时间<当前结束时间
    if (newEndTime.isBefore(endTime)) {
      res.endTime = isAfterClockTime ? newEndTime.add(2, 'minute').valueOf() : newEndTime.valueOf();
    } else {
      const endTimeAddMin = moment(endTime).add(2, 'minute');
      // 当前时间大于当前结束时间+2min
      const flag = moment().isAfter(endTimeAddMin);
      res.endTime = flag ? endTimeAddMin.valueOf() : res.endTime;
    }
  }
  return res;
};


export const getFilterField = (field: string, value: string) => {
  // 区分源目的ipv4、ipv6
  let tmpField = '';
  if (field === 'src_ip' || field === 'dest_ip') {
    if (field === 'src_ip') {
      tmpField = ipV4Regex.test(value) ? 'src_ipv4' : 'src_ipv6';
    } else {
      tmpField = ipV4Regex.test(value) ? 'dest_ipv4' : 'dest_ipv6';
    }
    return tmpField;
  }
  return snakeCase(field);
};

