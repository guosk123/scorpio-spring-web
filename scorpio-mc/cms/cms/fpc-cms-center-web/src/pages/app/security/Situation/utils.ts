import type { ILineEffectPoint } from '@/components/WorldMap';
import type { GeolocationModelState } from '@/models/app/geolocation';
import type { IGeo } from '@/pages/app/Configuration/Geolocation/typings';
import { isEmpty, isExisty } from '@/utils/utils';
import type { ISuricataAlertMessage } from '../../security/typings';

/** 根据地理位置ID查找地理位置信息 */
export const getLocationInfo = (
  geolocationModel: GeolocationModelState,
  info: { countryId: string; provinceId?: string; cityId?: string } | string,
): IGeo | undefined => {
  const { allCityMap, allProvinceMap, allCountryMap } = geolocationModel;
  if (typeof info === 'string') {
    return allCityMap[info] || allProvinceMap[info] || allCountryMap[info] || undefined;
  }
  if (info.countryId) {
    if (info.countryId !== '1') {
      return allCountryMap[info.countryId];
    }

    if (info.cityId) {
      return allCityMap[info.cityId];
    }

    if (info.provinceId) {
      return allProvinceMap[info.provinceId];
    }
  }

  return undefined;
};

/**
 * 用来获取经纬度数据
 */
export function getGeolocationPosition(
  eventList: ISuricataAlertMessage[],
  geolocationModel: GeolocationModelState,
) {
  const lineEffectData: [ILineEffectPoint, ILineEffectPoint][] = [];

  for (let index = 0; index < eventList.length; index += 1) {
    const row = eventList[index];
    // 已经存在的攻击列表
    const existedList: string[] = [];
    const initiator = getLocationInfo(geolocationModel, {
      cityId: row.cityIdInitiator,
      provinceId: row.provinceIdInitiator,
      countryId: row.countryIdInitiator,
    });
    const responder = getLocationInfo(geolocationModel, {
      cityId: row.cityIdResponder,
      provinceId: row.provinceIdResponder,
      countryId: row.countryIdResponder,
    });

    if (
      // 排除重复的
      !existedList.includes(`${initiator?.id}_${responder?.id}`) &&
      // 地区存在
      initiator &&
      responder &&
      // 经纬度有效
      isExisty(initiator.longitude) &&
      !isEmpty(initiator.latitude) &&
      isExisty(responder.longitude) &&
      !isEmpty(responder.latitude) &&
      // 起止位置不相等
      initiator.longitude !== responder.longitude &&
      initiator.latitude !== responder.latitude
    ) {
      // 如果地区名字是 Unknown ，并且经纬度是[0.0,0.0]，则排除掉
      const initiatorIsUnknown = initiator?.name === 'Unknown';
      const responderIsUnknown = responder?.name === 'Unknown';

      if (initiatorIsUnknown && initiator?.longitude === '0.0' && initiator.latitude === '0.0') {
        continue;
      }
      if (responderIsUnknown && responder?.longitude === '0.0' && responder.latitude === '0.0') {
        continue;
      }

      existedList.push(`${initiator?.id}_${responder?.id}`);

      lineEffectData.push([
        {
          coord: [initiator.longitude, initiator.latitude],
        },
        {
          coord: [responder.longitude, responder.latitude],
        },
      ]);
    }
  }

  return lineEffectData;
}
