import type { ProColumns } from '@ant-design/pro-table';
import type { IFlowIp } from '../../typing';
import type { AppModelState } from '@/models/app/index';
import type { ConnectState } from '@/models/connect';
import { Button, Tooltip } from 'antd';
import { EFieldOperandType, EFilterGroupOperatorTypes } from '@/components/FieldFilter/typings';
import { bytesToSize, getLinkUrl, jumpNewPage } from '@/utils/utils';
import { useContext } from 'react';
import { FilterContext } from '../../Flow';
import { queryFlowIpHist } from '../../service';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { connect } from 'umi';
import { ETimeType } from '@/components/GlobalTimeSelector';
import numeral from 'numeral';
import Template from '../../components/Template';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';

interface IFlowParams extends AppModelState {
  tableColumns: ProColumns<IFlowIp>[];
  pageName?: string;
  location: {
    pathname: string;
  };
}

const IP = ({ globalSelectedTime, location: { pathname } }: IFlowParams) => {
  // 本维度支持的过滤条件
  const { filterCondition, addConditionToFilter } = useContext(FilterContext);
  // 表格列定义
  const tableColumns: ProColumns<IFlowIp>[] = [
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
      pageName="ip"
      initialRank="totalBytes"
      tableColumns={tableColumns}
      filterCondition={filterCondition}
      addConditionToFilter={addConditionToFilter}
      queryFunction={queryFlowIpHist}
    />
  );
};

export default connect(({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
}))(IP);
