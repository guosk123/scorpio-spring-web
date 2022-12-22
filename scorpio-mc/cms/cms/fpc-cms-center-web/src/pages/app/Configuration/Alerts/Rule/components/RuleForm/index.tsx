import type { ConnectState } from '@/models/connect';
import type { AlertRefireTypeTypes } from '@/pages/app/Configuration/Alerts/typings';
import type { ICountry } from '@/pages/app/Configuration/Geolocation/typings';
import type { IpAddressGroup } from '@/pages/app/Configuration/IpAddressGroup/typings';
import type { ApplicationItem } from '@/pages/app/Configuration/SAKnowledge/typings';
import type { IService } from '@/pages/app/Configuration/Service/typings';
import {
  createConfirmModal,
  ipV4Regex,
  ipV6Regex,
  isCidr,
  nameRegex,
  updateConfirmModal,
} from '@/utils/utils';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import type { FormComponentProps, ValidationRule } from '@ant-design/compatible/es/form';
import { QuestionCircleOutlined, ReloadOutlined } from '@ant-design/icons';
import {
  Button,
  Card,
  Checkbox,
  Col,
  Divider,
  Input,
  InputNumber,
  Radio,
  Row,
  Select,
  Tooltip,
  TreeSelect,
} from 'antd';
import type { RadioChangeEvent } from 'antd/lib/radio';
import { connect } from 'dva';
import React, { Fragment, PureComponent } from 'react';
import type { Dispatch } from 'redux';
import type { INetworkTreeData } from 'umi';
import { history } from 'umi';
import type { IAlertRule, IFilterCondition } from '../../../typings';
import {
  ALERT_CALCULATION_ENUM,
  ALERT_CATEGORY_ENUM,
  ALERT_LEVEL_ENUM,
  ALERT_METRIC_ENUM,
  ALERT_REFIRE_TIME_ENUM,
  ALTER_SOURCE_ENUM,
  EAlertCategory,
  EAlertRefireType,
  ESource,
  ETrendWindowingModel,
  OPERATOR_ENUM,
  TREND_WEIGHTING_MODEL_ENUM,
  TREND_WINDOWING_MODEL_ENUM,
  WINDOW_SECONDS_ENUM,
} from '../../../typings';
import ComposeCondition from '../ComposeCondition';
import styles from './index.less';
import type { ILogicalSubnet } from '@/pages/app/Configuration/LogicalSubnet/typings';

const FormItem = Form.Item;
const { TextArea } = Input;

const formLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 18 },
};

const networkFormLayout = {
  labelCol: { span: 8 },
  wrapperCol: { span: 12 },
};

const DISABLE_METRIC_FOR_CALCULATION = [
  'tcp_client_network_latency_avg',
  'tcp_server_network_latency_avg',
  'server_response_latency_avg',
];

const DISABLE_CALCULATION_FOR_METRIC = ['TOTAL'];

// const inlineFormLayout = {
//   labelCol: { span: 6 },
//   wrapperCol: { span: 18 },
// };

type IEnumObj = Record<string, string>;
interface List {
  label: string;
  value: string;
}

/**
 * 枚举对象转数组
 * @param enumObj
 */
export const enumObj2List = (enumObj: IEnumObj): List[] => {
  if (Object.prototype.toString.call(enumObj) !== '[object Object]') {
    return [];
  }
  const result: List[] = [];

  Object.keys(enumObj).forEach((el) => {
    result.push({
      value: el,
      label: enumObj[el],
    });
  });

  return result;
};

interface IAlertRuleFormProps extends FormComponentProps {
  dispatch: Dispatch<any>;
  operateType: 'CREATE' | 'UPDATE' | 'COPY';
  detail: IAlertRule;
  allAlertRule: IAlertRule[];
  allServices: IService[];
  networkSensorTree: INetworkTreeData[];
  allCountryList: ICountry[];
  allIpAddressGroupList: IpAddressGroup[];
  allApplicationList: ApplicationItem[];
  submitLoading: boolean | undefined;
  allLogicalSubnets: ILogicalSubnet[];
  queryAllAlertRuleLoading: boolean | undefined;
  queryAllSourceLoading: boolean | undefined;
}
interface IAlertRuleFormState {
  filterCondition: IFilterCondition;
}
class AlertRuleForm extends PureComponent<IAlertRuleFormProps, IAlertRuleFormState> {
  createConfirmModal: ({
    dispatchType,
    values,
    onOk,
    onCancel,
  }: {
    dispatchType: any;
    values: any;
    onOk: any;
    onCancel: any;
    dispatch?: any;
  }) => void;
  updateConfirmModal: ({
    dispatchType,
    values,
    onOk,
    onCancel,
  }: {
    dispatchType: any;
    values: any;
    onOk: any;
    onCancel: any;
    dispatch?: any;
  }) => void;

