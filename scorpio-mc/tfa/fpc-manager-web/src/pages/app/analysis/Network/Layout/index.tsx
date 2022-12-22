/* eslint-disable react-hooks/rules-of-hooks */
import TimeRangeSlider from '@/components/TimeRangeSlider';
import type { IPageLayout } from '@/layouts/PageLayout';
import PageLayout from '@/layouts/PageLayout';
import type { ConnectState } from '@/models/connect';
import { Result, Space } from 'antd';
import classNames from 'classnames';
import pathToRegexp from 'path-to-regexp';
import React, { useEffect, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, history, useLocation, useParams } from 'umi';
import type {
  ILogicalSubnet,
  ILogicalSubnetMap,
} from '../../../configuration/LogicalSubnet/typings';
import type { INetwork, INetworkMap } from '../../../configuration/Network/typings';
import SearchTree, { SEARCH_TREE_COLLAPSED_KEY } from '../../components/SearchTree';
import type { IUriParams } from '../../typings';
import type { INetworkTreeData } from '@/models/app/network';
import styles from './index.less';
import storage from '@/utils/frame/storage';
import { queryMetrics } from '@/services/app/monitor';
import moment from 'moment';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { markoldestPacketArea } from '@/utils/frame/utils';

interface INetworkLayout extends IPageLayout {
  allNetworks: INetwork[];
  allNetworkMap: INetworkMap;
  allLogicalSubnets: ILogicalSubnet[];
  allLogicalSubnetMap: ILogicalSubnetMap;
  networkTree: INetworkTreeData[];
  globalSelectedTime: Required<IGlobalTime>;
  dispatch: Dispatch;
}

const NetworkLayout: React.FC<INetworkLayout> = (props) => {
  const { allNetworkMap, allLogicalSubnetMap, networkTree, dispatch, globalSelectedTime } = props;
  const [collapsed, setCollapsed] = useState<boolean>(
    () => storage.get(SEARCH_TREE_COLLAPSED_KEY) === 'true',
  );
  const { pathname } = useLocation();
  const params: IUriParams = useParams();

  useEffect(() => {
    // dispatch({ type: 'networkModel/queryNetworkTree' });
  }, [dispatch]);

  useEffect(() => {
    queryMetrics().then((res) => {
      const { success, result } = res;
      if (success) {
        let tmp = {};
        const oldestTime = moment(
          parseInt(
            result.find((item: any) => {
              return item?.metricName === 'data_oldest_time';
            })?.metricValue || 0,
          ) * 1000,
        );
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
        dispatch({
          type: 'npmdModel/changeOldestPacketArea',
          payload: { beforeOldestPacketArea: tmp },
        });
      }
    });
    return () => {
      dispatch({
        type: 'npmdModel/changeOldestPacketArea',
        payload: { beforeOldestPacketArea: {} },
      });
    };
  }, [dispatch, globalSelectedTime.endTime, globalSelectedTime.startTime]);

  // 判断是否为网络分析，排除网络列表，拓扑图
  const isNetworkAnalysis =
    pathToRegexp(`(/embed)?/analysis/performance/network/:networkId/(.*)`).test(`${pathname}/`) &&
    pathname !== '/analysis/performance/network/list' &&
    pathname !== '/embed/analysis/performance/network/list' &&
    pathname !== '/analysis/performance/network/topology';

  // 参数配置页面，不需要显示时间轴
  const hideTimeRange =
    pathToRegexp(`(/embed)?/analysis/performance/network/:networkId/payload/baseline`).test(
      `${pathname}/`,
    ) ||
    pathToRegexp(`(/embed)?/analysis/performance/network/:networkId/performance/setting`).test(
      `${pathname}/`,
    ) ||
    pathToRegexp(
      `(/embed)?/analysis/performance/network/:networkId/tcp/connection/long-connection/setting`,
    ).test(`${pathname}/`) ||
    pathToRegexp(`(/embed)?/analysis/performance/network/:networkId/packet/analysis`).test(
      `${pathname}/`,
    );

  const handleTreeSelect = (selectedKeys: React.Key[]) => {
    if (selectedKeys.length === 0) {
      return;
    }
    history.push(pathname.replace(params.networkId!, selectedKeys[0] as string));
  };

  const handleCollapsed = (nextCollapsed: boolean) => {
    setCollapsed(nextCollapsed);
    storage.put(SEARCH_TREE_COLLAPSED_KEY, nextCollapsed);
  };

  if (
    // 是网络分析
    isNetworkAnalysis &&
    // URI 中不存在网络 ID
    (!params.networkId ||
      // 不在所有的网络中并且不在所有的子网中
      !{ ...allNetworkMap, ...allLogicalSubnetMap }[params.networkId])
  ) {
    return <Result status="warning" subTitle="网络不存在或已被删除" />;
  }

  if (!isNetworkAnalysis) {
    return (
      <>
        {!hideTimeRange && <TimeRangeSlider />}
        <PageLayout {...props} />
      </>
    );
  }

  return (
    <div className={classNames([styles.layoutWrap, collapsed && styles.collapsed])}>
      <div className={styles.leftWrap}>
        <SearchTree
          data={networkTree}
          selectedKeys={[params.networkId!]}
          onSelect={handleTreeSelect}
          collapsed={collapsed}
          onToggleCollapsed={handleCollapsed}
        />
      </div>
      <div className={styles.contentWrap}>
        <Space size="middle">
          <span className={styles.name}>
            {{ ...allNetworkMap, ...allLogicalSubnetMap }[params.networkId!]?.name}
          </span>
          {!hideTimeRange && <TimeRangeSlider />}
        </Space>
        <PageLayout {...props} />
      </div>
    </div>
  );
};

export default connect(
  ({
    networkModel: { allNetworks, networkTree, allNetworkMap },
    logicSubnetModel: { allLogicalSubnets, allLogicalSubnetMap },
    appModel: { globalSelectedTime, realTimeStatisticsFlag },
  }: ConnectState) => ({
    allNetworks,
    allNetworkMap,
    allLogicalSubnets,
    networkTree,
    allLogicalSubnetMap,
    globalSelectedTime,
    realTimeStatisticsFlag,
  }),
)(NetworkLayout);
