import React from 'react';
import { Table } from 'antd';
import numeral from 'numeral';

const ProtocolTable = ({ data, srcIp }) => {
  const dataSource = [];
  let totalCount = 0;

  Object.keys(data).forEach((protocol) => {
    const count = data[protocol] || 0;
    totalCount += count;

    if (count > 0) {
      dataSource.push({
        key: protocol,
        protocol,
        count,
      });
    }
  });

  const columns = [
    {
      title: '协议',
      dataIndex: 'protocol',
      key: 'protocol',
      align: 'center',
    },
    {
      title: '事件数量',
      dataIndex: 'count',
      key: 'count',
      align: 'center',
      render: (count) => numeral(count).format('0,0'),
    },
    {
      title: '占比',
      dataIndex: 'precent',
      key: 'precent',
      align: 'center',
      render: (text, record) => {
        if (totalCount === 0) {
          return '--';
        }
        return `${((record.count / totalCount) * 100).toFixed(2)}%`;
      },
    },
  ];

  return (
    <Table
      title={() => (
        <div style={{ textAlign: 'center' }}>
          {srcIp && <span>源IP: {srcIp}，</span>}
          <span>事件总数量: {numeral(totalCount).format('0,0')}</span>
        </div>
      )}
      rowKey={(record) => record.protocol}
      size="small"
      pagination={false}
      bordered
      dataSource={dataSource}
      columns={columns}
    />
  );
};

export default ProtocolTable;
