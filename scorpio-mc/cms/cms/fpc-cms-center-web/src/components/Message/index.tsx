import type { ReactNode } from 'react';
import React from 'react';
import styles from './index.less';

interface MessageProps {
  title?: string | ReactNode;
  height?: number;
  message?: string | ReactNode;
}

const Message: React.FC<MessageProps> = ({ title, height, message = '暂无数据' }) => {
  return (
    <div className={styles.message}>
      {title && <div className={styles.message_title}>{title}</div>}
      <div className={styles.message_content} style={{ height: height || '100%' }}>
        <span className={styles.message_content_message}>{message}</span>
      </div>
    </div>
  );
};

export default Message;
