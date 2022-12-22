/**
 * Ant Design Pro v4 use `@ant-design/pro-layout` to handle Layout.
 * You can view component api by:
 * https://github.com/ant-design/ant-design-pro-layout
 */
import config from '@/common/applicationConfig';
import RightContent from '@/components/GlobalHeader/RightContent';
import type { ConnectState } from '@/models/connect';
import { RollbackOutlined } from '@ant-design/icons';
import type { BasicLayoutProps as ProLayoutProps, MenuDataItem } from '@ant-design/pro-layout';
import ProLayout, { getMenuData } from '@ant-design/pro-layout';
import { getBreadcrumbProps } from '@ant-design/pro-layout/lib/utils/getBreadcrumbProps';
import { Affix, Breadcrumb } from 'antd';
import type { DefaultSettings } from 'config/defaultSettings';
import React, { useRef, useState } from 'react';
import type { Dispatch, IProductInfo } from 'umi';
import { connect, history, Link, useIntl } from 'umi';
import styles from './index.less';

export interface BasicLayoutProps extends ProLayoutProps {
  breadcrumbNameMap: Record<string, MenuDataItem>;
  route: ProLayoutProps['route'] & {
    access: string;
  };
  settings: DefaultSettings;
  dispatch: Dispatch;
  productInfos: IProductInfo;
}
export type BasicLayoutContext = { [K in 'location']: BasicLayoutProps[K] } & {
  breadcrumbNameMap: Record<string, MenuDataItem>;
};

export const menuDataRender = (menuList: MenuDataItem[]): MenuDataItem[] => {
  return menuList.map((item) => {
    return {
      ...item,
      children: item.children ? menuDataRender(item.children) : undefined,
    };
  });
};

const BasicLayout: React.FC<BasicLayoutProps> = (props) => {
  const {
    dispatch,
    children,
    settings,
    location = {
      pathname: '/',
    },
    productInfos,
  } = props;
  const menuDataRef = useRef<MenuDataItem[]>([]);
  const [, setHeaderAffixed] = useState<boolean | undefined>(false);

  const { formatMessage } = useIntl();

  const handleMenuCollapse = (payload: boolean): void => {
    if (dispatch) {
      dispatch({
        type: 'globalModel/changeLayoutCollapsed',
        payload,
      });
    }
  };

  const { breadcrumbMap } = getMenuData(
    props.route?.routes || [],
    { locale: true },
    formatMessage,
    menuDataRender,
  );

  // gen breadcrumbProps, parameter for pageHeader
  const breadcrumbProps = getBreadcrumbProps(
    {
      ...props,
      breadcrumbMap,
    },
    props,
  );

  const breadcrumbRoutes = breadcrumbProps?.routes || [];
  const showBack = breadcrumbRoutes.length > 0 && history.length > 1;
  if (showBack) {
    breadcrumbRoutes.push({
      path: 'go-back',
      breadcrumbName: '返回上一页',
    });
  }

  const renderBreadcrumb = () => (
    <Breadcrumb
      className={showBack ? styles.showBack : null}
      routes={breadcrumbRoutes}
      itemRender={(route, _, routes) => {
        const last = routes.indexOf(route) === routes.length - 1;
        return last && route.path === 'go-back' ? (
          <span
            className="breadcrumb-go-back"
            style={{ cursor: 'pointer' }}
            onClick={() => {
              history.goBack();
            }}
          >
            <RollbackOutlined /> {route.breadcrumbName}
          </span>
        ) : (
          <span>{route.breadcrumbName}</span>
        );
      }}
    />
  );

  const renderPage = () => {
    const { pathname } = location;
    let pageHeaderContent = null;

    if (pathname === '/analysis/situation/network') {
      return children;
    }

    pageHeaderContent = (
      <div className={styles.subBar}>
        <div className={`${styles.subBarItem} ${styles.breadcrumb}`}>{renderBreadcrumb()}</div>
      </div>
    );

    return (
      <div className={styles.pageWraper}>
        <Affix offsetTop={48} onChange={(affixed) => setHeaderAffixed(affixed)}>
          <div className={styles.pageHeader}>{pageHeaderContent}</div>
        </Affix>
        <div className={styles.pageContainer}>{children}</div>
      </div>
    );
  };

  return (
    <ProLayout
      className={styles.layout}
      logo={<img src={productInfos?.logoBase64 || config.PRODUCT_LOGO} />}
      {...settings}
      title={productInfos.name || ''}
      formatMessage={formatMessage}
      {...props}
      onCollapse={handleMenuCollapse}
      onMenuHeaderClick={() => history.push('/')}
      isMobile={false}
      menuItemRender={(menuItemProps, defaultDom) => {
        if (menuItemProps.isUrl || !menuItemProps.path) {
          return defaultDom;
        }
        return <Link to={menuItemProps.path}>{defaultDom}</Link>;
      }}
      // itemRender={(route, _, routes) => {
      //   const last = routes.indexOf(route) === routes.length - 1;
      //   return last ? (
      //     <span
      //       className="breadcrumb-go-back"
      //       style={{ cursor: 'pointer' }}
      //       onClick={() => history.goBack()}
      //     >
      //       <RollbackOutlined /> {route.breadcrumbName}
      //     </span>
      //   ) : (
      //     <span>{route.breadcrumbName}</span>
      //   );
      // }}
      footerRender={() => null}
      menuDataRender={menuDataRender}
      rightContentRender={() => <RightContent />}
      headerContentRender={() => <div className={styles.headerContentWrap} />}
      postMenuData={(menuData) => {
        menuDataRef.current = menuData || [];
        return menuData || [];
      }}
    >
      {renderPage()}
    </ProLayout>
  );
};

export default connect(({ globalModel: { collapsed, productInfos }, settings }: ConnectState) => ({
  collapsed,
  productInfos,
  settings,
}))(React.memo(BasicLayout));
