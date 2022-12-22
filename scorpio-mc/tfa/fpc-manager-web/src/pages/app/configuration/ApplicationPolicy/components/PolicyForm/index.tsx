/* eslint-disable no-restricted-syntax */
import { getTablePaginationDefaultSettings } from '@/common/app';
import type { ConnectState } from '@/models/connect';
import { getLinkUrl, parseObjJson } from '@/utils/utils';
import type { FormComponentProps } from '@ant-design/compatible/es/form';
import { QuestionCircleOutlined, RollbackOutlined, SearchOutlined } from '@ant-design/icons';
import type { RadioChangeEvent } from 'antd';
import {
  Alert,
  Button,
  Card,
  Checkbox,
  Col,
  Form,
  Input,
  InputNumber,
  Modal,
  Radio,
  Result,
  Row,
  Select,
  Skeleton,
  Table,
  Tooltip,
} from 'antd';
import type { CheckboxChangeEvent } from 'antd/lib/checkbox';
import type { CheckboxValueType } from 'antd/lib/checkbox/Group';
import type { FormInstance } from 'antd/lib/form';
import type { ColumnProps } from 'antd/lib/table';
import { connect } from 'dva';
import lodash from 'lodash';
import React, { createRef } from 'react';
import type { Dispatch } from 'redux';
import type { SAKnowledgeModelState } from 'umi';
import { history } from 'umi';
import type {
  AppCategoryItem,
  ApplicationItem,
  AppSubCategoryItem,
} from '../../../SAKnowledge/typings';
import type { ApplicationPolicyModelState } from '../../model';
import type { IApplicationPolicy } from '../../typings';
import { EExceptFlowAction } from '../../typings';
import styles from './index.less';

const { Option } = Select;

/**
 * 默认规则的 ID
 */
export const DEFAULT_POLICY_ID = '1';

/**
 * 全量存储
 */
const ACTION_SAVE = 'save';

const ACTION_SAVE_VALUE = 0;

/**
 * 不存储
 */
const ACTION_NO_SAVE = 'no_save';

const ACTION_NO_SAVE_VALUE = 1;
/**
 * 截断存储
 */
const ACTION_PART_SAVE = 'part_save';

const MIN_PART_SAVE_VALUE = 64;
const MAX_PART_SAVE_VALUE = 1500;

const ACTION_LIST = [
  {
    label: '全量存储',
    value: ACTION_SAVE,
  },
  {
    label: '截断存储',
    value: ACTION_PART_SAVE,
  },
  {
    label: '不存储',
    value: ACTION_NO_SAVE,
  },
];

interface IFormValues {
  id: string;
  name: string;
  description?: string;
  applicationName?: string;
  storeAction?: string;
  exceptFlow: boolean;
}

interface ApplicationPolicyProps extends FormComponentProps {
  dispatch: Dispatch<any>;
  applicationPolicyModel: ApplicationPolicyModelState;
  SAKnowledgeModel: SAKnowledgeModelState;
  detail: IApplicationPolicy;
  queryAllApplicationsLoading: boolean;
  submitLoading: boolean;
  showMode: boolean;
}

interface IAction {
  key: 'save' | 'no_save' | 'part_save';
  value: number | undefined;
}

interface ApplicationFilterFormState {
  categoryIndeterminate: boolean;
  categoryCheckAll: boolean;
  checkedCategoryIds: string[];

  subCategoryIndeterminate: boolean;
  subCategoryCheckAll: boolean;
  checkedSubCategoryIds: string[];

  searchApplicationName?: string;
  searchStoreAction?: IAction['key'];

  allApplicationList: ApplicationItem[];
  allApplicationMap: Record<string, ApplicationItem>;
  /**
   * 所有应用对应的存储策略
   */
  allActionMap: Record<string, IAction>;

  /**
   * 默认配置
   */
  defaultActionMap: IAction;
}

