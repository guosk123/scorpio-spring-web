import type { ConnectState } from '@/models/connect';
import type { IProductInfo } from '@/models/frame/global';
import { DefaultFooter } from '@ant-design/pro-layout';
import React from 'react';
import { connect } from 'dva';
import styles from './index.less';

export interface GlobalFooterProps {
  productInfos: IProductInfo;
}

const GlobalFooter: React.FC<GlobalFooterProps> = ({ productInfos }) => {
  return (
    <DefaultFooter
      className={styles.footer}
      links={[]}
      copyright={`${new Date().getFullYear()} ${productInfos?.corporation}`}
    />
  );
};

export default connect(({ globalModel: { productInfos } }: ConnectState) => ({
  productInfos,
}))(GlobalFooter);
