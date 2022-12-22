import { randomSecret } from '@/utils/utils';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import type { FormComponentProps } from '@ant-design/compatible/es/form';
import { Button, Card, Col, Input, Modal, Row, Skeleton } from 'antd';
import React, { PureComponent } from 'react';
import type { Dispatch } from 'umi';
import { connect, history } from 'umi';
import type { ISsoModelStateType } from '../../model';
import type { ISsoPlatform } from '../../typings';

const FormItem = Form.Item;
const { TextArea } = Input;

const formLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 16 },
};

interface IUpdatePlatformProps extends FormComponentProps {
  dispatch: Dispatch<any>;
  location: {
    query: {
      id: string;
    };
  };
  ssoPlatformDetail: ISsoPlatform;
  queryDetailLoading: boolean;
  updateLoading: boolean;
}
interface IUpdatePlatformState {}

class UpdatePlatform extends PureComponent<IUpdatePlatformProps, IUpdatePlatformState> {
  componentDidMount() {
    const {
      dispatch,
      location: { query },
    } = this.props;
    dispatch({
      type: 'ssoModel/querySsoPlatformDetail',
      payload: { id: query.id },
    });
  }
  // 提交
  handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    const { form } = this.props;
    e.preventDefault();
    form.validateFieldsAndScroll((err, fieldsValue) => {
      if (err) return;
      Modal.confirm({
        title: '确定修改吗?',
        cancelText: '取消',
        okText: '确定',
        onOk: () => {
          this.props
            .dispatch({
              type: 'ssoModel/updateSsoPlatform',
              payload: fieldsValue,
            })
            .then((result: any) => {
              if (result) {
                Modal.success({
                  keyboard: false,
                  title: '修改成功',
                  okText: '返回列表页',
                  onOk: () => {
                    this.handleGoListPage();
                  },
                });
              }
            });
        },
      });
    });
  };

  handleRandomToken = () => {
    const { form } = this.props;
    form.setFieldsValue({
      appToken: randomSecret(),
    });
  };

  handleGoListPage = () => {
    history.goBack();
  };

  handleReset = () => {
    const { form } = this.props;
    form.resetFields();
  };

  render() {
    const {
      form,
      updateLoading,
      queryDetailLoading,
      ssoPlatformDetail: detail = {} as ISsoPlatform,
    } = this.props;
    return (
      <Card bordered={false}>
        <Skeleton active loading={queryDetailLoading}>
          <Form onSubmit={this.handleSubmit}>
            <FormItem key="id" {...formLayout} label="id" style={{ display: 'none' }}>
              {form.getFieldDecorator('id', {
                initialValue: detail.id,
              })(<Input placeholder="请输入" />)}
            </FormItem>
            <FormItem key="name" {...formLayout} label="外部系统名称">
              {form.getFieldDecorator('name', {
                initialValue: detail.name,
                rules: [
                  { required: true, whitespace: true, message: '名称不能为空' },
                  {
                    max: 30,
                    message: '最多可输入30个字符',
                  },
                ],
              })(<Input placeholder="外部系统名称" />)}
            </FormItem>
            <FormItem key="platformId" {...formLayout} label="外部系统ID">
              {form.getFieldDecorator('platformId', {
                initialValue: detail.platformId,
                rules: [
                  { required: true, whitespace: true, message: '系统ID不能为空' },
                  {
                    max: 64,
                    message: '最多可输入64个字符',
                  },
                ],
              })(<Input placeholder="外部系统ID" />)}
            </FormItem>
            <FormItem key="appToken" {...formLayout} label="Token" extra="不填写即表示不更新">
              <Row gutter={10}>
                <Col span={20}>
                  {form.getFieldDecorator('appToken', {
                    initialValue: '',
                    rules: [
                      { required: false, whitespace: true, message: 'Token' },
                      {
                        pattern: /^[a-zA-Z0-9-_@]+$/,
                        message: '只允许输入字母、数字、中划线-、下划线_ 和 @ 组合',
                      },
                      {
                        min: 10,
                        message: '最少输入10个字符',
                      },
                      {
                        max: 32,
                        message: '最多可输入32个字符',
                      },
                    ],
                  })(<Input allowClear placeholder="********" />)}
                </Col>
                <Col span={4}>
                  <Button onClick={this.handleRandomToken} type="primary" block>
                    随机生成
                  </Button>
                </Col>
              </Row>
            </FormItem>
            <FormItem key="description" {...formLayout} label="备注">
              {form.getFieldDecorator('description', {
                initialValue: detail.description || '',
                rules: [
                  { required: false, message: '请输入备注' },
                  { max: 255, message: '最多可输入255个字符' },
                ],
              })(<TextArea rows={4} placeholder="备注" />)}
            </FormItem>
            <FormItem wrapperCol={{ span: 12, offset: 4 }}>
              <Button
                style={{ marginRight: 10 }}
                type="primary"
                htmlType="submit"
                loading={updateLoading}
              >
                保存
              </Button>
              <Button onClick={() => this.handleGoListPage()}>返回</Button>
            </FormItem>
          </Form>
        </Skeleton>
      </Card>
    );
  }
}

export default connect(
  ({
    loading,
    ssoModel: { ssoPlatformDetail },
  }: {
    ssoModel: ISsoModelStateType;
    loading: { effects: Record<string, boolean> };
  }) => ({
    ssoPlatformDetail,
    queryDetailLoading: loading.effects['ssoModel/querySsoPlatformDetail'],
    updateLoading: loading.effects['ssoModel/updateSsoPlatform'],
  }),
)(Form.create<IUpdatePlatformProps>()(UpdatePlatform));
