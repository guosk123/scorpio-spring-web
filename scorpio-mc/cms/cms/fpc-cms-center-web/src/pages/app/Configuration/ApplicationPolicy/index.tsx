import DragSortTable, { DragHandle } from '@/components/DragSortTable';
import Import from '@/components/Import';
import type { ConnectState } from '@/models/connect';
import { ExportOutlined, PlusOutlined } from '@ant-design/icons';
import type { ProColumns } from '@ant-design/pro-table';
import { Button, Card, Checkbox, Dropdown, Menu, message, Popconfirm, Popover, Tree } from 'antd';
import { useCallback, useEffect, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect } from 'umi';
import type {
  INetworkGroup,
  INetworkGroupMap,
  INetworkSensor,
  INetworkSensorMap,
} from '../Network/typings';
import type { AppCategoryItem } from '../SAKnowledge/typings';
import ApplicationPolicyModal from './components/ApplicationPolicyModal';
import { DEFAULT_POLICY_ID } from './components/PolicyForm';
import { IMoveAppPoliciesParams, importFilterRules } from './service';
import { updateApplicationPolicy } from './service';
import { createApplicationPolicy } from './service';
import { exportApplicationPolicy } from './service';
import {
  alterApplicationPolicyStates,
  deleteApplicationPolicy,
  moveApplicationPolicies,
  queryApplicationPolicies,
} from './service';
import type { IApplicationPolicy } from './typings';
import { EApplicationPolicyAction } from './typings';
import { EApplicationPolicyState } from './typings';
import { convertComplexAppIdToSimple, getApplicationTree } from './utils/appTree';

enum EMoveDirection {
  MOVE_TOP = 'move_top',
  MOVE_BOTTOM = 'move_bottom',
  MOVE_UP = 'move_up',
  MOVE_DOWN = 'move_down',
  MOVE_PAGE = 'move_page',
}

export enum ESubmitType {
  CREATE = 'create',
  COPY = 'copy',
  EDIT = 'edit',
  INSERT = 'insert',
}

