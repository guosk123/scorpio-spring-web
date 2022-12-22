export function getCookie(name: string) {
  const arrStr = document.cookie.split('; ');

  for (let i = 0; i < arrStr.length; i += 1) {
    const temp = arrStr[i].split('=');
    if (temp[0] === name) {
      return unescape(temp[1]);
    }
  }
  return '';
}
