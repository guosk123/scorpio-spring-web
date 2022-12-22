import { Button, Form, Input, TreeSelect } from 'antd';
import { useCallback, useEffect } from 'react';
import { ipV4Regex, ipV6Regex, snakeCase } from '@/utils/utils';
import type { ConnectState } from '@/models/connect';
import type { INetworkStatData } from '../../../typings';
import { ESortDirection } from '../../../typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { Dispatch } from 'umi';
import { connect } from 'umi';
import type { ISearchBoxInfo } from '../typings';

const FormItem = Form.Item;
const InputGroup = Input.Group;

interface Props {
  onSubmit?: any;
  initSearchInfo: ISearchBoxInfo;
  dispatch: Dispatch;
  queryLoading: boolean | undefined;
  allNetworkStatData: INetworkStatData[];
  globalSelectedTime: Required<IGlobalTime>;
}

function SearchBox(props: Props) {
  const {
    onSubmit,
    initSearchInfo,
    dispatch,
    queryLoading,
    allNetworkStatData,
    globalSelectedTime,
  } = props;
  const [form] = Form.useForm();

  const queryData = useCallback(() => {
    dispatch({
      type: 'npmdModel/queryAllNetworkStat',
      payload: {
        sortProperty: snakeCase('totalBytes'),
        sortDirection: ESortDirection.DESC,
        startTime: globalSelectedTime.startTime,
        endTime: globalSelectedTime.endTime,
        interval: globalSelectedTime.interval,
      },
    });
  }, [
    dispatch,
    globalSelectedTime.endTime,
    globalSelectedTime.interval,
    globalSelectedTime.startTime,
  ]);

  useEffect(() => {
    queryData();
  }, [queryData]);

  // console.log(allNetworkStatData, 'allNetworkStatData');
  const buildTreeOpt = (arr: any) => {
    return arr.map((item: any) => {
      const selectedItem = { title: item.networkName, value: item.networkId, key: item.networkId };
      if (item.children) {
        return {
          ...selectedItem,
          children: buildTreeOpt(item?.children),
        };
      }
      return selectedItem;
    });
  };
  const networkDataOpt = buildTreeOpt(allNetworkStatData);
  // console.log(networkDataOpt, 'networkDataOpt');

  // const renderTreeNodes = (data: any) =>
  //   data.map((item: any) => {
  //     if (item.children) {
  //       item.disabled = true;
  //       return (
  //         <TreeNode
  //           key={item.networkId}
  //           title={item.networkName}
  //           value={item.networkId}
  //           disabled={item.disabled}
  //         >
  //           {renderTreeNodes(item.children)}
  //         </TreeNode>
  //       );
  //     }
  //     return <TreeNode key={item.networkId} title={item.networkName} value={item.networkId} />;
  //   });

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
    // else if (value.indexOf('-') > -1) {
    //   const ips = value.split('-');
    //   if (ips.length !== 2) {
    //     return Promise.reject('请输入正确的IP地址段。例，192.168.1.1-192.168.1.50');
    //   }
    //   const [ip1, ip2] = ips;
    //   // 2个ipV4
    //   if (!ipV4Regex.test(ip1) && !ipV4Regex.test(ip2)) {
    //     return Promise.reject('请输入正确的IP地址段。例，192.168.1.1-192.168.1.50');
    //   }
    //   // 2个都是ipV4的校验下大小关系
    //   if (ipV4Regex.test(ip1) && ipV4Regex.test(ip2)) {
    //     // 校验前后2个ip的大小关系
    //     const ip1Number = ip2number(ip1);
    //     const ip2Number = ip2number(ip2);

    //     // 起止地址是否符合大小要求
    //     if (ip1Number >= ip2Number) {
    //       return Promise.reject('截止IP必须大于开始IP');
    //     }
    //   } else if (!ipV6Regex.test(ip1) && !ipV6Regex.test(ip2)) {
    //     // ip v6
    //     return Promise.reject('请输入正确的IP地址段。例，192.168.1.1-192.168.1.50');
    //   }
    // }
    else if (!ipV4Regex.test(value)) {
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
        initialValues={{
          networkIds: initSearchInfo.networkIds || 'ALL',
          IpAddress: initSearchInfo.IpAddress,
        }}
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
              treeData={[{ label: '所有网络', value: 'ALL', key: 'ALL', children: networkDataOpt }]}
              // treeCheckable={true}
              loading={queryLoading}
              placeholder={'请选择网络'}
              showCheckedStrategy={'SHOW_PARENT'}
              style={{ width: 360 }}
            >
              {/* {renderTreeNodes(allNetworkStatData)} */}
            </TreeSelect>
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
export default connect(
  ({
    loading: { effects },
    npmdModel: { allNetworkStatData },
    appModel: { globalSelectedTime },
  }: ConnectState) => ({
    queryLoading: effects['npmdModel/queryAllNetworkStat'],
    allNetworkStatData,
    globalSelectedTime,
  }),
)(SearchBox);
