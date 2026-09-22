import server from "..";

const preFix = 'warehouseSettings'
export type type_WarehouseSettings = {
    id?: number,
    houseNo: string,
    temperatureCollectTime: string,
    temperatureIntervalHours: number | null,
    humidityCollectTime: string,
    humidityIntervalHours: number | null,
    gasCollectTime: string,
    gasIntervalHours: number | null,
    createTime?: string,
    updateTime?: string
}
export type type_UnsetHouse = {
    id: number,
    houseNo: string,
    houseName: string,
    warehouseID: number
}
class WarehouseSettings {
    list(current: number, size: number, houseNo: string, warehouseID?: string | number) {
        return server.get<ResponseData<Page<type_WarehouseSettings>>>(preFix, {
            params: {
                current,
                size,
                houseNo,
                warehouseID
            }
        })
    }
    unset(warehouseId: string | number) {
        return server.get<ResponseData<type_UnsetHouse[]>>(`${preFix}/unset/${warehouseId}`)
    }
    add(settings: type_WarehouseSettings) {
        return server.post(preFix, settings)
    }
    update(settings: type_WarehouseSettings) {
        return server.put(preFix, settings)
    }
    deleteById(id: number) {
        return server.delete(`${preFix}/${id}`)
    }
}

export default new WarehouseSettings()
