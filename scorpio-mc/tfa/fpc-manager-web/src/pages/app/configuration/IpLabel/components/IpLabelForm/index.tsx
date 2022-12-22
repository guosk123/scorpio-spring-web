import { ip2number, ipv4MaskRegex, ipV4Regex, ipV6Regex, jumpSamePage } from '@/utils/utils';
import { Button, Form, Input, Select } from 'antd';
import TextArea from 'antd/lib/input/TextArea';
import { useEffect } from 'react';
import { history } from 'umi';
import { createIpLabel, updateIpLabel } from '../../service';
import type { IIpLabel } from '../../typings';
import { IpLabelCategoryText } from '../../typings';

const IP_MAX_NUMBER = 50;

const formLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 16 },
};

const IpLabelForm = ({ detail }: { detail?: IIpLabel }) => {
  const [form] = Form.useForm<Omit<IIpLabel, 'id'>>();

  useEffect(() => {
    if (detail) {
      form.setFieldsValue({ ...detail, ipAddress: detail.ipAddress.split(',').join('\n') });
    }
  }, [detail, form]);

  const ipValidator = async (_: any, value: string) => {
    if (value) {
      const passIpArr: string[] = []; // 已经检查通过的IP
      const valueArr = value.split('\n');

      if (Array.isArray(valueArr)) {
        if (valueArr.length > 50) {
          throw new Error(`最多支持${IP_MAX_NUMBER}个`);
        }

        valueArr.forEach((item, index) => {
          const lineText = `第${index + 1}行[${item}]: `;
          if (!item) {
            throw new Error(`${lineText}不能为空`);
          }

          // IP网段
          if (item.indexOf('/') > -1) {
            const [ip, mask] = item.split('/');
            if (!ipV4Regex.test(ip) && !ipV6Regex.test(ip)) {
              throw new Error(`${lineText}请输入正确的IP/IP段`);
            }

            if (
              ipV4Regex.test(ip) &&
              (!mask ||
                isNaN(parseInt(mask, 10)) ||
                parseInt(mask, 10) <= 0 ||
                parseInt(mask, 10) > 32) &&
              !ipv4MaskRegex.test(mask)
            ) {
              throw new Error(`${lineText}请输入正确的IP v4网段。例，192.168.1.2/24`);
            }

            if (
              ipV6Regex.test(ip) &&
              (!mask ||
                isNaN(parseInt(mask, 10)) ||
                parseInt(mask, 10) <= 0 ||
                parseInt(mask, 10) > 128)
            ) {
              throw new Error(`${lineText}请输入正确的IP v6网段。例，2001:250:6EFA::/48`);
            }
          }

          // IP组
          else if (item.indexOf('-') > -1) {
            const ips = item.split('-');
            if (ips.length !== 2) {
              throw new Error(`${lineText}请输入正确的IP地址段。例，192.168.1.1-192.168.1.50`);
            }

            const [ip1, ip2] = ips;

            // 2个ipV4
            if (!ipV4Regex.test(ip1) && !ipV4Regex.test(ip2)) {
              throw new Error(`${lineText}请输入正确的IP地址段。例，192.168.1.1-192.168.1.50`);
            }
            // 2个都是ipV4的校验下大小关系
            if (ipV4Regex.test(ip1) && ipV4Regex.test(ip2)) {
              // 校验前后2个ip的大小关系
              const ip1Number = ip2number(ip1);
              const ip2Number = ip2number(ip2);

              // 起止地址是否符合大小要求
              if (ip1Number >= ip2Number) {
                throw new Error(`${lineText}截止IP必须大于开始IP`);
              }
            } else if (!ipV6Regex.test(ip1) && !ipV6Regex.test(ip2)) {
              // ip v6
              throw new Error(`${lineText}请输入正确的IP地址段。例，192.168.1.1-192.168.1.50`);
            }
          } else if (!ipV4Regex.test(item) && !ipV6Regex.test(item)) {
            throw new Error(`${lineText}请输入正确的IP/IP段`);
          }

          // 是否重复了
          if (passIpArr.indexOf(item) !== -1) {
            throw new Error(`${lineText}已重复`);
          }
          passIpArr.push(item);
          return;
        });
      }
    }
    return Promise.resolve();
  };

  const handleFinish = (values: Omit<IIpLabel, 'id'>) => {
    const finalValues: Omit<IIpLabel, 'id'> = { ...values };
    finalValues.ipAddress = finalValues.ipAddress.split('\n').join(',');

    if (detail) {
      updateIpLabel({ id: detail.id, ...finalValues }).then((res) => {
        if (res.success) {
          history.goBack();
        }
      });
    } else {
      createIpLabel(finalValues).then((res) => {
        if (res.success) {
          history.goBack();
        }
      });
    }
  };

  return (
    <Form form={form} {...formLayout} onFinish={handleFinish}>
      <Form.Item name="name" label="名称" rules={[{ required: true }]}>
        <Input />
      </Form.Item>
      <Form.Item name={'category'} label={'分类'} rules={[{ required: true }]}>
        <Select
          options={Object.keys(IpLabelCategoryText).map((key) => {
            return {
              value: key,
              label: IpLabelCategoryText[key],
            };
          })}
        />
      </Form.Item>
      <Form.Item
        name={'ipAddress'}
        label="IP/IP网段"
        validateTrigger={['onChange', 'onBlur']}
        extra={
          <ul style={{ paddingLeft: 20, listStyle: 'circle' }}>
            <li>每行输入一种IP地址，最多支持{IP_MAX_NUMBER}个；</li>
            <li>可以输入【A.B.C.D】格式的IP地址；</li>
            <li>或输入【A.B.C.D/掩码长度】格式的IP网段；</li>
            <li>或输入【A.B.C.D-E.F.G.H】格式的IP组，请确保 E.F.G.H &gt;= A.B.C.D。</li>
          </ul>
        }
        rules={[
          {
            required: true,
            message: '请输入IP/IP网段',
          },
          {
            validator: ipValidator,
          },
        ]}
      >
        <Input.TextArea rows={5} placeholder="请输入IP/IP网段" />
      </Form.Item>
      <Form.Item name={'description'} label="描述信息" initialValue={''}>
        <TextArea rows={4} placeholder="请输入描述信息" />
      </Form.Item>
      <Form.Item wrapperCol={{ span: 12, offset: 4 }}>
        <Button style={{ marginRight: 10 }} type="primary" htmlType="submit">
          保存
        </Button>
        <Button
          onClick={() => {
            jumpSamePage('/configuration/objects/ip-label');
          }}
        >
          返回
        </Button>
      </Form.Item>
    </Form>
  );
};

export default IpLabelForm;
