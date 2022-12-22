import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
import storage from '@/utils/frame/storage';
import { useEffect } from 'react';

interface Props {
  globalTime: IGlobalTime;
}

export const autoSaveHistoryTime = (timeInfoObj: any, globalTime: IGlobalTime) => {
  const oldHistoryTimeArr = JSON.parse(storage.get('timeHistory') || '[]');
  const timeInfoName = globalTime.timeLabel;
  // 最近记录时间段相同不存储
  // if (oldHistoryTimeArr.find((item: any) => item.timeInfoName === timeInfoName)) {
  //   return;
  // }
  if (
    oldHistoryTimeArr[oldHistoryTimeArr.length - 1] &&
    oldHistoryTimeArr[oldHistoryTimeArr.length - 1].timeInfoName === timeInfoName
  ) {
    return;
  }
  if (oldHistoryTimeArr.length > 21) {
    oldHistoryTimeArr.shift();
  }
  const timeObj = {
    timeInfoName,
    info: timeInfoObj,
  };
  storage.put('timeHistory', JSON.stringify([...oldHistoryTimeArr, timeObj]));
};

export default function AutoSave(props: Props) {
  const { globalTime } = props;
  const timeInfoObj = () => {
    let res: any = {};
    if (globalTime.relative && globalTime.type === ETimeType.RANGE) {
      res = {
        relative: globalTime.relative,
        timeType: globalTime.type,
        from: globalTime.last?.range,
        unit: globalTime.last?.unit,
      };
    } else if (globalTime.relative) {
      res = { relative: globalTime.relative, timeType: globalTime.type };
    } else {
      res = {
        from: globalTime.startTimestamp,
        to: globalTime.endTimestamp,
        relative: globalTime.relative,
        timeType: globalTime.type,
      };
    }
    if (res.type === 'default30m') {
      res.type = 'custom';
    }
    return res;
  };

  useEffect(() => {
    autoSaveHistoryTime(timeInfoObj(), globalTime);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [globalTime]);

  return <div style={{ display: 'none' }} />;
}
