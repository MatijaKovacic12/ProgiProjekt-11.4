import { GetData } from './baseApiClient'

export async function getCategories(){
    return GetData('/categories')
}

export async function getLocations(){
    return GetData('/locations')
}

export async function getAdditionalServices(){
    return GetData('/additional-services')
}
