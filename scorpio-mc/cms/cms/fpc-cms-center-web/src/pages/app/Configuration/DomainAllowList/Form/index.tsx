import { domainReg } from '@/utils/utils';
import { Button, Form, Input, message, Space } from 'antd';
import { useEffect, useState } from 'react';
import { history } from 'umi';
import { createDomainAllowListItem, updateDomainAllowListItem } from '../service';
import type { DomainAllowListItem } from '../typings';

const goBack = () => {
  history.push('/configuration/objects/domain-allow-list');
};

type DataType = Omit<DomainAllowListItem, 'id'>;

const domain_suffix_reg = /^\*(\.[a-zA-Z0-9][_-a-zA-Z0-9]{0,62})+$/;

const validateDomain = (domain: string) => {
  return domainReg.test(domain) || domain_suffix_reg.test(domain);
};

interface Props {
  detail?: DomainAllowListItem;
}

const DomainAllowListForm = (props: Props) => {
  const { detail } = props;
  const [form] = Form.useForm<DataType>();

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (detail) {
      form.setFieldsValue({
        ...detail,
        domain: detail.domain?.replaceAll(',', '\n'),
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleDomainValidate = (value: string) => {
    const domains = value?.split('\n');
    for (const domain of domains) {
      if (!validateDomain(domain)) {
        return false;
      }
    }

    return true;
  };

  const handleAfterSave = (success: boolean) => {
    setLoading(false);
    if (success) {
      message.success('创建成功');
      goBack();
    } else {
      message.error('新建失败');
    }
  };

  const handleFinish = (values: DataType) => {
    const finnalValues = {
      ...values,
      domain: values.domain?.split('\n').join(','),
    };

    setLoading(true);

    if (detail) {
      updateDomainAllowListItem({ ...finnalValues, id: detail.id }).then((res) => {
        const { success } = res;
        handleAfterSave(success);
      });
    } else {
      createDomainAllowListItem(finnalValues).then((res) => {
        const { success } = res;
        handleAfterSave(success);
      });
    }
  };

  return (
    <Form form={form} onFinish={handleFinish} wrapperCol={{ span: 14 }} labelCol={{ span: 4 }}>
      <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入名称' }]}>
        <Input />
      </Form.Item>
      {/* 根据开会讨论结果，最多配置100条域名 */}
      {/* 域名可以是精确域名或者后缀匹配：www.baidu,com、*.baidu.com */}
      <Form.Item
        name="domain"
        label="域名"
        validateTrigger={['onChange', 'onBlur']}
        rules={[
          { required: true, message: '请至少填写一个域名' },
          {
            validator: (r, value) => {
              if (handleDomainValidate(value)) {
                return Promise.resolve();
              }
              return Promise.reject('请填写合规域名');
            },
          },
        ]}
        extra={
          <ul style={{ paddingLeft: 20, listStyle: 'none' }}>
            <li>每行输入一个域名，最多支持{100}个；</li>
            <li>可以输入www.baidu.com格式的域名；</li>
            <li>或输入*.baidu.com 格式的后缀匹配域名；</li>
          </ul>
        }
      >
        <Input.TextArea rows={5} placeholder="请输入域名" />
      </Form.Item>
      <Form.Item name="description" label="描述">
        <Input.TextArea rows={4} placeholder="请输入描述信息" />
      </Form.Item>
      <Form.Item wrapperCol={{ offset: 4 }}>
        <Space>
          <Button type="primary" htmlType="submit" loading={loading}>
            保存
          </Button>
          <Button onClick={goBack}>返回</Button>
        </Space>
      </Form.Item>
    </Form>
  );
};

export default DomainAllowListForm;
