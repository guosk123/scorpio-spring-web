import { EMetricApiType } from '@/common/api/analysis';
import { ANALYSIS_APPLICATION_TYPE_ENUM } from '@/common/app';
import AutoHeightContainer from '@/components/AutoHeightContainer';
import EnhancedTable from '@/components/EnhancedTable';
// import EnhancedTable from '@/components/EnhancedTable';
import type { ConnectState } from '@/models/connect';
import {
  fieldFormatterFuncMap,
  fieldsMapping,
  SortedTypes,
} from '@/pages/app/analysis/components/fieldsManager';
import type { LocalTableColumnProps } from '@/pages/app/analysis/Flow/Application';
import { commonFieldList, overloadFieldsMapping } from '@/pages/app/analysis/Flow/Application';
import { queryNetworkFlow } from '@/pages/app/analysis/service';
import { ESortDirection, ESourceType } from '@/pages/app/analysis/typings';
import type { IApplicationMap } from '@/pages/app/configuration/SAKnowledge/typings';
import { isExisty, snakeCase } from '@/utils/utils';
import type { TableColumnProps, TablePaginationConfig } from 'antd';
import { Card } from 'antd';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { connect } from 'umi';

interface IServiceApplicationProps {
  serviceId?: string;
  networkId?: string;
  allApplicationMap: IApplicationMap;
  style?: React.CSSProperties;
  bodyStyle?: React.CSSProperties;
  isFullscreen?: boolean;
  timeInfo: any;
}

const ServiceApplicationStat = (props: IServiceApplicationProps) => {
  const {
    serviceId,
    networkId,
    allApplicationMap,
    bodyStyle = { height: 480 },
    style,
    timeInfo: { startTime, endTime, interval },
    isFullscreen = false,
  } = props;
  const [applicationStat, setApplicationStat] = useState<LocalTableColumnProps[]>([]);
  const [queryLoading, setQueryLoading] = useState(false);

  const [sortProperty, setSortProperty] = useState<string>('totalBytes');
  const [sortDirection, setSortDirection] = useState(ESortDirection.DESC);

  const queryServiceApplicationStat = useCallback(async () => {
    if (!networkId || !serviceId) {
      return;
    }

    setQueryLoading(true);
    setApplicationStat([]);
    const { success, result } = await queryNetworkFlow({
      sourceType: ESourceType.SERVICE,
      startTime,
      endTime,
      interval,
      networkId,
      serviceId,
      metricApi: EMetricApiType.application,
      sortProperty: snakeCase(sortProperty),
      sortDirection,
      drilldown: '0',
      dsl: `(network_id=${networkId} and service_id=${serviceId} and type = ${ANALYSIS_APPLICATION_TYPE_ENUM.应用})| gentimes timestamp start="${startTime}" end="${endTime}"`,
    });
    setQueryLoading(false);
    if (success) {
      setApplicationStat(result);
    }
  }, [networkId, serviceId, startTime, endTime, interval, sortProperty, sortDirection]);

  useEffect(() => {
    queryServiceApplicationStat();
  }, [queryServiceApplicationStat]);

  const columns = useMemo(() => {
    const fullColumns: TableColumnProps<LocalTableColumnProps>[] = [
      {
        title: '应用',
        align: 'center',
        dataIndex: 'applicationId',
        width: 100,
        ellipsis: true,
        fixed: 'left',
        render: (id) => (isExisty(id) ? allApplicationMap[id]?.nameText || `[已删除: ${id}]` : ''),
      },
    ];
    fullColumns.push(
      ...commonFieldList.map((field) => {
        const { name, formatterType } =
          (overloadFieldsMapping && overloadFieldsMapping[field]) || fieldsMapping[field];
        const renderFunc = fieldFormatterFuncMap[formatterType];
        return {
          title: name,
          dataIndex: field,
          width: name.length * 18 + 40,
          align: 'center' as any,
          ellipsis: true,
          sorter: SortedTypes.includes(formatterType),
          sortOrder: (sortProperty === field ? `${sortDirection}end` : false) as any,
          ...(renderFunc ? { render: (value: any) => renderFunc(value) } : {}),
        };
      }),
    );
    return fullColumns;
  }, [allApplicationMap, sortProperty, sortDirection]);

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

  const [tableHeight, setTableHeight] = useState(400);

  return (
    <Card
      size="small"
      title="应用性能指标"
      style={style}
      bodyStyle={bodyStyle}
      // extra={
      //   <Button
      //     type="link"
      //     style={{ float: 'right' }}
      //     size="small"
      //     icon={<ReloadOutlined />}
      //     loading={queryLoading}
      //     onClick={queryServiceApplicationStat}
      //   >
      //     刷新
      //   </Button>
      // }
    >
      <AutoHeightContainer
        onHeightChange={(h) => setTableHeight(h - 160)}
        autoHeight={isFullscreen}
      >
        <EnhancedTable<LocalTableColumnProps>
          tableKey="service-situation-application"
          rowKey={(col) => col.applicationId}
          sortProperty={sortProperty}
          loading={queryLoading}
          columns={columns}
          dataSource={applicationStat}
          draggable={true}
          onChange={handleTableChange}
          autoHeight={false}
          showColumnTool={true}
          scroll={{
            y: isFullscreen ? tableHeight : 480 - 200,
          }}
        />
      </AutoHeightContainer>
      {/* <Table
          bordered
          size={'small'}
          columns={columns}
          loading={queryLoading}
          dataSource={applicationStat}
          scroll={{ y: isFullscreen ? tableHeight : 300 }}
          rowKey={(col) => col.applicationId}
        /> */}
    </Card>
  );
};

export default connect(({ SAKnowledgeModel: { allApplicationMap } }: ConnectState) => ({
  allApplicationMap,
}))(ServiceApplicationStat);
