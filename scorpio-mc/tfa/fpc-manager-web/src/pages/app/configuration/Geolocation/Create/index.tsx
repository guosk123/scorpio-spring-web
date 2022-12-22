import { connect, history } from 'umi';
import type { ConnectState } from '@/models/connect';
import type { Dispatch } from 'umi';
import { useMemo } from 'react';
import type {
  ICountryMap,
  ICityMap,
  IProvinceMap,
} from '@/pages/app/configuration/Geolocation/typings';
import { Form, Input, Row, Col, Space, Button, Modal, notification, message } from 'antd';
import { ip2number, ipV4Regex, ipV6Regex, ipv4MaskRegex, getLinkUrl } from '@/utils/utils';
import { useState, useEffect, useRef } from 'react';
import WorldMap from '@/components/WorldMap';
import type { ICreateCustomCountry } from '../service';
import {
  createCustomCountry,
  updateIpAddress,
  updateCustomCountry,
  queryCustomGeoById,
} from '../service';
import type {
  ICity,
  IProvince,
  ICountry,
  ICustomCountry,
} from '@/pages/app/configuration/Geolocation/typings';
import { ExclamationCircleOutlined } from '@ant-design/icons';
import { isCustomGeo } from '../index';
import { EMapType } from '@/components/WorldMap';

// 页面类型 编辑/创建
enum EPageType {
  CREATE = 0,
  UPDATE = 1,
}

// 地区类型
enum EGeoType {
  BUILDIN = 0,
  CUSTOM = 1,
}

// url参数
interface ICreateQuery {
  id?: string;
  cityId: string;
  provinceId: string;
  countryId: string;
}

// 组件参数类型
interface IGeoLocationCreate {
  dispatch: Dispatch;
  allCountryMap: ICountryMap;
  allCityMap: ICityMap;
  allProvinceMap: IProvinceMap;
  location: {
    query: ICreateQuery;
    pathname: string;
  };
}

// 表单布局
const formLayout = {
  labelCol: { span: 1 },
  wrapperCol: { span: 22 },
};

const tailLayout = {
  wrapperCol: { span: 22, offset: 1 },
};

const { confirm } = Modal;

// 验证纬度信息
const validateLatitude = (rules: any, value: string, callback: any) => {
  if (!isNaN(parseInt(value, 10)) && parseInt(value, 10) >= -90 && parseInt(value, 10) <= 90) {
    callback();
  }
  callback('纬度信息应该在-90 ~ 90之间');
};

// 验证经度信息
const validateLongtitude = (rules: any, value: string, callback: any) => {
  if (!isNaN(parseInt(value, 10)) && parseInt(value, 10) >= -180 && parseInt(value, 10) <= 180) {
    callback();
  }
  callback('经度信息应该在-180 ~ 180之间');
};

