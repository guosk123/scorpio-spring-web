import { QuestionCircleOutlined } from '@ant-design/icons';
import { Form, Popconfirm, Select, Table, Divider, message } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import React, { forwardRef, useImperativeHandle, useState } from 'react';
import type { INetworkPolicy } from '../../typings';

export interface IPolicy {
  id: string;
  name: string;
}

interface EditableCellProps extends React.HTMLAttributes<HTMLElement> {
  editing?: boolean;
  dataIndex: string;
  title: any;
  record: INetworkPolicy;
  index: number;
  policyList: IPolicy[];
  children: React.ReactNode;
}

const EditableCell: React.FC<EditableCellProps> = ({
  editing = false,
  dataIndex,
  title,
  record,
  index,
  children,
  policyList,
  ...restProps
}) => {
  return (
    <td {...restProps}>
      {editing ? (
        <Form.Item
          name={dataIndex}
          style={{ margin: 0 }}
          rules={[
            {
              required: true,
              message: `请选择${title}`,
            },
          ]}
        >
          <Select size="small" style={{ width: 200 }}>
            {policyList.map((item) => (
              <Select.Option key={item.id} value={item.id}>
                {item.name}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
      ) : (
        children
      )}
    </td>
  );
};

export interface IEditableTableRefReturn {
  reset: () => void;
}

interface EditableTableProps {
  loading: boolean;
  columns: ColumnProps<any>[];
  dataSource: any[];
  onSave: (record: any) => void;
  submitting: boolean;
  policyList: IPolicy[];
}

interface EditableColumnProps<RecordType> extends ColumnProps<RecordType> {
  editable?: boolean;
}

const EditableTable = (
  { loading, dataSource, columns, submitting, onSave, policyList = [] }: EditableTableProps,
  wrapperRef: any,
) => {
  const [form] = Form.useForm();
  const [editingKey, setEditingKey] = useState('');
  // 编辑弹出框确认
  const [popconfirmVisible, setPopconfirmVisible] = useState(false);

  const isEditing = (record: INetworkPolicy) => record.networkId === editingKey;

  const edit = (record: INetworkPolicy) => {
    form.setFieldsValue({ ...record });
    setEditingKey(record.networkId);
  };

  const handleCancel = () => {
    setPopconfirmVisible(false);
    setEditingKey('');
  };

  useImperativeHandle(wrapperRef, () => ({
    reset: () => {
      return handleCancel();
    },
  }));

  const handleSave = async (key: React.Key) => {
    try {
      const formData = (await form.validateFields()) as INetworkPolicy;
      const current = dataSource.find((item) => item.networkId === key);
      if (onSave) {
        onSave({
          ...current,
          ...formData,
        });
      }
      return;
      // setEditingKey('');
    } catch (errInfo) {
      console.log('Validate Failed:', errInfo);
    }
  };

  const fullColumns: EditableColumnProps<INetworkPolicy>[] = [
    {
      title: '#',
      dataIndex: 'rowIndex',
      ellipsis: true,
      width: 70,
      align: 'center',
      render: (text, row, index) => index + 1,
    },
    ...columns,
    {
      title: '操作',
      dataIndex: 'operation',
      width: 200,
      align: 'center',
      render: (_, record) => {
        const editable = isEditing(record);
        return editable ? (
          <span>
            <Popconfirm
              visible={popconfirmVisible}
              title="确定保存吗？"
              okButtonProps={{ loading: submitting }}
              onConfirm={() => handleSave(record.networkId)}
              onCancel={() => setPopconfirmVisible(false)}
            >
              <a style={{ marginRight: 8 }} onClick={() => setPopconfirmVisible(true)}>
                保存
              </a>
            </Popconfirm>
            <a onClick={handleCancel}>取消</a>
          </span>
        ) : (
          <>
            {/** @ts-ignore */}
            <a disabled={editingKey !== ''} onClick={() => edit(record)}>
              编辑
            </a>
            {/* <Divider type="vertical" />  */}
            {/* <Popconfirm
              title="确定删除吗？"
              icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
            >
              <a href="#">删除</a>
            </Popconfirm> */}
          </>
        );
      },
    },
  ];

  const mergedColumns = fullColumns.map((col) => {
    if (!col.editable) {
      return col;
    }
    return {
      ...col,
      onCell: (record: INetworkPolicy) => ({
        record,
        dataIndex: col.dataIndex,
        title: col.title,
        editing: isEditing(record),
        policyList,
      }),
    };
  });

  return (
    <Form form={form} component={false}>
      <Table
        rowKey="networkId"
        rowClassName="editable-row"
        size="small"
        bordered
        components={{
          body: {
            cell: EditableCell,
          },
        }}
        // @ts-ignore
        columns={mergedColumns}
        pagination={false}
        loading={loading}
        dataSource={dataSource}
      />
    </Form>
  );
};

export default forwardRef(EditableTable);
