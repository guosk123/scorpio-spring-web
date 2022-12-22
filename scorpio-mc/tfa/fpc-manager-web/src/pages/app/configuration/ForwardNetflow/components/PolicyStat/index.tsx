import TimeAxisChart from '@/components/TimeAxisChart';
import type { TimeAxisChartData } from '@/components/TimeAxisChart/typing';
import { convertBandwidth } from '@/utils/utils';
import { TableOutlined } from '@ant-design/icons';
import { Card, Col, Row, Select, Table, Tooltip } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import moment from 'moment';
import { useEffect, useMemo, useState } from 'react';
import { queryForwardPolicyHistogram } from '../../service';
import type { IForwardPolicyStatData } from '../../typings';

const ALL = 'ALL';

// 按照target的结构进行数据合并
const mergeForwardStat = (target: IForwardPolicyStatData, source: IForwardPolicyStatData) => {
  const targetCopy = { ...target };
  Object.keys(targetCopy).map((key) => {
    if (!['timestamp', 'networkId', 'policyId', 'netifName'].includes(key)) {
      targetCopy[key] = target[key] + source[key];
    }
  });
  return targetCopy;
};

interface Props {
  policyName: string;
  policyId: string;
  networkIds: { label: string; value: string }[];
  netifNames: { label: string; value: string }[];
  height?: number;
}

