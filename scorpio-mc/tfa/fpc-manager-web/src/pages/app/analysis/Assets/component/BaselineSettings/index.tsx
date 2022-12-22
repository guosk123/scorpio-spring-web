import { Button, Checkbox, Col, Form, Input, message, Modal } from 'antd';
import { EbaselineTypeNameMap } from '../../typing';
import { useCallback, useState } from 'react';
import { enumObj2List } from '@/utils/utils';
import { getbaselineLists, settingsbaseline } from '../../service';

interface IBaselineSettingProps {
  buttonType: 'link' | 'primary';
  buttonName: string;
  ipAddress: string;
  type?: string[];
  reloadList?: () => void;
  operationType: 'create' | 'update';
}
const formItemLayout = {
  labelCol: { span: 2 },
  wrapperCol: { span: 22 },
};

const BaselineSettings: React.FC<IBaselineSettingProps> = ({
  buttonType,
  buttonName,
  ipAddress,
  type,
  reloadList,
  operationType,
}) => {
  const [form] = Form.useForm();

  const [loading, setLoading] = useState<boolean>(false);

  const [isModalVisible, setIsModalVisible] = useState(false);
  // const showModal = () => {
  //   setIsModalVisible(true);
  // };
  const unShowModal = () => {
    setIsModalVisible(false);
  };

  const [hasBasline, setHasBaseLine] = useState<boolean>(false);

  const queryBaseline = useCallback(
    (params) => {
      if (operationType === 'create') {
        setIsModalVisible(false);
      }
      if (operationType === 'update') {
        setLoading(true);
      }
      getbaselineLists(params).then((res) => {
        const { success, result } = res;
        if (success) {
          const { content } = result;
          if (operationType === 'create') {
            if (content.length > 0) {
              setHasBaseLine(true);
            }
            if (content.length === 0) {
              setIsModalVisible(true);
            }
          }
          if (operationType === 'update' && content.length === 1) {
            form.setFieldsValue(content[0]);
            setLoading(false);
            setIsModalVisible(true);
          }
        }
        else{
          message.error('查询基线失败!');
        }
      });
    },
    [form, operationType],
  );

  const handleOk = () => {
    setHasBaseLine(false);
    setIsModalVisible(true);
  };
  const handleCancel = () => {
    setHasBaseLine(false);
  };

  const judgeHasBasline = () => {
    if (operationType === 'create') {
      queryBaseline({ ipAddress: ipAddress });
    }
    if (operationType === 'update') {
      queryBaseline({ ipAddress: ipAddress });
    }
  };

  const submitData = async () => {
    const formValue = form.getFieldsValue();
    console.log(formValue, 'formValue');
    const types = [...new Set(formValue.type)].join(',');
    console.log(types, 'types');
    const params = {
      ipAddress: formValue.ipAddress,
      description: formValue.description,
      type: types,
    };
    console.log(params, 'params');
    const { success, result } = await settingsbaseline(params);
    console.log(result, 'result');
    if (success) {
      message.success('提交成功');
    } else {
      message.error('提交失败!');
    }
    if (reloadList) {
      reloadList();
    }
    unShowModal();
  };
  return (
    <>
      <Button type={buttonType} onClick={judgeHasBasline} loading={loading}>
        {buttonName}
      </Button>

      <Modal
        width={860}
        title="资产基线设置"
        visible={isModalVisible}
        onCancel={unShowModal}
        closeIcon={false}
        destroyOnClose
        footer={[
          <Button key="submit" type="primary" onClick={submitData}>
            确认
          </Button>,
          <Button key="cancel" type="primary" onClick={unShowModal}>
            关闭
          </Button>,
        ]}
      >
        <Form
          {...formItemLayout}
          form={form}
          // onFinish={beforeSubmit}
          initialValues={{ ipAddress: ipAddress, type: type }}
        >
          <Form.Item name="ipAddress" label="IP地址">
            <Input placeholder="请输入IP地址" disabled />
          </Form.Item>
          <Form.Item name="type" label="类型">
            <Checkbox.Group>
              {enumObj2List(EbaselineTypeNameMap).map((item: any) => {
                return (
                  <Col span={24}>
                    <Checkbox key={item.key} value={item.value}>
                      {item.label}
                    </Checkbox>
                  </Col>
                );
              })}
            </Checkbox.Group>
          </Form.Item>
          <Form.Item name="description" label="基线描述">
            <Input.TextArea placeholder="描述内容" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        okText="确认"
        cancelText="不用了"
        visible={hasBasline}
        onCancel={handleCancel}
        onOk={handleOk}
        destroyOnClose
      >
        此操作会覆盖之前的基线，请问是否还要继续？
      </Modal>
    </>
  );
};

export default BaselineSettings;
