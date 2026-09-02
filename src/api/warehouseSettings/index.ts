import server from "..";

const preFix = 'warehouseSettings'
export type type_WarehouseSettings = {
    id?: number,
    houseNo: string,
    temperatureMax: number | null,
    temperatureCollectTime: string,
    temperatureIntervalHours: number | null,
    humidityMax: number | null,
    humidityCollectTime: string,
    humidityIntervalHours: number | null,
    gasMax: number | null,
    gasCollectTime: string,
    gasIntervalHours: number | null,
    createTime?: string,
    updateTime?: string
}
class WarehouseSettings {
    list(current: number, size: number, houseNo: string) {
        return server.get<ResponseData<Page<type_WarehouseSettings>>>(preFix, {
            params: {
                current,
                size,
                houseNo
            }
        })
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