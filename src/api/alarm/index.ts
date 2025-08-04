
import server from '..'

const preFix = 'alarm'

export interface type_Alarm {
    id: number,
    houseNo: string,
    alertPos: string,
    alertType: string,
    alertLevel: string,
    alertTime: string,
    handle: string,
    isVerify: boolean,
    yuntu: string
}
export type AlarmArgDTO = {
    houseNO: string,
    houseName: string,
    common: number,
    serious: number,
    hot: number,
    dewAndMould: number,
    exception: number
}
class Alarm {
    list(level?: string[], type?: string[], verify?: string[], wareHouseID?: string, current?: number, size?: number) {
        return server.post<ResponseData<Page<type_Alarm>>>(preFix + `/${current}/${size}`, { level, type, verify, wareHouseID })
    }
    excel(level?: string[], type?: string[], verify?: string[], wareHouseID?: string) {
        return server.post(preFix + `/excel`, { level, type, verify, wareHouseID })
    }
    handle(id: number, handle: string) {
        return server.put<ResponseData<any>>(preFix, { id, handle })
    }
    arg(current?: number, size?: number) {
        return server.get<ResponseData<Page<AlarmArgDTO>>>(preFix + '/arg' + `/${current}/${size}`)
    }
}

export default new Alarm()