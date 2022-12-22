import classNames from 'classnames';
import styles from './index.less';
import FloatWindow from '../components/FloatWindow';
import { useState } from 'react';
import { history } from 'umi';

export default function TaskLayout(props: any) {
  const { children } = props;
  const [collapsed, setCollapsed] = useState<boolean>(false);

  if (
    history.location.pathname !== '/analysis/trace/transmit-task'
  ) {
    return <div>{children}</div>;
  }

  return (
    <div className={classNames([styles.layoutWrap, collapsed && styles.collapsed])}>
      <div className={styles.leftWrap}>
        <FloatWindow collapsed={collapsed} onToggleCollapsed={setCollapsed} />
      </div>
      <div className={styles.contentWrap}>{children}</div>
    </div>
  );
}
