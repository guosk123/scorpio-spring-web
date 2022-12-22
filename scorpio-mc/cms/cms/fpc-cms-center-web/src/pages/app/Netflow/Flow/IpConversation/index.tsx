import type { AppModelState } from '@/models/app/index';
import type { ProColumns } from '@ant-design/pro-table';
import type { IFlowSession } from '../../typing';
import type { ConnectState } from '@/models/connect';
import { Button, Tooltip } from 'antd';
import { bytesToSize, getLinkUrl, jumpNewPage } from '@/utils/utils';
import { useContext } from 'react';
import { FilterContext } from '..';
import { queryFlowSessionHist } from '../../service';
import { EFilterGroupOperatorTypes, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { connect } from 'umi';
import { ETimeType } from '@/components/GlobalTimeSelector';
import { EFieldOperandType } from '@/components/FieldFilter/typings';
import numeral from 'numeral';
import Template from '../../components/Template';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';

interface IFlowParams extends AppModelState {
  tableColumns: ProColumns<IFlowSession>[];
  pageName?: string;
  location: {
    pathname: string;
  };
}

const IPConversation = ({ globalSelectedTime, location: { pathname } }: IFlowParams) => {
  // 该维度支持的过滤条件
  const { filterCondition, addConditionToFilter } = useContext(FilterContext);
  // 表格列定义
  const tableColumns: ProColumns<IFlowSession>[] = [
    {
      title: '源IP地址',
      dataIndex: 'srcIp',
      width: '10%',
      align: 'center',
      search: false,
      render: (_, record) => {
        const srcIp = record.srcIp;
        if (!srcIp) {
          return null;
        }
        return (
          <FilterBubble
            dataIndex={'src_ip'}
            label={
              <Tooltip placement="topLeft" title={srcIp}>
                {srcIp}
              </Tooltip>
            }
            operand={srcIp}
            operandType={EFieldOperandType.IP}
            onClick={(newFilter) => {
              if (addConditionToFilter) {
                addConditionToFilter([newFilter]);
              }
            }}
          />
        );
      },
    },
    {
      title: '目的IP地址',
      dataIndex: 'destIp',
      width: '10%',
      align: 'center',
      search: false,
      render: (_, record) => {
        const destIp = record.destIp;
        if (!destIp) {
          return null;
        }
        return (
          <FilterBubble
            dataIndex={'dest_ip'}
            label={
              <Tooltip placement="topLeft" title={destIp}>
                {destIp}
              </Tooltip>
            }
            operand={destIp}
            operandType={EFieldOperandType.IP}
            onClick={(newFilter) => {
              if (addConditionToFilter) {
                addConditionToFilter([newFilter]);
              }
            }}
          />
        );
      },
    },
    {
      title: '源端口',
      dataIndex: 'srcPort',
      align: 'center',
      search: false,
      width: '10%',
      render: (_, record) => {
        const srcPort = record.srcPort;
        if (!srcPort) {
          return null;
        }

        return (
          <FilterBubble
            dataIndex={'src_port'}
            label={
              <Tooltip placement="topLeft" title={srcPort}>
                {srcPort}
              </Tooltip>
            }
            operand={srcPort}
            operandType={EFieldOperandType.PORT}
            onClick={(newFilter) => {
              if (addConditionToFilter) {
                addConditionToFilter([newFilter]);
              }
            }}
          />
        );
      },
    },
    {
      title: '目的端口',
      dataIndex: 'destPort',
      align: 'center',
      search: false,
      width: '10%',
      render: (_, record) => {
        const destPort = record.destPort;
        if (!destPort) {
          return null;
        }

        return (
          <FilterBubble
            dataIndex={'dest_port'}
            label={
              <Tooltip placement="topLeft" title={destPort}>
                {destPort}
              </Tooltip>
            }
            operand={destPort}
            operandType={EFieldOperandType.PORT}
            onClick={(newFilter) => {
              if (addConditionToFilter) {
                addConditionToFilter([newFilter]);
              }
            }}
          />
        );
      },
    },
    {
      title: '协议',
      dataIndex: 'protocol',
      align: 'center',
      search: false,
      width: '10%',
      render: (_, record) => {
        const protocol = record.protocol;
        if (!protocol) {
          return null;
        }

        return (
          <FilterBubble
            dataIndex={'protocol'}
            label={
              <Tooltip placement="topLeft" title={protocol}>
                {protocol}
              </Tooltip>
            }
            operand={protocol}
            operandType={EFieldOperandType.ENUM}
            onClick={(newFilter) => {
              if (addConditionToFilter) {
                addConditionToFilter([newFilter]);
              }
            }}
          />
        );
      },
    },
    {
      title: '总字节数',
      dataIndex: 'totalBytes',
      align: 'center',
      search: false,
      sorter: true,
      width: '10%',
      render: (_, record) => {
        if (record.totalBytes && parseInt(record.totalBytes, 10) !== 0) {
          return bytesToSize(parseInt(record.totalBytes, 10));
        }
        return 0;
      },
    },
    {
      title: '总包数',
      dataIndex: 'totalPackets',
      align: 'center',
      search: false,
      sorter: true,
      width: '10%',
      render: (_, record) => {
        return numeral(parseInt(record.totalPackets, 10)).format('0,0');
      },
    },
    {
      title: '操作',
      dataIndex: 'option',
      valueType: 'option',
      align: 'center',
      width: '20%',
      search: false,
      render: (_, record) => {
        return (
          <>
            <Button
              type="link"
              size="small"
              onClick={() => {
                const sessionFilterGroup = [
                  {
                    operator: EFilterOperatorTypes.EQ,
                    field: 'src_port',
                    operand: record.srcPort,
                  },
                  {
                    operator: EFilterOperatorTypes.EQ,
                    field: 'dest_port',
                    operand: record.destPort,
                  },
                  {
                    operator: EFilterOperatorTypes.EQ,
                    field: 'protocol',
                    operand: record.protocol,
                  },
                ];
                sessionFilterGroup.push(
                  {
                    operator: EFilterOperatorTypes.EQ,
                    field: 'src_ip',
                    operand: record.srcIp,
                  },
                  {
                    operator: EFilterOperatorTypes.EQ,
                    field: 'dest_ip',
                    operand: record.destIp,
                  },
                );

                jumpNewPage(
                  getLinkUrl(
                    `${pathname
                      .split('/')
                      .slice(0, -2)
                      .join('/')}/flow-record?filter=${encodeURIComponent(
                      JSON.stringify({
                        group: [...sessionFilterGroup, ...filterCondition],
                        operator: EFilterGroupOperatorTypes.AND,
                      }),
                    )}&from=${new Date(globalSelectedTime.originStartTime).getTime()}&to=${new Date(
                      globalSelectedTime.originEndTime,
                    ).getTime()}&timeType=${ETimeType.CUSTOM}`,
                  ),
                );
              }}
            >
              会话详单
            </Button>
          </>
        );
      },
    },
  ];
  return (
    // @ts-ignore
    <Template
      pageName="session"
      initialRank="totalBytes"
      tableColumns={tableColumns}
      filterCondition={filterCondition}
      addConditionToFilter={addConditionToFilter}
      queryFunction={queryFlowSessionHist}
    />
  );
};

export default connect(({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
}))(IPConversation);
