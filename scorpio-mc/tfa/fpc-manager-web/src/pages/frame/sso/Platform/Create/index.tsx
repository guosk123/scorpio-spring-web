import { createConfirmModal, randomSecret } from '@/utils/utils';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import type { FormComponentProps } from '@ant-design/compatible/es/form';
import { MinusCircleOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Card, Col, Input, notification, Row, Skeleton, Tooltip } from 'antd';
import { connect } from 'dva';
import hash from 'hash.js';
import _ from 'lodash';
import React, { PureComponent } from 'react';
import type { Dispatch } from 'redux';
import { history } from 'umi';
import type { ISsoPlatform } from '../../typings';
import type { ISsoModelStateType } from '../../model';

const FormItem = Form.Item;
const { TextArea } = Input;

// 分隔符
const SEPARATOR = '$_o_$_';
let itemNo = 0;

const nameSpan = 5;
const idSpan = 5;
const appTokenSpan = 6;
const descSpan = 6;
const formTitle = (
  <Row gutter={4}>
    <Col style={{ textAlign: 'center' }} span={1}>
      #
    </Col>
    <Col style={{ textAlign: 'center' }} span={nameSpan}>
      外部系统名称
      <label className="ant-form-item-required" />
    </Col>
    <Col style={{ textAlign: 'center' }} span={idSpan}>
      外部系统ID
      <label className="ant-form-item-required" />
    </Col>
    <Col style={{ textAlign: 'center' }} span={appTokenSpan}>
      Token
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

interface ICreatePlatformProps extends FormComponentProps {
  dispatch: Dispatch<any>;
  allSsoPlatforms: ISsoPlatform[];
  createLoading: boolean;
  quaryAllPlatformLoading: boolean;
}
interface ICreatePlatformState {}

class CreatePlatform extends PureComponent<ICreatePlatformProps, ICreatePlatformState> {
  createConfirmModal: (params: any) => void;
  constructor(props: ICreatePlatformProps) {
    super(props);
    this.createConfirmModal = createConfirmModal.bind(this);
  }

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'ssoModel/queryAllSsoPlatforms',
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

      const { formItemKeys, nameMap, platformIdMap, appTokenMap, descriptionMap } = fieldsValue;
      const allPlatformIds = Object.values(platformIdMap);
      // 比较是否有重复的外部系统ID
      if (allPlatformIds.length !== _.uniq(allPlatformIds).length) {
        notification.warning({
          message: '无法保存',
          description: '存在重复的外部系统ID。请修改后再次保存。',
        });
        return;
      }

      const formItemList: ISsoPlatform[] = [];
      const formItemHash: any[] = [];
      // 遍历数据组装
      formItemKeys.forEach((key: string) => {
        const kString = convertRuleItemKey(key);
        const itemObj = {
          name: nameMap[kString],
          platformId: platformIdMap[kString],
          appToken: appTokenMap[kString],
          description: descriptionMap[kString],
        } as ISsoPlatform;

        const values = Object.values(itemObj);
        // 如果所有的值都是空，忽略过去
        const isEmpty = values.filter((value) => value || value === 0).length === 0;
        if (!isEmpty) {
          formItemHash.push(convertRuleItemKey(values.toString()));
          formItemList.push(itemObj);
        }
      });

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

  handleCreate = (data: ISsoPlatform[]) => {
    this.createConfirmModal({
      dispatchType: 'ssoModel/createSsoPlatform',
      values: data,
      onOk: this.handleGoBack,
      onCancel: this.handleReset,
    });
  };

  checkPlatformId = (rule: any, value: string, callback: any) => {
    const { allSsoPlatforms } = this.props;
    if (
      value &&
      allSsoPlatforms.length > 0 &&
      allSsoPlatforms.find((item) => item.platformId === value)
    ) {
      callback(`外部系统ID【${value}】已存在`);
      return;
    }
    callback();
  };

  render() {
    const { createLoading, quaryAllPlatformLoading, form } = this.props;
    const { getFieldDecorator, getFieldValue } = form;
    getFieldDecorator('formItemKeys', {
      initialValue: [`${SEPARATOR}_${itemNo}`],
    });
    const formItemKeys = getFieldValue('formItemKeys');
    const formItems = formItemKeys.map((k: ISsoPlatform, index: number) => {
      const kString = convertRuleItemKey(k);
      return (
        <Row key={kString} data-key={kString} gutter={4}>
          <Col span={1} style={{ textAlign: 'center' }}>
            <Form.Item>{index + 1}</Form.Item>
          </Col>
          <Col span={nameSpan}>
            <Form.Item>
              {getFieldDecorator(`nameMap[${kString}]`, {
                initialValue: '',
                validateFirst: true,
                rules: [
                  {
                    required: true,
                    whitespace: true,
                    message: '名称不能为空',
                  },
                  {
                    max: 30,
                    message: '最多可输入30个字符',
                  },
                ],
              })(<Input placeholder="请输入外部系统名称" />)}
            </Form.Item>
          </Col>
          <Col span={idSpan}>
            <Form.Item>
              {getFieldDecorator(`platformIdMap[${kString}]`, {
                initialValue: '',
                validateFirst: true,
                rules: [
                  {
                    required: true,
                    whitespace: true,
                    message: '系统ID不能为空',
                  },
                  {
                    max: 64,
                    message: '最多可输入64个字符',
                  },
                  { validator: this.checkPlatformId },
                ],
              })(<Input placeholder="请输入外部系统ID" />)}
            </Form.Item>
          </Col>
          <Col span={appTokenSpan}>
            <Form.Item>
              {getFieldDecorator(`appTokenMap[${kString}]`, {
                initialValue: '',
                validateFirst: true,
                rules: [
                  {
                    required: true,
                    whitespace: true,
                    message: 'Token不能为空',
                  },
                  {
                    pattern: /^[a-zA-Z0-9-_@]+$/,
                    message: '只允许输入字母、数字、中划线-、下划线_ 和 @ 组合',
                  },
                  {
                    min: 10,
                    message: '最少请输入10个字符',
                  },
                  {
                    max: 32,
                    message: '最多可输入32个字符',
                  },
                ],
              })(
                <Input
                  placeholder="请输入Token"
                  addonAfter={
                    <Tooltip title="随机生成">
                      <ReloadOutlined
                        onClick={() => this.handleRandomToken(`appTokenMap[${kString}]`)}
                      />
                    </Tooltip>
                  }
                />,
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
        <Skeleton active loading={quaryAllPlatformLoading}>
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
    loading,
    ssoModel: { allSsoPlatforms },
  }: {
    ssoModel: ISsoModelStateType;
    loading: { effects: Record<string, boolean> };
  }) => ({
    allSsoPlatforms,
    createLoading: loading.effects['ssoModel/createSsoPlatform'],
    quaryAllPlatformLoading: loading.effects['ssoModel/queryAllSsoPlatforms'],
  }),
)(Form.create<ICreatePlatformProps>()(CreatePlatform));
