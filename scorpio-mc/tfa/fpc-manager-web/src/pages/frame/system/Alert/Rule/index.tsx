import type { ConnectState } from '@/models/connect';
import { InfoCircleOutlined } from '@ant-design/icons';
import {
  Button,
  Checkbox,
  Col,
  Form,
  InputNumber,
  Modal,
  Row,
  Select,
  Skeleton,
  Space,
  Switch,
  Tooltip,
} from 'antd';
import { connect } from 'dva';
import React, { useEffect } from 'react';
import { useMemo } from 'react';
import type { Dispatch } from 'umi';
import { history } from 'umi';
import type { IFireCriteria, ISystemAlertRule } from '../typings';
import {
  AlertLevelTypeList,
  AlertRefireTimeList,
  EOperandUnit,
  ESystemAlertMetric,
  ESystemAlertSourceType,
  ESystemAlertState,
} from '../typings';
import styles from './index.less';

const layout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 12 },
};

// 硬盘剩余空间监控
const fsSpaceList = [
  {
    value: ESystemAlertMetric.FS_SYSTEM_FREE,
    label: '系统分区',
  },
  {
    value: ESystemAlertMetric.FS_INDEX_FREE,
    label: '索引分区',
  },
  {
    value: ESystemAlertMetric.FS_METADATA_FREE,
    label: '详单冷分区',
  },
  {
    value: ESystemAlertMetric.FS_METADATA_HOT_FREE,
    label: '详单热分区',
  },
  {
    value: ESystemAlertMetric.FS_PACKET_FREE,
    label: '全流量存储分区',
  },
];
const fsSpaceValues = fsSpaceList.map((item) => item.value);

// 分区IO监控
const fsIOList = [
  {
    value: ESystemAlertMetric.FS_SYSTEM_IO,
    label: '系统分区',
  },
  {
    value: ESystemAlertMetric.FS_INDEX_IO,
    label: '索引分区',
  },
  {
    value: ESystemAlertMetric.FS_METADATA_IO,
    label: '详单冷分区',
  },
  {
    value: ESystemAlertMetric.FS_METADATA_HOT_IO,
    label: '详单热分区',
  },
  {
    value: ESystemAlertMetric.FS_PACKET_IO,
    label: '全流量存储分区',
  },
];
const fsIOValues = fsIOList.map((item) => item.value);

const ioUnitList = [
  {
    value: EOperandUnit['MB/S'],
    label: 'MB/秒',
  },
  {
    value: EOperandUnit['MB/MIN'],
    label: 'MB/分钟',
  },
];

