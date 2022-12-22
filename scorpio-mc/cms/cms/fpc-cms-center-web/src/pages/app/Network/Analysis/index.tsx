import storage from '@/utils/frame/storage';
import { Alert, Button, message, Modal, Result, Skeleton, Spin } from 'antd';
import classNames from 'classnames';
import { history } from 'umi';
import { createContext, Fragment, useContext, useEffect, useMemo } from 'react';
import { useState } from 'react';
import { useParams } from 'umi';
import type { IUriParams } from '../../analysis/typings';
import SearchTree, { SEARCH_TREE_COLLAPSED_KEY } from '../components/SearchTree';
import { queryNetWorkTree } from '../service';
import type { INetworkTreeItem } from '../typing';
import { ENetowrkType } from '../typing';
import { NetworkTabs } from './constant';
import styles from './index.less';
import React from 'react';
import EditTabs from '../components/EditTabs';
import LinkToAnalysis from '../components/LinkToAnalysis';
import { LayoutContext } from '../NetworkLayout';
import { classifyDataSet } from '../utils/index';
import { ApartmentOutlined, ApiOutlined, BlockOutlined, BorderOutlined } from '@ant-design/icons';

// 用来传递networktype
export const NetworkTypeContext = createContext<[ENetowrkType, INetworkTreeItem[]]>([
  ENetowrkType.NETWORK,
  [],
]);

