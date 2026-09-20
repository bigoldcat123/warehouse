import server from "..";

const PREFIX = "gfkt";

export interface GfktItem {
  airHourElec: number;   // 每小时空调用电量
  meterElec: number;     // 电表读数
  pvElec: number;        // 光伏发电量
  houseName: string;     // 仓号 / 房间名
}

class Gfkt {
  list() {
    return server.get<ResponseData<GfktItem[]>>(`${PREFIX}`)
  }
  getOneByHouseNo(houseNo:string) {
    return server.get<ResponseData<GfktItem>>(`${PREFIX}/${houseNo}`)
  }
}
export default new Gfkt();
