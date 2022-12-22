export const base64Code =  function () {
  var _PADCHAR = '=',
    _ALPHA = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/',
    _VERSION = '1.1';
  function _getbyte64(s, i) {
    var idx = _ALPHA.indexOf(s.charAt(i));
    if (idx === -1) {
      console.error('无法base64解码')
    }
    return idx;
  }
  function _decode_chars(y, x) {
    while (y.length > 0) {
      var ch = y[0];
      if (ch < 128) {
        y.shift();
        x.push(String.fromCharCode(ch));
      } else {
        if ((ch & 128) == 192) {
          if (y.length < 2) {
            break;
          }
          ch = y.shift();
          var ch1 = y.shift();
          x.push(String.fromCharCode(((ch & 31) << 6) + (ch1 & 63)));
        } else {
          if (y.length < 3) {
            break;
          }
          ch = y.shift();
          var ch1 = y.shift();
          var ch2 = y.shift();
          x.push(
            String.fromCharCode(
              ((ch & 15) << 12) + ((ch1 & 63) << 6) + (ch2 & 63)
            )
          );
        }
      }
    }
  }
  function _decode(s) {
    var pads = 0,
      i,
      b10,
      imax = s.length,
      x = [],
      y = [];
    s = String(s);
    if (imax === 0) {
      return s;
    }
    if (imax % 4 !== 0) {
      console.error('无法base64解码')
    }
    if (s.charAt(imax - 1) === _PADCHAR) {
      pads = 1;
      if (s.charAt(imax - 2) === _PADCHAR) {
        pads = 2;
      }
      imax -= 4;
    }
    for (i = 0; i < imax; i += 4) {
      var ch1 = _getbyte64(s, i);
      var ch2 = _getbyte64(s, i + 1);
      var ch3 = _getbyte64(s, i + 2);
      var ch4 = _getbyte64(s, i + 3);
      b10 =
        (_getbyte64(s, i) << 18) |
        (_getbyte64(s, i + 1) << 12) |
        (_getbyte64(s, i + 2) << 6) |
        _getbyte64(s, i + 3);
      y.push(b10 >> 16);
      y.push((b10 >> 8) & 255);
      y.push(b10 & 255);
      _decode_chars(y, x);
    }
    switch (pads) {
      case 1:
        b10 =
          (_getbyte64(s, i) << 18) |
          (_getbyte64(s, i + 1) << 12) |
          (_getbyte64(s, i + 2) << 6);
        y.push(b10 >> 16);
        y.push((b10 >> 8) & 255);
        break;
      case 2:
        b10 = (_getbyte64(s, i) << 18) | (_getbyte64(s, i + 1) << 12);
        y.push(b10 >> 16);
        break;
    }
    _decode_chars(y, x);
    if (y.length > 0) {
      console.error('无法base64解码')
    }
    return x.join('');
  }
  function _get_chars(ch, y) {
    if (ch < 128) {
      y.push(ch);
    } else {
      if (ch < 2048) {
        y.push(192 + ((ch >> 6) & 31));
        y.push(128 + (ch & 63));
      } else {
        y.push(224 + ((ch >> 12) & 15));
        y.push(128 + ((ch >> 6) & 63));
        y.push(128 + (ch & 63));
      }
    }
  }
  function _encode(s) {
    if (arguments.length !== 1) {
      console.error('无法base64解码');
    }
    s = String(s);
    if (s.length === 0) {
      return s;
    }
    var i,
      b10,
      y = [],
      x = [],
      len = s.length;
    i = 0;
    while (i < len) {
      _get_chars(s.charCodeAt(i), y);
      while (y.length >= 3) {
        var ch1 = y.shift();
        var ch2 = y.shift();
        var ch3 = y.shift();
        b10 = (ch1 << 16) | (ch2 << 8) | ch3;
        x.push(_ALPHA.charAt(b10 >> 18));
        x.push(_ALPHA.charAt((b10 >> 12) & 63));
        x.push(_ALPHA.charAt((b10 >> 6) & 63));
        x.push(_ALPHA.charAt(b10 & 63));
      }
      i++;
    }
    switch (y.length) {
      case 1:
        var ch = y.shift();
        b10 = ch << 16;
        x.push(
          _ALPHA.charAt(b10 >> 18) +
            _ALPHA.charAt((b10 >> 12) & 63) +
            _PADCHAR +
            _PADCHAR
        );
        break;
      case 2:
        var ch1 = y.shift();
        var ch2 = y.shift();
        b10 = (ch1 << 16) | (ch2 << 8);
        x.push(
          _ALPHA.charAt(b10 >> 18) +
            _ALPHA.charAt((b10 >> 12) & 63) +
            _ALPHA.charAt((b10 >> 6) & 63) +
            _PADCHAR
        );
        break;
    }
    return x.join('');
  }
  return { decode: _decode, encode: _encode, VERSION: _VERSION };
}