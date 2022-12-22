import { createConfirmModal, randomSecret } from '@/utils/utils';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import type { FormComponentProps } from '@ant-design/compatible/es/form';
import { MinusCircleOutlined, PlusOutlined } from '@ant-design/icons';
import { Badge, Button, Card, Col, Input, notification, Row, Select, Skeleton } from 'antd';
import { connect } from 'dva';
import hash from 'hash.js';
import _ from 'lodash';
import React, { Fragment, PureComponent } from 'react';
import type { Dispatch } from 'redux';
import { history } from 'umi';
import type { ISsoPlatform, ISsoUser, ISystemUser } from '../../typings';
import { EUSERTYPE } from '../../typings';
import type { ISsoModelStateType } from '../../model';

const FormItem = Form.Item;
const { TextArea } = Input;
const { OptGroup } = Select;

export const USER_LOCKED_TRUE = '1';
export const USER_LOCKED_FALSE = '0';

// 分隔符
const SEPARATOR = '$_o_$_';
let itemNo = 0;

const platformSpan = 5;
const platformUserSpan = 5;
const systemUserSpan = 6;
const descSpan = 6;
const formTitle = (
  <Row gutter={4}>
    <Col style={{ textAlign: 'center' }} span={1}>
      #
    </Col>
    <Col style={{ textAlign: 'center' }} span={platformSpan}>
      外部系统
      <label className="ant-form-item-required" />
    </Col>
    <Col style={{ textAlign: 'center' }} span={platformUserSpan}>
      外部用户ID
      <label className="ant-form-item-required" />
    </Col>
    <Col style={{ textAlign: 'center' }} span={systemUserSpan}>
      本系统用户
      <label className="ant-form-item-required" />
    </Col>
    <Col style={{ textAlign: 'center' }} span={descSpan}>
      备注
    </Col>
    <Col style={{ textAlign: 'center' }} span={1} />
  </Row>
);

/**
 * 计算每条规则的item的key值
 * 如果不是字符串，转成hash字符串
 * @param {Object} key
 */
const convertRuleItemKey = (key: any) => {
  if (typeof key === 'string') {
    return key;
  }
  return hash.sha256().update(JSON.stringify(key)).digest('hex');
};

interface ICreateSsoUserProps extends FormComponentProps {
  dispatch: Dispatch<any>;
  allSsoPlatforms: ISsoPlatform[];
  allSystemUsers: ISystemUser[];
  createLoading: boolean;
  quaryLoading: boolean;
}
interface ICreateSsoUserState {}

