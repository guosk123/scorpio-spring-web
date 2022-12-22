import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { getLinkUrl, jumpNewPage } from '@/utils/utils';
import { Button, Table } from 'antd';
import type { ColumnsType } from 'antd/lib/table';
import numeral from 'numeral';
import { useContext, useMemo } from 'react';
import { useParams, useSelector } from 'umi';
import { MetaDataContext } from '../../../Analysis';
import { jumpToMetadataTab } from '../../../Analysis/constant';
import { EMetadataTabType } from '../../../Analysis/typings';

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
}

export default function OverviewProtocolTable(props: Props) {
  const { data, srcIp } = props;

  const { pcapFileId } = useParams() as { pcapFileId: string };
  const [state, dispatch] = useContext(MetaDataContext);

  const { startTimestamp, endTimestamp } = useSelector<ConnectState, Required<IGlobalTime>>(
    (globalState) => globalState.appModel.globalSelectedTime,
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
  const columns: ColumnsType<IProtocolItem> = useMemo(() => {
    return [
      {
        title: '协议',
        dataIndex: 'protocol',
        key: 'protocol',
        align: 'center',
        render: (text) => {
          return (
            <Button
              type={'link'}
              size={'small'}
              onClick={() => {
                const filter = srcIp
                  ? [
                      {
                        field: 'src_ipv4',
                        operator: EFilterOperatorTypes.EQ,
                        operand: srcIp,
                      },
                    ]
                  : [];
                if (pcapFileId) {
                  jumpToMetadataTab(
                    state,
                    dispatch,
                    EMetadataTabType[text.toLocaleUpperCase()],
                    filter,
                  );
                } else {
                  jumpNewPage(
                    getLinkUrl(
                      `/analysis/trace/metadata/record?filter=${encodeURIComponent(
                        JSON.stringify(filter),
                      )}&jumpTabs=${text}&from=${startTimestamp}&to=${endTimestamp}&timeType=${
                        ETimeType.CUSTOM
                      }`,
                    ),
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
  }, [srcIp, pcapFileId, state, dispatch, startTimestamp, endTimestamp, tableData.totalCount]);

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
