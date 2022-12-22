/* eslint-disable max-classes-per-file */
import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { DeleteOutlined, FormOutlined, KeyOutlined, SearchOutlined } from '@ant-design/icons';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Modal, Table, Divider, Button, Popconfirm, Input, Row, Col, message, Card } from 'antd';
import { handleRefreshPage, handleTableChange, compareUri, handleQueryData } from '@/utils/utils';

import styles from './index.less';

const FormItem = Form.Item;
const { TextArea, Search } = Input;

// 创建编辑角色弹出框
const RoleForm = Form.create()(
  class extends React.Component {
    render() {
      const {
        operateType,
        roleModalVisible,
        form,
        handleRoleSubmit,
        handleRoleModalVisible,
        loading,
        selectedRole,
      } = this.props;

      const okHandle = () => {
        form.validateFieldsAndScroll((err, fieldsValue) => {
          if (err) return;
          handleRoleSubmit(fieldsValue);
        });
      };

      // 表单布局
      const formItemLayout = {
        labelCol: {
          xs: { span: 24 },
          sm: { span: 5 },
        },
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 17 },
        },
      };
      return (
        <Modal
          width={600}
          title={operateType === 'ADD' ? '添加角色' : '编辑角色'}
          visible={roleModalVisible}
          maskClosable={false}
          destroyOnClose
          keyboard={false}
          confirmLoading={loading}
          okText="保存"
          onOk={okHandle}
          onCancel={() => handleRoleModalVisible()}
        >
          <Form>
            <FormItem {...formItemLayout} label="id" style={{ display: 'none' }}>
              {form.getFieldDecorator('id', {
                initialValue: selectedRole ? selectedRole.id : '',
              })(<Input type="hidden" />)}
            </FormItem>

            <FormItem {...formItemLayout} label="中文名称">
              {form.getFieldDecorator('nameZh', {
                initialValue: selectedRole ? selectedRole.nameZh : '',
                validateFirst: true,
                rules: [
                  {
                    required: true,
                    whitespace: true,
                    message: '请填写角色中文名称',
                  },
                  {
                    pattern: /^[\u4E00-\u9FA5_-]+$/,
                    message: '只能输入中文',
                  },
                  {
                    max: 30,
                    message: '最长可输入30个字符',
                  },
                ],
              })(<Input placeholder="请填写角色中文名称" />)}
            </FormItem>
            <FormItem {...formItemLayout} label="英文名称">
              {form.getFieldDecorator('nameEn', {
                initialValue: selectedRole ? selectedRole.nameEn : '',
                validateFirst: true,
                rules: [
                  {
                    required: true,
                    whitespace: true,
                    message: '请填写角色英文名称',
                  },
                  {
                    pattern: /^[A-Za-z_-]+$/,
                    message: '只能输入大小写英文字母和下划线_',
                  },
                  {
                    max: 30,
                    message: '最长可输入30个字符',
                  },
                ],
              })(<Input placeholder="请填写角色英文名称" />)}
            </FormItem>
            <FormItem {...formItemLayout} label="备注">
              {form.getFieldDecorator('description', {
                initialValue: selectedRole ? selectedRole.description : '',
                rules: [
                  {
                    max: 255,
                    message: '最长可输入255个字符',
                  },
                ],
              })(<TextArea rows={4} placeholder="备注" />)}
            </FormItem>
          </Form>
        </Modal>
      );
    }
  },
);

