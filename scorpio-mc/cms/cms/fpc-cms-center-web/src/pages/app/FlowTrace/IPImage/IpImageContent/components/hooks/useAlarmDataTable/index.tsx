import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { getLinkUrl, jumpNewPage } from '@/utils/utils';
import { Tooltip } from 'antd';
import type { AlarmTableTitle } from '../../../../typings';
import { SeverityLevelMap } from '../../../../typings';
import type { ColumnsType } from 'antd/lib/table';
import moment from 'moment';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { useSelector } from 'umi';
import { useMemo } from 'react';
import type { IIpImageMoreTable } from '../useIpDataTable';

export default function useAlarmDataTable(props: IIpImageMoreTable) {
  const { networkId, sortProperty, sortDirection, windowWidth } = props;
  const globalSelectTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state: ConnectState) => state.appModel.globalSelectedTime,
  );
  const Tablecolumns: ColumnsType<AlarmTableTitle> = useMemo(() => {
    return [
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>名称</span>,
        dataIndex: 'msg',
        align: 'center',
        render: (_, record) => {
          const flowRecordFilter = {
            field: 'msg',
            operator: EFilterOperatorTypes.EQ,
            operand: record.msg,
          };
          return (
            <div
              style={{
                whiteSpace: 'nowrap',
                width: windowWidth,
                textOverflow: 'ellipsis',
                overflow: 'hidden',
                color: '#1890ff',
                cursor: 'pointer',
              }}
              onClick={() => {
                const url = getLinkUrl(
                  `/analysis/security/alert?from=${moment(
                    globalSelectTime.originStartTime,
                  ).valueOf()}&to=${moment(globalSelectTime.originEndTime).valueOf()}&timeType=${
                    ETimeType.CUSTOM
                  }&filter=${encodeURIComponent(
                    JSON.stringify([
                      flowRecordFilter,
                      {
                        field: 'network_id',
                        operator: EFilterOperatorTypes.EQ,
                        operand: networkId,
                      },
                    ]),
                  )}`,
                );
                jumpNewPage(url);
              }}
            >
              <Tooltip title={record.msg}>{record.msg}</Tooltip>
            </div>
          );
        },
      },
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>最近发生时间</span>,
        dataIndex: 'timestamp',
        width: 150,
        align: 'center',
      },
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>严重级别</span>,
        dataIndex: 'signatureSeverity',
        width: 80,
        align: 'center',
        sorter: true,
        sortOrder: (sortProperty === 'signatureSeverity' ? `${sortDirection}end` : false) as any,
        render: (_, record) => {
          return SeverityLevelMap[record.signatureSeverity];
        },
      },
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>发生次数</span>,
        width: 80,
        dataIndex: 'counts',
        align: 'center',
        sorter: true,
        sortOrder: (sortProperty === 'counts' ? `${sortDirection}end` : false) as any,
      },
    ];
  }, [
    globalSelectTime.originEndTime,
    globalSelectTime.originStartTime,
    networkId,
    sortDirection,
    sortProperty,
    windowWidth,
  ]);
  return Tablecolumns;
}
