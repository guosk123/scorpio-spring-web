/* eslint-disable no-restricted-syntax */
import { getTablePaginationDefaultSettings } from '@/common/app';
import { API_BASE_URL, API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import { BOOL_NO, BOOL_YES } from '@/common/dict';
import Import from '@/components/Import';
import LinkButton from '@/components/LinkButton';
import type { ConnectState } from '@/models/connect';
import { getLinkUrl } from '@/utils/utils';
import {
  ExportOutlined,
  PlusOutlined,
  QuestionCircleOutlined,
  RollbackOutlined,
  SearchOutlined,
} from '@ant-design/icons';
import {
  Button,
  Card,
  Checkbox,
  Col,
  Divider,
  Drawer,
  Form,
  Input,
  Popconfirm,
  Result,
  Row,
  Skeleton,
  Space,
  Table,
  Tooltip,
} from 'antd';
import type { CheckboxChangeEvent } from 'antd/lib/checkbox';
import type { CheckboxValueType } from 'antd/lib/checkbox/Group';
import type { FormInstance } from 'antd/lib/form';
import type { ColumnProps } from 'antd/lib/table';
import { connect } from 'dva';
import lodash from 'lodash';
import numeral from 'numeral';
import React, { createRef, Fragment } from 'react';
import type { Dispatch } from 'redux';
import type { SAKnowledgeModelState } from 'umi';
import { history } from 'umi';
import type { IApplicationPolicy } from '../ApplicationPolicy/typings';
import ConnectCmsState from '../components/ConnectCmsState';
import { MAX_CUSTOM_APPLICATION_LIMIT } from './CustomApplication/Create';
import Profile from './CustomApplication/Profile';
import styles from './index.less';
import type { AppCategoryItem, ApplicationItem, AppSubCategoryItem } from './typings';
import { ECustomSAApiType } from './typings';

interface ApplicationPolicyProps {
  dispatch: Dispatch<any>;
  SAKnowledgeModel: SAKnowledgeModelState;
  detail: IApplicationPolicy;
  queryAllApplicationsLoading: boolean | undefined;
  importLoading: boolean | undefined;
  location: {
    pathname: string;
    query: {
      onlyCustomApplication: '0' | '1';
    };
  };
}

interface ApplicationListState {
  categoryIndeterminate: boolean;
  categoryCheckAll: boolean;
  checkedCategoryIds: string[];

  subCategoryIndeterminate: boolean;
  subCategoryCheckAll: boolean;
  checkedSubCategoryIds: string[];

  searchApplicationName?: string;

  // 是否只看自定义应用
  onlyCustomApplication: boolean;
  // 当前查看详情的自定义应用 ID
  customApplicationId?: string;
  cmsConnectFlag: boolean;
}

class ApplicationList extends React.PureComponent<ApplicationPolicyProps, ApplicationListState> {
  constructor(props: ApplicationPolicyProps) {
    super(props);

    const {
      location: { query },
    } = this.props;

    this.state = {
      // 大类
      categoryIndeterminate: false,
      categoryCheckAll: true,
      checkedCategoryIds: [] as string[],
      // 小类
      subCategoryIndeterminate: false,
      subCategoryCheckAll: true,
      checkedSubCategoryIds: [],

      searchApplicationName: '',

      // 是否只看自定义应用
      onlyCustomApplication: query.onlyCustomApplication === BOOL_YES,
      // 当前查看详情的自定义应用 ID
      customApplicationId: '',
      cmsConnectFlag: false,
    };
  }

  formRef = createRef<FormInstance>();

  componentDidMount() {
    this.queryAllApplications();
  }

  queryAllApplications = () => {
    const { dispatch } = this.props;
    (
      dispatch({
        type: 'SAKnowledgeModel/queryAllApplications',
      }) as unknown as Promise<any>
    ).then(({ allCategoryList, allSubCategoryList }) => {
      this.setState({
        checkedCategoryIds: allCategoryList.map((item: AppCategoryItem) => item.categoryId),
        checkedSubCategoryIds: allSubCategoryList.map(
          (item: AppSubCategoryItem) => item.subCategoryId,
        ),
      });
    });
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
    });
    this.setState({
      searchApplicationName: '',
    });
  };

  handleSearch = () => {
    const { applicationName } = this.formRef.current?.getFieldsValue(['applicationName']);
    this.setState({
      searchApplicationName: applicationName,
    });
  };

  handleReset = () => {
    this.formRef.current?.resetFields();
  };

  handleCustomApplicationVisible = (e: CheckboxChangeEvent) => {
    const { location } = this.props;
    const onlyCustomApplication = e.target.checked;
    this.setState({
      onlyCustomApplication,
    });
    history.replace({
      pathname: location.pathname,
      query: {
        onlyCustomApplication: onlyCustomApplication ? BOOL_YES : BOOL_NO,
      },
    });
  };

  handleDeleteCustomApplication = ({ id }: ApplicationItem) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'customSAModel/deleteCustomSA',
      payload: { id, type: ECustomSAApiType.APPLICATION },
    } as unknown as Promise<any>).then(() => this.queryAllApplications());
  };

  // 自定义应用详情预览
  handleShowDrawer = (applicationId: string) => {
    this.setState({
      customApplicationId: applicationId,
    });
  };

  handleCloseDrawer = () => {
    this.setState({
      customApplicationId: '',
    });
  };
  handleAfterDrawerClose = (visible: boolean) => {
    if (!visible) {
      const { dispatch } = this.props;
      dispatch({
        type: 'customSAModel/updateState',
        payload: { customApplicationDetail: {} },
      });
    }
  };

  renderCreateButton = () => {
    const {
      queryAllApplicationsLoading,
      SAKnowledgeModel: { customApplicationList },
    } = this.props;

    if (queryAllApplicationsLoading) {
      return (
        <Button type="primary" loading disabled={this.state.cmsConnectFlag}>
          新建自定义应用
        </Button>
      );
    }

    // 超出最大限制
    if (customApplicationList.length >= MAX_CUSTOM_APPLICATION_LIMIT) {
      return (
        <Tooltip title={`最多支持新建${MAX_CUSTOM_APPLICATION_LIMIT}个自定义应用`}>
          <Button
            icon={<PlusOutlined />}
            type="primary"
            disabled={true && this.state.cmsConnectFlag}
          >
            新建自定义应用
          </Button>
        </Tooltip>
      );
    }

    return (
      <Button
        icon={<PlusOutlined />}
        type="primary"
        onClick={() =>
          history.push(getLinkUrl('/configuration/objects/sa-knowledge/application/create'))
        }
        disabled={this.state.cmsConnectFlag}
      >
        新建自定义应用
      </Button>
    );
  };

  /**
   * 删除自定义分类或子分类
   * @param id
   * @param type
   */
  handleDeleteCustom = (id: string, type: ECustomSAApiType) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'customSAModel/deleteCustomSA',
      payload: { id, type },
    });
  };

  handleExport = () => {
    const url = `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/appliance/sa/as-export`;
    window.open(url);
  };

  renderDeleteBtn = (
    id: string,
    type: ECustomSAApiType,
    children?: any[],
    flag: boolean = false,
  ) => {
    if (children && children.length > 0) {
      return (
        <Tooltip title={`包含${type === ECustomSAApiType.CATEGORY ? '子分类' : '应用'}，无法删除`}>
          <Button type={'link'} size={'small'} disabled={flag}>
            删除
          </Button>
        </Tooltip>
      );
    }

    return (
      <Popconfirm
        title="确定删除吗？"
        onConfirm={() => this.handleDeleteCustom(id!, type)}
        icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
        disabled={flag}
      >
        <Button type={'link'} size={'small'} disabled={flag}>
          删除
        </Button>
      </Popconfirm>
    );
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

      customApplicationId,
      onlyCustomApplication,
      cmsConnectFlag,
    } = this.state;
    const {
      SAKnowledgeModel: { allCategoryList, allSubCategoryList, applicationList },
      queryAllApplicationsLoading,
      importLoading,
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

    // 是否只展示自定义规则
    if (onlyCustomApplication) {
      filterApplicationList = filterApplicationList.filter((app) => app.isCustom);
    }

    if (!queryAllApplicationsLoading && allCategoryList.length === 0) {
      return (
        <Result
          title="还没有上传SA规则库"
          extra={
            <Button
              type="primary"
              key="console"
              disabled={cmsConnectFlag}
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
        width: 140,
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
        render: (text, record) => {
          const { subCategoryId } = record;
          const subCategoryInfo = allSubCategoryList.find(
            (subCate) => subCate.subCategoryId === subCategoryId,
          );
          if (!subCategoryInfo) {
            return '--';
          }
          const { categoryId } = subCategoryInfo;
          // 根据子分类的查找子分类的父节点
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
        width: 150,
        render: (text, record) => {
          const { isCustom, id } = record;

          if (!isCustom) {
            return null;
          }
          return (
            <Fragment>
              <a onClick={() => this.handleShowDrawer(id!)}>详情</a>
              <Divider type="vertical" />
              <LinkButton
                onClick={() => {
                  history.push(
                    getLinkUrl(
                      `/configuration/objects/sa-knowledge/application/${record.id}/update`,
                    ),
                  );
                }}
                disabled={cmsConnectFlag}
              >
                编辑
              </LinkButton>
              <Divider type="vertical" />
              <Popconfirm
                title="确定删除吗？"
                onConfirm={() => this.handleDeleteCustomApplication(record)}
                icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
                disabled={cmsConnectFlag}
              >
                <Button type="link" size="small" disabled={cmsConnectFlag}>
                  删除
                </Button>
              </Popconfirm>
            </Fragment>
          );
        },
      },
    ];

    return (
      <Skeleton active loading={queryAllApplicationsLoading}>
        <ConnectCmsState
          onConnectFlag={(flag: boolean) => {
            this.setState({
              ...this.state,
              cmsConnectFlag: flag,
            });
          }}
        />
        <Row gutter={10}>
          <Col span={5}>
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
              extra={
                <Button
                  onClick={() => {
                    history.push(getLinkUrl('/configuration/objects/sa-knowledge/category/create'));
                  }}
                  disabled={cmsConnectFlag}
                  size={'small'}
                  type={'link'}
                >
                  新建
                </Button>
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
                  {allCategoryList.map(
                    ({ id, isCustom, categoryId, nameText, subCategoryList }) => (
                      <Col span={24} key={`category_${categoryId}`}>
                        <div className={styles.row}>
                          <Checkbox className={styles.categoryCheckbox} value={categoryId}>
                            {nameText}
                          </Checkbox>
                          {/* 操作 */}
                          {isCustom && (
                            <div className={styles.operateWrap}>
                              <Button
                                type={'link'}
                                size={'small'}
                                disabled={cmsConnectFlag}
                                onClick={() => {
                                  history.push(
                                    getLinkUrl(
                                      `/configuration/objects/sa-knowledge/category/${id}/update`,
                                    ),
                                  );
                                }}
                              >
                                编辑
                              </Button>
                              <Divider type="vertical" />
                              {this.renderDeleteBtn(
                                id!,
                                ECustomSAApiType.CATEGORY,
                                subCategoryList,
                                cmsConnectFlag,
                              )}

                              {/* <Popconfirm
                                title="确定删除吗？"
                                onConfirm={() => this.handleDeleteCustomCategory(id!)}
                                icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
                              >
                                <span className="link">删除</span>
                              </Popconfirm> */}
                            </div>
                          )}
                        </div>
                      </Col>
                    ),
                  )}
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
              extra={
                <Button
                  type={'link'}
                  size={'small'}
                  disabled={cmsConnectFlag}
                  onClick={() => {
                    history.push(
                      getLinkUrl('/configuration/objects/sa-knowledge/subcategory/create'),
                    );
                  }}
                >
                  新建
                </Button>
              }
              className={styles.categoryCard}
            >
              <Checkbox.Group
                value={checkedSubCategoryIds}
                style={{ width: '100%' }}
                onChange={this.handleSubCategoryChange}
              >
                <Row>
                  {displaySubCategoryList.map(
                    ({ subCategoryId, nameText, isCustom, id, applicationList: applications }) => (
                      <Col span={24} key={`subCategory_${subCategoryId}`}>
                        <div className={styles.row}>
                          <Checkbox className={styles.categoryCheckbox} value={subCategoryId}>
                            {nameText}
                          </Checkbox>
                          {/* 操作 */}
                          {isCustom && (
                            <div className={styles.operateWrap}>
                              <Button
                                type={'link'}
                                size={'small'}
                                disabled={cmsConnectFlag}
                                onClick={() => {
                                  history.push(
                                    getLinkUrl(
                                      `/configuration/objects/sa-knowledge/subcategory/${id}/update`,
                                    ),
                                  );
                                }}
                              >
                                编辑
                              </Button>
                              <Divider type="vertical" />
                              {this.renderDeleteBtn(
                                id!,
                                ECustomSAApiType.SUB_CATEGORY,
                                applications,
                                cmsConnectFlag,
                              )}
                              {/* <Popconfirm
                              title="确定删除吗？"
                              onConfirm={() => this.handleDeleteCustomSubCategory(id!)}
                              icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
                            >
                              <span className="link">删除</span>
                            </Popconfirm> */}
                            </div>
                          )}
                        </div>
                      </Col>
                    ),
                  )}
                </Row>
              </Checkbox.Group>
            </Card>
          </Col>
          <Col span={19}>
            {/* 搜索框 */}
            <section className="searchForm small">
              <Row style={{ marginBottom: 10 }} gutter={10}>
                <Col xl={24}>
                  <Form.Item>
                    所有应用共计{numeral(applicationList.length).format('0,0')}个
                  </Form.Item>
                </Col>
                <Col xl={24}>
                  <Form
                    layout="inline"
                    ref={this.formRef}
                    style={{ display: 'flex', justifyContent: 'flex-end' }}
                    onFinish={this.handleSearch}
                  >
                    <Form.Item
                      label="应用名称"
                      name="applicationName"
                      initialValue={searchApplicationName}
                    >
                      <Input placeholder="应用名称" />
                    </Form.Item>
                    <Form.Item style={{ marginRight: 0 }}>
                      <Space>
                        <Checkbox
                          checked={onlyCustomApplication}
                          onChange={this.handleCustomApplicationVisible}
                        >
                          只看自定义应用
                        </Checkbox>
                        <Button
                          icon={<SearchOutlined />}
                          type="primary"
                          onClick={this.handleSearch}
                        >
                          查询
                        </Button>
                        <Button icon={<RollbackOutlined />} onClick={this.handleResetSearch}>
                          重置
                        </Button>
                        <Divider type="vertical" />
                        {this.renderCreateButton()}
                        <Import
                          loading={importLoading}
                          modalTitle={`导入`}
                          importFunc="customSAModel/importCustomSA"
                          tempDownloadUrl={`/appliance/sa/as-template`}
                          importSuccessCallback={this.queryAllApplications}
                          disabled={cmsConnectFlag}
                        />

                        <Button onClick={() => this.handleExport()}>
                          <ExportOutlined /> 导出
                        </Button>
                      </Space>
                    </Form.Item>
                  </Form>
                </Col>
              </Row>
            </section>
            <Table
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
        {/* 自定义应用详情弹出框 */}
        <Drawer
          title="自定义应用详情"
          width={650}
          visible={!!customApplicationId}
          onClose={this.handleCloseDrawer}
          afterVisibleChange={this.handleAfterDrawerClose}
        >
          {customApplicationId && <Profile id={customApplicationId} />}
        </Drawer>
      </Skeleton>
    );
  }
}

export default connect(({ SAKnowledgeModel, loading: { effects } }: ConnectState) => ({
  SAKnowledgeModel,
  queryAllApplicationsLoading: effects['SAKnowledgeModel/queryAllApplications'] || false,
  importLoading: effects['customSAModel/importCustomSA'],
}))(ApplicationList);
