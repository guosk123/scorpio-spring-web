import TimeRangeSlider from '@/components/TimeRangeSlider';
import { Space } from 'antd';
import pathToRegexp from 'path-to-regexp';
import { useLocation } from 'umi';
import styles from './index.less';

interface Props {
  children: any;
}

const Layout = (props: Props) => {
  const { children } = props;

  const { pathname } = useLocation();

  const hideTimeSlider =
    pathToRegexp(`/analysis/security/situation`).test(pathname) ||
    pathToRegexp(`/analysis/security/scenario-task(.*)`).test(pathname);

  return (
    <div className={styles.contentWrap}>
      {!hideTimeSlider && (
        <Space size="middle">
          <TimeRangeSlider />
        </Space>
      )}
      {children}
    </div>
  );
};

export default Layout;
