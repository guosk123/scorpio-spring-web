/* eslint-disable no-nested-ternary */
import AnyWhereContainer from '@/components/AnyWhereContainer';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { getFlowRecordFilterString } from '@/pages/app/security/RecordQuery/FlowRecord/utils';
import useClickAway from '@/utils/hooks/useClickAway';
import { bytesToSize, jumpNewPage } from '@/utils/utils';
import { TableOutlined } from '@ant-design/icons';
import type { MenuProps } from 'antd';
import { Card, Menu, Select, Space, Table, Tooltip } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import numeral from 'numeral';
import React, { useMemo, useRef, useState } from 'react';
import { useParams, useSelector } from 'umi';
import { v1 as uuidv1 } from 'uuid';
import type { IUriParams } from '../../typings';
import Bar from '../Bar';
import { EFormatterType } from '../fieldsManager';
import { getFlowAnalysisFilterString } from '../PageLayoutWithFilter';

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
  const { pcapFileId, networkId, serviceId } = useParams() as IUriParams;
  const { startTimestamp, endTimestamp } = useSelector<ConnectState, Required<IGlobalTime>>(
    (state) => state.appModel.globalSelectedTime,
  );
  const filterParamsRef = useRef<Parameters<typeof getFlowRecordFilterString>[0]>({});

  const [menuDisplay, setMenuDisplay] = useState(false);
  const [menuPos, setMenuPos] = useState<{ left: number; top: number }>({ left: -999, top: -999 });
  const containerRef = useRef<HTMLDivElement>(null);
  const menuRef = useRef<HTMLDivElement>(null);

  const uriTimeString = useMemo(() => {
    return `from=${startTimestamp}&to=${endTimestamp}&timeType=${ETimeType.CUSTOM}`;
  }, [startTimestamp, endTimestamp]);

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

  // 点击下钻
  const handleMenuClick: MenuProps['onClick'] = (info) => {
    let url = '';
    console.log(info);
    if (info.key === 'flow-record') {
      const filterString = getFlowRecordFilterString(filterParamsRef.current);

      const search = `filter=${filterString}&${uriTimeString}`;

      if (pcapFileId) {
        url = `/analysis/offline/${pcapFileId}/flow-record?${search}`;
      } else {
        url = `/analysis/trace/flow-record?${search}`;
      }
    }
    if (info.key === 'flow') {
      const ip: string[] = [];
      let suffix: string = '/flow/ip';
      if (filterParamsRef.current?.ip) {
        ip.push(filterParamsRef.current.ip);
      }
      if (filterParamsRef.current?.session) {
        ip.push(
          filterParamsRef.current.session.ipAAddress,
          filterParamsRef.current.session.ipBAddress,
        );
      }
      const filterString = getFlowAnalysisFilterString({ ip });
      const search = `filters=${filterString}&${uriTimeString}`;
      if (ip.length === 2) {
        suffix = '/flow/ip-conversation';
      }
      if (pcapFileId) {
        url = `/analysis/offline/${pcapFileId}${suffix}?${search}`;
      }
      if (serviceId) {
        url = `/analysis/performance/service/${serviceId}/${networkId}${suffix}?${search}`;
      }
      if (!serviceId && networkId) {
        url = `/analysis/performance/network/${networkId}${suffix}?${search}`;
      }
    }
    if (url) {
      jumpNewPage(url);
    }
    setMenuDisplay(false);
  };

  useClickAway(menuRef, () => {
    setMenuDisplay(false);
  });

  const menu = (
    <Menu onClick={handleMenuClick}>
      <Menu.Item key="flow-record">会话详单</Menu.Item>
      <Menu.Item key="flow">流量分析</Menu.Item>
    </Menu>
  );

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
    <Card size="small" title={title} extra={extra} bodyStyle={{ position: 'relative' }}>
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
        <div ref={containerRef}>
          <Bar
            loading={loading}
            height={height}
            onClick={(label, e) => {
              const filterParams: Parameters<typeof getFlowRecordFilterString>[0] = {
                networkId,
                serviceId,
              };
              if (label.indexOf('⇋') !== -1) {
                const session = label.split('⇋').map((item) => item.trim());
                if (session.length >= 2) {
                  filterParams.session = { ipAAddress: session[0], ipBAddress: session[1] };
                }
              } else {
                filterParams.ip = label.trim();
              }

              filterParamsRef.current = filterParams;

              const rect = containerRef.current?.getBoundingClientRect();
              const pos = {
                left: e.clientX - (rect?.left || 0),
                top: e.clientY - (rect?.top || 0),
              };
              setMenuPos(pos);
              setMenuDisplay(true);
            }}
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
          <AnyWhereContainer
            top={menuPos.top}
            left={menuPos.left}
            display={menuDisplay}
            children={menu}
            ref={menuRef}
          />
        </div>
      )}
    </Card>
  );
};

export default MultipeSourceBar;
