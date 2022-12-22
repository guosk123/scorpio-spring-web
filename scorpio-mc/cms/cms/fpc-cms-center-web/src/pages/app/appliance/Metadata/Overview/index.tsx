import { useState, useCallback, useEffect, useContext } from 'react';
import { Input, Layout, message, Select, Spin, TreeSelect } from 'antd';
import { ipV4Regex, ipV6Regex } from '@/utils/utils';
import { connect } from 'dva';
import OverviewProtocolPie from './components/OverviewProtocolPie/index';
import OverviewProtocolTable from './components/OverviewProtocolTable/index';
import type { ConnectState } from '@/models/connect';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { countFlowProtocol } from '@/services/app/metric';
import type { INetworkTreeItem } from '@/pages/app/Network/typing';
import { DimensionsSearchContext } from '@/pages/app/GlobalSearch/DimensionsSearch/SeartchTabs';
import { MetaDataContext } from '..';
import { getTabDetail } from '@/pages/app/Network/components/EditTabs';
import { EDRILLDOWN } from '@/pages/app/GlobalSearch/DimensionsSearch/typing';
import { useSelector } from 'umi';
import type { IService } from '@/pages/app/Configuration/Service/typings';
import { queryNetWorkTree } from '@/pages/app/Network/service';

const { Header, Content } = Layout;

interface Props {
  globalSelectedTime: Required<IGlobalTime>;
  isDimensionsTab?: boolean;
}

function Overview(props: Props) {
  const { globalSelectedTime, isDimensionsTab = false } = props;

  const [searchIp, setSearchIp] = useState();
  const [flowProtocol, setFlowProtocol] = useState({});

  const [networkId, setNetwrokId] = useState<string>('');
  const [serviceId, setServiceId] = useState<string>('');
  const [networkText, setNetworkText] = useState<string | undefined>();
  const [serviceText, setServiceText] = useState<string | undefined>();
  const [networkDataSet, setNetworkDataSet] = useState<INetworkTreeItem[]>([]);
  const [networkTreeLoading, setNetworkTreeLoading] = useState(true);

  useEffect(() => {
    queryNetWorkTree()
      .then((result) => {
        setNetworkDataSet(
          result.map((item) => ({
            ...item,
            value: `${item.value}^${item.type}`,
          })),
        );
        setNetworkTreeLoading(false);
      })
      .catch((err) => {
        message.error(err);
        setNetworkTreeLoading(false);
      });
  }, []);

  const [countFlowsLoading, setCountFlowsLoading] = useState(false);
  // const [networkType] = useContext<any>(serviceId ? ServiceContext : NetworkTypeContext);
  // MetaDataContext 占位用
  const [state, dispatch] = useContext<any>(
    isDimensionsTab ? DimensionsSearchContext : MetaDataContext,
  );

  const allServices = useSelector<ConnectState, IService[]>(
    (state) => state.serviceModel.allServices,
  );

  const dimensionsOverviewDetail = getTabDetail(state) || {};

  useEffect(() => {
    setCountFlowsLoading(true);
    const queryData = {
      srcIp: searchIp,
      startTime: globalSelectedTime.originStartTime,
      endTime: globalSelectedTime.originEndTime,
      serviceId,
    };
    queryData[networkId.includes('networkGroup') ? 'networkGroupId' : 'networkId'] =
      networkId?.split('^')[0];
    const dimensionsQueryData = (() => {
      const {
        searchBoxInfo,
        shareRow,
        drilldown = EDRILLDOWN.NOTDRILLDOWN,
      } = dimensionsOverviewDetail;
      const tmpIds = {
        networkId: searchBoxInfo?.networkIds
          .filter((item: string) => !item.includes('networkGroup'))
          .map((sub: string) => sub.replace('^network', ''))
          .join(','),
        networkGroupId: searchBoxInfo?.networkIds
          .filter((item: string) => item.includes('networkGroup'))
          .map((sub: string) => sub.replace('^networkGroup', ''))
          .join(','),
      };
      return {
        srcIp: searchBoxInfo?.content,
        startTime: globalSelectedTime.originStartTime,
        endTime: globalSelectedTime.originEndTime,
        ...tmpIds,
      };
    })();
    countFlowProtocol(isDimensionsTab ? dimensionsQueryData : queryData).then((res) => {
      const { success, result } = res;
      delete result?.TOTAL;
      if (success) {
        setFlowProtocol(result);
      }
      setCountFlowsLoading(false);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    globalSelectedTime.originEndTime,
    globalSelectedTime.originStartTime,
    networkId,
    searchIp,
    serviceId,
    // networkType,
    isDimensionsTab,
  ]);

  const handleSearchFlowsProtocol = useCallback((srcIp) => {
    // 检查是否是正确的ip地址
    if (srcIp && !ipV4Regex.test(srcIp) && !ipV6Regex.test(srcIp)) {
      message.warn('请输入正确的IP地址');
      return;
    }
    setSearchIp(srcIp);
  }, []);

  return (
    <div style={{ margin: '0 auto', maxWidth: 1200 }}>
      <Header style={{ backgroundColor: 'white', display: isDimensionsTab ? 'none' : '' }}>
        <div style={{ display: 'flex', width: '100%', gap: '10px' }}>
          <Input.Search
            placeholder="请输入源IP进行查询"
            onSearch={(value) => handleSearchFlowsProtocol(value)}
            enterButton
          />
          {
            <>
              <TreeSelect
                allowClear
                treeDefaultExpandAll
                treeData={networkDataSet}
                // treeCheckable={true}
                loading={networkTreeLoading}
                placeholder={'请选择网络'}
                showCheckedStrategy={'SHOW_PARENT'}
                style={{ width: '100%' }}
                onChange={(value) => {
                  setNetwrokId(value);
                  setNetworkText(networkDataSet.find((ele) => ele.key === value)?.title);
                }}
              />
              <Select
                placeholder="选择业务"
                onChange={(value) => {
                  setServiceId(value);
                  setServiceText(allServices.find((ele) => ele.id === value)?.name);
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
          }
        </div>
      </Header>
      <Content>
        <Spin spinning={countFlowsLoading}>
          <OverviewProtocolPie
            data={flowProtocol}
            srcIp={searchIp}
            demandShowText={{ networkText, serviceText }}
          />
          <OverviewProtocolTable
            data={flowProtocol}
            srcIp={searchIp}
            isDimensionsTab={isDimensionsTab}
          />
        </Spin>
      </Content>
    </div>
  );
}
export default connect(({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
}))(Overview);
