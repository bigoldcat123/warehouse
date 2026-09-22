import server from '..'

const prefix = 'receiverData'

export type SensorMatrix = number[][]

export type LatestPanoramaInfo = {
  houseNo: string
  oAir: number | null
  co2Air: number | null
  ph3: string | null
  testDate: string | null
}

class ReceiverDataApi {
  latest(houseNo: string) {
    return server.get<ResponseData<LatestPanoramaInfo | null>>(`${prefix}/latest`, {
      params: { houseNo },
    })
  }

  testDates(houseNo: string) {
    return server.get<ResponseData<string[]>>(`${prefix}/testDates`, {
      params: { houseNo },
    })
  }

  temperature(houseNo: string, testDate: string) {
    return server.get<ResponseData<SensorMatrix>>(`${prefix}/temperature`, {
      params: { houseNo, testDate },
    })
  }

  humidity(houseNo: string, testDate: string) {
    return server.get<ResponseData<SensorMatrix>>(`${prefix}/humidity`, {
      params: { houseNo, testDate },
    })
  }

  ph3(houseNo: string, testDate: string) {
    return server.get<ResponseData<SensorMatrix>>(`${prefix}/ph3`, {
      params: { houseNo, testDate },
    })
  }
}

export default new ReceiverDataApi()
