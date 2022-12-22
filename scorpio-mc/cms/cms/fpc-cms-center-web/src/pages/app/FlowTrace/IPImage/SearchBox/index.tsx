import { Button, Form, Input, message, TreeSelect } from 'antd';
import { useEffect, useState } from 'react';
import { ipV4Regex, ipV6Regex, ip2number } from '@/utils/utils';
import { connect } from 'umi';
import { queryNetWorkTree } from '@/pages/app/Network/service';
import type { INetworkTreeItem } from '@/pages/app/Network/typing';

const FormItem = Form.Item;
const InputGroup = Input.Group;

interface Props {
  onSubmit?: any;
}

export interface ISearchBoxInfo {
  networkIds: string;
  IpAddress: string;
}

function SearchBox(props: Props) {
  const { onSubmit } = props;
  const [form] = Form.useForm();

  const [networkDataSet, setNetworkDataSet] = useState<INetworkTreeItem[]>([]);
  const [networkTreeLoading, setNetworkTreeLoading] = useState(true);

  useEffect(() => {
    queryNetWorkTree()
      .then((result) => {
        setNetworkDataSet(
          result.map((item) => ({
            ...item,
            value: `${item.value}^${item.type}`,
          })),
        );
        setNetworkTreeLoading(false);
      })
      .catch((err) => {
        message.error(err);
        setNetworkTreeLoading(false);
      });
  }, []);

  const validateIpAddress = (obj: any, value: any) => {
    if (!value) {
      return Promise.resolve();
    }
    if (value.indexOf('/') > -1) {
      const ips = value.split('/');
      // 校验第一个 ip
      if (!ipV4Regex.test(ips[0]) && !ipV6Regex.test(ips[0])) {
        return Promise.reject('IP地址格式不正确，请重新输入!');
      }
      // 校验子网掩码
      // eslint-disable-next-line no-restricted-globals
      if (!ips[1] || isNaN(ips[1])) {
        return Promise.reject('请输入网络号!');
      }
      // 这里把 0 排除掉
      if ((ips[1] <= 0 || ips[1] > 32) && ipV4Regex.test(ips[0])) {
        return Promise.reject('子网掩码范围是(0,32]。例，192.168.1.2/24');
      }
      if ((ips[1] <= 0 || ips[1] > 128) && ipV6Regex.test(ips[0])) {
        return Promise.reject('子网掩码范围是(0,128]');
      }
    }
    // IP组
    else if (value.indexOf('-') > -1) {
      const ips = value.split('-');
      if (ips.length !== 2) {
        return Promise.reject('请输入正确的IP地址段。例，192.168.1.1-192.168.1.50');
      }
      const [ip1, ip2] = ips;
      // 2个ipV4
      if (!ipV4Regex.test(ip1) && !ipV4Regex.test(ip2)) {
        return Promise.reject('请输入正确的IP地址段。例，192.168.1.1-192.168.1.50');
      }
      // 2个都是ipV4的校验下大小关系
      if (ipV4Regex.test(ip1) && ipV4Regex.test(ip2)) {
        // 校验前后2个ip的大小关系
        const ip1Number = ip2number(ip1);
        const ip2Number = ip2number(ip2);

        // 起止地址是否符合大小要求
        if (ip1Number >= ip2Number) {
          return Promise.reject('截止IP必须大于开始IP');
        }
      } else if (!ipV6Regex.test(ip1) && !ipV6Regex.test(ip2)) {
        // ip v6
        return Promise.reject('请输入正确的IP地址段。例，192.168.1.1-192.168.1.50');
      }
    } else if (!ipV4Regex.test(value)) {
      if (!ipV6Regex.test(value)) {
        return Promise.reject('IP地址格式不正确，请重新输入!');
      }
    }
    return Promise.resolve();
  };

  const submitForm = (e: ISearchBoxInfo) => {
    const submitInfo = {
      ...e,
    };
    onSubmit(submitInfo);
  };

  return (
    <div>
      <Form
        layout="inline"
        initialValues={{}}
        name="widget"
        form={form}
        style={{ width: 900 }}
        // onValuesChange={(changedValues, allValues) => {}}
        onFinish={submitForm}
      >
        <InputGroup compact>
          <FormItem name="networkIds" rules={[{ required: true, message: '请选择网络' }]}>
            <TreeSelect
              allowClear
              treeDefaultExpandAll
              treeData={networkDataSet}
              // treeCheckable={true}
              loading={networkTreeLoading}
              placeholder={'请选择网络'}
              showCheckedStrategy={'SHOW_PARENT'}
              style={{ width: 360 }}
            />
          </FormItem>
          <FormItem
            name="IpAddress"
            rules={[{ required: true, message: '请输入IP地址' }, { validator: validateIpAddress }]}
            style={{ width: 360 }}
          >
            <Input placeholder="请输入IP地址" />
          </FormItem>
          <FormItem noStyle>
            <Button type="primary" style={{ width: 80 }} htmlType="submit">
              搜索
            </Button>
          </FormItem>
        </InputGroup>
      </Form>
    </div>
  );
}
export default connect()(SearchBox);
