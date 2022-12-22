import type { ProColumns } from '@ant-design/pro-table';
import type { IFlowTransmitIp } from '../../typing';
import type { ConnectState } from '@/models/connect';
import type { AppModelState } from '@/models/app/index';
import { Button, Tooltip } from 'antd';
import { bytesToSize, getLinkUrl, jumpNewPage } from '@/utils/utils';
import { EFieldOperandType, EFilterGroupOperatorTypes } from '@/components/FieldFilter/typings';
import { useContext } from 'react';
import { FilterContext } from '..';
import { queryFlowTransmitIpHist } from '../../service';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { connect } from 'umi';
import { ETimeType } from '@/components/GlobalTimeSelector';
import Template from '../../components/Template';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import numeral from 'numeral';

interface IFlowParams extends AppModelState {
  tableColumns: ProColumns<IFlowTransmitIp>[];
  pageName?: string;
  location: {
    pathname: string;
  };
}

const SenderIp = ({ globalSelectedTime, location: { pathname } }: IFlowParams) => {
  // 该维度支持的过滤条件
  const { filterCondition, addConditionToFilter } = useContext(FilterContext);
  // 表格列定义
  const tableColumns: ProColumns<IFlowTransmitIp>[] = [
    {
      title: 'IP地址',
      dataIndex: 'ipAddress',
      width: '20%',
      align: 'center',
      search: false,
      render: (_, record) => {
        const ipAddress = record.ipAddress;
        if (!ipAddress) {
          return null;
        }
        return (
          <FilterBubble
            dataIndex={'ip_address'}
            label={
              <Tooltip placement="topLeft" title={ipAddress}>
                {ipAddress}
              </Tooltip>
            }
            operand={ipAddress}
            operandType={EFieldOperandType.IPV4}
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
      dataIndex: 'transmitBytes',
      align: 'center',
      search: false,
      sorter: true,
      width: '20%',
      render: (_, record) => {
        if (record.transmitBytes && parseInt(record.transmitBytes, 10) !== 0) {
          return bytesToSize(parseInt(record.transmitBytes, 10));
        }
        return 0;
      },
    },
    {
      title: '总包数',
      dataIndex: 'transmitPackets',
      align: 'center',
      search: false,
      sorter: true,
      width: '20%',
      render: (_, record) => {
        return numeral(parseInt(record.transmitPackets, 10)).format('0,0');
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
              onClick={() =>
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
                            field: 'ip_address',
                            operand: record.ipAddress,
                          },
                          ...filterCondition,
                        ],
                        operator: EFilterGroupOperatorTypes.AND,
                      }),
                    )}&from=${new Date(globalSelectedTime.originStartTime).getTime()}&to=${new Date(
                      globalSelectedTime.originEndTime,
                    ).getTime()}&timeType=${ETimeType.CUSTOM}`,
                  ),
                )
              }
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
      pageName="transmit-ip"
      initialRank="transmitBytes"
      tableColumns={tableColumns}
      filterCondition={filterCondition}
      addConditionToFilter={addConditionToFilter}
      queryFunction={queryFlowTransmitIpHist}
    />
  );
};

export default connect(({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
}))(SenderIp);