const PolicyStat = (props: Props) => {
  const { policyName, policyId, networkIds, netifNames, height = 300 } = props;

  const [data, setData] = useState<IForwardPolicyStatData[]>([]);
  const [networkId, setNetworkId] = useState(ALL);
  const [netifName, setNetifName] = useState(ALL);
  const [byteTable, setByteTable] = useState(false);
  const [packetTable, setPacketTable] = useState(false);

  const startTime = moment().subtract(1, 'hours').valueOf();
  const endTime = moment().valueOf();
  const interval = 60;

  const byteTableColums: ColumnProps<Record<string, number>>[] = [
    {
      dataIndex: 'timestamp',
      title: '时间',
      render: (dom, record) => {
        const { timestamp } = record;
        return moment(timestamp).format();
      },
      width: 200,
    },
    {
      dataIndex: 'forwardFailBandWidth',
      title: '转发失败带宽',
      width: 100,
      render: (dom, record) => {
        const { forwardFailBandWidth } = record;
        return convertBandwidth(forwardFailBandWidth);
      },
    },
    {
      dataIndex: 'forwardSuccessBandWidth',
      title: '转发成功带宽',
      width: 100,
      render: (dom, record) => {
        const { forwardSuccessBandWidth } = record;
        return convertBandwidth(forwardSuccessBandWidth);
      },
    },
    {
      dataIndex: 'forwardTotalBandWidth',
      title: '转发总带宽',
      width: 100,
      render: (dom, record) => {
        const { forwardTotalBandWidth } = record;
        return convertBandwidth(forwardTotalBandWidth);
      },
    },
  ];

  const packetTableColums: ColumnProps<Record<string, number>>[] = [
    {
      dataIndex: 'timestamp',
      title: '时间',
      render: (dom, record) => {
        const { timestamp } = record;
        return moment(timestamp).format();
      },
      width: 200,
    },
    {
      dataIndex: 'forwardFailPackets',
      title: '转发失败包数',
      width: 100,
    },
    {
      dataIndex: 'forwardSuccessPackets',
      title: '转发成功包数',
      width: 100,
    },
    {
      dataIndex: 'forwardTotalPackets',
      title: '转发总包数',
      width: 100,
    },
  ];

  useEffect(() => {
    queryForwardPolicyHistogram({
      policyId,
      startTime: moment(startTime).format(),
      endTime: moment(endTime).format(),
      interval,
    }).then(({ success, result }) => {
      if (success) {
        setData(result);
      }
    });
  }, [endTime, policyId, startTime]);

  const chartData = useMemo(() => {
    const packetData: TimeAxisChartData[] = [];
    const byteData: TimeAxisChartData[] = [];
    let copyData = [...data];
    if (networkId !== ALL) {
      copyData = copyData.filter((item) => item.networkId === networkId);
    }
    if (netifName !== ALL) {
      copyData = copyData.filter((item) => item.netifName === netifName);
    }

    const dataMap: Record<string, IForwardPolicyStatData> = {};

    // 数据合并，归并为每个时间点仅有一条数据，同时间点的多条数据相加
    copyData.forEach((item) => {
      const { timestamp } = item;
      if (!dataMap[timestamp]) {
        dataMap[timestamp] = item;
      } else {
        dataMap[timestamp] = mergeForwardStat(dataMap[timestamp], item);
      }
    });

    copyData = Object.values(dataMap);

    copyData.forEach((item) => {
      const {
        forwardFailBytes,
        forwardSuccessBytes,
        forwardTotalBytes,
        forwardFailPackets,
        forwardSuccessPackets,
        forwardTotalPackets,
        timestamp,
      } = item;

      packetData.push({
        timestamp: new Date(timestamp).valueOf(),
        forwardFailPackets,
        forwardSuccessPackets,
        forwardTotalPackets,
      });

      byteData.push({
        timestamp: new Date(timestamp).getTime(),
        forwardFailBandWidth: (forwardFailBytes * 8) / 60,
        forwardSuccessBandWidth: (forwardSuccessBytes * 8) / 60,
        forwardTotalBandWidth: (forwardTotalBytes * 8) / 60,
      });
    });
    return {
      packetData,
      byteData,
    };
  }, [data, netifName, networkId]);

  return (
    <div>
      <Row justify="space-between" style={{ marginTop: 24 }}>
        <h3 style={{ marginLeft: '1em' }}>{policyName}</h3>
        <div>
          <span style={{ marginRight: '1em' }}>
            <label>网络:</label>
            <Select
              size="small"
              value={networkId}
              onChange={(value) => {
                setNetworkId(value);
              }}
              options={[...networkIds, { label: '所有', value: ALL }]}
            />
          </span>
          <span style={{ marginRight: '1em' }}>
            <label>接口:</label>
            <Select
              size="small"
              value={netifName}
              onChange={(value) => setNetifName(value)}
              options={[...netifNames, { label: '所有', value: ALL }]}
            />
          </span>
        </div>
      </Row>
      <Row gutter={4}>
        <Col span={12}>
          <Card
            title={'带宽曲线图'}
            size="small"
            extra={
              <Tooltip title={`${byteTable ? '关闭' : '打开'}表格预览`}>
                <TableOutlined
                  style={{ fontSize: 16, color: byteTable ? '#198ce1' : '' }}
                  onClick={() => {
                    setByteTable((prev) => !prev);
                  }}
                />
              </Tooltip>
            }
          >
            {byteTable ? (
              <Table
                size="small"
                bordered
                rowKey={'timestamp'}
                pagination={false}
                scroll={{ y: height - 40, x: 'max-content' }}
                style={{ height }}
                columns={byteTableColums}
                dataSource={chartData.byteData as Record<string, number>[]}
              />
            ) : (
              <TimeAxisChart
                nameMap={{
                  forwardFailBandWidth: '转发失败带宽',
                  forwardSuccessBandWidth: '转发成功带宽',
                  forwardTotalBandWidth: '转发总带宽',
                }}
                unitConverter={convertBandwidth}
                startTime={startTime}
                endTime={endTime}
                brush={false}
                interval={interval}
                data={chartData.byteData}
                chartHeight={height}
              />
            )}
          </Card>
        </Col>
        <Col span={12}>
          <Card
            title={'数据包曲线图'}
            size="small"
            extra={
              <Tooltip title={`${packetTable ? '关闭' : '打开'}表格预览`}>
                <TableOutlined
                  style={{ fontSize: 16, color: packetTable ? '#198ce1' : '' }}
                  onClick={() => {
                    setPacketTable((prev) => !prev);
                  }}
                />
              </Tooltip>
            }
          >
            {packetTable ? (
              <Table
                size="small"
                bordered
                rowKey={'timestamp'}
                pagination={false}
                scroll={{ y: height - 40, x: 'max-content' }}
                style={{ height }}
                columns={packetTableColums}
                dataSource={chartData.packetData as Record<string, number>[]}
              />
            ) : (
              <TimeAxisChart
                nameMap={{
                  forwardFailPackets: '转发失败包数',
                  forwardSuccessPackets: '转发成功包数',
                  forwardTotalPackets: '转发总包数',
                }}
                brush={false}
                chartHeight={height}
                startTime={startTime}
                endTime={endTime}
                interval={interval}
                data={chartData.packetData}
              />
            )}
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default PolicyStat;
