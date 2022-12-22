import React from 'react';
import { Progress } from 'antd';
import numeral from 'numeral';
// import { formatNumber } from '@/utils/utils';

import styles from './index.less';

const StandardProgress = ({ percent, ...rest }) => (
  <Progress
    className={styles.progress}
    size="small"
    percent={percent ? numeral(percent.toFixed(2)).value() : 0}
    status="normal"
    strokeColor={percent >= 90 ? 'red' : ''}
    {...rest}
  />
);

export default StandardProgress;
