/* eslint-disable no-restricted-globals */
/* eslint-disable no-throw-literal */
import { getLinkUrl } from '@/utils/utils';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { CheckCircleTwoTone, PlusOutlined } from '@ant-design/icons';
import { Button, DatePicker, Divider, Drawer, Input, Modal, Select } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import PropTypes from 'prop-types';
import React, { Fragment, PureComponent } from 'react';
import { history } from 'umi';
import TemplateProfile from '../ScenarioTemplateProfile';

const FormItem = Form.Item;
const { Option, OptGroup } = Select;

const formLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 16 },
};

/**
 * 分析数据时间跨度限制
 */
const MAX_DATA_RANGE_HOURS = 24;

/**
 * 分析场景：beacon
 */
export const ANALYSIS_SCENARIO_TYPE_BEACON = 'beacon-detection';
/**
 * 分析场景：非标准协议
 */
export const ANALYSIS_SCENARIO_TYPE_NONSTANDARD_PROTOCOL = 'nonstandard-protocol';
/**
 * 分析场景：动态域名
 */
export const ANALYSIS_SCENARIO_TYPE_DYNAMIC_DOMAIN = 'dynamic-domain';

/**
 * 分析场景：可疑https
 */
export const ANALYSIS_SCENARIO_TYPE_SUSPICIOUS_HTTPS = 'suspicious-https';
/**
 * 分析场景：IP情报
 */
export const ANALYSIS_SCENARIO_TYPE_IP_INTELLIGENCE = 'intelligence-ip';
/**
 * 分析场景：SSH暴破
 */
export const ANALYSIS_SCENARIO_TYPE_BRUTE_FORCE_SSH = 'brute-force-ssh';
/**
 * 分析场景：RDP暴破
 */
export const ANALYSIS_SCENARIO_TYPE_BRUTE_FORCE_RDP = 'brute-force-rdp';

/**
 * 自定义模板ID的前缀
 */
export const CUSTOM_TEMPLATE_PREFIX = 'custom_';

/**
 * 分析场景默认模板
 */
export const SCENARIO_TASK_DEFAULT_TEMPLATE_LIST = [
  {
    value: ANALYSIS_SCENARIO_TYPE_BEACON,
    label: 'Beacon',
  },
  {
    value: ANALYSIS_SCENARIO_TYPE_NONSTANDARD_PROTOCOL,
    label: '非标准协议',
  },
  {
    value: ANALYSIS_SCENARIO_TYPE_DYNAMIC_DOMAIN,
    label: '动态域名',
  },
  {
    value: ANALYSIS_SCENARIO_TYPE_SUSPICIOUS_HTTPS,
    label: '可疑https',
  },
  {
    value: ANALYSIS_SCENARIO_TYPE_IP_INTELLIGENCE,
    label: 'IP情报',
  },
  {
    value: ANALYSIS_SCENARIO_TYPE_BRUTE_FORCE_SSH,
    label: 'SSH暴破',
  },
  {
    value: ANALYSIS_SCENARIO_TYPE_BRUTE_FORCE_RDP,
    label: 'RDP暴破',
  },
];

export const getAnalysisScenarioTypeInfo = (scenarioType) => {
  const unknow = {
    value: scenarioType,
    label: '未知',
  };

  if (!scenarioType) {
    return unknow;
  }

  return SCENARIO_TASK_DEFAULT_TEMPLATE_LIST.find((type) => type.value === scenarioType) || unknow;
};

@Form.create()
@connect(({ scenarioTaskModel: { scenarioCustomTemplateList }, loading: { effects } }) => ({
  scenarioCustomTemplateList,
  queryAllCustomTemplateLoading: effects['scenarioTaskModel/queryAllScenarioCustomTemplates'],
}))
class ScenarioTaskForm extends PureComponent {
  static propTypes = {
    submitLoading: PropTypes.bool,
    operateType: PropTypes.oneOf(['CREATE', 'UPDATE']),
  };

  static defaultProps = {
    submitLoading: false,
    operateType: 'CREATE',
  };

  state = {
    customTemplateId: '',
  };

