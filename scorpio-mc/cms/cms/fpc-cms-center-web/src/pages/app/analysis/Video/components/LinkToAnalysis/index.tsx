import type { Dispatch } from 'umi';
import { useCallback, useContext, useEffect, useMemo } from 'react';
import { connect, useLocation } from 'umi';
import { EVideoTabType } from '../../typings';
import { history } from 'umi';
import { VideoTabsContext } from '../VideoEditTabs';
import { openNewVideoTab } from '../VideoEditTabs/constant';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeUnit, globalTimeFormatText } from '@/components/GlobalTimeSelector';
import { ETimeType, getGlobalTime } from '@/components/GlobalTimeSelector';
import moment from 'moment';

function LinkToAnalysis({ dispatch: modelDispatch }: { dispatch: Dispatch }) {
  const location = useLocation() as any;

  const from = useMemo(() => {
    return parseInt(location?.query?.from);
  }, [location?.query?.from]);

  const to = useMemo(() => {
    return parseInt(location?.query?.to);
  }, [location?.query?.to]);

  const [state, dispatch] = useContext(VideoTabsContext);

  const changeGlobalTime = useCallback(() => {
    if (!from || !to) {
      return;
    }
    // 修改全局时间
    const timeObj: IGlobalTime = {
      relative: false,
      type: ETimeType.CUSTOM,
      last: {
        range: 30,
        unit: ETimeUnit.MINUTES,
      },
      custom: undefined,
    };

    const time1 = moment(from).format(globalTimeFormatText);
    const time2 = moment(to).format(globalTimeFormatText);
    timeObj.custom = [moment(time1), moment(time2)];
    if (modelDispatch) {
      modelDispatch({
        type: 'appModel/updateGlobalTime',
        payload: getGlobalTime(timeObj),
      });
    }
  }, [from, to]);

  useEffect(() => {
    changeGlobalTime();
  }, [changeGlobalTime]);

  useEffect(() => {
    if (location?.query?.jumpTabs) {
      openNewVideoTab(
        state,
        dispatch,
        EVideoTabType[location.query.jumpTabs.toLocaleUpperCase()],
        {
          filter: location?.query?.filter,
          from: location?.query?.from,
          to: location?.query?.to,
        },
        location?.query?.tabTitle,
      );
      const tmpUrlQuery = location?.query;
      delete tmpUrlQuery.filter;
      delete tmpUrlQuery.jumpTabs;
      history.replace({
        query: tmpUrlQuery,
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  return <div />;
}

export default connect()(LinkToAnalysis);
