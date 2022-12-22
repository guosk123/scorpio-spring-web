import type { ConnectState } from '@/models/connect';
import { Button, Col, Popover, Result, Row, TreeSelect } from 'antd';
import { createContext, useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, history } from 'umi';
import type { INetworkTreeData } from 'umi';
import TimeRangeSlider from '@/components/TimeRangeSlider';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { INetworkSensor, INetworkSensorMap } from '@/pages/app/Configuration/Network/typings';
import type { ILogicalSubnet, ILogicalSubnetMap } from '@/pages/app/Configuration/LogicalSubnet/typings';

/** 提供一些全局信息 */
export const VideoContext = createContext<{
  network?: INetworkSensor | ILogicalSubnet;
  globalSelectedTime?: Required<IGlobalTime>;
  setNetworkSelect?: (showNetwork: boolean) => void;
}>({});

const NetworkTimeLayout = ({
  networkSensorTree,
  allNetworkSensorMap,
  allLogicalSubnetMap,
  globalSelectedTime,
  dispatch,
  children,
}: {
  networkSensorTree: INetworkTreeData[];
  allNetworkSensorMap: INetworkSensorMap;
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
  const selectedNetwork = useMemo<INetworkSensor | ILogicalSubnet>(() => {
    return { ...allNetworkSensorMap, ...allLogicalSubnetMap }[selectedvalue] || {};
  }, [allNetworkSensorMap, allLogicalSubnetMap, selectedvalue]);

  /** 是否有网络 */
  const hasNetwork = useMemo(() => {
    if (!networkSensorTree) {
      return true;
    }
    return networkSensorTree?.length === 0;
  }, [networkSensorTree]);

  // 查询设备列表，并且获得第一条数据为默认
  useEffect(() => {
    /** 初始化网络 */
    if (networkSensorTree?.length > 0) {
      setSelectValue(networkSensorTree[0]?.value);
    }
  }, [networkSensorTree]);

  useEffect(() => {
    if (networkSensorTree.length === 0) {
      return;
    }
  }, [networkSensorTree]);

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
 console.log(selectedNetwork)
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
                  treeData={networkSensorTree}
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
    networkModel: { networkSensorTree, allNetworkSensorMap },
    logicSubnetModel: { allLogicalSubnetMap },
    appModel: { globalSelectedTime },
  }: ConnectState) => ({
    networkSensorTree,
    allNetworkSensorMap,
    allLogicalSubnetMap,
    globalSelectedTime,
  }),
)(NetworkTimeLayout);