  componentDidMount() {
    this.queryScenarioCustomTemplate();
  }

  queryScenarioCustomTemplate = async () => {
    const { dispatch } = this.props;
    await dispatch({
      type: 'scenarioTaskModel/queryAllScenarioCustomTemplates',
      payload: { isDetail: false },
    });
  };

  // 校验开始时间
  checkFilterStartTime = (rule, value, callback) => {
    const { form } = this.props;
    // 截止时间
    const analysisEndTime = form.getFieldValue('analysisEndTime');
    // 重新触发截止时间的校验
    if (value && analysisEndTime) {
      form.validateFields(['analysisEndTime'], { force: true });
    }

    if (value) {
      if (analysisEndTime && moment(value).isAfter(moment(analysisEndTime), 'second')) {
        callback('开始时间不能晚于截止时间！');
        return;
      }
    }
    callback();
  };

  // 校验截止时间
  checkFilterEndTime = (rule, value, callback) => {
    const { form } = this.props;
    // 截止时间
    const analysisStartTime = form.getFieldValue('analysisStartTime');
    if (value) {
      if (analysisStartTime && moment(value).isBefore(moment(analysisStartTime), 'second')) {
        callback('截止时间不能早于开始时间！');
        return;
      }
      // 判断时间范围不能超过24小时
      if (moment.duration(moment(value).diff(moment(analysisStartTime))).as('hours') > 24) {
        callback(`分析数据时间范围不能超过${MAX_DATA_RANGE_HOURS}小时`);
        return;
      }
    }
    callback();
  };

  // 提交
  handleSubmit = (e) => {
    const { form, operateType } = this.props;
    e.preventDefault();
    form.validateFieldsAndScroll((err, fieldsValue) => {
      if (err) return;

      const { analysisStartTime, analysisEndTime } = fieldsValue;

      const values = {
        ...fieldsValue,
        analysisStartTime: moment(analysisStartTime).format(),
        analysisEndTime: moment(analysisEndTime).format(),
      };
      if (operateType === 'CREATE') {
        this.handleCreate(values);
      }
    });
  };

  handleGoListPage = () => {
    history.goBack();
  };

  handleReset = () => {
    const { form } = this.props;
    form.resetFields();
  };

  handleCreate = (values) => {
    const { dispatch } = this.props;
    Modal.confirm({
      title: '确定执行任务吗？',
      cancelText: '取消',
      okText: '确定',
      onOk: () => {
        dispatch({
          type: 'scenarioTaskModel/createScenarioTask',
          payload: values,
        }).then((result) => {
          if (result) {
            Modal.confirm({
              keyboard: false,
              title: '执行成功',
              icon: <CheckCircleTwoTone size={24} twoToneColor="#52c41a" />,
              cancelText: '继续添加',
              okText: '返回列表页',
              onOk: () => {
                this.handleGoListPage();
              },
              onCancel: () => {
                this.handleReset();
              },
            });
          }
        });
      },
    });
  };

  getCustomTemplateId = () => {
    const {
      form: { getFieldValue },
    } = this.props;

    const selectedType = getFieldValue('type');
    if (!selectedType) {
      return '';
    }
    if (selectedType.indexOf(CUSTOM_TEMPLATE_PREFIX) === -1) {
      return '';
    }
    return selectedType.replace(CUSTOM_TEMPLATE_PREFIX, '');
  };

  handleOpenCustomTemplateDetail = () => {
    this.setState({
      customTemplateId: this.getCustomTemplateId(),
    });
  };

  handleCloseCustomTemplateDetail = () => {
    this.setState({
      customTemplateId: '',
    });
  };

