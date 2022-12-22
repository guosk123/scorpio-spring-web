import { Tabs } from 'antd';
import { history, useParams } from 'umi';
import { useState, useEffect, useMemo, useCallback } from 'react';
import type { IUrlParams } from '../../typing';
import styles from './index.less';

export interface IRoute {
  exact: boolean;
  name: string;
  path: string;
  title: string;
}

export interface IFlowLayout {
  location: {
    pathname: string;
  };
  route: {
    routes: IRoute[];
  };
  match: {
    path: string;
    url: string;
  };
  children?: React.ReactNode;
}

const FlowLayout: React.FC<IFlowLayout> = ({
  location: { pathname },
  route: { routes },
  children,
}) => {
  // 动态路由
  const urlParams = useParams<IUrlParams>();
  // 记录当前活跃的标签
  const [activeMenu, setActiveMenu] = useState<string>('');
  // 所有标签菜单
  const menus = useMemo(() => routes.filter((route) => route.title !== undefined), [routes]);
  // 用来匹配合适的标签
  const findMatchedTag = useCallback(
    (pathName: string): any => {
      if (pathName === '') {
        return '';
      }
      const path = pathName
        .split('?')[0] // 清除参数
        .replace(urlParams.deviceName, ':deviceName')
        .replace(urlParams.netifNo, ':netifNo');
      let result = '';
      for (let index = 0; index < menus.length; index += 1) {
        const element = menus[index];
        if (element.path === path) {
          result = element.path;
          break;
        }
      }
      if (result) {
        return result;
      }
      const pathList = pathName.split('?')[0].split('/');
      pathList.pop();
      return findMatchedTag(pathList.join('/'));
    },
    [menus, urlParams.deviceName, urlParams.netifNo],
  );
  // 页面改变回调函数
  function handlePageChange(path: string) {
    setActiveMenu(path);
    if (path !== pathname && path !== activeMenu) {
      history.push(
        path.replace(':deviceName', urlParams.deviceName).replace(':netifNo', urlParams.netifNo),
      );
    }
  }
  // 查找标签
  useEffect(() => {
    setActiveMenu(findMatchedTag(location.hash.slice(1)));
  }, [findMatchedTag, menus, pathname]);

  return (
    <>
      <Tabs
        className={styles.tab}
        activeKey={activeMenu}
        size="small"
        type="card"
        onTabClick={handlePageChange}
      >
        {menus.map((menu) => (
          <Tabs.TabPane tab={menu.title} key={menu.path as string} />
        ))}
      </Tabs>
      {children}
    </>
  );
};

export default FlowLayout;
