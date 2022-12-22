import { globalTimeFormatText } from '@/components/GlobalTimeSelector';
import { useInterval } from 'ahooks';
import { DatePicker, Radio } from 'antd';
import moment from 'moment';
import { Fragment, useCallback, useEffect, useState } from 'react';

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

interface ICustomTime {
  startTime: string | undefined;
  endTime: string | undefined;
}

interface Props {
  // getNewTimeInfo?: (time: any) => void;
  onChange: (timeInfo: ICustomTime) => void;
}

function TimeAutoRefreshSelectBox({ onChange }: Props) {
  // const timeRef = useRef<any>();
  const [timeSelect, setTimeSelect] = useState<ETimeBoxType>(ETimeBoxType.default1h);
  const [customTime, setCustomTime] = useState<ICustomTime>({ startTime: '', endTime: '' });
  const getMomentTime = (hour: number) => ({
    startTime: moment(moment().add(hour, 'h')).format(),
    endTime: moment().format(),
  });

  const setSituationRefreshTimeInfo = useCallback(
    (timeInfo: ICustomTime) => {
      onChange(timeInfo);
    },
    [onChange],
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
          const startTime = moment(value?.[0]?.format(), globalTimeFormatText).format();
          const endTime = moment(value?.[1]?.format(), globalTimeFormatText).format();
          setCustomTime({
            startTime,
            endTime,
          });
        }}
        size="small"
        showTime={{ format: 'HH:mm' }}
        format="YYYY-MM-DD HH:mm:00"
      />
    </Fragment>
  );
}
export default TimeAutoRefreshSelectBox;
