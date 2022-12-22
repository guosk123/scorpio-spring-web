import type { IUriParams } from '@/pages/app/analysis/typings';
import type { Dispatch } from 'react';
import { createContext } from 'react';
import { useParams, history } from 'umi';
import type { IAction, IState } from './components/EditTabs';
import EditTabs from './components/EditTabs';
import LinkToAnalysis from './components/LinkToAnalysis';
import { MetadataTabs } from './constant';
import { EMetadataTabType } from './typings';

export const MetaDataContext = createContext<[IState, Dispatch<IAction>]>([
  { panes: [], shareInfo: null, activeKey: '' },
  () => {},
]);

export enum EditTabContentType {
  'ANALYSIS' = 'analysis',
  'RECORD' = 'record',
}

interface Props {
  contentType?: EditTabContentType;
  paneTitle?: string;
}

export default function Metadata(props: Props) {
  const { pcapFileId }: IUriParams = useParams();
  const { paneTitle, contentType } = props;
  return (
    <EditTabs
      tabs={(() => {
        if (contentType === EditTabContentType.ANALYSIS) {
          return {
            [EMetadataTabType.OVERVIEW]: MetadataTabs.overview,
            [EMetadataTabType.HTTPANALYSIS]: MetadataTabs.httpAnalysis,
            [EMetadataTabType.DHCPANALYSIS]: MetadataTabs.dhcpAnalysis,
            [EMetadataTabType.DHCPV6ANALYSIS]: MetadataTabs.dhcpv6Analysis,
          };
        }
        if (contentType === EditTabContentType.RECORD) {
          const tabKeys = Object.keys(MetadataTabs).filter((key) => {
            return ![
              EMetadataTabType.OVERVIEW,
              EMetadataTabType.HTTPANALYSIS,
              EMetadataTabType.DHCPANALYSIS,
              EMetadataTabType.DHCPV6ANALYSIS,
            ].includes(key as any);
          });
          const tmpMetadataTabs = {};
          tabKeys.forEach((key) => {
            tmpMetadataTabs[key] = MetadataTabs[key];
          });
          return tmpMetadataTabs;
        }
        if (pcapFileId) {
          const tabKeys = Object.keys(MetadataTabs).filter((key) => {
            return ![
              EMetadataTabType.HTTPANALYSIS,
              EMetadataTabType.DHCPANALYSIS,
              EMetadataTabType.DHCPV6ANALYSIS,
            ].includes(key as any);
          });
          const tmpMetadataTabs = {};
          tabKeys.forEach((key) => {
            tmpMetadataTabs[key] = MetadataTabs[key];
          });
          return tmpMetadataTabs;
        } else {
          return MetadataTabs;
        }
      })()}
      loading={history.location.query?.jumpTabs ? true : false}
      consumerContext={MetaDataContext}
      linkToTab={<LinkToAnalysis />}
      destroyInactiveTabPane={true}
      dirTabName={paneTitle}
      showTabSettingTool={true}
      tabsKey="metadata-analysis"
      cancelQueryOnChange={true}
    />
  );
}
