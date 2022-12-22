import { EMetadataProtocol } from '../typings';
import Mail from '../Mail';

interface Props {
  paneTitle?: string;
}

const MetadataSMTP = (props: Props) => {
  const { paneTitle } = props;
  return <Mail entry={paneTitle || ''} protocol={EMetadataProtocol.SMTP} />;
};

export default MetadataSMTP;
