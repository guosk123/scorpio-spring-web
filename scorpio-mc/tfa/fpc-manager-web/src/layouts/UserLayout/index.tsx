import GlobalFooter from '@/components/GlobalFooter';
import type { ConnectState } from '@/models/connect';
import type { GlobalModelState } from '@/models/frame/global';
import { PageLoading } from '@ant-design/pro-layout';
import React, { useEffect } from 'react';
import { Helmet, HelmetProvider } from 'react-helmet-async';
import type { ConnectProps, Dispatch } from 'umi';
import { connect, history } from 'umi';
import styles from './index.less';

export interface UserLayoutProps extends Partial<ConnectProps> {
  dispatch: Dispatch;
  globalModel: GlobalModelState;
  loading?: boolean;
}

const UserLayout: React.FC<UserLayoutProps> = (props) => {
  const { children, dispatch, loading, globalModel } = props;
  const { productInfos, currentUser } = globalModel;

  useEffect(() => {
    // 检查是已经登录了
    if (currentUser.id) {
      history.push('/redirect');
      return;
    }
    if (dispatch) {
      dispatch({
        type: 'globalModel/queryProductInfos',
      });
    }
  }, [dispatch, currentUser]);

  if (loading) {
    return <PageLoading />;
  }

  return (
    <HelmetProvider>
      <Helmet>
        <title>登录</title>
        <meta name="description" content="登录" />
      </Helmet>

      <div className={styles.container}>
        <div className={styles.lang}>{/* <SelectLang /> */}</div>
        <div className={styles.content}>
          <div className={styles.top}>
            <div className={styles.header}>
              <span className={styles.title}>{productInfos.name}</span>
            </div>
            <div className={styles.desc} />
          </div>
          {children}
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
}))(UserLayout);
