import type { ConnectState } from '@/models/connect';
import {
  EModelAlias,
  getEnumValueFromModelNext,
} from '@/pages/app/analysis/components/fieldsManager';
import type {
  ICityMap,
  ICountryMap,
  ICustomCountryMap,
  IProvinceMap,
} from '@/pages/app/Configuration/Geolocation/typings';
import type { IApplicationMap } from '@/pages/app/Configuration/SAKnowledge/typings';
import { useCallback } from 'react';
import { connect } from 'umi';
import type { ISearchBoxInfo } from '../../../components/SearchBox';
import { EDimensionsSearchType } from '../../../typing';

interface Props {
  text?: any;
  exInfo?: string;
  searchBoxInfo?: ISearchBoxInfo;
  allCountryMap?: ICountryMap;
  allProvinceMap?: IProvinceMap;
  allCityMap?: ICityMap;
  allCustomCountryMap?: ICustomCountryMap;
  allApplicationMap?: IApplicationMap;
}

function TransformTitle(props: Props) {
  const {
    text,
    exInfo,
    allCountryMap = {},
    allProvinceMap = {},
    allCityMap = {},
    allApplicationMap = {},
    allCustomCountryMap = {},
  } = props;
  const getTabName = useCallback(
    (item) => {
      let tmpFunc: any = () => item.content;
      const translationType = item?.dimensionsSearchType;
      if (translationType === EDimensionsSearchType.LOCATION) {
        tmpFunc = (ele: ISearchBoxInfo) => {
          const locationCodeArr = ele.content.split('_');
          let seriesName = locationCodeArr[locationCodeArr.length];
          if (ele.content.length === 20) {
            seriesName = Object.values(allCustomCountryMap).find((sub) => sub.id === ele.content)
              ?.fullName as string;
          } else if (locationCodeArr.length === 3) {
            seriesName = allCityMap[locationCodeArr.pop() || 0]?.fullName as string;
          } else if (locationCodeArr.length === 2) {
            seriesName = allProvinceMap[locationCodeArr.pop() || 0]?.fullName as string;
          } else if (locationCodeArr.length === 1) {
            seriesName = allCountryMap[locationCodeArr.pop() || 0]?.fullName as string;
          }
          return seriesName || '[--]';
        };
      } else if (translationType === EDimensionsSearchType.APPLICATION) {
        tmpFunc = (ele: ISearchBoxInfo) => {
          return allApplicationMap[ele.content]?.nameText || (ele.content as string);
        };
      } else if (translationType === EDimensionsSearchType.L7PROTOCOLID) {
        const protocol = getEnumValueFromModelNext(EModelAlias.l7protocol)?.list || [];
        tmpFunc = (ele: ISearchBoxInfo) => {
          return protocol.find((sub) => sub.value === ele.content)?.text || (ele.content as string);
        };
      }

      return tmpFunc(item);
    },
    [allApplicationMap, allCityMap, allCountryMap, allCustomCountryMap, allProvinceMap],
  );
  return <span>{`${getTabName(text)} - ${exInfo}`}</span>;
}

export default connect((state: ConnectState) => {
  const {
    appModel: { globalSelectedTime },
    geolocationModel: { allCountryMap, allProvinceMap, allCityMap, allCustomCountryMap },
    SAKnowledgeModel: { allApplicationMap },
  } = state;
  return {
    allCountryMap,
    allProvinceMap,
    allCityMap,
    allApplicationMap,
    allCustomCountryMap,
    globalSelectedTime,
  };
})(TransformTitle);