export const getNetworkType = (networkDataSet: INetworkTreeItem[], networkId: string) => {
  let res;
  networkDataSet.forEach((item) => {
    if (item.value === networkId) {
      res = item.type;
    }
  });
  return res;
};
export const AnalysisContext = createContext([]);
export default function Analysis() {
  const [, setSelectName] = useContext(LayoutContext);
  const [networkType, setNetworkType] = useState<ENetowrkType>();
  const [networkDataSet, setNetworkDataSet] = useState<INetworkTreeItem[]>([]);
  const params: IUriParams = useParams();
  // 用来记录networkid或serviceid, 每次切换成功之后会更新
  const [urlState, setUrlState] = useState<string | null>(() =>
    params.networkId === ':networkId' ? null : params.networkId || '',
  );

  const [networkTreeLoading, setNetworkTreeLoading] = useState(true);

  useEffect(() => {
    const tmpNetworks: INetworkTreeItem[] = [];
    networkDataSet.forEach((item) => {
      tmpNetworks.push(...(item.children || []));
    });
    setSelectName(
      networkDataSet.concat(tmpNetworks).find((item) => {
        return item.key === params.networkId;
      })?.title,
    );
    return () => {
      setSelectName('');
    };
  }, [networkDataSet, params.networkId, setSelectName]);

  useEffect(() => {
    networkDataSet.forEach((item) => {
      if (item.value === params.networkId) {
        setNetworkType(item.type);
      }
    });
  }, [params.networkId, networkDataSet]);

  const fetchNetworkDataSet = () => {
    setNetworkTreeLoading(true);
    queryNetWorkTree()
      .then((result) => {
        setNetworkDataSet(result);
        setNetworkTreeLoading(false);
      })
      .catch((err) => {
        message.error(err);
        setNetworkTreeLoading(false);
      });
  };

  useEffect(() => {
    if (params.networkId !== ':networkId' && networkDataSet.length) {
      return;
    }
    setNetworkTreeLoading(true);
    queryNetWorkTree()
      .then((result) => {
        if (result.length && params.networkId === ':networkId') {
          history.replace({
            pathname: `/performance/network/${result[0].value}/analysis`,
            query: history.location.query,
          });
          setUrlState(result[0].value);
          setNetworkType(result[0].type);
        }
        setNetworkDataSet(result);
        setNetworkTreeLoading(false);
      })
      .catch((err) => {
        message.error(err);
        setNetworkTreeLoading(false);
      });
  }, [networkDataSet.length, params.networkId]);

  const [collapsed, setCollapsed] = useState<boolean>(
    () => storage.get(SEARCH_TREE_COLLAPSED_KEY) === 'true',
  );

  const handleCollapsed = (nextCollapsed: boolean) => {
    setCollapsed(nextCollapsed);
    storage.put(SEARCH_TREE_COLLAPSED_KEY, nextCollapsed);
  };

  const [haveClosableTab, setHaveClosableTab] = useState(false);
  const [treeSelectKeys, setTreeSelectKeys] = useState<React.Key[]>([]);
  // const treeSelectKeys = useRef<React.Key[]>([])
  const [isModalVisible, setIsModalVisible] = useState(false);

  const handleTreeSelect = (selectedKeys: React.Key[]) => {
    if (haveClosableTab) {
      setIsModalVisible(true);
    }
    setTreeSelectKeys(selectedKeys);
    // treeSelectKeys.current = selectedKeys
  };
  useEffect(() => {
    if (treeSelectKeys.length === 0 || haveClosableTab) {
      return;
    }
    history.replace({
      pathname: `/performance/network/${treeSelectKeys[0]}/analysis`,
      query: history.location.query,
    });
  }, [haveClosableTab, treeSelectKeys]);

  const linkToTab = useMemo(() => {
    return <LinkToAnalysis />;
  }, []);

  const errorMessage = useMemo(() => {
    const network = networkDataSet.find((data) => data.value === params.networkId);
    if (network?.status === '1') {
      return <Alert message={network?.statusDetail} type="info" banner />;
    }
    return '';
  }, [params.networkId]);

  // 目前不分组，不需要
  // const networkToGroup = (networkArr: INetworkTreeItem[]) => {
  //   const res = [
  //     {
  //       title: '网络组',
  //       value: 'group-online',
  //       key: 'group-online',
  //       disabled: true,
  //       children: [] as any,
  //     },
  //     {
  //       title: '网络',
  //       value: 'network-online',
  //       key: 'network-online',
  //       disabled: true,
  //       children: [] as any,
  //     },
  //     {
  //       title: '离线设备',
  //       value: 'offline',
  //       key: 'offline',
  //       disabled: true,
  //       children: [] as any,
  //     },
  //   ];
  //   networkArr.forEach((item) => {
  //     if (item.type === ENetowrkType.NETWORK_GROUP && !item.status) {
  //       res[0].children.push(item);
  //     } else if (item.type === ENetowrkType.NETWORK && !item.status) {
  //       res[1].children.push(item);
  //     } else {
  //       res[2].children.push(item);
  //     }
  //   });
  //   return res;
  // };

  const addIcon = (networkArr: INetworkTreeItem[]) => {
    return networkArr.map((item) => {
      if (item.type === ENetowrkType.NETWORK_GROUP) {
        return {
          ...item,
          icon: <ApartmentOutlined />,
        };
      } else if (item.type === ENetowrkType.NETWORK && item.status === '1') {
        return {
          ...item,
          icon: <ApiOutlined />,
        };
      } else if (item?.logicNetwork) {
        return {
          ...item,
          icon: <BlockOutlined />,
        };
      } else {
        return {
          ...item,
          icon: <BorderOutlined />,
        };
      }
    });
  };

  let renderDom = <div />;

  if (!networkDataSet.length && !networkTreeLoading) {
    renderDom = (
      <Result
        status="info"
        title="还没有配置网络"
        extra={
          <Button type="primary" onClick={() => history.push('/configuration/network/sensor')}>
            配置网络
          </Button>
        }
      />
    );
  }

  renderDom = (
    <Fragment>
      {errorMessage}
      {!networkType || params.networkId === ':networkId' ? (
        <Spin />
      ) : (
        <NetworkTypeContext.Provider value={[networkType!, networkDataSet]}>
          <div className={classNames([styles.layoutWrap, collapsed && styles.collapsed])}>
            {networkTreeLoading ? (
              <Spin />
            ) : (
              <div>
                <div className={styles.leftWrap}>
                  {networkDataSet.length ? (
                    <SearchTree
                      selectedKeys={[params.networkId || '']}
                      data={addIcon(classifyDataSet(networkDataSet) as any)}
                      onSelect={handleTreeSelect}
                      collapsed={collapsed}
                      onToggleCollapsed={handleCollapsed}
                      showLine={false}
                      showIcon={true}
                      refreshFunc={fetchNetworkDataSet}
                    />
                  ) : (
                    <Skeleton />
                  )}
                </div>
                <div className={styles.contentWrap}>
                  {networkDataSet.length ? (
                    <EditTabs
                      tabs={NetworkTabs}
                      consumerContext={AnalysisContext}
                      onChangeTab={() => {
                        // history.replace({});
                      }}
                      // showTabSettingTool={true}
                      tabsKey="networkAnalysis"
                      linkToTab={linkToTab}
                      // showTabSettingTool={true}
                      // 相对时间的话，tab页每次切换都去获取最新的
                      // destroyInactiveTabPane={history.location.query?.relative === 'true'}
                      destroyInactiveTabPane={true}
                      resetTabsState={(reset) => {
                        // 切换成功后networkId会变，此时重置所有tab
                        if (urlState && urlState !== params.networkId) {
                          reset();
                          setUrlState(params.networkId || '');
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
          </div>
        </NetworkTypeContext.Provider>
      )}
    </Fragment>
  );
  return renderDom;
}
