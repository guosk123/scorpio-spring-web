// import { green, red } from '@ant-design/colors';
import { Tooltip } from 'antd';
import type { IGlobalSelectedTime } from 'umi';
import React, { useEffect, useMemo, useState } from 'react';
import styles from '../../index.less';
import { history } from 'umi';
import type { IAlarmDetail } from '@/pages/app/analysis/Service/List/typings';
import moment from 'moment';
import { queryAlarmDashboardMegs } from '../../../../service';

interface IServiceAlarmRateProps {
  selectedTime: IGlobalSelectedTime;
  networkGroupId?: string;
  networkId?: string;
  serviceId: string;
}

const AlarmRateDistribution: React.FC<IServiceAlarmRateProps> = ({
  selectedTime,
  networkGroupId,
  serviceId,
  networkId,
}) => {
  const totalAlarmCounts: number = 15;

  const [totalAlarms, setTotalAlarms] = useState<IAlarmDetail[]>([]);

  const queryAlarmParams = useMemo(() => {
    const startTime = moment(selectedTime.startTime);
    const endTime = moment(selectedTime.endTime);
    const timeBlock = endTime.diff(startTime, 'minutes');
    const newIntervalTimes = Math.ceil(timeBlock / totalAlarmCounts);
    const newInterval = newIntervalTimes * (selectedTime?.interval ? selectedTime?.interval : 60);
    return {
      startTime: selectedTime.startTime,
      endTime: selectedTime.endTime,
      interval: newInterval,
      networkId: networkId?.includes('^') ? networkId.split('^')[1] : networkId,
      networkGroupId: networkGroupId,
      serviceId: serviceId,
    };
  }, [
    networkGroupId,
    networkId,
    selectedTime.endTime,
    selectedTime?.interval,
    selectedTime.startTime,
    serviceId,
  ]);

  useEffect(() => {
    let mounted = true;
    queryAlarmDashboardMegs(queryAlarmParams).then((res) => {
      const { success, result } = res;
      if (success) {
        // console.log(result, 'Alarmres', Array.isArray(result),'result的类型');
        if (Array.isArray(result) || Object.prototype.toString.call(result) === '[object Array]') {
          if(mounted){
            setTotalAlarms(result);
          }
        }
      } else {
        setTotalAlarms([]);
      }
    });
    return () => {
      mounted = false;
    };
  }, [queryAlarmParams]);

  const alarmData = useMemo(() => {
    // console.log(totalAlarms.length, '告警的次数');
    const nonAlarmCount = new Array(totalAlarmCounts - totalAlarms.length).fill({});
    if (totalAlarms.length < totalAlarmCounts && totalAlarms.length > 0) {
      return totalAlarms.concat(nonAlarmCount);
    }
    if (totalAlarms.length === totalAlarmCounts) {
      return totalAlarms;
    }
    return nonAlarmCount;
  }, [totalAlarms]);

  const jumpToAlarmList = () => {
    history.push(`/analysis/network/${networkId}/alert`);
  };
  return (
    <>
      <div className={styles.stat__footer__alarm}>
        {totalAlarms.length > 0
          ? alarmData.map((item: any) => {
              if (item.alertCount) {  
                return (
                  <>
                    <Tooltip
                      title={
                        <span>
                          开始时间: {item.ariseTime}
                          <br />
                          {/* 结束时间: {item.endTime}
                          <br /> */}
                          告警: {item.alertCount}(个)
                        </span>
                      }
                    >
                      <div
                        key={item.id}
                        className={styles.stat__footer__alarm__alarmbox}
                        onClick={jumpToAlarmList}
                      />
                    </Tooltip>
                  </>
                );
              } else {
                return <div className={styles.stat__footer__alarm__noalarmbox} />;
              }
            })
          : alarmData.map(() => {
              return <div className={styles.stat__footer__alarm__green} />;
            })}
      </div>
    </>
  );
};

export default AlarmRateDistribution;