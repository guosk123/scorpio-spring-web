import type { ConnectState } from '@/models/connect';
import { createConfirmModal, updateConfirmModal } from '@/utils/utils';
import { ReloadOutlined } from '@ant-design/icons';
import { Button, Card, Form, Input, Tooltip, TreeSelect } from 'antd';
import type { TransferItem } from 'antd/lib/transfer';
import React, { useCallback, useEffect } from 'react';
import type { Dispatch, INetworkTreeData } from 'umi';
import { connect, history } from 'umi';
import type { INetwork } from '../../../Network/typings';
import type { ITreeData } from '../../../SAKnowledge/components/CustomTransfer';
import CustomTransfer from '../../../SAKnowledge/components/CustomTransfer';
import type { ApplicationItem, AppSubCategoryItem } from '../../../SAKnowledge/typings';
import type { IService, IServiceFormData } from '../../typings';

const { TreeNode } = TreeSelect;
const FormItem = Form.Item;
const { TextArea } = Input;

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 3 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 20 },
    md: { span: 18 },
  },
};

const submitFormLayout = {
  wrapperCol: {
    xs: { span: 24, offset: 0 },
    sm: { span: 12, offset: 3 },
  },
};

interface ServiceFormProps {
  submitting: boolean | undefined;
  dispatch: Dispatch;
  detail?: IService;
  allNetworks: INetwork[];
  networkTree: INetworkTreeData[];
  allSubCategoryList: AppSubCategoryItem[];
  allApplicationMap: Record<string, ApplicationItem>;
  queryAllNetworkLoading: boolean | undefined;
}
const ServiceForm: React.FC<ServiceFormProps> = (props) => {
  const {
    detail = {} as IService,
    networkTree = [],
    allSubCategoryList = [],
    allApplicationMap,
    submitting,
    dispatch,
  } = props;
  const [form] = Form.useForm();

  const queryNetworkTree = useCallback(() => {
    if (dispatch) {
      dispatch({
        type: 'networkModel/queryNetworkTree',
      });
    }
  }, [dispatch]);

  useEffect(() => {
    queryNetworkTree();
  }, [queryNetworkTree]);

  const handleGoBack = () => {
    history.goBack();
  };

  const handleReset = () => {
    form.resetFields();
  };

  const handleCreate = (values: IServiceFormData) => {
    createConfirmModal({
      dispatchType: 'serviceModel/createService',
      values,
      onOk: handleGoBack,
      onCancel: handleReset,
      dispatch,
    });
  };

  const handleUpdate = (values: IServiceFormData) => {
    updateConfirmModal({
      dispatchType: 'serviceModel/updateService',
      values,
      onOk: handleGoBack,
      dispatch,
      onCancel: () => {},
    });
  };

  const handleFinish = ({
    id,
    name,
    networkIds = [],
    applicationIds = [],
    description = '',
  }: Record<string, any>) => {
    const data = {
      id,
      name,
      networkIds: networkIds.join(','),
      application: applicationIds.join(','),
      description: description || '',
    } as IServiceFormData;

    if (id) {
      handleUpdate(data);
    } else {
      handleCreate(data);
    }
  };

  const applicationIds = detail?.application?.split(',') || [];
  let targetKeys: string[] = [];
  if (applicationIds.length > 0) {
    targetKeys = applicationIds
      .map((applicationId) => {
        const subCategoryId = allApplicationMap[applicationId]?.subCategoryId || '';
        if (subCategoryId) {
          return `${subCategoryId}_${applicationId}`;
        }
        return '';
      })
      .filter((id) => id);
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
        });
      }
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
        name="service-form"
        initialValues={{
          ...detail,
          networkIds: detail.networkIds?.split(',') || [],
          applicationIds: targetKeys,
          description: detail.description || '',
        }}
        onFinish={handleFinish}
        scrollToFirstError
      >
        <FormItem {...formItemLayout} label="ID" name="id" hidden>
          <Input placeholder="业务id" />
        </FormItem>
        <FormItem
          {...formItemLayout}
          label="名称"
          name="name"
          rules={[
            {
              required: true,
              whitespace: true,
              message: '请填写业务名称',
            },
            {
              max: 30,
              message: '最多可输入30个字符',
            },
          ]}
        >
          <Input placeholder="填写业务名称" />
        </FormItem>
        <FormItem
          {...formItemLayout}
          label="网络"
          name="networkIds"
          rules={[
            {
              required: true,
              message: '请选择归属网络',
            },
          ]}
        >
          <TreeSelect
            showSearch
            style={{ width: '100%' }}
            dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
            placeholder="选择网络"
            allowClear
            multiple
            treeDefaultExpandAll
            showCheckedStrategy={TreeSelect.SHOW_ALL}
            suffixIcon={
              <Tooltip title="刷新">
                <ReloadOutlined onClick={queryNetworkTree} />
              </Tooltip>
            }
          >
            {networkTree.map((network) => (
              <TreeNode key={network.key} value={network.key} title={network.title}>
                {network.children?.map((el) => (
                  <TreeNode key={el.key} value={el.key} title={el.title} />
                ))}
              </TreeNode>
            ))}
          </TreeSelect>
        </FormItem>

        <FormItem
          {...formItemLayout}
          label="应用配置"
          name="applicationIds"
          valuePropName="targetKeys"
          rules={[{ required: true, message: '请选择应用' }]}
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
            {
              required: false,
              message: '请填写描述信息',
            },
            { max: 255, message: '最多可输入255个字符' },
          ]}
        >
          <TextArea rows={4} />
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
    networkModel: { allNetworks, networkTree },
    SAKnowledgeModel: { allSubCategoryList, allApplicationMap },
  }: ConnectState) => ({
    networkTree,
    allNetworks,
    allSubCategoryList,
    allApplicationMap,
    submitting: effects['serviceModel/createService'] || effects['serviceModel/updateService'],
    queryAllNetworkLoading: effects['networkModel/queryNetworkTree'],
  }),
)(ServiceForm);
