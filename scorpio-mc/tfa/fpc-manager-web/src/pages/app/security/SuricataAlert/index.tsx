import { getTablePaginationDefaultSettings } from '@/common/app';
import { API_BASE_URL, API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import CustomPagination from '@/components/CustomPagination';
import EnhancedTable from '@/components/EnhancedTable';
import FieldFilter, { filterCondition2Spl } from '@/components/FieldFilter';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import type { IField, IFilter, IFilterCondition } from '@/components/FieldFilter/typings';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType, globalTimeFormatText } from '@/components/GlobalTimeSelector';
import useCancelAllQuery from '@/hooks/useCancelAllQuery';
import type { ConnectState } from '@/models/connect';
import {
  RuleProtocolOptions,
  RuleSignatureSeverityOptions,
} from '@/pages/app/configuration/Suricata/common';
import type {
  IColumnProps,
  IMitreAttack,
  IRuleClasstype,
  ISuricataAlertMessage,
} from '@/pages/app/security/typings';
import useFetchData from '@/utils/hooks/useFetchData';
import { getLinkUrl, isIpv4, jumpNewPage, snakeCase } from '@/utils/utils';
import { useDebounceFn, useLatest, useSafeState } from 'ahooks';
import type { TableProps } from 'antd';
import { Button, Col, Menu, message, Modal, Row, Space, Tooltip } from 'antd';
import moment from 'moment';
import qs, { stringify } from 'qs';
import { useEffect, useMemo, useState } from 'react';
import { history, useLocation, useSelector } from 'umi';
import { v1 as uuidv1 } from 'uuid';
import LinkMenu, {
  EIP_DRILLDOWN_MENU_KEY,
} from '../../analysis/Network/IPImage/IpImageContent/components/LinkMenu';
import { ESortDirection } from '../../analysis/typings';
import DownLoadPktBtn from '../../appliance/components/DownLoadPktBtn';
import ExportFile from '../../appliance/components/ExportFile';
import { querySuricataSource } from '../../configuration/Suricata/service';
import ClasstypeList from '../components/ClasstypeList';
import MitreAttackTree from '../components/MitreAttackTree';
import { queryAlertMessageList, queryAlertMessageTags, queryAlertPackets } from '../service';
import Detail from './Detail';
import styles from './index.less';

