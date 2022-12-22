import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import TimeRangeSlider from '@/components/TimeRangeSlider';
import type { ConnectState } from '@/models/connect';
import type { ILogicalSubnetMap } from '@/pages/app/Configuration/LogicalSubnet/typings';
import type {
  INetworkGroupMap,
  INetworkSensor,
  INetworkSensorMap,
} from '@/pages/app/Configuration/Network/typings';
import { ESensorStatus } from '@/pages/app/Configuration/Network/typings';
import storage from '@/utils/frame/storage';
import { Alert, Modal, Result, Skeleton, Space, Spin } from 'antd';
import classNames from 'classnames';
import React, { createContext, useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, history, useLocation, useParams } from 'umi';
import type { IService, IServiceMap } from '../../Configuration/Service/typings';
import EditTabs from '../../Network/components/EditTabs';
import LinkToAnalysis from '../../Network/components/LinkToAnalysis';
import { queryNetWorkTree } from '../../Network/service';
import type { INetworkTreeItem } from '../../Network/typing';
import { ENetowrkType } from '../../Network/typing';
import SearchTree, { SEARCH_TREE_COLLAPSED_KEY } from '../components/SearchTree';
import type { IUriParams } from '../typings';
import { ServiceTabs } from './constant';
import styles from './index.less';

const SEPARATOR = '$_$';

interface IServicePage {
  dispatch: Dispatch;
  allServices: IService[];
  allServiceMap: IServiceMap;
  allNetworkSensorMap: INetworkSensorMap;
  allNetworkGroupMap: INetworkGroupMap;
  allLogicalSubnetMap: ILogicalSubnetMap;
  globalSelectedTime: Required<IGlobalTime>;
}

// 用来传递networktype
export const ServiceContext = createContext<[ENetowrkType, INetworkTreeItem[], any]>([
  ENetowrkType.NETWORK,
  [],
  () => {},
]);

