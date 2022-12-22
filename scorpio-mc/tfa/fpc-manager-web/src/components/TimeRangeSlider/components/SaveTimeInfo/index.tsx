import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { createSelfDefinedTime } from '@/pages/app/configuration/SelfDefinedTime/services';
import { ECustomTimeType } from '@/pages/app/configuration/SelfDefinedTime/typings';
import { ClockCircleOutlined } from '@ant-design/icons';
import { Button, Input, message, Modal } from 'antd';
import { Fragment, useState } from 'react';
import { useSelector } from 'umi';

function SaveTimeInfo() {
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
      createSelfDefinedTime({
        name: timeInfoName,
        type: ECustomTimeType.DisposableTime,
        customTimeSetting: JSON.stringify([
          {
            start_time_1: globalSelectedTime.originStartTime,
            end_time_1: globalSelectedTime.originEndTime,
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
        />
      </Modal>
    </Fragment>
  );
}

export default SaveTimeInfo;
