import type { ConnectState } from '@/models/connect';
import { QuestionCircleTwoTone } from '@ant-design/icons';
import { Form, Input, Modal } from 'antd';
import { connect } from 'dva';
import { Fragment } from 'react';
import type { Dispatch } from 'umi';
import type { ISystemAlarm } from '../../typings';

const { TextArea } = Input;

interface IProps {
  dispatch: Dispatch;
  currentItem: ISystemAlarm;
  modalVisible: boolean;
  solveLoading?: boolean;
  callback?: () => void;
}
const AlarmSolveModel = ({
  dispatch,
  currentItem,
  modalVisible,
  solveLoading,
  /** 完成后的回调 */
  callback,
}: IProps) => {
  const [form] = Form.useForm();

  const handleSolveCancel = () => {
    dispatch({
      type: 'alarmModel/hideModal',
    });
  };

  // 处理解决告警
  const handleSolve = async () => {
    try {
      const { reason } = await form.validateFields();
      const { id } = currentItem;
      dispatch({
        type: 'alarmModel/solveAlerm',
        payload: {
          id,
          reason,
        },
      }).then((success: boolean) => {
        if (success) {
          handleSolveCancel();
          if (callback) {
            callback();
          }
        }
      });
    } catch (errorInfo) {
      //
    }
  };

  return (
    <Modal
      title={
        <Fragment>
          <QuestionCircleTwoTone twoToneColor="#faad14" style={{ marginRight: 4 }} />
          确认将此告警标记为【已解决】吗？
        </Fragment>
      }
      bodyStyle={{ paddingTop: 5 }}
      visible={modalVisible}
      destroyOnClose
      keyboard={false}
      maskClosable={false}
      onOk={handleSolve}
      confirmLoading={solveLoading}
      onCancel={handleSolveCancel}
    >
      <Form form={form} layout="vertical">
        <Form.Item label="备注信息" name="reason" style={{ marginBottom: 0 }}>
          <TextArea placeholder="非必填，请填写解决的备注信息" rows={4} />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default connect(
  ({
    loading: { effects },
    alarmModel: { modalVisible, currentItem },
  }: ConnectState & {
    alarmModel: {
      currentItem: ISystemAlarm;
      modalVisible: boolean;
    };
  }) => ({
    currentItem,
    modalVisible,
    solveLoading: effects['alarmModel/solveAlerm'],
  }),
)(AlarmSolveModel);