export const ServiceAnalysisContext = createContext([]);
const Service: React.FC<IServicePage> = (props) => {
  const {
    allServices,
    allServiceMap,
    allNetworkSensorMap,
    allNetworkGroupMap,
    allLogicalSubnetMap,
  } = props;
  const [collapsed, setCollapsed] = useState<boolean>(
    () => storage.get(SEARCH_TREE_COLLAPSED_KEY) === 'true',
  );
  const { pathname } = useLocation();
  const params: IUriParams = useParams();
  const [networkDataSet, setNetworkDataSet] = useState<INetworkTreeItem[]>([]);
  const [networkType, setNetworkType] = useState<ENetowrkType>(ENetowrkType.NETWORK);

  const [networkTreeLoading, setNetworkTreeLoading] = useState(true);

  // 用来记录networkid和serviceid, 每次切换成功之后会更新
  const [urlState, setUrlState] = useState<{
    networkId: string | undefined | null;
    serviceId: string | undefined | null;
  }>({
    networkId: params.networkId === ':networkId' ? null : params.networkId,
    serviceId: params.networkId === ':serviceId' ? null : params.serviceId,
  });

  const [haveClosableTab, setHaveClosableTab] = useState(false);
  const [treeSelectKeys, setTreeSelectKeys] = useState<React.Key[]>([]);
  const [isModalVisible, setIsModalVisible] = useState(false);

  const [networkStatusMap, setNetworkStatusMap] = useState<
    Map<string, { key: string; status: '0' | '1'; statusDetail?: string }>
  >(new Map());
  const handleTreeSelect = (selectedKeys: React.Key[]) => {
    if (selectedKeys.length === 0) {
      return;
    }
    if (haveClosableTab) {
      setIsModalVisible(true);
    }
    setTreeSelectKeys(selectedKeys);
  };

  useEffect(() => {
    if (treeSelectKeys.length === 0 || haveClosableTab) {
      return;
    }
    const [nextServiceId, nextNetworkId] = (treeSelectKeys[0] as string).split(SEPARATOR);
    history.replace({
      pathname: `/performance/service/${nextServiceId}/${nextNetworkId}`,
      query: history.location.query,
    });
  }, [haveClosableTab, params.networkId, pathname, treeSelectKeys]);

  const handleCollapsed = (nextCollapsed: boolean) => {
    setCollapsed(nextCollapsed);
    storage.put(SEARCH_TREE_COLLAPSED_KEY, nextCollapsed);
  };

  const serviceTree = useMemo(() => {
    const networkStatusMap = new Map();
    const tree = allServices.map((service) => {
      // 这个业务在哪个网络下
      const networkIds = (service.networkIds || service.networkGroupIds).split(',') || [];
      const networkList = networkIds.map((networkId) => {
        if (networkId.includes('^')) {
          const [netId, logicId] = networkId.split('^');
          const network = allNetworkSensorMap[netId];
          const logicSubnet = allLogicalSubnetMap[logicId];
          const status = logicSubnet?.status;
          const statusDetail = logicSubnet.statusDetail;
          networkStatusMap.set(networkId, {
            networkId,
            status,
            statusDetail,
          });
          return {
            title: `${`${network?.name || netId} - ${logicSubnet?.name || logicId}`}${
              logicSubnet?.status === ESensorStatus.OFFLINE ? '(离线)' : ''
            }`,
            key: `${service.id}${SEPARATOR}${networkId}`,
            children: [],
            status,
            statusDetail,
          };
        } else {
          const network = { ...allNetworkGroupMap, ...allNetworkSensorMap, ...allLogicalSubnetMap }[
            networkId
          ];
          const status = network?.status;
          const statusDetail = network.statusDetail;
          networkStatusMap.set(networkId, {
            key: networkId,
            status,
            statusDetail,
          });
          return {
            title: `${network?.name || networkId}${
              network?.status === ESensorStatus.OFFLINE ? '(离线)' : ''
            }`,
            key: `${service.id}${SEPARATOR}${networkId}`,
            children: [],
            status,
            statusDetail,
          };
        }
      });

      return {
        title: service.name,
        key: service.id,
        disabled: true,
        children: networkList,
      };
    });
    setNetworkStatusMap(networkStatusMap);
    return tree;
  }, [allServices, allNetworkGroupMap, allNetworkSensorMap, allLogicalSubnetMap]);

  const linkToTab = useMemo(() => {
    return <LinkToAnalysis />;
  }, []);

  const errorMessage = useMemo(() => {
    if (networkStatusMap.size !== 0) {
      const networkStatus = networkStatusMap?.get(params.networkId || '');
      if (networkStatus?.status === '1') {
        return <Alert message={networkStatus?.statusDetail} type="info" banner />;
      }
    }
    return '';
  }, [networkStatusMap, params.networkId]);

  const fetchServiceNetworkTree = () => {
    setNetworkTreeLoading(true);
    queryNetWorkTree()
      .then((result) => {
        setNetworkDataSet(result);
        setNetworkTreeLoading(false);
      })
      .catch(() => {
        setNetworkTreeLoading(false);
      });
  };
  useEffect(() => {
    fetchServiceNetworkTree();
  }, []);

  useEffect(() => {
    networkDataSet.forEach((item) => {
      if (item.value === params.networkId) {
        setNetworkType(item.type);
      }
    });
  }, [params.networkId, networkDataSet]);

  const [showTimeSelect, setShowTimeSelect] = useState(true);

  if (!params.serviceId || !allServiceMap[params.serviceId]) {
    return <Result status="warning" subTitle="业务不存在或已被删除" />;
  }

  return (
    <>
      {errorMessage}
      <ServiceContext.Provider value={[networkType, networkDataSet, setShowTimeSelect]}>
        {networkTreeLoading ? (
          <Spin />
        ) : (
          <div className={classNames([styles.layoutWrap, collapsed && styles.collapsed])}>
            <div className={styles.leftWrap}>
              {serviceTree.length ? (
                <SearchTree
                  data={serviceTree as any}
                  selectedKeys={[`${params.serviceId}${SEPARATOR}${params.networkId}`]}
                  onSelect={handleTreeSelect}
                  collapsed={collapsed}
                  onToggleCollapsed={handleCollapsed}
                  refreshFunc={fetchServiceNetworkTree}
                />
              ) : (
                <Skeleton />
              )}
            </div>
            <div className={styles.contentWrap}>
              <Space size="middle">
                <span className={styles.name}>
                  {`${allServiceMap[params.serviceId]?.name} / ${(() => {
                    if (params?.networkId?.includes('^')) {
                      const [netId, logicId] = params.networkId.split('^');
                      const network = allNetworkSensorMap[netId];
                      const logicSubnet = allLogicalSubnetMap[logicId];
                      return `${`${network?.name || netId} - ${logicSubnet?.name || logicId}`}`;
                    } else {
                      return (
                        { ...allNetworkGroupMap, ...allNetworkSensorMap, ...allLogicalSubnetMap }[
                          params?.networkId || ''
                        ]?.name || params.networkId
                      );
                    }
                  })()}`}
                </span>
                {/* {showTimeSelect && <TimeRangeSlider />} */}
              </Space>
              {serviceTree.length ? (
                <EditTabs
                  tabs={ServiceTabs}
                  consumerContext={ServiceAnalysisContext}
                  onChangeTab={() => {
                    // history.replace({});
                  }}
                  // showTabSettingTool={true}
                  tabsKey="serviceAnalysis"
                  linkToTab={linkToTab}
                  // 相对时间的话，tab页每次切换都去获取最新的
                  // destroyInactiveTabPane={history.location.query?.relative === 'true'}
                  destroyInactiveTabPane={true}
                  showTabSettingTool={true}
                  resetTabsState={(reset) => {
                    // 切换成功后networkId会变，此时重置所有tab
                    if (
                      urlState &&
                      (urlState.networkId !== params.networkId ||
                        urlState.serviceId !== params.serviceId)
                    ) {
                      reset();
                      setUrlState({
                        networkId: params.networkId,
                        serviceId: params.serviceId,
                      });
                    }
                  }}
                  haveClosableTab={(flag) => {
                    setHaveClosableTab(flag);
                  }}
                />
              ) : (
                <Skeleton />
              )}
            </div>
            <Modal
              title="是否切换"
              visible={isModalVisible}
              onOk={() => {
                setIsModalVisible(false);
                setHaveClosableTab(false);
              }}
              onCancel={() => {
                setIsModalVisible(false);
              }}
            >
              <span>切换后当前下钻标签页将会丢失</span>
            </Modal>
          </div>
        )}
      </ServiceContext.Provider>
    </>
  );
};

export default connect(
  ({
    serviceModel: { allServices, allServiceMap },
    networkModel: { allNetworkSensorMap, allNetworkGroupMap },
    logicSubnetModel: { allLogicalSubnetMap },
    appModel: { globalSelectedTime, realTimeStatisticsFlag },
  }: ConnectState) => ({
    allServices,
    allServiceMap,
    allNetworkSensorMap,
    allNetworkGroupMap,
    allLogicalSubnetMap,
    globalSelectedTime,
    realTimeStatisticsFlag,
  }),
)(Service);
