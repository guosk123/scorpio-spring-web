import type { ConnectState } from '@/models/connect';
import { Button, Col, Popover, Result, Row, Select, TreeSelect } from 'antd';
import { createContext, useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, history } from 'umi';
import type { INetworkTreeData } from 'umi';
import TimeRangeSlider from '@/components/TimeRangeSlider';
import type { INetwork, INetworkMap } from '@/pages/app/configuration/Network/typings';
import type {
  ILogicalSubnet,
  ILogicalSubnetMap,
} from '@/pages/app/configuration/LogicalSubnet/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';

/** 提供一些全局信息 */
export const VideoContext = createContext<{
  network?: INetwork | ILogicalSubnet;
  globalSelectedTime?: Required<IGlobalTime>;
  setNetworkSelect?: (showNetwork: boolean) => void;
}>({});

const NetworkTimeLayout = ({
  networkTree,
  allNetworkMap,
  allLogicalSubnetMap,
  globalSelectedTime,
  dispatch,
  children,
}: {
  networkTree: INetworkTreeData[];
  allNetworkMap: INetworkMap;
  allLogicalSubnetMap: ILogicalSubnetMap;
  globalSelectedTime: Required<IGlobalTime>;
  dispatch: Dispatch;
  children?: React.ReactNode;
}) => {
  const [selectedvalue, setSelectValue] = useState<string>('');
  /** 网络选择器展示开关 */
  const [showNetworkSelect, setNetworkSelect] = useState<boolean>(true);
  /** 简单树选择 */
  const [simpleTree, setSimpleTree] = useState<boolean>(true);
  /** 树弹窗开关 */
  const [selectOpen, setSelectOpen] = useState<boolean>(false);
  /** 选择的网络完整信息 */
  const selectedNetwork = useMemo<INetwork | ILogicalSubnet>(() => {
    return { ...allNetworkMap, ...allLogicalSubnetMap }[selectedvalue] || {};
  }, [allNetworkMap, allLogicalSubnetMap, selectedvalue]);

  /** 是否有网络 */
  const hasNetwork = useMemo(() => {
    if (!networkTree) {
      return true;
    }
    return networkTree?.length === 0;
  }, [networkTree]);

  // 查询设备列表，并且获得第一条数据为默认
  useEffect(() => {
    /** 初始化网络 */
    if (networkTree?.length > 0) {
      setSelectValue(networkTree[0]?.value);
    }
  }, [networkTree]);

  useEffect(() => {
    if (networkTree.length === 0) {
      return;
    }
  }, [networkTree]);

  useEffect(() => {
    dispatch({
      type: 'networkModel/queryAllNetworks',
    });
  }, []);

  if (hasNetwork) {
    return (
      <Result
        status="info"
        title="无可用网络"
        extra={
          <Button
            type="primary"
            onClick={() => history.push('/configuration/network-netif/network')}
          >
            配置网络
          </Button>
        }
      />
    );
  }

  return (
    <>
      <Row>
        <Col>
          {showNetworkSelect ? (
            <>
              {simpleTree ? (
                <Popover content="选择网络" placement="left">
                  <a
                    type="link"
                    onClick={(e) => {
                      e.preventDefault();
                      setSimpleTree(false);
                      setSelectOpen(true);
                    }}
                    style={{ lineHeight: '30px', marginRight: '10px' }}
                  >
                    {selectedNetwork?.name || selectedvalue}
                  </a>
                </Popover>
              ) : (
                <TreeSelect
                  treeDefaultExpandAll
                  treeData={networkTree}
                  placeholder={'请选择网络'}
                  showCheckedStrategy={'SHOW_PARENT'}
                  style={{ width: 200, marginRight: '10px' }}
                  value={selectedvalue}
                  open={selectOpen}
                  onChange={(e) => {
                    setSelectValue(e);
                    setSelectOpen(false);
                    setSimpleTree(true);
                  }}
                />
              )}
            </>
          ) : (
            ''
          )}
        </Col>
        <Col>
          <TimeRangeSlider />
        </Col>
      </Row>
      <VideoContext.Provider
        value={{
          network: selectedNetwork,
          globalSelectedTime,
          setNetworkSelect,
        }}
      >
        {children}
      </VideoContext.Provider>
    </>
  );
};

export default connect(
  ({
    networkModel: { networkTree, allNetworkMap },
    logicSubnetModel: { allLogicalSubnetMap },
    appModel: { globalSelectedTime },
  }: ConnectState) => ({
    networkTree,
    allNetworkMap,
    allLogicalSubnetMap,
    globalSelectedTime,
  }),
)(NetworkTimeLayout);
