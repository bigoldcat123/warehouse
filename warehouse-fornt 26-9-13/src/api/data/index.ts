import server from '..'

const preFix = 'data'
export type type_Data = {
            id : number,
            HouseNo : string,
             houseNo : string,// 与上面的HouseNo 是同一个东西
             houseName : string,
             house_type:string,
             layerMax : string,
             layerMin : string,
             layerAvg : string,
             housePh3 : string,
             oAir : number,
             co2Air : number,
             testDate : string
}

export type type_Data_Detail = {
       houseNo:string,

       houseName:string,

       wareHouseName:string,

       breed:string;

       water:string;

       keeper:string;

       inTemperature:string;

       outTemperature:string;

       inHumidity:string;

       outHumidity:string;
       entryTime:string;
       list:Array<Array<Array<string>>>,
       testTime:string
}

// 三维温度数组: key 为采集时间(ISO-8601, 升序), value 为 [层 z][行 x][列 y] 的字符串温度(1 位小数)
// "-999" 表示无效点位; 粮房不存在或无数据时为空对象 {}
export type type_TemperatureCube = Record<string, string[][][]>

// 温度记录：指定点 / 指定层平均 / 全仓平均 共用同一结构
export type type_TempRecord = {
    testDate: string  // 检测时间 yyyy-MM-dd HH:mm:ss
    temp: number      // 温度值（摄氏度）
}

class Data {
    list(from?:string | null,to?:string| null,houseName?:string| null,warehouseName?:string| null,current?:number,size?:number) {
        return server.post<ResponseData<Page<type_Data>>>(preFix + `/${current}/${size}`,{from,to,houseName,warehouseName})
    }
    getDetil(id:number) {
        return server.get<ResponseData<type_Data_Detail>>(`${preFix}/${id}`)
    }

    // 查询指定点温度记录（按三维坐标 层/行/列）
    tempRecords(houseNo: string, ceng: number, hang: number, lie: number) {
        return server.get<ResponseData<type_TempRecord[]>>(`${preFix}/tempRecords`, {
            params: { houseNo, ceng, hang, lie }
        })
    }

    // 查询指定层平均温度
    layerAvgTemp(houseNo: string, ceng: number) {
        return server.get<ResponseData<type_TempRecord[]>>(`${preFix}/layerAvgTemp`, {
            params: { houseNo, ceng }
        })
    }

    // 查询全部点平均温度
    allAvgTemp(houseNo: string) {
        return server.get<ResponseData<type_TempRecord[]>>(`${preFix}/allAvgTemp`, {
            params: { houseNo }
        })
    }

    // 查询指定点湿度记录（按三维坐标 层/行/列；当前为随机模拟数据）
    humidityRecords(houseNo: string, ceng: number, hang: number, lie: number) {
        return server.get<ResponseData<type_TempRecord[]>>(`${preFix}/humidityRecords`, {
            params: { houseNo, ceng, hang, lie }
        })
    }

    // 查询指定层平均湿度（当前为随机模拟数据）
    layerAvgHumidity(houseNo: string, ceng: number) {
        return server.get<ResponseData<type_TempRecord[]>>(`${preFix}/layerAvgHumidity`, {
            params: { houseNo, ceng }
        })
    }

    // 查询全部点平均湿度（当前为随机模拟数据）
    allAvgHumidity(houseNo: string) {
        return server.get<ResponseData<type_TempRecord[]>>(`${preFix}/allAvgHumidity`, {
            params: { houseNo }
        })
    }

    // 查询指定点气体浓度记录（按三维坐标 层/行/列）
    gasRecords(houseNo: string, ceng: number, hang: number, lie: number) {
        return server.get<ResponseData<type_TempRecord[]>>(`${preFix}/gasRecords`, {
            params: { houseNo, ceng, hang, lie }
        })
    }

    // 查询指定层平均气体浓度
    layerAvgGas(houseNo: string, ceng: number) {
        return server.get<ResponseData<type_TempRecord[]>>(`${preFix}/layerAvgGas`, {
            params: { houseNo, ceng }
        })
    }

    // 查询全部点平均气体浓度
    allAvgGas(houseNo: string) {
        return server.get<ResponseData<type_TempRecord[]>>(`${preFix}/allAvgGas`, {
            params: { houseNo }
        })
    }

    // 根据粮房编号获取每个采集时间点的三维温度数组(公开接口, 无需鉴权)
    temperatureCube(houseNo: string) {
        return server.get<ResponseData<type_TemperatureCube>>(`${preFix}/temperatureCube`, {
            params: { houseNo }
        })
    }
    // 根据粮房编号获取每个采集时间点的湿度三维数组(公开接口, 无需鉴权, 当前为随机模拟数据)
    humidityCube(houseNo: string) {
        return server.get<ResponseData<type_TemperatureCube>>(`${preFix}/humidityCube`, {
            params: { houseNo }
        })
    }
    // 根据粮房编号获取每个采集时间点的气体浓度三维数组(公开接口, 无需鉴权, 当前为随机模拟数据)
    gasCube(houseNo: string) {
        return server.get<ResponseData<type_TemperatureCube>>(`${preFix}/gasCube`, {
            params: { houseNo }
        })
    }
}

export default new Data()
