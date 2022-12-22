import { DimensionsSearchContext } from '@/pages/app/GlobalSearch/DimensionsSearch/SeartchTabs';
import {
  dimensionsUrl,
  jumpToDimensionsTab,
} from '@/pages/app/GlobalSearch/DimensionsSearch/SeartchTabs/constant';
import { DimensionsSearchMapping } from '@/pages/app/GlobalSearch/DimensionsSearch/typing';
import { getTabDetail } from '@/pages/app/Network/components/EditTabs';
import { Menu } from 'antd';
import { useContext } from 'react';
import { history } from 'umi';

interface Props {
  isNetworkList?: boolean;
  networkId?: string;
  drilldownWithFilter?: any;
}

export default function DimensionsDirlldownMenu(props: Props) {
  const { isNetworkList = false, networkId, drilldownWithFilter } = props;
  const isDimensionsTab = history.location.pathname.includes(dimensionsUrl);
  const [flowState, flowDispatch] = useContext(DimensionsSearchContext);
  const flowAnalysisDetail = getTabDetail(flowState) || {};
  return (
    <Menu.ItemGroup key={'jumpToOtherPage'} title="下钻到其他页">
      {/* <Menu.Item key={EIP_DRILLDOWN_MENU_KEY.IP_GRAPH} onClick={({ key }) => onClick(key)}>
        访问关系
      </Menu.Item> */}
      {Object.values(DimensionsSearchMapping)
        .filter((ele) => {
          return ele.name !== flowAnalysisDetail?.searchBoxInfo?.dimensionsSearchType;
        })
        ?.map((item: any) => {
          // 如果是networklist中下钻的话,需要将info中的networkid进行替换
          const networkListInfo = {
            ...flowAnalysisDetail?.searchBoxInfo,
            networkIds: [networkId],
          };
          return (
            <Menu.Item
              key={item.name}
              onClick={({ key }) => {
                jumpToDimensionsTab(
                  flowState,
                  flowDispatch,
                  key as any,
                  isNetworkList ? networkListInfo : flowAnalysisDetail?.searchBoxInfo,
                  drilldownWithFilter,
                );
              }}
            >
              {item.title}
            </Menu.Item>
          );
        })}
    </Menu.ItemGroup>
  );
}
