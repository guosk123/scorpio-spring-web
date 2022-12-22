import { getTabDetail } from '@/pages/app/Network/components/EditTabs';
import { connect } from 'dva';
import { useContext, useMemo, useState } from 'react';
import { DimensionsSearchContext } from '../..';
import DimensionsChartCard from '../../../components/EditTab/components/DimensionsChartCard';
import DimensionsTable, {
  ESortDirection,
} from '../../../components/EditTab/components/DimensionsTable';
import type { ISearchBoxInfo } from '../../../components/SearchBox';
import { EDRILLDOWN } from '../../../typing';
import { jumpToDimensionsTab } from '../../constant';
import { EDimensionsTab } from '../../typing';

interface Props {
  searchBoxInfo: ISearchBoxInfo;
  onClickDirllDown?: any;
  shareRow?: any;
  drilldown?: EDRILLDOWN;
}

function FlowAnalysis() {
  const [state, dispatch] = useContext<any>(DimensionsSearchContext);
  const flowAnalysisDetail = useMemo(() => getTabDetail(state) || {}, []);
  const { searchBoxInfo, shareRow, drilldown = EDRILLDOWN.NOTDRILLDOWN } = flowAnalysisDetail;
  const [sortCol, setSortCol] = useState<string>('totalBytes');
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  const [selectedRow, setSelectedRow] = useState<any>(shareRow);
  return (
    <div>
      <DimensionsChartCard
        searchBoxInfo={searchBoxInfo}
        sortProperty={sortCol}
        sortDirection={sortDirection}
        cancelRow={setSelectedRow}
        selectedRow={selectedRow}
        drilldown={drilldown}
      />
      <DimensionsTable
        searchBoxInfo={searchBoxInfo}
        tableSortDirection={setSortDirection}
        onClickDirllDown={(e: ISearchBoxInfo) => {
          console.log('dirlldown', e);
          jumpToDimensionsTab(state, dispatch, EDimensionsTab.FLOWANALYSIS, e);
          // onClickDirllDown({ e, selectedRow, tabType: searchBoxInfo.dimensionsSearchType });
        }}
        clickRow={(e: React.MouseEvent<HTMLElement, MouseEvent>, row: any) => {
          if (selectedRow) {
            setSelectedRow(undefined);
          } else {
            setSelectedRow(row);
          }
        }}
        tableSortProperty={setSortCol}
        drilldown={drilldown}
      />
    </div>
  );
}

export default connect()(FlowAnalysis);
