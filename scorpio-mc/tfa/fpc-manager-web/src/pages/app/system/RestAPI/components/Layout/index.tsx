import { ETimeType, getGlobalTime } from '@/components/GlobalTimeSelector';
import { queryUserList } from '@/pages/frame/system/User/services/users';
import type { IUser } from '@/pages/frame/system/User/tpyings';
import { Button, Row } from 'antd';
import moment from 'moment';
import type { FC, ReactNode } from 'react';
import { createContext, useCallback, useEffect, useState } from 'react';
import { history, useLocation } from 'umi';
import type { ITimeInfo } from '../../typings';
import DateRangePicker from '../DateRangePicker';

interface Props {
  children?: ReactNode;
}

export const RestStatCommonDataContext = createContext({
  startTime: moment().add(-30, 'minute').format(),
  endTime: moment().format(),
  interval: 60,
  userMap: {} as Record<string, IUser>,
});

const RestStatLayout: FC = (props: Props) => {
  const { pathname } = useLocation();
  const [timeInfo, setTimeInfo] = useState<ITimeInfo>({
    fromTime: moment().add(-30, 'minute').format(),
    toTime: moment().format(),
  });

  const [userMap, setUserMap] = useState<Record<string, IUser>>({});
  useEffect(() => {
    queryUserList().then((res) => {
      const { success, result } = res;
      if (success) {
        setUserMap(
          result.reduce((total, curr) => {
            return {
              ...total,
              [curr.id]: curr,
            };
          }, {}),
        );
      }
    });
  }, []);

  const [inRecord, setInRecord] = useState(false);

  useEffect(() => {
    setInRecord(pathname.indexOf('rest-api/record') > -1);
  }, [pathname]);

  const handleTimeChange = useCallback((start: string, to: string) => {
    setTimeInfo({
      fromTime: start,
      toTime: to,
    });
  }, []);

  return (
    <RestStatCommonDataContext.Provider
      value={{
        startTime: timeInfo.fromTime,
        endTime: timeInfo.toTime,
        userMap,
        interval: getGlobalTime({
          relative: false,
          type: ETimeType.CUSTOM,
          custom: [moment(timeInfo.fromTime), moment(timeInfo.toTime)],
        }).interval,
      }}
    >
      <div>
        <Row style={{ width: '100%', marginBottom: 6 }} justify={'space-between'}>
          <DateRangePicker onChange={handleTimeChange} />
          <span>
            {!inRecord && (
              <Button
                type="primary"
                onClick={() => history.push('/system/monitor/rest-api/record')}
              >
                详单
              </Button>
            )}
          </span>
        </Row>
        {props.children}
      </div>
    </RestStatCommonDataContext.Provider>
  );
};

export default RestStatLayout;
