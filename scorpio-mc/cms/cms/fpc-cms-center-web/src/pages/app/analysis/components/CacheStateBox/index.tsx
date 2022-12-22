import type { ConnectState } from '@/models/connect';
import { connect } from 'dva';
import { Fragment, useEffect } from 'react';

interface Props {
  cacheState?: any;
  onNewState?: (state: any) => void;
  /** 当实时刷新时该方法返回true */
  onCacheModelFlag?: (flag: boolean) => void;
  /** 允许刷新为true，不允许为false */
  isRefreshFlag?: boolean;
  realTimeStatisticsFlag?: any;
}

/**
 *
 * @param flag 判断是否需要loading的标识
 * @param loading 当前组件的loading状态
 * @returns
 */
export const stopLoad = (flag: boolean, loading: boolean) => {
  return flag ? false : loading;
};

function CacheStateBox(props: Props) {
  const {
    cacheState: cache,
    onNewState,
    onCacheModelFlag,
    isRefreshFlag = true,
    realTimeStatisticsFlag,
  } = props;

  useEffect(() => {
    if (onCacheModelFlag) {
      onCacheModelFlag(realTimeStatisticsFlag === '1');
    }
  }, [realTimeStatisticsFlag, onCacheModelFlag]);

  useEffect(() => {
    if (onNewState && isRefreshFlag) {
      onNewState(cache);
    }
  }, [isRefreshFlag, onNewState, cache]);

  return <Fragment />;
}
export default connect((state: ConnectState) => {
  const {
    npmdModel: { isRefreshFlag },
    appModel: { realTimeStatisticsFlag },
  } = state;
  return { isRefreshFlag, realTimeStatisticsFlag };
})(CacheStateBox);