// 验证ip信息
const checkTextAreaIp = (rule: any, value: string, callback: any) => {
  if (value) {
    const passIpArr: string[] = []; // 已经检查通过的IP
    const valueArr = value.split('\n');
    try {
      if (Array.isArray(valueArr)) {
        valueArr.forEach((item, index) => {
          const lineText = `第${index + 1}行[${item}]: `;
          if (!item) {
            throw new Error(`${lineText}不能为空`);
          }

          // IP网段
          if (item.indexOf('/') > -1) {
            const [ip, mask] = item.split('/');
            if (!ipV4Regex.test(ip) && !ipV6Regex.test(ip)) {
              throw new Error(`${lineText}请输入正确的IP/IP段`);
            }

            if (
              ipV4Regex.test(ip) &&
              (!mask ||
                isNaN(parseInt(mask, 10)) ||
                parseInt(mask, 10) <= 0 ||
                parseInt(mask, 10) > 32) &&
              !ipv4MaskRegex.test(mask)
            ) {
              throw new Error(`${lineText}请输入正确的IP v4网段。例，192.168.1.2/24`);
            }

            if (
              ipV6Regex.test(ip) &&
              (!mask ||
                isNaN(parseInt(mask, 10)) ||
                parseInt(mask, 10) <= 0 ||
                parseInt(mask, 10) > 128)
            ) {
              throw new Error(`${lineText}请输入正确的IP v6网段。例，2001:250:6EFA::/48`);
            }
          }

          // IP组
          else if (item.indexOf('-') > -1) {
            const ips = item.split('-');
            if (ips.length !== 2) {
              throw new Error(`${lineText}请输入正确的IP地址段。例，192.168.1.1-192.168.1.50`);
            }

            const [ip1, ip2] = ips;

            // 2个ipV4
            if (!ipV4Regex.test(ip1) && !ipV4Regex.test(ip2)) {
              throw new Error(`${lineText}请输入正确的IP地址段。例，192.168.1.1-192.168.1.50`);
            }
            // 2个都是ipV4的校验下大小关系
            if (ipV4Regex.test(ip1) && ipV4Regex.test(ip2)) {
              // 校验前后2个ip的大小关系
              const ip1Number = ip2number(ip1);
              const ip2Number = ip2number(ip2);

              // 起止地址是否符合大小要求
              if (ip1Number >= ip2Number) {
                throw new Error(`${lineText}截止IP必须大于开始IP`);
              }
            } else if (!ipV6Regex.test(ip1) && !ipV6Regex.test(ip2)) {
              // ip v6
              throw new Error(`${lineText}请输入正确的IP地址段。例，192.168.1.1-192.168.1.50`);
            }
          } else if (!ipV4Regex.test(item) && !ipV6Regex.test(item)) {
            throw new Error(`${lineText}请输入正确的IP/IP段`);
          }

          // 是否重复了
          if (passIpArr.indexOf(item) !== -1) {
            throw new Error(`${lineText}已重复`);
          }
          passIpArr.push(item);
        });
      }
    } catch (e) {
      callback(e);
    } finally {
      callback();
    }
  } else {
    callback();
  }
};