// 编辑角色的权限弹出框
const PermissionForm = Form.create()(
  class extends React.Component {
    state = {
      keyword: null,
    };

    // 根据关键字搜索权限
    searchPerm = (keyword) => {
      this.setState({
        keyword,
      });
    };

    render() {
      const {
        permissionModalVisible,
        handlePermissionSubmit,
        handlePermissionModalVisible,
        handlePermissionChange,
        selectedRole,
        selectedPerm,
        allPerms,
        loading,
      } = this.props;

      // 根据关键字过滤权限
      const { keyword } = this.state;
      const filterPermList = [];
      allPerms.forEach((element) => {
        if (!element.nameEn || !element.nameZh) {
          return;
        }

        if (
          element.nameZh.indexOf(keyword) > -1 ||
          element.nameEn.toLowerCase().indexOf(keyword ? keyword.toLowerCase() : '') > -1
        ) {
          filterPermList.push({
            ...element,
            key: element.id,
          });
        }
      });

      // 设置权限提交
      const okHandle = () => {
        if (!selectedPerm || selectedPerm.length === 0) {
          message.warning('请至少选择一个权限');
          return;
        }
        if (!selectedRole) return;
        // 提交权限
        handlePermissionSubmit(selectedRole.id, selectedPerm.join(','));
      };
      // 权限选择
      const onSelectChange = (perms) => {
        handlePermissionChange(perms);
      };

      const rowSelection = {
        selectedRowKeys: selectedPerm,
        onChange: onSelectChange,
      };

      const columns = [
        {
          title: '中文名称',
          dataIndex: 'nameZh',
          key: 'nameZh',
          width: 150,
        },
        {
          title: '英文名称',
          dataIndex: 'nameEn',
          key: 'nameEn',
          width: 150,
        },
        {
          title: '备注',
          dataIndex: 'description',
          key: 'description',
          width: 150,
        },
      ];

      return (
        <Modal
          width={700}
          title={selectedRole && `${selectedRole.nameZh} -权限设置`}
          visible={permissionModalVisible}
          maskClosable={false}
          destroyOnClose
          keyboard={false}
          onOk={okHandle}
          confirmLoading={loading}
          onCancel={() => handlePermissionModalVisible()}
        >
          {/* 权限搜索 */}
          <Row>
            <Col>
              <Search
                className={styles.searchInput}
                placeholder="根据中/英文名称搜索权限"
                enterButton={
                  <span>
                    <SearchOutlined />
                    查询
                  </span>
                }
                onSearch={(value) => this.searchPerm(value)}
              />
            </Col>
          </Row>

          {/* 所有的权限 */}
          <Table
            rowKey="id"
            dataSource={filterPermList}
            columns={columns}
            bordered
            pagination={false}
            rowSelection={rowSelection} // 可选择
            scroll={{ y: 240 }}
            locale={{ emptyText: '未搜索到相关权限' }}
          />
        </Modal>
      );
    }
  },
);

@connect((state) => {
  const {
    rolesModel: { roles: rolesList, pagination },
    permsModel: { allPerms },
    loading,
  } = state;
  const { effects } = loading;
  return {
    rolesList,
    allPerms,
    pagination,
    loading: effects['rolesModel/queryRoles'],
    updateLoading: effects['rolesModel/updateRole'],
    addLoading: effects['rolesModel/createRole'],
    changePermLoading: effects['permsModel/updateRolePerm'],
  };
})
class Role extends PureComponent {
  constructor(props) {
    super(props);

    this.handleRefreshPage = handleRefreshPage.bind(this);
    this.handleTableChange = handleTableChange.bind(this);
    this.compareUri = compareUri.bind(this);
    this.handleQueryData = handleQueryData.bind(this);
  }

  state = {
    operateType: 'ADD', // ADD: 增加，UPDATE: 编辑
    selectedRole: null, // 当前编辑的角色
    selectedPerm: [], // 某个角色设置的权限
    roleModalVisible: false, // 添加、编辑角色弹出框
    permissionModalVisible: false, // 角色权限编辑弹出框
  };

  componentDidMount() {}

  queryRoles = (newQuery = {}) => {
    this.handleQueryData(newQuery, 'rolesModel/queryRoles');
  };

  // 角色的新增和编辑
  handleRoleSubmit = (fields) => {
    const { operateType } = this.state;
    const { dispatch } = this.props;

    const dispatchType = operateType === 'ADD' ? 'rolesModel/createRole' : 'rolesModel/updateRole';

    dispatch({
      type: dispatchType,
      payload: {
        ...fields,
      },
    }).then((cbData) => {
      if (cbData) {
        this.setState({
          selectedRole: null, // 当前选中的行
          roleModalVisible: false,
        });

        this.queryRoles();
      }
    });
  };

  // 添加角色弹出框
  handleRoleModalVisible = (flag, type) => {
    this.handleRestForm(flag);

    this.setState({
      operateType: type,
      roleModalVisible: !!flag,
    });
  };

  // 重置表单
  handleRestForm = (flag) => {
    // 如果是弹框关闭，重置数据
    if (!flag) {
      this.setState({
        selectedPerm: [],
        selectedRole: null, // 当前选中的行
      });
    }
  };

  // 点击编辑，开始编辑
  handleUpdate = (record) => {
    if (!record) return;
    this.setState({
      selectedRole: record, // 当前选中的行
      selectedPerm: record.perms.map((perm) => perm.id),
    });

    this.handleRoleModalVisible(true, 'UPDATE');
  };

  // 删除
  handleDelete = (id) => {
    const { rolesList, pagination, dispatch } = this.props;
    dispatch({
      type: 'rolesModel/deleteRole',
      payload: id,
    }).then((success) => {
      if (success) {
        this.queryRoles({
          page:
            // 如果删除前只有1条数据，并且页码不是1，就请求上一页数据
            rolesList.length === 1 && pagination.current > 1
              ? pagination.current - 1
              : pagination.current,
        });
      }
    });
  };

  // !!! 注意，当 form 在弹出框中时，需要代理一下
  // @see: https://ant.design/components/form/#components-form-demo-form-in-modal
  saveRoleFormRef = (formRef) => {
    this.roleFormRef = formRef;
  };

