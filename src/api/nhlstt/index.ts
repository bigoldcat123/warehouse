import server from "..";
const preFix = 'nhlstt'
export interface Nhlstt {
  id?: number;              // 主键，自增ID
  houseNo?: string;         // 房间号
  wareHouseNo?: string;     // 仓库号
  inH?: number;             // 仓湿
  inT?: number;             // 仓温
  gt?: number;              // 粮温
  hiTemp?: number;          // 开机温度（高）
  loTemp?: number;          // 开机温度（低）
  tmTest?: string;          // 检测时间 (ISO时间字符串)
  run?: boolean;            // 1：开机；0：关机
  houseName:string
}
class Nhlsst {
  getall() {
    return server.get<ResponseData<Nhlstt[]>>(`${preFix}`);
  }
}
export default new Nhlsst();
