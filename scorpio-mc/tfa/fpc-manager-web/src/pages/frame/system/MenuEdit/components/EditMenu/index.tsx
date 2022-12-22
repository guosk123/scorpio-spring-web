import { Fragment, useState } from 'react';
import { Menu } from 'antd';
import AuthenticationCheckBox from '../AuthenticationCheckBox';
import type { IAccessRouteItem } from '../../typing';
import { ACCESS_FUNCTION_TYPE } from '../../typing';

interface Props {
  menuArr: IAccessRouteItem[];
  onClickMenu: any;
  authenticationMap: any;
}

export default function EditMenu(props: Props) {
  const { menuArr, onClickMenu, authenticationMap } = props;
  const [openKeysState, setOpenKeysState] = useState<string[]>([]);
  return (
    <Menu
      mode="inline"
      openKeys={openKeysState}
      onOpenChange={(openKeys) => {
        setOpenKeysState(openKeys);
      }}
      style={{
        width: 280,
        height: 'calc(100%)',
        overflow: 'hidden auto',
      }}
    >
      {menuArr
        .filter((menu) => menu?.title && !menu.accessMenuIgnore)
        .map((item) => {
          return (
            <Menu.SubMenu
              title={
                <Fragment>
                  <AuthenticationCheckBox
                    identificationCode={item.path}
                    authenticationMap={authenticationMap}
                  />
                  <span style={{ paddingLeft: 8 }}> {item?.title}</span>
                </Fragment>
              }
              key={item.path}
            >
              {(item.routes || [])
                .filter((menu) => menu?.title && !menu.accessMenuIgnore)
                .map((subItem) => {
                  return (
                    <Menu.Item
                      key={subItem.path}
                      onClick={() => {
                        const tmpShowTabs = [
                          ...((subItem.children || []).filter(
                            (childItem) =>
                              !Object.values(ACCESS_FUNCTION_TYPE).includes(childItem.type as any),
                          ) || []),
                        ];
                        onClickMenu(
                          // [{ ...subItem, defTab: true }, ...(subItem.routes || [])] || [subItem],
                          tmpShowTabs.length ? tmpShowTabs : [{ ...subItem, defTab: true }],
                        );
                      }}
                    >
                      <Fragment>
                        <AuthenticationCheckBox
                          identificationCode={subItem.path}
                          authenticationMap={authenticationMap}
                        />
                        <span style={{ paddingLeft: 8 }}>{subItem?.title}</span>
                      </Fragment>
                    </Menu.Item>
                  );
                })}
            </Menu.SubMenu>
          );
        })}
    </Menu>
  );
}
