import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import application from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory, IProTableData } from '@/common/typings';
import AutoHeightContainer from '@/components/AutoHeightContainer';
import { TableEmpty } from '@/components/EnhancedTable';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { getGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { NetworkTypeContext } from '@/pages/app/Network/Analysis';
import { ServiceContext } from '@/pages/app/analysis/Service/index';
import { AlertContext } from '@/pages/app/Network/components/Alert';
import { jumpToAlertTab } from '@/pages/app/Network/components/Alert/constant';
import { EAlertTabs } from '@/pages/app/Network/components/Alert/typing';
import type { INetworkTreeItem } from '@/pages/app/Network/typing';
import { ENetowrkType } from '@/pages/app/Network/typing';
import ajax from '@/utils/frame/ajax';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button } from 'antd';
import { connect } from 'dva';
import { stringify } from 'qs';
import { Fragment, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { useParams } from 'umi';
import type { IAlertMessage } from '../../typings';
import { ALERT_CATEGORY_ENUM, ALERT_LEVEL_ENUM } from '../../typings';
import DisposeAlert, { EDispose } from '../components/DisposeAlert';
import type { IUriParams } from '@/pages/app/analysis/typings';
import useAbortXhr from '@/pages/app/Network/hooks/useAbortXhr';

const { API_VERSION_PRODUCT_V1 } = application;
interface IAlertMessageProps {
  globalSelectedTime: Required<IGlobalTime>;
}

const AlertMessage = ({ globalSelectedTime }: IAlertMessageProps) => {
  const [tableHeight, setTableHeight] = useState(200);
  const [total, setTotal] = useState(0);

  const urlIds = useParams<IUriParams>();
  const { serviceId, networkId } = useMemo(() => {
    const tmpNetworkId = urlIds.networkId || '';
    if (tmpNetworkId.includes('^')) {
      return {
        serviceId: urlIds.serviceId,
        networkId: tmpNetworkId.split('^')[1],
      };
    }
    return { serviceId: urlIds.serviceId, networkId: urlIds.networkId };
  }, [urlIds.networkId, urlIds.serviceId]);

  const [networkType] = serviceId
    ? useContext<[ENetowrkType, INetworkTreeItem[], any]>(ServiceContext)
    : useContext<[ENetowrkType, INetworkTreeItem[]]>(NetworkTypeContext);
  const actionRef = useRef<ActionType>();

  const [state, dispatch] = useContext(AlertContext);
  const columns: ProColumns<IAlertMessage>[] = [
    {
      title: '名称',
      dataIndex: 'name',
      align: 'center',
    },
    {
      title: '告警分类',
      dataIndex: 'category',
      align: 'center',
      valueType: 'select',
      valueEnum: Object.keys(ALERT_CATEGORY_ENUM).reduce((prev, current) => {
        return {
          ...prev,
          [current]: { text: ALERT_CATEGORY_ENUM[current] },
        };
      }, {}),
    },
    {
      title: '告警级别',
      dataIndex: 'level',
      align: 'center',
      valueType: 'select',
      valueEnum: Object.keys(ALERT_LEVEL_ENUM).reduce((prev, current) => {
        return {
          ...prev,
          [current]: { text: ALERT_LEVEL_ENUM[current] },
        };
      }, {}),
    },
    {
      title: '触发时间',
      dataIndex: 'ariseTime',
      align: 'center',
      search: false,
      ellipsis: true,
      valueType: 'dateTime',
    },
    {
      title: '操作',
      dataIndex: 'action',
      align: 'center',
      valueType: 'option',
      render: (text, record) => {
        return (
          <Fragment>
            <DisposeAlert
              id={record.id}
              buttonType={'link'}
              onChange={actionRef.current?.reload}
              disable={record.status === EDispose.Processed}
            />
            {/* <Link to={`${pathname}/${record.id}/message`}>详情</Link> */}
            <Button
              onClick={() => {
                jumpToAlertTab(state, dispatch, EAlertTabs.ALERTMESSAGEDETAIL, {
                  alertId: record.id,
                });
              }}
              type="link"
              size="small"
            >
              详情
            </Button>
          </Fragment>
        );
      },
    },
  ];

  useAbortXhr({
    cancelUrls: ['/appliance/alert-messages'],
  });

  useEffect(() => {
    actionRef.current?.reload();
  }, [networkId, serviceId, globalSelectedTime]);

  return (
    // 高度 - 搜索表单高度- 表头高度 - 分页高度
    <AutoHeightContainer onHeightChange={(h) => setTableHeight(h - 52 - 40)}>
      <ProTable<IAlertMessage>
        bordered
        size="small"
        actionRef={actionRef}
        columns={columns}
        pagination={getTablePaginationDefaultSettings()}
        request={async (params = {}) => {
          const { current, pageSize, startTime, endTime, ...rest } = params;
          const newParams = { pageSize, page: current! - 1, serviceId, ...rest } as any;
          newParams[networkType === ENetowrkType.NETWORK ? 'networkId' : 'networkGroupId'] =
            networkId;
          // 特殊处理时间
          let newGlobalSelectedTime = globalSelectedTime;
          if (globalSelectedTime.relative) {
            newGlobalSelectedTime = getGlobalTime(globalSelectedTime);
          }
          newParams.startTime = newGlobalSelectedTime.originStartTime;
          newParams.endTime = newGlobalSelectedTime.originEndTime;
          const { success, result } = (await ajax(
            `${API_VERSION_PRODUCT_V1}/appliance/alert-messages?${stringify(newParams)}`,
          )) as IAjaxResponseFactory<IPageFactory<IAlertMessage>>;

          setTotal(success ? result.totalElements : 0);

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
          } as IProTableData<IAlertMessage[]>;
        }}
        search={{
          ...proTableSerchConfig,
          span: 6,
        }}
        form={{
          ignoreRules: false,
        }}
        dateFormatter="string"
        toolBarRender={false}
        // 没有数据时，不显示分页，所以要把分页的高度也给占用了
        scroll={{ y: total ? tableHeight - 40 : tableHeight }}
        locale={{
          emptyText: <TableEmpty componentName="ProTable" height={tableHeight} />,
        }}
      />
    </AutoHeightContainer>
  );
};

export default connect(({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
}))(AlertMessage);