class ApplicationFilterForm extends React.PureComponent<
  ApplicationPolicyProps,
  ApplicationFilterFormState
> {
  formRef = createRef<FormInstance>();

  state = {
    // 大类
    categoryIndeterminate: false,
    categoryCheckAll: true,
    checkedCategoryIds: [] as string[],
    // 小类
    subCategoryIndeterminate: false,
    subCategoryCheckAll: true,
    checkedSubCategoryIds: [],

    allApplicationMap: {} as Record<string, ApplicationItem>,

    allApplicationList: [] as ApplicationItem[],
    allActionMap: {} as Record<string, IAction>,

    defaultActionMap: {} as IAction,

    searchApplicationName: '',
    searchStoreAction: undefined,
  };

  componentDidMount() {
    this.queryAllApplications();
  }

  queryAllApplications = () => {
    const { dispatch, detail = {} as IApplicationPolicy } = this.props;
    (
      dispatch({
        type: 'SAKnowledgeModel/queryAllApplications',
      }) as unknown as Promise<any>
    ).then(
      ({
        allCategoryList,
        allSubCategoryList,
        allApplicationMap,
        allApplicationList,
      }: {
        allApplicationList: ApplicationItem[];
        [propName: string]: any;
      }) => {
        // 如果是编辑，则重建所有应用的默认存储规则
        const currentActionMap = {};
        let defaultActionMap = {} as IAction;
        if (detail.id) {
          // 例外的存储配置
          const { defaultAction, exceptApplication } = detail;
          const exceptApplicationObj = parseObjJson(exceptApplication);

          defaultActionMap = this.calculateActionObjByValue(defaultAction);
          // 填充表单默认值
          this.formRef.current?.setFieldsValue({
            defaultActionKey: defaultActionMap.key,
          });

          // 计算默认存储配置
          // 计算所有应用的存储配置
          for (let i = 0; i < allApplicationList.length; i += 1) {
            const app = allApplicationList[i];
            currentActionMap[app.applicationId] = exceptApplicationObj.hasOwnProperty(
              app.applicationId,
            )
              ? // 不在例外中，就是默认规则
                this.calculateActionObjByValue(exceptApplicationObj[app.applicationId])
              : this.calculateActionObjByValue(defaultAction);
          }
        }

        this.setState({
          allApplicationList,
          allApplicationMap,
          checkedCategoryIds: allCategoryList.map((item: AppCategoryItem) => item.categoryId),
          checkedSubCategoryIds: allSubCategoryList.map(
            (item: AppSubCategoryItem) => item.subCategoryId,
          ),
          allActionMap: currentActionMap,
          defaultActionMap,
        });
      },
    );
  };

  // ====处理大类====
  handleAllCategoryChecked = (e: CheckboxChangeEvent) => {
    const {
      SAKnowledgeModel: { allCategoryList },
    } = this.props;

    const { checked } = e.target;

    this.setState(
      {
        checkedCategoryIds: checked ? allCategoryList.map((item) => item.categoryId) : [],
        categoryIndeterminate: false,
        categoryCheckAll: checked,
      },
      () => {
        // 如果是大类全部选中，小类也要相应变化
        let checkedSubCategoryIds: string[] = [];
        const subCategoryIndeterminate: boolean = false;
        let subCategoryCheckAll = false;
        if (checked) {
          const displaySubCategoryList = this.getDisplaySubCategoryList();
          checkedSubCategoryIds = displaySubCategoryList.map((item) => item.subCategoryId);
          subCategoryCheckAll = checked;
        }
        this.setState({
          checkedSubCategoryIds,
          subCategoryIndeterminate,
          subCategoryCheckAll,
        });
      },
    );
  };

  handleCategoryChange = (checkedList: CheckboxValueType[]) => {
    const {
      SAKnowledgeModel: { allCategoryList, allSubCategoryList },
    } = this.props;

    const {
      checkedCategoryIds: oldCheckedCategoryIds,
      checkedSubCategoryIds: oldCheckedSubCategoryIds,
    } = this.state;
    // 比较一下新的大类和旧的大类，新增了哪些，删除了哪些
    // 并集
    const staticCategoryIds = lodash.intersection(oldCheckedCategoryIds, checkedList);
    // 新增大类
    const newCategoryIds = lodash.difference(checkedList, staticCategoryIds);
    // 删除的大类
    const delCategoryIds = lodash.difference(oldCheckedCategoryIds, staticCategoryIds);

    const newCheckedSubCategoryIds: string[] = oldCheckedSubCategoryIds.slice();
    allSubCategoryList.forEach((sub) => {
      if (newCategoryIds.indexOf(sub.categoryId) > -1) {
        newCheckedSubCategoryIds.push(sub.subCategoryId);
      }
      if (delCategoryIds.indexOf(sub.categoryId) > -1) {
        lodash.pull(newCheckedSubCategoryIds, sub.subCategoryId);
      }
    });

    this.setState(
      {
        checkedCategoryIds: checkedList as string[],
        categoryIndeterminate: !!checkedList.length && checkedList.length < allCategoryList.length,
        categoryCheckAll: checkedList.length === allCategoryList.length,
      },
      () => {
        const displaySubCategoryList = this.getDisplaySubCategoryList();
        const subCategoryIds = displaySubCategoryList.map((item) => item.subCategoryId);

        this.setState({
          checkedSubCategoryIds: newCheckedSubCategoryIds,
          subCategoryIndeterminate: newCheckedSubCategoryIds.length < subCategoryIds.length,
          subCategoryCheckAll: newCheckedSubCategoryIds.length === subCategoryIds.length,
        });
      },
    );
  };

  // ====处理小类====
  /**
   * 根据所有的大类和已选择的大类，展示下面的小类
   */
  getDisplaySubCategoryList = () => {
    const {
      SAKnowledgeModel: { allCategoryList },
    } = this.props;
    const { checkedCategoryIds } = this.state;
    let displaySubCategoryList: AppSubCategoryItem[] = [];
    for (const category of allCategoryList) {
      if (checkedCategoryIds.indexOf(category.categoryId) > -1 && category.subCategoryList) {
        displaySubCategoryList = displaySubCategoryList.concat(category.subCategoryList);
      }
    }

    return displaySubCategoryList.sort((a, b) => +a.subCategoryId - +b.subCategoryId);
  };

  handleAllSubCategoryChecked = (e: CheckboxChangeEvent) => {
    const displaySubCategoryList = this.getDisplaySubCategoryList();
    const { checked } = e.target;
    this.setState({
      checkedSubCategoryIds: checked
        ? displaySubCategoryList.map((item) => item.subCategoryId)
        : [],
      subCategoryIndeterminate: false,
      subCategoryCheckAll: checked,
    });
  };

  handleSubCategoryChange = (checkedList: CheckboxValueType[]) => {
    const displaySubCategoryList = this.getDisplaySubCategoryList();
    this.setState({
      checkedSubCategoryIds: checkedList as string[],
      subCategoryIndeterminate:
        !!checkedList.length && checkedList.length < displaySubCategoryList.length,
      subCategoryCheckAll: checkedList.length === displaySubCategoryList.length,
    });
  };

  /**
   * 根据展示的小类，展示下面所属的应用
   */
  getDisplayApplicationList = (
    displaySubCategoryList: AppSubCategoryItem[],
    checkedSubCategoryIds: string[],
  ): ApplicationItem[] => {
    let displayApplicationList: ApplicationItem[] = [];
    for (const subCategory of displaySubCategoryList) {
      if (
        checkedSubCategoryIds.indexOf(subCategory.subCategoryId) > -1 &&
        subCategory.applicationList
      ) {
        displayApplicationList = displayApplicationList.concat(subCategory.applicationList);
      }
    }

    return displayApplicationList.sort((a, b) => +a.applicationId - +b.applicationId);
  };

  handleResetSearch = () => {
    this.formRef.current?.setFieldsValue({
      applicationName: '',
      storeAction: '',
    });
    this.setState({
      searchApplicationName: '',
      searchStoreAction: undefined,
    });
  };

  handleSearch = () => {
    const { storeAction, applicationName } = this.formRef.current?.getFieldsValue([
      'storeAction',
      'applicationName',
    ]);
    this.setState({
      searchApplicationName: applicationName,
      searchStoreAction: storeAction,
    });
  };

  // ======== 修改默认配置 S =======
  /**
   * 默认存储配置变化
   * @param e
   */
  handleDefaultActionChange = (e: RadioChangeEvent) => {
    // 触发校验
    this.formRef.current?.validateFields(['applicationActions']);

    const { value: actionKey } = e.target;
    const actionObj = this.calculateActionObjByKey(actionKey);
    // this.formRef.current?.setFieldsValue({
    //   defaultActionKey: actionKey,
    // });

    const { allApplicationList } = this.state;
    const nextActionMap = {} as Record<string, IAction>;

    for (let i = 0; i < allApplicationList.length; i += 1) {
      const app = allApplicationList[i];
      nextActionMap[app.applicationId] = this.calculateActionObjByKey(actionKey);
    }

    // 修改所有的应用的默认规则
    this.setState({
      defaultActionMap: actionObj,
      allActionMap: nextActionMap,
    });
  };

  /**
   * 截断存储范围值变化
   * @param value
   */
  handlePartSaveValueChange = (value: number | undefined) => {
    // 触发校验
    this.formRef.current?.validateFields(['defaultActionKey', 'applicationActions']);
    // 调整所有应用的存储策略
    // 当前是截断存储的，全部更新
    const { allActionMap, allApplicationList } = this.state;
    const nextActionMap = {};
    for (let i = 0; i < allApplicationList.length; i += 1) {
      const app = allApplicationList[i];
      const oldAction = allActionMap[app.applicationId];
      if (oldAction.key === ACTION_PART_SAVE) {
        nextActionMap[app.applicationId] = {
          ...oldAction,
          value,
        };
      } else {
        nextActionMap[app.applicationId] = oldAction;
      }
    }
    this.setState({
      defaultActionMap: {
        key: ACTION_PART_SAVE,
        value,
      },
      allActionMap: nextActionMap,
    });
  };

  calculateActionObjByValue = (actionValue: IAction['value']) => {
    const result = {
      value: actionValue,
    } as IAction;
    if (actionValue === ACTION_SAVE_VALUE) {
      result.key = ACTION_SAVE;
    } else if (actionValue === ACTION_NO_SAVE_VALUE) {
      result.key = ACTION_NO_SAVE;
    } else {
      result.key = ACTION_PART_SAVE;
    }

    return result;
  };
  calculateActionObjByKey = (actionKey: IAction['key']) => {
    const result = {
      key: actionKey,
      value: undefined,
    } as IAction;
    if (actionKey === ACTION_SAVE) {
      result.value = ACTION_SAVE_VALUE;
    } else if (actionKey === ACTION_NO_SAVE) {
      result.value = ACTION_NO_SAVE_VALUE;
    } else if (actionKey === ACTION_PART_SAVE) {
      result.value = undefined;
    }

    return result;
  };
  // ======== 修改默认配置 E =======

  // ======== 修改应用的配置 S =======
  // 修改某个应用的配置
  handleAppActionChange = (applicationId: string, actionKey: IAction['key']) => {
    // 触发校验
    this.formRef.current?.validateFields(['applicationActions']);

    const nextActionMap = lodash.cloneDeep(this.state.allActionMap);
    nextActionMap[applicationId] = this.calculateActionObjByKey(actionKey);

    this.setState({
      allActionMap: nextActionMap,
    });
  };

  handleAppPartSaveValueChange = (applicationId: string, actionValue: IAction['value']) => {
    // 触发校验
    this.formRef.current?.validateFields(['applicationActions']);

    const nextActionMap = lodash.cloneDeep(this.state.allActionMap);
    nextActionMap[applicationId].value = actionValue;

    this.setState({
      allActionMap: nextActionMap,
    });
  };
  // ======== 修改应用的配置 E =======

  handleSubmit = (values: IFormValues) => {
    const { id, name, exceptFlow, description } = values;
    const { allApplicationList, allActionMap, defaultActionMap } = this.state;

    // 计算默认存储配置
    const defaultAction = defaultActionMap.value;
    // 例外的应用
    const exceptApplicationMap: Record<string, number> = {};
    for (let i = 0; i < allApplicationList.length; i += 1) {
      const { applicationId } = allApplicationList[i];
      const appAction = allActionMap[applicationId];
      if (appAction.key !== defaultActionMap.key || appAction.value !== defaultActionMap.value) {
        exceptApplicationMap[applicationId] = appAction.value as number;
      }
    }

    const nextExceptFlow = exceptFlow ? EExceptFlowAction.SAVE : EExceptFlowAction.NO_SAVE;
    const submitData = {
      id,
      name,
      exceptFlow: nextExceptFlow,
      defaultAction,
      exceptApplication: JSON.stringify(exceptApplicationMap),
      description,
    };

    Modal.confirm({
      title: '确定保存吗？',
      content: '',
      onOk: () => {
        if (id) {
          this.updateApplicationPolicy(submitData);
        } else {
          this.createApplicationPolicy(submitData);
        }
      },
    });
  };

  createApplicationPolicy = (data: any) => {
    const { dispatch } = this.props;
    (
      dispatch({
        type: 'applicationPolicyModel/createApplicationPolicy',
        payload: data,
      }) as unknown as Promise<any>
    ).then((success) => {
      if (success) {
        Modal.success({
          keyboard: false,
          title: '保存成功',
          okText: '返回列表页',
          onOk: () => {
            this.handleGoBack();
          },
        });
      }
    });
  };

  updateApplicationPolicy = (data: any) => {
    const { dispatch } = this.props;
    (
      dispatch({
        type: 'applicationPolicyModel/updateApplicationPolicy',
        payload: data,
      }) as unknown as Promise<any>
    ).then((success) => {
      if (success) {
        Modal.success({
          keyboard: false,
          title: '修改成功',
          okText: '返回列表页',
          onOk: () => {
            this.handleGoBack();
          },
        });
      }
    });
  };

  handleGoBack = () => {
    history.goBack();
  };

  render() {
    const {
      categoryIndeterminate,
      categoryCheckAll,
      checkedCategoryIds,

      subCategoryIndeterminate,
      subCategoryCheckAll,
      checkedSubCategoryIds,

      searchApplicationName,
      searchStoreAction,

      allApplicationList,
      allActionMap,
      defaultActionMap,
    } = this.state;
    const {
      SAKnowledgeModel: { allCategoryList, allSubCategoryList, applicationList },
      detail = {} as IApplicationPolicy,
      queryAllApplicationsLoading,
      submitLoading,
      showMode = false,
    } = this.props;

    const displaySubCategoryList = this.getDisplaySubCategoryList();
    const displayApplicationList = this.getDisplayApplicationList(
      displaySubCategoryList,
      checkedSubCategoryIds,
    );

    let filterApplicationList: ApplicationItem[] = displayApplicationList.slice();
    // 根据应用名称过滤一下
    if (searchApplicationName) {
      filterApplicationList = filterApplicationList.filter(
        (app) =>
          app.nameText &&
          app.nameText.toLocaleUpperCase().indexOf(searchApplicationName.toLocaleUpperCase()) > -1,
      );
    }

    // 不是全部的话
    if (searchStoreAction) {
      filterApplicationList = filterApplicationList.filter((app) => {
        // 判断下这个应用当前的存储策略
        if (allActionMap[app.applicationId]?.key === searchStoreAction) {
          return true;
        }
        return false;
      });
    }

    if (!queryAllApplicationsLoading && allCategoryList.length === 0) {
      return (
        <Result
          title="还没有上传SA规则库"
          extra={
            <Button
              type="primary"
              key="console"
              onClick={() => history.push(getLinkUrl('/configuration/sa-knowledge/upload'))}
            >
              上传SA规则库
            </Button>
          }
        />
      );
    }

    // 根据应用名称和过滤规则过滤出展示的应用
    const applicationColumns: ColumnProps<ApplicationItem>[] = [
      {
        title: '应用ID',
        dataIndex: 'applicationId',
        key: 'applicationId',
        align: 'center',
        width: 100,
      },
      {
        title: '应用名称',
        dataIndex: 'nameText',
        key: 'nameText',
        align: 'center',
        width: 300,
        render: (text, record) => <Tooltip title={record.descriptionText}>{text}</Tooltip>,
      },
      {
        title: '分类',
        dataIndex: 'categoryName',
        key: 'categoryName',
        align: 'center',
        width: 200,
        render: (text, record) => {
          const { categoryId } = record;
          const categoryInfo = allCategoryList.find((cate) => cate.categoryId === categoryId);
          if (categoryInfo) {
            return categoryInfo.nameText;
          }
          return '--';
        },
      },
      {
        title: '子分类',
        dataIndex: 'subCategoryName',
        key: 'subCategoryName',
        align: 'center',
        width: 200,
        render: (text, record) => {
          const { subCategoryId } = record;
          const subCategoryInfo = allSubCategoryList.find(
            (subCate) => subCate.subCategoryId === subCategoryId,
          );
          if (subCategoryInfo) {
            return subCategoryInfo.nameText;
          }
          return '--';
        },
      },
      {
        title: '操作',
        dataIndex: 'operate',
        key: 'operate',
        align: 'center',
        width: 400,
        render: (text, record) => {
          const { applicationId } = record;
          // 默认配置
          const appAction = allActionMap[applicationId] || {};
          return (
            <Radio.Group
              value={appAction.key}
              onChange={(e) => this.handleAppActionChange(applicationId, e.target.value)}
            >
              {ACTION_LIST.map((action) => (
                <Radio key={action.value} value={action.value}>
                  {action.label}
                  {action.value === ACTION_PART_SAVE && (
                    <InputNumber
                      disabled={appAction.key !== ACTION_PART_SAVE}
                      min={MIN_PART_SAVE_VALUE}
                      max={MAX_PART_SAVE_VALUE}
                      precision={0}
                      value={appAction.key === ACTION_PART_SAVE ? appAction.value : undefined}
                      onChange={(value) => this.handleAppPartSaveValueChange(applicationId, value)}
                      style={{ width: 100, marginLeft: 10 }}
                    />
                  )}
                </Radio>
              ))}
            </Radio.Group>
          );
        },
      },
    ];

    return (
      <Skeleton active loading={queryAllApplicationsLoading}>
        <Form
          ref={this.formRef}
          labelCol={{ span: 2 }}
          wrapperCol={{ span: 22 }}
          initialValues={{
            id: detail.id,
            name: detail.name,
            exceptFlow: detail.exceptFlow === EExceptFlowAction.SAVE,
            description: detail.description || '',
          }}
          scrollToFirstError
          onFinish={this.handleSubmit}
        >
          <Form.Item label="ID" name="id" hidden>
            <Input placeholder="id" />
          </Form.Item>
          <Form.Item
            label="名称"
            name="name"
            rules={[
              { required: true, message: '请输入规则名称' },
              { max: 30, message: '最多可输入30个字符' },
            ]}
          >
            <Input placeholder="请输入规则名称" />
          </Form.Item>
          <Form.Item
            label="会话应用识别前的流量存储"
            labelCol={{ span: 4 }}
            className={styles.extra}
          >
            <Form.Item noStyle name="exceptFlow" valuePropName="checked" rules={[]}>
              <Checkbox />
            </Form.Item>
            <span className="ant-form-text" style={{ marginLeft: 10 }}>
              <Alert
                message="不勾选，则会话中识别出DPI应用前的流量（例如TCP握手报文）不会存储，反之则存储；应用识别后，则按照具体应用配置执行存储策略。"
                type="info"
                showIcon
              />
            </span>
          </Form.Item>
          <Form.Item label="所有应用默认存储配置" labelCol={{ span: 4 }} required>
            <Form.Item
              noStyle
              label="所有应用默认存储配置"
              name="defaultActionKey"
              validateFirst
              shouldUpdate
              rules={[
                {
                  validator: async () => {
                    if (!defaultActionMap.value && defaultActionMap.value !== 0) {
                      throw new Error('请设置默认存储配置');
                    }
                  },
                },
              ]}
            >
              <Radio.Group onChange={this.handleDefaultActionChange}>
                <Radio value={ACTION_SAVE}>全量存储</Radio>
                <Radio value={ACTION_PART_SAVE}>
                  截断存储{' '}
                  <Tooltip
                    title={`截断范围在 ${MIN_PART_SAVE_VALUE} - ${MAX_PART_SAVE_VALUE} 之间`}
                  >
                    <QuestionCircleOutlined />
                  </Tooltip>
                  <InputNumber
                    disabled={defaultActionMap.key !== ACTION_PART_SAVE}
                    value={
                      defaultActionMap.key === ACTION_PART_SAVE ? defaultActionMap.value : undefined
                    }
                    min={MIN_PART_SAVE_VALUE}
                    max={MAX_PART_SAVE_VALUE}
                    precision={0}
                    style={{ width: 100, marginLeft: 10 }}
                    onChange={this.handlePartSaveValueChange}
                  />
                </Radio>
                <Radio value={ACTION_NO_SAVE}>不存储</Radio>
              </Radio.Group>
            </Form.Item>
          </Form.Item>
          <Form.Item label="应用配置">
            <Row gutter={10}>
              <Col span={4}>
                <Card
                  size="small"
                  title={
                    <>
                      <Checkbox
                        indeterminate={categoryIndeterminate}
                        checked={categoryCheckAll}
                        onChange={this.handleAllCategoryChecked}
                      >
                        分类
                      </Checkbox>
                    </>
                  }
                  className={styles.categoryCard}
                  style={{ marginBottom: 10 }}
                >
                  <Checkbox.Group
                    value={checkedCategoryIds}
                    style={{ width: '100%' }}
                    onChange={this.handleCategoryChange}
                  >
                    <Row>
                      {allCategoryList.map(({ categoryId, nameText }) => (
                        <Col span={24} key={`category_${categoryId}`}>
                          <Checkbox className={styles.categoryCheckbox} value={categoryId}>
                            {nameText}
                          </Checkbox>
                        </Col>
                      ))}
                    </Row>
                  </Checkbox.Group>
                </Card>
                <Card
                  size="small"
                  title={
                    <>
                      <Checkbox
                        indeterminate={subCategoryIndeterminate}
                        checked={subCategoryCheckAll}
                        onChange={this.handleAllSubCategoryChecked}
                      >
                        子分类
                      </Checkbox>
                    </>
                  }
                  className={styles.categoryCard}
                >
                  <Checkbox.Group
                    value={checkedSubCategoryIds}
                    style={{ width: '100%' }}
                    onChange={this.handleSubCategoryChange}
                  >
                    <Row>
                      {displaySubCategoryList.map(({ subCategoryId, nameText }) => (
                        <Col span={24} key={`subCategory_${subCategoryId}`}>
                          <Checkbox className={styles.categoryCheckbox} value={subCategoryId}>
                            {nameText}
                          </Checkbox>
                        </Col>
                      ))}
                    </Row>
                  </Checkbox.Group>
                </Card>
              </Col>
              <Col span={20}>
                {/* 搜索框 */}
                <section className="searchForm small">
                  <Row style={{ marginBottom: 10 }} gutter={10}>
                    <Col span={6}>
                      <Form.Item>所有应用共计{applicationList.length}个</Form.Item>
                    </Col>
                    <Col span={6}>
                      <Form.Item
                        label="应用名称"
                        name="applicationName"
                        initialValue={searchApplicationName}
                      >
                        <Input placeholder="应用名称" />
                      </Form.Item>
                    </Col>
                    <Col span={6}>
                      <Form.Item
                        label="存储规则"
                        name="storeAction"
                        initialValue={searchStoreAction || ''}
                      >
                        <Select>
                          <Option value="">全部</Option>
                          {ACTION_LIST.map((rule) => (
                            <Option key={rule.value} value={rule.value}>
                              {rule.label}
                            </Option>
                          ))}
                        </Select>
                      </Form.Item>
                    </Col>
                    <Col span={6} style={{ textAlign: 'right' }}>
                      <Form.Item style={{ marginRight: 0 }}>
                        <Button
                          icon={<SearchOutlined />}
                          type="primary"
                          onClick={this.handleSearch}
                        >
                          查询
                        </Button>
                        <Button
                          style={{ marginLeft: 10 }}
                          icon={<RollbackOutlined />}
                          onClick={this.handleResetSearch}
                        >
                          重置
                        </Button>
                      </Form.Item>
                    </Col>
                  </Row>
                </section>
                <Table
                  className={styles.appTable}
                  rowKey="applicationId"
                  size="small"
                  bordered
                  loading={queryAllApplicationsLoading}
                  dataSource={filterApplicationList}
                  columns={applicationColumns}
                  pagination={getTablePaginationDefaultSettings()}
                />
              </Col>
            </Row>
            <Form.Item
              name="applicationActions"
              style={{ marginBottom: 0 }}
              rules={[
                {
                  validator: async () => {
                    let isOk = true;
                    let errorAppName = '';
                    for (let i = 0; i < allApplicationList.length; i += 1) {
                      const { applicationId, nameText } = allApplicationList[i];
                      const appAction = allActionMap[applicationId];
                      if (!appAction.key || (!appAction.value && appAction.value !== 0)) {
                        isOk = false;
                        errorAppName = nameText;
                        break;
                      }
                    }
                    if (!isOk) {
                      throw new Error(`${errorAppName}存储配置没有填写完整`);
                    }
                  },
                },
              ]}
            >
              <Input style={{ display: 'none' }} />
            </Form.Item>
          </Form.Item>
          <Form.Item
            label="描述"
            name="description"
            rules={[
              { required: false, message: '请填写描述信息' },
              { max: 255, message: '最多可输入255个字符' },
            ]}
          >
            <Input.TextArea rows={4} />
          </Form.Item>
          <Form.Item>
            <div className={styles.operateWrap} style={{ display: showMode ? 'none' : '' }}>
              <Button type="primary" htmlType="submit" loading={submitLoading}>
                保存
              </Button>
              <Button
                onClick={() => {
                  history.goBack();
                }}
                loading={submitLoading}
              >
                返回
              </Button>
            </div>
          </Form.Item>
        </Form>
      </Skeleton>
    );
  }
}

export default connect(
  ({ applicationPolicyModel, SAKnowledgeModel, loading: { effects } }: ConnectState) => ({
    applicationPolicyModel,
    SAKnowledgeModel,
    queryAllApplicationsLoading: effects['SAKnowledgeModel/queryAllApplications'] || false,
    submitLoading:
      effects['applicationPolicyModel/updateApplicationPolicy'] ||
      effects['applicationPolicyModel/createApplicationPolicy'] ||
      false,
  }),
)(ApplicationFilterForm);
