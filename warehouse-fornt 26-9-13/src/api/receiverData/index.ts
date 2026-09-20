import server from '..'

const prefix = 'receiverData'

export type SensorMatrix = number[][]

class ReceiverDataApi {
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
