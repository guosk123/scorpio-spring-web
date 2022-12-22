import ProTable, { ProColumns } from '@ant-design/pro-table';
import { IPktAnalysis, EParseStatus } from './typings';
import { useEffect, useRef, useState } from 'react';
import LinkButton from '@/components/LinkButton';
import { Button, Divider, message, Modal, Popconfirm } from 'antd';
import { history } from 'umi';
import {
  queryPktAnalysisPlugins,
  deletePktAnalysisPlugin,
  downloadPlugin,
  previewPlugin,
} from './services';
import { PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { getTablePaginationDefaultSettings } from '@/common/app';
import MonacoEditor from 'react-monaco-editor';
import { CopyToClipboard } from 'react-copy-to-clipboard';
import moment from 'moment';

export default () => {
  const actionRef = useRef<any>();
  const handleDelete = async (id: string) => {
    const { success } = await deletePktAnalysisPlugin(id);
    if (!success) {
      message.error('删除失败!');
      return;
    }
    message.success('删除成功!');
    actionRef.current.reload();
  };
  const [preview, setPreview] = useState<boolean>(false);
  const [pluginCode, setPluginCode] = useState<string>('');
  const columns: ProColumns<IPktAnalysis>[] = [
    {
      title: '名称',
      dataIndex: 'fileName',
      align: 'center',
      search: false,
    },
    {
      title: '协议',
      dataIndex: 'protocol',
      align: 'center',
      search: false,
    },
    {
      title: '描述',
      dataIndex: 'description',
      align: 'center',
      search: false,
      ellipsis: true,
      render: (_, record) => {
        if (record === undefined) {
          return '';
        }
        return record.description;
      },
    },
    {
      title: '导入时间',
      dataIndex: 'createTime',
      align: 'center',
      search: false,
      render: (_, record) => {
        return moment(record.createTime).format('YYYY-MM-DD HH:mm:ss');
      },
    },
    {
      title: '解析状态',
      align: 'center',
      search: false,
      render: (_, record) => {
        const { parseStatus, parseLog } = record;
        if (parseStatus === EParseStatus.ERROR) {
          return `出错: ${parseLog}`;
        }
        if (parseStatus === EParseStatus.SUCCESS) {
          return '成功';
        }
        if (parseStatus === EParseStatus.UNRESOLVED) {
          return '未解析';
        }
        return '';
      },
    },
    {
      title: '操作',
      width: 300,
      align: 'center',
      search: false,
      render: (text, record) => (
        <>
          <LinkButton
            onClick={() => {
              downloadPlugin(record.id);
            }}
          >
            下载
          </LinkButton>
          <Divider type="vertical" />
          <LinkButton
            onClick={async () => {
              const { success, result } = await previewPlugin(record.id);
              if (!success) {
                message.error('查询错误!');
                return;
              }
              setPluginCode(result);
              setPreview(true);
            }}
          >
            预览
          </LinkButton>
          <Divider type="vertical" />
          <Popconfirm
            title="确定删除吗？"
            onConfirm={() => handleDelete(record.id)}
            icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
          >
            <LinkButton>删除</LinkButton>
          </Popconfirm>
        </>
      ),
    },
  ];

  useEffect(() => {
    // 刷新列表
    const timer = setInterval(() => {
      actionRef.current?.reload();
    }, 10000);
    return () => {
      clearInterval(timer);
    };
  }, []);

  return (
    <>
      <ProTable
        rowKey="id"
        bordered
        size="small"
        columns={columns}
        actionRef={actionRef}
        search={{
          optionRender: () => [
            <Button
              icon={<PlusOutlined />}
              type="primary"
              key="create"
              onClick={() => {
                history.push('/configuration/objects/pktanalysis/plugin/create');
              }}
            >
              新建
            </Button>,
          ],
        }}
        request={async (params = {}) => {
          const { current = 0, pageSize = 0 } = params;
          const newParams = {
            pageSize,
            page: current && current - 1,
          };
          const { success, result } = await queryPktAnalysisPlugins(newParams);
          if (!success) {
            return [];
          }
          return {
            data: result.content,
            page: result.number,
            total: result.totalElements,
            success: true,
          };
        }}
        toolBarRender={false}
        pagination={getTablePaginationDefaultSettings()}
      />
      <Modal
        visible={preview}
        destroyOnClose
        footer={[
          <CopyToClipboard text={pluginCode} onCopy={() => message.success('拷贝成功!')}>
            <Button key="submit" type="primary">
              复制
            </Button>
          </CopyToClipboard>,
          <Button
            key="submit"
            onClick={() => {
              setPreview(false);
            }}
          >
            返回
          </Button>,
        ]}
        width={900}
        onCancel={() => {
          setPreview(false);
        }}
      >
        <MonacoEditor
          height={500}
          width={800}
          language="lua"
          options={{
            selectOnLineNumbers: true,
            readOnly: true,
            theme: 'vs',
          }}
          value={pluginCode}
        />
      </Modal>
    </>
  );
};