  savePermFormRef = (formRef) => {
    this.permFormRef = formRef;
  };

  // =====编辑权限====
  // 编辑权限
  handleChangePermission = (record) => {
    if (!record) return;
    this.setState({
      selectedRole: record, // 当前选中的行
      selectedPerm: record.perms.map((perm) => perm.id),
    });

    this.handlePermissionModalVisible(true);
  };

  handlePermissionModalVisible = (flag) => {
    const { dispatch } = this.props;
    // 弹出框出现时，取权限的信息
    if (flag) {
      dispatch({
        type: 'permsModel/queryAllPerms',
      });
    } else {
      this.setState({
        selectedRole: null,
      });
    }
    this.setState({
      permissionModalVisible: !!flag,
    });
  };

  handlePermissionChange = (selectedPerm) => {
    this.setState({
      selectedPerm,
    });
  };

  handlePermissionSubmit = (roleId, permIds) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'permsModel/updateRolePerm',
      payload: { roleId, permIds },
    }).then((cbData) => {
      if (cbData) {
        // 取最新的角色列表
        this.queryRoles();

        this.setState({
          selectedPerm: [],
          permissionModalVisible: false,
        });
      } else {
        this.setState({
          permissionModalVisible: false,
        });
      }
    });
  };

  render() {
    const {
      rolesList,
      allPerms,
      pagination,
      loading,
      addLoading,
      updateLoading,
      changePermLoading,
    } = this.props;

    const { selectedRole } = this.state;

    // 角色弹出框所需参数
    const { roleModalVisible, operateType } = this.state;
    // 权限弹出框所需参数
    const { permissionModalVisible, selectedPerm } = this.state;

    // 分页信息
    const paginationProps = { ...pagination };

    const columns = [
      {
        title: '中文名称',
        dataIndex: 'nameZh',
        key: 'nameZh',
        align: 'center',
        width: 200,
      },
      {
        title: '英文名称',
        dataIndex: 'nameEn',
        key: 'nameEn',
        align: 'center',
        width: 200,
      },
      {
        title: '备注',
        dataIndex: 'description',
        key: 'description',
        align: 'center',
        width: 200,
      },
      {
        title: '功能项数量',
        dataIndex: 'perms',
        key: 'perms',
        align: 'center',
        width: 200,
        render: (text) => <span>{text.length}</span>,
      },
      {
        title: '操作',
        dataIndex: 'operate',
        key: 'operate',
        align: 'center',
        width: 300,
        render: (text, record) => (
          <span>
            <Button size="small" icon={<FormOutlined />} onClick={() => this.handleUpdate(record)}>
              编辑
            </Button>
            <Divider type="vertical" />
            <Popconfirm title="确定删除吗?" onConfirm={() => this.handleDelete(record.id)}>
              <Button size="small" icon={<DeleteOutlined />}>
                删除
              </Button>
            </Popconfirm>
            <Divider type="vertical" />
            <Button
              size="small"
              icon={<KeyOutlined />}
              onClick={() => this.handleChangePermission(record)}
            >
              权限
            </Button>
          </span>
        ),
      },
    ];

    return (
      <div>
        <div>
          <Card
            headStyle={{ borderBottom: 'none' }}
            bodyStyle={{ padding: 0 }}
            bordered={false}
            extra={
              <Button type="primary" onClick={() => this.handleRoleModalVisible(true, 'ADD')}>
                添加
              </Button>
            }
          >
            <Table
              style={{ marginBottom: 20 }}
              rowKey="id"
              loading={loading}
              onSelectRow={this.handleSelectRows}
              bordered
              dataSource={rolesList}
              columns={columns}
              pagination={paginationProps}
              onChange={this.handleTableChange}
            />
          </Card>
        </div>

        {/* 添加、编辑角色 */}
        <RoleForm
          wrappedComponentRef={this.saveRoleFormRef}
          operateType={operateType}
          handleRoleSubmit={this.handleRoleSubmit}
          handleRoleModalVisible={this.handleRoleModalVisible}
          roleModalVisible={roleModalVisible}
          selectedRole={selectedRole}
          loading={addLoading || updateLoading}
        />

        {/* 权限弹出框 */}
        <PermissionForm
          wrappedComponentRef={this.savePermFormRef}
          handlePermissionSubmit={this.handlePermissionSubmit}
          handlePermissionModalVisible={this.handlePermissionModalVisible}
          handlePermissionChange={this.handlePermissionChange}
          permissionModalVisible={permissionModalVisible}
          selectedRole={selectedRole}
          selectedPerm={selectedPerm}
          allPerms={allPerms}
          loading={changePermLoading}
          handlePermissionSearch={this.handlePermissionSearch}
        />
      </div>
    );
  }
}

export default Role;
