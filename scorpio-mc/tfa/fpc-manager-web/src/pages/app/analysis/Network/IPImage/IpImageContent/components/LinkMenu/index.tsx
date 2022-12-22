import type { IFilter, IFilterCondition } from '@/components/FieldFilter/typings';
import { EFilterGroupOperatorTypes, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { isIpv4, jumpNewPage } from '@/utils/utils';
import { Menu } from 'antd';
import moment from 'moment';
import { useMemo, useRef } from 'react';
import { useSelector } from 'umi';

export enum EIP_DRILLDOWN_MENU_KEY {
  IP_IMAGE = 'ip_image',
  // IP_GRAPH = 'ip_graph',
  // DNS_DETAIL = 'dns-detail',
  FLOW_RECORD = 'flow_record',
  SECURITY_ALARM = 'security_alarm',
}
const menuToLastUrl = {
  [EIP_DRILLDOWN_MENU_KEY.IP_IMAGE]: 'ip-image',
  // [EIP_DRILLDOWN_MENU_KEY.IP_GRAPH]: 'ip-graph',
  // [EIP_DRILLDOWN_MENU_KEY.DNS_DETAIL]: 'metadata/record',
  [EIP_DRILLDOWN_MENU_KEY.FLOW_RECORD]: 'flow-record',
  [EIP_DRILLDOWN_MENU_KEY.SECURITY_ALARM]: 'alert',
};

const menuToMiddleUrl = {
  [EIP_DRILLDOWN_MENU_KEY.IP_IMAGE]: 'trace',
  // [EIP_DRILLDOWN_MENU_KEY.IP_GRAPH]: 'trace',
  // [EIP_DRILLDOWN_MENU_KEY.DNS_DETAIL]: 'trace',
  [EIP_DRILLDOWN_MENU_KEY.FLOW_RECORD]: 'trace',
  [EIP_DRILLDOWN_MENU_KEY.SECURITY_ALARM]: 'security',
};

export enum MenuItemTitle {
  JUMPPAGE = 'jumpToOtherPage',
  ADDFILTER = 'addFilter',
}

export interface MenuItemGroup {
  key: string | MenuItemTitle;
  label: string;
  children: MenuItem[];
}

export interface MenuItem {
  label: string;
  key?: string;
  value?: string;
}

export interface IpGroup {
  srcIp: string;
  desIp: string;
}

export interface Settings {
  ipPair?: IpGroup;
  networkId?: string | null;
  alarmMessage?: string;
  imageIp?: string;
}

interface Props {
  MenuItemsGroup?: MenuItemGroup[];
  MenuItems?: MenuItem[];
  settings: Settings;
}

const LinkMenu = (props: Props) => {
  const { MenuItemsGroup, MenuItems, settings } = props;
  const { networkId, ipPair, alarmMessage, imageIp } = settings;
  const dirlldownTypeRef = useRef<EIP_DRILLDOWN_MENU_KEY>();
  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state) => state.appModel.globalSelectedTime,
  );

  const filter: IFilter[] = useMemo(() => {
    const basicFilter: IFilter[] = [];
    if (networkId) {
      basicFilter.push({
        field: 'network_id',
        operator: EFilterOperatorTypes.EQ,
        operand: networkId,
      });
    }
    return basicFilter;
  }, [networkId]);

  const urlTimeInfo = useMemo(() => {
    return `from=${moment(globalSelectedTime.originStartTime).valueOf()}&to=${moment(
      globalSelectedTime.originEndTime,
    ).valueOf()}&timeType=${ETimeType.CUSTOM}&`;
  }, [globalSelectedTime.originEndTime, globalSelectedTime.originStartTime]);

  const jumpToNewFn = (key: EIP_DRILLDOWN_MENU_KEY) => {
    const currentFilter: IFilterCondition = [...filter];
    let linkUrl = `/analysis/${menuToMiddleUrl[key]}/${menuToLastUrl[key]}?${urlTimeInfo}`;
    switch (true) {
      case dirlldownTypeRef.current === EIP_DRILLDOWN_MENU_KEY.FLOW_RECORD:
        if (ipPair) {
          currentFilter.push({
            operator: EFilterGroupOperatorTypes.AND,
            group: [
              {
                field: isIpv4(ipPair.srcIp) ? 'ipv4_initiator' : 'ipv6_initiator',
                operator: EFilterOperatorTypes.EQ,
                operand: ipPair.srcIp,
              },
              {
                field: isIpv4(ipPair.desIp) ? 'ipv4_responder' : 'ipv6_responder',
                operator: EFilterOperatorTypes.EQ,
                operand: ipPair.desIp,
              },
            ],
          });
        }
        if (currentFilter.length > 0) {
          linkUrl = linkUrl + `filter=${encodeURIComponent(JSON.stringify(currentFilter))}`;
        }
        break;
      case dirlldownTypeRef.current === EIP_DRILLDOWN_MENU_KEY.SECURITY_ALARM:
        if (alarmMessage) {
          currentFilter.push({
            field: 'msg',
            operator: EFilterOperatorTypes.EQ,
            operand: alarmMessage,
          });
        }
        if (currentFilter.length > 0) {
          linkUrl = linkUrl + `filter=${encodeURIComponent(JSON.stringify(currentFilter))}`;
        }
        break;
      case dirlldownTypeRef.current === EIP_DRILLDOWN_MENU_KEY.IP_IMAGE:
        if (imageIp) {
          linkUrl = linkUrl + `ipAddress=${imageIp}`;
        }
      default:
        break;
    }
    jumpNewPage(linkUrl);
  };

  const LinkClick = (key: string) => {
    dirlldownTypeRef.current = key as EIP_DRILLDOWN_MENU_KEY;
    jumpToNewFn(key as EIP_DRILLDOWN_MENU_KEY);
  };

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const manageMenuItem = (item: MenuItem) => {
    let currentKey = item.key;
    if (!currentKey) {
      currentKey = item.label;
    }
    const currentLabel = item.label;
    return (
      <Menu.Item key={currentKey} onClick={({ key }) => LinkClick(key)}>
        {currentLabel}
      </Menu.Item>
    );
  };

  const renderMenuItems = useMemo(() => {
    if (MenuItems && !MenuItemsGroup) {
      return [...MenuItems.map(manageMenuItem)];
    }
    if (MenuItemsGroup) {
      return MenuItemsGroup.map((item: MenuItemGroup) => {
        return (
          <Menu.ItemGroup key={item.key} title={item.label}>
            {item.children.map(manageMenuItem)}
          </Menu.ItemGroup>
        );
      });
    }
    return <></>;
  }, [MenuItemsGroup, MenuItems, manageMenuItem]);

  return <>{renderMenuItems}</>;
};

export default LinkMenu;
