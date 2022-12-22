/* eslint-disable max-classes-per-file */
import { getTablePaginationDefaultSettings, pageSizeOptions } from '@/common/app';
import storage from '@/utils/frame/storage';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { SettingOutlined } from '@ant-design/icons';
import { Checkbox, Col, Modal, Row, Table, Tooltip } from 'antd';
import PropTypes from 'prop-types';
import React, { PureComponent } from 'react';
import styles from './index.less';

export const SETTING_OPERATE_KEY = 'settingOperate';

class StandardTable extends PureComponent {
  static defaultProps = {
    data: {},
    showPagination: true,
    showIndex: true,
    showColumnsSetting: false,
    onChange: () => {},
  };

  static propTypes = {
    columns: PropTypes.arrayOf(
      PropTypes.shape({
        title: PropTypes.string.isRequired,
        dataIndex: PropTypes.string.isRequired,
        disabled: PropTypes.bool,
      }).isRequired,
    ).isRequired,
    rowKey: PropTypes.string.isRequired,
    // 数据和分页
    data: PropTypes.shape({
      list: PropTypes.array.isRequired,
      pagination: PropTypes.oneOfType([
        PropTypes.shape({
          current: PropTypes.number,
          pageSize: PropTypes.number,
          total: PropTypes.number,
        }),
        PropTypes.bool,
      ]),
    }),
    showIndex: PropTypes.bool,
    showPagination: PropTypes.bool, // 是否显示分页
    onChange: PropTypes.func,
    showColumnsSetting: PropTypes.bool, // 是否可以自定义列
  };

  constructor(props) {
    super(props);

    const { tableName, columns, showColumnsSetting } = props;

    let tableColumnsProps = columns || [];

    // 如果开启自定义设置，从还原本地设置
    if (showColumnsSetting && tableName) {
      let localColumnKeys = storage.get(tableName);
      localColumnKeys = localColumnKeys ? localColumnKeys.split(',') : [];

      tableColumnsProps = tableColumnsProps.map((item) => ({
        ...item,
        disabled:
          localColumnKeys.length > 0
            ? localColumnKeys.indexOf(item.dataIndex) === -1
            : item.disabled,
        title:
          item.dataIndex === SETTING_OPERATE_KEY ? (
            <span>
              操作
              <Tooltip title="自定义显示列">
                <SettingOutlined className={styles.customIcon} onClick={this.handleColumnSetting} />
              </Tooltip>
            </span>
          ) : (
            item.title
          ),
      }));
    }

    this.state = {
      selectedRowKeys: [],
      tableColumns: tableColumnsProps, // 表格显示列表
    };
  }

  componentDidMount() {}

  componentWillUnmount() {}

  static getDerivedStateFromProps(nextProps) {
    // clean state
    if (nextProps.selectedRowKey && nextProps.selectedRowKey.length === 0) {
      return {
        selectedRowKeys: [],
      };
    }
    return null;
  }

  handleRowSelectChange = (selectedRowKeys, selectedRows) => {
    const { onSelectRow } = this.props;
    if (onSelectRow) {
      onSelectRow(selectedRows);
    }

    this.setState({ selectedRowKeys });
  };

  handleTableChange = (pageObj, filters, sorter) => {
    const { onChange } = this.props;
    const { current, pageSize } = pageObj;
    const {
      data: { pagination = {} },
    } = this.props;
    if (onChange) {
      // pageSize 发生变化时，页码从 1 开始，否则跳转到选择的页码
      onChange(pageSize === pagination.pageSize ? current : 1, pageSize, filters, sorter);
    }
  };