const AlertMessage = () => {
  const {
    query: { filter },
  } = useLocation() as unknown as { query: { filter?: string } };

  const [selectedMitreIds, setSelectedMitreIds] = useState<string[]>();
  const [selectedClasstypeIds, setSelectedClasstypeIds] = useState<string[]>();
  const [filterCondition, setFilterCondition] = useState<IFilter[]>(() => {
    if (filter) {
      const parsed = JSON.parse(decodeURIComponent(filter)) as IFilter[];
      return [...parsed];
    }
    return [];
  });
  const [selectdRow, setSelectdRow] = useState<ISuricataAlertMessage>();
  const [sortProperty, setSortProperty] = useState<string>('timestamp');
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);

  const { data: basicTags } = useFetchData<string[]>(queryAlertMessageTags);

  const [pagination, setPagination] = useState<{
    total: number;
    currentPage: number;
    pageSize: number;
  }>({
    total: 0,
    currentPage: 0,
    pageSize: getTablePaginationDefaultSettings().defaultPageSize!,
  });

  const [tableData, setTableData] = useState<ISuricataAlertMessage[]>([]);
  const [tableLoading, setTableLoading] = useState<boolean>(false);
  const [pageIsReady, setPageIsReady] = useState<boolean>(false);

  const mitreDict = useSelector<ConnectState, Record<string, IMitreAttack>>(
    (state: ConnectState) => state.suricataModel.mitreDict,
  );

  const attackList = useSelector<ConnectState, IMitreAttack[]>(
    (state) => state.suricataModel.mitreAttackList,
  );

  const classtypeDict = useSelector<ConnectState, Record<string, IRuleClasstype>>(
    (state: ConnectState) => state.suricataModel.classtypeDict,
  );

  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state: ConnectState) => state.appModel.globalSelectedTime,
  );

  const selectedTime = useLatest(globalSelectedTime);

  const [sources, setSources] = useSafeState<Record<string, string>>({});

  const columns: IColumnProps<ISuricataAlertMessage>[] = useMemo(() => {
    return [
      {
        title: '时间',
        dataIndex: 'timestamp',
        sortOrder: 'descend',
        width: 250,
        render: (time) => {
          return moment(time).format(globalTimeFormatText);
        },
      },
      {
        title: '来源',
        dataIndex: 'source',
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        enumValue: Object.keys(sources).map((key) => {
          return {
            value: key,
            text: sources[key],
          };
        }),
        render: (_, record) => {
          if (record.source) {
            return sources[record.source] || record.source;
          }
          return '-';
        },
        width: 80,
      },
      {
        title: '规则id',
        dataIndex: 'sid',
        searchable: true,
        operandType: EFieldOperandType.NUMBER,
      },
      {
        title: '规则描述',
        dataIndex: 'msg',
        searchable: true,
        ellipsis: true,
      },
      {
        title: '规则分类',
        dataIndex: 'classtypeId',
        align: 'center',
        search: false,
        render: (_, record) => {
          if (record.classtypeId) {
            return classtypeDict[record.classtypeId]?.name || '-';
          }
          return '-';
        },
        width: 150,
      },
      {
        title: '战术分类',
        dataIndex: 'mitreTacticId',
        align: 'center',
        search: false,
        render: (_, record) => {
          if (record.mitreTacticId) {
            return mitreDict[record.mitreTacticId]?.name;
          }
          return '-';
        },
        width: 150,
      },
      {
        title: '技术分类',
        dataIndex: 'mitreTechniqueId',
        align: 'center',
        search: false,
        render: (_, record) => {
          if (record.mitreTechniqueId) {
            return mitreDict[record.mitreTechniqueId]?.name;
          }
          return '-';
        },
        width: 150,
      },
      {
        title: '域名',
        dataIndex: 'domain',
        searchable: true,
        align: 'center',
        operandType: EFieldOperandType.STRING,
      },
      {
        title: 'CVE',
        dataIndex: 'cve',
        align: 'center',
        ellipsis: true,
        searchable: true,
        operandType: EFieldOperandType.STRING,
      },
      {
        title: 'CNNVD',
        dataIndex: 'cnnvd',
        ellipsis: true,
        searchable: true,
        operandType: EFieldOperandType.STRING,
      },
      {
        title: '严重级别',
        dataIndex: 'signatureSeverity',
        searchable: true,
        valueType: 'select',
        sortOrder: 'descend',
        operandType: EFieldOperandType.ENUM,
        enumValue: RuleSignatureSeverityOptions.map((item) => {
          return {
            value: item.value.toString(),
            text: item.label.toString(),
          };
        }),
        render: (value) => {
          return RuleSignatureSeverityOptions.find((item) => item.value === value.toString())
            ?.label;
        },
      },
      {
        title: '受害者',
        dataIndex: 'target',
        searchable: true,
      },
      {
        title: '传输协议',
        dataIndex: 'protocol',
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        enumValue: RuleProtocolOptions.map((item) => {
          return {
            value: item.value,
            text: item.label,
          };
        }),
      },
      {
        title: '应用协议',
        dataIndex: 'l7Protocol',
        search: false,
      },
      {
        title: '源IP',
        dataIndex: 'srcIp',
        searchable: false,
        render: (dom, record) => {
          const ipInitiator = record.srcIp;
          if (!ipInitiator) {
            return null;
          }

          return (
            <FilterBubble
              dataIndex={isIpv4(ipInitiator) ? 'src_ipv4' : 'src_ipv6'}
              label={
                <Tooltip placement="topLeft" title={ipInitiator}>
                  {ipInitiator}
                </Tooltip>
              }
              operand={ipInitiator}
              operandType={isIpv4(ipInitiator) ? EFieldOperandType.IPV4 : EFieldOperandType.IPV6}
              onClick={(newFilter) => {
                setFilterCondition((prev) => [...prev, newFilter]);
              }}
              style={{ width: 200 }}
              DrilldownMenu={
                <LinkMenu
                  MenuItemsGroup={[
                    {
                      label: '跳转到其他页',
                      key: 'jumpToOtherPage',
                      children: [{ label: 'IP画像', key: EIP_DRILLDOWN_MENU_KEY.IP_IMAGE }],
                    },
                  ]}
                  settings={{ imageIp: ipInitiator }}
                />
              }
            />
          );
        },
      },
      {
        title: '源ipv4',
        dataIndex: 'srcIpv4',
        searchable: true,
        show: false,
        operandType: EFieldOperandType.IPV4,
        fieldType: EFieldType.IPV4,
      },
      {
        title: '源ipv6',
        dataIndex: 'srcIpv6',
        searchable: true,
        show: false,
        operandType: EFieldOperandType.IPV6,
        fieldType: EFieldType.IPV6,
      },
      {
        title: '源端口',
        dataIndex: 'srcPort',
        searchable: true,
        operandType: EFieldOperandType.PORT,
      },
      {
        title: '目的IP',
        dataIndex: 'destIp',
        searchable: false,
        render: (dom, record) => {
          const ipResponder = record.destIp;
          if (!ipResponder) {
            return null;
          }

          return (
            <FilterBubble
              dataIndex={isIpv4(ipResponder) ? 'dest_ipv4' : 'dest_ipv6'}
              label={
                <Tooltip placement="topLeft" title={ipResponder}>
                  {ipResponder}
                </Tooltip>
              }
              operand={ipResponder}
              operandType={isIpv4(ipResponder) ? EFieldOperandType.IPV4 : EFieldOperandType.IPV6}
              onClick={(newFilter) => {
                setFilterCondition((prev) => [...prev, newFilter]);
              }}
              style={{ width: 200 }}
              DrilldownMenu={
                <LinkMenu
                  MenuItemsGroup={[
                    {
                      label: '跳转到其他页',
                      key: 'jumpToOtherPage',
                      children: [{ label: 'IP画像', key: EIP_DRILLDOWN_MENU_KEY.IP_IMAGE }],
                    },
                  ]}
                  settings={{ imageIp: ipResponder }}
                />
              }
            />
          );
        },
      },
      {
        title: '目的IPv4',
        dataIndex: 'destIpv4',
        searchable: true,
        show: false,
        operandType: EFieldOperandType.IPV4,
        fieldType: EFieldType.IPV4,
      },
      {
        title: '目的IPv6',
        dataIndex: 'destIpv6',
        searchable: true,
        show: false,
        operandType: EFieldOperandType.IPV6,
        fieldType: EFieldType.IPV6,
      },
      {
        title: '目的端口',
        dataIndex: 'destPort',
        searchable: true,
        operandType: EFieldOperandType.PORT,
      },
      { title: '标签', dataIndex: 'tag', searchable: true, fieldType: EFieldType.ARRAY },
      {
        title: '基础标签',
        dataIndex: 'basicTag',
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        enumValue: basicTags?.map((tag) => ({ value: tag, text: tag })),
      },
      {
        title: '操作',
        key: 'option',
        dataIndex: 'operation',
        valueType: 'option',
        width: 200,
        fixed: 'right',
        render: (_, record) => {
          return (
            <Space>
              <Button
                type="link"
                size="small"
                onClick={() => {
                  history.push(getLinkUrl(`/analysis/trace/flow-record?flowId=${record.flowId}`));
                }}
              >
                会话详单
              </Button>
              <Button
                type="link"
                size="small"
                onClick={() => {
                  setSelectdRow(record);
                }}
              >
                详情
              </Button>
            </Space>
          );
        },
      },
    ];
  }, [basicTags, classtypeDict, mitreDict, sources]);

  // 组件卸载取消全部请求
  useCancelAllQuery();

  const showColumns = useMemo(() => {
    return columns
      .filter((col) => col.show !== false)
      .map((col) => {
        return {
          ...col,
          key: col.dataIndex as string,
          width: col.width || (col.title as string)?.length * 18 + 20,
          align: 'center' as any,
          sortOrder: sortProperty === col.dataIndex ? (`${sortDirection}end` as any) : false,
          ellipsis: col.ellipsis,
          render: (value: any, record: ISuricataAlertMessage, index: number) => {
            let label = value;
            if (col.render) {
              label = col.render(value, record, index);
            }

            if (!col.searchable) {
              return label;
            }

            if (col.fieldType === EFieldType.ARRAY) {
              const values: string[] = record[col.dataIndex as string];
              return values.map((item) => {
                let content: string = item;
                if (col.operandType === EFieldOperandType.ENUM) {
                  content =
                    col.enumValue?.find((enumItem) => enumItem.value === item)?.text || item;
                }
                return (
                  <FilterBubble
                    key={content}
                    dataIndex={snakeCase(col.dataIndex as string)}
                    label={<span className="table-cell-button">{content}</span>}
                    operand={item}
                    fieldType={col.fieldType}
                    operandType={col.operandType as EFieldOperandType}
                    onClick={(newFilter) => {
                      setFilterCondition((prev) => [...prev, newFilter]);
                    }}
                  />
                );
              });
            }

            return (
              <FilterBubble
                dataIndex={snakeCase(col.dataIndex as string)}
                label={label}
                operand={String(value)}
                fieldType={col.fieldType}
                operandType={col.operandType as EFieldOperandType}
                onClick={(newFilter) => {
                  setFilterCondition((prev) => [...prev, newFilter]);
                }}
                // 如果是sid，需要渲染一个下钻会话详单的按钮
                {...(col.dataIndex === 'sid'
                  ? {
                      DrilldownMenu: (
                        <Menu.ItemGroup>
                          <Menu.Item
                            key="flow-record"
                            onClick={() => {
                              const url = `/analysis/trace/flow-record?${stringify({
                                sid: record.sid,
                                from: new Date(selectedTime.current.originStartTime).getTime(),
                                to: new Date(selectedTime.current.originEndTime).getTime(),
                                timeType: ETimeType.CUSTOM,
                              })}`;
                              jumpNewPage(url);
                            }}
                          >
                            会话详单
                          </Menu.Item>
                        </Menu.ItemGroup>
                      ),
                    }
                  : {})}
              />
            );
          },
        };
      });
  }, [columns, selectedTime, sortDirection, sortProperty]);

  const handleClasstypeChange = (classtypeIds: string[]) => {
    setSelectedClasstypeIds(classtypeIds);
  };

  const handleMitreChange = (mitreIds: string[]) => {
    setSelectedMitreIds(mitreIds);
  };

  // dsl简化
  const processedMitreIds = useMemo(() => {
    const tree: Record<string, string[]> = {};

    attackList.forEach((item) => {
      if (!item.parentId) {
        // item 为根节点
        if (!tree[item.id]) {
          tree[item.id] = [];
        }
      } else {
        // item 为子节点，且父节点可能有多个
        const parentIds = item.parentId.split(',');

        parentIds.forEach((pid) => {
          if (!tree[pid]) {
            tree[pid] = [];
          }
          tree[pid].push(item.id);
        });
      }
    });

    const selectedTree: Record<string, string[]> = {};
    const tacticIds: string[] = [];

    (selectedMitreIds || []).forEach((id) => {
      if (mitreDict[id] && !mitreDict[id].parentId) {
        selectedTree[id] = [];
      } else {
        const [parentId, subId] = id.split('-');
        if (!selectedTree[parentId]) {
          selectedTree[parentId] = [];
        }
        selectedTree[parentId].push(subId);
      }
    });

    // console.log(tree, selectedTree, selectedMitreIds);

    Object.keys(selectedTree).forEach((parentId) => {
      if (
        selectedTree[parentId]?.length === tree[parentId]?.length ||
        selectedTree[parentId]?.length === 0
      ) {
        tacticIds.push(parentId);
        delete selectedTree[parentId];
        return;
      }
    });

    return {
      tacticIds,
      techniqueTree: selectedTree,
    };
  }, [attackList, mitreDict, selectedMitreIds]);

  const searchFields = useMemo<IField[]>(() => {
    return columns
      .filter((item) => {
        return item.searchable;
      })
      .map((item) => {
        return {
          type: item.fieldType,
          title: item.title as string,
          dataIndex: snakeCase(item.dataIndex as string),
          enumValue: item.enumValue || [],
          operandType: item.operandType,
        };
      });
  }, [columns]);

  const handleFilterChange = (newFilter: IFilterCondition) => {
    setFilterCondition(newFilter as IFilter[]);
  };

  const handleTableChange: TableProps<ISuricataAlertMessage>['onChange'] = (
    _pagination,
    _filters,
    sorter,
  ) => {
    if (!(sorter instanceof Array)) {
      if (sorter.field !== sortProperty) {
        setSortProperty(sorter.field as string);
        setSortDirection(ESortDirection.DESC);
      } else {
        setSortDirection(sorter.order === 'ascend' ? ESortDirection.ASC : ESortDirection.DESC);
      }
    }
  };

  // 设置页面加载状态
  useEffect(() => {
    if (!pageIsReady && selectedMitreIds !== undefined && selectedClasstypeIds !== undefined) {
      setPageIsReady(true);
    }
  }, [pageIsReady, selectedClasstypeIds, selectedMitreIds]);

  // dsl表达式构造
  const dsl = useMemo(() => {
    const { originStartTime: startTime, originEndTime: endTime } = globalSelectedTime;
    let nextDsl = filterCondition2Spl(filterCondition, searchFields);
    setPagination((prev) => {
      return {
        ...prev,
        currentPage: 0,
      };
    });

    const { tacticIds, techniqueTree } = processedMitreIds;

    // 时间及其他参数拼接
    if (selectedClasstypeIds && selectedClasstypeIds?.length > 0) {
      if (nextDsl) {
        nextDsl += ` AND `;
      }
      nextDsl += `classtype_id in (${selectedClasstypeIds.map((item) => `'${item}'`).join(',')})`;
    }

    let tacticSql = '';
    let techniqueSql = '';
    if (tacticIds?.length > 0) {
      tacticSql = `mitre_tactic_id in (${tacticIds.map((item) => `'${item}'`).join(',')})`;
    }

    if (Object.keys(techniqueTree)?.length > 0) {
      const sqls = Object.keys(techniqueTree).map((parentId) => {
        if (techniqueTree[parentId]?.length > 0) {
          const str = `mitre_tactic_id = ${parentId} AND mitre_technique_id in (${techniqueTree[
            parentId
          ]
            .map((item) => `'${item}'`)
            .join(',')})`;
          return `(${str})`;
        } else {
          return `(mitre_tactic_id = ${parentId})`;
        }
      });

      techniqueSql = `(${sqls.join(' OR ')})`;
    }

    const mitreSql = `${tacticSql} ${techniqueSql && tacticSql ? 'OR' : ''} ${techniqueSql}`;
    if (mitreSql.trim()) {
      if (nextDsl) {
        nextDsl += ' AND ';
      }

      nextDsl += `(${mitreSql})`;
    }

    nextDsl += ` | gentimes timestamp start="${startTime}" end="${endTime}"`;
    return nextDsl;
  }, [globalSelectedTime, filterCondition, searchFields, processedMitreIds, selectedClasstypeIds]);

  // 表格时间的请求参数构造
  const queryParams = useMemo(() => {
    return {
      sortProperty,
      sortDirection,
      dsl,
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
    };
  }, [dsl, pagination.currentPage, pagination.pageSize, sortDirection, sortProperty]);

  const { run: queryData } = useDebounceFn(
    () => {
      setTableLoading(true);
      queryAlertMessageList({ ...queryParams }).then((res) => {
        const { result, success } = res;
        if (success) {
          const { content, totalElements } = result;
          setTableData(content);
          setPagination((prev) => {
            return {
              ...prev,
              total: totalElements,
            };
          });
        } else {
          message.error('获取失败');
        }

        setTableLoading(false);
      });
    },
    { wait: 200 },
  );

  useEffect(() => {
    if (pageIsReady) {
      queryData();
    }

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [queryParams, pageIsReady]);

  useEffect(() => {
    querySuricataSource().then(({ success, result }) => {
      if (success) {
        setSources(result);
      }
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handlePageChange = (current: number, pageSize: number) => {
    setPagination((prev) => {
      return {
        ...prev,
        currentPage: current - 1,
        pageSize,
      };
    });
  };

  return (
    <Row gutter={[10, 10]}>
      <Col span={4} className={styles.sideTree}>
        <Space direction="vertical" className={styles.space}>
          <MitreAttackTree onChange={handleMitreChange} className={styles.mitreAttack} />
          <ClasstypeList onChange={handleClasstypeChange} className={styles.classtype} />
        </Space>
      </Col>
      <Col span={20}>
        <EnhancedTable<ISuricataAlertMessage>
          tableKey="suricata-alarm-table"
          columns={showColumns}
          onChange={handleTableChange}
          autoHeight={true}
          dataSource={tableData}
          loading={tableLoading || !pageIsReady}
          pagination={false}
          rowKey={() => uuidv1()}
          extraTool={
            <FieldFilter
              fields={searchFields}
              historyStorageKey="suricata-alarm-filter"
              condition={filterCondition}
              onChange={handleFilterChange}
              extra={
                <Space>
                  <Button onClick={queryData}>查询</Button>
                  <DownLoadPktBtn
                    queryFn={(params) => {
                      return queryAlertPackets({
                        ...params,
                        dsl,
                        startTime: globalSelectedTime.originStartTime,
                        endTime: globalSelectedTime.originEndTime,
                        sortDirection,
                        sortProperty,
                      });
                    }}
                    totalPkt={pagination.total}
                    loading={tableLoading || !pageIsReady}
                  />
                  <ExportFile
                    loading={tableLoading}
                    totalNum={pagination.total}
                    queryFn={async (params: any) => {
                      window.open(
                        `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/suricata/alert-messages/as-export?${qs.stringify(
                          {
                            dsl,
                            ...params,
                          },
                        )}`,
                      );
                    }}
                  />
                </Space>
              }
            />
          }
          extraFooter={
            <CustomPagination
              onChange={handlePageChange}
              currentPage={pagination.currentPage + 1}
              pageSize={pagination.pageSize}
              total={pagination.total}
            />
          }
        />
      </Col>
      <Modal
        width={1500}
        destroyOnClose
        title="告警详情"
        onCancel={() => setSelectdRow(undefined)}
        visible={!!selectdRow}
        footer={null}
      >
        {selectdRow && <Detail alert={selectdRow} />}
      </Modal>
    </Row>
  );
};

export default AlertMessage;
