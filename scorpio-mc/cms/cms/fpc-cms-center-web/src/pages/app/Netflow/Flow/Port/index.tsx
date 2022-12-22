import type { ProColumns } from '@ant-design/pro-table';
import type { IFlowPort } from '../../typing';
import type { AppModelState } from '@/models/app/index';
import type { ConnectState } from '@/models/connect';
import { Button, Tooltip } from 'antd';
import { bytesToSize, getLinkUrl, jumpNewPage } from '@/utils/utils';
import { EFieldOperandType } from '@/components/FieldFilter/typings';
import { useContext } from 'react';
import { FilterContext } from '..';
import { queryFlowPortHist } from '../../service';
import { EFilterGroupOperatorTypes, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { connect } from 'umi';
import { ETimeType } from '@/components/GlobalTimeSelector';
import Template from '../../components/Template';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import numeral from 'numeral';

interface IFlowParams extends AppModelState {
  tableColumns: ProColumns<IFlowPort>[];
  pageName?: string;
  location: {
    pathname: string;
  };
}

const Port = ({ globalSelectedTime, location: { pathname } }: IFlowParams) => {
  // 该维度支持的过滤条件
  const { filterCondition, addConditionToFilter } = useContext(FilterContext);
  // 表格列定义
  const tableColumns: ProColumns<IFlowPort>[] = [
    {
      title: '端口',
      dataIndex: 'port',
      align: 'center',
      search: false,
      render: (_, record) => {
        const port = record.port;
        if (!port) {
          return null;
        }

        return (
          <FilterBubble
            dataIndex={'port'}
            label={
              <Tooltip placement="topLeft" title={port}>
                {port}
              </Tooltip>
            }
            operand={port}
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
            operandType={EFieldOperandType.STRING}
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
      width: '20%',
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
      width: '20%',
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
                jumpNewPage(
                  getLinkUrl(
                    `${pathname
                      .split('/')
                      .slice(0, -2)
                      .join('/')}/flow-record?filter=${encodeURIComponent(
                      JSON.stringify({
                        group: [
                          {
                            operator: EFilterOperatorTypes.EQ,
                            field: 'protocol',
                            operand: record.protocol,
                          },
                          {
                            group: [
                              {
                                operator: EFilterOperatorTypes.EQ,
                                field: 'src_port',
                                operand: record.port,
                              },
                              {
                                operator: EFilterOperatorTypes.EQ,
                                field: 'dest_port',
                                operand: record.port,
                              },
                            ],
                            operator: EFilterGroupOperatorTypes.OR,
                          },
                          ...filterCondition,
                        ],
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
      pageName="protocol-port"
      initialRank="totalBytes"
      tableColumns={tableColumns}
      filterCondition={filterCondition}
      addConditionToFilter={addConditionToFilter}
      queryFunction={queryFlowPortHist}
    />
  );
};

export default connect(({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
}))(Port);
