import styles from './index.less';
import { Button } from 'antd';
import {
  LockOutlined,
  PicCenterOutlined,
  SaveOutlined,
  SelectOutlined,
  UnlockOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
} from '@ant-design/icons';

export interface IControlAction {
  key: 'center' | 'zoomOut' | 'zoomIn' | 'lock' | 'unlock' | 'save' | 'brush';
  description: string;
  render: any;
}

interface IGraphControlProps {
  onClick?: (action: IControlAction) => void;
  historyGraph?: boolean;
}
const GraphControl = ({ onClick, historyGraph = true }: IGraphControlProps) => {
  const controlActions: IControlAction[] = [
    {
      key: 'center',
      description: '居中',
      render: () => {
        return <Button shape="circle" icon={<PicCenterOutlined />} />;
      },
    },
    {
      key: 'zoomOut',
      description: '放大',
      render: () => {
        return <Button shape="circle" icon={<ZoomInOutlined />} />;
      },
    },
    {
      key: 'zoomIn',
      description: '缩小',
      render: () => {
        return <Button shape="circle" icon={<ZoomOutOutlined />} />;
      },
    },
    {
      key: 'lock',
      description: '锁定布局',
      render: () => {
        return <Button shape="circle" icon={<LockOutlined />} />;
      },
    },
    {
      key: 'unlock',
      description: '解锁布局',
      render: () => {
        return <Button shape="circle" icon={<UnlockOutlined />} />;
      },
    },
    {
      key: 'save',
      description: '保存成图片',
      render: () => {
        return <Button shape="circle" icon={<SaveOutlined />} />;
      },
    },
    {
      key: 'brush',
      description: '框选',
      render: () => {
        return <Button shape="circle" icon={<SelectOutlined />} />;
      },
    },
  ].filter(({ key }) => {
    if (!historyGraph && key !== 'brush') {
      return true;
    }
    return false;
  }) as IControlAction[];

  return (
    <div className={styles.control}>
      {controlActions.map((action) => (
        <div
          key={action.key}
          className={styles['control-item']}
          onClick={() => {
            if (onClick) {
              onClick(action);
            }
          }}
          title={action.description}
        >
          {action.render()}
        </div>
      ))}
    </div>
  );
};

export default GraphControl;
