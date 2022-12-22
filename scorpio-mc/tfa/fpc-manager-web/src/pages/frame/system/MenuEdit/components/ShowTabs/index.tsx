import { Tabs } from 'antd';
import { Fragment, useEffect, useState } from 'react';
import type { IAccessRouteItem } from '../../typing';
import { ACCESS_FUNCTION_TYPE } from '../../typing';
import AuthenticationCheckBox from '../AuthenticationCheckBox';

interface Props {
  routeTabs: IAccessRouteItem[];
  authenticationMap: any;
}

export default function ShowTabs(props: Props) {
  const { routeTabs, authenticationMap } = props;

  const [tabInRoutes, setTabInRoutes] = useState(() => {
    // 默认展示第一个tab下的tab
    if (routeTabs.length) {
      return routeTabs[0].routes || [];
    }
    return [];
  });

  useEffect(() => {
    setTabInRoutes(() => {
      // 默认展示第一个tab下的tab
      if (routeTabs.length) {
        return routeTabs[0].routes || [];
      }
      return [];
    });
  }, [routeTabs]);

  // const options = tabInRoutes[1].accessFunction
  //   ? [
  //       { label: '导出', value: 'Apple' },
  //       { label: '下载数据包', value: 'Pear' },
  //       { label: '新建', value: 'Orange' },
  //       { label: '修改', value: 'Orange1' },
  //     ]
  //   : [];

  return (
    <Tabs
      onChange={(activeKey) => {
        setTabInRoutes(routeTabs.find((item) => item.path === activeKey)?.routes || []);
      }}
      type="card"
    >
      {routeTabs
        .filter((item) => item?.title && !item.accessMenuIgnore)
        .map((item) => {
          const options = item.accessFunction || [];
          return (
            <Tabs.TabPane
              tab={
                <Fragment>
                  {!item?.defTab && (
                    <AuthenticationCheckBox
                      identificationCode={item.path}
                      authenticationMap={authenticationMap}
                    />
                  )}
                  <span style={{ paddingLeft: 8 }}>{item?.title}</span>
                </Fragment>
              }
              key={item?.path}
              style={{ padding: 12 }}
            >
              {tabInRoutes.length ? (
                <ShowTabs routeTabs={tabInRoutes} authenticationMap={authenticationMap} />
              ) : (
                options
                  .filter((optEle) => optEle.type !== ACCESS_FUNCTION_TYPE.DEFAULT)
                  .map((accessFunctionItem) => (
                    <AuthenticationCheckBox
                      accessFunctionBox={true}
                      identificationCode={accessFunctionItem.path}
                      authenticationMap={authenticationMap}
                      label={accessFunctionItem.title}
                    />
                  ))
              )}
            </Tabs.TabPane>
          );
        })}
    </Tabs>
  );
}
