import TimeRangeSlider from '@/components/TimeRangeSlider';
import { Space } from 'antd';

interface Props {
  children: any;
}

const Layout = (props: Props) => {
  const { children } = props;

  return (
    <div>
      <Space size="middle">
        <TimeRangeSlider />
      </Space>
      {children}
    </div>
  );
};

export default Layout;
