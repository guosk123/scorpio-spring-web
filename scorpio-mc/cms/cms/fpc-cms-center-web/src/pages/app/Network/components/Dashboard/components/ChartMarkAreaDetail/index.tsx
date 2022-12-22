import { queryOldestTime } from '@/pages/app/Network/service';
import { markoldestPacketArea } from '@/utils/frame/utils';
import moment from 'moment';
import { useEffect } from 'react';

export default function ChartMarkAreaDetail(props: any) {
  const { networkId, networkGroupId, markAreaDetail, globalSelectedTime } = props;
  useEffect(() => {
    const param = { networkId, networkGroupId };
    queryOldestTime(param).then((res) => {
      const { success, result } = res;
      if (success) {
        let tmp = {};
        const oldestTime = moment(parseInt(result.dataOldestTime) * 1000);
        // 最早报文时间在选中的时间的开始时间之后，此时flag为true，图表中需要添加标记
        const haveDataFlag = oldestTime.isAfter(globalSelectedTime.startTime);
        if (haveDataFlag) {
          // 最早报文时间在选中的时间的结束时间之后，此时flag为true，结束时间应该是endtime
          const endTimeBeforeStartTime = oldestTime.isAfter(globalSelectedTime.endTime);
          if (endTimeBeforeStartTime) {
            tmp = markoldestPacketArea(
              moment(globalSelectedTime.startTime).valueOf(),
              oldestTime.valueOf(),
            );
          } else {
            tmp = markoldestPacketArea(
              moment(globalSelectedTime.startTime).valueOf(),
              oldestTime.valueOf(),
            );
          }
        }
        markAreaDetail(tmp);
      }
    });
  }, [globalSelectedTime, markAreaDetail, networkGroupId, networkId]);

  return <div style={{ display: 'none' }} />;
}
