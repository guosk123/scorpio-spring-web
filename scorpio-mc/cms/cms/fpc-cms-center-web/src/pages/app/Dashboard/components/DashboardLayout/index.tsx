import { ETimeType, ETimeUnit, getGlobalTime } from '@/components/GlobalTimeSelector';
import TimeRangeSlider from '@/components/TimeRangeSlider';
import type { BasicLayoutProps } from '@/layouts/BasicLayout';
import PageLayout from '@/layouts/PageLayout';
import { useEffect } from 'react';
import type { Dispatch } from 'umi';
import { connect, history } from 'umi';

export interface ITimeLayout {
  dispatch: Dispatch;
  location: {
    pathname: string;
  };
  route: BasicLayoutProps['route'] & {
    authority: string[];
  };
  match: {
    path: string;
    url: string;
  };
  children?: React.ReactNode;
}

const Index: React.FC<ITimeLayout> = ({
  dispatch,
  route,
  match,
  location = {
    pathname: '/',
  },
  children,
}) => {
  const resetTime = () => {
    // console.log(
    //   'refresh',
    //   getGlobalTime({
    //     relative: false,
    //     type: ETimeType.RANGE,
    //     last: {
    //       unit: ETimeUnit.DAYS,
    //       range: 1,
    //     },
    //   }),
    // );
    dispatch({
      type: 'appModel/updateGlobalTime',
      payload: getGlobalTime({
        relative: false,
        type: ETimeType.RANGE,
        last: {
          unit: ETimeUnit.DAYS,
          range: 1,
        },
      }),
    });
  };

  // useEffect(() => {
  //   setTimeout(() => {
  //     resetTime();
  //   });
  //   return () => {
  //     history.replace({});
  //   };
  // }, []);

  return (
    <>
      <TimeRangeSlider />
      <PageLayout location={location as any} route={route} match={match} children={children} />
    </>
  );
};

export default connect()(Index);
