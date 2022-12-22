/* eslint-disable no-nested-ternary */
import { bytesToSize } from '@/utils/utils';
import { TableOutlined } from '@ant-design/icons';
import { Card, Select, Space, Table, Tooltip } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import numeral from 'numeral';
import { v1 as uuidv1 } from 'uuid';
import React, { useMemo, useState } from 'react';
import Bar from '../Bar';
import { EFormatterType } from '../fieldsManager';

enum EDataSource {
  'BYTE',
  'SESSION',
}

const dataSourceList = [
  {
    label: '总流量',
    value: EDataSource.BYTE,
  },
  {
    label: '总会话数',
    value: EDataSource.SESSION,
  },
];

interface IByteDatasource {
  byteLabel: string;
  byteValue: number;
  key: string;
}
interface ISessionDatasource {
  sessionLabel: string;
  sessionValue: number;
  key: string;
}

interface IDatasource {
  label: string;
  value: number;
}
interface IMultipeSourceBarProps {
  title: string;
  data: {
    totalBytes: IDatasource[];
    totalSessions: IDatasource[];
  };
  height?: number;
  loading?: boolean;
}
const MultipeSourceBar: React.FC<IMultipeSourceBarProps> = ({
  title,
  data,
  height = 300,
  loading = false,
}) => {
  const [dataSource, setDataSource] = useState<EDataSource>(EDataSource.BYTE);
  const [showTable, setShowTable] = useState<boolean>(false);

  const handleTableClick = () => {
    setShowTable((prev) => {
      return !prev;
    });
  };

  // 数据源
  const byteDataSource = useMemo<IByteDatasource[]>(() => {
    return (
      data.totalBytes.map((item) => ({
        byteLabel: item.label,
        byteValue: item.value,
        key: uuidv1(),
      })) || []
    );
  }, [data.totalBytes]);

  const sessionDataSource = useMemo<ISessionDatasource[]>(() => {
    return (
      data.totalSessions.map((item) => ({
        sessionLabel: item.label,
        sessionValue: item.value,
        key: uuidv1(),
      })) || []
    );
  }, [data.totalSessions]);

  // 图表的参数
  // const categoryList: string[] = [];
  // const seriesData: number[] = [];
  // 表格的参数
  const byteTableColumns = useMemo<ColumnProps<IByteDatasource>[]>(() => {
    return [
      {
        title: 'IP',
        dataIndex: 'byteLabel',
        align: 'center',
      },
      {
        title: dataSource === EDataSource.BYTE ? '总流量' : '--',
        dataIndex: 'byteValue',
        align: 'center',
        render: (text, record) => bytesToSize(record.byteValue),
      },
    ];
  }, [dataSource]);

  const sessionTableColumns = useMemo<ColumnProps<ISessionDatasource>[]>(() => {
    return [
      {
        title: 'IP',
        dataIndex: 'sessionLabel',
        align: 'center',
      },
      {
        title: dataSource === EDataSource.SESSION ? '总会话数' : '--',
        dataIndex: 'sessionValue',
        align: 'center',
        render: (text, record) => numeral(record.sessionValue).format('0,0'),
      },
    ];
  }, [dataSource]);

  // dataSourceData.forEach((point) => {
  //   categoryList.push(point.label);
  //   seriesData.push(point.value);
  // });

  const handleDisplayChange = (value: EDataSource) => {
    setDataSource(value);
  };

  const extra = (
    <div style={{ display: 'flex', alignItems: 'center' }}>
      <Space>
        <Select
          value={dataSource}
          size="small"
          style={{ width: 120 }}
          onChange={handleDisplayChange}
        >
          {dataSourceList.map((item) => {
            return (
              <Select.Option value={item.value} key={item.value}>
                {item.label}
              </Select.Option>
            );
          })}
        </Select>
        <Tooltip title={`${showTable ? '关闭' : '打开'}表格预览`}>
          <TableOutlined
            style={{ fontSize: 16, color: showTable ? '#198ce1' : '' }}
            onClick={handleTableClick}
          />
        </Tooltip>
      </Space>
    </div>
  );

  return (
    <Card size="small" title={title} extra={extra}>
      {showTable ? (
        dataSource === EDataSource.BYTE ? (
          <Table
            rowKey="key"
            bordered
            size="small"
            loading={loading}
            columns={byteTableColumns}
            dataSource={byteDataSource}
            pagination={false}
            style={{ height }}
            // 表头高度40px
            scroll={{ y: height - 40 }}
          />
        ) : (
          <Table
            rowKey="key"
            bordered
            size="small"
            loading={loading}
            columns={sessionTableColumns}
            dataSource={sessionDataSource}
            pagination={false}
            style={{ height }}
            // 表头高度40px
            scroll={{ y: height - 40 }}
          />
        )
      ) : (
        <Bar
          loading={loading}
          height={height}
          data={
            dataSource === EDataSource.BYTE
              ? byteDataSource.map((item) => ({ label: item.byteLabel, value: item.byteValue }))
              : sessionDataSource.map((ele) => ({
                  label: ele.sessionLabel,
                  value: ele.sessionValue,
                }))
          }
          formatterType={
            dataSource === EDataSource.BYTE ? EFormatterType.BYTE : EFormatterType.COUNT
          }
        />
      )}
    </Card>
  );
};

export default MultipeSourceBar;
