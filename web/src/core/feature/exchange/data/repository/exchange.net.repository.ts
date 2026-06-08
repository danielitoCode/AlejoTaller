import type {CupExchangeDTO} from "../dto/CupExchangeDTO";
import {ENV} from "../../../../infrastructure/env";


export class ExchangeNetRepository {
    private get baseUrl(): string {
        const url = (ENV.elToqueApiUrl || "").trim();
        if (!url) throw new Error("VITE_ELTOQUE_API_URL no esta configurado");
        return url.replace(/\/$/, "");
    }

    private get elToqueAuthKey(): string {
        const key = (ENV.elToqueApiKey || "").trim();
        if (!key) throw new Error("VITE_ELTOQUE_API_Key no esta configurado");
        return key;
    }

    async getExchangeToday(): Promise<CupExchangeDTO> {
        const res = await fetch(this.baseUrl, {
            method: "GET",
            headers: { "authorization" : "Bearer " + this.elToqueAuthKey }
        });

        const text =await res.text();
        let data: CupExchangeDTO;

        try {
            data = JSON.parse(text);
        } catch {
            throw new Error("El servidor de cambio El toque devolvió una respuesta inválida.");
        }

        return data
    }

    async getExchangeToADay(date: Date): Promise<CupExchangeDTO> {
        const url = this.baseUrl
            + "?date_from=" + this.formatDateToApiDateStructure(date)
            + "&date_to=" + this.formatDateToApiDateStructure(date)
        const res = await fetch(this.baseUrl, {
            method: "GET",
            headers: {
                "authorization" : "Bearer " + this.elToqueAuthKey,
            }
        });

        const text =await res.text();
        let data: CupExchangeDTO;

        try {
            data = JSON.parse(text);
        } catch {
            throw new Error("El servidor de cambio El toque devolvió una respuesta inválida.");
        }

        return data
    }

    async getExchangeToRankOfDay(dateFrom: Date, dateTo: Date): Promise<CupExchangeDTO[]> {
        const url = this.baseUrl
            + "?date_from=" + this.formatDateToApiDateStructure(dateFrom)
            + "&date_to=" + this.formatDateToApiDateStructure(dateTo)
        const res = await fetch(this.baseUrl, {
            method: "GET",
            headers: {
                "authorization" : "Bearer " + this.elToqueAuthKey,
            }
        });

        const text =await res.text();
        let data: CupExchangeDTO[];

        try {
            data = JSON.parse(text || "{}");
        } catch {
            throw new Error("El servidor de cambio El toque devolvió una respuesta inválida.");
        }

        return data
    }

    // Hay que arreglar
    private formatDateToApiDateStructure(dateTime: Date): string {
        const date = dateTime.getFullYear() + "-"
            + (dateTime.getMonth()+1) + "-"
            + dateTime.getUTCDate()

        const time = dateTime.getHours()
        return date + " " + time
    }
 }