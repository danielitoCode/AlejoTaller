import type {CupExchangeDTO} from "../dto/CupExchangeDTO";
import {ENV} from "../../../../infrastructure/env";


export class ExchangeNetRepository {
    private get baseUrl(): string {
        const url = (ENV.directorioCubanoApiUrl || "https://widgets.directoriocubano.info/api/tasas").trim();
        if (!url) throw new Error("VITE_DIRECTORIO_CUBANO_API_URL no esta configurado");
        return url.replace(/\/+$/, "");
    }

    /*private get elToqueAuthKey(): string {
        const key = (ENV.elToqueApiKey || "").trim();
        if (!key) throw new Error("VITE_EL_TOQUE_API_KEY no esta configurado");
        return key;
    }*/

    async getExchangeToday(): Promise<CupExchangeDTO> {
        return this.request()
    }

    async getExchangeToADay(date: Date): Promise<CupExchangeDTO> {
        const params = new URLSearchParams({
            date_from: this.formatDateToApiDateStructure(date),
            date_to: this.formatDateToApiDateStructure(date)
        });
        return this.request(params);
    }

    async getExchangeToRankOfDay(dateFrom: Date, dateTo: Date): Promise<CupExchangeDTO[]> {
        const params = new URLSearchParams({
            date_from: this.formatDateToApiDateStructure(dateFrom),
            date_to: this.formatDateToApiDateStructure(dateTo)
        });
        const data = await this.request(params);
        return Array.isArray(data) ? data : [data];
    }

    private formatDateToApiDateStructure(dateTime: Date): string {
        const year = dateTime.getFullYear();
        const month = String(dateTime.getMonth() + 1).padStart(2, "0");
        const day = String(dateTime.getDate()).padStart(2, "0");
        return `${year}-${month}-${day}`;
    }

    private async request(params?: URLSearchParams): Promise<CupExchangeDTO> {
        const url = params ? `${this.baseUrl}?${params.toString()}` : this.baseUrl;
        const res = await fetch(url, {
            method: "GET",
            headers: {
                Accept: "application/json"
            }
        });

        const text = await res.text();
        if (!res.ok) {
            throw new Error(`directorioCubano respondio ${res.status}: ${text || res.statusText}`);
        }

        try {
            return JSON.parse(text || "{}");
        } catch {
            throw new Error("El servidor de cambio directorioCubano devolvio una respuesta invalida.");
        }
    }
 }