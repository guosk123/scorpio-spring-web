import { getTablePaginationDefaultSettings } from '@/common/app';
import application from '@/common/applicationConfig';
import Import from '@/components/Import';
import type { ConnectState } from '@/models/connect';
import { ExportOutlined, PlusOutlined, RollbackOutlined, SearchOutlined } from '@ant-design/icons';
import {
  Button,
  Checkbox,
  Divider,
  Form,
  Input,
  message,
  Popconfirm,
  Popover,
  Space,
  Table,
} from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import { connect } from 'dva';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { history } from 'umi';
import { deleteCustomCountry } from './service';
import type { ICity, ICountry, ICustomCountry, IProvince } from './typings';

const { API_BASE_URL, API_VERSION_PRODUCT_V1 } = application;
const searchData = (data: any[], keyword: string): any => {
  const res: any[] = [];
  if (data.length === 0) {
    return res;
  }
  data.forEach((row) => {
    // 先判断父节点是否命中
    // 父节点命中就跳过子节点
    if (
      row.name.toLocaleLowerCase().includes(keyword.toLocaleLowerCase()) ||
      row.nameText?.toLocaleLowerCase().includes(keyword.toLocaleLowerCase())
    ) {
      res.push({ ...row });
    } else {
      // 父节点未命中，再判断子节点
      const cb = searchData(row.children || [], keyword);
      if (cb.length > 0) {
        res.push({ ...row, children: cb });
      }
    }
  });

  return res;
};

// 判断是不是自定义地区
export const isCustomGeo = (countryId: string) => {
  if (parseInt(countryId, 10) >= 300 && parseInt(countryId, 10) <= 499) {
    return true;
  }
  return false;
};

interface IGeoLocationProps {
  dispatch: Dispatch;
  allCountryList: ICountry[];
  allCustomCountryList: ICustomCountry[];
  queryLoading: boolean | undefined;
}