  constructor(props: IAlertRuleFormProps) {
    super(props);
    // @ts-ignore
    this.createConfirmModal = createConfirmModal.bind(this);
    // @ts-ignore
    this.updateConfirmModal = updateConfirmModal.bind(this);
  }

  componentDidMount() {
    this.queryAllAlertRules();
    this.queryAllSource();
    this.queryIpAddressGroups();
  }

  networkAndServiceToTreeData(network: INetworkTreeData[], noService?: boolean) {
    const res: any = network.map((item) => {
      const serviceChildren = this.props.allServices
        .filter((ele) => ele.networkIds.split(',').includes(item.key))
        .map((sub) => ({
          title: `${sub.name}(业务)`,
          value: `${sub.id}^${item.key}`,
          key: `${sub.id}^${item.key}`,
        }));
      return {
        title: item.title,
        value: item.key,
        key: item.key,
        children: noService ? [] : serviceChildren,
      };
    });
    return res;
  }

  queryAllAlertRules = () => {
    const { dispatch } = this.props;
    dispatch({
      type: 'alertModel/queryAllAlertRules',
      payload: {
        category: [EAlertCategory.THRESHOLD, EAlertCategory.TREND].join(','),
      },
    });
  };

  queryIpAddressGroups = () => {
    const { dispatch } = this.props;
    dispatch({
      type: 'ipAddressGroupModel/queryAllIpAddressGroup',
    });
  };

  queryAllSource = () => {
    const { dispatch } = this.props;
    dispatch({
      type: 'networkModel/queryNetworkSensorTree',
    });
    dispatch({
      type: 'serviceModel/queryAllServices',
    });
  };

