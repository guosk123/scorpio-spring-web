import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { FlowNetworkContext } from '@/pages/app/GlobalSearch/DimensionsSearch/SeartchTabs/components/FlowNetwork';
import { DimensionsTypeToFlowFilterMap } from '@/pages/app/GlobalSearch/DimensionsSearch/typing';
import { useContext, useEffect } from 'react';

interface Props {
  dimensionsNetworkPayload: any;
  globalSelectedTime: any;
  sortProperty: any;
  sortDirection: any;
  dslPayload: any;
  flowAnalysisDetail: any;
}

export default function InDimensionsPayload(props: Props) {
  const {
    dimensionsNetworkPayload,
    globalSelectedTime,
    sortDirection,
    sortProperty,
    dslPayload,
    flowAnalysisDetail,
  } = props;

  const [, setPayload] = useContext(FlowNetworkContext);

  useEffect(() => {
    console.log('setpayload');
    setPayload(dimensionsNetworkPayload);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [globalSelectedTime, sortProperty, sortDirection]);
  return <div style={{ display: 'none' }} />;
}