const CreateGeolocation: React.FC<IGeoLocationCreate> = ({
  dispatch,
  allCountryMap,
  allProvinceMap,
  allCityMap,
  location: { query, pathname },
}) => {
  // 编辑页面信息 (undefined为create)
  const [updateData, setUpdateData] = useState<ICity | IProvince | ICountry | ICustomCountry>();
  // 表单实例Ref
  const form = useRef<any>();
  // 地图选择Modal弹出
  const [mapModalVisible, setMapModalVisible] = useState<boolean>(false);
  // 地图标记经纬度
  const [longitude, setLongtitude] = useState<number>();
  const [latitude, setLatitude] = useState<number>();

  // 记录选择的坐标，此时未确认
  const [selectedCoord, setSelectedCoord] = useState<[number, number]>();

  // 记录鼠标滑过的坐标，便于地图tooltip实时显示
  const [mouseCoord, setMouseCoord] = useState<[number, number]>();

  // 数据变动时更新表单
  useEffect(() => {
    const { setFieldsValue, getFieldsValue } = form.current;
    const currentFieldsValue = getFieldsValue();
    setFieldsValue({
      name: updateData?.name || currentFieldsValue.name || undefined,
      description: updateData?.description || currentFieldsValue.description || undefined,
      longitude: longitude || updateData?.longitude || undefined,
      latitude: latitude || updateData?.latitude || undefined,
      ipAddress:
        updateData?.ipAddress?.replace(/,/g, '\n') || currentFieldsValue.ipAddress || undefined,
    });
  }, [longitude, latitude, updateData]);

  // 根据url参数获取数据
  useEffect(() => {
    const { id, cityId, provinceId, countryId } = query;
    if (id) {
      queryCustomGeoById(id).then((res) => {
        if (res.success) {
          setUpdateData(res.result);
        }
      });
    } else {
      if (cityId) {
        setUpdateData(allCityMap[cityId]);
        return;
      }
      if (provinceId) {
        setUpdateData(allProvinceMap[provinceId]);
        return;
      }
      if (countryId) {
        setUpdateData(allCountryMap[countryId]);
      }
    }
  }, [query, allCityMap, allProvinceMap, allCountryMap]);
  // 页面类型 创建｜编辑
  const pageType: EPageType = useMemo(() => {
    return pathname === '/configuration/objects/geolocation/update'
      ? EPageType.UPDATE
      : EPageType.CREATE;
  }, [pathname]);

  // 地区类型 自定义/系统内置
  const geoType = useMemo(() => {
    if (updateData && isCustomGeo(updateData.countryId)) {
      return EGeoType.CUSTOM;
    }
    return EGeoType.BUILDIN;
  }, [updateData]);

  // 处理Modal确认
  const handleModalOk = () => {
    if (selectedCoord && selectedCoord[0] && selectedCoord[1]) {
      setLongtitude(parseFloat(selectedCoord[0].toFixed(2)));
      setLatitude(parseFloat(selectedCoord[1].toFixed(2)));
      setMapModalVisible(false);
    } else {
      notification.open({
        message: '错误',
        description: '您未选择地点坐标',
        duration: 0,
      });
    }
  };

  const handleSubmit = (formValues: ICreateCustomCountry) => {
    confirm({
      title: '确定保存吗?',
      icon: <ExclamationCircleOutlined />,
      onOk() {
        if (pageType === EPageType.CREATE) {
          createCustomCountry({
            ...formValues,
            ipAddress: formValues.ipAddress?.replace(/\n/g, ','),
          }).then((res) => {
            if (res.success) {
              message.success('保存成功');
              dispatch({ type: 'geolocationModel/queryGeolocations' });
            }
          });
        } else if (pageType === EPageType.UPDATE) {
          if (updateData && geoType === EGeoType.BUILDIN) {
            // 内置地区更新ip
            updateIpAddress({
              countryId: updateData.countryId,
              provinceId: (updateData as IProvince).provinceId,
              cityId: (updateData as ICity).cityId,
              ipAddress: formValues.ipAddress ? formValues.ipAddress?.replace(/\n/g, ',') : '',
            }).then((res) => {
              if (res.success) {
                message.success('保存成功');
                dispatch({ type: 'geolocationModel/queryGeolocations' });
              }
            });
          } else if (updateData && geoType === EGeoType.CUSTOM) {
            // 自定义地区
            updateCustomCountry({
              id: updateData.id,
              ...formValues,
              ipAddress: formValues.ipAddress ? formValues.ipAddress?.replace(/\n/g, ',') : '',
            }).then((res) => {
              if (res.success) {
                message.success('保存成功');
              }
            });
          }
        }
      },
    });
  };
  return (
    <>
      <Row>
        <Col span={22} offset={1}>
          <Form
            ref={form}
            {...formLayout}
            onFinish={handleSubmit}
            // onFinishFailed={onFinishFailed}
            style={{ marginTop: '30px' }}
          >
            <Form.Item
              shouldUpdate={false}
              label="名称"
              name="name"
              rules={[
                { required: true, message: '必须输入名称' },
                { max: 30, message: '输入超过限制' },
              ]}
            >
              <Input
                disabled={pageType === EPageType.UPDATE && geoType === EGeoType.BUILDIN}
                placeholder="请输入名称"
              />
            </Form.Item>
            <div style={{ width: '100%', position: 'relative' }}>
              <Form.Item
                label="经度"
                name="longitude"
                rules={[
                  { required: true, message: '必须输入经度信息' },
                  {
                    pattern: new RegExp(/^((-?[1-9]\d*.?\d*)|(0{1}.?\d*))$/, 'g'),
                    message: '输入必须是数字',
                  },
                  { validator: validateLongtitude },
                ]}
              >
                <Input
                  style={{ width: '100%' }}
                  disabled={pageType === EPageType.UPDATE && geoType === EGeoType.BUILDIN}
                  placeholder="请输入经度"
                />
              </Form.Item>
              <Button
                style={{ position: 'absolute', right: 0, top: 0 }}
                type="link"
                disabled={pageType === EPageType.UPDATE && geoType === EGeoType.BUILDIN}
                onClick={() => {
                  setMapModalVisible(true);
                }}
              >
                选择
              </Button>
            </div>
            <Form.Item
              label="纬度"
              name="latitude"
              rules={[
                { required: true, message: '必须输入纬度信息' },
                {
                  pattern: new RegExp(/^((-?[1-9]\d*.?\d*)|(0{1}.?\d*))$/, 'g'),
                  message: '输入必须是数字',
                },
                { validator: validateLatitude },
              ]}
            >
              <Input
                style={{ width: '100%' }}
                disabled={pageType === EPageType.UPDATE && geoType === EGeoType.BUILDIN}
                placeholder="请输入纬度"
              />
            </Form.Item>
            <Form.Item
              shouldUpdate={false}
              label="IP地址"
              name="ipAddress"
              rules={[{ validator: checkTextAreaIp }]}
              extra={
                <ul style={{ paddingLeft: 20, listStyle: 'circle' }}>
                  <li>每行输入一个地址范围，或单个IP</li>
                  <li>行之间用回车分隔，示例:</li>
                  <li>192.168.10.10-192.168.10.20</li>
                  <li>192.168.10.30</li>
                  <li>192.168.10.0/24</li>
                  <li>192.168.10.0/255.255.255.0</li>
                </ul>
              }
            >
              <Input.TextArea rows={4} placeholder="请输入IP地址" />
            </Form.Item>
            <Form.Item
              label="描述"
              name="description"
              rules={[{ max: 255, message: '最多可输入255个字符' }]}
            >
              <Input.TextArea
                placeholder="请输入描述信息"
                disabled={pageType === EPageType.UPDATE && geoType === EGeoType.BUILDIN}
              />
            </Form.Item>
            <Form.Item {...tailLayout}>
              <Space>
                <Button type="primary" htmlType="submit">
                  保存
                </Button>
                <Button
                  onClick={() => {
                    history.push(getLinkUrl('/configuration/objects/geolocation'));
                  }}
                >
                  返回
                </Button>
              </Space>
            </Form.Item>
          </Form>
        </Col>
      </Row>
      <Modal
        mask={true}
        maskClosable={false}
        width={1429}
        bodyStyle={{ height: 690 }}
        title="设置经纬度"
        visible={mapModalVisible}
        destroyOnClose
        onOk={handleModalOk}
        onCancel={() => {
          setMapModalVisible(false);
        }}
      >
        <WorldMap
          mapType={EMapType.SELECTER}
          mousePosition={mouseCoord}
          selectPosition={selectedCoord}
          lineEffectData={[]}
          onChartRendered={(echart: any) => {
            echart.on('click', (params: any) => {
              setSelectedCoord(
                echart.convertFromPixel('geo', [params.event.offsetX, params.event.offsetY]),
              );
            });
            echart.on('mousemove', (params: any) => {
              const mousePos = echart.convertFromPixel('geo', [
                params.event.offsetX,
                params.event.offsetY,
              ]);
              setMouseCoord([mousePos[0].toFixed(2), mousePos[1].toFixed(2)]);
            });
          }}
        />

        <div style={{ position: 'absolute', left: 20, bottom: 50 }}>
          <ul>
            <li>经度: 西经-180.00° ～ 东经180.00°</li>
            <li>纬度: 南纬-90.00° ～ 北纬90.00°</li>
          </ul>
        </div>
      </Modal>
    </>
  );
};
export default connect(
  ({ geolocationModel: { allCityMap, allProvinceMap, allCountryMap } }: ConnectState) => ({
    allCityMap,
    allProvinceMap,
    allCountryMap,
  }),
)(CreateGeolocation);
