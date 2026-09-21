import server from "..";

const preFix = 'entry'

export interface type_Entry {
    id?:number
    houseID : number,
    breed:string
    entryTime:string
    water:number
    stockman:number
    entryUserId:number
}
type EntryQuery = {
    warehouseId?:string,
    houseId? : number,
    from?:string,
    to?:string
}
class Entry {
    list(query:EntryQuery,current:number,size:number) {
        return server.post<ResponseData<Page<type_Entry>>>('entry' + `/${current}/${size}`,query)
    }

    add(entry:type_Entry | any) {
        return server.post<ResponseData<any>>(preFix, entry)
    }

    update(entry:type_Entry) {
        return server.put<ResponseData<any>>(preFix, entry)
    }

    deleteById(id:number) {
        return server.delete<ResponseData<any>>(`${preFix}/${id}`)
    }
}

export default new Entry()