interface ISystemAlertRuleProps {
  dispatch: Dispatch;
  match: {
    params: { id: string };
  };
  systemAlertRuleDetail: ISystemAlertRule;
  submitLoading?: boolean;
  queryLoading?: boolean;
}
const SystemAlertRule: React.FC<ISystemAlertRuleProps> = ({
  systemAlertRuleDetail,
  match,
  dispatch,
  submitLoading,
  queryLoading = true,
}) => {
  const [form] = Form.useForm();
  const { id } = match.params;

  useEffect(() => {
    dispatch({
      type: 'systemAlertModel/querySystemAlertRuleDetail',
      payload: {
        id,
      },
    });
    return () => {
      dispatch({
        type: 'systemAlertModel/updateState',
        payload: {
          systemAlertRuleDetail: {},
        },
      });
    };
  }, [dispatch, id]);

  // 初始化数据
  const { fireCriteria = [], sourceType } = systemAlertRuleDetail;
  // 分区空间监控
  const isDistSpaceAlert = useMemo(() => sourceType === ESystemAlertSourceType.DISK, [sourceType]);
  // IO监控
  const isIOAlert = useMemo(() => sourceType === ESystemAlertSourceType.IO, [sourceType]);

  const handleSubmit = (values: any) => {
    const fireCriteriaList: IFireCriteria[] = [];
    if (sourceType === ESystemAlertSourceType.CPU) {
      fireCriteriaList.push({
        metric: ESystemAlertMetric.CPU_USAGE,
        operand: values[`operand.${ESystemAlertMetric.CPU_USAGE}`],
        operator: '>',
        operand_unit: EOperandUnit.PERCENT,
      });
    }
    if (sourceType === ESystemAlertSourceType.MEMORY) {
      fireCriteriaList.push({
        metric: ESystemAlertMetric.MEMORY_USAGE,
        operand: values[`operand.${ESystemAlertMetric.MEMORY_USAGE}`],
        operator: '>',
        operand_unit: EOperandUnit.PERCENT,
      });
    }
    if (sourceType === ESystemAlertSourceType.DISK) {
      const { fsMetrics = [] } = values;
      if (fsMetrics.length > 0) {
        fsMetrics.forEach((metric: ESystemAlertMetric) => {
          const obj = {
            metric,
            operand: values[`operand.${metric}`],
            operator: '<',
            operand_unit: EOperandUnit.MB,
          };
          fireCriteriaList.push(obj);
        });
      }
    }
    if (sourceType === ESystemAlertSourceType.IO) {
      const { fsMetrics = [] } = values;
      if (fsMetrics.length > 0) {
        fsMetrics.forEach((metric: ESystemAlertMetric) => {
          const obj = {
            metric,
            operand: values[`operand.${metric}`],
            operator: '>',
            operand_unit: values[`operandUnit.${metric}`],
          };
          fireCriteriaList.push(obj);
        });
      }
    }

    const postValue = {
      ...systemAlertRuleDetail,
      state: values.state ? ESystemAlertState.开启 : ESystemAlertState.关闭,
      level: values.level,
      refireSeconds: values.refireSeconds,
      fireCriteria: JSON.stringify(fireCriteriaList),
    };
    Modal.confirm({
      title: '确定保存吗？',
      maskClosable: false,
      keyboard: false,
      onOk: () => {
        dispatch({
          type: 'systemAlertModel/updateSystemAlertRule',
          payload: {
            ...postValue,
          },
        });
      },
    });
  };

  const handleGoback = () => {
    history.goBack();
  };

  if (!id) {
    return null;
  }

  if (queryLoading) {
    return <Skeleton active />;
  }

  if (!systemAlertRuleDetail.id) {
    return null;
  }

  const initOperand = {};
  const initFsMetrics: ESystemAlertMetric[] = [];
  (fireCriteria as IFireCriteria[]).forEach((item) => {
    initOperand[`operand.${item.metric}`] = item.operand;
    if (isIOAlert) {
      initOperand[`operandUnit.${item.metric}`] = item.operand_unit;
    }
    if (isDistSpaceAlert || isIOAlert) {
      initFsMetrics.push(item.metric);
    }
  });

  const alertState = systemAlertRuleDetail.state === ESystemAlertState.开启;
  form.setFieldsValue({
    state: alertState,
  });

  return (
    <Form
      {...layout}
      onFinish={handleSubmit}
      form={form}
      initialValues={{
        ...systemAlertRuleDetail,
        state: alertState,
        ...initOperand,
        fsMetrics: initFsMetrics,
      }}
    >
      <Form.Item
        name="name"
        style={{ marginBottom: 10 }}
        label="名称"
        rules={[
          {
            required: false,
            whitespace: true,
            message: '请填写名称',
          },
          { max: 30, message: '最多可输入30个字符' },
        ]}
      >
        <span className="ant-form-text">{systemAlertRuleDetail.name}</span>
      </Form.Item>
      <Form.Item
        name="state"
        label="状态"
        valuePropName="checked"
        rules={[
          {
            required: false,
            message: '请设置告警状态',
          },
        ]}
      >
        <Switch checkedChildren="开启" unCheckedChildren="关闭" />
      </Form.Item>
      <Form.Item
        name="level"
        label="级别"
        rules={[
          {
            required: true,
            message: '请选择告警级别',
          },
        ]}
      >
        <Select placeholder="请选择告警级别">
          {AlertLevelTypeList.map((item) => (
            <Select.Option key={item.value} value={item.value}>
              {item.label}
            </Select.Option>
          ))}
        </Select>
      </Form.Item>
      {sourceType === ESystemAlertSourceType.CPU && (
        <Form.Item label="告警阈值" required key={sourceType}>
          <span className="ant-form-text">CPU使用率大于</span>
          <Form.Item
            label="告警阈值"
            name={`operand.${ESystemAlertMetric.CPU_USAGE}`}
            required
            noStyle
            rules={[
              {
                required: true,
                message: '请填写告警阈值',
              },
            ]}
          >
            <InputNumber min={1} max={100} precision={0} />
          </Form.Item>
          <span className="ant-form-text">%</span>
        </Form.Item>
      )}
      {sourceType === ESystemAlertSourceType.MEMORY && (
        <Form.Item label="告警阈值" required>
          <span className="ant-form-text">内存使用率大于</span>
          <Form.Item
            label="告警阈值"
            name={`operand.${ESystemAlertMetric.MEMORY_USAGE}`}
            required
            noStyle
            rules={[
              {
                required: true,
                message: '请填写告警阈值',
              },
            ]}
          >
            <InputNumber min={1} max={100} precision={0} />
          </Form.Item>
          <span className="ant-form-text">%</span>
        </Form.Item>
      )}
      {isIOAlert && (
        <Form.Item label="监控分区" key={sourceType}>
          <Row>
            <Col span={6}>
              <Form.Item
                noStyle
                name="fsMetrics"
                label="分区"
                dependencies={['state']}
                rules={[
                  {
                    validator: async (rule, value) => {
                      const state = form.getFieldValue('state');
                      // 告警开启并且没有选择监控分区时，提示错误
                      if (state && value.length === 0) {
                        throw new Error('请选择监控分区');
                      }
                    },
                  },
                ]}
              >
                <Checkbox.Group>
                  <Row>
                    {fsIOList.map((fsName) => (
                      <Col key={fsName.value} span={24} className={styles.fsCol}>
                        <Checkbox key={fsName.value} value={fsName.value}>
                          {fsName.label}
                        </Checkbox>
                      </Col>
                    ))}
                  </Row>
                </Checkbox.Group>
              </Form.Item>
            </Col>
            <Col span={18}>
              <Form.Item noStyle shouldUpdate>
                {({ getFieldValue }: any) => {
                  const fsMetrics = getFieldValue('fsMetrics') || [];
                  return fsIOValues.map((fsId) => {
                    return fsMetrics.indexOf(fsId) > -1 ? (
                      <Space className={styles.fsCol} key={fsId}>
                        <Form.Item noStyle>
                          <span className="ant-form-text">IO大于</span>
                          <Form.Item
                            noStyle
                            name={`operand.${fsId}`}
                            label="分区阈值"
                            rules={[
                              {
                                required: true,
                                message: '请填写分区阈值',
                              },
                            ]}
                          >
                            <InputNumber min={1} precision={0} style={{ width: 200 }} />
                          </Form.Item>
                          <Form.Item
                            noStyle
                            name={`operandUnit.${fsId}`}
                            label="阈值单位"
                            rules={[
                              {
                                required: true,
                                message: '请选择阈值单位',
                              },
                            ]}
                          >
                            <Select style={{ width: 120, marginLeft: 10 }}>
                              {ioUnitList.map((el) => (
                                <Select.Option key={el.value} value={el.value}>
                                  {el.label}
                                </Select.Option>
                              ))}
                            </Select>
                          </Form.Item>
                        </Form.Item>
                      </Space>
                    ) : (
                      <div className={styles.fsCol} key={fsId} />
                    );
                  });
                }}
              </Form.Item>
            </Col>
          </Row>
        </Form.Item>
      )}
      {isDistSpaceAlert && (
        <Form.Item label="监控分区" key={sourceType}>
          <Row>
            <Col span={6}>
              <Form.Item
                noStyle
                name="fsMetrics"
                label="分区"
                dependencies={['state']}
                rules={[
                  {
                    validator: async (rule, value) => {
                      const state = form.getFieldValue('state');
                      // 告警开启并且没有选择监控分区时，提示错误
                      if (state && value.length === 0) {
                        throw new Error('请选择监控分区');
                      }
                    },
                  },
                ]}
              >
                <Checkbox.Group>
                  <Row>
                    {fsSpaceList.map((fsName) => (
                      <Col key={fsName.value} span={24} className={styles.fsCol}>
                        <Checkbox key={fsName.value} value={fsName.value}>
                          {fsName.label}
                        </Checkbox>
                      </Col>
                    ))}
                  </Row>
                </Checkbox.Group>
              </Form.Item>
            </Col>
            <Col span={18}>
              <Form.Item noStyle shouldUpdate>
                {({ getFieldValue }: any) => {
                  const fsMetrics = getFieldValue('fsMetrics') || [];
                  return (
                    <Row>
                      {fsSpaceValues.map((fsId) => {
                        return (
                          <Col span={24} className={styles.fsCol} key={fsId}>
                            {fsMetrics.indexOf(fsId) > -1 ? (
                              <Form.Item noStyle>
                                <span className="ant-form-text">剩余空间小于</span>
                                <Form.Item
                                  noStyle
                                  name={`operand.${fsId}`}
                                  label="分区阈值"
                                  rules={[
                                    {
                                      required: true,
                                      message: '请填写分区阈值',
                                    },
                                  ]}
                                >
                                  <InputNumber min={1} precision={0} style={{ width: 200 }} />
                                </Form.Item>
                                <span className="ant-form-text">
                                  {EOperandUnit.MB.toLocaleUpperCase()}
                                </span>
                                <span className="ant-form-text">
                                  <Tooltip title="1GB = 1,000MB">
                                    <InfoCircleOutlined />
                                  </Tooltip>
                                </span>
                              </Form.Item>
                            ) : null}
                          </Col>
                        );
                      })}
                    </Row>
                  );
                }}
              </Form.Item>
            </Col>
          </Row>
        </Form.Item>
      )}
      <Form.Item
        name="refireSeconds"
        label="告警间隔"
        rules={[
          {
            required: true,
            message: '请选择告警间隔',
          },
        ]}
      >
        <Select placeholder="请选择告警间隔">
          {AlertRefireTimeList.map((item) => (
            <Select.Option key={item.value} value={item.value}>
              {item.label}
            </Select.Option>
          ))}
        </Select>
      </Form.Item>

      <Form.Item wrapperCol={{ span: 12, offset: 4 }}>
        <Button
          style={{ marginRight: 10 }}
          type="primary"
          htmlType="submit"
          loading={submitLoading}
        >
          保存
        </Button>
        <Button onClick={() => handleGoback()}>返回</Button>
      </Form.Item>
    </Form>
  );
};

export default connect(
  ({ loading: { effects }, systemAlertModel: { systemAlertRuleDetail } }: ConnectState) => ({
    submitLoading: effects['systemAlertModel/updateSystemAlertRule'],
    queryLoading: effects['systemAlertModel/querySystemAlertRuleDetail'],
    systemAlertRuleDetail,
  }),
)(SystemAlertRule);