class CreateSsoUser extends PureComponent<ICreateSsoUserProps, ICreateSsoUserState> {
  createConfirmModal: (params: any) => void;
  constructor(props: ICreateSsoUserProps) {
    super(props);
    this.createConfirmModal = createConfirmModal.bind(this);
  }
  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'ssoModel/queryAllSsoPlatforms',
    });
    dispatch({
      type: 'ssoModel/queryAllSystemUsers',
    });
  }
  handleCategoryChange = () => {
    const { form } = this.props;
    form.setFieldsValue({
      subCategoryId: undefined,
    });
  };

  addFormItem = () => {
    const { form } = this.props;
    const formItemKeys = form.getFieldValue('formItemKeys');
    itemNo += 1;
    const nextKeys = formItemKeys.concat(`${SEPARATOR}${itemNo}`);
    form.setFieldsValue({
      formItemKeys: nextKeys,
    });
  };

  removeFormItem = (k: string) => {
    const { form } = this.props;
    const formItemKeys = form.getFieldValue('formItemKeys');
    form.setFieldsValue({
      formItemKeys: formItemKeys.filter((key: string) => convertRuleItemKey(key) !== k),
    });
  };

  handleRandomToken = (itemKey: string) => {
    const {
      form: { setFieldsValue },
    } = this.props;
    setFieldsValue({
      [itemKey]: randomSecret(),
    });
  };

  // 提交
  handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    const { form } = this.props;
    e.preventDefault();
    form.validateFieldsAndScroll((err, fieldsValue) => {
      if (err) {
        return;
      }

      const { formItemKeys, ssoPlatformIdMap, platformUserIdMap, systemUserIdMap, descriptionMap } =
        fieldsValue;
      const formItemList: ISsoUser[] = [];
      const formItemHash: any[] = [];
      // 遍历数据组装
      formItemKeys.forEach((key: string) => {
        const kString = convertRuleItemKey(key);
        const itemObj = {
          ssoPlatformId: ssoPlatformIdMap[kString],
          platformUserId: platformUserIdMap[kString],
          systemUserId: systemUserIdMap[kString],
          description: descriptionMap[kString],
        } as ISsoUser;

        const values = Object.values(itemObj);
        // 如果所有的值都是空，忽略过去
        const isEmpty = values.filter((value) => value || value === 0).length === 0;
        if (!isEmpty) {
          formItemHash.push(convertRuleItemKey(values.toString()));
          formItemList.push(itemObj);
        }
      });

      // 比较外部系统ID + 外部用户ID是否是唯一的
      const groupBy = _.groupBy(
        formItemList,
        (item) => `${item.ssoPlatformId}$$$${item.platformUserId}`,
      );

      let canSubmit = true;
      const groupByKeys = Object.keys(groupBy);
      for (let index = 0; index < groupByKeys.length; index += 1) {
        const key = groupByKeys[index];
        const [platformUserId] = key.split('$$$$');
        if (groupBy[key].length > 1) {
          notification.warning({
            message: '无法保存',
            description: `外部用户【${platformUserId}】重复关联了多个用户`,
          });
          canSubmit = false;
          break;
        }
      }
      if (!canSubmit) {
        return;
      }

      // 比较有效是否完全不同
      const uniqResult = _.uniq(formItemHash);
      if (uniqResult.length !== formItemHash.length) {
        notification.warning({
          message: '无法保存',
          description: '存在完全一样的系统配置。请修改后再次保存。',
        });
        return;
      }

      this.handleCreate(formItemList);
    });
  };

  handleGoBack = () => {
    history.goBack();
  };

  handleReset = () => {
    const { form } = this.props;
    form.resetFields();
  };

  handleCreate = (data: ISsoUser[]) => {
    this.createConfirmModal({
      dispatchType: 'ssoModel/createSsoUser',
      values: data,
      onOk: this.handleGoBack,
      onCancel: this.handleReset,
    });
  };

  render() {
    const {
      createLoading,
      quaryLoading,
      form,
      allSystemUsers = [],
      allSsoPlatforms = [],
    } = this.props;
    const { getFieldDecorator, getFieldValue } = form;
    getFieldDecorator('formItemKeys', {
      initialValue: [`${SEPARATOR}_${itemNo}`],
    });
    const formItemKeys = getFieldValue('formItemKeys');
    const formItems = formItemKeys.map((k: ISsoUser, index: number) => {
      const kString = convertRuleItemKey(k);
      return (
        <Row key={kString} data-key={kString} gutter={4}>
          <Col span={1} style={{ textAlign: 'center' }}>
            <Form.Item>{index + 1}</Form.Item>
          </Col>
          <Col span={platformSpan}>
            <Form.Item>
              {getFieldDecorator(`ssoPlatformIdMap[${kString}]`, {
                validateFirst: true,
                rules: [
                  {
                    required: true,
                    whitespace: true,
                    message: '外部系统不能为空',
                  },
                ],
              })(
                <Select
                  placeholder="请选择外部系统"
                  showSearch
                  filterOption={(input: string, option: any) =>
                    option.children.toLowerCase().indexOf(input.toLowerCase()) > -1
                  }
                >
                  {allSsoPlatforms.map((platform) => (
                    <Select.Option key={platform.id} value={platform.id as any}>
                      {platform.name}
                    </Select.Option>
                  ))}
                </Select>,
              )}
            </Form.Item>
          </Col>
          <Col span={platformUserSpan}>
            <Form.Item>
              {getFieldDecorator(`platformUserIdMap[${kString}]`, {
                initialValue: '',
                validateFirst: true,
                rules: [
                  {
                    required: true,
                    whitespace: true,
                    message: '外部用户ID不能为空',
                  },
                  {
                    max: 64,
                    message: '最多可输入64个字符',
                  },
                ],
              })(<Input placeholder="请输入外部用户ID" />)}
            </Form.Item>
          </Col>
          <Col span={systemUserSpan}>
            <Form.Item>
              {getFieldDecorator(`systemUserIdMap[${kString}]`, {
                validateFirst: true,
                rules: [
                  {
                    required: true,
                    whitespace: true,
                    message: '本系统用户不能为空',
                  },
                ],
              })(
                <Select
                  placeholder="请选择本系统用户"
                  showSearch
                  filterOption={(input: string, option: any) =>
                    option.children.props.children[1].indexOf(input) > -1
                  }
                >
                  <OptGroup label="CMS连接内置用户">
                    {allSystemUsers
                      .filter((item) => item.userType === EUSERTYPE.SINGLESIGNUSER)
                      .map((user) => (
                        <Select.Option value={user.id} disabled={user.locked === USER_LOCKED_TRUE}>
                          {/* TODO: 展示用户权限 */}
                          <Fragment>
                            <Badge
                              status={user.locked === USER_LOCKED_FALSE ? 'success' : 'error'}
                            />
                            {user.fullname}
                          </Fragment>
                        </Select.Option>
                      ))}
                  </OptGroup>
                  <OptGroup label="普通用户">
                    {allSystemUsers
                      .filter((item) => item.userType === EUSERTYPE.SIMPLEUSER)
                      .map((user) => (
                        <Select.Option value={user.id} disabled={user.locked === USER_LOCKED_TRUE}>
                          {/* TODO: 展示用户权限 */}
                          <Fragment>
                            <Badge
                              status={user.locked === USER_LOCKED_FALSE ? 'success' : 'error'}
                            />
                            {user.fullname}
                          </Fragment>
                        </Select.Option>
                      ))}
                  </OptGroup>
                </Select>,
              )}
            </Form.Item>
          </Col>
          <Col span={descSpan}>
            <FormItem>
              {form.getFieldDecorator(`descriptionMap[${kString}]`, {
                initialValue: '',
                rules: [
                  { required: false, message: '请输入描述信息' },
                  { max: 255, message: '最多可输入255个字符' },
                ],
              })(<TextArea rows={1} placeholder="请输入描述信息" />)}
            </FormItem>
          </Col>
          <Col span={1}>
            {/* 至少一条规则 */}
            {formItemKeys.length > 1 && (
              <Form.Item>
                <MinusCircleOutlined onClick={() => this.removeFormItem(kString)} />
              </Form.Item>
            )}
          </Col>
        </Row>
      );
    });

    return (
      <Card bordered={false}>
        <Skeleton active loading={quaryLoading}>
          <Form onSubmit={this.handleSubmit}>
            <FormItem key="applicationRule" label="">
              {formTitle}
              {formItems}
              {/* 增加按钮 */}
              <Form.Item style={{ marginBottom: 0, textAlign: 'center' }}>
                <Button
                  type="dashed"
                  onClick={this.addFormItem}
                  size="small"
                  block
                  style={{ width: 200 }}
                >
                  <PlusOutlined /> 新增
                </Button>
              </Form.Item>
            </FormItem>

            <FormItem style={{ textAlign: 'center' }}>
              <Button
                style={{ marginRight: 10 }}
                type="primary"
                htmlType="submit"
                loading={createLoading}
              >
                保存
              </Button>
              <Button onClick={() => this.handleGoBack()}>返回</Button>
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
    ssoModel: { allSsoPlatforms, allSystemUsers },
  }: {
    ssoModel: ISsoModelStateType;
    loading: { effects: Record<string, boolean> };
  }) => ({
    allSsoPlatforms,
    allSystemUsers,
    createLoading: effects['ssoModel/createSsoUser'],
    quaryLoading:
      effects['ssoModel/queryAllSsoPlatforms'] || effects['ssoModel/queryAllSystemUsers'],
  }),
)(Form.create<ICreateSsoUserProps>()(CreateSsoUser));
