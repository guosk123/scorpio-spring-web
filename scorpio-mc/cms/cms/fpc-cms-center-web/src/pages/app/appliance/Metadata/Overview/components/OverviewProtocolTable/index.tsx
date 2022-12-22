import { Button, Table } from 'antd';
import type { ColumnsType } from 'antd/lib/table';
import { useContext, useMemo } from 'react';
import numeral from 'numeral';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { jumpToMetadataDetailTab, jumpToMetadataTab } from '../../..';
import { MetaDataContext } from '../../..';
import { EMetadataTabType } from '@/pages/app/appliance/Metadata/typings';
import { DimensionsSearchContext } from '@/pages/app/GlobalSearch/DimensionsSearch/SeartchTabs';
import { jumpToDimensionsTab } from '@/pages/app/GlobalSearch/DimensionsSearch/SeartchTabs/constant';
import { EDimensionsTab } from '@/pages/app/GlobalSearch/DimensionsSearch/SeartchTabs/typing';
import { getTabDetail } from '@/pages/app/Network/components/EditTabs';
import { history, useSelector } from 'umi';
import type { ConnectState } from '@/models/connect';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import moment from 'moment';

interface IProtocolItem {
  key: string;
  protocol: string;
  count: number;
}

interface ITableData {
  data: IProtocolItem[];
  totalCount: number;
}

interface Props {
  data: any;
  srcIp: string | undefined;
  isDimensionsTab: boolean;
}

export default function OverviewProtocolTable(props: Props) {
  const { data, srcIp, isDimensionsTab } = props;
  const [state, dispatch] = useContext(isDimensionsTab ? DimensionsSearchContext : MetaDataContext);
  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state) => state.appModel.globalSelectedTime,
  );
  const tableData = useMemo<ITableData>(() => {
    let totalCount = 0;
    const dataSource: IProtocolItem[] = [];

    Object.keys(data).forEach((protocol) => {
      const count = data[protocol] || 0;
      totalCount += count;

      if (count > 0) {
        dataSource.push({
          key: protocol,
          protocol,
          count,
        });
      }
    });
    return {
      data: dataSource,
      totalCount,
    };
  }, [data]);
  const flowAnalysisDetail = (() => {
    return getTabDetail(state) || {};
  })();
  const columns: ColumnsType<IProtocolItem> = useMemo(() => {
    return [
      {
        title: '协议',
        dataIndex: 'protocol',
        key: 'protocol',
        align: 'center',
        render: (text: any) => {
          return (
            <Button
              type={'link'}
              size={'small'}
              onClick={() => {
                const tmpInfo = srcIp
                  ? JSON.stringify([
                      {
                        field: 'src_ipv4',
                        operator: EFilterOperatorTypes.EQ,
                        operand: srcIp,
                      },
                    ])
                  : '';
                const isNewPage = history.location.pathname.includes('mata-data-analysis');
                if (isNewPage) {
                  jumpToMetadataDetailTab(
                    {
                      startTime: moment(globalSelectedTime.startTime).valueOf(),
                      endTime: moment(globalSelectedTime.endTime).valueOf(),
                    },
                    EDimensionsTab[text.toLocaleUpperCase()],
                  );
                }
                if (isDimensionsTab) {
                  jumpToDimensionsTab(
                    state,
                    dispatch,
                    EDimensionsTab[text.toLocaleUpperCase()],
                    flowAnalysisDetail?.searchBoxInfo,
                  );
                } else {
                  jumpToMetadataTab(
                    state,
                    dispatch,
                    EMetadataTabType[text.toLocaleUpperCase()],
                    tmpInfo,
                  );
                }
              }}
            >
              {text}
            </Button>
          );
        },
      },
      {
        title: '事件数量',
        dataIndex: 'count',
        key: 'count',
        align: 'center',
        render: (count) => numeral(count).format('0,0'),
      },
      {
        title: '占比',
        dataIndex: 'precent',
        key: 'precent',
        align: 'center',
        render: (text, record) => {
          if (tableData.totalCount === 0) {
            return '--';
          }
          return `${((record.count / tableData.totalCount) * 100).toFixed(2)}%`;
        },
      },
    ];
  }, [
    dispatch,
    flowAnalysisDetail?.searchBoxInfo,
    globalSelectedTime.endTime,
    globalSelectedTime.startTime,
    isDimensionsTab,
    srcIp,
    state,
    tableData.totalCount,
  ]);

  return (
    <Table
      title={() => (
        <div style={{ textAlign: 'center' }}>
          {srcIp && <span>源IP: {srcIp}，</span>}
          <span>事件总数量: {numeral(tableData.totalCount).format('0,0')}</span>
        </div>
      )}
      rowKey={(record) => record.protocol}
      size="small"
      pagination={false}
      bordered
      dataSource={tableData.data.sort((v1, v2) => v2.count - v1.count)}
      columns={columns}
    />
  );
}
