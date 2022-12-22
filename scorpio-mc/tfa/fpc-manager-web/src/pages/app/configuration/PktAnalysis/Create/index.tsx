import { Button, Form, Input, message, Modal, Upload } from 'antd';
import { useMemo, useState } from 'react';
import { history } from 'umi';
import { createPktAnalysisPlugin } from '../services';
import { getCookie } from '@/utils/frame/cookie';
import { UploadOutlined } from '@ant-design/icons';
import { UploadFile } from 'antd/lib/upload/interface';

export default () => {
  const [fileList, setFileList] = useState<UploadFile<unknown>[]>([]);

  const [form] = Form.useForm();

  const validateProtocol = (rule: any, value: string, callback: any) => {
    const regExp = /^[A-Za-z_]*$/;
    if (regExp.exec(value)) {
      callback();
    }
    callback('协议必须由大小写字母和下划线组成');
  };

  const validateFile = (rule: any, value: string, callback: any) => {
    fileList.length > 0 ? callback() : callback('请选择文件');
  };

  const handleSubmit = () => {
    Modal.confirm({
      title: '确定保存吗?',
      cancelText: '取消',
      okText: '确定',
      onOk: async () => {
        if (fileList.length > 0) {
          const { protocol, description } = form.getFieldsValue();
          const formData = new FormData();
          formData.append('file', fileList[0] as any);
          formData.append('fileName', fileList[0].name);
          formData.append('description', description || '');
          formData.append('protocol', protocol);
          const { success } = await createPktAnalysisPlugin(formData);
          if (success) {
            message.success('上传成功!');
            history.push('/configuration/objects/pktanalysis/plugin');
            return;
          }
          message.error('上传失败!');
        }
      },
    });
  };

  const uploadProps = useMemo(() => {
    return {
      name: 'file',
      withCredentials: true,
      headers: {
        'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
      },
      accept: '.lua',
      beforeUpload: (file: UploadFile<unknown>) => {
        setFileList([file]);
        return false;
      },
      onRemove: () => {
        setFileList([]);
      },
      fileList,
      maxCount: 1,
    };
  }, [fileList]);

  return (
    <>
      <Form
        style={{
          marginTop: '20px',
        }}
        form={form}
        labelCol={{ span: 4 }}
        wrapperCol={{ span: 16 }}
        onFinish={handleSubmit}
        autoComplete="off"
      >
        <Form.Item
          wrapperCol={{ span: 8, offset: 4 }}
          name="file"
          rules={[{ validator: validateFile }]}
        >
          <Upload {...uploadProps}>
            <Button icon={<UploadOutlined />}>上传Lua文件</Button>
          </Upload>
        </Form.Item>
        <Form.Item
          label="协议"
          name="protocol"
          rules={[
            { required: true, message: '请输入协议' },
            { max: 64, message: '协议长度超出范围' },
            { validator: validateProtocol },
          ]}
        >
          <Input placeholder="协议必须由大小写字母和下划线组成" />
        </Form.Item>
        <Form.Item
          label="描述信息"
          name="description"
          rules={[
            {
              max: 512,
              message: '描述信息超出范围',
            },
          ]}
        >
          <Input.TextArea rows={5} placeholder="描述信息最多输入512个字符" />
        </Form.Item>
        <Form.Item wrapperCol={{ offset: 4 }}>
          <Button type="primary" htmlType="submit" style={{ marginRight: '10px' }}>
            保存
          </Button>
          <Button
            htmlType="button"
            onClick={() => {
              history.push('/configuration/objects/pktanalysis/plugin');
            }}
          >
            返回
          </Button>
        </Form.Item>
      </Form>
    </>
  );
};
