import { pageIsEmbed } from '@/utils/utils';
import { getMenuData } from '@ant-design/pro-layout';
import { getMatchMenu } from '@umijs/route-utils';
import { Affix, Tabs } from 'antd';
import { useMemo, useRef } from 'react';
import { history, useIntl, useParams } from 'umi';
import type { BasicLayoutProps } from '../BasicLayout';
import { menuDataRender } from '../BasicLayout';
import styles from './index.less';

// 判断是否是内嵌页面，内嵌页面不固定位置
const ContentWrap = (props: any) => {
  if (!pageIsEmbed()) {
    return (
      // 78 = 48px(公共 Header) + 30px(面包屑)
      <Affix key={location.pathname} offsetTop={78}>
        {props.children}
      </Affix>
    );
  }
  return <>{props.children}</>;
};

export interface IPageLayout {
  location: {
    pathname: string;
    query: Record<string, any>;
  };
  route: BasicLayoutProps['route'] & {
    authority: string[];
  };
  match: {
    path: string;
    url: string;
  };
  children?: React.ReactNode;
}

const PageLayout: React.FC<IPageLayout> = ({
  route,
  // match,
  children,
  location = {
    pathname: '/',
    query: {},
  },
}) => {
  const { formatMessage } = useIntl();
  const params: {
    networkId?: string;
    serviceId?: string;
    pcapFileId?: string;
  } = useParams();

  const container = useRef<HTMLDivElement | null>(null);

  const { menuData } = getMenuData(
    route?.routes || [],
    { locale: false },
    formatMessage,
    menuDataRender,
  );

  // 权限过滤
  const accessibleMenuData = useMemo(() => {
    return menuData.filter((el) => !el.unaccessible);
  }, [menuData]);

  // 有权限的菜单
  const matchMenus = useMemo(() => {
    return getMatchMenu(location.pathname || '/', accessibleMenuData || [], true);
  }, [location.pathname, accessibleMenuData]);

  // 找到第一个即可
  const matchMenuKeys = useMemo(
    () => Array.from(new Set(matchMenus.map((item) => item.key || item.path || ''))),
    [matchMenus],
  );

  // console.log('route', route);
  // console.log('menuData', menuData);
  // console.log('match', match);
  // console.log('matchMenus', matchMenus);
  // console.log('matchMenuKeys', matchMenuKeys);
  // console.log('menuData', menuData);
  // console.log('allMatchMenus', allMatchMenus);

  const lastDeepMenus = useMemo(() => {
    return accessibleMenuData.filter((menu) => !!menu.title);
  }, [accessibleMenuData]);

  const activeTabKey = useMemo(() => {
    // 这里是硬编码了
    // 如果是两层嵌套的话，数组的第二个值是 Tab 的 Key
    if (matchMenuKeys.length === 3) {
      return matchMenuKeys[1];
    }
    // 如果是一层嵌套，最后一个值就是 Tab 的 key
    return matchMenuKeys[matchMenuKeys.length - 1];
  }, [matchMenuKeys]);

  const handlePageChange = (path: string) => {
    if (path === location.pathname || path === activeTabKey) {
      return;
    }

    history.push({
      pathname: path
        .replace('/analysis/network/:networkId', `/analysis/network/${params.networkId}`)
        .replace(
          '/analysis/service/:serviceId/:networkId',
          `/analysis/service/${params.serviceId}/${params.networkId}`,
        )
        .replace('/analysis/offline/:pcapFileId', `/analysis/offline/${params.pcapFileId}`),
      query: {
        ...(location.query.timeType ? { timeType: location.query.timeType } : {}),
        ...(location.query.from ? { from: location.query.from } : {}),
        ...(location.query.to ? { to: location.query.to } : {}),
        ...(location.query.unit ? { unit: location.query.unit } : {}),
        ...(location.query.relative ? { relative: location.query.relative } : {}),
      },
    });
  };

  return (
    <>
      {lastDeepMenus.length > 0 && (
        <div className={styles.pageMenuWrap} ref={container}>
          <ContentWrap>
            <Tabs activeKey={activeTabKey} type="card" size="small" onChange={handlePageChange}>
              {lastDeepMenus.map((menu) => (
                <Tabs.TabPane tab={menu.title} key={menu.key as string} />
              ))}
            </Tabs>
          </ContentWrap>
        </div>
      )}
      <>{children}</>
    </>
  );
};

export default PageLayout;
