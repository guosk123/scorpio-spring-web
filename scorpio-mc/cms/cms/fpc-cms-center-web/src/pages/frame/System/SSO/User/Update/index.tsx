import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import type { FormComponentProps } from '@ant-design/compatible/es/form';
import { Badge, Button, Card, Input, Modal, Select, Skeleton } from 'antd';
import React, { Fragment, PureComponent } from 'react';
import type { Dispatch } from 'umi';
import { connect, history } from 'umi';
import type { ISsoModelStateType } from '../../model';
import type { ISsoPlatform, ISsoUser, ISystemUser } from '../../typings';
import { USER_LOCKED_FALSE, USER_LOCKED_TRUE } from '../Create';

const FormItem = Form.Item;
const { TextArea } = Input;

const formLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 16 },
};

interface IUpdateSsoUserProps extends FormComponentProps {
  dispatch: Dispatch<any>;
  location: {
    query: {
      id: string;
    };
  };
  ssoUserDetail: ISsoUser;
  allSsoPlatforms: ISsoPlatform[];
  allSystemUsers: ISystemUser[];
  quaryLoading: boolean;
  queryDetailLoading: boolean;
  updateLoading: boolean;
}
interface IUpdateSsoUserState {}

class UpdateSsoUser extends PureComponent<IUpdateSsoUserProps, IUpdateSsoUserState> {
  componentDidMount() {
    const {
      dispatch,
      location: { query },
    } = this.props;
    dispatch({
      type: 'ssoModel/queryAllSsoPlatforms',
    });
    dispatch({
      type: 'ssoModel/queryAllSystemUsers',
    });
    dispatch({
      type: 'ssoModel/querySsoUserDetail',
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
              type: 'ssoModel/updateSsoUser',
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
      quaryLoading,
      updateLoading,
      queryDetailLoading,
      ssoUserDetail: detail = {} as ISsoUser,
      allSsoPlatforms = [],
      allSystemUsers = [],
    } = this.props;
    return (
      <Card bordered={false}>
        <Skeleton active loading={queryDetailLoading || quaryLoading}>
          <Form onSubmit={this.handleSubmit}>
            <FormItem key="id" {...formLayout} label="id" style={{ display: 'none' }}>
              {form.getFieldDecorator('id', {
                initialValue: detail.id,
              })(<Input placeholder="请输入" />)}
            </FormItem>
            <FormItem key="ssoPlatformId" {...formLayout} label="外部系统">
              {form.getFieldDecorator('ssoPlatformId', {
                initialValue: detail.ssoPlatformId,
                rules: [{ required: true, whitespace: true, message: '外部系统不能为空' }],
              })(
                <Select placeholder="请选择外部系统" showSearch>
                  {allSsoPlatforms.map((platform) => (
                    <Select.Option value={platform.id as any}>{platform.name}</Select.Option>
                  ))}
                </Select>,
              )}
            </FormItem>
            <FormItem key="platformUserId" {...formLayout} label="外部用户ID">
              {form.getFieldDecorator('platformUserId', {
                initialValue: detail.platformUserId,
                rules: [
                  { required: true, whitespace: true, message: '外部用户ID不能为空' },
                  {
                    max: 64,
                    message: '最多可输入64个字符',
                  },
                ],
              })(<Input placeholder="请输入外部用户ID" />)}
            </FormItem>
            <FormItem key="systemUserId" {...formLayout} label="本系统用户">
              {form.getFieldDecorator('systemUserId', {
                initialValue: detail.systemUserId,
                rules: [{ required: false, whitespace: true, message: 'Token' }],
              })(
                <Select placeholder="请选择本系统用户" showSearch>
                  {allSystemUsers.map((user) => (
                    <Select.Option value={user.id} disabled={user.locked === USER_LOCKED_TRUE}>
                      {/* TODO: 展示用户权限 */}
                      <Fragment>
                        <Badge status={user.locked === USER_LOCKED_FALSE ? 'success' : 'error'} />
                        {user.fullname}
                      </Fragment>
                    </Select.Option>
                  ))}
                </Select>,
              )}
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
    loading: { effects },
    ssoModel: { ssoUserDetail, allSsoPlatforms, allSystemUsers },
  }: {
    ssoModel: ISsoModelStateType;
    loading: { effects: Record<string, boolean> };
  }) => ({
    ssoUserDetail,
    allSsoPlatforms,
    allSystemUsers,
    quaryLoading:
      effects['ssoModel/queryAllSsoPlatforms'] || effects['ssoModel/queryAllSystemUsers'],
    queryDetailLoading: effects['ssoModel/querySsoUserDetail'],
    updateLoading: effects['ssoModel/updateSsoUser'],
  }),
)(Form.create<IUpdateSsoUserProps>()(UpdateSsoUser));
