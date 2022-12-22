import { Button } from 'antd';
import { useContext, useEffect, useRef, useState } from 'react';
import { DimensionsSearchContext } from '../..';
import { SearchInfoContext } from '../../..';
import type { ISearchBoxInfo } from '../../../components/SearchBox';
import { EDimensionsSearchType } from '../../../typing';
import { jumpToDimensionsTab } from '../../constant';
import { EDimensionsTab } from '../../typing';

interface Props {
  searchInfo?: ISearchBoxInfo;
  onChangeInfo?: any;
}

export default function LinkToSearchTab(props: Props) {
  const [state, dispatch] = useContext<any>(DimensionsSearchContext);
  const [searchInfo, setSearchInfo] = useContext<any>(SearchInfoContext);
  useEffect(() => {
    if (!searchInfo) {
      return;
    }
    // if (searchInfo.dimensionsSearchType === EDimensionsSearchType.IPADDRESS) {
    //   jumpToDimensionsTab(
    //     state,
    //     dispatch,
    //     [
    //       EDimensionsTab.NETWORK,
    //       EDimensionsTab.OVERVIEW,
    //       EDimensionsTab[searchInfo.dimensionsSearchType.toLocaleUpperCase()],
    //     ],
    //     searchInfo,
    //   );
    // } else {
    //   jumpToDimensionsTab(
    //     state,
    //     dispatch,
    //     [
    //       EDimensionsTab.NETWORK,
    //       EDimensionsTab[searchInfo.dimensionsSearchType.toLocaleUpperCase()],
    //     ],
    //     searchInfo,
    //   );
    // }
    jumpToDimensionsTab(state, dispatch, [EDimensionsTab.NETWORK], searchInfo);
  }, [searchInfo]);

  return <div style={{ display: 'none' }} />;
}
