import config from '@/common/applicationConfig';
import { QuestionCircleOutlined, SettingOutlined, UploadOutlined } from '@ant-design/icons';
import { Button, Divider, Form, Input, message, Modal, Spin, Tooltip, Upload } from 'antd';
import type { UploadFile } from 'antd/es/upload/interface';
import { useEffect, useState } from 'react';
import type { IProductInfo } from 'umi';
import { queryProductInfo, updateProductInfo } from './service';

const formItemLayout = {
  labelCol: { span: 6 },
  wrapperCol: { span: 14 },
};

const { PRODUCT_LOGO } = config;

const FormItem = Form.Item;

export default function Product() {
  const [loadings, setLoadings] = useState(true);
  const [logoFile, setLogoFile] = useState<UploadFile[]>([]);

  const [productInfos, setProductInfos] = useState<IProductInfo>({
    corporation: '',
    description: '',
    name: '',
    series: '',
    version: '',
    logoBase64: PRODUCT_LOGO,
  });

  useEffect(() => {
    queryProductInfo().then((res) => {
      const { result, success } = res;
      if (success) {
        setProductInfos(result);
      }
      setLoadings(false);
    });
  }, []);

  // 表单提交
  const onSubmit = (values: any) => {
    let logoBase64: string | ArrayBuffer | null | undefined = undefined;
    if (logoFile.length) {
      const file = logoFile[0];
      const reader = new FileReader();
      reader.readAsDataURL(file as any);
      reader.onload = function (ev) {
        if (!ev) {
          return;
        }
        const dataURL = ev.target?.result;
        logoBase64 = dataURL;
      };
    } else {
      logoBase64 = productInfos?.logoBase64;
    }

    Modal.confirm({
      width: 500,
      title: '确定保存吗?',
      icon: <SettingOutlined />,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        updateProductInfo({ ...values, logoBase64 }).then((res) => {
          const { success } = res;
          if (success) {
            window.location.reload();
          }
        });
      },
    });
  };

  const initProductValue = productInfos;
  if (loadings) {
    return <Spin />;
  }
  return (
    <Form initialValues={initProductValue || {}} onFinish={onSubmit} {...formItemLayout}>
      <FormItem
        label={
          <div>
            <span>产品图标</span>
            <Tooltip
              title={'建议图片比例为1:1或1:3，不合适的图片比例可能图标会畸变，图片大小应小于0.5M'}
            >
              <QuestionCircleOutlined style={{ margin: 2 }} />
            </Tooltip>
          </div>
        }
      >
        <img src={productInfos?.logoBase64 || '/custom-static/current/logo.png'} height={30} />
        <Divider type="vertical" />
        <Upload
          beforeUpload={(file) => {
            const isLt2M = file.size / 1024 / 512 < 1;
            if (!isLt2M) {
              message.error('图片大小应小于0.5M!');
              return;
            }
            setLogoFile([file]);
          }}
          fileList={logoFile}
          accept={'png'}
        >
          <Button icon={<UploadOutlined />}>更改</Button>
        </Upload>
      </FormItem>
      <FormItem label="产品名称" name={'name'}>
        <Input />
      </FormItem>
      <FormItem label="产品型号" name={'series'}>
        <Input />
      </FormItem>
      <FormItem label="产品版本" name={'version'}>
        <Input />
      </FormItem>
      <FormItem label="基础功能" name={'description'}>
        <Input />
      </FormItem>
      <FormItem label="产品版权信息" name={'corporation'}>
        <Input />
      </FormItem>
      <FormItem wrapperCol={{ span: 12, offset: 6 }}>
        <Button type="primary" htmlType="submit" style={{ marginRight: '10px' }}>
          保存
        </Button>
      </FormItem>
    </Form>
  );
}
