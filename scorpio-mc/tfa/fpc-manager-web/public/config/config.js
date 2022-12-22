(() => {
  const disablePaths = []
  disablePaths.push('/configuration/objects/ip-label');
  disablePaths.push('/analysis/security/mail-login');
  disablePaths.push('/configuration/safety-analysis/mail-login')
  // disablePaths.push('/analysis/security')
  // disablePaths.push('/configuration/safety-analysis')
  window.disablePath = disablePaths;
  // window.redirectUrl = '/analysis/performance/list/network'
})()
