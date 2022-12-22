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
  Switch,
  Tooltip,
} from 'antd';
import { connect } from 'dva';
import React, { useEffect } from 'react';
import type { Dispatch } from 'umi';
import { history } from 'umi';
import type { ISystemAlertRule } from '../typings';
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

const diskList = [
  {
    value: ESystemAlertMetric.FS_SYSTEM_FREE,
    label: '系统分区',
  },
];

const diskValues = diskList.map((item) => item.value);

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

  const handleSubmit = (values: any) => {
    const { sourceType } = systemAlertRuleDetail;
    const fireCriteria = [];
    if (sourceType === ESystemAlertSourceType.CPU) {
      fireCriteria.push({
        metric: ESystemAlertMetric.CPU_USAGE,
        operand: values[`operand.${ESystemAlertMetric.CPU_USAGE}`],
        operator: '>',
        operand_unit: EOperandUnit.PERCENT,
      });
    }
    if (sourceType === ESystemAlertSourceType.MEMORY) {
      fireCriteria.push({
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
          fireCriteria.push(obj);
        });
      }
    }

    const postValue = {
      ...systemAlertRuleDetail,
      state: values.state ? ESystemAlertState.开启 : ESystemAlertState.关闭,
      level: values.level,
      refireSeconds: values.refireSeconds,
      fireCriteria: JSON.stringify(fireCriteria),
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

  // 初始化数据
  const { fireCriteria = [], sourceType } = systemAlertRuleDetail;
  const initOperand = {};
  const initFsMetrics: ESystemAlertMetric[] = [];
  const isDistAlert = sourceType === ESystemAlertSourceType.DISK;
  fireCriteria.forEach((item) => {
    initOperand[`operand.${item.metric}`] = item.operand;
    if (isDistAlert) {
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
      {isDistAlert && (
        <Form.Item label="监控分区">
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
                    {diskList.map((fsName) => (
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
                      {diskValues.map((item) => {
                        return (
                          <Col span={24} className={styles.fsCol} key={item}>
                            {fsMetrics.indexOf(item) > -1 ? (
                              <Form.Item noStyle>
                                <span className="ant-form-text">剩余空间小于</span>
                                <Form.Item
                                  noStyle
                                  name={`operand.${item}`}
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
                                <span className="ant-form-text">MB</span>
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
