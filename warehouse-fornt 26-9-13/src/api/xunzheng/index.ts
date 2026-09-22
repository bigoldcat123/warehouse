import server from '..'

const preFix = 'xunzheng'

export type type_Xunzheng = {
  id?: number
  houseNo: string
  startDt: string
  stopDt: string | null
  setDatetime?: string
}

class Xunzheng {
  list(current: number, size: number, warehouseID: string, houseNo?: string) {
    return server.get<ResponseData<Page<type_Xunzheng>>>(preFix, {
      params: { current, size, warehouseID, houseNo }
    })
  }

  add(record: type_Xunzheng) {
    return server.post<ResponseData<unknown>>(preFix, record)
  }

  update(record: type_Xunzheng) {
    return server.put<ResponseData<unknown>>(preFix, record)
  }

  deleteById(id: number) {
    return server.delete<ResponseData<unknown>>(`${preFix}/${id}`)
  }
}

export default new Xunzheng()