const GeoLocation: React.FC<IGeoLocationProps> = ({
  dispatch,
  allCustomCountryList,
  allCountryList,
  queryLoading,
}) => {
  // form
  const formRef = useRef<any>();
  // 模糊搜索关键字
  const [keyword, setKeyword] = useState<string>();

  // 是否显示自定义
  const [showCustom, setShowCustom] = useState<boolean>(false);

  // 进入页面需要刷新数据
  useEffect(() => {
    dispatch({ type: 'geolocationModel/queryGeolocations' });
  }, [dispatch]);

  // 展示的数据
  const displayData = useMemo(() => {
    if (showCustom) {
      if (!keyword) {
        return allCustomCountryList;
      }
      return searchData(allCustomCountryList, keyword);
    }
    if (!keyword) {
      return [...allCountryList];
    }
    return searchData([...allCountryList], keyword);
  }, [keyword, allCustomCountryList, allCountryList, showCustom]);

  // 自定义的地区数量
  const customGeoNumber = useMemo(() => {
    return allCustomCountryList.length;
  }, [allCustomCountryList]);

  // 更新处理函数
  const handleUpdate = (record: ICountry | ICustomCountry | IProvince | ICity) => {
    // 自定义地区
    if (isCustomGeo(record.countryId)) {
      history.push(`/configuration/objects/geolocation/update?id=${record.id}`);
      return;
    }
    // 非叶子结点不能编辑
    if (record.children) {
      return;
    }

    history.push(
      `/configuration/objects/geolocation/update?cityId=${
        (record as ICity).cityId || ''
      }&provinceId=${(record as IProvince).provinceId || ''}&countryId=${record.countryId}`,
    );
  };

  // 创建处理函数
  const handleCreate = () => {
    if (customGeoNumber <= 200) {
      history.push('/configuration/objects/geolocation/create');
    }
  };

  // 删除处理函数
  const handleDelete = (record: ICustomCountry) => {
    // 判断下是否是自定义地区
    if (!isCustomGeo(record.countryId)) {
      message.info('只有自定义地区才可以删除');
      return;
    }
    deleteCustomCountry(record.id).then((res) => {
      if (res.success) {
        message.success('删除成功');
        dispatch({ type: 'geolocationModel/queryGeolocations' });
      }
    });
  };

  // 导出处理函数
  const handleExport = () => {
    const url = `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/appliance/geolocation/as-export`;
    window.location.href = url;
  };

  // 修改查询关键字
  const queryGeoList = (values: Record<string, any>) => {
    setKeyword(values.areaname);
  };

  const tableColumns: ColumnProps<ICountry>[] = [
    // {
    //   title: 'ID',
    //   dataIndex: 'id',
    //   key: 'id',
    //   align: 'center',
    //   width: 200,
    //   render: (text, record) =>
    //     (record as ICity).cityId || (record as ICity).provinceId || (record as ICity).countryId,
    // },
    {
      title: '名称',
      dataIndex: 'nameText',
      key: 'nameText',
      width: '15%',
      render: (_, record) => {
        return isCustomGeo(record.countryId) ? record.name : record.nameText;
      },
    },
    {
      title: '国家代码',
      dataIndex: 'countryCode',
      key: 'countryCode',
      width: '10%',
      render: (_, record) => record.countryCode || '',
    },
    {
      title: '经纬度',
      dataIndex: 'longitude&latitude',
      key: 'longitude&latitude',
      width: '10%',
      render: (_, record) => {
        return `[${record.longitude}, ${record.latitude}]`;
      },
    },
    {
      title: '描述',
      dataIndex: 'fullName',
      key: 'fullName',
      render: (_, record) => {
        return isCustomGeo(record.countryId) ? record.description : record.fullName;
      },
    },
    {
      title: 'IP',
      dataIndex: 'ipAddress',
      key: 'ipAddress',
      width: '20%',
      render: (_, record) => {
        if (record.ipAddress && record.ipAddress.length >= 40) {
          return (
            <>
              <Popover
                content={
                  <>
                    <ul style={{ padding: 0 }}>
                      {record.ipAddress.split(',').map((ipAddress: string) => {
                        return <li key={ipAddress}>{ipAddress}</li>;
                      })}
                    </ul>
                  </>
                }
                title="IP地址"
                trigger="hover"
              >
                <span style={{ padding: 0 }}>{record.ipAddress?.slice(0, 39)}...</span>
              </Popover>
            </>
          );
        }
        return record.ipAddress;
      },
    },
    {
      title: '操作',
      dataIndex: 'operaton',
      width: 200,
      align: 'center',
      render: (_, record) => {
        return (
          <>
            <Button
              style={{ padding: '0px' }}
              type="link"
              onClick={() => {
                handleUpdate(record);
              }}
              disabled={record.children !== undefined}
            >
              编辑
            </Button>
            <Divider type="vertical" />
            <Popconfirm
              key={`${record.countryId}__${record.id}`}
              title="是否确定删除此地区？"
              onConfirm={() => {
                handleDelete(record);
              }}
              disabled={!isCustomGeo(record.countryId)}
            >
              <Button
                style={{ padding: '0px' }}
                type="link"
                danger
                disabled={!isCustomGeo(record.countryId)}
              >
                删除
              </Button>
            </Popconfirm>
          </>
        );
      },
    },
  ];

  return (
    <>
      <div style={{ margin: 10, textAlign: 'right' }}>
        <Space align="baseline">
          <Form
            ref={formRef}
            layout="inline"
            style={{ display: 'flex', justifyContent: 'flex-Send' }}
            onFinish={queryGeoList}
          >
            <Form.Item label="地区名称" name="areaname">
              <Input style={{ width: 300 }} placeholder="地区名称" />
            </Form.Item>

            <Form.Item name="custom" valuePropName="checked">
              <Checkbox
                checked={showCustom}
                onChange={(e) => {
                  setShowCustom(e.target.checked);
                }}
              >
                只看自定义地区
              </Checkbox>
            </Form.Item>
            <Form.Item>
              <Space>
                <Button icon={<SearchOutlined />} type="primary" htmlType="submit">
                  查询
                </Button>
                <Button
                  icon={<RollbackOutlined />}
                  onClick={() => {
                    formRef.current.setFieldsValue({
                      areaname: '',
                    });
                    setKeyword('');
                  }}
                >
                  重置
                </Button>
                <Divider type="vertical" />
                <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={handleCreate}
                  disabled={customGeoNumber > 200}
                >
                  新建自定义地区
                </Button>
                <Import
                  loading={false}
                  modalTitle={`导入`}
                  importFunc="geolocationModel/importCustomGeo"
                  tempDownloadUrl={`/appliance/geolocation/as-template`}
                  importSuccessCallback={() => {}}
                />

                <Button
                  onClick={() => {
                    handleExport();
                  }}
                >
                  <ExportOutlined /> 导出
                </Button>
              </Space>
            </Form.Item>
          </Form>
        </Space>
      </div>
      <Table
        rowKey="id"
        size="small"
        bordered
        loading={queryLoading}
        columns={tableColumns}
        dataSource={displayData}
        pagination={getTablePaginationDefaultSettings()}
        scroll={{ scrollToFirstRowOnChange: true }}
      />
    </>
  );
};

export default connect(
  ({
    loading: { effects },
    geolocationModel: { allCustomCountryList, allCountryList },
  }: ConnectState) => ({
    allCountryList,
    allCustomCountryList,
    queryLoading: effects['geolocationModel/queryGeolocations'],
  }),
)(GeoLocation);
