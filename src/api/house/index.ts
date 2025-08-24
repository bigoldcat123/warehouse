import server from "..";

const preFix = 'house'
export type type_House = {
    id?:number,
    houseNo:string,
    houseName:string,
    warehouseID:number,
    houseAddr:string,
    houseType:'平房仓' | '筒仓' | string,
    z:number,
    x:number,
    y:number
    yuntu:string,
    quxian:string,
    threeD:string,
    valeWin:string,
    tongfengLx:string,
    tongfengZt:string,
    tongfengTu:string,
    TFModeWin:string,
    TongFengSst:string,
}
export type TongfengInfo = {
    tfmodeWin:string,
    tongFengSst:string,
    tongfengLx:string,
    tongfengTu:string,
    tongfengZt:string
}
class House  {

    list(current:number,size:number,wareHouseId:string,houseNo:string) {
        return server.get<ResponseData<Page<type_House>>>(preFix,{
            params:{
                current,
                size,
                wareHouseId,
                houseNo
            }
        } )
    }
    add(house:type_House) {
        return server.post(preFix,house)
    }
    deleteById(id:number) {
        return server.delete(`${preFix}/${id}`)
    }
    update(house:type_House) {
        return server.put(preFix,house)
    }
    kv() {
        return server.get(preFix + '/kv')
    }
    findByWarehouseId(id:number) {
        return server.get(preFix + '/kv/' + id)
    }
    getTongFengInfo(id:number) {
        return server.get<ResponseData<TongfengInfo>>(preFix + "/tongfeng?houseId="+id)
    }
}

export default new House()