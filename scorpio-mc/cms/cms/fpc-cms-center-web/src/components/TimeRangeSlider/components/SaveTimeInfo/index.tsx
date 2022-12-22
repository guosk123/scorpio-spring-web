import type { IGlobalTime } from '@/components/GlobalTimeSelector';
// import { ETimeType } from '@/components/GlobalTimeSelector';
// import storage from '@/utils/frame/storage';
import { ClockCircleOutlined } from '@ant-design/icons';
import { Button, Input, message, Modal } from 'antd';
import { Fragment, useState } from 'react';
import { createSingleSelfdefinedTime } from '@/pages/app/Configuration/SelfDefinedTime/services';
import { ECustomTimeType } from '@/pages/app/Configuration/SelfDefinedTime/typings';
import { useSelector } from 'umi';
import type { ConnectState } from '@/models/connect';

// interface Props {
//   globalTime: IGlobalTime;
// }

export default function SaveTimeInfo() {
  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state) => state.appModel.globalSelectedTime,
  );
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [timeInfoName, setTimeInfoName] = useState<string>();

  const showModal = () => {
    setIsModalVisible(true);
  };

  const handleOk = () => {
    if (timeInfoName) {
      createSingleSelfdefinedTime({
        name: timeInfoName,
        type: ECustomTimeType.DisposableTime,
        customTimeSetting: JSON.stringify([
          {
            start_time_1: globalSelectedTime.startTime,
            end_time_1: globalSelectedTime.endTime,
          },
        ]),
      });
    } else {
      message.error('未获取到名称');
    }
    setIsModalVisible(false);
  };

  const handleCancel = () => {
    setIsModalVisible(false);
  };

  return (
    <Fragment>
      <Button size="small" icon={<ClockCircleOutlined />} onClick={showModal} type="link">
        保存时间
      </Button>
      <Modal
        title="将当前选择时间保存到浏览器"
        visible={isModalVisible}
        onOk={handleOk}
        okButtonProps={{ disabled: !timeInfoName }}
        onCancel={handleCancel}
      >
        <Input
          addonBefore="名称"
          onChange={(e) => {
            setTimeInfoName(e.target.value);
          }}
          // ref={saveNameRef.current}
        />
      </Modal>
    </Fragment>
  );
}
