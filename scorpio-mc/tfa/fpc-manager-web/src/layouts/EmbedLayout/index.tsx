/**
 * Ant Design Pro v4 use `@ant-design/pro-layout` to handle Layout.
 * You can view component api by:
 * https://github.com/ant-design/ant-design-pro-layout
 */
import type { ConnectState } from '@/models/connect';
import type { INetwork } from '@/pages/app/configuration/Network/typings';
import type {
  BasicLayoutProps as ProLayoutProps,
  MenuDataItem,
  Settings,
} from '@ant-design/pro-layout';
import { getMenuData, getPageTitle } from '@ant-design/pro-layout';
import { Button, Result } from 'antd';
import React, { useEffect } from 'react';
import type { Dispatch, IProductInfo } from 'umi';
import { connect, history, useIntl } from 'umi';
import styles from '../BasicLayout/index.less';

export interface BasicLayoutProps extends ProLayoutProps {
  breadcrumbNameMap: Record<string, MenuDataItem>;
  route: ProLayoutProps['route'] & {
    authority: string[];
  };
  settings: Settings;
  dispatch: Dispatch;
  productInfos: IProductInfo;
  allNetworks: INetwork[];
}
export type BasicLayoutContext = { [K in 'location']: BasicLayoutProps[K] } & {
  breadcrumbNameMap: Record<string, MenuDataItem>;
};

const EmbedLayout: React.FC<BasicLayoutProps> = (props) => {
  const {
    children,
    location = {
      pathname: '/',
    },
    productInfos,
  } = props;
  const { formatMessage } = useIntl();
  const noMatch = (
    <Result
      status="403"
      title="403"
      subTitle="抱歉，你无权访问该页面"
      extra={[
        <Button key="go-home" type="primary" onClick={() => history.push('/embed/home')}>
          返回首页
        </Button>,
      ]}
    />
  );

  const { breadcrumb } = getMenuData(props.route?.routes || [], { locale: true }, formatMessage);
  const title = getPageTitle(
    {
      pathname: location.pathname,
      breadcrumb,
      ...props,
    },
    true,
  );

  useEffect(() => {
    document.title = `${title}- ${productInfos?.name}`;
  }, [title, productInfos]);

  const renderPage = () => {
    const { pathname } = location;
    if (pathname === '/embed/home') {
      return children;
    }
    return (
      <div className={styles.pageWraper}>
        <div className={styles.pageContainer}>{children}</div>
      </div>
    );
  };

  return <div className={styles.layout}>{renderPage()}</div>;
};

export default connect(({ globalModel: { collapsed, productInfos }, settings }: ConnectState) => ({
  collapsed,
  productInfos,
  settings,
}))(EmbedLayout);
