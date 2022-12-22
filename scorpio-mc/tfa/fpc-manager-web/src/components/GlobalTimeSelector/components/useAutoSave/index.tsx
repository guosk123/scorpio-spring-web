import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import storage from '@/utils/frame/storage';
import { useEffect, useMemo } from 'react';
import { useSelector } from 'umi';

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

export default function useAutoSaveTime() {
  const globalTime = useSelector<ConnectState, IGlobalTime>((state) => state.appModel.globalTime);

  const timeInfoObj = useMemo(() => {
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
  }, [globalTime]);

  useEffect(() => {
    autoSaveHistoryTime(timeInfoObj, globalTime);
  }, [globalTime, timeInfoObj]);
}
