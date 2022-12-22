import moment from 'moment';
import React from 'react';
import { useState } from 'react';

import SearchBox from './components/SearchBox';
import ShowPart from './components/ShowPart';

// interface Props {
//   globalSelectedTime: Required<IGlobalTime>;
//   dispatch: Dispatch;
//   queryLoading: boolean | undefined;
//   allNetworkStatData: INetworkStatData[];
// }
export const SearchDataContext = React.createContext<any>([]);
function SegmentAnalysis() {
  const [searchInfo, setSearchInfo] = useState();

  // const currentNetworkMap = {};

  // useEffect(() => {
  //   console.log(allNetworkStatData);
  // }, [allNetworkStatData]);

  // const buildTreeOpt = (arr: any) => {
  //   return arr.map((item: any) => {
  //     const selectedItem = { title: item.networkName, value: item.networkId? item.networkId+'^networkId', key: item.networkId };
  //     currentNetworkMap[item.networkId] = item.networkName;
  //     if (item.children) {
  //       return {
  //         ...selectedItem,
  //         children: buildTreeOpt(item?.children),
  //       };
  //     }
  //     return selectedItem;
  //   });
  // };
  // const networkDataOpt = buildTreeOpt(allNetworkStatData);

  return (
    <SearchDataContext.Provider value={{}}>
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

export default SegmentAnalysis;
