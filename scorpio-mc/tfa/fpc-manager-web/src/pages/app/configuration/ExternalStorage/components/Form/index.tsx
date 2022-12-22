import { ONE_KILO_1024 } from '@/common/dict';
import { ipV4Regex } from '@/utils/utils';
import {
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Select,
  Space,
  Switch,
} from 'antd';
import React, { useMemo, useState } from 'react';
import { history } from 'umi';
import { createExternalStorage, updateExternalStorage } from '../../service';
import type { IExternalStorage } from '../../typings';
import {
  EExternalStorageState,
  EXTERNAL_STORAGE_TYPE_LIST,
  EXTERNAL_STORAGE_USAGE_MAP,
} from '../../typings';

/**
 * Bytes 转换为 GB
 */
export const formatCapacity2GB = (bytes: number) => {
  if (!bytes) {
    return 0;
  }
  return bytes / Math.pow(ONE_KILO_1024, 3);
};

/**
 * BG 还原回 Bytes
 */
export const formatCapacityFromGB = (gigaBytes: number) => {
  if (!gigaBytes) {
    return 0;
  }
  return gigaBytes * Math.pow(ONE_KILO_1024, 3);
};

const layout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 12 },
};
const tailLayout = {
  wrapperCol: { offset: 4, span: 12 },
};

