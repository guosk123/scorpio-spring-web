import React from 'react';
import { DHCP_VERSION_ENUM } from '@/common/app';
import DHCPAnalysisComponent from '../../components/DHCPAnalysisComponent';

interface IDHCPAnalysisProps {}

const DHCPAnalysis: React.FC<IDHCPAnalysisProps> = () => {
  return (
    <>
      <DHCPAnalysisComponent dhcpType={DHCP_VERSION_ENUM.DHCP} />
    </>
  );
};

export default DHCPAnalysis;
