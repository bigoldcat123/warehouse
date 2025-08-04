/// <reference types="vite/client" />
/**
 * new config.
 */

////////////////////////////////////////////////
//com.example.demo.common.R
declare type ResponseData<T> = {
    code:string,
    message:string,
    show:boolean,
    value:T
}

declare type Page<T> = {
    records?: T[],
    total?: number,
    size?: number,
    current?: number,
    pages?: number
}

//com.example.demo.common.CurrentUser
declare type UserDetail = {
    id:string
    name: string
     sex:string;
    companyID:number;
     position:string;
     phone:string;
     priv:string;
     username:string
}
//com.example.demo.common.CurrentUserVo
declare type CurrentUser = {
    token:string,
    detail:UserDetail
}

declare type DaoLoginUser = {
    username: string
    password: string
}
declare type MailLoginUser = {
    email: string
    code: string
}