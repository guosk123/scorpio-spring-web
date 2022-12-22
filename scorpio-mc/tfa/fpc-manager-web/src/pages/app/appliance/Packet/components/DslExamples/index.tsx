import { QuestionCircleOutlined } from '@ant-design/icons';
import { Modal, Table } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import { useState } from 'react';

export default function DslExamples() {
  const [isModalVisible, setIsModalVisible] = useState(false);

  const eventColumns: ColumnProps<any>[] = [
    {
      title: '语法',
      dataIndex: 'grammar',
      key: 'grammar',
      width: 400,
      align: 'center',
    },
    {
      title: '示例',
      dataIndex: 'example',
      key: 'example',
      width: 400,
      align: 'center',
    },
  ];

  return (
    <div>
      <div
        style={{
          marginLeft: 10,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: 'pointer',
          height: 32,
        }}
        onClick={() => {
          setIsModalVisible(true);
        }}
      >
        <QuestionCircleOutlined />
        <span style={{ marginLeft: 5 }}>BPF语法示例</span>
      </div>
      <Modal
        title="BPF语法示例"
        visible={isModalVisible}
        footer={null}
        width={900}
        onCancel={() => {
          setIsModalVisible(false);
        }}
      >
        <Table
          rowKey="type"
          bordered
          size="small"
          columns={eventColumns}
          pagination={false}
          dataSource={[
            {
              grammar: 'ether [src|dst] host <MAC>',
              example: (
                <span>
                  ether host 00:00:5E:00:53:00 <br />
                  ether dst host 00:00:5E:00:53:00
                </span>
              ),
            },
            {
              grammar: 'vlan <ID>',
              example: <span>vlan 100</span>,
            },
            {
              grammar: '[src|dst] host <host ip>',
              example: (
                <span>
                  host 203.0.113.50 <br />
                  dst host 198.51.100.200
                </span>
              ),
            },
            {
              grammar: '[ip|ip6][src|dst] proto <protocol>',
              example: (
                <span>
                  proto 1 <br />
                  src 10.4.9.40 and proto ICMP
                  <br />
                  ip6 and src fe80::aebc:32ff:fe84:70b7 and proto 47
                  <br />
                  ip and src 10.4.9.40 and proto 0x0006
                </span>
              ),
            },
            {
              grammar: '[ip|ip6][tcp|udp] [src|dst] port <port>',
              example: (
                <span>
                  udp and src port 2005 <br />
                  ip6 and tcp and src port 80
                </span>
              ),
            },
            {
              grammar: '[src|dst] net <network>',
              example: (
                <span>
                  dst net 192.168.1.0 <br />
                  src net 10 <br />
                  net 192.168.1.0/24 <br />
                </span>
              ),
            },
            {
              grammar: '[ip|ip6] tcp tcpflags & (tcp-[ack|fin|syn|rst|push|urg|)',
              example: (
                <span>
                  {`tcp[tcpflags] & (tcp-ack) !=0`} <br />
                  {`tcp[13] & 16 !=0`} <br />
                  {`ip6 and (ip6[40+13] & (tcp-syn) != 0)`} <br />
                </span>
              ),
            },
            {
              grammar: 'Fragmented IPv4 packets (ip_offset != 0)',
              example: <span> {`ip[6:2] & 0x3fff != 0x0000`}</span>,
            },
          ]}
        />
      </Modal>
    </div>
  );
}
