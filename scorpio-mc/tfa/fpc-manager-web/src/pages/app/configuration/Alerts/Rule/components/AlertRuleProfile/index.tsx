/* eslint-disable no-nested-ternary */
import type { INetworkTreeData } from '@/models/app/network';
import type { ConnectState } from '@/models/connect';
import type {
  ICityMap,
  ICountryMap,
  IProvinceMap,
} from '@/pages/app/configuration/Geolocation/typings';
import type { IpAddressGroupMap } from '@/pages/app/configuration/IpAddressGroup/typings';
import type { ILogicalSubnet } from '@/pages/app/configuration/LogicalSubnet/typings';
import type { IApplicationMap } from '@/pages/app/configuration/SAKnowledge/typings';
import type { IService } from '@/pages/app/configuration/Service/typings';
import { enumValue2Label } from '@/utils/utils';
import { QuestionCircleOutlined } from '@ant-design/icons';
import { Card, Drawer, Empty, Form, Skeleton, Tooltip } from 'antd';
import { connect } from 'dva';
import type { ReactNode } from 'react';
import { useMemo } from 'react';
import { Fragment, useState } from 'react';
import type { Dispatch } from 'redux';
import type { IAlertRule } from '../../../typings';
import { ALTER_SOURCE_ENUM } from '../../../typings';
import {
  ALERT_CALCULATION_ENUM,
  ALERT_CATEGORY_ENUM,
  ALERT_LEVEL_ENUM,
  ALERT_METRIC_ENUM,
  ALERT_REFIRE_TIME_ENUM,
  ALERT_REFIRE_TYPE_ENUM,
  EAlertCategory,
  EAlertRefireType,
  ESource,
  OPERATOR_ENUM,
  TREND_WEIGHTING_MODEL_ENUM,
  TREND_WINDOWING_MODEL_ENUM,
  WINDOW_SECONDS_ENUM,
} from '../../../typings';
import ComposeCondition from '../ComposeCondition';

const FormItem = Form.Item;

const formLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 20 },
  style: { marginBottom: 8 },
};

interface IAlertRuleProfileProps {
  dispatch: Dispatch<any>;
  id: string;
  category: EAlertCategory;
  detail: IAlertRule;
  allAlertRule: IAlertRule[];
  allCountryMap: ICountryMap;
  allProvinceMap: IProvinceMap;
  allCityMap: ICityMap;
  allApplicationMap: IApplicationMap;
  allIpAddressGroupMap: IpAddressGroupMap;
  queryDetailLoading: boolean | undefined;
  networkTree: INetworkTreeData[];
  allLogicalSubnets: ILogicalSubnet[];
  allServices: IService[];
  children: string | ReactNode;
}

