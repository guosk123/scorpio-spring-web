import Box from '@/components/Box';
import timeZone from '@/utils/frame/timezone';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Button, Card, DatePicker, Input, message, Radio, Select, Skeleton } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import ConnectCmsState from '@/pages/app/configuration/components/ConnectCmsState';
import React, { PureComponent } from 'react';

const FormItem = Form.Item;
const { Option } = Select;

@Form.create()
@connect((state) => {
  const { deviceNTPModel, loading } = state;
  const { effects } = loading;
  return {
    ntpInfo: deviceNTPModel.ntpInfo,
    queryLoading: effects['deviceNTPModel/queryDeviceNtps'],
    updateLoading: effects['deviceNTPModel/updateDeviceNtps'],
  };
})
class DeviceNTP extends PureComponent {
  state = {
    running: false,
    cmsConnectFlag: false,
  };

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'deviceNTPModel/queryDeviceNtps',
    });
  }

  // 消除定时
  componentWillUnmount() {
    if (this.timer) {
      clearInterval(this.timer);
    }
    if (this.updateStatePolling) {
      clearInterval(this.updateStatePolling);
    }
  }

  // 设置日期时间
  setTimeValue = (time) => {
    const { form } = this.props;
    if (this.timer) {
      clearInterval(this.timer);
    }
    // 这里会出现 'Warning: You cannot set a form field before rendering a field associated with the value.'
    // @see: https://github.com/ant-design/ant-design/issues/8880
    form.setFieldsValue({
      dateTime: time,
    });

    this.timer = setInterval(this.setTimeValueInterval, 1000);
  };

  // 定时刷新日期时间
  setTimeValueInterval = () => {
    const { form } = this.props;
    const time = form.setFieldsValue('dateTime');
    const newTime = moment(time).add(1, 's');

    this.setTimeValue(newTime);
  };

  stopPolling = () => {
    clearInterval(this.updateStatePolling);
    this.setState({
      running: false,
    });
  };

  // 获取更新状态
  queryUpdateStatePolling = (id) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'deviceNTPModel/queryDeviceNtpState',
      payload: {
        id,
      },
    }).then(({ success, result = '' }) => {
      if (!success) {
        this.stopPolling();
        message.error('查询NTP更新状态失败');
        return;
      }

      const [state, errMsg] = result.split(':');
      switch (state) {
        case 'success':
          this.stopPolling();
          message.success('修改成功');
          break;
        case 'fail':
          this.stopPolling();
          message.error(errMsg || '修改失败');
          break;
        case 'running':
          break;
        default:
          this.stopPolling();
          break;
      }
    });
  };

  handleSubmit = (e) => {
    e.preventDefault();
    const { form, dispatch } = this.props;
    form.validateFields((err, fields) => {
      if (!err) {
        this.setState({
          running: true,
        });

        const { dateTime, ntpEnabled } = fields;
        const values = {
          ...fields,
        };
        // ntp可用时，清空手动设置
        if (ntpEnabled) {
          values.timeZone = '';
          values.dateTime = '';
        } else {
          values.ntpServer = '';
          values.dateTime = moment(dateTime).format('YYYY-MM-DD HH:mm:ss');
        }
        dispatch({
          type: 'deviceNTPModel/updateDeviceNtps',
          payload: values,
        }).then(({ success, result }) => {
          if (!success) {
            // 停止 loading
            this.setState({
              running: false,
            });
            return;
          }

          // 如果有任务 id，轮询取修改状态
          if (result.id) {
            this.queryUpdateStatePolling(result.id);
            this.updateStatePolling = setInterval(
              () => this.queryUpdateStatePolling(result.id),
              1000,
            );
          } else {
            this.setState({
              running: false,
            });
            message.warning('更新时间发生冲突，请稍候再试。');
          }
        });
      }
    });
  };

  render() {
    const { running, cmsConnectFlag } = this.state;
    const { form, ntpInfo, queryLoading } = this.props;
    const { getFieldDecorator, getFieldValue } = form;
    const formItemLayout = {
      labelCol: { span: 4 },
      wrapperCol: { span: 10 },
    };

    return (
      <Card bordered={false}>
        <ConnectCmsState
          onConnectFlag={(flag) => {
            this.setState({
              cmsConnectFlag: flag,
            });
          }}
        />
        <Skeleton active loading={queryLoading}>
          <Form onSubmit={this.handleSubmit}>
            <Form.Item
              {...formItemLayout}
              label="设置方式"
              extra={
                getFieldValue('ntpEnabled') ? '设置NTP服务器，自动同步时间' : '请手动设置时区和时间'
              }
            >
              {getFieldDecorator('ntpEnabled', {
                initialValue: !!ntpInfo.ntpEnabled,
              })(
                <Radio.Group>
                  <Radio value={false}>手动设置</Radio>
                  <Radio value>NTP服务器同步</Radio>
                </Radio.Group>,
              )}
            </Form.Item>
            <Box title="手动设置">
              <FormItem {...formItemLayout} label="时区">
                {getFieldDecorator('timeZone', {
                  initialValue: ntpInfo.timeZone,
                  rules: [{ required: false, message: '请选择时区!' }],
                })(
                  <Select
                    placeholder="请选择时区"
                    allowClear
                    disabled={getFieldValue('ntpEnabled')}
                  >
                    {timeZone.map((item) => (
                      <Option
                        key={item.key}
                        value={item.key}
                        title={`${item.key}   [${item.title}]`}
                      >
                        {item.key}&nbsp;&nbsp;&nbsp;[{item.title}]
                      </Option>
                    ))}
                  </Select>,
                )}
              </FormItem>
              <FormItem {...formItemLayout} label="日期">
                {getFieldDecorator('dateTime', {
                  initialValue: moment(ntpInfo.dateTime),
                  rules: [{ required: false, message: '请选择日期!' }],
                })(
                  <DatePicker
                    style={{ width: '100%' }}
                    allowClear
                    showTime
                    format="YYYY-MM-DD HH:mm:ss"
                    disabled={getFieldValue('ntpEnabled')}
                  />,
                )}
              </FormItem>
            </Box>
            <Box title="NTP服务器同步">
              <FormItem {...formItemLayout} label="NTP服务器">
                {getFieldDecorator('ntpServer', {
                  initialValue: ntpInfo.ntpServer,
                  rules: [
                    { required: false, message: '请填写NTP服务器!' },
                    { max: 128, message: '最多输入128个字符!' },
                  ],
                })(
                  <Input
                    placeholder="NTP服务器的IP或域名"
                    disabled={!getFieldValue('ntpEnabled')}
                  />,
                )}
              </FormItem>
              <FormItem {...formItemLayout} label="NTP服务器(备用)">
                {getFieldDecorator('slaveNtpServer', {
                  initialValue: ntpInfo.slaveNtpServer,
                  rules: [
                    { required: false, message: '请填写NTP服务器!' },
                    { max: 128, message: '最多输入128个字符!' },
                  ],
                })(
                  <Input
                    placeholder="NTP服务器的IP或域名"
                    disabled={!getFieldValue('ntpEnabled')}
                  />,
                )}
              </FormItem>
            </Box>
            <FormItem wrapperCol={{ span: 12, offset: 4 }}>
              <Button
                type="primary"
                htmlType="submit"
                style={{ marginRight: 10 }}
                loading={running}
                disabled={cmsConnectFlag}
              >
                保存
              </Button>
            </FormItem>
          </Form>
        </Skeleton>
      </Card>
    );
  }
}

export default DeviceNTP;
