import AccessBox from '@/pages/frame/system/MenuEdit/components/AccessBox';
import useComponentsAccess, {
  IGNORE_KEY,
} from '@/pages/frame/system/MenuEdit/hooks/useComponentsAccess';
import { DownloadOutlined } from '@ant-design/icons';
import { Button, Dropdown, Menu } from 'antd';
import { packetFileType } from '../../../components/DownLoadPktBtn';

interface Props {
  disabled: boolean;
  onClick: any;
  accessKey?: string;
}

export default function DownLoadPktFileBtn(props: Props) {
  const { disabled, onClick = () => {}, accessKey = IGNORE_KEY } = props;
  const access = useComponentsAccess(accessKey);
  // const downloadPKTFileTypeRef = useRef(EPacketFileType.PCAP);
  return (
    <AccessBox access={access ? true : false}>
      <Dropdown
        disabled={disabled}
        overlay={
          <Menu
            onClick={(e) => {
              // downloadPKTFileTypeRef.current = packetFileType[e.key].key;
              onClick({ fileType: packetFileType[e.key].key });
            }}
          >
            {Object.keys(packetFileType).map((pktKey) => {
              const pktItem = packetFileType[pktKey];
              return <Menu.Item key={pktItem.key}>{pktItem.label}</Menu.Item>;
            })}
          </Menu>
        }
        trigger={['click']}
      >
        <Button type="primary" disabled={disabled} icon={<DownloadOutlined />} size="small">
          下载 PCAP
        </Button>
      </Dropdown>
    </AccessBox>
  );
}
