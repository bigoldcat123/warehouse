import server from '..'

const preFix = 'data'
export type type_Data = {
            id : number,
            HouseNo : string,
            houseNo : string,// 与上面的HouseNo 是同一个东西
             houseName : string,
             house_type:string,
             inTemperature : number,
             inHumidity : number,
             maxTemperature : number,
             minTemperature : number,
             avgTemperature : number,
             layerMax : string,
             layerMin : string,
             layerAvg : string,
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
class Data {
    list(from?:string | null,to?:string| null,houseName?:string| null,warehouseName?:string| null,current?:number,size?:number) {
        return server.post<ResponseData<Page<type_Data>>>(preFix + `/${current}/${size}`,{from,to,houseName,warehouseName})
    }
    getDetil(id:number) {
        return server.get<ResponseData<type_Data_Detail>>(`${preFix}/${id}`)
    }
}

export default new Data()
