import { useMemo } from 'react';
import type { ColumnsType } from 'antd/lib/table';
import type { AlarmTableTitle } from '../../../../typings';
import { SeverityLevelMap } from '../../../../typings';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import EllipsisCom from '@/components/EllipsisCom';
import LinkMenu, { EIP_DRILLDOWN_MENU_KEY } from '../../LinkMenu';
import type { IIpImageMoreTable } from '../useIpdataTable';

export default function useAlarmTable(props: IIpImageMoreTable) {
  const { sortProperty, sortDirection, widowWidth } = props;
  const Tablecolumns: ColumnsType<AlarmTableTitle> = useMemo(() => {
    return [
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>名称</span>,
        dataIndex: 'msg',
        width: widowWidth,
        render: (_, record) => {
          return (
            <FilterBubble
              dataIndex={record.msg}
              label={<EllipsisCom style={{ width: widowWidth }}>{record.msg}</EllipsisCom>}
              hasFilter={false}
              DrilldownMenu={
                <LinkMenu
                  MenuItemsGroup={[
                    {
                      label: '跳转到其他页',
                      key: 'jumpToOtherPage',
                      children: [{ label: '安全告警', key: EIP_DRILLDOWN_MENU_KEY.SECURITY_ALARM }],
                    },
                  ]}
                  settings={{
                    alarmMessage: record.msg,
                  }}
                />
              }
            />
          );
        },
      },
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>最近发生时间</span>,
        dataIndex: 'timestamp',
        // width: 150,
      },
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>严重级别</span>,
        dataIndex: 'signatureSeverity',
        // width: 80,
        sorter: true,
        sortOrder: (sortProperty === 'signatureSeverity' ? `${sortDirection}end` : false) as any,
        render: (_, record) => {
          return SeverityLevelMap[record.signatureSeverity];
        },
      },
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>发生次数</span>,
        // width: 80,
        dataIndex: 'counts',
        sorter: true,
        sortOrder: (sortProperty === 'counts' ? `${sortDirection}end` : false) as any,
      },
    ];
  }, [sortDirection, sortProperty, widowWidth]);

  return Tablecolumns;
}
