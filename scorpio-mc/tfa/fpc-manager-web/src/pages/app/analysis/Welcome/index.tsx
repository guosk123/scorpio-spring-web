import type { ConnectState } from '@/models/connect';
import { SmileOutlined } from '@ant-design/icons';
import { Result } from 'antd';
import type { IProductInfo } from '@/models/frame/global';
import { useSelector } from 'umi';

export default function Welcome() {
  const productInfos = useSelector<ConnectState, Required<IProductInfo>>((state) => ({
    ...state.globalModel.productInfos,
    description: '',
  }));
  return <Result icon={<SmileOutlined />} title={`欢迎使用${productInfos.name || '分析系统'}`} />;
}
