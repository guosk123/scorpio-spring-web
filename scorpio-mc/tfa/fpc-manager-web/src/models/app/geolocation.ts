import modelExtend from 'dva-model-extend';
import { model } from '@/utils/frame/model';
import type { Effect } from 'umi';
import lodash from 'lodash';
import {
  queryGeolocationKnowledge,
  queryGeolocations,
  importCustomGeo,
} from '@/pages/app/configuration/Geolocation/service';
import type {
  IGeolocationKnowledge,
  ICountry,
  IProvince,
  ICity,
  ICountryMap,
  IProvinceMap,
  ICityMap,
  ICustomCountry,
  ICustomCountryMap,
} from '@/pages/app/configuration/Geolocation/typings';
import { message } from 'antd';

export interface GeolocationModelState {
  geolocationKnowledge: IGeolocationKnowledge;

  allCountryList: ICountry[];
  allCountryMap: ICountryMap;

  allProvinceList: IProvince[];
  allProvinceMap: IProvinceMap;

  allCityList: ICity[];
  allCityMap: ICityMap;

  allCustomCountryList: ICustomCountry[];
  allCustomCountryMap: ICustomCountryMap;
}

interface GeolocationModelType {
  namespace: string;
  state: GeolocationModelState;
  effects: {
    /**
     * 获取地理位置库
     */
    queryGeolocationKnowledge: Effect;
    /**
     * 获取地址位置映射关系
     */
    queryGeolocations: Effect;
    /**
     * 导入自定义地区
     */
    importCustomGeo: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'geolocationModel',
  state: {
    geolocationKnowledge: {} as IGeolocationKnowledge,
    allCountryList: [],
    allCountryMap: {},
    allProvinceList: [],
    allProvinceMap: {},
    allCityList: [],
    allCityMap: {},
    allCustomCountryList: [],
    allCustomCountryMap: {},
  },

  effects: {
    *queryGeolocationKnowledge(_, { call, put }) {
      const { success, result } = yield call(queryGeolocationKnowledge);
      yield put({
        type: 'updateState',
        payload: {
          geolocationKnowledge: success ? result : {},
        },
      });
    },
    *queryGeolocations(_, { call, put }) {
      const {
        success,
        result,
      }: {
        success: boolean;
        result: {
          countryList: ICountry[];
          provinceList: IProvince[];
          cityList: ICity[];
          customCountryList: ICustomCountry[];
        };
      } = yield call(queryGeolocations);

      // 解析地址位置
      let allCountryList: ICountry[] = [];
      const allCountryMap: ICountryMap = {};
      let allProvinceList: IProvince[] = [];
      const allProvinceMap: IProvinceMap = {};
      let allCityList: ICity[] = [];
      const allCityMap: ICityMap = {};
      let allCustomCountryList: ICustomCountry[] = [];
      const allCustomCountryMap: ICustomCountryMap = {};

      if (success) {
        // 国家
        allCountryList = Array.isArray(result.countryList) ? result.countryList : [];
        // 省份
        allProvinceList = Array.isArray(result.provinceList) ? result.provinceList : [];
        // 城市
        allCityList = Array.isArray(result.cityList) ? result.cityList : [];
        // 自定义
        allCustomCountryList = Array.isArray(result.customCountryList)
          ? result.customCountryList
          : [];

        const cityGroupByProvince = lodash.groupBy(allCityList, 'provinceId');
        const provinceGroupByCountry = lodash.groupBy(allProvinceList, 'countryId');

        for (let index = 0; index < allCountryList.length; index += 1) {
          const item = allCountryList[index];
          item.id = `${item.countryId}`;
          item.fullName = item.nameText;
          item.value = item.id;
          item.title = item.nameText;
          if ((provinceGroupByCountry[item.countryId] || []).length > 0) {
            item.children = provinceGroupByCountry[item.countryId] || [];
          }

          allCountryMap[item.countryId] = item;
        }
        for (let index = 0; index < allProvinceList.length; index += 1) {
          const item = allProvinceList[index];
          item.id = `${item.countryId}_${item.provinceId}`;
          item.fullName = allCountryMap[item.countryId]
            ? `${allCountryMap[item.countryId].fullName}-${item.nameText}`
            : item.nameText;
          item.value = item.id;
          item.title = item.nameText;
          if ((cityGroupByProvince[item.provinceId!] || []).length > 0) {
            item.children = cityGroupByProvince[item.provinceId!] || [];
          }

          allProvinceMap[item.provinceId!] = item;
        }
        for (let index = 0; index < allCityList.length; index += 1) {
          const item = allCityList[index];
          item.id = `${item.countryId}_${item.provinceId}_${item.cityId}`;
          item.fullName = allProvinceMap[item.provinceId]
            ? `${allProvinceMap[item.provinceId].fullName}-${item.nameText}`
            : item.nameText;
          item.value = item.id;
          item.title = item.nameText;
          allCityMap[item.cityId!] = item;
        }

        const tempAllCustomCountryList: any[] = [];
        for (let index = 0; index < allCustomCountryList.length; index += 1) {
          const item = allCustomCountryList[index];
          item.value = item.id;
          item.title = item.name;
          item.nameText = `${item.name}[自定义]`;
          item.fullName = `${item.name}[自定义]`;
          item.countryCode = undefined;
          allCustomCountryMap[item.countryId] = item;
          // 填充到国家中，方便其他地方取数据时直接使用
          allCountryMap[item.countryId] = item;
          tempAllCustomCountryList.push(item);
        }

        allCountryList = [...tempAllCustomCountryList, ...allCountryList];
      }

      yield put({
        type: 'updateState',
        payload: {
          allCountryList,
          allCountryMap,
          allProvinceList,
          allProvinceMap,
          allCityList,
          allCityMap,
          allCustomCountryList,
          allCustomCountryMap,
        },
      });
    },
    *importCustomGeo({ payload }, { call, put }) {
      const { success } = yield call(importCustomGeo, payload);
      if (success) {
        message.success('导入成功');
        // 重新拉取SA规则库内容
        yield put({
          type: 'geolocationModel/queryGeolocations',
        });
      } else {
        message.error('导入失败');
      }
      return success;
    },
  },

  reducers: {},
} as GeolocationModelType);
