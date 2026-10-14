import {GetData} from './baseApiClient'

export async function helloWorldFromBackend(){
    return await GetData('/test/hello')
}