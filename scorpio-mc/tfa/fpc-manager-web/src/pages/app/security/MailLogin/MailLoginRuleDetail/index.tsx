import type { ConnectState } from '@/models/connect';
import type { IMailLoginRule } from '@/pages/app/configuration/MailLoginRule/typings';
import { RuleActionLabel, WeekLabel } from '@/pages/app/configuration/MailLoginRule/typings';
import { Descriptions, Spin } from 'antd';
import { useMemo } from 'react';
import type { GeolocationModelState } from 'umi';
import { useSelector } from 'umi';

const MailLoginRuleDetail = ({
  detail,
  loading,
}: {
  detail?: IMailLoginRule;
  loading: boolean;
}) => {
  const { allCityMap, allCountryMap, allProvinceMap } = useSelector<
    ConnectState,
    GeolocationModelState
  >((state) => state.geolocationModel);

  const location = useMemo(() => {
    if (!detail) {
      return '';
    }
    let result = '';
    if (detail.countryId !== '0' && allCountryMap[detail.countryId]?.nameText) {
      result += allCountryMap[detail.countryId]?.nameText;
    }
    if (
      detail.provinceId &&
      detail.provinceId !== '0' &&
      allProvinceMap[detail.provinceId]?.nameText
    ) {
      result += `/${allProvinceMap[detail.provinceId]?.nameText}`;
    }
    if (detail.cityId && detail.cityId !== '0' && allCityMap[detail.cityId]?.nameText) {
      result += `/${allCityMap[detail.cityId]?.nameText}`;
    }
    return result;
  }, [allCityMap, allCountryMap, allProvinceMap, detail]);

  if (loading || !detail) {
    return (
      <div className="center">
        <Spin />
      </div>
    );
  }

  return (
    <Descriptions bordered column={1} size="small">
      <Descriptions.Item label="邮箱">{detail.mailAddress}</Descriptions.Item>
      <Descriptions.Item label="地区">{location}</Descriptions.Item>
      <Descriptions.Item label="开始时间">{detail.startTime}</Descriptions.Item>
      <Descriptions.Item label="开始时间">{detail.endTime}</Descriptions.Item>
      <Descriptions.Item label="动作">{RuleActionLabel[detail.action]}</Descriptions.Item>
      <Descriptions.Item label="每周生效时间">
        {detail.period
          ?.split(',')
          .map((p) => WeekLabel[p])
          .join(',')}
      </Descriptions.Item>
    </Descriptions>
  );
};

export default MailLoginRuleDetail;
