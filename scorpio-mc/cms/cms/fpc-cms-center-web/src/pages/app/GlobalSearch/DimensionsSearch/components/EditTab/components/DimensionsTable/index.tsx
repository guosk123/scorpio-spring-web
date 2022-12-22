import EnhancedTable from '@/components/EnhancedTable';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type {
  ICityMap,
  ICountryMap,
  IProvinceMap,
} from '@/pages/app/Configuration/Geolocation/typings';
import type { IApplicationMap } from '@/pages/app/Configuration/SAKnowledge/typings';
import { snakeCase } from '@/utils/utils';
import type { TableColumnProps, TablePaginationConfig } from 'antd';
import { connect } from 'dva';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { queryDimensionsTable } from '../../../../service';
import type { EDRILLDOWN } from '../../../../typing';
import { EDimensionsSearchType, DimensionsSearchMapping } from '../../../../typing';
import type { ISearchBoxInfo } from '../../../SearchBox';
import type { DirllDownBoxType } from './components/DirllDownBubble';
import DirllDownBubble from './components/DirllDownBubble';
import { DimensionsCol } from './constant';

export enum ESortDirection {
  'DESC' = 'desc',
  'ASC' = 'asc',
}

interface Props {
  searchBoxInfo: ISearchBoxInfo;
  onClickDirllDown: any;
  tableSortProperty: any;
  clickRow: any;
  tableSortDirection: any;
  allCountryMap: ICountryMap;
  allProvinceMap: IProvinceMap;
  allCityMap: ICityMap;
  allApplicationMap: IApplicationMap;
  globalSelectedTime: IGlobalTime;
  drilldown?: EDRILLDOWN;
}

