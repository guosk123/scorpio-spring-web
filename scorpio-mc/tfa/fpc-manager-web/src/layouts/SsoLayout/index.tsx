import GlobalFooter from '@/components/GlobalFooter';
import type { ConnectState } from '@/models/connect';
import type { GlobalModelState } from '@/models/frame/global';
import { PageLoading } from '@ant-design/pro-layout';
import { Card } from 'antd';
import React, { useEffect } from 'react';
import { Helmet, HelmetProvider } from 'react-helmet-async';
import type { ConnectProps, Dispatch } from 'umi';
import { connect } from 'dva';
import styles from './index.less';

export interface SsoLayoutProps extends Partial<ConnectProps> {
  dispatch: Dispatch;
  globalModel: GlobalModelState;
  loading?: boolean;
}

const SsoLayout: React.FC<SsoLayoutProps> = (props) => {
  const { children, dispatch, loading, globalModel } = props;

  const { productInfos } = globalModel;
  useEffect(() => {
    if (dispatch) {
      dispatch({
        type: 'globalModel/queryProductInfos',
      });
    }
  }, []);

  if (loading) {
    return <PageLoading />;
  }

  return (
    <HelmetProvider>
      <Helmet>
        <title>单点登录</title>
        <meta name="description" content="单点登录" />
      </Helmet>

      <div className={styles.container}>
        <div className={styles.lang}>{/* <SelectLang /> */}</div>
        <div className={styles.content}>
          <div className={styles.header}>
            <div className={styles.header}>
              <span className={styles.productTitle}>{productInfos.name}</span>
            </div>
            <div className={styles.desc}>单点登录</div>
          </div>
          <Card bordered={false} className={styles.innerWrap}>
            {children}
          </Card>
        </div>
        <GlobalFooter />
      </div>
    </HelmetProvider>
  );
};

export default connect(({ settings, globalModel, loading }: ConnectState) => ({
  ...settings,
  globalModel,
  loading: loading.models.globalModel,
}))(SsoLayout);
