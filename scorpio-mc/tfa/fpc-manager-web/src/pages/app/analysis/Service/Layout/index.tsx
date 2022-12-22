import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import TimeRangeSlider from '@/components/TimeRangeSlider';
import type { IPageLayout } from '@/layouts/PageLayout';
import PageLayout from '@/layouts/PageLayout';
import type { ConnectState } from '@/models/connect';
import type { ILogicalSubnetMap } from '@/pages/app/configuration/LogicalSubnet/typings';
import type { INetworkMap } from '@/pages/app/configuration/Network/typings';
import { queryMetrics } from '@/services/app/monitor';
import storage from '@/utils/frame/storage';
import { markoldestPacketArea } from '@/utils/frame/utils';
import { Result, Space } from 'antd';
import classNames from 'classnames';
import moment from 'moment';
import pathToRegexp from 'path-to-regexp';
import React, { useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, history, useLocation, useParams } from 'umi';
import type { IService, IServiceMap } from '../../../configuration/Service/typings';
import SearchTree, { SEARCH_TREE_COLLAPSED_KEY } from '../../components/SearchTree';
import type { IUriParams } from '../../typings';
import styles from './index.less';

const SEPARATOR = '$_$';

interface IServiceLayout extends IPageLayout {
  dispatch: Dispatch;
  allServices: IService[];
  allServiceMap: IServiceMap;
  allNetworkMap: INetworkMap;
  allLogicalSubnetMap: ILogicalSubnetMap;
  globalSelectedTime: Required<IGlobalTime>;
}
const ServiceLayout: React.FC<IServiceLayout> = (props) => {
  const {
    dispatch,
    allServices,
    allServiceMap,
    allNetworkMap,
    allLogicalSubnetMap,
    globalSelectedTime,
  } = props;

  const [collapsed, setCollapsed] = useState<boolean>(
    () => storage.get(SEARCH_TREE_COLLAPSED_KEY) === 'true',
  );
  const { pathname } = useLocation();
  const params: IUriParams = useParams();

  const isServiceAnalysis =
    pathToRegexp(`(/embed)?/analysis/performance/service/:serviceId/:networkId/(.*)`).test(`${pathname}/`) &&
    pathname !== '/analysis/performance/service/list' &&
    pathname !== '/embed/analysis/performance/service/list';

  // 参数配置页面，不需要显示时间轴
  const hideTimeRange =
    pathToRegexp(`(/embed)?/analysis/performance/service/:serviceId/:networkId/payload/baseline`).test(
      `${pathname}/`,
    ) ||
    pathToRegexp(`(/embed)?/analysis/performance/service/:serviceId/:networkId/performance/setting`).test(
      `${pathname}/`,
    ) ||
    pathToRegexp(
      `(/embed)?/analysis/performance/service/:serviceId/:networkId/tcp/connection/long-connection/setting`,
    ).test(`${pathname}/`) ||
    pathToRegexp(`(/embed)?/analysis/performance/service/:serviceId/:networkId/packet/analysis`).test(
      `${pathname}/`,
    );

  const handleTreeSelect = (selectedKeys: React.Key[]) => {
    if (selectedKeys.length === 0) {
      return;
    }
    const [nextServiceId, nextNetworkId] = (selectedKeys[0] as string).split(SEPARATOR);
    history.push(
      pathname.replace(params.serviceId!, nextServiceId).replace(params.networkId!, nextNetworkId),
    );
  };

  const handleCollapsed = (nextCollapsed: boolean) => {
    setCollapsed(nextCollapsed);
    storage.put(SEARCH_TREE_COLLAPSED_KEY, nextCollapsed);
  };

  const serviceTree = useMemo(() => {
    return allServices.map((service) => {
      // 这个业务在哪个网络下
      const networkIds = service.networkIds?.split(',') || [];
      const networkList = networkIds.map((networkId) => {
        return {
          title: { ...allNetworkMap, ...allLogicalSubnetMap }[networkId]?.name || networkId,
          key: `${service.id}${SEPARATOR}${networkId}`,
          children: [],
        };
      });

      return {
        title: service.name,
        key: service.id,
        disabled: true,
        children: networkList,
      };
    });
  }, [allServices, allNetworkMap, allLogicalSubnetMap]);

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

  if (isServiceAnalysis && (!params.serviceId || !allServiceMap[params.serviceId])) {
    return <Result status="warning" subTitle="业务不存在或已被删除" />;
  }

  if (!isServiceAnalysis) {
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
          data={serviceTree}
          selectedKeys={[`${params.serviceId}${SEPARATOR}${params.networkId}`]}
          onSelect={handleTreeSelect}
          collapsed={collapsed}
          onToggleCollapsed={handleCollapsed}
        />
      </div>
      <div className={styles.contentWrap}>
        <Space size="middle">
          <span className={styles.name}>
            {`${allServiceMap[params.serviceId]?.name} / ${
              { ...allNetworkMap, ...allLogicalSubnetMap }[params.networkId]?.name
            }`}
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
    serviceModel: { allServices, allServiceMap },
    networkModel: { allNetworkMap },
    logicSubnetModel: { allLogicalSubnetMap },
    appModel: { globalSelectedTime, realTimeStatisticsFlag },
  }: ConnectState) => ({
    allServices,
    allServiceMap,
    allNetworkMap,
    allLogicalSubnetMap,
    globalSelectedTime,
    realTimeStatisticsFlag,
  }),
)(ServiceLayout);
