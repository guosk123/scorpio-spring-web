/** 验证邮箱信息 */
const checkMail = (mail: string) => {
  return /^\n?[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/.test(mail);
};

/** 验证ipv4信息 */
const checkIpv4 = (ip: string) => {
  return /^\n?((25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))$/.test(
    ip,
  );
};
/** 验证ipv6信息 */
const checkIpv6 = (ip: string) => {
  return /^\n?([a-f0-9]{1,4}(:[a-f0-9]{1,4}){7}|[a-f0-9]{1,4}(:[a-f0-9]{1,4}){0,7}::[a-f0-9]{0,4}(:[a-f0-9]{1,4}){0,7})$/.test(
    ip,
  );
};
/** 验证端口号 */
const checkPort = (port: string) => {
  return /^([0-9]|[1-9]\d|[1-9]\d{2}|[1-9]\d{3}|[1-5]\d{4}|6[0-4]\d{3}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/.test(
    port,
  );
};

/** 验证邮件服务器 */
const checkSmtp = (addr: string) => {
  return /^([pP][oO][pP][3]|[sS][mM][tT][pP]|[iI][mM][aA][pP]){1}\.[\w|.]+\.[cC]([oO][mM]|[nN])$/.test(
    addr,
  );
};

export const validateIp = (_: any, value: string, callback: any) => {
  if (!value) {
    callback('请输入IP地址');
  }
  if (!checkIpv4(value) && !checkIpv6(value)) {
    callback(value + '格式错误');
  }
  callback();
};

export const validateMail = (_: any, value: string, callback: any) => {
  if (!value) {
    callback();
  }
  if (!checkMail(value)) {
    callback('邮箱格式错误!');
  }
  callback();
};

/** 验证多个邮件 */
export const validateMailList = (_: any, value: string, callback: any) => {
  if (!value) {
    callback();
    return;
  }
  const mailList = value
    .replace(/\n/g, '')
    .split(',')
    .filter((mail) => mail !== '');
  for (let i = 0; i < mailList.length; i++) {
    const mail = mailList[i];
    if (mail !== '' && !checkMail(mail)) {
      callback(mail + '邮箱格式错误!');
    }
    if (mailList.slice(0, i).find((preMail: string) => preMail === mail)) {
      callback(mail + '邮箱输入重复!');
    }
  }
  mailList.forEach((mail) => {
    if (mail !== '' && !checkMail(mail)) {
      callback(mail + '邮箱格式错误!');
    }
  });
  callback();
};

/** 验证端口号 */
export const validatePort = (_: any, value: string, callback: any) => {
  if (value === '') {
    callback();
  }
  if (!checkPort(value)) {
    callback(`端口号:${value}格式错误!`);
  }
  callback();
};

/** 验证IP:port */
export const validateCompleteAddress = (_: any, value: string, callback: any) => {
  if (value === '') {
    callback();
  }
  const [ip, port] = value.split(':');
  if (!ip || !port) {
    callback(`地址:${value}格式有误!`);
    return;
  }
  if (ip !== '' && !checkIpv4(ip) && !checkIpv6(ip)) {
    callback(`IP地址:${ip}格式有误!`);
    return;
  }
  if (port !== '' && !checkPort(port)) {
    callback(`端口号:${port}输入有误!`);
    return;
  }
  callback();
};

/** 验证多个完整地址 */
export const validateCompleteAddressList = (_: any, value: string, callback: any) => {
  if (value === '') {
    callback();
  }
  const addressList = value.split('\n');
  addressList.forEach((address) => {
    const [ip, port] = address.split(':');
    if (!ip || !port) {
      callback(`地址:${address}格式有误!`);
      return;
    }
    if (ip !== '' && !checkIpv4(ip) && !checkIpv6(ip)) {
      callback(`IP地址:${ip}格式有误!`);
      return;
    }
    if (port !== '' && !checkPort(port)) {
      callback(`端口号:${port}输入有误!`);
      return;
    }
  });
  callback();
};

/** 验证smtp服务器 */
export const validateSmtpServer = (_: any, value: string, callback: any) => {
  if (value === '') {
    callback();
  }
  if (!checkSmtp(value)) {
    callback(`邮件服务器地址:${value}格式错误!`);
  }
  callback();
};
