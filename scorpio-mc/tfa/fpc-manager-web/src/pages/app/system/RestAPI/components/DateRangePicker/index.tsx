import { STATS_TIME_RANGE } from '@/common/dict';
import * as dateMath from '@/utils/frame/datemath';
import { Button, DatePicker } from 'antd';
import moment from 'moment';
import { useEffect, useRef, useState } from 'react';
import type { ITimeInfo } from '../../typings';

const { RangePicker } = DatePicker;
const TIME_FORMAT = 'YYYY-MM-DD HH:mm';

const getTimeInfo = (from: string) => ({
  fromTime: moment(moment(dateMath.parse(from)).format(TIME_FORMAT), TIME_FORMAT).format(),
  toTime: moment(moment().format(TIME_FORMAT), TIME_FORMAT).format(),
});

interface Props {
  onChange: (startTime: string, endTime: string) => void;
}

const DateRangePicker = ({ onChange }: Props) => {
  const [selectedTimeKey, setSelectedTimeKey] = useState<string>(STATS_TIME_RANGE[0].key);
  const [selectedTimeInfo, setSelectedTimeInfo] = useState<ITimeInfo>(getTimeInfo(selectedTimeKey));

  const pickerRef = useRef<any>();

  useEffect(() => {
    if (selectedTimeKey) {
      setSelectedTimeInfo(getTimeInfo(selectedTimeKey));
    }
  }, [selectedTimeKey]);

  useEffect(() => {
    onChange(selectedTimeInfo.fromTime, selectedTimeInfo.toTime);
  }, [onChange, selectedTimeInfo]);

  return (
    <RangePicker
      ref={pickerRef}
      allowClear={false}
      value={[moment(selectedTimeInfo.fromTime), moment(selectedTimeInfo.toTime)]}
      showTime
      onChange={(timeRange) => {
        if (timeRange && timeRange.length === 2) {
          setSelectedTimeInfo({
            fromTime: moment(timeRange[0]!.format(TIME_FORMAT), TIME_FORMAT).format(),
            toTime: moment(timeRange[1]!.format(TIME_FORMAT), TIME_FORMAT).format(),
          });
          setSelectedTimeKey('');
        }
      }}
      renderExtraFooter={() => {
        return (
          <>
            {STATS_TIME_RANGE.map((item) => {
              return (
                <Button
                  size={'small'}
                  type={item.key === selectedTimeKey ? 'primary' : 'link'}
                  key={item.key}
                  onClick={() => {
                    setSelectedTimeKey(item.key);
                    pickerRef.current.blur();
                  }}
                >
                  {item.name}
                </Button>
              );
            })}
          </>
        );
      }}
    />
  );
};

export default DateRangePicker;
