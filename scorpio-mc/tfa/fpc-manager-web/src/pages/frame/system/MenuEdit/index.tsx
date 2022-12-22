import AutoHeightContainer from '@/components/AutoHeightContainer';
import { getRouteAccessMap, getRoutesMenu } from '@/utils/frame/menuAccess';
import { Button, Card, message } from 'antd';
import React, { Fragment } from 'react';
import { useEffect, useState } from 'react';
import AuthenticationCheckBox from './components/AuthenticationCheckBox';
import EditMenu from './components/EditMenu';
import ShowTabs from './components/ShowTabs';
import { queryAuthenticationOfMenu, updataAuthenticationOfMenu } from './service';
import type { IAccessRouteItem } from './typing';
import { history } from 'umi';
import { DEF_ADM_ACCESS_MAP } from '@/access';

export const MenuEditContext = React.createContext<any>(null);
export default function MenuEdit() {
  const menuArr: IAccessRouteItem[] = getRoutesMenu();
  const [routeTabs, setRouteTabs] = useState<IAccessRouteItem[]>([]);
  const [height, setHeight] = useState(300);
  const [authenticationMap, setAuthenticationMap] = useState<any>({});
  const [updateLoading, setUpdateLoading] = useState(false);
  const menuUpdateKey = 'menuAccessSetting';
  // const [queryRoutesLoading, setQueryRoutesLoading] = useState(true);
  const queryAccessMenus = () => {
    queryAuthenticationOfMenu({ userId: history.location.query?.id?.toString() || 'null' }).then(
      (res) => {
        const { success, result } = res;
        if (success) {
          setAuthenticationMap(() => {
            const routeAccessMap = getRouteAccessMap();
            if (JSON.stringify(result) === DEF_ADM_ACCESS_MAP) {
              return getRouteAccessMap(true);
            }
            Object.keys(result).forEach((key) => {
              routeAccessMap[key] = result[key];
            });
            return { ...routeAccessMap };
          });
        } else {
          setAuthenticationMap((prev: any) => {
            return prev;
          });
        }
      },
    );
  };

  useEffect(() => {
    queryAccessMenus();
    return () => {
      setAuthenticationMap({});
    };
  }, []);
  // return <TreeMenus />

  return (
    <MenuEditContext.Provider value={[queryAccessMenus, setAuthenticationMap]}>
      <AutoHeightContainer
        onHeightChange={(h) => {
          setHeight(h);
        }}
      >
        <Card
          title={
            <Fragment>
              <AuthenticationCheckBox
                identificationCode={''}
                authenticationMap={authenticationMap}
                label="菜单设置"
              />
            </Fragment>
          }
          extra={
            <Button
              type="primary"
              size="small"
              loading={updateLoading}
              disabled={updateLoading}
              onClick={() => {
                setUpdateLoading(true);
                message.loading({ content: '正在保存', key: menuUpdateKey });
                updataAuthenticationOfMenu({
                  menus: Object.keys(authenticationMap).map((ele) => {
                    return { resource: ele, perm: authenticationMap[ele] };
                  }),
                  userId: history.location.query?.id?.toString() || '',
                }).then((res) => {
                  setUpdateLoading(false);
                  const { success } = res;
                  if (success) {
                    message.success({ content: '保存成功', key: menuUpdateKey });
                  } else {
                    message.error({ content: '保存失败', key: menuUpdateKey });
                  }
                  queryAccessMenus();
                });
              }}
            >
              保存
            </Button>
          }
          size="small"
          bodyStyle={{ display: 'flex', height: 'calc(100% - 35px)' }}
          style={{ height: height }}
        >
          <EditMenu
            menuArr={menuArr}
            onClickMenu={setRouteTabs}
            authenticationMap={authenticationMap}
          />
          <div style={{ flex: 1 }}>
            <ShowTabs routeTabs={routeTabs} authenticationMap={authenticationMap} />
          </div>
        </Card>
      </AutoHeightContainer>
    </MenuEditContext.Provider>
  );
}
