import type { ConnectState } from '@/models/connect';
import { createConfirmModal, updateConfirmModal } from '@/utils/utils';
import { Card, Form, Input } from 'antd';
import Button from 'antd/es/button';
import type { TransferItem } from 'antd/lib/transfer';
import type { FC } from 'react';
import type { Dispatch } from 'umi';
import { connect, history } from 'umi';
import type { ITreeData } from '../components/CustomTransfer';
import CustomTransfer from '../components/CustomTransfer';
import type { AppCategoryItem, AppSubCategoryItem, ICustomCategory } from '../typings';
import { ECustomSAApiType } from '../typings';

const FormItem = Form.Item;
const { TextArea } = Input;

/**
 * 自定义分类最小ID
 */
export const SA_CUSTOM_CATEGORY_ID_MIN = 101;
/**
 * 自定义分类最大ID
 */
export const SA_CUSTOM_CATEGORY_ID_MAX = 150;
/**
 * 自定义分类数量
 */
export const SA_CUSTOM_CATEGORY_COUNT = 50;

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

interface ICustomCategoryFormProps {
  dispatch: Dispatch;
  allCategoryList: AppCategoryItem[];
  allSubCategoryList: AppSubCategoryItem[];
  submitting: boolean;
  detail: ICustomCategory;
}
const CustomCategoryForm: FC<ICustomCategoryFormProps> = ({
  dispatch,
  submitting,
  allCategoryList,
  detail = {} as ICustomCategory,
}) => {
  const [form] = Form.useForm();
  const onFinish = (values: any) => {
    const params = {
      data: {
        ...values,
        subCategoryIds: values.subCategoryIds?.join(',') || '',
        description: values.description || '',
      },
      type: ECustomSAApiType.CATEGORY,
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

  const subCategoryIds = detail?.subCategoryIds?.split(',') || [];
  let targetKeys: string[] = [];
  if (subCategoryIds.length > 0) {
    targetKeys = subCategoryIds.map((item) => `${detail.categoryId}_${item}`);
  }

  // 树形结构数据
  const treeData: ITreeData[] = [];
  const transferData: TransferItem[] = [];
  for (let index = 0; index < allCategoryList.length; index += 1) {
    const element = allCategoryList[index];

    const children: ITreeData[] = [];
    if (Array.isArray(element.subCategoryList)) {
      for (let j = 0; j < element.subCategoryList.length; j += 1) {
        const item = element.subCategoryList[j];
        const itemObj = {
          key: `${item.categoryId}_${item.subCategoryId}`,
          title: item.nameText,
        };
        children.push(itemObj);
        transferData.push({
          ...itemObj,
          // 自定义的子分类在此大类下，不能被移除
          disabled: item.isCustom && subCategoryIds.includes(item.subCategoryId),
        });
      }
    }

    // 编辑模式下，左边树中隐藏自己
    if (detail.id && detail.categoryId === element.categoryId) {
      continue;
    }

    treeData.push({
      disabled: detail.categoryId === element.categoryId,
      key: element.categoryId,
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
          subCategoryIds: targetKeys,
        }}
      >
        <FormItem {...formItemLayout} label="ID" name="id" hidden>
          <Input placeholder="分类id" />
        </FormItem>
        <FormItem
          {...formItemLayout}
          label="分类名称"
          name="name"
          normalize={(value) => (value ? value.trim() : '')}
          rules={[
            {
              required: true,
              whitespace: true,
              message: '请填写分类名称',
            },
            { max: 30, message: '最多可输入30个字符' },
          ]}
        >
          <Input placeholder="填写分类名称" />
        </FormItem>

        <FormItem
          {...formItemLayout}
          label="子分类配置"
          name="subCategoryIds"
          valuePropName="targetKeys"
          rules={[{ required: false, message: '请设置子分类' }]}
        >
          <CustomTransfer
            transferData={transferData}
            treeData={treeData}
            titles={['待选子分类', '已选子分类']}
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
)(CustomCategoryForm);