  // 自定义设置列
  handleColumnSetting = () => {
    const colSpan = 12;
    const minColumnNumber = 5;
    const { tableColumns } = this.state;

    const showKeys = tableColumns.filter((item) => !item.disabled).map((item) => item.dataIndex);

    // fix: 这里写成纯函数后，无法获取 form
    const SettingForm = Form.create({ name: 'column_setting_form' })(
      class CustomForm extends React.Component {
        validLength = (rule, value, callback) => {
          if (value.length < minColumnNumber) {
            callback(`请至少显示${minColumnNumber}列`);
          } else {
            callback();
          }
        };

        render() {
          const {
            form: { getFieldDecorator },
            onOk,
          } = this.props;
          return (
            <Form layout="horizontal" onSubmit={onOk} style={{ marginTop: 20 }}>
              <Form.Item>
                {getFieldDecorator('dataIndex', {
                  initialValue: showKeys,
                  rules: [
                    {
                      validator: this.validLength,
                    },
                  ],
                })(
                  <Checkbox.Group style={{ width: '100%' }}>
                    <Row>
                      {tableColumns.map(
                        ({ dataIndex, title }) =>
                          dataIndex !== SETTING_OPERATE_KEY && (
                            <Col span={colSpan} key={dataIndex}>
                              <Checkbox value={dataIndex}>{title}</Checkbox>
                            </Col>
                          ),
                      )}
                      <Col span={colSpan}>
                        <Checkbox disabled value={SETTING_OPERATE_KEY}>
                          操作
                        </Checkbox>
                      </Col>
                    </Row>
                  </Checkbox.Group>,
                )}
              </Form.Item>
            </Form>
          );
        }
      },
    );

    const saveformRef = (formRef) => {
      this.formRef = formRef;
    };

    const handleOk = () => {
      const { tableName } = this.props;
      const { form } = this.formRef.props;
      form.validateFields((err, { dataIndex }) => {
        if (err) {
          return;
        }

        confirmModal.update({
          okButtonProps: {
            loading: true,
          },
        });
        // 更改 state
        const newTableColumns = tableColumns.map((item) => ({
          ...item,
          disabled: dataIndex.indexOf(item.dataIndex) === -1,
        }));
        this.setState({
          tableColumns: newTableColumns,
        });

        // 更改 locationStorage
        storage.put(tableName, dataIndex);

        confirmModal.update({
          okButtonProps: {
            loading: false,
          },
        });
        confirmModal.destroy();
      });
    };

    const confirmModal = Modal.confirm({
      width: 500,
      title: '自定义列',
      icon: <SettingOutlined />,
      className: styles.columnSettingModal,
      content: <SettingForm wrappedComponentRef={saveformRef} onOk={handleOk} />,
      okText: '确定',
      autoFocusButton: true,
      cancelText: '取消',
      onOk: () => {
        handleOk();
        return Promise.reject();
      },
      onCancel: () => {},
    });
  };

  cleanSelectedKeys = () => {
    this.handleRowSelectChange([], []);
  };

  render() {
    const { selectedRowKeys, tableColumns } = this.state;
    // showPagination 为 false 时不分页
    const {
      columns,
      data = {},
      showPagination = true,
      showIndex = true,
      rowKey,
      onChange,
      onSelectRow,
      ...rest
    } = this.props;
    const { list = [], pagination } = data;

    const paginationProps =
      // eslint-disable-next-line no-nested-ternary
      typeof pagination === 'boolean'
        ? pagination
        : showPagination
        ? {
            hideOnSinglePage: false,
            showQuickJumper: true,
            showSizeChanger: true,
            pageSizeOptions,
            showTotal: (total) => `共 ${total} 条`,
            ...getTablePaginationDefaultSettings(),
            ...pagination,
          }
        : false;

    // 过滤可用的表格列
    const enabledColumns = tableColumns.filter((item) => !item.disabled);

    if (showIndex) {
      // 表格统一添加行号
      enabledColumns.unshift({
        title: '#',
        dataIndex: 'index',
        align: 'center',
        width: 60,
        ellipsis: true,
        render: (text, record, index) => {
          const number = index + 1;
          if (!pagination) {
            return number;
          }
          const { current, pageSize } = pagination;
          return (current - 1) * Math.abs(pageSize) + number;
        },
      });
    }
    let scroll = {};
    // eslint-disable-next-line no-prototype-builtins
    const hasFixed = enabledColumns.some((el) => el.hasOwnProperty('fixed'));
    if (hasFixed) {
      scroll = { x: 'max-content' };
    }

    // eslint-disable-next-line no-unused-vars
    const rowSelection = onSelectRow
      ? {
          selectedRowKeys,
          onChange: this.handleRowSelectChange,
          getCheckboxProps: (record) => ({
            disabled: record.disabled,
          }),
        }
      : null;

    return (
      <div className={styles.standardTable}>
        <Table
          columns={enabledColumns}
          rowKey={rowKey || 'id'}
          rowSelection={rowSelection}
          bordered
          size="small"
          // sticky
          dataSource={list}
          pagination={paginationProps}
          onChange={this.handleTableChange}
          scroll={{ scrollToFirstRowOnChange: true, ...scroll }}
          {...rest}
        />
      </div>
    );
  }
}

export default StandardTable;
