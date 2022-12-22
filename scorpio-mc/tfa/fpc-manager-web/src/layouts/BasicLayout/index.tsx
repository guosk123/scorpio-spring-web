/**
 * Ant Design Pro v4 use `@ant-design/pro-layout` to handle Layout.
 * You can view component api by:
 * https://github.com/ant-design/ant-design-pro-layout
 */
import { PRODUCT_LOGO } from '@/common/applicationConfig';
import RightContent from '@/components/GlobalHeader/RightContent';
import type { ConnectState } from '@/models/connect';
import { RollbackOutlined } from '@ant-design/icons';
import type { BasicLayoutProps as ProLayoutProps, MenuDataItem } from '@ant-design/pro-layout';
import ProLayout, { getMenuData } from '@ant-design/pro-layout';
import { getBreadcrumbProps } from '@ant-design/pro-layout/lib/utils/getBreadcrumbProps';
import { Affix, Breadcrumb } from 'antd';
import type { DefaultSettings } from 'config/defaultSettings';
import React, { useEffect, useRef, useState } from 'react';
import type { Dispatch, IProductInfo } from 'umi';
import { connect, history, Link, useIntl, useLocation } from 'umi';
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
  const { dispatch, children, settings, productInfos } = props;
  const menuDataRef = useRef<MenuDataItem[]>([]);
  const { formatMessage } = useIntl();

  const { pathname } = useLocation();
  const [subMenuKeys, setSelectedKeys] = useState<string[]>([]);

  const handleMenuCollapse = (payload: boolean): void => {
    if (dispatch) {
      dispatch({
        type: 'globalModel/changeLayoutCollapsed',
        payload,
      });
    }
  };

  useEffect(() => {
    const keys: string[] = [];
    const parent = menuDataRef.current.find((item) => {
      if (!item.path) return false;
      return pathname.indexOf(item.path) > -1;
    });

    if (parent) {
      if (parent.key) {
        keys.push(parent.key);
      }
      const child = parent.routes?.find((r) => {
        if (!r.path) return false;
        return pathname.indexOf(r.path) > -1;
      });
      if (child?.key) {
        keys.push(child.key);
      }
    }

    setSelectedKeys(keys);
  }, [pathname]);

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
            onClick={() => history.goBack()}
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
    let pageHeaderContent = null;

    pageHeaderContent = (
      <div className={styles.subBar}>
        <div className={`${styles.subBarItem} ${styles.breadcrumb}`}>{renderBreadcrumb()}</div>
      </div>
    );

    return (
      <div className={styles.pageWraper}>
        <Affix offsetTop={48}>
          <div className={styles.pageHeader}>{pageHeaderContent}</div>
        </Affix>
        <div className={styles.pageContainer}>{children}</div>
      </div>
    );
  };

  return (
    <ProLayout
      className={styles.layout}
      logo={<img src={productInfos?.logoBase64 || PRODUCT_LOGO} />}
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
      itemRender={(route, _, routes) => {
        const last = routes.indexOf(route) === routes.length - 1;
        return last ? (
          <span
            className="breadcrumb-go-back"
            style={{ cursor: 'pointer' }}
            onClick={() => history.goBack()}
          >
            <RollbackOutlined /> {route.breadcrumbName}
          </span>
        ) : (
          <span>{route.breadcrumbName}</span>
        );
      }}
      footerRender={() => null}
      menuDataRender={menuDataRender}
      rightContentRender={() => <RightContent />}
      headerContentRender={() => <div className={styles.headerContentWrap} />}
      postMenuData={(menuData) => {
        menuDataRef.current = menuData || [];
        return menuData || [];
      }}
      menuProps={{
        multiple: false,
        onSelect: ({ selectedKeys }) => {
          setSelectedKeys(selectedKeys);
        },
        selectedKeys: subMenuKeys,
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
}))(BasicLayout);