const AlertRuleProfile = (props: IAlertRuleProfileProps) => {
  const {
    dispatch,
    id,
    category,

    allAlertRule = [],
    allIpAddressGroupMap = {},
    allApplicationMap = {},
    allCountryMap = {},
    allProvinceMap = {},
    allCityMap = {},

    detail = {} as IAlertRule,
    queryDetailLoading,
    children,

    networkTree,
    allLogicalSubnets,
    allServices,
  } = props;

  const [drawerVisible, setDrawerVisible] = useState<boolean>(false);

  const handleOpen = () => {
    setDrawerVisible(true);

    dispatch({
      type: 'alertModel/queryAlertRuleDetail',
      payload: {
        id,
      },
    });
    if (category === EAlertCategory.ADVANCED) {
      dispatch({
        type: 'alertModel/queryAllAlertRules',
        payload: {
          category: [EAlertCategory.THRESHOLD, EAlertCategory.TREND].join(','),
        },
      });
    }
  };

  const handleClose = () => {
    setDrawerVisible(false);
  };

  const handleClearDetailData = (visible: boolean) => {
    if (!visible) {
      dispatch({
        type: 'alertModel/updateState',
        payload: {
          alertRuleDetail: {},
        },
      });
    }
  };

  const sourceValueToLabel = (sourceType: ESource, value: string) => {
    if (sourceType === ESource.HOSTGROUP) {
      return allIpAddressGroupMap[value]?.name || value;
    }
    if (sourceType === ESource.APPLICATION) {
      return allApplicationMap[value]?.nameText || value;
    }
    if (sourceType === ESource.GEOLOCATION) {
      // 分隔 value（国家ID_省份ID_城市ID）
      const [countryId, privinceId, cityId] = value?.split('_') || [];
      if (cityId) {
        return allCityMap[cityId]?.nameText || value;
      }
      if (privinceId) {
        return allProvinceMap[privinceId]?.nameText || value;
      }
      if (countryId) {
        return allCountryMap[countryId]?.nameText || value;
      }
      return value;
    }
    if (sourceType === ESource.IPADDRESS) {
      return value;
    }
    return value;
  };

  const networkAndServiceList = useMemo(() => {
    const tmpNetwork = networkTree.concat(
      allLogicalSubnets.map((item) => ({
        title: `${item.name}(子网)`,
        key: item.id,
        value: item.id,
      })),
    );
    const serviceAndNetwork: any[] = [];
    tmpNetwork.forEach((item) => {
      serviceAndNetwork.push(
        ...allServices
          .filter((ele) => ele.networkIds?.split(',').includes(item.key))
          .map((sub) => ({
            title: `${sub.name}(业务)`,
            key: `${sub.id}^${item.key}`,
          })),
      );
    });
    return tmpNetwork.concat(serviceAndNetwork);
  }, [allLogicalSubnets, allServices, networkTree]);

  return (
    <Fragment>
      <span onClick={handleOpen}>{children}</span>
      <Drawer
        width={660}
        destroyOnClose
        title="告警配置详情"
        onClose={() => handleClose()}
        afterVisibleChange={handleClearDetailData}
        visible={drawerVisible}
      >
        {queryDetailLoading ? (
          <Skeleton active />
        ) : !detail.id ? (
          <Empty description="告警配置不存在或已被删除" />
        ) : (
          <Form>
            <FormItem key="id" {...formLayout} label="id" style={{ display: 'none' }}>
              <span className="ant-form-text">{detail.id}</span>
            </FormItem>
            <FormItem key="name" {...formLayout} label="名称">
              <span className="ant-form-text">{detail.name}</span>
            </FormItem>
            <FormItem key="category" {...formLayout} label="分类">
              <span className="ant-form-text">
                {enumValue2Label(ALERT_CATEGORY_ENUM, detail.category)}
              </span>
            </FormItem>
            <FormItem key="level" {...formLayout} label="级别">
              <span className="ant-form-text">
                {enumValue2Label(ALERT_LEVEL_ENUM, detail.level)}
              </span>
            </FormItem>
            {(detail.thresholdSettings?.metrics?.numerator?.sourceType ||
              detail.trendSettings?.metrics?.numerator?.sourceType) && (
              <FormItem key="source" {...formLayout} label="告警数据源" style={{ marginBottom: 0 }}>
                <span className="ant-form-text">
                  <span className="ant-form-text">
                    {ALTER_SOURCE_ENUM[
                      detail.thresholdSettings?.metrics?.numerator?.sourceType ||
                        detail.trendSettings?.metrics?.numerator?.sourceType
                    ] ||
                      detail.thresholdSettings?.metrics?.numerator?.sourceType ||
                      detail.trendSettings?.metrics?.numerator?.sourceType}
                  </span>
                  <span>值：</span>
                  <span className="ant-form-text">
                    {sourceValueToLabel(
                      detail.thresholdSettings?.metrics?.numerator?.sourceType ||
                        detail.trendSettings?.metrics?.numerator?.sourceType,
                      detail.thresholdSettings?.metrics?.numerator?.sourceValue ||
                        detail.trendSettings?.metrics?.numerator?.sourceValue,
                    )}
                  </span>
                </span>
              </FormItem>
            )}

            {(detail?.thresholdSettings?.metrics?.numerator ||
              detail?.trendSettings?.metrics?.numerator) && (
              <FormItem key="metrics[numerator[metric]]" {...formLayout} label="指标">
                <span className="ant-form-text">
                  {enumValue2Label(
                    ALERT_METRIC_ENUM,
                    detail.thresholdSettings?.metrics?.numerator?.metric ||
                      detail?.trendSettings?.metrics?.numerator?.metric,
                  )}
                </span>
              </FormItem>
            )}
            {(detail?.thresholdSettings?.metrics?.isRatio ||
              detail?.trendSettings?.metrics?.isRatio) && (
              <Fragment>
                <FormItem key="metrics[denominator[metric]]" {...formLayout} label="分母指标">
                  <span className="ant-form-text">
                    {enumValue2Label(
                      ALERT_METRIC_ENUM,
                      detail.thresholdSettings?.metrics?.denominator?.metric ||
                        detail.trendSettings?.metrics?.denominator?.metric,
                    )}
                  </span>
                </FormItem>
              </Fragment>
            )}

            {detail.category === EAlertCategory.TREND && (
              <FormItem key="trendDefine" {...formLayout} label="基线定义">
                <Card bodyStyle={{ padding: '10px 10px 0' }}>
                  <FormItem key="trendDefine[weightingModel]" {...formLayout} label="权重模型">
                    <span className="ant-form-text">
                      {enumValue2Label(
                        TREND_WEIGHTING_MODEL_ENUM,
                        detail.trendDefine?.weightingModel,
                      )}
                    </span>
                  </FormItem>
                  <FormItem key="trendDefine[windowingModel]" {...formLayout} label="基线窗口">
                    <span className="ant-form-text">
                      {enumValue2Label(
                        TREND_WINDOWING_MODEL_ENUM,
                        detail.trendDefine?.windowingModel,
                      )}
                    </span>
                  </FormItem>
                  <FormItem key="trendDefine[windowingCount]" {...formLayout} label="回顾周期">
                    <span className="ant-form-text">{detail.trendDefine?.windowingCount}</span>
                  </FormItem>
                </Card>
              </FormItem>
            )}
            {detail.category !== EAlertCategory.ADVANCED && (
              <FormItem
                key="fireCriteria"
                {...formLayout}
                label="告警条件"
                style={{ marginBottom: 0 }}
              >
                <span className="ant-form-text">过去</span>
                <span className="ant-form-text">
                  {enumValue2Label(WINDOW_SECONDS_ENUM, `${detail.fireCriteria?.windowSeconds}`)}
                </span>
                <span className="ant-form-text">内</span>
                <span className="ant-form-text">
                  {enumValue2Label(ALERT_CALCULATION_ENUM, detail.fireCriteria?.calculation)}
                </span>
                <span className="ant-form-text">值</span>
                <span className="ant-form-text">
                  {enumValue2Label(OPERATOR_ENUM, detail.fireCriteria?.operator)}
                </span>
                <span className="ant-form-text">{detail.fireCriteria?.operand}</span>
                {detail.category === EAlertCategory.TREND && (
                  <span className="ant-form-text" style={{ marginLeft: 4 }}>
                    趋势百分比
                  </span>
                )}
              </FormItem>
            )}
            {detail.category === EAlertCategory.ADVANCED && (
              <Fragment>
                <FormItem label="告警组合配置" {...formLayout}>
                  <ComposeCondition
                    readonly
                    condition={detail.advancedSettings?.fireCriteria}
                    alertSetings={allAlertRule}
                  />
                </FormItem>
                <FormItem
                  label={
                    <span>
                      触发周期{' '}
                      <Tooltip title="周期时间内组合条件均满足才认为告警满足">
                        <QuestionCircleOutlined />
                      </Tooltip>
                    </span>
                  }
                  {...formLayout}
                >
                  <span className="ant-form-text">
                    {enumValue2Label(
                      WINDOW_SECONDS_ENUM,
                      `${detail.advancedSettings?.windowSeconds}`,
                    )}
                  </span>
                </FormItem>
              </Fragment>
            )}
            <FormItem key="refire" {...formLayout} label="告警间隔">
              <span className="ant-form-text">
                {enumValue2Label(ALERT_REFIRE_TYPE_ENUM, detail.refire?.type)}
              </span>
              {detail.refire?.type === EAlertRefireType.REPEATEDLY && (
                <span className="ant-form-text">
                  间隔时间：
                  {enumValue2Label(ALERT_REFIRE_TIME_ENUM, `${detail.refire?.seconds}`)}
                </span>
              )}
            </FormItem>
            <FormItem key="description" {...formLayout} label="描述信息">
              <span className="ant-form-text">{detail.description}</span>
            </FormItem>
            <FormItem key="scope" {...formLayout} label="作用域">
              <span className="ant-form-text">
                {detail?.networkIds === 'allNetwork'
                  ? '所有网络'
                  : networkAndServiceList
                      .filter((item) => detail?.networkIds?.split(',').includes(item.key))
                      .concat(
                        networkAndServiceList.filter((item) =>
                          detail?.serviceIds?.split(',').includes(item.key),
                        ),
                      )
                      .map((ele: any) => ele.title)
                      .join()}
              </span>
            </FormItem>
          </Form>
        )}
      </Drawer>
    </Fragment>
  );
};

export default connect(
  ({
    alertModel: { alertRuleDetail, allAlertRule },
    geolocationModel: { allCountryMap, allProvinceMap, allCityMap },
    ipAddressGroupModel: { allIpAddressGroupMap },
    networkModel: { networkTree },
    logicSubnetModel: { allLogicalSubnets },
    serviceModel: { allServices },
    SAKnowledgeModel: { allApplicationMap },
    loading: { effects },
  }: ConnectState) => ({
    detail: alertRuleDetail,
    allAlertRule,
    allCountryMap,
    allProvinceMap,
    allCityMap,
    networkTree,
    allLogicalSubnets,
    allServices,
    allIpAddressGroupMap,
    allApplicationMap,
    queryDetailLoading:
      effects['alertModel/queryAlertRuleDetail'] || effects['alertModel/queryAllAlertRules'],
  }),
)(AlertRuleProfile);