function DimensionsTable(props: Props) {
  const {
    searchBoxInfo,
    onClickDirllDown,
    tableSortProperty,
    clickRow,
    tableSortDirection,
    allCountryMap,
    allProvinceMap,
    allCityMap,
    allApplicationMap,
    globalSelectedTime,
    drilldown,
  } = props;
  const [sortProperty, setSortProperty] = useState<string>('totalBytes');
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);

  const translationCol = useCallback(
    (dataIndex, text) => {
      let tmpFunc: any = () => text;
      const translationType = searchBoxInfo?.dimensionsSearchType;
      if (translationType === EDimensionsSearchType.LOCATION) {
        tmpFunc = (index: any, content: any) => {
          const name = index;
          switch (name) {
            case 'countryId': {
              return allCountryMap[content]?.nameText || (content as string);
            }
            case 'provinceId': {
              return allProvinceMap[content]?.nameText || (content as string);
            }
            case 'cityId': {
              return allCityMap[content]?.nameText || (content as string);
            }
            default: {
              return content as string;
            }
          }
        };
      } else if (translationType === EDimensionsSearchType.APPLICATION) {
        tmpFunc = (index: any, content: any) => {
          return allApplicationMap[content]?.nameText || (content as string);
        };
      }

      return tmpFunc(dataIndex, text);
    },
    [
      allApplicationMap,
      allCityMap,
      allCountryMap,
      allProvinceMap,
      searchBoxInfo?.dimensionsSearchType,
    ],
  );

  const dimensionsColumns = useMemo<TableColumnProps<any>[]>(() => {
    const firstTableColMapping = {
      ipAddress: [{ title: 'IP地址', dataIndex: 'ipAddress' }],
      application: [{ title: '应用', dataIndex: 'applicationId' }],
      l7ProtocolId: [{ title: '应用层协议', dataIndex: 'l7ProtocolId' }],
      port: [{ title: '端口', dataIndex: 'port' }],
      ipConversation: [{ title: '地址组', dataIndex: 'hostgroupId' }],
      ipSegment: [{ title: 'IP地址', dataIndex: 'ipAddress' }],
      location: [
        { title: '国家', dataIndex: 'countryId' },
        { title: '省份', dataIndex: 'provinceId' },
      ],
    };

    const tmpFirstCol: any = firstTableColMapping[searchBoxInfo?.dimensionsSearchType]?.map(
      (item: any) => {
        return {
          ...item,
          sorter: false,
          ellipsis: true,
          align: 'center',
          render: (text: string, record: any) => {
            return (
              <div
                onClick={(e) => {
                  e.stopPropagation();
                }}
              >
                <DirllDownBubble
                  content={record[searchBoxInfo.dimensionsSearchType]}
                  dataIndex={item.dataIndex}
                  dirllDownTypes={Object.values(DimensionsSearchMapping).filter((ele) => {
                    return ele.name !== searchBoxInfo?.dimensionsSearchType;
                  })}
                  onClick={(e: DirllDownBoxType) => {
                    onClickDirllDown({
                      ...searchBoxInfo,
                      dimensionsSearchType: e.key,
                      content: e.content,
                    });
                  }}
                >
                  {translationCol(item.dataIndex, 1) || text || 1}
                  {/* {translationCol(item.dataIndex, text)} */}
                </DirllDownBubble>
              </div>
            );
          },
        };
      },
    );

    const cols = [
      {
        title: '#',
        align: 'center',
        dataIndex: 'index',
        width: 60,
        fixed: 'left',
        render: (text: any, record: any, index: number) => index + 1,
      },
      ...tmpFirstCol,
      ...DimensionsCol,
    ];
    return cols.map((col) => ({
      ...col,
      key: col.dataIndex,
      sortOrder: sortProperty === col.dataIndex ? `${sortDirection}end` : false,
      align: 'center',
    }));
  }, [searchBoxInfo, onClickDirllDown, sortDirection, sortProperty, translationCol]);

  const [dataSource, setDataSource] = useState();
  const dimensionsQueryData = (() => {
    const tmpIds = {
      networkId: searchBoxInfo?.networkIds
        .filter((item: string) => !item.includes('networkGroup'))
        .map((sub: string) => sub.replace('^network', ''))
        .join(','),
      networkGroupId: searchBoxInfo?.networkIds
        .filter((item: string) => item.includes('networkGroup'))
        .map((sub: string) => sub.replace('^networkGroup', ''))
        .join(','),
    };
    return tmpIds;
  })();

  const payload = useMemo(() => {
    return {
      ...dimensionsQueryData,
      // networkId: searchBoxInfo.networkIds.join(','),
      startTime: globalSelectedTime.startTime,
      endTime: globalSelectedTime.endTime,
      interval: globalSelectedTime.interval,
      sortProperty: snakeCase(sortProperty || ''),
      sortDirection: 'desc',
      drilldown,
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    drilldown,
    globalSelectedTime.endTime,
    globalSelectedTime.interval,
    globalSelectedTime.startTime,
    searchBoxInfo?.content,
    sortProperty,
  ]);

  const [queryLoding, setQueryLoding] = useState(true);

  useEffect(() => {
    tableSortDirection(sortDirection);
    tableSortProperty(sortProperty);
    queryDimensionsTable(payload).then((res) => {
      const { success, result } = res;
      if (success) {
        setQueryLoding(false);
        setDataSource(result);
      }
    });
  }, [searchBoxInfo, payload, sortDirection, sortProperty, tableSortDirection, tableSortProperty]);

  const handleTableChange = (pagination: TablePaginationConfig, filters: any, sorter: any) => {
    let newSortDirection: ESortDirection =
      sorter.order === 'descend' ? ESortDirection.DESC : ESortDirection.ASC;
    const newSortProperty = sorter.field;
    // 如果当前排序字段不是现在的字段，默认是倒序
    if (newSortProperty !== sortProperty) {
      newSortDirection = ESortDirection.DESC;
    }

    setSortDirection(newSortDirection);
    setSortProperty(newSortProperty);
  };

  return (
    <div>
      <EnhancedTable
        tableKey={`table-key`}
        columns={dimensionsColumns}
        rowKey={(record) => record.index}
        loading={queryLoding}
        dataSource={dataSource}
        size="small"
        bordered
        onChange={handleTableChange}
        autoHeight
        onRow={(record) => {
          return {
            onClick: (e) => {
              clickRow(e, record);
            },
          };
        }}
      />
    </div>
  );
}

export default connect((state: any) => {
  const {
    appModel: { globalSelectedTime },
    geolocationModel: { allCountryMap, allProvinceMap, allCityMap },
    SAKnowledgeModel: { allApplicationMap },
  } = state;
  return { allCountryMap, allProvinceMap, allCityMap, allApplicationMap, globalSelectedTime };
})(DimensionsTable);
