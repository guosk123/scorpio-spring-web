import type { ConnectState } from '@/models/connect';
import moment from 'moment';
import { useCallback, useEffect, useState } from 'react';
import SearchBox from './components/SearchBox';
import ShowPart from './components/ShowPart';
import { connect } from 'umi';
import type { INetworkStatData } from '../typings';
import { ESortDirection } from '../typings';
import type { Dispatch } from 'umi';
import { snakeCase } from '@/utils/utils';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import React from 'react';

interface Props {
  globalSelectedTime: Required<IGlobalTime>;
  dispatch: Dispatch;
  queryLoading: boolean | undefined;
  allNetworkStatData: INetworkStatData[];
}
export const SearchDataContext = React.createContext<any>({});

function SegmentAnalysis(props: Props) {
  const { globalSelectedTime, dispatch, queryLoading, allNetworkStatData } = props;
  const [searchInfo, setSearchInfo] = useState();

  const queryData = useCallback(() => {
    dispatch({
      type: 'npmdModel/queryAllNetworkStat',
      payload: {
        sortProperty: snakeCase('totalBytes'),
        sortDirection: ESortDirection.DESC,
        startTime: globalSelectedTime.startTime,
        endTime: globalSelectedTime.endTime,
        interval: globalSelectedTime.interval,
      },
    });
  }, [
    dispatch,
    globalSelectedTime.endTime,
    globalSelectedTime.interval,
    globalSelectedTime.startTime,
  ]);

  useEffect(() => {
    queryData();
  }, [queryData]);

  // console.log(allNetworkStatData, 'allNetworkStatData');
  const currentNetworkMap = {};

  const buildTreeOpt = (arr: any) => {
    return arr.map((item: any) => {
      const selectedItem = { title: item.networkName, value: item.networkId, key: item.networkId };
      currentNetworkMap[item.networkId] = item.networkName;
      if (item.children) {
        return {
          ...selectedItem,
          children: buildTreeOpt(item?.children),
        };
      }
      return selectedItem;
    });
  };
  const networkDataOpt = buildTreeOpt(allNetworkStatData);

  return (
    <SearchDataContext.Provider value={{ queryLoading, networkDataOpt, currentNetworkMap }}>
      <SearchBox
        onSubmit={(e: any) => {
          setSearchInfo({ ...e, timeStamp: moment().valueOf() });
          // searchState(e);
        }}
      />
      <ShowPart searchInfo={(searchInfo || undefined) as any} />
    </SearchDataContext.Provider>
  );
}

export default connect(
  ({
    loading: { effects },
    npmdModel: { allNetworkStatData },
    appModel: { globalSelectedTime },
  }: ConnectState) => ({
    queryLoading: effects['npmdModel/queryAllNetworkStat'],
    allNetworkStatData,
    globalSelectedTime,
  }),
)(SegmentAnalysis);
