/* eslint-disable no-param-reassign */
import {
  BOOL_NO,
  BOOL_YES,
  DEVICE_NETIF_CATEGORY_RECEIVE,
  DEVICE_NETIF_STATE_UP,
  ONE_KILO_1000,
} from '@/common/dict';
import type { ConnectState } from '@/models/connect';
import type { INetif } from '@/pages/app/configuration/DeviceNetif/typings';
import { DEFAULT_POLICY_ID as DEFAULT_INGEST_POLICY_ID } from '@/pages/app/configuration/IngestPolicy/components/IngestPolicyForm';
import type { IIngestPolicy } from '@/pages/app/configuration/IngestPolicy/typings';
import type { IpAddressGroup } from '@/pages/app/configuration/IpAddressGroup/typings';
import {
  createConfirmModal,
  enumObj2List,
  ip2number,
  ipV4Regex,
  ipV6Regex,
  updateConfirmModal,
} from '@/utils/utils';
import { ExclamationCircleOutlined, LoadingOutlined, RedoOutlined } from '@ant-design/icons';
import {
  Alert,
  Button,
  Card,
  Checkbox,
  Col,
  Divider,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  Space,
  Spin,
  Table,
  Tooltip,
} from 'antd';
import type { CheckboxChangeEvent } from 'antd/lib/checkbox';
import type { ColumnProps } from 'antd/lib/table';
import { Address6 } from 'ip-address';
import React, { useCallback, useEffect, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, history } from 'umi';
import {
  DEFAULT_POLICY_ID as DEFAULT_APPLICATION_POLICY_ID,
  DEFAULT_POLICY_ID,
} from '../../../ApplicationPolicy/components/PolicyForm';
import type { IApplicationPolicy } from '../../../ApplicationPolicy/typings';
import ConnectCmsState from '../../../components/ConnectCmsState';
import { querySendPolicyStateOn } from '../../../SendPolicy/service';
import type { INetwork, INetworkFormData, INetworkNetif, TNetifDirection } from '../../typings';
import styles from './index.less';

const FormItem = Form.Item;
const { TextArea } = Input;
const { Option } = Select;

const MAX_IP_COUNT = 100;

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 4 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 20 },
    md: { span: 18 },
  },
};

const submitFormLayout = {
  wrapperCol: {
    xs: { span: 24, offset: 0 },
    sm: { span: 12, offset: 4 },
  },
};

interface NetworkFormProps {
  submitting: boolean | undefined;
  dispatch: Dispatch;
  detail?: INetwork;
  allNetifs: INetif[];
  usedNetifs: INetworkNetif[];
  allIngestPolicy: IIngestPolicy[];
  allApplicationPolicy: IApplicationPolicy[];
  allIpAddressGroupList: IpAddressGroup[];

  queryNetifLoading: boolean | undefined;
  queryIpAddressLoading: boolean | undefined;
}

/**
 * 特殊的流
 */
const FLOWLOG_SPECIAL_STATUS = 'syn_sent';

/**
 * 额外的统计
 */
const ANALYSIS_EXTRA_FLOW = 'interframe,preamble';

/**
 * 双向流量
 */
const NETIF_CATEGORY_HYBRID = '1';
/**
 * 单向流量
 */
const NETIF_CATEGORY_SINGLE = '0';
/**
 * 接口流量方向类型
 */
export const netifCategoryEnum = {
  [NETIF_CATEGORY_SINGLE]: '单向流量',
  [NETIF_CATEGORY_HYBRID]: '双向流量',
};

/**
 * 流量方向
 */
const DIRECTION_UPSTREAM = 'upstream';
const DIRECTION_DOWNSTREAM = 'downstream';
export const singleDirectionEnum = {
  [DIRECTION_UPSTREAM]: '上行',
  [DIRECTION_DOWNSTREAM]: '下行',
};

/**
 * 混合
 */
const DIRECTION_HYBRID = 'hybrid';

export const hybridDirectionEnum = {
  [DIRECTION_HYBRID]: '混合',
};

