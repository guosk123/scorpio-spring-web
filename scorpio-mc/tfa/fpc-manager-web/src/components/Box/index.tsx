import React from 'react';
import styles from './index.less';

interface IBox {
  title: string;
}
const Box: React.FC<IBox> = ({ title, children }) => (
  <div className={styles.box}>
    <div className={styles['box-title']}>{title}</div>
    <div className={styles['box-content']}>{children}</div>
  </div>
);

export default Box;
