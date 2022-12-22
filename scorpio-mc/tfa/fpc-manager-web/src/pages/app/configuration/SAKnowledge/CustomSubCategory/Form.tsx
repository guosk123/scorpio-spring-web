import type { ConnectState } from '@/models/connect';
import { createConfirmModal, updateConfirmModal } from '@/utils/utils';
import { Card, Form, Input, Select } from 'antd';
import Button from 'antd/es/button';
import type { TransferItem } from 'antd/lib/transfer';
import type { FC } from 'react';
import React from 'react';
import type { Dispatch } from 'umi';
import { connect, history } from 'umi';
import type { ITreeData } from '../components/CustomTransfer';
import CustomTransfer from '../components/CustomTransfer';
import type { AppCategoryItem, AppSubCategoryItem, ICustomSubCategory } from '../typings';
import { ECustomSAApiType } from '../typings';

const FormItem = Form.Item;
const { TextArea } = Input;

/**
 * 自定义分类最小ID
 */
export const SA_SUB_CUSTOM_CATEGORY_ID_MIN = 101;
/**
 * 自定义分类最大ID
 */
export const SA_SUB_CUSTOM_CATEGORY_ID_MAX = 150;

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 3 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 20 },
    md: { span: 19 },
  },
};

const submitFormLayout = {
  wrapperCol: {
    xs: { span: 24, offset: 0 },
    sm: { span: 12, offset: 3 },
  },
};

interface ICustomSubCategoryFormProps {
  dispatch: Dispatch;
  allCategoryList: AppCategoryItem[];
  allSubCategoryList: AppSubCategoryItem[];
  submitting: boolean;
  detail: ICustomSubCategory;
}
const CustomSubCategoryForm: FC<ICustomSubCategoryFormProps> = ({
  dispatch,
  submitting,
  allCategoryList,
  allSubCategoryList,
  detail = {} as ICustomSubCategory,
}) => {
  const [form] = Form.useForm();
  const onFinish = (values: any) => {
    const params = {
      data: {
        ...values,
        applicationIds: values.applicationIds?.join(',') || '',
        description: values.description || '',
      },
      type: ECustomSAApiType.SUB_CATEGORY,
    };

    if (values.id) {
      handleUpdate(params);
    } else {
      handleCreate(params);
    }
  };
  const onFinishFailed = (errorInfo: any) => {
    // eslint-disable-next-line no-console
    console.log('Failed:', errorInfo);
  };

  const handleCreate = (values: any) => {
    createConfirmModal({
      dispatchType: 'customSAModel/createCustomSA',
      values,
      onOk: handleGoBack,
      onCancel: handleReset,
      dispatch,
    });
  };

  const handleUpdate = (values: any) => {
    updateConfirmModal({
      dispatchType: 'customSAModel/updateCustomSA',
      values,
      onOk: handleGoBack,
      dispatch,
      onCancel: () => {},
    });
  };

  const handleGoBack = () => {
    history.goBack();
  };

  const handleReset = () => {
    form.resetFields();
  };

  const applicationIds = detail?.applicationIds?.split(',') || [];
  let targetKeys: string[] = [];
  if (applicationIds.length > 0) {
    targetKeys = applicationIds.map((item) => `${detail.subCategoryId}_${item}`);
  }

  // 树形结构数据
  const treeData: ITreeData[] = [];
  const transferData: TransferItem[] = [];
  for (let index = 0; index < allSubCategoryList.length; index += 1) {
    const element = allSubCategoryList[index];
    const children: ITreeData[] = [];
    if (Array.isArray(element.applicationList)) {
      for (let j = 0; j < element.applicationList.length; j += 1) {
        const item = element.applicationList[j];
        const itemObj = {
          key: `${item.subCategoryId}_${item.applicationId}`,
          title: item.nameText,
        };
        children.push(itemObj);
        transferData.push({
          ...itemObj,
          // 自定义的应用在此子分类下，不能被移除
          disabled: item.isCustom && applicationIds.includes(item.applicationId),
        });
      }
    }

    // 屏蔽当前正在编辑的子分类
    if (detail.id && detail.subCategoryId === element.subCategoryId) {
      continue;
    }

    treeData.push({
      key: element.subCategoryId,
      title: element.nameText,
      children,
    });
  }

  return (
    <Card bordered={false}>
      <Form
        form={form}
        name="sa-custom-category-form"
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
        scrollToFirstError
        initialValues={{
          ...detail,
          applicationIds: targetKeys,
          description: detail.description || '',
        }}
      >
        <FormItem {...formItemLayout} label="ID" name="id" hidden>
          <Input placeholder="子分类id" />
        </FormItem>
        <FormItem
          {...formItemLayout}
          label="子分类名称"
          name="name"
          normalize={(value) => (value ? value.trim() : '')}
          rules={[
            {
              required: true,
              whitespace: true,
              message: '请填写子分类名称',
            },
            { max: 30, message: '最多可输入30个字符' },
          ]}
        >
          <Input placeholder="填写子分类名称" />
        </FormItem>

        <FormItem
          {...formItemLayout}
          label="所属分类"
          name="categoryId"
          rules={[{ required: true, message: '请选择所属分类' }]}
        >
          <Select
            placeholder="请选择所属分类"
            showSearch
            filterOption={(input, option) =>
              option!.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
            }
          >
            {allCategoryList.map((cate) => (
              <Select.Option key={cate.categoryId} value={cate.categoryId}>
                {cate.nameText}
              </Select.Option>
            ))}
          </Select>
        </FormItem>

        <FormItem
          {...formItemLayout}
          label="应用配置"
          name="applicationIds"
          valuePropName="targetKeys"
          rules={[{ required: false, message: '请设置子分类' }]}
        >
          <CustomTransfer
            transferData={transferData}
            treeData={treeData}
            titles={['待选应用', '已选应用']}
          />
        </FormItem>

        <FormItem
          {...formItemLayout}
          label="描述"
          name="description"
          rules={[
            { required: false, message: '请输入描述信息' },
            { max: 255, message: '最多可输入255个字符' },
          ]}
        >
          <TextArea rows={4} placeholder="请输入描述信息" />
        </FormItem>

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
    SAKnowledgeModel: { allCategoryList, allSubCategoryList },
  }: ConnectState) => ({
    allCategoryList,
    allSubCategoryList,
    submitLoading:
      effects['customSAModel/createCustomSA'] || effects['customSAModel/updateCustomSA'],
  }),
)(CustomSubCategoryForm);