export const directionEnum = {
  ...singleDirectionEnum,
  ...hybridDirectionEnum,
};

type INetifFormValue = Record<
  string,
  {
    selected: boolean;
    direction: TNetifDirection;
    specification: number;
  }
>;

const NetworkForm: React.FC<NetworkFormProps> = (props) => {
  const {
    detail = {} as INetwork,
    allNetifs = [],
    usedNetifs = [],
    allIpAddressGroupList = [],
    allIngestPolicy = [],
    allApplicationPolicy = [],
    queryNetifLoading,
    queryIpAddressLoading,
    submitting,
    dispatch,
  } = props;
  const [form] = Form.useForm();
  const [sendPolicies, setSendPolicies] = useState<any[]>([]);
  const [policyLoading, setPolicyLoading] = useState<boolean>(false);
  const [cmsConnectFlag, setCmsConnectFlag] = useState(false);
  // 被选中的业务接口
  const [selectedNetifs, setSelectNetifs] = useState<string[]>(
    Array.isArray(detail.netif) ? detail.netif.map((item) => item.netifName) : [],
  );

  const fetchPolicies = async () => {
    setPolicyLoading(true);
    const { success, result } = await querySendPolicyStateOn();
    if (success) {
      setSendPolicies(result);
    }
    setPolicyLoading(false);
  };

  useEffect(() => {
    fetchPolicies();
  }, []);

  const queryUsedNetifs = useCallback(() => {
    dispatch({
      type: 'networkModel/queryUsedNetifs',
    });
  }, [dispatch]);

  useEffect(() => {
    if (dispatch) {
      queryUsedNetifs();

      dispatch({
        type: 'deviceNetifModel/queryDeviceNetifs',
      });
      dispatch({
        type: 'ipAddressGroupModel/queryAllIpAddressGroup',
      });
      dispatch({
        type: 'ingestPolicyModel/queryAllIngestPolicies',
      });
      dispatch({
        type: 'applicationPolicyModel/queryAllApplicationPolicies',
      });
    }
  }, [dispatch, queryUsedNetifs]);

  const handleGoBack = () => {
    history.goBack();
  };

  const handleReset = () => {
    form.resetFields();
    // 刷新接口
    setSelectNetifs([]);
    queryUsedNetifs();
  };

  const handleCreate = (values: INetworkFormData) => {
    createConfirmModal({
      dispatchType: 'networkModel/createNetwork',
      values,
      onOk: handleGoBack,
      onCancel: handleReset,
      dispatch,
    });
  };

  const handleUpdate = (values: INetworkFormData) => {
    updateConfirmModal({
      dispatchType: 'networkModel/updateNetwork',
      values,
      onOk: handleGoBack,
      dispatch,
      onCancel: () => {},
    });
  };

  const onFinish = (values: Record<string, any>) => {
    const {
      insideIpAddress,
      flowlogDefaultAction,
      metadataDefaultAction,
      flowlogExceptStatus,
      flowlogExceptStatistics,
      sessionVlanAction,
      filterRuleIds,
      sendPolicyIds,
      ...restValue
    } = values;
    const data = {
      ...restValue,
      insideIpAddress: insideIpAddress ? insideIpAddress.split('\n').join(',') : '',
      sendPolicyIds: sendPolicyIds.join(','),
      extraSettings: JSON.stringify({
        flowlogDefaultAction: flowlogDefaultAction ? BOOL_YES : BOOL_NO,
        metadataDefaultAction: metadataDefaultAction ? BOOL_YES : BOOL_NO,
        flowlogExceptStatus: flowlogExceptStatus ? FLOWLOG_SPECIAL_STATUS : '',
        flowlogExceptStatistics: flowlogExceptStatistics ? ANALYSIS_EXTRA_FLOW : '',
        sessionVlanAction: sessionVlanAction ? BOOL_YES : BOOL_NO,
      }),
      filterRuleIds: filterRuleIds?.join(',') || '',
    } as INetworkFormData;
    // 过滤出被选中的接口
    const netifList: INetwork['netif'] = [];
    const netifObj: INetifFormValue = values.netifMap;
    Object.keys(netifObj).forEach((key) => {
      const item = netifObj[key];
      if (item.selected && item.specification && item.direction) {
        netifList.push({
          netifName: key,
          specification: item.specification,
          direction: item.direction,
        });
      }
    });
    // @ts-ignore
    delete data.netifMap;
    data.netif = JSON.stringify(netifList);

    if (data.id) {
      handleUpdate(data);
    } else {
      handleCreate(data);
    }
  };

  const onFinishFailed = (errorInfo: any) => {
    // eslint-disable-next-line no-console
    console.log('Failed:', errorInfo);
  };

  const handleNetifChecked = (e: CheckboxChangeEvent) => {
    const { value, checked } = e.target;
    if (checked) {
      setSelectNetifs([...selectedNetifs, value]);
    } else {
      setSelectNetifs(selectedNetifs.filter((item) => item !== value));
    }
  };

  const quickSelectIpAddress = (ipAddress: string) => {
    // 往填充到内网配置里面
    if (ipAddress) {
      const oldIpAddress = form.getFieldValue(['insideIpAddress']);
      const splitIp = ipAddress.replace(/,/g, '\n');
      form.setFieldsValue({
        insideIpAddress: oldIpAddress ? `${oldIpAddress}\n${splitIp}` : splitIp,
      });
    }
  };

  const checkIpAddress = async (rule: any, value: string) => {
    if (!value) {
      return Promise.resolve();
    }

    const passIpArr: string[] = []; // 已经检查通过的IP
    const valueArr = value.split('\n');

    for (let index = 0; index < valueArr.length; index += 1) {
      const item = valueArr[index];
      const lineText = `第${index + 1}行[${item}]: `;
      if (!item) {
        return Promise.reject(`${lineText}不能为空`);
      }

      // IP网段
      if (item.indexOf('/') > -1) {
        const [ip, mask] = item.split('/');

        const maskNum = +mask;

        if (!ipV4Regex.test(ip) && !ipV6Regex.test(ip)) {
          return Promise.reject(`${lineText}请输入正确的IP/IP段`);
        }

        if (ipV4Regex.test(ip) && (!mask || isNaN(maskNum) || maskNum <= 0 || maskNum > 32)) {
          return Promise.reject(`${lineText}请输入正确的IPv4网段。例，192.168.1.2/24`);
        }

        if (ipV6Regex.test(ip) && (!mask || isNaN(maskNum) || maskNum <= 0 || maskNum > 128)) {
          return Promise.reject(`${lineText}请输入正确的IPv6网段。例，2001:250:6EFA::/48`);
        }
      }

      // IP组
      else if (item.indexOf('-') > -1) {
        const ips = item.split('-');
        if (ips.length !== 2) {
          return Promise.reject(`${lineText}请输入正确的IP地址段。例，192.168.1.1-192.168.1.50`);
        }

        const [ip1, ip2] = ips;

        // 2个IPv4
        if (ipV4Regex.test(ip1) && ipV4Regex.test(ip2)) {
          const ip1Number = ip2number(ip1);
          const ip2Number = ip2number(ip2);

          // 起止地址是否符合大小要求
          if (ip1Number >= ip2Number) {
            return Promise.reject(`${lineText}IP地址段范围错误`);
          }
        }

        // 2个IPv6
        else if (ipV6Regex.test(ip1) && ipV6Regex.test(ip2)) {
          if (new Address6(ip1).bigInteger() >= new Address6(ip2).bigInteger()) {
            return Promise.reject(`${lineText}IP地址段范围错误`);
          }
        } else {
          return Promise.reject(`${lineText}请输入正确的IP地址段`);
        }
      } else if (!ipV4Regex.test(item) && !ipV6Regex.test(item)) {
        return Promise.reject(`${lineText}请输入正确的IP/IP段`);
      }

      // 是否重复了
      if (passIpArr.indexOf(item) !== -1) {
        return Promise.reject(`${lineText}已重复`);
      }
      passIpArr.push(item);
    }

    if (passIpArr.length > MAX_IP_COUNT) {
      return Promise.reject(`最多支持${MAX_IP_COUNT}个`);
    }

    return Promise.resolve();
  };

  const netifColumns: ColumnProps<INetif>[] = [
    {
      title: '#',
      dataIndex: 'checked',
      key: 'checked',
      align: 'center',
      width: 80,
      render: (name, row) => {
        // 判断
        let disabled = false;
        const findResult = usedNetifs.find((item) => item.netifName === row.name);
        // 编辑的情况下，排除掉网络自身管理的接口
        if (detail.id) {
          if (findResult && findResult.networkId !== detail.id) {
            disabled = true;
          }
        } else if (findResult) {
          // 新建的情况下，被使用的接口全部禁用
          disabled = true;
        }

        if (disabled) {
          return (
            <Tooltip title={disabled ? '已被使用' : ''}>
              <ExclamationCircleOutlined />
            </Tooltip>
          );
        }
        return (
          <FormItem
            name={['netifMap', row.name, 'selected']}
            valuePropName="checked"
            initialValue={selectedNetifs.indexOf(row.name) > -1}
            noStyle
          >
            <Checkbox disabled={disabled} value={row.name} onChange={handleNetifChecked} />
          </FormItem>
        );
      },
    },
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '状态',
      dataIndex: 'state',
      key: 'state',
      render: (state) => (state === DEVICE_NETIF_STATE_UP ? 'up' : 'down'),
    },
    {
      title: '物理带宽',
      dataIndex: 'specification',
      key: 'specification',
      render: (specification) => (specification ? `${specification / ONE_KILO_1000}Gbps` : '--'),
    },
    {
      title: '总带宽（Mbps）',
      dataIndex: 'customSpecification',
      key: 'customSpecification',
      width: 200,
      render: (text, row) => {
        // 计算最大的屋里带宽（Mbps）
        const { specification } = row;
        return selectedNetifs.includes(row.name) ? (
          <FormItem
            noStyle
            validateFirst
            name={['netifMap', row.name, 'specification']}
            rules={[{ required: true, message: '请填写接口的总带宽' }]}
          >
            <InputNumber
              precision={0}
              size="small"
              min={1}
              max={specification || Infinity}
              placeholder=""
              style={{ width: '100%' }}
            />
          </FormItem>
        ) : null;
      },
    },
    {
      title: '方向',
      dataIndex: 'direction',
      key: 'direction',
      width: 200,
      render: (text: any, row: INetif) => {
        return selectedNetifs.includes(row.name) ? (
          <Form.Item
            noStyle
            shouldUpdate={(prevValues, currentValues) =>
              prevValues.netifType !== currentValues.netifType
            }
          >
            {({ getFieldValue }) => {
              let list = directionEnum as any;
              const netifCategoryValue = getFieldValue('netifType');
              const isSingle = netifCategoryValue === NETIF_CATEGORY_SINGLE;
              const isHybrid = netifCategoryValue === NETIF_CATEGORY_HYBRID;
              if (isHybrid) {
                list = hybridDirectionEnum;
              }
              if (isSingle) {
                list = singleDirectionEnum;
              }

              return (
                <FormItem
                  noStyle
                  validateFirst
                  name={['netifMap', row.name, 'direction']}
                  rules={[
                    { required: true, message: '请选择接口方向' },
                    {
                      validator: (_, value) => {
                        if (isSingle && !Object.keys(singleDirectionEnum).includes(value)) {
                          return Promise.reject('接口方向不正确');
                        }
                        if (isHybrid && !Object.keys(hybridDirectionEnum).includes(value)) {
                          return Promise.reject('接口方向不正确');
                        }

                        return Promise.resolve();
                      },
                    },
                  ]}
                >
                  <Select size="small" style={{ width: '100%' }}>
                    {enumObj2List(list).map(({ label, value }) => (
                      <Select.Option key={value} value={value}>
                        {label}
                      </Select.Option>
                    ))}
                  </Select>
                </FormItem>
              );
            }}
          </Form.Item>
        ) : null;
      },
    },
  ];

  const initNetifMap = {};
  if (Array.isArray(detail.netif)) {
    detail.netif.forEach((item) => {
      initNetifMap[item.netifName] = {
        ...item,
        selected: true,
      };
    });
  }

  const initExtraSettings = detail?.extraSettings || {};
  const filterRuleIds = detail.filterRuleIds?.split(',').filter((i) => i) || [];
  if (filterRuleIds.findIndex((r) => r === DEFAULT_POLICY_ID) < 0) {
    filterRuleIds.push(DEFAULT_POLICY_ID);
  }
  return (
    <Card bordered={false}>
       <ConnectCmsState onConnectFlag={setCmsConnectFlag} />
      <Form
        form={form}
        name="network-form"
        initialValues={{
          ...detail,
          insideIpAddress: detail.insideIpAddress ? detail.insideIpAddress.replace(/,/g, '\n') : '',
          netifMap: initNetifMap,
          ingestPolicyId: detail.ingestPolicyId || DEFAULT_INGEST_POLICY_ID,
          filterRuleIds,
          sendPolicyIds: detail?.sendPolicyIds?.split(',').filter((f) => f) || [],
          // 额外的统计配置
          flowlogDefaultAction: initExtraSettings.flowlogDefaultAction === BOOL_YES,
          metadataDefaultAction: initExtraSettings.metadataDefaultAction === BOOL_YES,
          flowlogExceptStatus: !!initExtraSettings.flowlogExceptStatus,
          flowlogExceptStatistics: !!initExtraSettings.flowlogExceptStatistics,
          sessionVlanAction: initExtraSettings.sessionVlanAction === BOOL_YES,
        }}
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
        onValuesChange={(changeValues, allValues) => {
          if (changeValues.netifType === NETIF_CATEGORY_HYBRID && allValues.netifMap) {
            Object.keys(allValues.netifMap).forEach((key) => {
              allValues.netifMap[key].direction = DIRECTION_HYBRID;
            });
          } else if (changeValues.netifType === NETIF_CATEGORY_SINGLE && allValues.netifMap) {
            Object.keys(allValues.netifMap).forEach((key) => {
              allValues.netifMap[key].direction = DIRECTION_UPSTREAM;
            });
          }
          form.setFieldsValue(allValues);
        }}
        scrollToFirstError
      >
        <FormItem {...formItemLayout} label="ID" name="id" hidden>
          <Input placeholder="网络id" />
        </FormItem>
        <FormItem
          {...formItemLayout}
          label="名称"
          name="name"
          rules={[
            {
              required: true,
              whitespace: true,
              message: '请填写网络名称',
            },
            { max: 30, message: '最多可输入30个字符' },
          ]}
        >
          <Input placeholder="填写网络名称" />
        </FormItem>
        <FormItem
          {...formItemLayout}
          label="流量方向"
          name="netifType"
          className={styles.netifCategoryItem}
          rules={[
            {
              required: true,
              message: '请选择流量方向',
            },
          ]}
          extra={
            <Alert
              type="info"
              showIcon
              message={
                <ul className={styles.desc}>
                  <li>单向流量，必须指定每个选中的接口的方向</li>
                  <li>
                    双向流量，需要配置监控的内网IP地址段。系统将根据此配置区分内外网以及上下行进行统计分析
                  </li>
                </ul>
              }
            />
          }
        >
          <Select placeholder="选择流量方向">
            {enumObj2List(netifCategoryEnum).map((item) => (
              <Select.Option key={item.value} value={item.value}>
                {item.label}
              </Select.Option>
            ))}
          </Select>
        </FormItem>

        <FormItem
          {...formItemLayout}
          label="业务接口"
          name="netif"
          validateFirst
          // shouldUpdate={true}
          rules={[
            {
              validator: () => {
                if (allNetifs.length === 0) {
                  return Promise.reject('请配置业务接口');
                }
                if (selectedNetifs.length === 0) {
                  return Promise.reject('请配置业务接口');
                }
                return Promise.resolve();
              },
            },
          ]}
        >
          <Table<INetif>
            rowKey="name"
            size="small"
            bordered
            pagination={false}
            loading={queryNetifLoading}
            columns={netifColumns}
            dataSource={allNetifs.filter(
              (netif) => netif.category === DEVICE_NETIF_CATEGORY_RECEIVE,
            )}
          />
        </FormItem>

        <FormItem
          {...formItemLayout}
          label="捕获过滤规则"
          name="ingestPolicyId"
          rules={[
            {
              required: true,
              message: '请选择捕获过滤规则',
            },
          ]}
        >
          <Select placeholder="选择捕获过滤规则">
            {allIngestPolicy.map((item) => (
              <Select.Option key={item.id} value={item.id}>
                {item.name}
              </Select.Option>
            ))}
          </Select>
        </FormItem>

        <FormItem
          {...formItemLayout}
          label="存储过滤规则"
          name="filterRuleIds"
          rules={[
            {
              required: true,
              message: '请选择存储过滤规则',
            },
          ]}
        >
          <Select placeholder="选择存储过滤规则" mode="multiple" defaultValue={DEFAULT_POLICY_ID}>
            {allApplicationPolicy.map((item) => (
              <Select.Option key={item.id} value={item.id} disabled={item.id === DEFAULT_POLICY_ID}>
                {item.name}
              </Select.Option>
            ))}
          </Select>
        </FormItem>

        <FormItem {...formItemLayout} label="生成会话详单" style={{ marginBottom: '0' }}>
          <Row gutter={10}>
            <Col span={3}>
              <FormItem
                name="flowlogDefaultAction"
                valuePropName="checked"
                style={{ display: 'inline-block', width: '100px' }}
              >
                <Checkbox />
              </FormItem>
            </Col>
            <Col span={10}>
              <FormItem
                noStyle
                shouldUpdate={(prevValues, currentValues) =>
                  prevValues.flowlogDefaultAction !== currentValues.flowlogDefaultAction
                }
              >
                {({ getFieldValue }) => {
                  return getFieldValue('flowlogDefaultAction') ? (
                    <FormItem
                      label="单包会话是否生成会话详单"
                      name="flowlogExceptStatus"
                      valuePropName="checked"
                      preserve
                    >
                      <Checkbox />
                    </FormItem>
                  ) : null;
                }}
              </FormItem>
            </Col>
          </Row>
        </FormItem>
        <FormItem {...formItemLayout} label="生成应用层协议详单" style={{ marginBottom: '0' }}>
          <Row gutter={10}>
            <Col span={3}>
              <FormItem
                name="metadataDefaultAction"
                valuePropName="checked"
                style={{ display: 'inline-block', width: '100px' }}
              >
                <Checkbox />
              </FormItem>
            </Col>
            <Col span={8}>
              <FormItem
                label="流量统计包含帧间隙和前导码"
                name="flowlogExceptStatistics"
                valuePropName="checked"
                preserve
              >
                <Checkbox />
              </FormItem>
            </Col>
            <Col span={8}>
              <FormItem
                label="区分VLAN建流分析"
                name="sessionVlanAction"
                valuePropName="checked"
                preserve
              >
                <Checkbox />
              </FormItem>
            </Col>
          </Row>
        </FormItem>
        <Form.Item
          noStyle
          shouldUpdate={(prevValues, currentValues) =>
            prevValues.netifType !== currentValues.netifType
          }
        >
          {({ getFieldValue }) => {
            return getFieldValue('netifType') === NETIF_CATEGORY_HYBRID ? (
              <FormItem {...formItemLayout} label="内网IP配置">
                <FormItem label="选择IP地址组" style={{ marginBottom: 4 }}>
                  <Select
                    style={{ width: 300 }}
                    loading={queryIpAddressLoading}
                    onChange={quickSelectIpAddress}
                    value={''}
                  >
                    <Select.Option disabled value={''}>
                      可快速选择已有的IP地址组
                    </Select.Option>
                    {allIpAddressGroupList.map((group) => (
                      <Select.Option key={group.id} value={group.ipAddress || ''}>
                        {group.name}
                      </Select.Option>
                    ))}
                  </Select>
                </FormItem>
                <FormItem
                  name="insideIpAddress"
                  rules={[
                    {
                      required: false,
                      message: '请填写内网IP',
                    },
                    {
                      validator: checkIpAddress,
                    },
                  ]}
                  extra={
                    <ul style={{ paddingLeft: 20, listStyle: 'circle' }}>
                      <li>每行输入一种IP地址，最多支持{MAX_IP_COUNT}个；</li>
                      <li>可以输入【A.B.C.D】格式的IP地址；</li>
                      <li>或输入【A.B.C.D/掩码长度】格式的IP网段；</li>
                      <li>或输入【A.B.C.D-E.F.G.H】格式的IP组，请确保 E.F.G.H &gt;= A.B.C.D。</li>
                    </ul>
                  }
                >
                  <TextArea rows={4} />
                </FormItem>
              </FormItem>
            ) : null;
          }}
        </Form.Item>

        <Form.Item label="外发策略" {...formItemLayout}>
          <Space>
            <Form.Item name="sendPolicyIds" noStyle>
              <Select
                mode="multiple"
                placeholder="请选择外发策略"
                style={{ width: '45vw' }}
                disabled={cmsConnectFlag}
                dropdownRender={(menu) => {
                  return (
                    <>
                      {menu}
                      <Divider style={{margin:'0px'}} />
                        <Button
                         style={{margin:'5px'}}
                          onClick={() => {
                            fetchPolicies();
                          }}
                          size='small'
                          type='link'
                        >
                          刷新
                        </Button>
                    </>
                  );
                }}
                suffixIcon={
                  policyLoading ? (
                    <Spin
                      size="small"
                      indicator={
                        <LoadingOutlined style={{ fontSize: '10px', marginTop: '7px' }} spin />
                      }
                    />
                  ) : (
                    <Tooltip title="刷新外发策略列表">
                      <RedoOutlined
                        onClick={() => {
                          fetchPolicies();
                        }}
                      />
                    </Tooltip>
                  )
                }
              >
                {sendPolicies.map((policy) => {
                  return <Option value={policy.id}>{policy.name}</Option>;
                })}
              </Select>
            </Form.Item>
            <Button
              type="link"
              onClick={() => {
                window.open('/#/configuration/third-party/send-policy/create');
              }}
              disabled={cmsConnectFlag}
            >
              新建外发策略
            </Button>
          </Space>
        </Form.Item>

        <FormItem {...submitFormLayout} style={{ marginTop: 32 }}>
          <Button type="primary" htmlType="submit" loading={submitting}>
            保存
          </Button>
          <Button style={{ marginLeft: 8 }} onClick={handleGoBack}>
            取消
          </Button>
        </FormItem>
      </Form>
    </Card>
  );
};

export default connect(
  ({
    loading: { effects },
    deviceNetifModel: { list: allNetifs },
    ipAddressGroupModel: { allIpAddressGroupList },
    networkModel: { usedNetifs },
    applicationPolicyModel: { allApplicationPolicy },
    ingestPolicyModel: { allIngestPolicy },
  }: ConnectState) => ({
    allNetifs,
    allIpAddressGroupList,
    usedNetifs,
    allIngestPolicy,
    allApplicationPolicy,
    submitting: effects['networkModel/createNetwork'] || effects['networkModel/updateNetwork'],
    queryNetifLoading:
      effects['deviceNetifModel/queryDeviceNetifs'] || effects['networkModel/queryUsedNetifs'],
    queryIpAddressLoading: effects['ipAddressGroupModel/queryAllIpAddressGroup'],
  }),
)(NetworkForm);
