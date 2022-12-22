import type { ConnectState } from '@/models/connect';
import { createConfirmModal, updateConfirmModal } from '@/utils/utils';
import { Button, Card, Form, Input, TreeSelect, Select } from 'antd';
import type { TransferItem } from 'antd/lib/transfer';
import React, { ReactNode, useEffect, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, history } from 'umi';
import type { INetworkGroup, INetworkSensor } from '../../../Network/typings';
import type { ITreeData } from '../../../SAKnowledge/components/CustomTransfer';
import CustomTransfer from '../../../SAKnowledge/components/CustomTransfer';
import type { ApplicationItem, AppSubCategoryItem } from '../../../SAKnowledge/typings';
import type { IService, IServiceFormData } from '../../typings';
import { INetworkTreeData } from '@/models/app/network';
import { v4 as uuidv4 } from 'uuid';

const FormItem = Form.Item;
const { TextArea } = Input;
const { Option } = Select;
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

enum ENetworkFormSelect {
  'dafault' = 0,
  'networkIds' = 1,
  'networkGroups' = 2,
}

interface ServiceFormProps {
  submitting: boolean | undefined;
  dispatch: Dispatch;
  detail?: IService;
  allNetworkGroup: INetworkGroup[];
  allNetworkSensor: INetworkSensor[];
  allSubCategoryList: AppSubCategoryItem[];
  allApplicationMap: Record<string, ApplicationItem>;
  queryAllNetworkSensorLoading: boolean | undefined;
  networkSensorTree: INetworkTreeData[];
}

// 修饰sensorNetworkTree
const decorateTree = (tree: INetworkTreeData, parentId?: string, parentName?: string) => {
  if (!tree.children || tree.children?.length === 0) {
    /** 叶子结点的情况 */
    if (parentId) {
      const value = `${parentId}^${tree.value}`;
      tree.key = value;
      tree.value = value;
      tree.title = `${parentName} - ${tree.title}`;
    }
  } else {
    /** 父节点的情况下 */
    tree.children?.forEach((node) => decorateTree(node, tree.value, tree.title));
  }
};

const ServiceForm: React.FC<ServiceFormProps> = (props) => {
  const {
    detail = {} as IService,
    allSubCategoryList = [],
    allApplicationMap,
    submitting,
    dispatch,
    networkSensorTree,
    allNetworkGroup,
  } = props;
  const [networkIds, setNetworkIds] = useState<string[]>([]);
  const [networkGroups, setNetworkGroups] = useState<string[]>([]);
  const [treeId, setTreeId] = useState<string>('');
  const [networkFormSelect, setNetworkFormSelect] = useState<ENetworkFormSelect>(
    ENetworkFormSelect.dafault,
  );
  const [form] = Form.useForm();

  useEffect(() => {
    networkSensorTree.forEach((tree) => decorateTree(tree));
    setTreeId(uuidv4());
  }, [networkSensorTree]);

  useEffect(() => {
    if (networkIds.length === 0 && networkGroups.length === 0) {
      setNetworkFormSelect(ENetworkFormSelect.dafault);
    } else if (networkIds.length === 0) {
      setNetworkFormSelect(ENetworkFormSelect.networkGroups);
      form.validateFields(['networkIds']);
    } else if (networkGroups.length === 0) {
      setNetworkFormSelect(ENetworkFormSelect.networkIds);
      form.validateFields(['networkGroups']);
    }
  }, [networkIds, networkGroups]);

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
    networkGroups = [],
    applicationIds = [],
    description = '',
  }: Record<string, any>) => {
    const data = {
      id,
      name,
      networkIds: networkIds.join(','),
      networkGroupIds: networkGroups.join(','),
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
  useEffect(() => {
    dispatch({
      type: 'networkModel/queryNetworkSensorTree',
    });
    dispatch({
      type: 'networkModel/queryAllNetworkGroups',
    });

    if (Object.keys(detail).length !== 0) {
      setNetworkIds(detail.networkIds !== '' ? detail.networkIds.split(',') : []);
      setNetworkGroups(detail.networkGroupIds !== '' ? detail.networkGroupIds.split(',') : []);
    }
  }, []);

  return (
    <Card bordered={false}>
      <Form
        form={form}
        name="service-form"
        initialValues={{
          ...detail,
          networkIds:
            Object.keys(detail).length !== 0 && detail.networkIds !== ''
              ? detail.networkIds.split(',')
              : [],
          networkGroups:
            Object.keys(detail).length !== 0 && detail.networkGroupIds !== ''
              ? detail.networkGroupIds.split(',')
              : [],
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
              required:
                networkFormSelect === ENetworkFormSelect.dafault ||
                networkFormSelect === ENetworkFormSelect.networkIds,
              message: '请选择探针网络',
            },
          ]}
        >
          <TreeSelect
            showSearch
            style={{ width: '100%' }}
            dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
            placeholder="请选择探针网络"
            allowClear
            multiple
            disabled={networkFormSelect === ENetworkFormSelect.networkGroups}
            onChange={(value: string[]) => {
              setNetworkIds(value);
            }}
            key={treeId}
            showCheckedStrategy={TreeSelect.SHOW_ALL}
            treeData={networkSensorTree}
            treeNodeFilterProp={'title'}
          />
        </FormItem>
        <FormItem
          {...formItemLayout}
          label="网络组"
          name="networkGroups"
          rules={[
            {
              required:
                networkFormSelect === ENetworkFormSelect.dafault ||
                networkFormSelect === ENetworkFormSelect.networkGroups,
              message: '请选择归属网络组',
            },
          ]}
        >
          <Select
            disabled={networkFormSelect === ENetworkFormSelect.networkIds}
            onChange={(value: string[]) => {
              setNetworkGroups(value);
            }}
            mode="multiple"
            style={{ width: '100%' }}
            placeholder="请选网络组"
            allowClear
            showSearch
          >
            {allNetworkGroup.map((el: any) => (
              <Option key={el.id} value={el.id}>
                {el.name}
              </Option>
            ))}
          </Select>
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
    networkModel: { allNetworkSensor, allNetworkGroup, networkSensorTree },
    SAKnowledgeModel: { allSubCategoryList, allApplicationMap },
  }: ConnectState) => ({
    networkSensorTree,
    allNetworkSensor,
    allSubCategoryList,
    allApplicationMap,
    allNetworkGroup,
    submitting: effects['serviceModel/createService'] || effects['serviceModel/updateService'],
    queryAllNetworkSensorLoading: effects['networkModel/queryAllNetworkSensor'],
  }),
)(ServiceForm);
