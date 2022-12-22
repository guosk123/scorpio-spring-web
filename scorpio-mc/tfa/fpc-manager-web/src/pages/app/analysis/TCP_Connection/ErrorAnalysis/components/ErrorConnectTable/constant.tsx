import { IP_ADDRESS_LOCALITY_ENUM } from '@/common/app';
import { EServiceType } from '@/pages/app/analysis/typings';
import numeral from 'numeral';

const dataIndexKeyFn = (keys: string[], serviceType: EServiceType) => {
  const dataIndexArr = keys;
  if (serviceType === EServiceType.TOTALSERVICE) {
    return dataIndexArr[0];
  } else if (serviceType === EServiceType.INTRANETSERVICE) {
    return dataIndexArr[1];
  } else {
    return dataIndexArr[2];
  }
};

export const tableColumns = (serviceType: EServiceType, tablePage: any) => {
  return [
    {
      title: '#',
      dataIndex: 'index',
      align: 'center',
      width: 40,
      fixed: 'left',
      render: (text: any, record: any, index: any) => {
        const number = index + 1;
        const { page, pageSize } = tablePage;
        if (!page) {
          return number;
        }
        return (page - 1) * Math.abs(pageSize!) + number;
      },
    },
    {
      title: 'IP地址',
      dataIndex: 'ipAddress',
      key: 'ipAddress',
      align: 'center',
      width: 150,
    },
    {
      title: 'IP位置',
      dataIndex: 'ipLocality',
      key: 'ipLocality',
      align: 'center',
      width: 100,
      render: (text: string) => IP_ADDRESS_LOCALITY_ENUM[text],
    },
    {
      title: '总建连失败次数',
      dataIndex: (() => {
        const dataIndexArr = [
          'tcpEstablishedFailCounts',
          'tcpEstablishedFailCountsInsideService',
          'tcpEstablishedFailCountsOutsideService',
        ];
        if (serviceType === EServiceType.TOTALSERVICE) {
          return dataIndexArr[0];
        } else if (serviceType === EServiceType.INTRANETSERVICE) {
          return dataIndexArr[1];
        } else {
          return dataIndexArr[2];
        }
      })(),
      align: 'center',
      sorter: true,
      width: 150,
    },
    {
      title: '服务端建连失败次数',
      dataIndex: (() => {
        const dataIndexArr = [
          'tcpServerEstablishedFailCounts',
          'tcpServerEstablishedFailCountsInsideService',
          'tcpServerEstablishedFailCountsOutsideService',
        ];
        if (serviceType === EServiceType.TOTALSERVICE) {
          return dataIndexArr[0];
        } else if (serviceType === EServiceType.INTRANETSERVICE) {
          return dataIndexArr[1];
        } else {
          return dataIndexArr[2];
        }
      })(),
      key: 'tcpServerEstablishedFailCounts',
      align: 'center',
      sorter: true,
      width: 150,
    },
    {
      title: '客户端建连失败次数',
      dataIndex: (() => {
        const dataIndexArr = [
          'tcpClientEstablishedFailCounts',
          'tcpClientEstablishedFailCountsInsideService',
          'tcpClientEstablishedFailCountsOutsideService',
        ];
        if (serviceType === EServiceType.TOTALSERVICE) {
          return dataIndexArr[0];
        } else if (serviceType === EServiceType.INTRANETSERVICE) {
          return dataIndexArr[1];
        } else {
          return dataIndexArr[2];
        }
      })(),
      key: 'tcpClientEstablishedFailCounts',
      align: 'center',
      sorter: true,
      width: 150,
    },
    {
      title: '总建连次数',
      dataIndex: (() => {
        const dataIndexArr = [
          'tcpEstablishedCounts',
          'tcpEstablishedCountsInsideService',
          'tcpEstablishedCountsOutsideService',
        ];
        if (serviceType === EServiceType.TOTALSERVICE) {
          return dataIndexArr[0];
        } else if (serviceType === EServiceType.INTRANETSERVICE) {
          return dataIndexArr[1];
        } else {
          return dataIndexArr[2];
        }
      })(),
      align: 'center',
      sorter: true,
      width: 150,
    },
    {
      title: '服务端建连次数',
      dataIndex: dataIndexKeyFn(
        [
          'tcpServerEstablishedCounts',
          'tcpServerEstablishedCountsInsideService',
          'tcpServerEstablishedCountsOutsideService',
        ],
        serviceType,
      ),
      key: 'tcpServerEstablishedCounts',
      align: 'center',
      sorter: true,
      width: 150,
    },
    {
      title: '客户端建连次数',
      dataIndex: dataIndexKeyFn(
        [
          'tcpClientEstablishedCounts',
          'tcpClientEstablishedCountsInsideService',
          'tcpClientEstablishedCountsOutsideService',
        ],
        serviceType,
      ),
      key: 'tcpClientEstablishedCounts',
      align: 'center',
      sorter: true,
      width: 150,
    },
    {
      title: '总建连失败率',
      dataIndex: (() => {
        const dataIndexArr = [
          'tcpEstablishedFailCountsRate',
          'tcpEstablishedFailCountsInsideServiceRate',
          'tcpEstablishedFailCountsOutsideServiceRate',
        ];
        if (serviceType === EServiceType.TOTALSERVICE) {
          return dataIndexArr[0];
        } else if (serviceType === EServiceType.INTRANETSERVICE) {
          return dataIndexArr[1];
        } else {
          return dataIndexArr[2];
        }
      })(),
      align: 'center',
      sorter: true,
      width: 150,
      render: (text = 0) => `${numeral((text * 100).toFixed(2)).value()}%`,
    },
    {
      title: '服务端建连失败率',
      dataIndex: (() => {
        const dataIndexArr = [
          'tcpServerEstablishedFailCountsRate',
          'tcpServerEstablishedFailCountsInsideServiceRate',
          'tcpServerEstablishedFailCountsOutsideServiceRate',
        ];
        if (serviceType === EServiceType.TOTALSERVICE) {
          return dataIndexArr[0];
        } else if (serviceType === EServiceType.INTRANETSERVICE) {
          return dataIndexArr[1];
        } else {
          return dataIndexArr[2];
        }
      })(),
      align: 'center',
      sorter: true,
      width: 150,
      render: (text = 0) => `${numeral((text * 100).toFixed(2)).value()}%`,
    },
    {
      title: '客户端建连失败率',
      dataIndex: dataIndexKeyFn(
        [
          'tcpClientEstablishedFailCountsRate',
          'tcpClientEstablishedFailCountsInsideServiceRate',
          'tcpClientEstablishedFailCountsOutsideServiceRate',
        ],
        serviceType,
      ),
      key: 'tcpClientEstablishedFailRate',
      align: 'center',
      sorter: true,
      width: 150,
      render: (text = 0) => `${numeral((text * 100).toFixed(2)).value()}%`,
    },
  ];
};
