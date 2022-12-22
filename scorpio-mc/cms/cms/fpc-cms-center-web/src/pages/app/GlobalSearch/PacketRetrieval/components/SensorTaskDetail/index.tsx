import { Button, Drawer } from 'antd';
import { useState } from 'react';
import TransmitTaskProfile from '../TransmitTaskProfile';

interface Props {
  detail?: any;
  taskDetail?: any;
}

export default function SensorTaskDetail(props: Props) {
  const { detail, taskDetail } = props;
  const [visible, setVisible] = useState(false);

  return (
    <div>
      <Button
        size={'small'}
        type={'link'}
        onClick={() => {
          setVisible(true);
        }}
      >
        详情
      </Button>
      <Drawer
        width={900}
        destroyOnClose
        title="任务详情"
        visible={visible}
        onClose={() => {
          setVisible(false);
        }}
      >
        <TransmitTaskProfile loading={false} detail={{ sensorDetail: detail, taskDetail }} />
      </Drawer>
    </div>
  );
}
