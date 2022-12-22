import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
// import Import from '@/components/Import';
import ajax from '@/utils/frame/ajax';
import type { IAjaxResponseFactory, IPageFactory, IProTableData } from '@/common/typings';
// import { ExportOutlined, PlusOutlined, UploadOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { IocTypeNameMap } from './typings';
import type { IThreatIntelligence } from './typings';
import { stringify } from 'querystring';
import { ThreatbookbasicTagSelection } from './service';
import AutoHeightContainer from '@/components/AutoHeightContainer';

const ThreatIntelligenceList = () => {
  const actionRef = useRef<ActionType>();
  const [basicTags, setBasicTags] = useState([]);
  const queryBasicTagSelection = useCallback(async () => {
    const { success, result } = await ThreatbookbasicTagSelection();
    if (success) {
      console.log(result, 'result');
      setBasicTags(result);
    }
  }, []);

  useEffect(() => {
    queryBasicTagSelection();
  }, [queryBasicTagSelection]);

  const basicTagMap = useMemo(() => {
    console.log(basicTags, 'basicTags');
    const map = {};
    basicTags.forEach((item: string) => {
      map[item] = item;
    });
    console.log(map, 'map');
    return map;
  }, [basicTags]);

  const columns: ProColumns<IThreatIntelligence>[] = [
    {
      title: 'IOC类型',
      dataIndex: 'iocType',
      align: 'center',
      valueType: 'select',
      valueEnum: IocTypeNameMap,
    },
    {
      title: 'IOC内容',
      dataIndex: 'iocRaw',
      search: false,
      align: 'center',
    },
    {
      title: '基础标签',
      dataIndex: 'basicTag',
      align: 'center',
      valueType: 'select',
      valueEnum: basicTagMap,
    },
    {
      title: '标签',
      dataIndex: 'tag',
      align: 'center',
    },
    {
      title: '情报类型',
      dataIndex: 'intelType',
      align: 'center',
      search: false,
    },
    {
      title: '来源',
      dataIndex: 'source',
      align: 'center',
      search: false,
    },
    {
      title: '时间',
      key: 'time',
      dataIndex: 'time',
      align: 'center',
      search: false,
      valueType: 'dateTime',
    },
    // {
    //   title: '操作',
    //   key: 'option',
    //   align: 'center',
    //   width: 300,
    //   valueType: 'option',
    //   render: (_, record) => [],
    // },
  ];
  const [tableHeight, setTableHeight] = useState(0);
  const handleHeightChange = (height: number) => {
    setTableHeight(height);
  };

  return (
    <AutoHeightContainer autoHeight={true} onHeightChange={handleHeightChange}>
      <ProTable<IThreatIntelligence>
        bordered
        size="small"
        columns={columns}
        request={async (params = {}) => {
          const { current, pageSize, ...rest } = params;
          const newParams = { pageSize, page: current! - 1, ...rest };
          const { success, result } = (await ajax(
            `${API_VERSION_PRODUCT_V1}/analysis/ti-threatbook?${stringify(newParams)}`,
          )) as IAjaxResponseFactory<IPageFactory<IThreatIntelligence>>;
          if (!success) {
            return {
              data: [],
              success,
            };
          }

          return {
            data: result.content,
            success,
            page: result.number,
            total: result.totalElements,
          } as IProTableData<IThreatIntelligence[]>;
        }}
        rowKey="id"
        search={{
          ...proTableSerchConfig,
          // optionRender: (searchConfig, formProps, dom) => [...dom.reverse()],
        }}
        form={{
          ignoreRules: false,
        }}
        actionRef={actionRef}
        onReset={actionRef.current?.reload}
        dateFormatter="string"
        toolBarRender={false}
        pagination={getTablePaginationDefaultSettings()}
        scroll={{ y: tableHeight - 225, x:  'max-content'}}
      />
    </AutoHeightContainer>
  );
};

export default ThreatIntelligenceList;