  render() {
    const { form, submitLoading, queryAllCustomTemplateLoading, scenarioCustomTemplateList } =
      this.props;
    const { customTemplateId } = this.state;

    // 禁用日期
    const disabledDate = (current) =>
      current && current.clone().endOf('day') >= moment().add(1, 'day');

    return (
      <Fragment>
        <Form onSubmit={this.handleSubmit}>
          <FormItem key="id" {...formLayout} label="id" style={{ display: 'none' }}>
            {form.getFieldDecorator('id', {})(<Input placeholder="请输入" />)}
          </FormItem>
          <FormItem key="name" {...formLayout} label="任务名称">
            {form.getFieldDecorator('name', {
              rules: [
                { required: true, message: '请输入任务名称' },
                {
                  max: 30,
                  message: '最多可输入30个字符',
                },
              ],
            })(<Input placeholder="请输入任务名称" />)}
          </FormItem>
          <FormItem key="analysisStartTime" {...formLayout} label="分析数据开始时间">
            {form.getFieldDecorator('analysisStartTime', {
              rules: [
                { required: true, message: '请选择分析数据开始时间' },
                {
                  validator: this.checkFilterStartTime,
                },
              ],
            })(
              <DatePicker
                showTime
                showToday={false}
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择分析数据开始时间"
                disabledDate={disabledDate}
                style={{ width: '100%' }}
              />,
            )}
          </FormItem>
          <FormItem key="analysisEndTime" {...formLayout} label="分析数据截止时间">
            {form.getFieldDecorator('analysisEndTime', {
              rules: [
                { required: true, message: '请选择分析数据截止时间' },
                {
                  validator: this.checkFilterEndTime,
                },
              ],
            })(
              <DatePicker
                showTime
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择分析数据截止时间"
                disabledDate={disabledDate}
                style={{ width: '100%' }}
              />,
            )}
          </FormItem>
          <FormItem
            key="type"
            {...formLayout}
            label="分析模板"
            extra={
              this.getCustomTemplateId() && (
                <a onClick={this.handleOpenCustomTemplateDetail}>点击查看模板详情</a>
              )
            }
          >
            {form.getFieldDecorator('type', {
              rules: [{ required: true, message: '请选择分析模板' }],
            })(
              <Select
                placeholder="请选择分析模板"
                loading={queryAllCustomTemplateLoading}
                dropdownRender={(menu) => (
                  <div>
                    {menu}
                    <Divider
                      style={{ marginTop: '2px !important', marginBottom: '2px !important' }}
                    />
                    <Button
                      style={{ display: 'block', margin: '0 auto 10px' }}
                      type="primary"
                      size="small"
                      onMouseDown={(e) => e.preventDefault()}
                      icon={<PlusOutlined />}
                      onClick={() =>
                        history.push(
                          getLinkUrl(
                            '/configuration/safety-analysis/scenario-task-template/create',
                          ),
                        )
                      }
                    >
                      新增自定义分析模板
                    </Button>
                  </div>
                )}
              >
                <OptGroup label="内置模板">
                  {SCENARIO_TASK_DEFAULT_TEMPLATE_LIST.map((temp) => (
                    <Option value={temp.value}>{temp.label}</Option>
                  ))}
                </OptGroup>
                <OptGroup label="自定义模板">
                  {scenarioCustomTemplateList.map((temp) => (
                    <Option value={`${CUSTOM_TEMPLATE_PREFIX}${temp.id}`}>{temp.name}</Option>
                  ))}
                </OptGroup>
              </Select>,
            )}
            {/* 如果是 */}
          </FormItem>
          <FormItem key="description" {...formLayout} label="描述信息">
            {form.getFieldDecorator('description', {
              initialValue: '',
              rules: [
                { required: false, message: '请输入描述信息' },
                { max: 255, message: '最多可输入255个字符' },
              ],
            })(<Input.TextArea rows={4} placeholder="请输入描述信息" />)}
          </FormItem>
          <FormItem wrapperCol={{ span: 12, offset: 4 }}>
            <Button
              style={{ marginRight: 10 }}
              type="primary"
              htmlType="submit"
              loading={submitLoading}
            >
              立即执行
            </Button>
            <Button onClick={() => this.handleGoListPage()}>返回</Button>
          </FormItem>
        </Form>
        <Drawer
          width={600}
          destroyOnClose
          title="自定义模板详情"
          onClose={() => this.handleCloseCustomTemplateDetail()}
          visible={!!customTemplateId}
        >
          {customTemplateId && <TemplateProfile id={customTemplateId} />}
        </Drawer>
      </Fragment>
    );
  }
}

export default ScenarioTaskForm;
