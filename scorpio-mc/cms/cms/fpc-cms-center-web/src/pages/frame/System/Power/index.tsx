import { PauseCircleOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Modal, Space } from 'antd';
import { connect } from 'dva';
import React from 'react';
import type { Dispatch } from 'redux';

interface ISystemPowerProps {
  dispatch: Dispatch<any>;
}
const SystemPower: React.FC<ISystemPowerProps> = ({ dispatch }) => {
  const handleReboot = () => {
    Modal.confirm({
      title: '确定要重启设备吗？',
      onOk: () => {
        dispatch({
          type: 'systemPowerModel/powerReboot',
        });
      },
      onCancel: () => {},
    });
  };

  const handleShadow = () => {
    Modal.confirm({
      title: '确定要关闭设备吗？',
      onOk: () => {
        dispatch({
          type: 'systemPowerModel/powerShutdown',
        });
      },
      onCancel: () => {},
    });
  };

  return (
    <div style={{ textAlign: 'center' }}>
      <Space align="center">
        <Button icon={<ReloadOutlined />} type="primary" onClick={handleReboot}>
          设备重启
        </Button>
        <Button icon={<PauseCircleOutlined />} type="primary" danger onClick={handleShadow}>
          设备关机
        </Button>
      </Space>
    </div>
  );
};

export default connect(
  ({ loading: { effects } }: { loading: { effects: Record<string, boolean> } }) => ({
    rebootLoading: effects['systemPowerModel/powerReboot'],
    shutdownLoading: effects['systemPowerModel/powerShutdown'],
  }),
)(SystemPower);
