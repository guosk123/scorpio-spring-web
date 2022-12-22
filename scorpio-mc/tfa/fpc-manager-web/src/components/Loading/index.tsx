import type { SpinProps } from 'antd';
import { Spin } from 'antd';
import styles from './index.less';

interface ILoadingProps extends SpinProps {
  height?: number;
}

const Loading = ({ height = 100, ...restProps }: ILoadingProps) => (
  <div className={styles['loading-wrap']} style={{ height }}>
    <Spin size="small" {...restProps} />
  </div>
);

export default Loading;
