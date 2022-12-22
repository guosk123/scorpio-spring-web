// import type { ConnectState } from '@/models/connect';
// import { Card, Empty, Skeleton } from 'antd';
// import React, { useEffect } from 'react';
// import type { Dispatch } from 'umi';
// import { useParams } from 'umi';
// import { connect } from 'umi';
// import type { IAbnormalEventRule } from '../../typings';
// import RuleForm from '../components/RuleForm';

// interface IUpdateAbnormalEventRuleProps {
//   dispatch: Dispatch;
//   queryLoading: boolean | undefined;
//   abnormalEventRuleDetail?: IAbnormalEventRule;
// }

// const UpdateAbnormalEventRule: React.FC<IUpdateAbnormalEventRuleProps> = ({
//   dispatch,
//   queryLoading,
//   abnormalEventRuleDetail = {} as IAbnormalEventRule,
// }) => {
//   const params: { eventRuleId: string } = useParams();
//   useEffect(() => {
//     dispatch({
//       type: 'abnormalEventModel/queryAbnormalEventRuleDetail',
//       payload: {
//         id: params.eventRuleId,
//       },
//     });
//   }, [dispatch, params.eventRuleId]);

//   return (
//     <Card bordered={false}>
//       <Skeleton active loading={queryLoading}>
//         {abnormalEventRuleDetail.id ? (
//           <RuleForm detail={abnormalEventRuleDetail} />
//         ) : (
//           <Empty description="自定义异常事件不存在或已被删除" />
//         )}
//       </Skeleton>
//     </Card>
//   );
// };

// export default connect(
//   ({ loading: { effects }, abnormalEventModel: { abnormalEventRuleDetail } }: ConnectState) => ({
//     queryLoading: effects['abnormalEventModel/queryAbnormalEventRuleDetail'],
//     abnormalEventRuleDetail,
//   }),
// )(UpdateAbnormalEventRule);
