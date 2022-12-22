import { DHCP_VERSION_ENUM } from '@/common/app';
import React from 'react';
import DHCPAnalysisComponent from '../../components/DHCPAnalysisComponent';

interface IDHCPAnalysisProps {}

const DHCPV6Analysis: React.FC<IDHCPAnalysisProps> = () => {
  return <DHCPAnalysisComponent dhcpType={DHCP_VERSION_ENUM.DHCPv6} />;
};

export default DHCPV6Analysis;
