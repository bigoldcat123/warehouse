import server from '..'
const preFix = 'user'

export interface type_User {
    id?:number
    username:string
    password?:string
    name:string
    sex:string
    companyID:number
    position:string,
    phone:string
    priv:string
}

export const privList = ['所有数据','一般问题','严重问题','报警核准']
class User {
    list(current:number,size:number) {
        return server.get<ResponseData<Page<type_User>>>(preFix+ `?current=${current}&size=${size}`)
    }

    add(user:type_User | any) {
        return server.post<ResponseData<any>>(preFix, user)
    }

    update(user:type_User) {
        return server.put<ResponseData<any>>(preFix, user)
    }

    deleteById(id:number) {
        return server.delete<ResponseData<any>>(`${preFix}/${id}`)
    }
    kv() {
        return server.get<ResponseData<{key:string,value:string}[]>>(`${preFix}/kv`)
    }
    changePasswd(oldP:string,newP:string){
        return server.post<ResponseData<any>>(`${preFix}/passwd`,{oldP,newP})
    }
    getUsersByWarehouseId(warehouseId:number) {
        return server.get<ResponseData<any>>(`${preFix}/kv/getUsersByWarehouseId/${warehouseId}`)
    }
}

export default new User()