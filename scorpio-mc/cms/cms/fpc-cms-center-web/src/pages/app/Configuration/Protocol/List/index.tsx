import { proTableSerchConfig, getTablePaginationDefaultSettings } from '@/common/app';
import type { IProTableData } from '@/common/typings';
import AutoHeightContainer from '@/components/AutoHeightContainer';
import { queryAllProtocols } from '@/pages/app/appliance/Metadata/service';
import type { IL7Protocol } from '@/pages/app/appliance/Metadata/typings';
import { protocolStandardMap } from '@/pages/app/appliance/Metadata/typings';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Select, Tooltip } from 'antd';
import { useEffect, useRef, useState } from 'react';
import { queryAllProtocolLabels } from '../service';

interface ILabel {
  labelId: string;
  nameText: string;
  name: string;
}

export default function List() {
  const actionRef = useRef<ActionType>();
  const [tableHeight, setTableHeight] = useState(200);
  const [labels, setLabels] = useState<ILabel[]>([]);
  useEffect(() => {
    queryAllProtocolLabels().then((res) => {
      const { success, result } = res;
      if (success) {
        setLabels(result);
      }
    });
  }, []);

  const columns: ProColumns<IL7Protocol>[] = [
    {
      title: '协议ID',
      dataIndex: 'protocolId',
      align: 'center',
      search: false,
    },
    {
      title: '协议名称',
      dataIndex: 'nameText',
      align: 'center',
    },
    {
      title: '协议描述',
      dataIndex: 'descriptionText',
      align: 'center',
      width: 660,
      search: false,
      render: (text, record) => {
        return (
          <Tooltip title={record.descriptionText}>
            <span
              style={{
                display: 'block',
                whiteSpace: 'nowrap',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
              }}
            >
              {record.descriptionText}
            </span>
          </Tooltip>
        );
      },
    },
    {
      title: '类型',
      dataIndex: 'standard',
      align: 'center',
      render: (value, record) => {
        return protocolStandardMap[record.standard || ''];
      },
      renderFormItem: () => {
        return (
          <Select placeholder="选择类型">
            <Select.Option value="">全部</Select.Option>
            {Object.keys(protocolStandardMap).map((key) => (
              <Select.Option key={key} value={key}>
                {protocolStandardMap[key]}
              </Select.Option>
            ))}
          </Select>
        );
      },
    },
    {
      title: '分类',
      dataIndex: 'label',
      align: 'center',
      render: (value, record) => {
        return record.label?.join(',');
      },
      renderFormItem: () => {
        return (
          <Select placeholder="选择分类">
            <Select.Option value="">全部</Select.Option>
            {labels.map((item) => (
              <Select.Option key={item.labelId} value={item.labelId}>
                {item.nameText}
              </Select.Option>
            ))}
          </Select>
        );
      },
    },
  ];

  return (
    // 高度 - 搜索表单高度- 表头高度 - 分页高度
    <div>
      <AutoHeightContainer onHeightChange={(h) => setTableHeight(h - 94 - 76)}>
        <ProTable<IL7Protocol>
          bordered
          size="small"
          actionRef={actionRef}
          columns={columns}
          rowKey={(record) => `${record.protocolId}`}
          request={async (params = {}) => {
            const { current, pageSize, ...rest } = params;
            const newParams = { pageSize, page: current! - 1, ...rest } as any;
            const { success, result } = await queryAllProtocols({
              protocolName: newParams.nameText,
              label: newParams.label,
              standard: newParams.standard,
            });
            if (!success) {
              return {
                data: [],
                success,
              };
            }
            return {
              data: result,
            } as IProTableData<IL7Protocol[]>;
          }}
          search={{
            ...proTableSerchConfig,
            labelWidth: 'auto',
            span: 6,
            optionRender: (searchConfig, formProps, dom) => [...dom.reverse()],
          }}
          toolBarRender={false}
          scroll={{ y: tableHeight }}
          pagination={getTablePaginationDefaultSettings()}
        />
      </AutoHeightContainer>
    </div>
  );
}