function ApplicationPolicy({
  allCategoryList,
  dispatch,
  allNetworkSensor,
  allNetworkGroup,
  allNetworkSensorMap,
  allNetworkGroupMap,
}: {
  allCategoryList: AppCategoryItem[];
  dispatch: Dispatch;
  /* 探针网络 */
  allNetworkSensor: INetworkSensor[];
  /** 探针网络 Map */
  allNetworkSensorMap: INetworkSensorMap;
  /** 网络组 */
  allNetworkGroup: INetworkGroup[];
  /** 网络组 Map */
  allNetworkGroupMap: INetworkGroupMap;
}) {
  /** cms链接信息 */
  // const [cmsConnectFlag, setCmsConnectFlag] = useState(false);

  /** 原数据 */
  const [originData, setOriginData] = useState<IApplicationPolicy[]>([]);

  /** 表格数据 */
  const [tableData, setTableData] = useState<IApplicationPolicy[]>([]);

  /** 缓存数据 */
  const [tableCacheData, setTableCatchData] = useState<IApplicationPolicy[]>([]);

  /** 处理勾选 */
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

  const onSelectChange = (newSelectedRowKeys: React.Key[]) => {
    setSelectedRowKeys(newSelectedRowKeys.filter((k) => k !== DEFAULT_POLICY_ID));
  };
  /** 缓存勾选 */
  const [selectedCacheKeys, setSelectedCacheKeys] = useState<React.Key[]>([]);

  /** 缓存列表变动回调 */
  const onCacheChange = (newSelectedRowKeys: React.Key[]) => {
    setSelectedCacheKeys(newSelectedRowKeys);
  };

  /** 分页信息 */
  const [pageData, setPageData] = useState<{ page: number; pageSize: number }>();

  /** 总行数 */
  const [totalElements, setTotalElements] = useState<number>(0);

  /** 创建Modal */
  const [modalVisiable, setModalvisiable] = useState<boolean>(false);

  /** 加载数据 */
  const fetchData = useCallback(
    async (exceptData?: IApplicationPolicy[]) => {
      if (pageData) {
        const { success, result } = await queryApplicationPolicies({
          ...pageData,
        });
        if (success) {
          setTotalElements(result?.totalElements);
          const newContent = result?.content.map((item) => {
            return {
              ...item,
              ...(JSON.parse((item as any).tuple || '[]')[0] || {}),
              disabled: (exceptData || tableCacheData).findIndex((d) => d.id === item.id) >= 0,
            };
          }) as IApplicationPolicy[];
          const list = newContent
            .filter((c) => c.disabled === false)
            .map((item) => {
              return {
                ...item,
                ...(JSON.parse((item as any).tuple || '[]')[0] || {}),
              };
            }) as IApplicationPolicy[];
          console.log(newContent);
          setOriginData(newContent);
          setTableData(list);
          return list;
        }
      }
      return [];
    },
    [pageData],
  );

  /** Modal状态 */
  const [modalState, setModalState] = useState<ESubmitType>(ESubmitType.CREATE);

  /** Modal初始化值，在拷贝，编辑，的时候使用 */
  const [modalInitialValue, setModalInitialValue] = useState<Record<string, any> | null>(null);

  /** 进入页面初始化数据 */
  useEffect(() => {
    fetchData();
  }, [fetchData]);

  /** 当前编辑id */
  const [currentEditId, setCurrentEditId] = useState<string>('');

  /** 根据tableData修改selectedRowKeys */
  useEffect(() => {
    // 过滤掉已经移出去的行
    setSelectedRowKeys(selectedRowKeys.filter((k) => tableData.findIndex((d) => d.id === k) >= 0));
  }, [tableData]);

  /** 是否可以拖拽开关 */
  const shouldRowDragged = (draggedData: IApplicationPolicy) => {
    return draggedData?.id !== DEFAULT_POLICY_ID;
  };

  /** 和后端同步移动 */
  const syncMove = async (params: IMoveAppPoliciesParams) => {
    const { success } = await moveApplicationPolicies(params);

    if (success) {
      message.success('移动成功');
      return Promise.resolve(params);
    } else {
      message.error('移动失败!');
      return Promise.reject(params);
    }
  };

  /** 处理批量移动 */
  const handleBatchRowsMove = (key?: string, newData?: IApplicationPolicy[]) => {
    let idList = [] as string[];
    if (key) {
      idList = selectedRowKeys as string[];
      return syncMove({
        idList: idList.join(','),
        operator: key,
        page: pageData?.page || 0,
        pageSize: pageData?.pageSize || 0,
      });
    } else {
      idList = (newData || tableData).map((d) => d.id);
      return syncMove({
        idList: idList.join(','),
        page: pageData?.page || 0,
        pageSize: pageData?.pageSize || 0,
      });
    }
  };

  /** 修改状态 */
  const alterPolicyState = async (idList: string, state: EApplicationPolicyState) => {
    const { success } = await alterApplicationPolicyStates({ idList, state });
    if (success) {
      return Promise.resolve({ success, idList });
    }
    return Promise.reject({ success, idList });
  };

  /** 批量启用 */
  const batchAlterState = (state: EApplicationPolicyState) => {
    alterPolicyState(selectedRowKeys.join(','), state).then(() => {
      setTableData(
        tableData.map((item) => {
          if (selectedRowKeys.findIndex((k) => k === item.id) >= 0) {
            return {
              ...item,
              state: state,
            };
          }
          return item;
        }),
      );
    });
  };

  /** 导出 */
  const handleExport = () => {
    exportApplicationPolicy();
  };

  /** 操作modal */
  const operateModal = (open: boolean, refresh?: boolean, mode?: ESubmitType) => {
    if (open === true) {
      if (mode) {
        setModalState(mode);
      }
      setModalvisiable(true);
    } else {
      setModalInitialValue(null);
      setModalState(ESubmitType.CREATE);
      if (refresh) {
        fetchData();
      }
      setCurrentEditId('');
      setModalvisiable(false);
    }
  };

  /** 表格列定义 */
  const columns: ProColumns<IApplicationPolicy>[] = [
    {
      title: '#',
      align: 'center',
      dataIndex: 'index',
      width: 60,
      fixed: 'left',
      search: false,
      render: (text, record, index) => {
        return (
          <>
            {(pageData?.page || 0) * (pageData?.pageSize || 0) + index + 1}
            &nbsp;&nbsp;
            {record.id !== DEFAULT_POLICY_ID ? (
              <DragHandle />
            ) : (
              <span style={{ width: '15px', display: 'inline-block' }} />
            )}
          </>
        );
      },
    },
    {
      title: '名称',
      align: 'center',
      dataIndex: 'name',
      search: false,
    },
    {
      title: '源IP',
      align: 'center',
      dataIndex: 'sourceIp',
      search: false,
    },
    {
      title: '源端口',
      align: 'center',
      dataIndex: 'sourcePort',
      search: false,
    },
    {
      title: '目的IP',
      align: 'center',
      dataIndex: 'destIp',
      search: false,
    },
    {
      title: '目的端口',
      align: 'center',
      dataIndex: 'destPort',
      search: false,
    },
    {
      title: '传输层协议',
      align: 'center',
      dataIndex: 'protocol',
      search: false,
    },
    {
      title: 'VLANID',
      align: 'center',
      dataIndex: 'vlanId',
      search: false,
    },
    {
      title: '应用',
      align: 'center',
      dataIndex: 'applicationId',
      search: false,
      render: (text, record) => {
        const simpleAppIdInfo = convertComplexAppIdToSimple(record.applicationId);
        return (
          <Popover
            placement="bottom"
            content={
              <>
                <Tree
                  checkable
                  disabled={true}
                  defaultSelectedKeys={simpleAppIdInfo}
                  defaultCheckedKeys={simpleAppIdInfo}
                  treeData={getApplicationTree(allCategoryList)}
                />
              </>
            }
            trigger="click"
          >
            <Button size="small" type="link">
              查看
            </Button>
          </Popover>
        );
      },
    },
    {
      title: '生效网络',
      align: 'center',
      dataIndex: 'networkId',
      search: false,
      render: (text, record) => {
        if (record?.id === DEFAULT_POLICY_ID) {
          return 'ALL';
        }
        const list: string[] = [];
        const { networkId, networkGroupId } = record;
        networkId?.split(',').forEach((id) => {
          list.push(allNetworkSensorMap[id]?.name || id);
        });
        networkGroupId?.split(',').forEach((id) => {
          list.push(allNetworkGroupMap[id]?.name || id);
        });
        return list.filter((f) => f).join(',');
      },
    },
    {
      title: '动作',
      align: 'center',
      dataIndex: 'action',
      search: false,
      render: (text, record) => {
        if (record.action === EApplicationPolicyAction.DROP) {
          return '不存储';
        } else if (record.action === EApplicationPolicyAction.STORE) {
          return '存储';
        } else if (record.action === EApplicationPolicyAction.TRUNCATE) {
          return `截断存储，截断长度: ${record.truncLen}`;
        }
        return '';
      },
    },
    {
      title: '启用',
      align: 'center',
      dataIndex: 'state',
      search: false,
      render: (text, record) => {
        return (
          <Checkbox
            defaultChecked={record.state === EApplicationPolicyState.Enable}
            disabled={record.id === DEFAULT_POLICY_ID || record.disabled}
            onChange={(e) => {
              alterPolicyState(
                record?.id,
                e.target.checked ? EApplicationPolicyState.Enable : EApplicationPolicyState.Disable,
              )
                .then(() => {
                  message.success('修改状态成功!');
                })
                .catch(() => {
                  message.error('修改状态失败!');
                  setTimeout(() => {
                    /** 复原checkbox */
                    const index = tableData.findIndex((d) => d.id === record?.id);
                    if (index >= 0) {
                      const list = [...tableData];
                      list[index] = {
                        ...list[index],
                        state:
                          e.target.checked === true
                            ? EApplicationPolicyState.Disable
                            : EApplicationPolicyState.Enable,
                      };
                      setTableData(list);
                    }
                  });
                });
            }}
          />
        );
      },
    },
    {
      title: '操作',
      align: 'center',
      search: false,
      render: (text, record) => {
        return (
          <>
            <Button
              type="link"
              size="small"
              disabled={record.disabled}
              onClick={() => {
                // 查找数据并解析,设置初始化值
                setModalInitialValue(tableData.find((d) => d.id === record.id) || {});
                setCurrentEditId(record?.id || '');
                operateModal(true, false, ESubmitType.EDIT);
              }}
            >
              编辑
            </Button>
          </>
        );
      },
    },
  ];

  /** 操作栏渲染 */
  const operationLineRender = () => {
    return (
      <div>
        <Button
          type="link"
          size="small"
          disabled={selectedRowKeys.length > 1}
          onClick={() => {
            if (selectedRowKeys.length === 1) {
              // 查找数据并解析,设置初始化值
              setModalInitialValue(tableData.find((d) => d.id === selectedRowKeys[0]) || {});
              operateModal(true, false, ESubmitType.COPY);
            }
          }}
        >
          复制
        </Button>
        <Dropdown
          overlay={
            <Menu
              onClick={({ key }: { key: string }) => {
                if (key === EMoveDirection.MOVE_PAGE) {
                  const newCacheData = [
                    ...tableCacheData,
                    ...selectedRowKeys.map((k) => {
                      return {
                        ...(tableData.find((d) => d.id === k) as any),
                        disabled: true,
                      };
                    }),
                  ];
                  setTableCatchData(newCacheData);
                  fetchData(newCacheData);
                } else {
                  handleBatchRowsMove(key).then(() => {
                    // 重新获取数据
                    fetchData();
                  });
                }
              }}
            >
              <Menu.Item key={EMoveDirection.MOVE_TOP}>移动到顶部</Menu.Item>
              <Menu.Item
                key={EMoveDirection.MOVE_UP}
                disabled={
                  selectedRowKeys.length > 1 ||
                  (selectedRowKeys[0] === tableData[0]?.id && pageData?.page === 0)
                }
              >
                上移
              </Menu.Item>
              <Menu.Item
                key={EMoveDirection.MOVE_DOWN}
                disabled={(() => {
                  if (selectedRowKeys.length !== 1) {
                    return true;
                  }
                  if (
                    tableData[(tableData.findIndex((d) => d.id === selectedRowKeys[0]) || 0) + 1]
                      ?.id === DEFAULT_POLICY_ID
                  ) {
                    return true;
                  }
                  return false;
                })()}
              >
                下移
              </Menu.Item>
              <Menu.Item key={EMoveDirection.MOVE_PAGE}>跨页移动</Menu.Item>
              <Menu.Item key={EMoveDirection.MOVE_BOTTOM}>移动到底部</Menu.Item>
            </Menu>
          }
        >
          <Button type="link" size="small">
            移动
          </Button>
        </Dropdown>
        <Popconfirm
          title="是否确定删除？"
          onConfirm={() => {
            deleteApplicationPolicy(selectedRowKeys.join(','))
              .then(() => {
                message.success('删除成功!');
                fetchData();
              })
              .catch(() => {
                message.error('删除成功!');
              });
          }}
        >
          <Button type="link" size="small">
            删除
          </Button>
        </Popconfirm>
        <Button
          type="link"
          size="small"
          disabled={selectedRowKeys.length > 1}
          onClick={() => {
            operateModal(true, false, ESubmitType.INSERT);
          }}
        >
          插入
        </Button>
        <Button
          type="link"
          size="small"
          onClick={() => {
            batchAlterState(EApplicationPolicyState.Enable);
          }}
        >
          启用
        </Button>
        <Button
          type="link"
          size="small"
          onClick={() => {
            batchAlterState(EApplicationPolicyState.Disable);
          }}
        >
          禁用
        </Button>
        <Button
          type="link"
          size="small"
          onClick={() => {
            setSelectedRowKeys([]);
          }}
        >
          取消选择
        </Button>
      </div>
    );
  };

  /** 创建policy */
  const handleSubmitPolicy = async (type: ESubmitType, params: any) => {
    if (type === ESubmitType.CREATE) {
      const { success } = await createApplicationPolicy(params);
      if (success) {
        message.success('创建成功!');
        operateModal(false, true);
        return;
      }
      return message.error('创建失败!');
    } else if (type === ESubmitType.COPY) {
      // 处理拷贝
      if (selectedRowKeys.length === 1) {
        const before = selectedRowKeys[0] as string;
        const { success } = await createApplicationPolicy(params, before);
        if (success) {
          message.success('拷贝成功!');
          operateModal(false, true);
          return;
        }
        return message.error('拷贝失败!');
      }
    } else if (type === ESubmitType.INSERT) {
      // 处理插入
      const before = selectedRowKeys[0] as string;
      const { success } = await createApplicationPolicy(params, before);
      if (success) {
        message.success('插入成功!');
        operateModal(false, true);
        return;
      }
      return message.error('插入失败!');
    } else if (type === ESubmitType.EDIT) {
      //处理编辑
      const { success } = await updateApplicationPolicy({
        ...params,
        id: currentEditId,
      });
      if (success) {
        message.success('编辑成功!');
        operateModal(false, true);
        return;
      }
      return message.error('插入失败!');
    }
  };

  /** 更新网络数据 */
  useEffect(() => {
    dispatch({
      type: 'saKnowledgeModel/queryAllApplications',
    });
    dispatch({
      type: 'networkModel/queryNetworkGroupTree',
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  /** 渲染缓存小表 */
  const renderCacheTable = () => {
    const cacheColumn = [...columns];
    cacheColumn.shift();
    cacheColumn.pop();
    return (
      <>
        {tableCacheData.length > 0 ? (
          <Card title="跨页移动" size="small">
            <DragSortTable<IApplicationPolicy>
              columns={cacheColumn}
              data={tableCacheData}
              setData={setTableCatchData}
              shouldRowDragged={shouldRowDragged}
              optionRender={false}
              pagination={false}
              rowSelection={{
                selectedRowKeys: selectedCacheKeys,
                onChange: onCacheChange,
              }}
              tableAlertOptionRender={() => {
                const menu = (
                  <Menu
                    style={{
                      maxHeight: '200px',
                      maxWidth: '200px',
                      overflowX: 'hidden',
                      overflowY: 'auto',
                    }}
                    onClick={({ key }: { key: string }) => {
                      const index = originData.findIndex((d) => d.id === key);
                      if (index >= 0) {
                        const list = originData.map((d) => d.id);
                        list.splice(index, 0, ...(selectedCacheKeys as string[]));
                        moveApplicationPolicies({
                          idList: list.join(','),
                          operator: EMoveDirection.MOVE_PAGE,
                          page: pageData?.page || 0,
                          pageSize: pageData?.pageSize || 10,
                        })
                          .then(() => {
                            message.success('插入成功！');
                            const newTableCache = tableCacheData.filter(
                              (d) => selectedCacheKeys.findIndex((k) => k === d.id) < 0,
                            );
                            setSelectedCacheKeys([]);
                            setTableCatchData(newTableCache);
                            setTimeout(() => {
                              fetchData(newTableCache);
                            });
                          })
                          .catch(() => {
                            message.error('插入失败! ');
                          });
                      }
                    }}
                  >
                    {originData.map((data) => {
                      return (
                        <Menu.Item key={data.id} disabled={data.disabled}>
                          {data.name}
                        </Menu.Item>
                      );
                    })}
                  </Menu>
                );

                // 判断当前选中是否有当页元素
                let hasCurrentPage = false;
                selectedCacheKeys.forEach((k) => {
                  if (!hasCurrentPage) {
                    hasCurrentPage = originData.findIndex((d) => d.id === k) >= 0;
                  }
                });

                return (
                  <div>
                    <Dropdown overlay={menu} disabled={hasCurrentPage}>
                      <Button type="link" size="small">
                        插入
                      </Button>
                    </Dropdown>
                    <Button
                      type="link"
                      size="small"
                      onClick={() => {
                        const newTableCache = tableCacheData.filter(
                          (d) => selectedCacheKeys.findIndex((k) => k === d.id) < 0,
                        );
                        setSelectedCacheKeys([]);
                        setTableCatchData(newTableCache);
                        setTimeout(() => {
                          fetchData(newTableCache);
                        });
                      }}
                    >
                      还原
                    </Button>
                  </div>
                );
              }}
            />
          </Card>
        ) : (
          ''
        )}
      </>
    );
  };

  return (
    <>
      {/* <ConnectCmsState onConnectFlag={setCmsConnectFlag} /> */}
      <DragSortTable<IApplicationPolicy>
        columns={columns}
        data={tableData}
        setData={setTableData}
        shouldRowDragged={shouldRowDragged}
        optionRender={() => [
          <Import
            loading={false}
            modalTitle={`导入`}
            customImportFunc={async (formData, handleCloseModal) => {
              const { success } = await importFilterRules(formData);
              if (success) {
                message.success('上传成功!');
                if (handleCloseModal) {
                  handleCloseModal();
                }
                fetchData();
                return;
              }
              message.error('上传失败!');
            }}
            tempDownloadUrl={`/appliance/filter-rules/as-template`} importFunc={''} />,
          <Button
            type="primary"
            icon={<PlusOutlined />}
            // disabled={cmsConnectFlag}
            onClick={() => {
              operateModal(true, false, ESubmitType.CREATE);
            }}
          >
            新建
          </Button>,
          <Button type="primary" icon={<ExportOutlined />} onClick={handleExport}>
            导出
          </Button>,
        ]}
        rowSelection={{
          selectedRowKeys,
          onChange: onSelectChange,
          renderCell: function (checked, record, index, originNode) {
            if (record.id !== DEFAULT_POLICY_ID) {
              return originNode;
            }
            return <Checkbox disabled={true} />;
          },
        }}
        tableAlertOptionRender={operationLineRender}
        onGragEnd={(oldIndex, newIndex, newData, oldData) => {
          handleBatchRowsMove(undefined, newData).catch(() => {
            if (oldData) {
              setTableData(oldData);
            }
          });
        }}
        totalElements={totalElements}
        setPages={setPageData}
        tableExtraRender={renderCacheTable}
      />
      <ApplicationPolicyModal
        title={`${(() => {
          if (modalState === ESubmitType.CREATE) {
            return '创建存储过滤策略';
          } else if (modalState === ESubmitType.COPY) {
            return '拷贝存储过滤策略';
          } else if (modalState === ESubmitType.INSERT) {
            return '插入存储过滤策略';
          } else if (modalState === ESubmitType.EDIT) {
            return '编辑存储过滤策略';
          }
          return '';
        })()}`}
        allCategoryList={allCategoryList}
        allNetworkSensor={allNetworkSensor}
        allNetworkGroup={allNetworkGroup}
        visiable={modalVisiable}
        closeFunc={() => {
          operateModal(false);
        }}
        handleSubmit={(params) => {
          handleSubmitPolicy(modalState, params);
        }}
        initialValues={modalInitialValue}
      />
    </>
  );
}

export default connect(
  ({
    SAKnowledgeModel: { allCategoryList },
    networkModel: { allNetworkSensor, allNetworkSensorMap, allNetworkGroup, allNetworkGroupMap },
  }: ConnectState) => {
    return {
      allCategoryList,
      allNetworkSensor,
      allNetworkSensorMap,
      allNetworkGroup,
      allNetworkGroupMap,
    };
  },
)(ApplicationPolicy);
