import server from "..";

const preFix = 'warehouse'
export type type_WareHouse = {
    id?:number,
    warehouseNo:string,
    warehouseName:string,
    warehouseAddress:string
}
class WareHouse {
     list(current:number,size:number) { 
        return server.get<ResponseData<Page<type_WareHouse>>>(preFix + `?current=${current}&size=${size}`)
    }
    add(warehouse:type_WareHouse) {
        return server.post(preFix,warehouse)
    }
    deleteById(id:number) {
        return server.delete(`${preFix}/${id}`)
    }
    update(warehouse:type_WareHouse) {
        return server.put(preFix,warehouse)
    }
    kv() {
        return server.get(preFix + '/kv')
    }
    belongKv(){
        return server.get(preFix + '/belong/kv')
    }
    getWarehouseIdBy(houseID:number) {
        return server.get(preFix + '/getWarehouseIdBy/' + houseID)
    }
}

export default new WareHouse()