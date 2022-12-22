import { connect } from 'dva';
import { useState } from 'react';
import DimensionsLineChart from '../DimensionsChartCard';
import DimensionsTable, { ESortDirection } from '../DimensionsTable';
import type { ISearchBoxInfo } from '../../../SearchBox';
import { EDRILLDOWN } from '../../../../typing';

interface Props {
  searchBoxInfo: ISearchBoxInfo;
  onClickDirllDown?: any;
  shareRow?: any;
  drilldown?: EDRILLDOWN;
}

function TabContent(props: Props) {
  const { searchBoxInfo, onClickDirllDown, shareRow, drilldown = EDRILLDOWN.NOTDRILLDOWN } = props;
  const [sortCol, setSortCol] = useState<string>('totalBytes');
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  const [selectedRow, setSelectedRow] = useState<any>(shareRow);
  return (
    <div>
      <DimensionsLineChart
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
          onClickDirllDown({ e, selectedRow, tabType: searchBoxInfo.dimensionsSearchType });
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

export default connect()(TabContent);
