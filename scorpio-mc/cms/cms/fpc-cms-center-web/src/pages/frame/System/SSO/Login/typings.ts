export interface IJwt {
  jwt: string;
}

export interface ISsoLoginData extends IJwt {}
export interface ISsoUserBindData extends IJwt {
  username: string;
  password: string;
  code: string;
  token: string;
}
export interface ISsoUserRegisterData extends IJwt {
  name: string;
  fullname: string;
  email: string;
  password: string;
  token: string;
}
