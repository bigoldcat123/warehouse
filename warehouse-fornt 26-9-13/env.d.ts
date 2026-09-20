/// <reference types="vite/client" />

interface ImportMetaEnv {
    readonly ENV_PANORAMA_HOUSE_NUMBERS: string
}

interface ImportMeta {
    readonly env: ImportMetaEnv
}
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
declare type MqttDeviceInfoResponse = {
  mid:string,
  devices:Array<{
    serciceId:string,
    data:{
      warehouse_code:number, // 这个是 house_no
      warehouse_name:string, // 这个是 house_name
      PV_elec:number,
      air_data:Array<{
        air_assetCode:string,
        air_hour_elec:number
      }>,
      meter_data:Array<{
        meter_name:string,
        meter_addr:string,
        meter_installAddr:string,
        meter_elec:number
      }>,
      eventTime:string
    }
  }>
}
