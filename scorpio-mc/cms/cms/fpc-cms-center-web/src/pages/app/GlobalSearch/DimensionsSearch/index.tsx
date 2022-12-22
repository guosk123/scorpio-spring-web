import moment from 'moment';
import React from 'react';
import { useState } from 'react';
import SearchBox from './components/SearchBox';
import SeartchTabs from './SeartchTabs';

export const SearchInfoContext = React.createContext<any>([]);
export default function DimensionsSearch() {
  const [searchInfo, setSearchInfo] = useState();

  return (
    <SearchInfoContext.Provider value={[searchInfo, setSearchInfo]}>
      <SearchBox
        onSubmit={(e: any) => {
          setSearchInfo({ ...e, timeStamp: moment().valueOf() });
          // searchState(e);
        }}
      />
      <SeartchTabs searchInfo={(searchInfo || {}) as any} />
      {/* <EditTab searchBoxInfo={searchInfo} /> */}
    </SearchInfoContext.Provider>
  );
}