interface IExternalStorageProps {
  detail?: IExternalStorage;
}
const ExternalStorage: React.FC<IExternalStorageProps> = ({ detail = {} as IExternalStorage }) => {
  const [form] = Form.useForm();
  const [updateLoading, setUpdateLoading] = useState<boolean>(false);

  const isUpdatePage = useMemo(() => {
    return !!detail.id;
  }, [detail.id]);

  const handleTypeChange = (type: IExternalStorage['type']) => {
    if (type === 'FTP') {
      form.setFieldsValue({ port: 21 });
    } else if (type === 'SMB') {
      form.setFieldsValue({ port: 445 });
    } else {
      form.setFieldsValue({ port: undefined });
    }
  };

  const handleUsageChange = (usage: IExternalStorage['usage']) => {
    if (usage === 'transmit_task') {
      form.setFieldsValue({ type: 'SMB' });
    }
  };

  const onFinish = (values: IExternalStorage) => {
    Modal.confirm({
      title: '确定保存吗?',
      cancelText: '取消',
      okText: '保存',
      onOk: async () => {
        setUpdateLoading(true);

        const settings: IExternalStorage = {
          ...values,
          state: values.state ? EExternalStorageState.Open : EExternalStorageState.Closed,
          capacity: formatCapacityFromGB(values.capacityGigaByte),
        };

        const { success } = await (isUpdatePage
          ? updateExternalStorage(settings)
          : createExternalStorage(settings));

        setUpdateLoading(false);

        if (!success) {
          message.error('保存失败');
          return;
        }

        message.success('保存成功');
        history.goBack();
      },
      onCancel() {},
    });
  };

  return (
    <Card bordered={false}>
      <Form
        {...layout}
        form={form}
        name="external-storage-form"
        initialValues={{
          ...detail,
          state: detail.state === EExternalStorageState.Open,
        }}
        scrollToFirstError
        onFinish={onFinish}
      >
        <Form.Item label="ID" name="id" hidden>
          <Input placeholder="id" />
        </Form.Item>
        <Form.Item
          label="名称"
          name="name"
          rules={[
            {
              required: true,
              whitespace: true,
              message: '请填写存储服务器名称',
            },
            { max: 30, message: '最多可输入30个字符' },
          ]}
        >
          <Input placeholder="填写存储服务器名称" />
        </Form.Item>

        <Form.Item
          label="用途"
          name="usage"
          rules={[{ required: true, message: '请选择服务器用途' }]}
        >
          <Select placeholder="请选择服务器用途" onChange={handleUsageChange}>
            {Object.keys(EXTERNAL_STORAGE_USAGE_MAP).map((usageKey) => (
              <Select.Option key={usageKey} value={usageKey}>
                {EXTERNAL_STORAGE_USAGE_MAP[usageKey]}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item
          noStyle
          shouldUpdate={(prevValues, curValues) => prevValues.usage !== curValues.usage}
        >
          {({ getFieldValue }) => {
            const usageValue = getFieldValue('usage');

            let typeList = [...EXTERNAL_STORAGE_TYPE_LIST];
            // 全流量查询任务，目前只支持 SMB
            if (usageValue === 'transmit_task') {
              typeList = typeList.filter((el) => el === 'SMB');
            }

            return (
              <Form.Item
                label="服务器类型"
                name="type"
                dependencies={['usage']}
                rules={[{ required: true, message: '请选择服务器类型' }]}
              >
                <Select placeholder="请选择服务器类型" onChange={handleTypeChange}>
                  {typeList.map((type) => (
                    <Select.Option key={type} value={type}>
                      {type}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>
            );
          }}
        </Form.Item>

        <Form.Item
          label="IP地址"
          name="ipAddress"
          rules={[
            { required: true, message: '请输入IP地址' },
            {
              pattern: ipV4Regex,
              message: '请输入正确的IPv4地址',
            },
          ]}
        >
          <Input placeholder="请输入IP地址" />
        </Form.Item>

        <Form.Item
          label="端口"
          name="port"
          rules={[{ required: true, type: 'number', message: '请输入端口' }]}
        >
          <InputNumber
            min={1}
            max={65535}
            precision={0}
            placeholder="请输入端口"
            style={{ width: 200 }}
          />
        </Form.Item>

        <Form.Item
          label="用户名"
          name="username"
          rules={[
            { required: true, message: '请输入用户名' },
            { max: 32, message: '最多可输入32个字符' },
          ]}
        >
          <Input placeholder="请输入用户名" />
        </Form.Item>

        <Form.Item
          label="密码"
          name="password"
          rules={[
            { required: !isUpdatePage, message: '请输入密码' },
            { max: 32, message: '最多可输入32个字符' },
          ]}
          extra={isUpdatePage ? '不填写即表示不修改' : ''}
        >
          <Input.Password autoComplete="new-password" placeholder="请输入密码" />
        </Form.Item>
        <Form.Item
          label="存储目录"
          name="directory"
          rules={[
            { required: true, message: '请输入存储目录' },
            { max: 255, message: '最多可输入255个字符' },
          ]}
        >
          <Input placeholder="请输入存储目录" />
        </Form.Item>

        <Form.Item
          noStyle
          shouldUpdate={(prevValues, curValues) => prevValues.usage !== curValues.usage}
        >
          {({ getFieldValue }) => {
            const usageValue = getFieldValue('usage');
            // 全流量查询任务需要配置容量
            if (usageValue === 'transmit_task') {
              return (
                <Form.Item label="可用容量" required>
                  <Form.Item
                    noStyle
                    name="capacityGigaByte"
                    rules={[{ required: true, type: 'number', message: '请输入可用容量' }]}
                  >
                    <InputNumber
                      min={1}
                      precision={0}
                      style={{ width: 200 }}
                      placeholder="请输入可用容量"
                    />
                  </Form.Item>
                  <span className="ant-form-text"> GiB</span>
                </Form.Item>
              );
            }
            return undefined;
          }}
        </Form.Item>

        <Form.Item label="状态" name="state" valuePropName="checked">
          <Switch checkedChildren="开启" unCheckedChildren="关闭" />
        </Form.Item>

        <Form.Item {...tailLayout}>
          <Space>
            <Button type="primary" htmlType="submit" loading={updateLoading}>
              保存
            </Button>
            <Button loading={updateLoading} onClick={() => history.goBack()}>
              返回
            </Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
};

export default ExternalStorage;
