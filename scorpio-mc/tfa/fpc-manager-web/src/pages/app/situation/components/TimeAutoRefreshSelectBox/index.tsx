import { useInterval } from 'ahooks';
import { DatePicker, Radio } from 'antd';
import moment from 'moment';
import { Fragment, useCallback, useEffect, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect } from 'umi';

enum ETimeBoxType {
  'default1h' = 'default1h',
  'default24h' = 'default24h',
  'default7d' = 'default7d',
  'custom' = 'custom',
}

const relativeTime = [
  {
    type: ETimeBoxType.default1h,
    label: '最近1小时',
  },
  {
    type: ETimeBoxType.default24h,
    label: '最近24小时',
  },
  {
    type: ETimeBoxType.default7d,
    label: '最近7天',
  },
  {
    type: ETimeBoxType.custom,
    label: '自定义',
  },
];

const defaultRefreshTime = {
  default1h: 60 * 1000,
  default24h: 5 * 60 * 1000,
  default7d: 60 * 60 * 1000,
};

const defaultTimeToHours = {
  default1h: -1,
  default24h: -24,
  default7d: -24 * 7,
};

interface Props {
  // getNewTimeInfo?: (time: any) => void;
  setSituationRefreshTimeInfo?: any;
  dispatch: Dispatch;
}

interface ICustomTime {
  startTime: string | undefined;
  endTime: string | undefined;
}

function TimeAutoRefreshSelectBox(props: Props) {
  const { dispatch } = props;
  // const timeRef = useRef<any>();
  const [timeSelect, setTimeSelect] = useState<ETimeBoxType>(ETimeBoxType.default1h);
  const [customTime, setCustomTime] = useState<ICustomTime>({ startTime: '', endTime: '' });
  const getMomentTime = (hour: number) => ({
    startTime: moment(moment().add(hour, 'h')).format(),
    endTime: moment().format(),
  });

  const setSituationRefreshTimeInfo = useCallback(
    (timeInfo: any) => {
      dispatch({ type: 'situationModel/setSituationRefreshTimeInfo', payload: timeInfo });
    },
    [dispatch],
  );

  useEffect(() => {
    if (timeSelect === ETimeBoxType.custom) {
      // 先判断自定义时间，再判断是否存在值
      if (customTime.startTime !== '' && customTime.endTime !== '') {
        setSituationRefreshTimeInfo(customTime);
      }
    }
  }, [customTime, setSituationRefreshTimeInfo, timeSelect]);

  useInterval(
    () => {
      setSituationRefreshTimeInfo(getMomentTime(defaultTimeToHours[timeSelect]));
    },
    timeSelect === ETimeBoxType.custom ? undefined : defaultRefreshTime[timeSelect],
    {
      immediate: true,
    },
  );

  // useEffect(() => {
  //   clearInterval(timeRef.current);
  //   if (timeSelect === ETimeBoxType.custom) {
  //     // 先判断自定义时间，再判断是否存在值
  //     if (customTime.startTime !== '' && customTime.endTime !== '') {
  //       setSituationRefreshTimeInfo(customTime);
  //     }
  //   } else {
  //     setSituationRefreshTimeInfo(getMomentTime(defaultTimeToHours[timeSelect]));
  //     timeRef.current = window.setInterval(() => {
  //       setSituationRefreshTimeInfo(getMomentTime(defaultTimeToHours[timeSelect]));
  //     }, defaultRefreshTime[timeSelect]);
  //   }
  //   return () => {
  //     clearInterval(timeRef.current);
  //   };
  // }, [customTime, setSituationRefreshTimeInfo, timeSelect]);

  return (
    <Fragment>
      <Radio.Group
        defaultValue={timeSelect}
        onChange={(e) => {
          setTimeSelect(e?.target?.value);
        }}
        style={{ whiteSpace: 'nowrap' }}
      >
        {relativeTime.map(({ label, type }) => (
          <Radio value={type} key={type}>
            {label}
          </Radio>
        ))}
      </Radio.Group>
      <DatePicker.RangePicker
        disabled={timeSelect !== ETimeBoxType.custom}
        onChange={(value) => {
          if (!value) {
            return;
          }

          setCustomTime({
            startTime: moment(value?.[0]?.format('YYYY-MM-DD HH:mm')).format(),
            endTime: moment(value?.[1]?.format('YYYY-MM-DD HH:mm')).format(),
          });
        }}
        size="small"
        showTime={{ format: 'HH:mm' }}
        format="YYYY-MM-DD HH:mm"
      />
    </Fragment>
  );
}
export default connect()(TimeAutoRefreshSelectBox);
