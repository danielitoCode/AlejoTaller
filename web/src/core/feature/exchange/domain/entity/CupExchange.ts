export interface CupExchange {
    id: string
    usd_reference: number
    euro_reference: number
}

export function createCupExchange(cupExchange: CupExchange): CupExchange {
    if(cupExchange.id || cupExchange.id.trim() === "")
        throw new Error("The value of exchange identifier cannot be empty")
    if(cupExchange.usd_reference < 0 || cupExchange.euro_reference < 0)
        throw new Error("The value of exchange must by positive")
    return {
        ...cupExchange
    }
}