  // 提交
  handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    const { form, operateType } = this.props;
    e.preventDefault();
    form.validateFieldsAndScroll((err, formValue) => {
      if (err) return;

      const fieldsValue = { ...formValue };
      const { category, source, fireCriteria, trendDefine, refire } = fieldsValue;

      // 阈值告警
      if (category === EAlertCategory.THRESHOLD) {
        fieldsValue.thresholdSettings = JSON.stringify({
          fireCriteria,
        });
        fieldsValue.trendSettings = JSON.stringify({});
        fieldsValue.advancedSettings = JSON.stringify({});
      }
      // 基线告警
      if (category === EAlertCategory.TREND) {
        fieldsValue.trendSettings = JSON.stringify({
          fireCriteria,
          trend: trendDefine,
        });
        fieldsValue.thresholdSettings = JSON.stringify({});
        fieldsValue.advancedSettings = JSON.stringify({});
      }
      // 组合告警
      if (category === EAlertCategory.ADVANCED) {
        fieldsValue.advancedSettings = JSON.stringify(fieldsValue.advancedSettings);
        fieldsValue.thresholdSettings = JSON.stringify({});
        fieldsValue.trendSettings = JSON.stringify({});
      }

      delete fieldsValue?.source;
      delete fieldsValue?.fireCriteria;
      delete fieldsValue?.trendDefine;

      fieldsValue.refire = JSON.stringify(refire);

      // 将网络和业务ID进行区分
      fieldsValue.serviceIds = [];
      fieldsValue.networkIds = [];

      if (fieldsValue.allNetwork) {
        fieldsValue.networkIds = ['allNetwork'];
        fieldsValue.serviceIds = [];
      } else {
        fieldsValue.scope?.forEach((item: any) => {
          if (item.value ? item.value?.split('^').length === 2 : item.split('^').length === 2) {
            fieldsValue.serviceIds.push(item.value ? item.value : item);
          } else {
            fieldsValue.networkIds.push(item.value ? item.value : item);
          }
        });
      }

      fieldsValue.networkIds = fieldsValue.networkIds.join();
      fieldsValue.serviceIds = fieldsValue.serviceIds.join();

      const tmpMetircs = {
        numerator: {
          ...fieldsValue?.metrics?.numerator,
          sourceType: source?.sourceType,
          sourceValue: source?.sourceValue,
        },
        isRatio: fieldsValue?.metrics?.isRatio,
        denominator: {
          ...fieldsValue?.metrics?.denominator,
          sourceType: source?.sourceType,
          sourceValue: source?.sourceValue,
        },
      };
      if (fieldsValue.category === 'trend') {
        const tmpJson = JSON.parse(fieldsValue?.trendSettings);
        tmpJson.metrics = { ...tmpMetircs };
        fieldsValue.trendSettings = JSON.stringify(tmpJson);
      } else if (fieldsValue.category === 'threshold') {
        const tmpJson = JSON.parse(fieldsValue?.thresholdSettings);
        tmpJson.metrics = { ...tmpMetircs };
        fieldsValue.thresholdSettings = JSON.stringify(tmpJson);
      }

      // console.log('fieldsValue', fieldsValue);

      if (operateType === 'CREATE') {
        this.handleCreate(fieldsValue);
      } else if (operateType === 'COPY') {
        fieldsValue.id = '';
        this.handleCreate(fieldsValue);
      } else {
        this.handleUpdate(fieldsValue);
      }
    });
  };

  /**
   * 告警维度变化时，清空指标的原有数据源配置
   */
  handleSourceTypeChange = () => {
    const { form } = this.props;
    form.setFieldsValue({
      // 清空数据源
      'source[numerator[sourceType]]': undefined,
      'source[numerator[sourceValue]]': undefined,
      'source[denominator[sourceType]]': undefined,
      'source[denominator[sourceValue]]': undefined,
    });
  };

  handleCategoryChange = (category: string) => {
    const { form } = this.props;
    if (category === 'trend') {
      form.setFieldsValue({
        // 清空告警指标
        'metrics[numerator[metric]]': undefined,
        'metrics[denominator[metric]]': undefined,
      });
    }
    form.setFieldsValue({
      // 清空计算方法
      'fireCriteria[calculation]': undefined,
    });
  };

  handleRefireTypeChange = (e: RadioChangeEvent) => {
    const refireType: AlertRefireTypeTypes = e.target.value;
    const { form } = this.props;
    if (refireType !== EAlertRefireType.REPEATEDLY) {
      form.setFieldsValue({
        'refire[seconds]': undefined,
      });
    }
  };

  handleComposeConditionChange = (condition: IFilterCondition) => {
    const { form } = this.props;
    form.setFieldsValue({
      'advancedSettings[fireCriteria]': condition,
    });
    // 重新触发校验
    form.validateFields(['advancedSettings[fireCriteria]'], { force: true });
  };

  checkComposeCondition = (rule: any, value: IFilterCondition, callback: any) => {
    if (!value.operator || !value.group) {
      callback('请检查告警组合是否填写完整');
      return;
    }
    if (value.group.length === 0) {
      callback('请检查告警组合是否填写完整');
      return;
    }
    callback();
  };

  handleWindowingModelChange = (value: ETrendWindowingModel) => {
    if (!value) {
      return;
    }
    const { form } = this.props;
    form.setFieldsValue({
      'trendDefine[windowingCount]': undefined,
    });
  };

  /**
   * 检查回顾窗口
   */
  trendDefineWindowingCount = (rule: any, value: number, callback: any) => {
    // 获取基线窗口
    const { form } = this.props;
    const windowingModel: ETrendWindowingModel = form.getFieldValue('trendDefine[windowingModel]');
    if (!value) {
      callback('请填写回顾周期');
      return;
    }

    if (windowingModel === ETrendWindowingModel.HOUR_OF_WEEK) {
      if (value < 1 || value > 4) {
        callback('回顾周期范围支持1-4');
        return;
      }
    } else if (value < 1 || value > 60) {
      callback('回顾周期范围支持1-60');
      return;
    }

    callback();
  };

  handleGoListPage = () => {
    history.goBack();
  };

  handleReset = () => {
    const { form } = this.props;
    form.resetFields();
  };

  handleCreate = (values: IAlertRule) => {
    this.createConfirmModal({
      dispatchType: 'alertModel/createAlertRule',
      values,
      onOk: this.handleGoListPage,
      onCancel: this.handleReset,
    });
  };

  handleUpdate = (values: IAlertRule) => {
    this.updateConfirmModal({
      dispatchType: 'alertModel/updateAlertRule',
      values,
      onOk: this.handleGoListPage,
      onCancel: () => {},
    });
  };

  renderSourceContent = (sourceType: string) => {
    const { allCountryList = [], allIpAddressGroupList = [], allApplicationList = [] } = this.props;
    let operandDom = (
      <InputNumber min={0} style={{ width: '100%' }} disabled={true} placeholder="数据源的值" />
    );
    if (sourceType === ESource.HOSTGROUP) {
      operandDom = (
        <Select
          className={styles.sourceInput}
          showSearch
          filterOption={(input: any, option: any) =>
            option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
          }
        >
          {allIpAddressGroupList.map((item: any) => (
            <Select.Option key={item.id} value={item.id}>
              {item.name}
            </Select.Option>
          ))}
        </Select>
      );
    } else if (sourceType === ESource.APPLICATION) {
      operandDom = (
        <Select
          className={styles.sourceInput}
          showSearch
          filterOption={(input: any, option: any) =>
            option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
          }
        >
          {allApplicationList.map((item: any) => (
            <Select.Option key={item.applicationId} value={item.applicationId}>
              {item.nameText}
            </Select.Option>
          ))}
        </Select>
      );
    } else if (sourceType === ESource.GEOLOCATION) {
      operandDom = (
        <TreeSelect
          style={{ width: '100%' }}
          showSearch
          treeNodeFilterProp="title"
          dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
          treeData={allCountryList}
          placeholder="选择地区"
        />
      );
    } else if (sourceType === ESource.IPADDRESS) {
      operandDom = <Input min={0} style={{ width: '100%' }} placeholder="输入正确的IPv4或IPv6" />;
    }

    return operandDom;
  };

  getSourceValueRules = (source: ESource) => {
    if (source === ESource.IPADDRESS) {
      const rules: ValidationRule[] = [
        {
          validator: async (rule, value) => {
            if (!value) {
              throw new Error('请输入正确的IPv4或IPv6地址');
            }
            if (!ipV4Regex.test(value) && !ipV6Regex.test(value)) {
              throw new Error('请输入正确的IPv4或IPv6地址');
            }
          },
          required: this.props.form.getFieldValue('source[sourceType]'),
        },
      ];
      return rules;
    }
    return [
      {
        required: this.props.form.getFieldValue('source[sourceType]'),
        message: '数据源的值',
      },
    ];
  };

  checkServiceRelationNetwork = (allServices: IService[], serviceId: string, networkId: string) => {
    return [
      {
        validator: async () => {
          if (
            !allServices
              .filter((ele) => ele.id === serviceId)
              .filter((ele) => ele.networkIds.split(',').includes(networkId)).length
          ) {
            throw new Error('作用业务和作用网络没有关联关系');
          }
        },
      },
    ];
  };

  render() {
    const {
      form,
      submitLoading,
      detail = {} as IAlertRule,
      allAlertRule = [],
      networkSensorTree = [],
      queryAllAlertRuleLoading,
    } = this.props;
    const { getFieldValue } = form;
    const networkAndSubNet = networkSensorTree.concat(
      // @ts-ignore
      this.props.allLogicalSubnets.map((item) => ({ title: `${item.name}(子网)`, key: item.id })),
    );
    const treeData = this.networkAndServiceToTreeData(networkAndSubNet, false);
    const treeDataNoService = this.networkAndServiceToTreeData(networkAndSubNet, true);
    const fixedAlertCalculationEnum = { ...ALERT_CALCULATION_ENUM };
    // 基线告警不显示计数和标准差
    if (getFieldValue('category') === EAlertCategory.TREND) {
      // @ts-ignore
      delete fixedAlertCalculationEnum.TOTAL;
      // @ts-ignore
      delete fixedAlertCalculationEnum.STDEV;
    }
    console.log(treeData);
    return (
      <Form onSubmit={this.handleSubmit}>
        <FormItem key="id" {...formLayout} label="id" style={{ display: 'none' }}>
          {form.getFieldDecorator('id', {
            initialValue: detail.id,
          })(<Input placeholder="请输入" />)}
        </FormItem>
        <FormItem key="name" {...formLayout} label="名称">
          {form.getFieldDecorator('name', {
            initialValue: detail.name,
            rules: [
              { required: true, whitespace: true, message: '请输入告警配置名称' },
              {
                max: 30,
                message: '最多可输入30个字符',
              },
              {
                pattern: nameRegex,
                message: '名称只能包含字母，汉字，数字',
              },
            ],
          })(<Input placeholder="请输入告警配置名称" />)}
        </FormItem>
        <FormItem key="category" {...formLayout} label="分类">
          {form.getFieldDecorator('category', {
            initialValue: detail.category,
            rules: [{ required: true, message: '请选择告警配置分类' }],
          })(
            <Select placeholder="请选择告警配置分类" onChange={this.handleCategoryChange}>
              {enumObj2List(ALERT_CATEGORY_ENUM).map((item) => {
                return (
                  <Select.Option key={item.value} value={item.value}>
                    {item.label}
                  </Select.Option>
                );
              })}
            </Select>,
          )}
        </FormItem>
        <FormItem key="level" {...formLayout} label="级别">
          {form.getFieldDecorator('level', {
            initialValue: detail.level,
            rules: [{ required: true, message: '请选择告警配置级别' }],
          })(
            <Select placeholder="请选择告警配置级别">
              {enumObj2List(ALERT_LEVEL_ENUM).map((item) => (
                <Select.Option key={item.value} value={item.value}>
                  {item.label}
                </Select.Option>
              ))}
            </Select>,
          )}
        </FormItem>
        {getFieldValue('category') !== EAlertCategory.ADVANCED && (
          <Fragment>
            <FormItem
              key="source"
              {...formLayout}
              label="数据源"
              required={getFieldValue('source[sourceType]')}
              style={{ marginBottom: 0 }}
            >
              {![
                'established_tcp_sessions',
                'concurrent_tcp_sessions',
                'long_connections',
                'broadcast_packets',
              ].includes(getFieldValue('metrics[numerator[metric]]')) &&
              ![
                'established_tcp_sessions',
                'concurrent_tcp_sessions',
                'long_connections',
                'broadcast_packets',
              ].includes(getFieldValue('metrics[denominator[metric]]')) ? (
                <Row gutter={10}>
                  <Col span={8}>
                    <FormItem>
                      {form.getFieldDecorator('source[sourceType]', {
                        initialValue:
                          detail.thresholdSettings?.metrics?.numerator?.sourceType ||
                          detail.trendSettings?.metrics?.numerator?.sourceType,
                        rules: [{ message: '告警指标数据源' }],
                      })(
                        <Select
                          placeholder="告警指标数据源"
                          onChange={() => {
                            form.setFieldsValue({ 'source[sourceType]': undefined });
                          }}
                        >
                          {enumObj2List(ALTER_SOURCE_ENUM).map((item) => (
                            <Select.Option key={item.value} value={item.value}>
                              {item.label}
                            </Select.Option>
                          ))}
                        </Select>,
                      )}
                    </FormItem>
                  </Col>
                  <Col span={8}>
                    <FormItem>
                      {form.getFieldDecorator('source[sourceValue]', {
                        initialValue:
                          detail.thresholdSettings?.metrics?.numerator?.sourceValue ||
                          detail.trendSettings?.metrics?.numerator?.sourceValue,
                        rules: this.getSourceValueRules(getFieldValue('source[sourceType]')),
                      })(this.renderSourceContent(getFieldValue('source[sourceType]')))}
                    </FormItem>
                  </Col>
                  <Col span={8}>
                    <Button
                      type="link"
                      disabled={
                        !(
                          getFieldValue('source[sourceType]') !== undefined ||
                          getFieldValue('source[sourceValue]') !== undefined
                        )
                      }
                      onClick={() => {
                        form.setFieldsValue({
                          // 清空数据源
                          'source[sourceType]': undefined,
                          'source[sourceValue]': undefined,
                        });
                      }}
                    >
                      清除
                    </Button>
                  </Col>
                </Row>
              ) : (
                <Row gutter={10}>
                  <Col span={8}>
                    <FormItem>
                      <Select placeholder="告警指标数据源" disabled />
                    </FormItem>
                  </Col>
                  <Col span={8}>
                    <FormItem>
                      <InputNumber style={{ width: '100%' }} disabled placeholder="数据源的值" />
                    </FormItem>
                  </Col>
                </Row>
              )}
            </FormItem>
          </Fragment>
        )}
        {getFieldValue('category') !== EAlertCategory.ADVANCED && (
          <Fragment>
            <FormItem key="metrics[numerator]" {...formLayout} label="指标" required>
              <Col span={8}>
                {form.getFieldDecorator('metrics[numerator[metric]]', {
                  initialValue:
                    detail.thresholdSettings?.metrics?.numerator?.metric ||
                    detail.trendSettings?.metrics?.numerator?.metric,
                  rules: [{ required: true, message: '请选择告警指标' }],
                })(
                  <Select placeholder="请选择告警指标">
                    {enumObj2List(ALERT_METRIC_ENUM)
                      .filter((ele) => {
                        const tmpCalculation = getFieldValue('fireCriteria[calculation]');
                        let res = true;
                        if (
                          (DISABLE_CALCULATION_FOR_METRIC.includes(tmpCalculation) &&
                            DISABLE_METRIC_FOR_CALCULATION.includes(ele?.value)) ||
                          (getFieldValue('category') === EAlertCategory.TREND &&
                            (ele?.value === 'broadcast_packets' ||
                              ele?.value === 'long_connections'))
                        ) {
                          res = false;
                        }
                        return res;
                      })
                      .map((item) => (
                        <Select.Option key={item.value} value={item.value}>
                          {item.label}
                        </Select.Option>
                      ))}
                  </Select>,
                )}
              </Col>
            </FormItem>
            <FormItem key="metrics[isRatio]" {...formLayout} label="比率">
              {form.getFieldDecorator('metrics[isRatio]', {
                initialValue: detail.thresholdSettings?.metrics?.isRatio || false,
                valuePropName: 'checked',
                rules: [],
              })(<Checkbox />)}
            </FormItem>
            {getFieldValue('metrics[isRatio]') && (
              <FormItem key="metrics[denominator]" {...formLayout} label="分母指标" required>
                <Col span={8}>
                  {form.getFieldDecorator('metrics[denominator[metric]]', {
                    initialValue:
                      detail.thresholdSettings?.metrics?.denominator?.metric ||
                      detail.trendSettings?.metrics?.denominator?.metric,
                    rules: [{ required: true, message: '请选择告警指标' }],
                  })(
                    <Select placeholder="请选择告警指标">
                      {enumObj2List(ALERT_METRIC_ENUM)
                        .filter((ele) => {
                          const tmpCalculation = getFieldValue('fireCriteria[calculation]');
                          return (
                            (!DISABLE_CALCULATION_FOR_METRIC.includes(tmpCalculation) ||
                              !DISABLE_METRIC_FOR_CALCULATION.includes(ele?.value)) &&
                            !(
                              getFieldValue('category') === EAlertCategory.TREND &&
                              ele?.value === 'long_connections'
                            )
                          );
                        })
                        .map((item) => (
                          <Select.Option key={item.value} value={item.value}>
                            {item.label}
                          </Select.Option>
                        ))}
                    </Select>,
                  )}
                </Col>
              </FormItem>
            )}
          </Fragment>
        )}

        {/* 基线定义 */}
        {getFieldValue('category') === EAlertCategory.TREND && (
          <FormItem key="trendDefine" {...formLayout} label="基线定义" required>
            <Card bodyStyle={{ padding: '20px 10px 0' }}>
              <FormItem key="trendDefine[weightingModel]" {...formLayout} label="权重模型">
                {form.getFieldDecorator('trendDefine[weightingModel]', {
                  initialValue: detail.trendDefine?.weightingModel,
                  rules: [{ required: true, message: '请选择权重模型' }],
                })(
                  <Select placeholder="权重模型" style={{ width: 200 }}>
                    {enumObj2List(TREND_WEIGHTING_MODEL_ENUM).map((item) => (
                      <Select.Option key={item.value} value={item.value}>
                        {item.label}
                      </Select.Option>
                    ))}
                  </Select>,
                )}
              </FormItem>
              <FormItem key="trendDefine[windowingModel]" {...formLayout} label="基线窗口">
                {form.getFieldDecorator('trendDefine[windowingModel]', {
                  initialValue: detail.trendDefine?.windowingModel,
                  rules: [{ required: true, message: '请选择基线窗口' }],
                })(
                  <Select
                    placeholder="基线窗口"
                    style={{ width: 200 }}
                    onChange={this.handleWindowingModelChange}
                  >
                    {enumObj2List(TREND_WINDOWING_MODEL_ENUM).map((item) => (
                      <Select.Option key={item.value} value={item.value}>
                        {item.label}
                      </Select.Option>
                    ))}
                  </Select>,
                )}
              </FormItem>
              <FormItem
                key="trendDefine[windowingCount]"
                {...formLayout}
                label={
                  <span>
                    回顾周期{' '}
                    <Tooltip title="数量集达到所要求的回顾周期跨度时才有可能会产生告警">
                      <QuestionCircleOutlined />
                    </Tooltip>
                  </span>
                }
              >
                {form.getFieldDecorator('trendDefine[windowingCount]', {
                  initialValue: detail.trendDefine?.windowingCount,
                  validateFirst: true,
                  rules: [
                    { required: true, message: '请选择回顾周期' },
                    {
                      validator: this.trendDefineWindowingCount,
                    },
                  ],
                })(
                  <InputNumber
                    placeholder="回顾周期"
                    min={1}
                    precision={0}
                    style={{ width: 200 }}
                  />,
                )}
              </FormItem>
            </Card>
          </FormItem>
        )}

        {/* 告警条件：阈值告警和基线告警存在  */}
        {getFieldValue('category') !== EAlertCategory.ADVANCED && (
          <FormItem
            key="fireCriteria"
            {...formLayout}
            label="告警条件"
            required
            style={{ marginBottom: 0 }}
          >
            <span className="ant-form-text">过去</span>
            <FormItem style={{ display: 'inline-block', width: 120, marginRight: 10 }}>
              {form.getFieldDecorator('fireCriteria[windowSeconds]', {
                initialValue: detail.fireCriteria?.windowSeconds,
                rules: [{ required: true, message: '请选择时间窗口' }],
              })(
                <Select placeholder="时间窗口">
                  {enumObj2List(WINDOW_SECONDS_ENUM).map((item) => (
                    <Select.Option key={item.value} value={+item.value}>
                      {item.label}
                    </Select.Option>
                  ))}
                </Select>,
              )}
            </FormItem>
            <span className="ant-form-text">内</span>
            <FormItem style={{ display: 'inline-block', width: 120, marginRight: 10 }}>
              {form.getFieldDecorator('fireCriteria[calculation]', {
                initialValue: detail.fireCriteria?.calculation,
                rules: [{ required: true, message: '请选择指标计算方法' }],
              })(
                <Select placeholder="计算方法">
                  {enumObj2List(fixedAlertCalculationEnum)
                    .filter((ele) => {
                      const tmpMetrics = getFieldValue('metrics');
                      const isDelaytimeFlag =
                        DISABLE_METRIC_FOR_CALCULATION.includes(tmpMetrics?.numerator?.metric) ||
                        DISABLE_METRIC_FOR_CALCULATION.includes(tmpMetrics?.denominator?.metric);
                      return (
                        !isDelaytimeFlag || !DISABLE_CALCULATION_FOR_METRIC.includes(ele?.value)
                      );
                    })
                    .map((item) => (
                      <Select.Option key={item.value} value={item.value}>
                        {item.label}
                      </Select.Option>
                    ))}
                </Select>,
              )}
            </FormItem>
            <span className="ant-form-text">值</span>
            <FormItem style={{ display: 'inline-block', width: 120, marginRight: 10 }}>
              {form.getFieldDecorator('fireCriteria[operator]', {
                initialValue: detail.fireCriteria?.operator,
                rules: [{ required: true, message: '请选择操作符' }],
              })(
                <Select placeholder="操作符">
                  {enumObj2List(OPERATOR_ENUM).map((item) => (
                    <Select.Option key={item.value} value={item.value}>
                      {item.label}
                    </Select.Option>
                  ))}
                </Select>,
              )}
            </FormItem>
            <FormItem style={{ display: 'inline-block', width: 120 }}>
              {form.getFieldDecorator('fireCriteria[operand]', {
                initialValue: detail.fireCriteria?.operand,
                rules: [{ required: true, message: '请填写阈值' }],
              })(<InputNumber min={0} style={{ width: '100%' }} placeholder="阈值" />)}
            </FormItem>
            {getFieldValue('category') === EAlertCategory.TREND && (
              <span className="ant-form-text" style={{ marginLeft: 4 }}>
                趋势百分比
              </span>
            )}
          </FormItem>
        )}

        {/* 组合告警 */}
        {getFieldValue('category') === EAlertCategory.ADVANCED && (
          <Fragment>
            <FormItem label="告警组合配置" {...formLayout} required>
              {form.getFieldDecorator('advancedSettings[fireCriteria]', {
                initialValue: [],
                rules: [
                  {
                    required: true,
                    message: '告警组合配置',
                  },
                  {
                    validator: this.checkComposeCondition,
                  },
                ],
              })(<Input style={{ display: 'none' }} />)}

              <ComposeCondition
                condition={detail.advancedSettings?.fireCriteria}
                alertSetings={allAlertRule}
                onChange={this.handleComposeConditionChange}
                loading={queryAllAlertRuleLoading}
                extra={
                  <Tooltip title="重新获取告警列表">
                    <ReloadOutlined onClick={this.queryAllAlertRules} />
                  </Tooltip>
                }
              />
            </FormItem>
            <FormItem
              label={
                <span>
                  触发周期{' '}
                  <Tooltip title="周期时间内组合条件均满足才认为告警满足">
                    <QuestionCircleOutlined />
                  </Tooltip>
                </span>
              }
              {...formLayout}
            >
              {form.getFieldDecorator('advancedSettings[windowSeconds]', {
                initialValue: detail.advancedSettings?.windowSeconds,
                rules: [{ required: true, message: '请选择触发周期' }],
              })(
                <Select placeholder="触发周期">
                  {enumObj2List(WINDOW_SECONDS_ENUM).map((item) => (
                    <Select.Option key={item.value} value={+item.value}>
                      {item.label}
                    </Select.Option>
                  ))}
                </Select>,
              )}
            </FormItem>
          </Fragment>
        )}

        <FormItem key="refire" {...formLayout} label="告警间隔" required>
          <Row gutter={10}>
            <Col>
              <FormItem>
                {form.getFieldDecorator('refire[type]', {
                  initialValue: detail.refire?.type,
                  rules: [{ required: true, message: '请选择告警间隔类型' }],
                })(
                  <Radio.Group
                    className={styles.alertBehavior}
                    onChange={this.handleRefireTypeChange}
                  >
                    {getFieldValue('category') !== EAlertCategory.ADVANCED && (
                      <Radio value="none">不告警</Radio>
                    )}
                    <Radio value="once">只告警一次</Radio>
                    <Radio value="repeatedly">周期性告警</Radio>
                  </Radio.Group>,
                )}
              </FormItem>
            </Col>
            <Col>
              <FormItem label="间隔" style={{ width: 200 }}>
                {form.getFieldDecorator('refire[seconds]', {
                  initialValue: detail.refire?.seconds,
                  rules: [
                    {
                      required: getFieldValue('refire[type]') === EAlertRefireType.REPEATEDLY,
                      message: '请选择告警间隔时间',
                    },
                  ],
                })(
                  <Select
                    style={{ display: 'inline-block' }}
                    placeholder="间隔时间"
                    disabled={getFieldValue('refire[type]') !== EAlertRefireType.REPEATEDLY}
                  >
                    {enumObj2List(ALERT_REFIRE_TIME_ENUM).map((item) => {
                      return (
                        <Select.Option key={item.value} value={+item.value}>
                          {item.label}
                        </Select.Option>
                      );
                    })}
                  </Select>,
                )}
              </FormItem>
            </Col>
          </Row>
        </FormItem>

        <FormItem key="description" {...formLayout} label="描述信息">
          {form.getFieldDecorator('description', {
            initialValue: detail.description || '',
            rules: [
              { required: false, message: '请输入描述信息' },
              { max: 255, message: '最多可输入255个字符' },
            ],
          })(<TextArea rows={4} placeholder="请输入描述信息" />)}
        </FormItem>
        <Row gutter={10}>
          <Divider />
          <Col span={12}>
            <FormItem key="allNetwork" {...networkFormLayout} label="是否作用于所有网络">
              {form.getFieldDecorator('allNetwork', {
                initialValue: detail?.networkIds === 'allNetwork' && 'checked',
                valuePropName: 'checked',
                rules: [],
              })(<Checkbox />)}
            </FormItem>
          </Col>
          <Col />
          <Col span={12}>
            <FormItem label="自定义作用域" {...networkFormLayout}>
              {form.getFieldDecorator('scope', {
                initialValue: detail?.networkIds
                  ?.split(',')
                  .concat(detail?.serviceIds?.split(','))
                  .filter((ele) => ele.length > 0 && ele !== 'allNetwork'),
                rules: [
                  {
                    required: !getFieldValue('allNetwork'),
                    message: '告警作用域',
                  },
                ],
              })(
                <TreeSelect
                  showSearch
                  style={{ width: '100%' }}
                  dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
                  placeholder="选择作用域"
                  allowClear
                  disabled={getFieldValue('allNetwork')}
                  treeDefaultExpandAll
                  treeCheckable={true}
                  treeNodeFilterProp="title"
                  treeCheckStrictly={true}
                  treeData={
                    // 长连接和广播包数不可作用于业务
                    ['long_connections', 'broadcast_packets'].includes(
                      getFieldValue('metrics[numerator[metric]]'),
                    ) ||
                    ['long_connections', 'broadcast_packets'].includes(
                      getFieldValue('metrics[denominator[metric]]'),
                    )
                      ? treeDataNoService
                      : treeData
                  }
                />,
              )}
            </FormItem>
          </Col>
        </Row>
        <FormItem wrapperCol={{ span: 12, offset: 4 }}>
          <Button
            style={{ marginRight: 10 }}
            type="primary"
            htmlType="submit"
            loading={submitLoading}
          >
            保存
          </Button>
          <Button onClick={() => this.handleGoListPage()}>返回</Button>
        </FormItem>
      </Form>
    );
  }
}

export default connect(
  ({
    alertModel: { allAlertRule },
    serviceModel: { allServices },
    geolocationModel: { allCountryList },
    ipAddressGroupModel: { allIpAddressGroupList },
    SAKnowledgeModel: { allApplicationList },
    networkModel: { networkSensorTree },
    logicSubnetModel: { allLogicalSubnets },
    loading: { effects },
  }: ConnectState) => ({
    allAlertRule,
    allServices,
    networkSensorTree,
    allCountryList,
    allIpAddressGroupList,
    allApplicationList,
    allLogicalSubnets,
    submitLoading: effects['alertModel/updateAlertRule'] || effects['alertModel/createAlertRule'],
    queryAllAlertRuleLoading: effects['alertModel/queryAllAlertRules'],
    queryAllSourceLoading:
      effects['serviceModel/queryAllServices'] || effects['networkModel/queryNetworkSensorTree'],
  }),
)(Form.create<IAlertRuleFormProps>()(AlertRuleForm));
