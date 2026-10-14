export async function GetData(url){
    const res = await fetch(`/api${url}`);
    if(!res.ok){
        throw new Error("Error na backendu: "+res.status);
    }
        return res.json()
    
}