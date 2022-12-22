import type { ECOption } from '@/components/ReactECharts';
import ReactECharts from '@/components/ReactECharts';
import TimeAxisChart from '@/components/TimeAxisChart';
import {
  EllipsisOutlined,
  FullscreenExitOutlined,
  FullscreenOutlined,
  LineOutlined,
  TableOutlined,
} from '@ant-design/icons';
import { useFullscreen } from 'ahooks';
import { Card, Dropdown, Menu, Space, Table, Tooltip } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import { useContext, useMemo, useRef, useState } from 'react';
import type { IRestMetric } from '../../typings';
import { RestStatCommonDataContext } from '../Layout';

enum EShowType {
  'TABLE' = 'table',
  'LINE' = 'line',
  'PIE' = 'pie',
}

interface Props {
  title: string;
  data: IRestMetric[];
  startTime: number;
  endTime: number;
  onQueryAll: () => void;
}

const StatCard = (props: Props) => {
  const { title, data, startTime, endTime, onQueryAll } = props;

  const { interval } = useContext(RestStatCommonDataContext);
  const [showType, setShowType] = useState<EShowType>(EShowType.LINE);

  const wrapRef = useRef<any>();
  const [isFullScreen, { toggleFullscreen }] = useFullscreen(wrapRef);

  const pieOption: ECOption = useMemo(() => {
    const pieData: Record<string, number> = {};

    data.forEach((item) => {
      const { timestamp, ...rest } = item;

      Object.keys(rest).forEach((key) => {
        if (!pieData[key]) {
          pieData[key] = rest[key] as number;
          return;
        }
        pieData[key] += rest[key] as number;
      });
    });
    return {
      tooltip: {
        trigger: 'item',
      },
      series: [
        {
          type: 'pie',
          radius: '50%',
          data: Object.keys(pieData).map((key) => {
            return {
              name: key,
              value: pieData[key],
            };
          }),
        },
      ],
    };
  }, [data]);

  const columns = useMemo(() => {
    const titles = Object.keys(data?.[0] || {});
    const res: ColumnProps<any>[] = titles.map((key) => {
      return {
        dataIndex: key,
        title: key,
        ellipsis: true,
        width: key.length * 12,
        align: 'center',
        fixed: key === 'timestamp' ? 'right' : false,
      };
    });
    return res;
  }, [data]);

  return (
    <div ref={wrapRef}>
      <Card
        size="small"
        title={title}
        bodyStyle={{ height: isFullScreen ? '100vh' : 400 }}
        extra={
          <Space>
            <Tooltip title={'打开趋势图预览'}>
              <span
                onClick={() => {
                  setShowType(EShowType.LINE);
                }}
              >
                <LineOutlined
                  style={{ fontSize: 16, color: showType === EShowType.LINE ? '#198ce1' : '' }}
                />
              </span>
            </Tooltip>
            <Tooltip title={'打开表格预览'}>
              <span
                onClick={() => {
                  setShowType(EShowType.TABLE);
                }}
              >
                <TableOutlined
                  style={{ fontSize: 16, color: showType === EShowType.TABLE ? '#198ce1' : '' }}
                />
              </span>
            </Tooltip>

            <Tooltip title={isFullScreen ? '还原' : '全屏'}>
              <span onClick={() => toggleFullscreen()}>
                {isFullScreen ? <FullscreenExitOutlined /> : <FullscreenOutlined />}
              </span>
            </Tooltip>
            <Dropdown
              overlay={
                <Menu>
                  <Menu.Item
                    key="all"
                    onClick={() => {
                      onQueryAll();
                    }}
                  >
                    全部
                  </Menu.Item>
                  <Menu.Item
                    key="pie"
                    onClick={() => {
                      setShowType(EShowType.PIE);
                    }}
                  >
                    饼图
                  </Menu.Item>
                </Menu>
              }
            >
              <EllipsisOutlined />
            </Dropdown>
          </Space>
        }
      >
        {showType === EShowType.LINE && (
          <TimeAxisChart
            chartHeight={isFullScreen ? undefined : 400 - 24}
            startTime={startTime}
            endTime={endTime}
            interval={interval}
            data={data}
            brush={false}
          />
        )}
        {showType === EShowType.TABLE && (
          <Table
            size="small"
            bordered
            rowKey={(r) => r.timestamp}
            columns={columns}
            dataSource={data}
            scroll={{ x: 'max-content', y: isFullScreen ? '85vh' : 400 - 39 - 24 }}
            pagination={false}
          />
        )}
        {showType === EShowType.PIE && (
          <ReactECharts option={pieOption} style={{ height: isFullScreen ? '90%' : 400 - 24 }} />
        )}
      </Card>
    </div>
  );
};

export default StatCard;
