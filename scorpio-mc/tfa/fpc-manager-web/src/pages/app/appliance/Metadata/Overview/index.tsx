import { useState, useCallback, useEffect, useMemo } from 'react';
import type { TreeSelectProps } from 'antd';
import { Input, Layout, message, Select, Spin, TreeSelect } from 'antd';
import { ipV4Regex, ipV6Regex } from '@/utils/utils';
import { connect, useParams } from 'dva';
import type { Dispatch } from 'umi';
import { useSelector } from 'umi';
import OverviewProtocolPie from './components/OverviewProtocolPie';
import OverviewProtocolTable from './components/OverviewProtocolTable';
import type { ConnectState } from '@/models/connect';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { IUriParams } from '@/pages/app/analysis/typings';
import type { IOfflinePcapData } from '@/pages/app/analysis/OfflinePcapAnalysis/typing';
import type { INetwork } from '@/pages/app/configuration/Network/typings';
import type { ILogicalSubnet } from '@/pages/app/configuration/LogicalSubnet/typings';
import type { IService } from '@/pages/app/configuration/Service/typings';

const { Header, Content } = Layout;

interface Props {
  countFlowProtocol: any;
  metricModel: any;
  globalSelectedTime: Required<IGlobalTime>;
  currentPcpInfo: IOfflinePcapData;
  countFlowsLoading: boolean | undefined;
}

function Overview(props: Props) {
  const {
    countFlowProtocol,
    metricModel: { flowProtocol },
    globalSelectedTime,
    currentPcpInfo,
    countFlowsLoading,
  } = props;

  const [searchIp, setSearchIp] = useState();

  const { pcapFileId }: IUriParams = useParams();
  const [networkId, setNetwrokId] = useState<string>('');
  const [serviceId, setServiceId] = useState<string>('');
  const [networkText, setNetworkText] = useState<string | undefined>();
  const [serviceText, setServiceText] = useState<string | undefined>();
  const allNetworks = useSelector<ConnectState, INetwork[]>(
    (state) => state.networkModel.allNetworks,
  );

  const allLogicalSubnets = useSelector<ConnectState, ILogicalSubnet[]>(
    (state) => state.logicSubnetModel.allLogicalSubnets,
  );

  const allServices = useSelector<ConnectState, IService[]>(
    (state) => state.serviceModel.allServices,
  );

  const networkTree: TreeSelectProps['treeData'] = useMemo(() => {
    const nets: TreeSelectProps['treeData'] = allNetworks.map((item) => {
      return {
        title: item.name,
        value: item.id,
        children: [],
      };
    });

    allLogicalSubnets.forEach((logicalSubnet) => {
      const parent = logicalSubnet.networkId;
      const find = nets.find((net) => net.value === parent);
      if (find) {
        find.children?.push({
          title: logicalSubnet.name,
          value: logicalSubnet.id,
        });
      }
    });
    return nets;
  }, [allLogicalSubnets, allNetworks]);

  useEffect(() => {
    countFlowProtocol(
      searchIp,
      globalSelectedTime.originStartTime,
      globalSelectedTime.originEndTime,
      networkId,
      serviceId,
      currentPcpInfo,
      pcapFileId,
    );
  }, [
    countFlowProtocol,
    currentPcpInfo,
    globalSelectedTime,
    networkId,
    pcapFileId,
    searchIp,
    serviceId,
  ]);

  const handleSearchFlowsProtocol = useCallback((srcIp) => {
    // 检查是否是正确的ip地址 /.rwuginvbjdeSk:xm
    if (srcIp && !ipV4Regex.test(srcIp) && !ipV6Regex.test(srcIp)) {
      message.warn('请输入正确的IP地址');
      return;
    }
    setSearchIp(srcIp);
  }, []);

  return (
    <div style={{ margin: '0 auto', maxWidth: 1200 }}>
      <Header style={{ backgroundColor: 'white' }}>
        <div style={{ display: 'flex', width: '100%', gap: '10px' }}>
          <Input.Search
            placeholder="请输入源IP进行查询"
            onSearch={(value) => handleSearchFlowsProtocol(value)}
            enterButton
          />
          {!pcapFileId ? (
            <>
              <TreeSelect
                onChange={(value) => {
                  setNetworkText(allNetworks.find((ele) => ele.id === value)?.name);
                  setNetwrokId(value || []);
                }}
                allowClear
                treeDefaultExpandAll
                treeData={networkTree}
                placeholder={'请选择网络'}
                showCheckedStrategy={'SHOW_PARENT'}
                style={{ width: '100%' }}
                multiple={false}
              />
              <Select
                placeholder="选择业务"
                allowClear
                onChange={(value) => {
                  setServiceText(allServices.find((ele) => ele.id === value)?.name);
                  setServiceId(value);
                }}
                style={{ width: '100%' }}
              >
                {allServices.map((item: any) => (
                  <Select.Option key={item.id} value={item.id}>
                    {item.name}
                  </Select.Option>
                ))}
              </Select>
            </>
          ) : null}
        </div>
      </Header>
      <Content>
        <Spin spinning={countFlowsLoading}>
          <OverviewProtocolPie data={flowProtocol} srcIp={searchIp} demandShowText={{networkText, serviceText}} />
          <OverviewProtocolTable data={flowProtocol} srcIp={searchIp} />
        </Spin>
      </Content>
    </div>
  );
}
export default connect(
  ({
    loading: { effects },
    metricModel,
    appModel: { globalSelectedTime },
    npmdModel: { currentPcpInfo },
  }: ConnectState) => ({
    metricModel,
    globalSelectedTime,
    currentPcpInfo,
    countFlowsLoading: effects['metricModel/countFlowProtocol'],
  }),
  (dispatch: Dispatch) => {
    return {
      countFlowProtocol: (
        srcIp: string,
        startTime: string,
        endTime: string,
        networkId: string,
        serviceId: string,
        currentPcpInfo: IOfflinePcapData,
        pcapFileId: string,
      ) => {
        const payload: any = {
          srcIp,
          startTime,
          endTime,
          networkId,
          serviceId,
          packetFileId: pcapFileId,
        };
        if (pcapFileId) {
          payload.sourceType = 'packetFile';
          payload.startTime = currentPcpInfo?.filterStartTime;
          payload.endTime = currentPcpInfo?.filterEndTime;
        }
        dispatch({
          type: 'metricModel/countFlowProtocol',
          payload,
        });
      },
    };
  },
)(Overview);
